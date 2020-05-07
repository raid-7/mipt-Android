package ru.raid.miptandroid.db

import android.net.Uri
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = "notes")
class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val text: String,
    val imagePath: String,
    val date: Long,
    val inSyncId: String? = null
) {
    val imageUri: Uri
        get() = Uri.parse(imagePath)
}
