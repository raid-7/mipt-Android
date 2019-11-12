package ru.raid.miptandroid

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.io.File

val noteRepo = NoteRepository(2000)
val Resources.isTablet: Boolean
    get() = getBoolean(R.bool.is_tablet)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSelection, NoteListFragment())
                .commit()
        }
    }

    fun showDetailedNote(note: Note) {
        supportFragmentManager.popBackStack(DETAILED_NOTE_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentInfo, DetailedNoteFragment.forNote(note))
            .addToBackStack(DETAILED_NOTE_FRAGMENT)
            .commit()
    }

    fun showCamera() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentInfo, CameraFragment())
            .addToBackStack(null)
            .commit()
    }

    fun onPictureCaptured(file: File) {
        supportFragmentManager.popBackStack()
        // TODO
    }

    companion object {
        private val DETAILED_NOTE_FRAGMENT = "detailed_note"
    }
}
