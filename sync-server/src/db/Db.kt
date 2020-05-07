package ru.raid.miptandroid.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.DEFAULT_REPETITION_ATTEMPTS
import org.jetbrains.exposed.sql.transactions.transaction
import ru.raid.miptandroid.NoteData
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object SharedNotes : Table("shared_notes") {
    val id = varchar("id", 32)
    val text = text("text")
    val image = blob("image")
    val creationTimestamp = long("creation_timestamp")
    val shareTimestamp = long("share_timestamp")

    override val primaryKey = PrimaryKey(id)
}


class Db(url: String? = null, user: String? = null, password: String? = null) {
    private val conn: Database

    init {
        val finalUrl = url ?: "jdbc:sqlite:bot-test.db"
        val driver = try {
            DriverManager.getDriver(finalUrl).javaClass.name
        } catch (_: SQLException) {
            when {
                finalUrl.contains("sqlite") -> "org.sqlite.JDBC"
                finalUrl.contains("postgres") -> "org.postgresql.Driver"
                else -> throw RuntimeException("Cannot determine jdbc driver")
            }
        }
        conn = Database.connect(finalUrl, driver, user = user ?: "", password = password ?: "")

        transaction {
            SchemaUtils.create(SharedNotes)
        }
    }

    fun addSharedNote(
        idGenerator: () -> String,
        text: String, image: ByteArray, timestamp: Long,
        numRetries: Int = 5
    ): String? {
        val shareTime = System.currentTimeMillis()
        for (i in 1..numRetries) {
            val id = idGenerator()
            try {
                transaction { doInsert(id, text, image, timestamp, shareTime) }
                return id
            } catch (exc: SQLException) {
            }
        }
        return null
    }

    fun loadNoteData(id: String): NoteData? = transaction {
        SharedNotes
            .slice(SharedNotes.text, SharedNotes.creationTimestamp)
            .select {
                SharedNotes.id.eq(id)
            }.map {
                NoteData(it[SharedNotes.text], it[SharedNotes.creationTimestamp])
            }.firstOrNull()
    }

    fun loadNoteImage(id: String): ByteArray? = transaction {
        SharedNotes
            .slice(SharedNotes.image)
            .select {
                SharedNotes.id.eq(id)
            }.map {
                it[SharedNotes.image].bytes
            }.firstOrNull()
    }

    fun removeOldNotes(age: Long) {
        val before = System.currentTimeMillis() - age
        transaction {
            SharedNotes.deleteWhere {
                SharedNotes.shareTimestamp.less(before)
            }
        }
    }

    fun countNotes(): Long =
        transaction {
            SharedNotes.selectAll().count()
        }

    private fun Transaction.doInsert(
        id_: String,
        text_: String,
        image_: ByteArray,
        creationTime_: Long,
        shareTime_: Long
    ) {
        SharedNotes.insert {
            it[id] = id_
            it[text] = text_
            it[image] = ExposedBlob(image_)
            it[creationTimestamp] = creationTime_
            it[shareTimestamp] = shareTime_
        }
    }

    private fun <T> transaction(statement: Transaction.() -> T): T = transaction(
        Connection.TRANSACTION_SERIALIZABLE, DEFAULT_REPETITION_ATTEMPTS, conn, statement
    )
}
