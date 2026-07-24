package com.wavez.themewallpaper.ui.widgets.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.data.model.widget.WidgetsModel
import com.wavez.themewallpaper.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetSuccessViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _suggestedWidgets = MutableStateFlow<List<WidgetsModel>>(emptyList())
    val suggestedWidgets: StateFlow<List<WidgetsModel>> = _suggestedWidgets.asStateFlow()

    fun loadSameCategoryWidgets(categoryId: String, excludeId: String) {
        viewModelScope.launch {
            widgetRepository.getAllWidgets()
                .onSuccess { widgets ->
                    _suggestedWidgets.value = widgets.filter {
                        it.categoryId == categoryId && it.id != excludeId
                    }
                }
        }
    }

    fun saveAddedWidget(widget: WidgetsModel) {
        viewModelScope.launch {
            widgetRepository.saveAddedWidget(widget)
        }
    }
}
