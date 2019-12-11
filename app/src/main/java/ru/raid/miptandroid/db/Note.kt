package ru.raid.miptandroid.db

import android.widget.ImageView
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File


@Entity(tableName = "notes")
class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val text: String,
    val imagePath: String,
    val date: Long
) {
    fun loadImageInto(view: ImageView, onSuccessListener: (() -> Unit)? = null) {
        Picasso.get().load(File(imagePath))
            .fit()
            .centerInside()
            .into(view, object : Callback.EmptyCallback() {
                override fun onSuccess() {
                    onSuccessListener?.invoke()
                }
            })
    }
}
