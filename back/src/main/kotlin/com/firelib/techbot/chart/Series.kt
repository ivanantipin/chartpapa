package com.firelib.techbot.chart

data class Series<T>(val name : String, val data : Map<T, Double>)