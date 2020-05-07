package ru.raid.miptandroid

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.NoteDao

interface SyncService {
    @GET("/shared-notes/{id}/data")
    suspend fun fetchNoteData(@Path("id") id: String): NoteData

    @GET("/shared-notes/{id}/image")
    @Streaming
    suspend fun fetchNoteImage(@Path("id") id: String): ResponseBody

    @Multipart
    @POST("/shared-notes")
    suspend fun shareNote(@Part("data") data: NoteData, image: MultipartBody.Part): String
}
