package com.firelib.techbot.chart

data class Series<T>(val name : String, val data : Map<T, Double>)

fun <T : Comparable<T>> Series<T>.toSortedList(): List<Pair<T,Double>> {
    return data.toSortedMap().map { it.key to it.value }
}