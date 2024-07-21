package firelib.model

import java.time.LocalDate

data class Div(val ticker: String, val lastDayWithDivs: LocalDate, val div: Double, val status : String)
