package ru.raid.miptandroid


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_detailed_note.noteImage
import kotlinx.android.synthetic.main.fragment_detailed_note.noteText


fun Note.getImageResource(context: Context) =
    context.resources.getIdentifier("p${imageId}", "drawable", context.packageName)

class DetailedNoteFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detailed_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getInt(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val note = noteRepo[noteId]
        bindNote(note)
    }

    private fun bindNote(note: Note) {
        noteImage.setImageResource(note.getImageResource(context!!))
        noteText.text = note.text
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(note: Note): DetailedNoteFragment {
            val fragment = DetailedNoteFragment()
            fragment.arguments = Bundle().apply {
                putInt(NOTE_ID, note.id)
            }
            return fragment
        }
    }
}
