package com.wavez.controllerwidget.ui.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.base.UiState
import com.wavez.controllerwidget.data.model.LanguageModel
import com.wavez.controllerwidget.data.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
) : ViewModel() {

    private val _selectedUiLanguage = MutableLiveData<LanguageModel>()
    val selectedUiLanguage: LiveData<LanguageModel> = _selectedUiLanguage

    private val _uiLanguageState = MutableLiveData<UiState<List<LanguageModel>>>()
    val uiLanguageState: LiveData<UiState<List<LanguageModel>>> = _uiLanguageState

    fun setLanguageDefault(languages: List<LanguageModel>) {
        val configLanguage = languageRepository.getConfigLanguageCode()
        languages.find { it.code == configLanguage }?.let { onLanguageChanged(it) }
    }

    fun getUiLanguages() {
        viewModelScope.launch {
            _uiLanguageState.value = UiState.Loading
            _uiLanguageState.value = UiState.Data(languageRepository.getLanguages())
        }
    }

    fun onLanguageChanged(uiLanguage: LanguageModel) {
        _selectedUiLanguage.value = uiLanguage
    }

    fun getConfig(): String = languageRepository.getConfigLanguageCode()

    fun saveConfig() {
        viewModelScope.launch {
            _selectedUiLanguage.value?.let { languageRepository.saveLanguage(it) }
        }
    }
}
