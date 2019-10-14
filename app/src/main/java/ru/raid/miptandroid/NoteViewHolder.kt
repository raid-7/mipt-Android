package ru.raid.miptandroid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.note_card.view.*
import java.text.SimpleDateFormat

class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var currentNote: Note

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
            noteDate.text = dateFormat.format(note.date.time)
            noteText.text = note.text
            noteImage.setImageResource(
                resources.getIdentifier("p${note.imageId}", "drawable", context.packageName)
            )
        }
    }

    private fun showDetailedView(note: Note) {
        itemView.context.let { ctx ->
            ctx.startActivity(DetailedNoteActivity.getIntent(ctx, note.id))
        }
    }
}
