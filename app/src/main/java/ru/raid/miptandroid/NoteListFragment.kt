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
import kotlinx.android.synthetic.main.fragment_note_list.*
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import kotlin.math.roundToInt

class NoteListFragment : PermissionHelperFragment<NoteListFragment.PermissionTag>(PermissionTag.values()) {
    private val noteListener = object : NoteActionListener {
        override fun onSelect(note: Note) {
            val mainActivity = activity as? MainActivity
            mainActivity?.showDetailedNote(note)
        }

        override fun onDelete(note: Note) {
            val mainActivity = activity as? MainActivity
            mainActivity?.showDeleteNoteDialog(note)
        }

        override fun onShare(note: Note) {
            val mainActivity = activity as? MainActivity
            mainActivity?.noteFlows?.share(note)
        }

        override fun onSync(note: Note) {
            val mainActivity = activity as? MainActivity
            mainActivity?.showSyncPostFragment(note)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.isTablet
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val recycleViewAdapter = NoteAdapter(noteListener)
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
        syncLoadButton.setOnClickListener { showSyncLoad() }
    }

    override fun onResume() {
        super.onResume()
        notesList.adapter?.notifyDataSetChanged()
    }

    override fun onPermissionsResult(tag: PermissionTag, granted: Boolean) {
        if (!granted)
            return

        val mainActivity = activity as? MainActivity ?: return
        when (tag) {
            PermissionTag.SYNC_LOAD_START -> {
                mainActivity.showSyncLoad()
            }
            PermissionTag.CAMERA_START -> {
                mainActivity.showCamera()
            }
        }
    }

    private fun showCamera() {
        withPermissions(
            arrayOf(Manifest.permission.CAMERA),
            R.string.camera_rationale,
            R.string.camera_rationale_in_settings,
            PermissionTag.CAMERA_START
        )
    }

    private fun showSyncLoad() {
        withPermissions(
            arrayOf(Manifest.permission.CAMERA),
            R.string.camera_rationale_qr,
            R.string.camera_rationale_in_settings,
            PermissionTag.SYNC_LOAD_START
        )
    }

    enum class PermissionTag {
        CAMERA_START,
        SYNC_LOAD_START
    }
}
