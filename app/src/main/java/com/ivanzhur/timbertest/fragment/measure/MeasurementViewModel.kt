package com.ivanzhur.timbertest.fragment.measure

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import com.ivanzhur.timbertest.model.LinesDisplayData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val stateLiveData = MutableLiveData<State>()

    var lastLinesDataToDisplay: LinesDisplayData? = null
    var lastLinesDataOnImage: LinesDisplayData? = null
    val linesDataFlow = MutableStateFlow<LinesDisplayData?>(null)

    var imageStartXPortrait = 0f
    var imageStartYPortrait = 0f
    var imageStartXLandscape = 0f
    var imageStartYLandscape = 0f
    var canvasToImageRatioPortrait = 1f
    var canvasToImageRatioLandscape = 1f

    var currentOrientationIsPortrait: Boolean? = null

    enum class State {
        IDLE, DRAWING_LENGTH, DRAWING_DIAMETER
    }

    init {
        stateLiveData.value = State.IDLE
    }

    fun setImageData(
        imageWidthPixels: Int,
        imageHeightPixels: Int,
        canvasWidthPixels: Int,
        canvasHeightPixels: Int,
        isPortrait: Boolean,
    ) {
        Timber.i("init image: $imageWidthPixels x $imageHeightPixels, canvas: $canvasWidthPixels x $canvasHeightPixels")
        val imageWhRatio = imageWidthPixels.toFloat() / imageHeightPixels.toFloat()
        val canvasWhRatio = canvasWidthPixels.toFloat() / canvasHeightPixels.toFloat()

        val canvasToImageRatio: Float
        val imageStartX: Float
        val imageStartY: Float

        // Image is wider than canvas.
        // So there will be some whitespace to top and bottom
        // and image will fit View width.
        if (imageWhRatio > canvasWhRatio) {
            canvasToImageRatio = canvasWidthPixels.toFloat() / imageWidthPixels.toFloat()
            val imageHeightOnCanvas = imageHeightPixels * canvasToImageRatio
            imageStartX = 0f
            imageStartY = (canvasHeightPixels - imageHeightOnCanvas) / 2f
        }
        // Canvas is wider that image.
        // So there will be some whitespace to left and right
        // and image will fit View height
        else {
            canvasToImageRatio = canvasHeightPixels.toFloat() / imageHeightPixels.toFloat()
            val imageWidthOnCanvas = imageWidthPixels * canvasToImageRatio
            imageStartX = (canvasWidthPixels - imageWidthOnCanvas) / 2f
            imageStartY = 0f
        }

        Timber.i("image start: $imageStartX | $imageStartY | $canvasToImageRatio")

        if (isPortrait) {
            imageStartXPortrait = imageStartX
            imageStartYPortrait = imageStartY
            canvasToImageRatioPortrait = canvasToImageRatio
        }
        else {
            imageStartXLandscape = imageStartX
            imageStartYLandscape = imageStartY
            canvasToImageRatioLandscape = canvasToImageRatio
        }

        if (currentOrientationIsPortrait != isPortrait) {
            currentOrientationIsPortrait = isPortrait
            handleConfigurationChange()
        }
        else {
            currentOrientationIsPortrait = isPortrait
        }
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

    private fun convertCanvasXToImageX(x: Float?): Float? {
        val orientation = currentOrientationIsPortrait ?: return null
        if (x == null) return null
        val imageStartX = if (orientation) imageStartXPortrait else imageStartXLandscape
        val canvasToImageRatio = if (orientation) canvasToImageRatioPortrait else canvasToImageRatioLandscape
        return (x - imageStartX) / canvasToImageRatio
    }

    private fun convertCanvasYToImageY(y: Float?): Float? {
        val orientation = currentOrientationIsPortrait ?: return null
        if (y == null) return null
        val imageStartY = if (orientation) imageStartYPortrait else imageStartYLandscape
        val canvasToImageRatio = if (orientation) canvasToImageRatioPortrait else canvasToImageRatioLandscape
        return (y - imageStartY) / canvasToImageRatio
    }

    private fun convertImageXToCanvasX(x: Float?): Float? {
        val orientation = currentOrientationIsPortrait ?: return null
        if (x == null) return null
        val imageStartX = if (orientation) imageStartXPortrait else imageStartXLandscape
        val canvasToImageRatio = if (orientation) canvasToImageRatioPortrait else canvasToImageRatioLandscape
        return x * canvasToImageRatio + imageStartX
    }

    private fun convertImageYToCanvasY(y: Float?): Float? {
        val orientation = currentOrientationIsPortrait ?: return null
        if (y == null) return null
        val imageStartY = if (orientation) imageStartYPortrait else imageStartYLandscape
        val canvasToImageRatio = if (orientation) canvasToImageRatioPortrait else canvasToImageRatioLandscape
        return y * canvasToImageRatio + imageStartY
    }

    private suspend fun updateStartLength(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            lengthStartX = startX,
            lengthStartY = startY,
            lengthEndX = null,
            lengthEndY = null,
        )
        lastLinesDataToDisplay = newPoint
        lastLinesDataOnImage = (lastLinesDataOnImage ?: LinesDisplayData()).copy(
            lengthStartX = convertCanvasXToImageX(newPoint.lengthStartX),
            lengthStartY = convertCanvasYToImageY(newPoint.lengthStartY),
            lengthEndX = null,
            lengthEndY = null,
        )
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateStartDiameter(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            diameterStartX = startX,
            diameterStartY = startY,
            diameterEndX = null,
            diameterEndY = null,
        )
        lastLinesDataToDisplay = newPoint
        lastLinesDataOnImage = (lastLinesDataOnImage ?: LinesDisplayData()).copy(
            diameterStartX = convertCanvasXToImageX(newPoint.diameterStartX),
            diameterStartY = convertCanvasYToImageY(newPoint.diameterStartY),
            diameterEndX = null,
            diameterEndY = null,
        )
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateEndLength(endX: Float, endY: Float) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            lengthEndX = endX,
            lengthEndY = endY,
        )
        lastLinesDataOnImage = lastLinesDataOnImage?.copy(
            lengthEndX = convertCanvasXToImageX(newPoint.lengthEndX),
            lengthEndY = convertCanvasYToImageY(newPoint.lengthEndY),
        )
        lastLinesDataToDisplay = newPoint
        linesDataFlow.emit(newPoint)
    }

    private suspend fun updateEndDiameter(endX: Float, endY: Float) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            diameterEndX = endX,
            diameterEndY = endY,
        )
        lastLinesDataOnImage = lastLinesDataOnImage?.copy(
            diameterEndX = convertCanvasXToImageX(newPoint.diameterEndX),
            diameterEndY = convertCanvasYToImageY(newPoint.diameterEndY),
        )
        lastLinesDataToDisplay = newPoint
        linesDataFlow.emit(newPoint)
    }

    private fun handleConfigurationChange() {
        val newDisplayData = lastLinesDataOnImage?.copy(
            lengthStartX = convertImageXToCanvasX(lastLinesDataOnImage?.lengthStartX),
            lengthStartY = convertImageYToCanvasY(lastLinesDataOnImage?.lengthStartY),
            lengthEndX = convertImageXToCanvasX(lastLinesDataOnImage?.lengthEndX),
            lengthEndY = convertImageYToCanvasY(lastLinesDataOnImage?.lengthEndY),
            diameterStartX = convertImageXToCanvasX(lastLinesDataOnImage?.diameterStartX),
            diameterStartY = convertImageYToCanvasY(lastLinesDataOnImage?.diameterStartY),
            diameterEndX = convertImageXToCanvasX(lastLinesDataOnImage?.diameterEndX),
            diameterEndY = convertImageYToCanvasY(lastLinesDataOnImage?.diameterEndY),
        )
        lastLinesDataToDisplay = newDisplayData
        viewModelScope.launch {
            linesDataFlow.emit(newDisplayData)
        }
    }
}