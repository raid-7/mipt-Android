package ru.raid.miptandroid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detailed_note.noteImage
import kotlinx.android.synthetic.main.activity_detailed_note.noteText

fun Note.getImageResource(context: Context) =
    context.resources.getIdentifier("p${imageId}", "drawable", context.packageName)

class DetailedNoteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_note)

        val note = noteRepo[intent.getIntExtra(NOTE_ID, -1)]
        bindNote(note)
    }

    private fun bindNote(note: Note) {
        noteImage.setImageResource(note.getImageResource(this))
        noteText.text = note.text
        supportActionBar?.apply {
            title = note.title
        }
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun getIntent(context: Context, noteId: Int) =
            Intent(context, DetailedNoteActivity::class.java).putExtra(NOTE_ID, noteId)
    }
}
