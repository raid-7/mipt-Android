package ru.raid.miptandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.raid.miptandroid.db.Note


interface NoteActionListener {
    fun onSelect(note: Note)
    fun onDelete(note: Note)
    fun onShare(note: Note)
    fun onSync(note: Note)
}

class NoteAdapter(private val listener: NoteActionListener) :
    RecyclerView.Adapter<NoteViewHolder>() {
    var notes: List<Note> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return NoteViewHolder(view, listener)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }
}
