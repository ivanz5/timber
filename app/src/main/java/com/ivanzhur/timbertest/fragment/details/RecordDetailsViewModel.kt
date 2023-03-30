package com.ivanzhur.timbertest.fragment.details

import android.content.Context
import android.graphics.*
import androidx.lifecycle.MutableLiveData
import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.core.viewmodel.BaseViewModel
import com.ivanzhur.timbertest.data.repository.contract.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class RecordDetailsViewModel @Inject constructor(
    private val storageRepository: StorageRepository
) : BaseViewModel() {

    val bitmapLiveData = MutableLiveData<Bitmap>()

    val recordLiveData = MutableLiveData<RecordModel>()

    fun loadData(recordId: Int, context: Context) = launch(Dispatchers.IO) {
        val record = storageRepository.getRecord(recordId)
        recordLiveData.postValue(record)

        // Get bitmap of selected image to know its size
        val inputStream = context.contentResolver.openInputStream(record.imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        val bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas()
        canvas.setBitmap(bitmap)

        val lengthPaint =  Paint().apply {
            color = Color.YELLOW
            strokeWidth = 4 * context.resources.displayMetrics.density
        }

        val diameterPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4 * context.resources.displayMetrics.density
        }

        pointNullCheck(
            record.lengthStartX, record.lengthStartY,
            record.lengthEndX, record.lengthEndY
        ) { startX, startY, endX, endY ->
            canvas.drawLine(startX, startY, endX, endY, lengthPaint)
        }

        pointNullCheck(
            record.diameterStartX, record.diameterStartY,
            record.diameterEndX, record.diameterEndY
        ) { startX, startY, endX, endY ->
            canvas.drawLine(startX, startY, endX, endY, diameterPaint)
            // Also draw a circle for diameter for better understanding
            val centerX = startX + (endX - startX) / 2
            val centerY = startY + (endY - startY) / 2
            val radius = sqrt((endX - startX).pow(2) + (endY - startY).pow(2)) / 2
            canvas.drawCircle(centerX, centerY, radius, diameterPaint)
        }

        bitmapLiveData.postValue(bitmap)
    }

    private fun pointNullCheck(
        first: Float?,
        second: Float?,
        third: Float?,
        fourth: Float?,
        block: (Float, Float, Float, Float) -> Unit,
    ) {
        if (first != null && second != null && third != null && fourth != null) block(first, second, third, fourth)
    }
}