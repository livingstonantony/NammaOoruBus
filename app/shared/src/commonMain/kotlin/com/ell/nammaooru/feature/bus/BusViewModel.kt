package com.ell.nammaooru.feature.bus

import androidx.lifecycle.ViewModel
import com.ell.nammaooru.domain.model.Bus
import com.ell.nammaooru.network.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

class BusViewModel: ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Bus>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Bus>>> = _state.asStateFlow()

}