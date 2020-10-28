package com.firelib.techbot.domain

enum class LineType(val priceCmp: (price: Double, other: Double) -> Boolean) {
    Support({ price, other -> price < other }), Resistance({ price, other -> price > other });
}