package ru.raid.miptandroid

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import ru.raid.miptandroid.db.Note

val Resources.isTablet: Boolean
    get() = getBoolean(R.bool.is_tablet)

class MainActivity : AppCompatActivity() {
    lateinit var noteFlows: NoteFlows

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteFlows = NoteFlows(this, lifecycle)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSelection, NoteListFragment())
                .commit()
        }
    }

    fun showDetailedNote(note: Note) {
        if (!noteFlows.isReady(note))
            return

        supportFragmentManager.popBackStack(DETAILED_NOTE_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentInfo, DetailedNoteFragment.forNote(note), DETAILED_NOTE_FRAGMENT)
            .addToBackStack(DETAILED_NOTE_FRAGMENT)
            .commit()
    }

    fun showCamera() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentInfo, CameraFragment())
            .addToBackStack(null)
            .commit()
    }

    fun showDeleteNoteDialog(note: Note) {
        DeleteNoteDialogFragment.forNote(note).show(supportFragmentManager, null)
    }

    fun popFragment() {
        supportFragmentManager.popBackStack()
    }

    companion object {
        private const val DETAILED_NOTE_FRAGMENT = "detailed_note"
    }
}
