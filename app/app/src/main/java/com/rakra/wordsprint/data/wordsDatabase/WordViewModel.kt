import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakra.wordsprint.data.wordsDatabase.Status
import com.rakra.wordsprint.data.wordsDatabase.WordDao
import com.rakra.wordsprint.data.wordsDatabase.WordEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WordViewModel(private val wordDao: WordDao) : ViewModel() {
    private val wordsByStatusCache = mutableMapOf<Pair<Int, Status>, StateFlow<List<WordEntry>>>()

    val knownWordsFlow: Flow<List<WordEntry>> = wordDao.getWordsUnitless(Status.KNOWN)

    init {
        viewModelScope.launch {
            hasAtLeastNUnknownWords(1).collect { hasEnough ->
                Log.d("UnknownWordsCheck", "Has enough: $hasEnough")
            }
        }
    }

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
        val key = unit to status
        return wordsByStatusCache.getOrPut(key) {
            wordDao.getAllWords(unit, status)
                .onEach { Log.d("DEBUG", "Emitted words for $key = $it") }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList(),
                )
        }
    }

    /**
     * Returns the next unknown word for the given unit, excluding any already shown or dismissed.
     */
    suspend fun getNextWord(unit: Int, exclude: List<WordEntry>): WordEntry? =
        withContext(Dispatchers.IO) {
            val excludedIds = exclude.map { it.id }.toSet()
            val allUnknown = wordDao.getAllWords(unit, Status.UNKNOWN).first()
            allUnknown.firstOrNull { it.id !in excludedIds }
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

    fun getRandomizedWordsFlow(unit: Int): Flow<List<WordEntry>> {
        return wordDao.getAllWordsInUnit(unit)
            .map { it.shuffled() }
    }

    suspend fun fetchWordsByIds(ids: List<Int>): List<WordEntry> =
        ids.mapNotNull { id ->
            wordDao.getWordByIdSuspend(id)
        }

    private fun countUnknownWords(unit: Int): Flow<Int> =
        wordDao.countUnknownWordsInUnit(unit, "UNKNOWN")

    suspend fun getProgressionMap(): Map<Int, Float> {
        val entries = wordDao.getAllWords().first()

        val groupedByUnit: Map<Int, List<WordEntry>> = entries.groupBy { it.unit }

        return groupedByUnit.mapValues { (unit, wordsInUnit) ->
            val total = wordsInUnit.size.toFloat()
            Log.d("DEBUG", "TOTAL WORDS IN UNIT $unit IS $total")
            val notSelectedOrUnknownCount = wordsInUnit.count { it.status == Status.NOT_SELECTED || it.status == Status.UNKNOWN }
            (total - notSelectedOrUnknownCount) / total
        }
    }

    private fun hasAtLeastNUnknownWords(unit: Int, n: Int = 10): StateFlow<Boolean> {
        return countUnknownWords(unit)
            .map { count -> count >= n }
            .onStart { emit(false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = false,
            )
    }
}
