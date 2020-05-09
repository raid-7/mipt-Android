package ru.raid.miptandroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import java.util.*

class NoteFlows(private val context: Context, private val lifecycle: Lifecycle) {
    private val recognizer = TextRecognizer(context)
    private val nonReadyNotes = mutableSetOf<Long>()

    fun addNewNote(imageUri: Uri) {
        val noteDao = AppDatabase.getInstance(context).noteDao()
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            val note = createWithImage(imageUri)
            val noteId = noteDao.insert(note)

            synchronized(nonReadyNotes) {
                nonReadyNotes.add(noteId)
            }

            val text = recognizer.recognizeText(imageUri)
            noteDao.update(Note(noteId, text, note.imagePath, note.date))

            synchronized(nonReadyNotes) {
                nonReadyNotes.remove(noteId)
            }
        }
    }

    fun addReadyNote(data: NoteData, imageUri: Uri, inSyncId: String) {
        val noteDao = AppDatabase.getInstance(context).noteDao()
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            val note = createWithData(data, imageUri, inSyncId)
            val noteId = noteDao.insert(note)
        }
    }

    fun deleteNote(noteId: Long) {
        val noteDao = AppDatabase.getInstance(context).noteDao()
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            noteDao.remove(noteId)
        }
    }

    fun share(note: Note) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, note.text)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    fun isReady(note: Note): Boolean = synchronized(nonReadyNotes) {
        note.id !in nonReadyNotes
    }

    private fun createWithData(data: NoteData, imageUri: Uri, inSyncId: String) =
        Note(
            0,
            data.text,
            imageUri.toString(),
            data.date,
            inSyncId
        )

    private fun createWithImage(imageUri: Uri) =
        Note(
            0,
            "",
            imageUri.toString(),
            Calendar.getInstance().timeInMillis
        )
}