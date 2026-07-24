package com.wavez.themewallpaper.ui.home.fragment.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.widget.WidgetCategoryModel
import com.wavez.themewallpaper.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(
    private val widgetRepository: WidgetRepository,
) : ViewModel() {

    private val _categoriesState =
        MutableStateFlow<UiState<List<WidgetCategoryModel>>>(UiState.Idle)
    val categoriesState: StateFlow<UiState<List<WidgetCategoryModel>>> =
        _categoriesState.asStateFlow()

    init {
        getCategories()
    }

    fun getCategories(forceRefresh: Boolean = false) {
        if (_categoriesState.value is UiState.Loading) return
        viewModelScope.launch {
            _categoriesState.value = UiState.Loading
            widgetRepository.getCategories(forceRefresh)
                .onSuccess { categories ->
                    _categoriesState.value = UiState.Data(categories)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _categoriesState.value = UiState.Error(error, message)
                }
        }
    }
}
