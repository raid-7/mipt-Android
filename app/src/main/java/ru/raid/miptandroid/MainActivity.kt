package ru.raid.miptandroid

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import java.io.File

val Resources.isTablet: Boolean
    get() = getBoolean(R.bool.is_tablet)

class MainActivity : AppCompatActivity() {
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

    fun onPictureCaptured(file: File) {
        supportFragmentManager.popBackStack()

        val noteDao = AppDatabase.getInstance(this).noteDao()
        lifecycleScope.launch(Dispatchers.IO) {
            noteDao.insert(NoteGenerator.generateNote(file))
        }
    }

    companion object {
        private const val DETAILED_NOTE_FRAGMENT = "detailed_note"
    }
}
