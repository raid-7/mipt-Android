package ru.raid.miptandroid

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.notesList
import kotlin.math.roundToInt

val noteRepo = NoteRepository(2000)


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(notesList) {
            layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(this@MainActivity, 2, RecyclerView.VERTICAL, false)
                    .apply {
                        addItemDecoration(
                            NoteLandscapeViewDecoration((8 * resources.displayMetrics.density).roundToInt()))
                    }
            } else {
                LinearLayoutManager(this@MainActivity)
            }
            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = NoteAdapter(noteRepo)
            setHasFixedSize(true)
        }
    }

}
