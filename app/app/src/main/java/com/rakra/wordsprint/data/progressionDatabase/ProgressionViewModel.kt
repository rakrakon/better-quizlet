package com.rakra.wordsprint.data.progressionDatabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressionViewModel(
    private val dao: ProgressionDao,
) : ViewModel() {

    private val _allEntries = MutableStateFlow<List<ProgressionEntry>>(emptyList())
    val allEntries: StateFlow<List<ProgressionEntry>> = _allEntries.asStateFlow()
    private val practiceFlows = mutableMapOf<Int, MutableStateFlow<List<ProgressionEntry>>>()

    fun loadAll() {
        viewModelScope.launch {
            _allEntries.value = dao.getAll()
        }
    }

    fun loadPracticesForUnit(unit: Int): StateFlow<List<ProgressionEntry>> {
        practiceFlows[unit]?.let { return it.asStateFlow() }

        val stateFlow = MutableStateFlow<List<ProgressionEntry>>(emptyList())
        practiceFlows[unit] = stateFlow

        viewModelScope.launch {
            val data = dao.getAllPracticesOfUnit(unit)
            stateFlow.value = data
        }

        return stateFlow.asStateFlow()
    }

    fun refreshPracticesForUnit(unit: Int) {
        viewModelScope.launch {
            val data = dao.getAllPracticesOfUnit(unit)
            practiceFlows[unit]?.value = data
        }
    }

    fun getEntry(unit: Int, practiceNum: Int, onResult: (ProgressionEntry?) -> Unit) {
        viewModelScope.launch {
            onResult(dao.getByUnitAndPractice(unit, practiceNum))
        }
    }

    fun getNumberOfPracticesInUnit(unit: Int) {
        viewModelScope.launch {
            dao.getAllPracticesOfUnit(unit).size
        }
    }

    suspend fun getProgressionMap(): Map<Int, Float> {
        val entries = dao.getAll()

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
            loadAll()
        }
    }

    fun update(entry: ProgressionEntry) {
        viewModelScope.launch {
            dao.update(entry)
            loadAll()
        }
    }

    fun delete(entry: ProgressionEntry) {
        viewModelScope.launch {
            dao.delete(entry)
            loadAll()
        }
    }
}
