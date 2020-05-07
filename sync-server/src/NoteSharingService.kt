package ru.raid.miptandroid

import io.ktor.config.ApplicationConfig
import ru.raid.miptandroid.db.Db
import java.security.SecureRandom
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NoteSharingService(conf: ApplicationConfig) {
    private val executor = Executors.newScheduledThreadPool(1)
    private val random = SecureRandom()
    private val db: Db

    init {
        db = initDb(conf)
        executor.scheduleAtFixedRate(::regularCleanup, 1, CLEANUP_RATE, TimeUnit.MINUTES);
    }

    fun getNoteData(id: String): NoteData {
        return db.loadNoteData(id) ?: throw NotFoundException("No such note")
    }

    fun getNoteImage(id: String): ByteArray {
        return db.loadNoteImage(id) ?: throw NotFoundException("No such note")
    }

    private fun regularCleanup() {
        db.removeOldNotes(MAX_NOTE_AGE)
    }

    private fun genId() =
        (1..ID_LENGTH)
            .map {
                ID_ALPHABET[random.nextInt(ID_ALPHABET.length)]
            }
            .joinToString("")

    private companion object {
        const val MAX_NOTE_AGE = 30 * 86_400_000L // milliseconds
        const val CLEANUP_RATE = 60L // minutes
        val ID_ALPHABET = ('A'..'Z').joinToString("") + (0..9).joinToString("")
        val ID_LENGTH = 8

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