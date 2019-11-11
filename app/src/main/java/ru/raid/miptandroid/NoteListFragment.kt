package ru.raid.miptandroid

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_note_list.notesList
import kotlin.math.roundToInt

class NoteListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.isTablet
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        with(notesList) {
            layoutManager = if (isLandscape xor isTablet) {
                addItemDecoration(NoteLandscapeViewDecoration((8 * resources.displayMetrics.density).roundToInt()))
                GridLayoutManager(this@NoteListFragment.context, 2, RecyclerView.VERTICAL, false)
            } else {
                LinearLayoutManager(this@NoteListFragment.context)
            }

            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = NoteAdapter(noteRepo, ::showDetailedView)
            setHasFixedSize(true)
        }
    }

    private fun showDetailedView(note: Note) {
        val mainActivity = activity as? MainActivity
        mainActivity?.showDetailedNote(note)
    }
}
