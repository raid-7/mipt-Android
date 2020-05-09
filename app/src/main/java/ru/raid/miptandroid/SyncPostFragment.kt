package ru.raid.miptandroid

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_sync_post.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import ru.raid.miptandroid.db.AppDatabase
import ru.raid.miptandroid.db.Note
import java.io.IOException


class SyncPostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sync_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val noteDao = AppDatabase.getInstance(requireContext()).noteDao()

        showLoadAnimation()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val note = checkNotNull(noteDao.get(noteId)) { "No such note" }
            val syncedNote = if (note.inSyncId == null) obtainSyncId(note) else note
            val syncId = syncedNote.inSyncId
            withContext(Dispatchers.Main) {
                if (syncId == null) {
                    syncFailed()
                } else {
                    showSyncQr(syncId)
                }
            }
        }
    }

    @MainThread
    private fun showLoadAnimation() {
        progressOverlay.visibility = View.VISIBLE
        qrView.visibility = View.GONE
    }

    @MainThread
    private fun showSyncQr(syncId: String) {
        Log.d("SyncPost", syncId)
        progressOverlay.visibility = View.GONE
        qrView.visibility = View.VISIBLE
        qrView.setData(syncId).build()
    }

    private fun syncFailed() {
        val mainActivity = activity as? MainActivity
        mainActivity?.let {
            Toast.makeText(it, R.string.sync_post_failed, Toast.LENGTH_SHORT).show()
            it.popFragment()
        }
    }

    private suspend fun obtainSyncId(note: Note): Note = withContext(Dispatchers.IO) {
        val id = shareNote(note)
        updateWithSyncId(note, id)
    }

    @Suppress("BlockingMethodInNonBlockingContext") // known to run under Dispatchers.IO
    private suspend fun shareNote(note: Note): String? {
        val contentResolver = activity?.contentResolver ?: return null
        val syncService = RetrofitServices.getInstance(requireContext()).syncService

        return try {
            val uri = note.imageUri
            val bytes = contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            }

            // CameraX library save images to JPEG
            // camera-core:1.0.0-beta03
            val type = contentResolver.getType(uri) ?: "image/jpeg"
            if (bytes == null) {
                Log.e("SyncPost", "type={$type}, bytes=${bytes}")
                return null
            }
            val imageRequestBody = RequestBody.create(MediaType.parse(type), bytes)
            val imagePart = MultipartBody.Part
                .createFormData("image", uri.lastPathSegment ?: "image", imageRequestBody)

            syncService.shareNote(NoteData(note.text, note.date), imagePart)
        } catch (exc: Exception) {
            when(exc) {
                is HttpException, is IOException -> {
                    Log.e("SyncPost", exc.message, exc)
                    null
                }
                else -> throw exc
            }
        }
    }

    private suspend fun updateWithSyncId(note: Note, id: String?): Note {
        if (id == null)
            return note
        val noteDao = AppDatabase.getInstance(requireContext()).noteDao()
        val updatedNote = Note(note.id, note.text, note.imagePath, note.date, id)
        noteDao.update(updatedNote)
        return updatedNote
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(note: Note): SyncPostFragment {
            val fragment = SyncPostFragment()
            fragment.arguments = Bundle().apply {
                putLong(NOTE_ID, note.id)
            }
            return fragment
        }
    }
}
