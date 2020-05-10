package ru.raid.miptandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.raid.miptandroid.db.Note


interface NoteActionListener {
    fun onSelect(note: Note)
    fun onDelete(note: Note)
    fun onShare(note: Note)
    fun onSync(note: Note)
}

class NoteAdapter(private val listener: NoteActionListener, private val scope: CoroutineScope) :
    RecyclerView.Adapter<NoteViewHolder>() {
    var notes: List<Note> = emptyList()
        set(value) {
            val old = field
            field = value
            applyChanges(old, value)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return NoteViewHolder(view, listener)
    }

    override fun getItemCount(): Int = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    private fun applyChanges(old: List<Note>, new: List<Note>) {
        if (old.size > DIFF_LIMIT || new.size > DIFF_LIMIT) {
            notifyDataSetChanged()
            return
        }
        scope.launch(Dispatchers.IO) {
            val diff = DiffUtil.calculateDiff(NoteDiffCallback(old, new), false)
            withContext(Dispatchers.Main) {
                diff.dispatchUpdatesTo(this@NoteAdapter)
            }
        }
    }

    private companion object {
        const val DIFF_LIMIT = 200
    }
}
