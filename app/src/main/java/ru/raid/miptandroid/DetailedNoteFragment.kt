package ru.raid.miptandroid


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_detailed_note.noteImage
import kotlinx.android.synthetic.main.fragment_detailed_note.noteText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note


class DetailedNoteFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detailed_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val noteDao = AppDatabase.getInstance(requireContext()).noteDao()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val note = noteDao.get(noteId)
            withContext(Dispatchers.Main) {
                bindNote(checkNotNull(note) { "No such note" })
            }
        }
    }

    private fun bindNote(note: Note) {
        noteImage.setImageBitmap(note.bitmap)
        noteText.text = note.text
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(note: Note): DetailedNoteFragment {
            val fragment = DetailedNoteFragment()
            fragment.arguments = Bundle().apply {
                putLong(NOTE_ID, note.id)
            }
            return fragment
        }
    }
}
