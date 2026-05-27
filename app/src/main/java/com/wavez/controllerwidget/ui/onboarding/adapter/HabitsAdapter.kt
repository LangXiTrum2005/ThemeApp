package com.wavez.controllerwidget.ui.onboarding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.wavez.controllerwidget.ui.onboarding.model.Habits
import com.wavez.controllerwidget.core.base.BaseRecyclerViewAdapter
import com.wavez.controllerwidget.core.extensions.setDrawableStart
import com.wavez.controllerwidget.databinding.ItemHabitsBinding

class HabitsAdapter : BaseRecyclerViewAdapter<Habits, ItemHabitsBinding>() {

    private var habitsSelects = mutableListOf<Habits>()

    var onItemSelected: (() -> Unit)? = null

    override fun providesItemViewBinding(parent: ViewGroup, viewType: Int): ItemHabitsBinding {
        return ItemHabitsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun bindData(
        binding: ItemHabitsBinding,
        data: Habits,
        position: Int,
        context: Context
    ) {
        Glide.with(context).load(data.image).into(binding.imageHabit)
        binding.tvHabitsName.text = context.getString(data.name)
        binding.tvHabitsName.setDrawableStart(data.icon)

        binding.root.setOnClickListener {
            if (habitsSelects.contains(data)) {
                habitsSelects.remove(data)
                binding.root.isSelected = false
            } else {
                habitsSelects.add(data)
                binding.root.isSelected = true
            }
            onItemSelected?.invoke()
        }
    }

    fun getSelectedItems(): List<Habits> {
        return habitsSelects
    }

}
