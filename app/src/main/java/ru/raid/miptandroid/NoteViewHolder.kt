package ru.raid.miptandroid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.note_card.view.noteDate
import kotlinx.android.synthetic.main.note_card.view.noteImage
import kotlinx.android.synthetic.main.note_card.view.noteText
import kotlinx.android.synthetic.main.note_card.view.noteTitle
import ru.raid.miptandroid.db.Note
import java.text.SimpleDateFormat
import java.util.Date

class NoteViewHolder(itemView: View, private val listener: NoteSelectionListener) : RecyclerView.ViewHolder(itemView) {
    private lateinit var currentNote: Note

    init {
        itemView.setOnClickListener {
            showDetailedView(currentNote)
        }
    }

    fun bind(note: Note) {
        currentNote = note
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        with(itemView) {
            noteTitle.text = note.title
            noteDate.text = dateFormat.format(Date(note.date))
            noteText.text = note.text
            noteImage.setImageBitmap(note.bitmap)
        }
    }

    private fun showDetailedView(note: Note) {
        listener(note)
    }
}
