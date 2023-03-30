package com.ivanzhur.timbertest.fragment.measure

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import com.ivanzhur.timbertest.model.LinesDisplayData
import com.ivanzhur.timbertest.model.LinesMeasurementData
import com.ivanzhur.timbertest.util.ifAllNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val stateLiveData = MutableLiveData<State>()

    /**
     * Last drawn data in canvas coordinates
     */
    private var lastLinesDataToDisplay: LinesDisplayData? = null

    /**
     * Last drawn data in image coordinates
     */
    private var lastLinesDataOnImage: LinesDisplayData? = null

    /**
     * Lines data flow for UI update
     */
    val linesDataFlow = MutableStateFlow<LinesDisplayData?>(null)

    /**
     * Measurement values flow for UI update
     */
    val linesMeasurementFlow = MutableStateFlow<LinesMeasurementData?>(null)

    /**
     * Emit [ValidityState.VALID] to mark successful data save.
     * Emit other states to mark corresponding validation errors.
     */
    val saveValidityStateFlow = MutableStateFlow<ValidityState?>(null)

    // These values are used to convert between 'image' coordinates and 'canvas' coordinates
    private var imageStartXPortrait = 0f
    private var imageStartYPortrait = 0f
    private var imageStartXLandscape = 0f
    private var imageStartYLandscape = 0f
    private var canvasToImageRatioPortrait = 1f
    private var canvasToImageRatioLandscape = 1f
    private var currentOrientationIsPortrait: Boolean? = null

    /**
     * Represents which line is being drawn at the moment
     */
    enum class State {
        IDLE, DRAWING_LENGTH, DRAWING_DIAMETER
    }

    /**
     * Represents validity state for given canvas situation (drawn lines).
     */
    enum class ValidityState {
        NO_LENGTH, // Length line is missing
        NO_DIAMETER, // Diameter line is missing
        VALID, // Data is valid
        INVALID_TOO_FAR, // Length line is too far from the diameter circle on image
    }

    init {
        stateLiveData.value = State.IDLE
    }

    /**
     * Calculate values
     */
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
        // Coordinates of image top-left corner relative to canvas
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

    fun onSaveClick(imageUri: Uri) = launch {
        val validationResult = validateData()
        if (validationResult == ValidityState.VALID) {
            val measurementData = getMeasuredData()
            val record = RecordModel(
                0, imageUri,
                lastLinesDataOnImage?.lengthStartX ?: 0f,
                lastLinesDataOnImage?.lengthStartY ?: 0f,
                lastLinesDataOnImage?.lengthEndX ?: 0f,
                lastLinesDataOnImage?.lengthEndY ?: 0f,
                lastLinesDataOnImage?.diameterStartX ?: 0f,
                lastLinesDataOnImage?.diameterStartY ?: 0f,
                lastLinesDataOnImage?.diameterEndX ?: 0f,
                lastLinesDataOnImage?.diameterEndY ?: 0f,
                measurementData.lengthValue ?: 0f,
                measurementData.diameterValue ?: 0f,
            )
            withContext(Dispatchers.IO) {
                storageRepository.saveRecord(record)
            }
        }
        saveValidityStateFlow.emit(validationResult)
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

    /**
     * Call on ACTION_DOWN event to update length line start
     */
    private suspend fun updateStartLength(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            lengthStartX = startX,
            lengthStartY = startY,
            lengthEndX = null,
            lengthEndY = null,
        )
        lastLinesDataOnImage = (lastLinesDataOnImage ?: LinesDisplayData()).copy(
            lengthStartX = convertCanvasXToImageX(newPoint.lengthStartX),
            lengthStartY = convertCanvasYToImageY(newPoint.lengthStartY),
            lengthEndX = null,
            lengthEndY = null,
        )
        updatePointAndMeasureData(newPoint)
    }

    /**
     * Call on ACTION_DOWN event to update diameter line start
     */
    private suspend fun updateStartDiameter(startX: Float?, startY: Float?) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            diameterStartX = startX,
            diameterStartY = startY,
            diameterEndX = null,
            diameterEndY = null,
        )
        lastLinesDataOnImage = (lastLinesDataOnImage ?: LinesDisplayData()).copy(
            diameterStartX = convertCanvasXToImageX(newPoint.diameterStartX),
            diameterStartY = convertCanvasYToImageY(newPoint.diameterStartY),
            diameterEndX = null,
            diameterEndY = null,
        )
        updatePointAndMeasureData(newPoint)
    }

    /**
     * Call on ACTION_MOVE event to update length line end,
     * which if where the finger is currently located
     */
    private suspend fun updateEndLength(endX: Float, endY: Float) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            lengthEndX = endX,
            lengthEndY = endY,
        )
        lastLinesDataOnImage = lastLinesDataOnImage?.copy(
            lengthEndX = convertCanvasXToImageX(newPoint.lengthEndX),
            lengthEndY = convertCanvasYToImageY(newPoint.lengthEndY),
        )
        updatePointAndMeasureData(newPoint)
    }

    /**
     * Call on ACTION_MOVE event to update diameter line end,
     * which if where the finger is currently located
     */
    private suspend fun updateEndDiameter(endX: Float, endY: Float) {
        val newPoint = (lastLinesDataToDisplay ?: LinesDisplayData()).copy(
            diameterEndX = endX,
            diameterEndY = endY,
        )
        lastLinesDataOnImage = lastLinesDataOnImage?.copy(
            diameterEndX = convertCanvasXToImageX(newPoint.diameterEndX),
            diameterEndY = convertCanvasYToImageY(newPoint.diameterEndY),
        )
        updatePointAndMeasureData(newPoint)
    }

    /**
     * When configuration is changed take data in image coordinates
     * and convert it to canvas coordinates for corresponding orientation.
     * Data in image coordinates doesn't change on orientation changes.
     */
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

    private suspend fun updatePointAndMeasureData(newDisplayPoint: LinesDisplayData) {
        lastLinesDataToDisplay = newDisplayPoint
        linesDataFlow.emit(newDisplayPoint)
        val newMeasurement = getMeasuredData()
        linesMeasurementFlow.emit(newMeasurement)
    }

    private fun getMeasuredData(): LinesMeasurementData {
        val lengthValue: Float? = ifAllNotNull(
            lastLinesDataOnImage?.lengthStartX, lastLinesDataOnImage?.lengthStartY,
            lastLinesDataOnImage?.lengthEndX, lastLinesDataOnImage?.lengthEndY,
        ) { xA, yA, xB, yB ->
            sqrt((xA - xB).pow(2) + (yA - yB).pow(2))
        }

        val diameterValue: Float? = ifAllNotNull(
            lastLinesDataOnImage?.diameterStartX, lastLinesDataOnImage?.diameterStartY,
            lastLinesDataOnImage?.diameterEndX, lastLinesDataOnImage?.diameterEndY,
        ) { xA, yA, xB, yB ->
            sqrt((xA - xB).pow(2) + (yA - yB).pow(2))
        }

        return LinesMeasurementData(lengthValue, diameterValue)
    }

    /**
     * Data validation:
     * 1. Both lines must be drawn.
     * 2. One of length line ends must be on the diameter circle (with some precision).
     *
     * Also it's good to check that length line don't intersect the diameter circle,
     * meaning that some of its part is inside the circle and it intersects the outer
     * stroke in 2 places. But it's not implemented here.
     */
    private fun validateData(): ValidityState {
        val lengthStartX = lastLinesDataToDisplay?.lengthStartX ?: return ValidityState.NO_LENGTH
        val lengthStartY = lastLinesDataToDisplay?.lengthStartY ?: return ValidityState.NO_LENGTH
        val lengthEndX = lastLinesDataToDisplay?.lengthEndX ?: return ValidityState.NO_LENGTH
        val lengthEndY = lastLinesDataToDisplay?.lengthEndY ?: return ValidityState.NO_LENGTH
        val diameterStartX = lastLinesDataToDisplay?.diameterStartX ?: return ValidityState.NO_DIAMETER
        val diameterStartY = lastLinesDataToDisplay?.diameterStartY ?: return ValidityState.NO_DIAMETER
        val diameterEndX = lastLinesDataToDisplay?.diameterEndX ?: return ValidityState.NO_DIAMETER
        val diameterEndY = lastLinesDataToDisplay?.diameterEndY ?: return ValidityState.NO_DIAMETER


        val circleCenterX = diameterEndX - diameterStartX
        val circleCenterY = diameterEndY - diameterStartY
        val radius: Float = sqrt((diameterEndX - diameterStartX).pow(2) + (diameterEndY - diameterStartY).pow(2)) / 2
        val distanceToLengthStart = sqrt((circleCenterX - lengthStartX).pow(2) + (circleCenterY - lengthStartY).pow(2)) / 2
        val distanceToLengthEnd = sqrt((circleCenterX - lengthEndX).pow(2) + (circleCenterY - lengthEndY).pow(2)) / 2

        return if (abs(distanceToLengthStart - radius) <= LINE_ON_CIRCLE_TOLERANCE
            || abs(distanceToLengthEnd - radius) <= LINE_ON_CIRCLE_TOLERANCE) ValidityState.VALID
        else ValidityState.INVALID_TOO_FAR
    }

    companion object {

        /**
         * Maximum distance between outer circle stroke and one of length line ends
         * for the measurement to be considered valid
         */
        const val LINE_ON_CIRCLE_TOLERANCE = 100f
    }
}