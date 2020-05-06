package ru.raid.miptandroid

import io.ktor.config.ApplicationConfig
import ru.raid.miptandroid.db.Db
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NoteSharingService(conf: ApplicationConfig) {
    private val executor = Executors.newScheduledThreadPool(1)
    private val db: Db

    init {
        db = initDb(conf)
        executor.scheduleAtFixedRate(::regularCleanup, 1, CLEANUP_RATE, TimeUnit.MINUTES);
    }



    private fun regularCleanup() {
        db.removeOldNotes(MAX_NOTE_AGE)
    }

    private companion object {
        const val MAX_NOTE_AGE = 30 * 86_400_000L // milliseconds
        const val CLEANUP_RATE = 60L // minutes

        fun initDb(conf: ApplicationConfig): Db {
            val dbConf = conf.config("db")
            return dbConf.run {
                Db(
                    propertyOrNull("url")?.getString(),
                    propertyOrNull("user")?.getString(),
                    propertyOrNull("password")?.getString()
                )
            }
        }
    }
}