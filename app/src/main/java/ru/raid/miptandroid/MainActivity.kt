package ru.raid.miptandroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.notesList

val noteRepo = NoteRepository(2000)


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with (notesList) {
            layoutManager = LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = NoteAdapter(noteRepo)
            setHasFixedSize(true)
        }
    }

}
