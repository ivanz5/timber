package com.ivanzhur.timbertest.fragment.measure

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.*
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ivanzhur.timbertest.R
import com.ivanzhur.timbertest.core.fragment.BaseFragmentWithViewModel
import com.ivanzhur.timbertest.databinding.FragmentMeasurementBinding
import com.ivanzhur.timbertest.model.LinesDisplayData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class MeasurementFragment : BaseFragmentWithViewModel<FragmentMeasurementBinding, MeasurementViewModel>() {

    private val args: MeasurementFragmentArgs by navArgs()

    private val lengthPaint by lazy {
        Paint().apply {
            color = Color.YELLOW
            strokeWidth = 4 * resources.displayMetrics.density
        }
    }

    private val diameterPaint by lazy {
        Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4 * resources.displayMetrics.density
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewModelCreated() {
        ui.image.setImageURI(args.imageUri)

        setupInitialData()

        ui.lengthButton.setOnClickListener {
            viewModel.onLengthClick()
        }

        ui.diameterButton.setOnClickListener {
            viewModel.onDiameterClick()
        }

        ui.image.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> viewModel.onTouchDown(motionEvent.x, motionEvent.y)
                MotionEvent.ACTION_MOVE -> viewModel.onMove(motionEvent.x, motionEvent.y)
                MotionEvent.ACTION_UP -> viewModel.onTouchUp(motionEvent.x, motionEvent.y)
            }
            return@setOnTouchListener true
        }
    }

    private fun setupInitialData() {
        // Get bitmap of selected image to know its size
        val inputStream = requireContext().contentResolver.openInputStream(args.imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Use .post so that View will be laid out and measured at this time.
        // canvasImage and image Views are of the same size
        ui.canvasImage.post {
            viewModel.setImageData(
                bitmap.width, bitmap.height,
                ui.canvasImage.measuredWidth, ui.canvasImage.measuredHeight,
                resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT,
            )
        }
    }

    override fun observeLiveData() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            val lengthButtonResId =
                if (state == MeasurementViewModel.State.DRAWING_LENGTH) R.string.measurement_button_reset_length
                else R.string.measurement_button_length
            val diameterButtonResId =
                if (state == MeasurementViewModel.State.DRAWING_DIAMETER) R.string.measurement_button_reset_diameter
                else R.string.measurement_button_diameter
            ui.lengthButton.setText(lengthButtonResId)
            ui.diameterButton.setText(diameterButtonResId)
        }

        lifecycleScope.launch {
            viewModel.linesDataFlow.sample(UPDATE_INTERVAL).collect { linesData ->
                linesData?.let { drawLines(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.linesMeasurementFlow.sample(UPDATE_INTERVAL).collect { measurementData ->
                measurementData?.let {
                    val stringBuilder = StringBuilder()
                    if (it.lengthValue != null)
                        stringBuilder.append(getString(R.string.measurement_result_length, it.lengthValue))
                    if (it.diameterValue != null)
                        stringBuilder.append(getString(R.string.measurement_result_diameter, it.diameterValue))
                    ui.results.text = stringBuilder.toString()
                }
            }
        }
    }

    private fun drawLines(linesData: LinesDisplayData, ) {
        val bitmap = Bitmap.createBitmap(
            ui.canvasImage.measuredWidth,
            ui.canvasImage.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas()
        canvas.setBitmap(bitmap)

        pointNullCheck(
            linesData.lengthStartX, linesData.lengthStartY,
            linesData.lengthEndX, linesData.lengthEndY
        ) { startX, startY, endX, endY ->
            canvas.drawLine(startX, startY, endX, endY, lengthPaint)
        }

        pointNullCheck(
            linesData.diameterStartX, linesData.diameterStartY,
            linesData.diameterEndX, linesData.diameterEndY
        ) { startX, startY, endX, endY ->
            canvas.drawLine(startX, startY, endX, endY, diameterPaint)
            // Also draw a circle for diameter for better understanding
            val centerX = startX + (endX - startX) / 2
            val centerY = startY + (endY - startY) / 2
            val radius = sqrt((endX - startX).pow(2) + (endY - startY).pow(2)) / 2
            canvas.drawCircle(centerX, centerY, radius, diameterPaint)
        }

        ui.canvasImage.setImageBitmap(bitmap)
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

    override fun getViewModelClass() = MeasurementViewModel::class.java

    override fun setupViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMeasurementBinding.inflate(inflater, container, false)

    companion object {
        const val UPDATE_INTERVAL = 20L // Update UI every 20 milliseconds
    }
}