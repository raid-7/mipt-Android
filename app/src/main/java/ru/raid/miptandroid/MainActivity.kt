package ru.raid.miptandroid

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

val noteRepo = NoteRepository(2000)


class MainActivity : FragmentActivity() {
    val isTablet: Boolean
        get() = resources.getBoolean(R.bool.is_tablet)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentLeft, NoteListFragment())
                .commit()
        }
    }

    fun showDetailedNote(note: Note) {
        supportFragmentManager.popBackStack(DETAILED_NOTE_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentRight, DetailedNoteFragment.forNote(note))
            .addToBackStack(DETAILED_NOTE_FRAGMENT)
            .commit()
    }

    companion object {
        private val DETAILED_NOTE_FRAGMENT = "detailed_note"
    }
}
