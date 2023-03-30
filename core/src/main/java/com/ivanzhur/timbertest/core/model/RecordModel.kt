package com.ivanzhur.timbertest.core.model

import android.net.Uri

data class RecordModel(
    val id: Int = 0,
    val imageUri: Uri,
    val lengthStartX: Float,
    val lengthStartY: Float,
    val lengthEndX: Float,
    val lengthEndY: Float,
    val diameterStartX: Float,
    val diameterStartY: Float,
    val diameterEndX: Float,
    val diameterEndY: Float,
    val lengthValue: Float,
    val diameterValue: Float,
)
