package ru.raid.miptandroid

import java.util.Calendar
import java.util.Random

class NoteRepository(val size: Int) {
    private val notes: Array<Note>
    private val random = Random()

    init {
        notes = Array(size) { generateNote(it) }
    }

    operator fun get(index: Int) = notes[index]

    private fun generateNote(id: Int) = Note(
        id,
        Calendar.getInstance(),
        generateText(4, 8, random.nextInt(2) + 1),
        generateText(3, 10, random.nextInt(20) + 20),
        random.nextInt(TOTAL_IMAGES_AVAILABLE)
    )

    private fun generateWord(minWordLen: Int, maxWordLen: Int, first_uppercase: Boolean = false): String {
        val result = StringBuilder()
        val len = random.nextInt(maxWordLen + 1 - minWordLen) + minWordLen
        for (j in 0 until len) {
            val alphabet = if (first_uppercase && j == 0) {
                ALPHABET_UPPERCASE
            } else {
                ALPHABET_LOWERCASE
            }
            result.append(alphabet[random.nextInt(alphabet.length)])
        }
        return result.toString()
    }

    private fun generateText(minWordLen: Int, maxWordLen: Int, words: Int): String {
        val result = StringBuilder()
        for (i in 0 until words) {
            if (i != 0)
                result.append(' ')
            result.append(generateWord(minWordLen, maxWordLen,  i == 0 || random.nextBoolean()))
        }
        return result.toString()
    }

    companion object {
        private const val TOTAL_IMAGES_AVAILABLE = 20
        private const val ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
        private const val ALPHABET_UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    }
}
