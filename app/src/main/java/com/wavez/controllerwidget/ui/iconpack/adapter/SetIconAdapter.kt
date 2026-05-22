package com.wavez.controllerwidget.ui.iconpack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.R
import com.wavez.controllerwidget.databinding.ItemSetIconBinding
import com.wavez.controllerwidget.ui.iconpack.model.IconPackItemUi
import com.wavez.controllerwidget.ui.iconpack.model.SetIconState


class SetIconAdapter : ListAdapter<IconPackItemUi, SetIconAdapter.IconViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    var onActionClick: ((IconPackItemUi, SetIconState) -> Unit)? = null
    var onEditClick: ((IconPackItemUi) -> Unit)? = null

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        return IconViewHolder(
            ItemSetIconBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: IconViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.isEmpty()) {
            holder.bind(getItem(position))
        } else {
            holder.bindState(getItem(position))
        }
    }

    inner class IconViewHolder(
        private val binding: ItemSetIconBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: IconPackItemUi) {
            Glide.with(binding.ivPackIcon)
                .load(item.iconUrl)
                .into(binding.ivPackIcon)
            bindState(item)
        }

        fun bindState(item: IconPackItemUi) {
            val targetIcon = item.targetIcon
            binding.ivAppIcon.isVisible = targetIcon != null
            binding.tvPlus.visibility = if (targetIcon == null) View.VISIBLE else View.GONE
            binding.ivEdit.isVisible = item.isInstallable
            binding.ivEdit.setOnClickListener {
                onEditClick?.invoke(item)
            }

            if (targetIcon != null) {
                binding.ivAppIcon.setImageDrawable(targetIcon)
            } else {
                binding.ivAppIcon.setImageDrawable(null)
            }

            binding.containerAppIcon.animate()
                .alpha(if (item.isInstallable) 1f else 0.75f)
                .setDuration(120)
                .start()

            bindAction(item)
        }

        private fun bindAction(item: IconPackItemUi) {
            val context = binding.root.context
            val state = when {
                item.isApplied -> SetIconState.APPLIED
                item.canInstall -> SetIconState.INSTALL
                item.isInstallable -> SetIconState.UNLOCK
                else -> SetIconState.CHOOSE
            }

            binding.btnAction.animate().alpha(1f).setDuration(80).withEndAction {
                when (state) {
                    SetIconState.APPLIED -> {
                        binding.btnAction.setText(context.getString(R.string.applied))
                        binding.btnAction.setIcon(R.drawable.ic_check_circle)
                        binding.btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.success_high_em)
                        )
                        binding.btnAction.setButtonBackground(R.drawable.bg_button_applied)
                    }

                    SetIconState.UNLOCK -> {
                        binding.btnAction.setText(context.getString(R.string.unlock))
                        binding.btnAction.setIcon(R.drawable.ic_play)
                        binding.btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.primary_high_em)
                        )
                        binding.btnAction.setButtonBackground(R.drawable.bg_button_outline_1)
                    }

                    SetIconState.INSTALL -> {
                        binding.btnAction.setText(context.getString(R.string.install))
                        binding.btnAction.setIcon(R.drawable.ic_download_2)
                        binding.btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.primary_high_em)
                        )
                        binding.btnAction.setButtonBackground(R.drawable.bg_button_outline_1)
                    }

                    SetIconState.CHOOSE -> {
                        binding.btnAction.setText(context.getString(R.string.choose))
                        binding.btnAction.setIcon(0)
                        binding.btnAction.setTextColor(
                            ContextCompat.getColor(context, R.color.primary_high_em)
                        )
                        binding.btnAction.setButtonBackground(R.drawable.bg_button_outline_1)
                    }
                }

                binding.btnAction.setOnClickListener {
                    if (state != SetIconState.APPLIED) {
                        onActionClick?.invoke(item, state)
                    }
                }
            }.start()
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<IconPackItemUi>() {
        override fun areItemsTheSame(oldItem: IconPackItemUi, newItem: IconPackItemUi): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IconPackItemUi, newItem: IconPackItemUi): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: IconPackItemUi, newItem: IconPackItemUi): Any? {
            return if (oldItem.iconUrl == newItem.iconUrl && oldItem.appName == newItem.appName) {
                PAYLOAD_STATE
            } else {
                null
            }
        }
    }

    private companion object {
        const val PAYLOAD_STATE = "payload_state"
    }
}
