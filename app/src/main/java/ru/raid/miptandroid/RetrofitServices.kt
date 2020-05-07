package ru.raid.miptandroid

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitServices private constructor(private val retrofit: Retrofit) {
    val syncService = retrofit.create(SyncService::class.java)

    companion object {
        @Volatile
        private var INSTANCE: RetrofitServices? = null

        fun getInstance(context: Context): RetrofitServices {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val address = context.getString(R.string.sync_service)
                val retrofit = Retrofit.Builder()
                    .baseUrl(address)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val instance = RetrofitServices(retrofit)
                INSTANCE = instance
                return instance
            }
        }
    }
}
