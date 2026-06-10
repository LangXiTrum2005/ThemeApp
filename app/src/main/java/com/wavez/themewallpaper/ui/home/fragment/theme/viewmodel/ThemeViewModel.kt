package com.wavez.themewallpaper.ui.home.fragment.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.theme.ThemeCategoryModel
import com.wavez.themewallpaper.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    private val _categoriesResult =
        MutableStateFlow<UiState<List<ThemeCategoryModel>>>(UiState.Idle)
    val categoriesResult: StateFlow<UiState<List<ThemeCategoryModel>>> =
        _categoriesResult.asStateFlow()

    init {
        getCategories()
    }

    fun getCategories(forceRefresh: Boolean = false) {
        if (_categoriesResult.value is UiState.Loading) return
        viewModelScope.launch {
            _categoriesResult.value = UiState.Loading
            themeRepository.getThemeCategories(forceRefresh)
                .onSuccess { categories ->
                    _categoriesResult.value = UiState.Data(categories)
                }
                .onFailure { error ->
                    val message = error.message.orEmpty().ifBlank { "Unknown error" }
                    _categoriesResult.value = UiState.Error(error, message)
                }
        }
    }
}
