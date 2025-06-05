import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakra.wordsprint.database.Status
import com.rakra.wordsprint.database.WordDao
import com.rakra.wordsprint.database.WordEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WordViewModel(private val wordDao: WordDao) : ViewModel() {
    val wordsState: StateFlow<List<WordEntry>> = wordDao.getAllWords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Retrieves a [StateFlow] of up to 50 [WordEntry] items matching the given [unit] and [status].
     *
     * This function fetches the words from the database using a Room DAO query with a `LIMIT 50` clause.
     * It converts the resulting [Flow] into a [StateFlow] to maintain observable state within the ViewModel scope.
     *
     * @param unit The unit number to filter the word entries.
     * @param status The status to filter the word entries (e.g., NEW, LEARNED).
     * @return A [StateFlow] containing a list of up to 50 [WordEntry] objects.
     */
    fun getWordsByStatus(unit: Int, status: Status): StateFlow<List<WordEntry>> {
        return wordDao.getAllWords(unit, status)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun insertWords(words: List<WordEntry>) {
        viewModelScope.launch {
            wordDao.insertAll(words)
        }
    }

    fun insertWord(word: WordEntry) {
        viewModelScope.launch {
            wordDao.insertWord(word)
        }
    }

    fun updateWord(word: WordEntry) {
        Log.d("UPDATE", "Updating word with id=${word.id}, status=${word.status}")
        viewModelScope.launch {
            wordDao.updateWord(word)
        }
    }

    fun deleteWord(word: WordEntry) {
        viewModelScope.launch {
            wordDao.deleteWord(word)
        }
    }

    private fun countUnknownWords(unit: Int) = wordDao.countUnknownWordsInUnit(unit)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun hasAtLeastNUnknownWords(unit: Int, n: Int = 10): StateFlow<Boolean> {
        return countUnknownWords(unit)
            .map { count -> count >= n }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false
            )
    }
}
