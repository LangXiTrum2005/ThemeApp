package com.wavez.themewallpaper.ui.home.fragment.wallpaper.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.themewallpaper.core.base.UiState
import com.wavez.themewallpaper.data.model.WallpaperCategoryModel
import com.wavez.themewallpaper.data.repository.WallpaperRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val wallpaperRepository: WallpaperRepository,
) : ViewModel() {

    private val _categoriesResult =
        MutableStateFlow<UiState<List<WallpaperCategoryModel>>>(UiState.Idle)
    val categoriesResult: StateFlow<UiState<List<WallpaperCategoryModel>>> = _categoriesResult

    init {
        getCategories()
    }

    fun getCategories() {
        if (_categoriesResult.value is UiState.Loading) return
        viewModelScope.launch {
            _categoriesResult.value = UiState.Loading
            val categories = wallpaperRepository.getCategoriesUnlimited()
            categories.onSuccess {
                _categoriesResult.value = UiState.Data(it)
            }.onFailure {
                val message = it.message.orEmpty().ifBlank { "Unknown error" }
                _categoriesResult.value = UiState.Error(it, message)
            }
        }
    }

}
