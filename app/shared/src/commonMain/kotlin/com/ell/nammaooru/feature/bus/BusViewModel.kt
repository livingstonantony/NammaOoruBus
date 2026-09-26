package com.ell.nammaooru.feature.bus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.domain.repository.BusRepository
import com.ell.nammaooru.feature.bus.UiState
import com.ell.nammaooru.network.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

sealed class BusIntent {
    object GetBuse : BusIntent()
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

    private val _stateCreateBus = MutableStateFlow<UiState<Bus?>>(UiState.Loading)
    val stateCreateBus: StateFlow<UiState<Bus?>> = _stateCreateBus.asStateFlow()

    private val _stateUpdateBus = MutableStateFlow<UiState<Bus?>>(UiState.Loading)
    val stateUpdateBus: StateFlow<UiState<Bus?>> = _stateUpdateBus.asStateFlow()

    private val _stateDeleteBus = MutableStateFlow<UiState<Bus?>>(UiState.Loading)
    val stateDeleteBus: StateFlow<UiState<Bus?>> = _stateDeleteBus.asStateFlow()


    fun processIntent(intent: BusIntent) {
        viewModelScope.launch {
            when (intent) {
                is BusIntent.GetBuse -> {
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
                _stateBuses.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )
    }

    suspend fun createBus(bus: Bus) {
        _stateCreateBus.value = UiState.Loading
        repository.createBus(bus).fold(
            onSuccess = {
                _stateCreateBus.value = UiState.Success(it.data)
                getBuses()
            },
            onFailure = {
                _stateCreateBus.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )

    }

    suspend fun updateBus(id: String, bus: Bus) {
        _stateUpdateBus.value = UiState.Loading
        repository.updateBus(id, bus).fold(
            onSuccess = {
                _stateUpdateBus.value = UiState.Success(it.data)
                getBuses()
            },
            onFailure = {
                _stateUpdateBus.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )
    }

    suspend fun deleteBus(id: String) {
        _stateDeleteBus.value = UiState.Loading
        repository.deleteBus(id).fold(
            onSuccess = {
                _stateDeleteBus.value = UiState.Success(it.data)
                getBuses()
            },
            onFailure = {
                _stateDeleteBus.value = UiState.Error(it.message ?: "Something went wrong")
            }
        )
    }


}