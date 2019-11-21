package ru.raid.miptandroid

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_note_list.addButton
import kotlinx.android.synthetic.main.fragment_note_list.notesList
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import kotlin.math.roundToInt

class NoteListFragment : PermissionHelperFragment<NoteListFragment.PermissionTag>(PermissionTag.values()) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.isTablet
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val recycleViewAdapter = NoteAdapter(::showDetailedView)
        AppDatabase.getInstance(requireContext()).noteDao().getAll().observe(::getLifecycle) {
            recycleViewAdapter.notes = it
        }

        with(notesList) {
            layoutManager = if (isLandscape xor isTablet) {
                addItemDecoration(NoteLandscapeViewDecoration((8 * resources.displayMetrics.density).roundToInt()))
                GridLayoutManager(this@NoteListFragment.context, 2, RecyclerView.VERTICAL, false)
            } else {
                LinearLayoutManager(this@NoteListFragment.context)
            }

            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = recycleViewAdapter
            setHasFixedSize(true)
        }

        addButton.setOnClickListener { showCamera() }
    }

    private fun showDetailedView(note: Note) {
        val mainActivity = activity as? MainActivity
        mainActivity?.showDetailedNote(note)
    }

    private fun showCamera() {
        withPermissions(
            arrayOf(Manifest.permission.CAMERA),
            R.string.camera_rationale,
            R.string.camera_rationale_in_settings,
            PermissionTag.CAMERA_START
        )
    }

    override fun onPermissionsResult(tag: PermissionTag, granted: Boolean) {
        if (tag == PermissionTag.CAMERA_START && granted) {
            val mainActivity = activity as? MainActivity
            mainActivity?.showCamera()
        }
    }

    enum class PermissionTag {
        CAMERA_START
    }
}
