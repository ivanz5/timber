package com.ivanzhur.timbertest.data.mapper

import com.ivanzhur.timbertest.core.model.RecordModel
import com.ivanzhur.timbertest.data.model.RecordEntity

fun RecordEntity.mapToModel() = RecordModel(id)

fun RecordModel.mapToEntity() = RecordEntity(id)