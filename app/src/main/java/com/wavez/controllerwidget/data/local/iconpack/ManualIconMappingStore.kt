package com.wavez.controllerwidget.data.local.iconpack

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.wavez.controllerwidget.data.model.ManualIconMapping
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class ManualIconMappingStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi,
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("icon_manual_mappings", Context.MODE_PRIVATE)
    }

    private val listType = Types.newParameterizedType(List::class.java, ManualIconMapping::class.java)
    private val adapter by lazy { moshi.adapter<List<ManualIconMapping>>(listType) }

    fun getMappings(iconPackId: String): List<ManualIconMapping> {
        val json = prefs.getString(iconPackId, null) ?: return emptyList()
        return runCatching { adapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())
    }

    fun saveMapping(iconPackId: String, iconId: String, packageName: String, appName: String) {
        val current = getMappings(iconPackId).toMutableList()
        current.removeAll { it.iconId == iconId }
        current.add(ManualIconMapping(iconPackId = iconPackId, iconId = iconId, packageName = packageName, appName = appName))
        prefs.edit { putString(iconPackId, adapter.toJson(current)) }
    }

    fun removeMapping(iconPackId: String, iconId: String) {
        val current = getMappings(iconPackId).toMutableList()
        if (current.removeAll { it.iconId == iconId }) {
            prefs.edit { putString(iconPackId, adapter.toJson(current)) }
        }
    }
}
