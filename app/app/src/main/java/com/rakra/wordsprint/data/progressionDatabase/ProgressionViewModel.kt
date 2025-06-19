package com.rakra.wordsprint.data.progressionDatabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProgressionViewModel(
    private val dao: ProgressionDao,
) : ViewModel() {

    // Expose all entries as StateFlow, backed by DAO Flow
    val allEntries: StateFlow<List<ProgressionEntry>> = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cache of StateFlows per unit, backed by DAO Flow
    private val practiceFlows = mutableMapOf<Int, StateFlow<List<ProgressionEntry>>>()

    // Return a StateFlow that updates automatically as DAO Flow emits new data
    fun loadPracticesForUnit(unit: Int): StateFlow<List<ProgressionEntry>> {
        return practiceFlows.getOrPut(unit) {
            dao.getAllPracticesOfUnit(unit)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }

    // No need for manual refresh method now,
    // because loadPracticesForUnit returns a reactive StateFlow.

    // For single entries, still suspend fetch (no reactive support here)
    fun getEntry(unit: Int, practiceNum: Int, onResult: (ProgressionEntry?) -> Unit) {
        viewModelScope.launch {
            val entry = dao.getByUnitAndPractice(unit, practiceNum)
            onResult(entry)
        }
    }

    // Returns the number of practices in a unit as a suspend function
    suspend fun getNumberOfPracticesInUnit(unit: Int): Int {
        return dao.getAllPracticesOfUnit(unit).first().size
    }

    // Calculate progression map as suspend function
    suspend fun getProgressionMap(): Map<Int, Float> {
        val entries = dao.getAll().first()

        return entries
            .groupBy { it.unit }
            .mapValues { (_, unitEntries) ->
                val total = unitEntries.size.takeIf { it > 0 } ?: return@mapValues 0f
                val completed = unitEntries.count { it.completion == ProgressStatus.COMPLETED }
                completed.toFloat() / total
            }
    }

    fun insertWords(entries: List<ProgressionEntry>) {
        viewModelScope.launch {
            dao.insertAll(entries)
        }
    }

    fun insert(entry: ProgressionEntry) {
        viewModelScope.launch {
            dao.insert(entry)
        }
    }

    fun update(entry: ProgressionEntry) {
        viewModelScope.launch {
            dao.update(entry)
        }
    }

    fun delete(entry: ProgressionEntry) {
        viewModelScope.launch {
            dao.delete(entry)
        }
    }
}
