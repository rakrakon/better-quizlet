import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
