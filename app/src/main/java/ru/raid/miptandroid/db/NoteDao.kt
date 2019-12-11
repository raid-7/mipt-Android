package ru.raid.miptandroid.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
abstract class NoteDao {
    @Insert
    abstract suspend fun insert(note: Note): Long

    @Update
    abstract suspend fun update(note: Note)

    @Query("SELECT * FROM notes")
    abstract fun getAll(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    abstract suspend fun get(id: Long): Note?

    @Query("DELETE FROM notes WHERE id = :id")
    abstract suspend fun remove(id: Long)
}
