package ru.raid.miptandroid


import android.content.res.Configuration
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_detailed_note.noteImage
import kotlinx.android.synthetic.main.fragment_detailed_note.noteText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note


class DetailedNoteFragment : Fragment() {
    private lateinit var note: Note

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detailed_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val noteDao = AppDatabase.getInstance(requireContext()).noteDao()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            note = checkNotNull(noteDao.get(noteId)) { "No such note" }
            withContext(Dispatchers.Main) {
                bindNote(note)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detailed_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menuSave -> {
                saveNoteContent()
                true
            }
            else -> false
        }

    private fun bindNote(note: Note) {
        note.loadImageInto(noteImage, ::adjustLayout)
        noteText.setText(note.text)
    }

    private fun adjustLayout() {
        val isTablet = resources.isTablet
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val layout = noteImage.parent as ConstraintLayout
        TransitionManager.beginDelayedTransition(layout)

        val helper = ConstraintSet()
        helper.clone(layout)

        if (isLandscape && !isTablet) {
            helper.constrainWidth(R.id.noteImage, 0)
            helper.constrainMaxWidth(R.id.noteImage, noteImage.width)
            helper.constrainDefaultWidth(R.id.noteImage, ConstraintSet.MATCH_CONSTRAINT_WRAP)
        } else {
            helper.constrainHeight(R.id.noteImage, 0)
            helper.constrainMaxHeight(R.id.noteImage, noteImage.height)
            helper.constrainDefaultHeight(R.id.noteImage, ConstraintSet.MATCH_CONSTRAINT_WRAP)
        }

        helper.applyTo(layout)
    }

    private fun saveNoteContent() {
        if (!::note.isInitialized)
            return

        val text = noteText.text.toString()
        note = Note(note.id, text, note.imagePath, note.date)
        val noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            noteDao.update(note)
        }
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
