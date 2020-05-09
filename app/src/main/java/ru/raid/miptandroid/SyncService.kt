package ru.raid.miptandroid

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface SyncService {
    @GET("/shared-notes/{id}/data")
    suspend fun fetchNoteData(@Path("id") id: String): NoteData

    @GET("/shared-notes/{id}/image")
    @Streaming
    suspend fun fetchNoteImage(@Path("id") id: String): ResponseBody

    @Multipart
    @POST("/shared-notes")
    suspend fun shareNote(@Part("data") data: NoteData, @Part image: MultipartBody.Part): String
}
