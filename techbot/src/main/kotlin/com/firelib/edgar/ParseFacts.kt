package com.firelib.edgar

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.firelib.techbot.chart.ChartCreator
import com.firelib.techbot.chart.ChartService
import java.time.LocalDate

class ParseFacts {


}

fun period(start : LocalDate, end : LocalDate) : String{
    val months = listOf(2, 5, 8, 11)
    val startMonth = if(start.monthValue == 12) 1 else start.monthValue
    val idx = months.indexOfFirst {
        startMonth < it && end.monthValue > it
    }
    if(idx < 0){
        if(end.toEpochDay()  - start.toEpochDay() < 100){
            println("ERRROR")

        }
        return "NA"
    }
    if(end.toEpochDay()  - start.toEpochDay() > 100){
        return "NA"
    }
    return start.year.toString() +  "Q" + (1 + idx)
}


fun convertToPoints(metric : String, node : JsonNode) : List<Triple<String,Double, JsonNode>>{
    return node
        .map {

            val start = LocalDate.parse(  it["start"].textValue())
            val end = LocalDate.parse(it["end"].textValue() )
            Triple(period(start, end), it["val"].doubleValue(), it)
        }.filter {  it.first != "NA"}.sortedBy { it.first }

}

fun main() {
    val tree = ObjectMapper().readTree(ParseFacts::class.java.getResourceAsStream("/facts1.json"))
    val arr = tree["facts"]["us-gaap"]["Revenues"]["units"]["USD"] as JsonNode
}