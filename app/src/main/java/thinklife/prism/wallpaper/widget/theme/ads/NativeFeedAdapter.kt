/*
 * Copyright (c) 2026 1Tabb. All rights reserved.
 * https://1tabb.com
 *
 * SPDX-License-Identifier: LicenseRef-1Tabb-Proprietary
 *
 * Proprietary and confidential. Contains trade secrets of 1Tabb.
 * Unauthorized copying, modification, distribution, reverse engineering,
 * decompilation or disassembly of this file, via any medium, is strictly
 * prohibited except with prior written permission of 1Tabb.
 */

package thinklife.prism.wallpaper.widget.theme.ads

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adshub.monetization.config.ConfigManager
import thinklife.prism.wallpaper.widget.theme.databinding.ItemNativeFeedBinding

/** Giữ adapter nội dung nguyên vẹn; vị trí ads không được lọt vào index dữ liệu. */
class NativeFeedAdapter<VH : RecyclerView.ViewHolder>(
    private val content: RecyclerView.Adapter<VH>,
    private val screenId: String,
    private val slotId: String,
    private val owner: LifecycleOwner,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), DefaultLifecycleObserver {
    private val attachedAds = mutableSetOf<NativeHolder>()
    private val ownedAds = mutableSetOf<NativeHolder>()
    private var registered = false
    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = refresh()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) =
            changeContent(positionStart, itemCount, null)
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
            changeContent(positionStart, itemCount, payload)
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = refresh()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = refresh()
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = refresh()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refresh() = notifyDataSetChanged()

    private fun changeContent(start: Int, count: Int, payload: Any?) {
        // Chọn một nội dung không được rebind toàn list và khởi động lại các request ads.
        for (position in start until start + count) {
            notifyItemChanged(position + position / CONTENT_INTERVAL, payload)
        }
    }

    override fun getItemCount(): Int = content.itemCount + content.itemCount / CONTENT_INTERVAL
    private fun isAd(position: Int) = (position + 1) % GROUP_SIZE == 0
    private fun contentPosition(position: Int) = position - position / GROUP_SIZE
    override fun getItemViewType(position: Int): Int =
        if (isAd(position)) AD_VIEW_TYPE else content.getItemViewType(contentPosition(position))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == AD_VIEW_TYPE) {
            NativeHolder(ItemNativeFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else content.onCreateViewHolder(parent, viewType)

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NativeHolder) {
            ownedAds.add(holder)
            holder.binding.root.tag = AdsSlot.listTag(slotId)
            if (holder in attachedAds) render(holder)
        } else content.onBindViewHolder(holder as VH, contentPosition(position))
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder is NativeHolder) onBindViewHolder(holder, position)
        else content.onBindViewHolder(holder as VH, contentPosition(position), payloads)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        if (!registered) {
            content.registerAdapterDataObserver(observer)
            owner.lifecycle.addObserver(this)
            registered = true
        }
        content.onAttachedToRecyclerView(recyclerView)
        (recyclerView.layoutManager as? GridLayoutManager)?.let { grid ->
            grid.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = if (isAd(position)) grid.spanCount else 1
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if (holder is NativeHolder) {
            ownedAds.add(holder)
            attachedAds.add(holder)
            render(holder)
        } else content.onViewAttachedToWindow(holder as VH)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is NativeHolder) {
            attachedAds.remove(holder)
            retainReadyOrRelease(holder)
        } else content.onViewDetachedFromWindow(holder as VH)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is NativeHolder) {
            attachedAds.remove(holder)
            release(holder)
            ownedAds.remove(holder)
        } else content.onViewRecycled(holder as VH)
    }

    private fun render(holder: NativeHolder) {
        if (!owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
        if (holder.state != LoadState.IDLE) return
        holder.state = LoadState.LOADING
        val token = Any()
        holder.requestToken = token
        holder.job = owner.lifecycleScope.launch {
            try {
                // Tránh tạo request khi holder chỉ lướt qua màn hình trong lúc fling.
                delay(ATTACH_DELAY_MS)
                val configured = withTimeoutOrNull(CONFIG_WAIT_MS) {
                    while (!ConfigManager.isInitialConfigLoaded || AdsBootstrap.isFullScreenAdActive()) {
                        delay(POLL_MS)
                    }
                    true
                } ?: false
                if (!configured) return@launch
                val lease = withTimeoutOrNull(SLOT_WAIT_MS) {
                    var available = AdsBootstrap.availableFeedSlot(slotId)
                    while (available == null) {
                        delay(POLL_MS)
                        available = AdsBootstrap.availableFeedSlot(slotId)
                    }
                    available
                } ?: return@launch

                // Callback muộn chỉ chạm container của lần tải cũ, không hồi sinh skeleton trên row.
                val container = ItemNativeFeedBinding.inflate(
                    LayoutInflater.from(holder.binding.root.context), holder.binding.root, false,
                ).root
                holder.container = container
                holder.activeSlot = lease
                holder.binding.root.addView(container)
                if (!AdsBootstrap.renderSlotNow(screenId, lease, container)) return@launch
                val rendered = withTimeoutOrNull(AdsBootstrap.nativeSlotTimeoutMs(lease) + RENDER_GRACE_MS) {
                    while (!AdsBootstrap.isNativeSlotRendered(container)) delay(POLL_MS)
                    true
                } ?: false
                if (rendered) holder.state = LoadState.READY
            } finally {
                // Job bị hủy có thể hoàn tất sau lần bind mới; không dọn request mới của holder.
                if (holder.requestToken === token) {
                    holder.job = null
                    if (holder.state != LoadState.READY) {
                        clearContainer(holder)
                        holder.state = LoadState.FAILED
                    }
                }
            }
        }
    }

    private fun release(holder: NativeHolder) {
        holder.requestToken = null
        holder.job?.cancel()
        holder.job = null
        clearContainer(holder)
        holder.state = LoadState.IDLE
    }

    private fun clearContainer(holder: NativeHolder) {
        val id = holder.activeSlot
        val container = holder.container
        if (id != null && container != null && AdsBootstrap.isSlotOwnedBy(id, container)) {
            AdsBootstrap.releaseSlot(id)
        }
        holder.binding.root.removeAllViews()
        holder.activeSlot = null
        holder.container = null
    }

    override fun onResume(owner: LifecycleOwner) = attachedAds.forEach(::render)

    override fun onPause(owner: LifecycleOwner) {
        // Sticky cache giữ ad trong SDK; gỡ view của tab cũ để tab mới không bind trùng ad đó.
        ownedAds.forEach(::release)
    }

    private fun retainReadyOrRelease(holder: NativeHolder) {
        if (holder.state == LoadState.READY) return
        if (holder.container?.let(AdsBootstrap::isNativeSlotRendered) == true) {
            holder.state = LoadState.READY
        } else release(holder)
    }

    override fun onDestroy(owner: LifecycleOwner) = dispose()

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        content.onDetachedFromRecyclerView(recyclerView)
        dispose()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    private fun dispose() {
        ownedAds.forEach(::release)
        ownedAds.clear()
        attachedAds.clear()
        if (registered) content.unregisterAdapterDataObserver(observer)
        owner.lifecycle.removeObserver(this)
        registered = false
    }

    private class NativeHolder(val binding: ItemNativeFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        var state = LoadState.IDLE
        var activeSlot: String? = null
        var container: ViewGroup? = null
        var job: Job? = null
        var requestToken: Any? = null
    }

    private enum class LoadState { IDLE, LOADING, READY, FAILED }

    private companion object {
        const val POLL_MS = 100L
        const val ATTACH_DELAY_MS = 200L
        const val CONFIG_WAIT_MS = 6_000L
        const val SLOT_WAIT_MS = 6_000L
        const val RENDER_GRACE_MS = 2_000L
        const val CONTENT_INTERVAL = 6
        const val GROUP_SIZE = CONTENT_INTERVAL + 1
        const val AD_VIEW_TYPE = Int.MIN_VALUE
    }
}
