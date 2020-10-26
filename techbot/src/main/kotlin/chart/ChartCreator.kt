package chart

import firelib.core.domain.Ohlc
import kotlinx.serialization.Serializable

@Serializable
data class HRangeSelector(val enabled: Boolean)

@Serializable
data class HMarker(val enabled: Boolean)

@Serializable

data class HSeries(
    var type: String,
    var name: String,
    var marker: HMarker,
    var data: List<Array<Double>> = mutableListOf(),
    val showInLegend: Boolean,
    val color : String? = null,
    val dashStyle : String? = null,
    val lineWidth: Int? = null
)

@Serializable
data class HChart(var zoomType: String)

@Serializable
data class HAxis(
    val height: String = "100%",
    val lineWidth: Int = 1,
    val title: HTitle? = null,
    val gapGridLineWidth: Int = 0
)

@Serializable
data class HTitle(var text: String)

@Serializable
data class HNavigator(val enabled: Boolean)

@Serializable
data class HScrollbar(val enabled: Boolean)



@Serializable
data class HOptions(
    val title: HTitle? = null,
    val rangeSelector: HRangeSelector? = null,
    val chart: HChart? = null,
    val yAxis: MutableList<HAxis> = mutableListOf(),
    var xAxis: HAxis? = null,
    val series: MutableList<HSeries> = mutableListOf(),
    var navigator : HNavigator? = null,
    var scrollbar : HScrollbar? = null,
    val legend : HLegend? = null

)

val defaultLegend = HLegend(
    floating = true,
    layout = "vertical",
    borderWidth = 1,
    borderColor = "#444444",
    align = "left",
    x = 80,
    verticalAlign = "top",
    y = 30
)

@Serializable
data class HLegend(
    val floating: Boolean,
    val layout: String,
    val borderWidth: Int,
    val borderColor: String,
    val align: String,
    val x: Int,
    val verticalAlign: String,
    val y: Int
)




object ChartCreator{
    fun makeOptions(ohlc: List<Ohlc>, ticker : String): HOptions {
        val data = ohlc.mapIndexed{idx, it-> arrayOf(it.endTime.toEpochMilli().toDouble(), it.open, it.high, it.low, it.close) }
        return HOptions(
            title = HTitle(ticker),
            rangeSelector = HRangeSelector(false),
            legend = defaultLegend
        ).apply {
            yAxis += HAxis(height = "100%", lineWidth = 1, title = null)
            xAxis = HAxis(gapGridLineWidth = 1, lineWidth = 1)
            series += HSeries("ohlc", ticker, data = data, marker = HMarker(true), showInLegend = true)
            navigator = HNavigator(false)
            scrollbar = HScrollbar(false)
        }
    }
}