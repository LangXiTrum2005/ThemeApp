//package com.example.m52_reverseaudio_challenge.reverseaudio.data.db.entity
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.example.m52_reverseaudio_challenge.reverseaudio.feature.model.AudioRecord
//import com.google.gson.annotations.SerializedName
//
//@Entity(tableName = "audio_record")
//data class AudioRecordEntity(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int,
//    val fileName: String,
//    @SerializedName("dateTime")
//    val dateTime: String,
//    @SerializedName("duration")
//    val duration: Long,
//    @SerializedName("originalPath")
//    val originalPath: String,
//    @SerializedName("reversedPath")
//    val reversedPath: String,
//) {
//    companion object {
//
//        fun AudioRecordEntity.toDomain(): AudioRecord = AudioRecord(
//            id = id,
//            fileName = fileName,
//            dateTime = dateTime,
//            duration = duration,
//            originalPath = originalPath,
//            reversedPath = reversedPath
//        )
//
//    }
//}