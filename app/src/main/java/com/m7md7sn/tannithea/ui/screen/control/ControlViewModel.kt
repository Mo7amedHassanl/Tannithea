package com.m7md7sn.tannithea.ui.screen.control

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.m7md7sn.tannithea.data.repository.SensorRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val repository: SensorRepository
) : ViewModel() {
    private val _pumpStates = MutableStateFlow(List(7) { false })
    val pumpStates: StateFlow<List<Boolean>> = _pumpStates

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _scheduleStatus = MutableStateFlow("unknown")
    val scheduleStatus: StateFlow<String> = _scheduleStatus

    init {
        listenToPumpStates()
        listenToScheduleStatus()
    }

    private fun listenToPumpStates() {
        viewModelScope.launch {
            try {
                repository.getPumpStatesFlow().collectLatest { states ->
                    _pumpStates.value = states
                    _error.value = null
                }
            } catch (e: Exception) {
                _error.value = "Failed to load pump states: ${e.message}"
            }
        }
    }

    private fun listenToScheduleStatus() {
        viewModelScope.launch {
            try {
                repository.getScheduleStatusFlow().collectLatest { status ->
                    _scheduleStatus.value = status
                }
            } catch (e: Exception) {
                _error.value = "Failed to load schedule status: ${e.message}"
            }
        }
    }

    fun retry() {
        _error.value = null
        listenToPumpStates()
    }

    fun togglePump(index: Int) {
        val newState = !_pumpStates.value[index]
        try {
            repository.setPumpState(index, newState)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to update pump: ${e.message}"
        }
    }

    fun turnAllOn() {
        try {
            repository.setAllPumps(true)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to turn all pumps on: ${e.message}"
        }
    }

    fun turnAllOff() {
        try {
            repository.setAllPumps(false)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Failed to turn all pumps off: ${e.message}"
        }
    }

    fun schedulePumps() {
        // Placeholder for scheduling logic
    }

    fun controlSchedule(command: String) {
        viewModelScope.launch {
            try {
                repository.setScheduleCommand(command)
            } catch (e: Exception) {
                _error.value = "Failed to control schedule: ${e.message}"
            }
        }
    }
} 