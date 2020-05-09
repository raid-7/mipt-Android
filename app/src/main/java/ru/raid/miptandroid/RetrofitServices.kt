package ru.raid.miptandroid

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetrofitServices private constructor(private val retrofit: Retrofit) {
    val syncService: SyncService = retrofit.create(SyncService::class.java)

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
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val instance = RetrofitServices(retrofit)
                INSTANCE = instance
                return instance
            }
        }
    }
}
