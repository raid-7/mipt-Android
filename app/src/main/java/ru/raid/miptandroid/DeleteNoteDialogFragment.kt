package ru.raid.miptandroid

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.raid.miptandroid.db.Note

class DeleteNoteDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext()).apply {
            setMessage(R.string.askDeleteNote)
            setPositiveButton(R.string.buttonYes) { _, _ ->
                val mainActivity = activity as? MainActivity
                val noteId = arguments?.getLong(NOTE_ID)
                if (mainActivity != null && noteId != null)
                    mainActivity.deleteNote(noteId)
            }
            setNegativeButton(R.string.buttonCancel) { _, _ -> }
        }.create()

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(note: Note): DeleteNoteDialogFragment {
            val fragment = DeleteNoteDialogFragment()
            fragment.arguments = Bundle().apply {
                putLong(NOTE_ID, note.id)
            }
            return fragment
        }
    }
}