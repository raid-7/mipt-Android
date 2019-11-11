package ru.raid.miptandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


typealias NoteSelectionListener = (Note) -> Unit

class NoteAdapter(private val repo: NoteRepository, private val listener: NoteSelectionListener) :
    RecyclerView.Adapter<NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return NoteViewHolder(view, listener)
    }

    override fun getItemCount(): Int = repo.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(repo[position])
    }
}
