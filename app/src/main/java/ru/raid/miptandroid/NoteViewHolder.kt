package ru.raid.miptandroid

import android.net.Uri
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.note_card.view.noteDate
import kotlinx.android.synthetic.main.note_card.view.noteImage
import kotlinx.android.synthetic.main.note_card.view.noteOptions
import kotlinx.android.synthetic.main.note_card.view.noteText
import ru.raid.miptandroid.db.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class NoteViewHolder(itemView: View, private val listener: NoteActionListener) : RecyclerView.ViewHolder(itemView) {
    private lateinit var currentNote: Note

    init {
        itemView.setOnClickListener {
            showDetailedView(currentNote)
        }
        itemView.noteOptions.setOnClickListener {
            showPopupMenu(currentNote, it)
        }
    }

    fun bind(note: Note) {
        currentNote = note
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        with(itemView) {
            noteDate.text = dateFormat.format(Date(note.date))
            noteText.text = note.text
            Picasso.get().load(note.imageUri)
                .fit()
                .centerInside()
                .into(noteImage)
        }
    }

    private fun showPopupMenu(note: Note, callingView: View) {
        val menu = PopupMenu(callingView.context, callingView)
        menu.inflate(R.menu.menu_note_popup)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menuSync -> {
                    listener.onSync(note)
                    true
                }
                R.id.menuShare -> {
                    listener.onShare(note)
                    true
                }
                R.id.menuDelete -> {
                    listener.onDelete(note)
                    true
                }
                else -> false
            }
        }

        menu.show()
    }

    private fun showDetailedView(note: Note) {
        listener.onSelect(note)
    }
}
