package com.ivanzhur.timbertest.fragment.measure

import androidx.lifecycle.MutableLiveData
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import com.ivanzhur.timbertest.model.LinesDisplayData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val stateLiveData = MutableLiveData<State>()

    var lastLinesData: LinesDisplayData? = null
    val linesDataFlow = MutableStateFlow<LinesDisplayData?>(null)

    enum class State {
        IDLE, DRAWING_LENGTH, DRAWING_DIAMETER
    }

    init {
        stateLiveData.value = State.IDLE
    }

    fun onLengthClick() = launch {
        // Remove length line and go back to IDLE state
        if (stateLiveData.value == State.DRAWING_LENGTH) {
            updateStartLength(null, null)
            stateLiveData.value = State.IDLE
        }
        else {
            stateLiveData.value = State.DRAWING_LENGTH
        }
    }

    fun onDiameterClick() = launch {
        // Remove diameter line and go back to IDLE state
        if (stateLiveData.value == State.DRAWING_DIAMETER) {
            updateStartDiameter(null, null)
            stateLiveData.postValue(State.IDLE)
        }
        else {
            stateLiveData.postValue(State.DRAWING_DIAMETER)
        }
    }

    fun onTouchDown(x: Float, y: Float) = launch {
        when (stateLiveData.value) {
            State.DRAWING_LENGTH -> updateStartLength(x, y)
            State.DRAWING_DIAMETER -> updateStartDiameter(x, y)
            else -> {}
        }
    }

    fun onMove(x: Float, y: Float) = launch {
        when (stateLiveData.value) {
            State.DRAWING_LENGTH -> updateEndLength(x, y)
            State.DRAWING_DIAMETER -> updateEndDiameter(x, y)
            else -> {}
        }
    }

    fun onTouchUp(x: Float, y: Float) {

    }

    private suspend fun updateStartLength(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesData ?: LinesDisplayData()).copy(
            lengthStartX = startX,
            lengthStartY = startY,
            lengthEndX = null,
            lengthEndY = null,
        )
        lastLinesData = newPoint
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateStartDiameter(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesData ?: LinesDisplayData()).copy(
            diameterStartX = startX,
            diameterStartY = startY,
            diameterEndX = null,
            diameterEndY = null,
        )
        lastLinesData = newPoint
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateEndLength(endX: Float, endY: Float) {
        val newPoint = (lastLinesData ?: LinesDisplayData()).copy(
            lengthEndX = endX,
            lengthEndY = endY,
        )
        lastLinesData = newPoint
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateEndDiameter(endX: Float, endY: Float) {
        val newPoint = (lastLinesData ?: LinesDisplayData()).copy(
            diameterEndX = endX,
            diameterEndY = endY,
        )
        lastLinesData = newPoint
        linesDataFlow.emit(newPoint)
    }
}