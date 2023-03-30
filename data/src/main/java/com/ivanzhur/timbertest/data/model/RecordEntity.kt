package com.ivanzhur.timbertest.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val imageUri: String,
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
