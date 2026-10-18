package com.ell.nammaooru.feature.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.repository.BusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState<out T> {

    data object Loading : UiState<Nothing>

    data class Success<T>(
        val data: T
    ) : UiState<T>

    data class Error(
        val message: String
    ) : UiState<Nothing>
}

sealed interface OperationState {
    data object Idle : OperationState
    data object Loading : OperationState
    data class Success(val message: String) : OperationState
    data class Error(val message: String) : OperationState
}

sealed class BusIntent {
    object GetBuses : BusIntent()
    data class GetBus(val id: String) : BusIntent()
    data class CreateBus(val bus: Bus) : BusIntent()
    data class UpdateBus(val id: String, val bus: Bus) : BusIntent()
    data class DeleteBus(val id: String) : BusIntent()

}

class BusViewModel(val repository: BusRepository) : ViewModel() {

    private val _stateBuses = MutableStateFlow<UiState<List<Bus>>>(UiState.Loading)
    val stateBuses: StateFlow<UiState<List<Bus>>> = _stateBuses.asStateFlow()

    private val _stateBus = MutableStateFlow<UiState<Bus?>>(UiState.Loading)
    val stateBus: StateFlow<UiState<Bus?>> = _stateBus.asStateFlow()

    private val _stateOperation = MutableStateFlow<OperationState>(OperationState.Idle)
    val stateOperation: StateFlow<OperationState> = _stateOperation.asStateFlow()

    fun clearOperation() {
        _stateOperation.value = OperationState.Idle
    }

    fun processIntent(intent: BusIntent) {
        viewModelScope.launch {
            when (intent) {
                is BusIntent.GetBuses -> {
                    getBuses()
                }

                is BusIntent.GetBus -> {
                    getBus(intent.id)
                }

                is BusIntent.CreateBus -> {
                    createBus(intent.bus)
                }

                is BusIntent.UpdateBus -> {
                    updateBus(intent.id, intent.bus)
                }

                is BusIntent.DeleteBus -> {
                    deleteBus(intent.id)

                }
            }
        }
    }

    suspend fun getBuses() {
        _stateBuses.value = UiState.Loading
        repository.getBuses().fold(
            onSuccess = { response ->
                _stateBuses.value = UiState.Success(response.data.orEmpty())
            },
            onFailure = {
                _stateBuses.value = UiState.Error(it.message ?: "Something went wrong")
            }

        )
    }

    suspend fun getBus(id: String) {
        _stateBus.value = UiState.Loading
        repository.getBus(id).fold(
            onSuccess = { response ->
                _stateBus.value = UiState.Success(response.data)
            },
            onFailure = {
                _stateBus.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )
    }

    suspend fun createBus(bus: Bus) = runWrite("Bus added") { repository.createBus(bus) }

    suspend fun updateBus(id: String, bus: Bus) = runWrite("Bus updated") { repository.updateBus(id, bus) }

    suspend fun deleteBus(id: String) = runWrite("Bus deleted") { repository.deleteBus(id) }

    private suspend fun <T> runWrite(successMessage: String, call: suspend () -> Result<T>) {
        _stateOperation.value = OperationState.Loading
        call().fold(
            onSuccess = {
                _stateOperation.value = OperationState.Success(successMessage)
                getBuses()
            },
            onFailure = {
                _stateOperation.value = OperationState.Error(it.message ?: "Something went wrong")
            }
        )
    }
}
