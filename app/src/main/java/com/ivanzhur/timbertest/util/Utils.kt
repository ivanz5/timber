package com.ivanzhur.timbertest.util

fun <T> ifAllNotNull(
    first: Float?,
    second: Float?,
    third: Float?,
    fourth: Float?,
    block: (Float, Float, Float, Float) -> T,
): T? {
    return if (first != null && second != null && third != null && fourth != null) block(first, second, third, fourth)
    else null
}