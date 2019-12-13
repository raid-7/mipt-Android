package ru.raid.miptandroid

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import java.io.File
import java.util.Calendar

class NoteFlows(private val context: Context, private val lifecycle: Lifecycle) {
    private val recognizer = TextRecognizer(context)
    private val nonReadyNotes = mutableSetOf<Long>()

    fun addNewNote(file: File) {
        val noteDao = AppDatabase.getInstance(context).noteDao()
        lifecycle.coroutineScope.launch(Dispatchers.IO) {
            val note = createWithImage(file)
            val noteId = noteDao.insert(note)

            nonReadyNotes.add(noteId)

            val text = recognizer.recognizeText(file)
            noteDao.update(Note(noteId, text, note.imagePath, note.date))

            nonReadyNotes.remove(noteId)
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

    fun isReady(note: Note): Boolean = note.id !in nonReadyNotes

    private fun createWithImage(image: File) =
        Note(
            0,
            "",
            image.absolutePath,
            Calendar.getInstance().timeInMillis
        )
}