package ru.raid.miptandroid.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "notes")
class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val text: String,
    val imagePath: String,
    val date: Long
)
