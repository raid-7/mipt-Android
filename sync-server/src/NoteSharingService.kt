package ru.raid.miptandroid

import io.ktor.config.ApplicationConfig
import ru.raid.miptandroid.db.Db
import java.security.SecureRandom
import java.sql.SQLException
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

    fun getNoteData(id: String): NoteData = sqlSafe {
        return db.loadNoteData(id) ?: throw NotFoundException("No such note")
    }

    fun getNoteImage(id: String): ByteArray = sqlSafe {
        return db.loadNoteImage(id) ?: throw NotFoundException("No such note")
    }

    fun addSharedNote(data: NoteData, image: ByteArray): String = sqlSafe {
        return db.addSharedNote(::genId, data.text, image, data.date)
            ?: throw ServiceUnavailableException("Try again")
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
        const val ID_LENGTH = 8
        val ID_ALPHABET = ('A'..'Z').joinToString("") + (0..9).joinToString("")

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

    private inline fun <R> sqlSafe(func: () -> R): R {
        try {
            return func()
        } catch (exc: SQLException) {
            exc.printStackTrace()
            throw InternalServerErrorException()
        }
    }
}
