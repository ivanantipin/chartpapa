package com.funstat.finam

/**
 * @author Denis Gabaydulin
 * @since 11.06.17
 */
enum class Period private constructor(val id: Int) {
    // TODO: add more periods
    FIVE_MINUTES(3),
    DAILY(8),
    TEN_MINUTES(4)
}
