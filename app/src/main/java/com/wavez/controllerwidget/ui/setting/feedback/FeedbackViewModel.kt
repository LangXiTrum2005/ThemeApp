package com.wavez.controllerwidget.ui.setting.feedback

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavez.controllerwidget.core.extensions.sendFeedback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(

) : ViewModel() {

    companion object {
        const val MAX_IMAGE = 3
    }

    private var description = ""

    fun addDescription(str: String) {
        description = str
    }

    private val _uris = MutableStateFlow<List<Uri>>(emptyList())
    val uris: StateFlow<List<Uri>> = _uris

    fun updateListImage(uri: Uri, isRemove: Boolean = false) {
        viewModelScope.launch {
            val updatedUris = if (isRemove) {
                _uris.value - uri
            } else if (_uris.value.size <= MAX_IMAGE) {
                listOf(uri) + _uris.value
            } else {
                _uris.value
            }
            _uris.value = updatedUris
        }
    }

    private fun getFeedback(): Feedback {
        return Feedback(description, _uris.value)
    }

    fun submitFeedback(activity: Activity) {
        val feedback = getFeedback()
        activity.sendFeedback(feedback)
    }

    fun hasImages(): Boolean {
        return _uris.value.isNotEmpty()
    }
}