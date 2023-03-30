package com.ivanzhur.timbertest.data.mapper

import android.net.Uri
import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.data.model.RecordEntity

fun RecordEntity.mapToModel() = RecordModel(id, Uri.parse(imageUri), lengthStartX, lengthStartY, lengthEndX, lengthEndY, diameterStartX, diameterStartY, diameterEndX, diameterEndY, lengthValue, diameterValue)

fun RecordModel.mapToEntity() = RecordEntity(id, imageUri.toString(), lengthStartX, lengthStartY, lengthEndX, lengthEndY, diameterStartX, diameterStartY, diameterEndX, diameterEndY, lengthValue, diameterValue)