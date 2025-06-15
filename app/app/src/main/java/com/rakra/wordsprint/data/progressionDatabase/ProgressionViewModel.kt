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

    fun loadAll() {
        viewModelScope.launch {
            _allEntries.value = dao.getAll()
        }
    }

    fun loadPracticesForUnit(unit: Int): StateFlow<List<ProgressionEntry>> {
        val stateFlow = MutableStateFlow<List<ProgressionEntry>>(emptyList())
        viewModelScope.launch {
            stateFlow.value = dao.getAllPracticesOfUnit(unit)
        }
        return stateFlow
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
