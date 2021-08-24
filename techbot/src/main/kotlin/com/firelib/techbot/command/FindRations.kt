package com.firelib.techbot.command

import firelib.core.misc.JsonHelper
import java.util.*
import kotlin.math.absoluteValue

val str = """
{
"date": "2021-06-30",
          "filing_date": "2021-07-29",
          "currency_symbol": "USD",
          "totalAssets": "10727900000.00",
          "intangibleAssets": "809700000.00",
          "earningAssets": null,
          "otherCurrentAssets": "295800000.00",
          "totalLiab": "7098600000.00",
          "totalStockholderEquity": "3586400000.00",
          "deferredLongTermLiab": "27900000.00",
          "otherCurrentLiab": "1422600000.00",
          "commonStock": "450600000.00",
          "retainedEarnings": "-4714000000.00",
          "otherLiab": "448000000.00",
          "goodWill": null,
          "otherAssets": "218300000.00",
          "cash": "854900000.00",
          "totalCurrentLiabilities": "3760900000.00",
          "netDebt": "2332500000.00",
          "shortTermDebt": "297700000.00",
          "shortLongTermDebt": "297700000.00",
          "shortLongTermDebtTotal": "3187400000.00",
          "otherStockholderEquity": "-1294900000.00",
          "propertyPlantEquipment": "3491700000.00",
          "totalCurrentAssets": "5884200000.00",
          "longTermInvestments": "324000000.00",
          "netTangibleAssets": "2776700000.00",
          "shortTermInvestments": "800500000.00",
          "netReceivables": "2703700000.00",
          "longTermDebt": "2180200000.00",
          "inventory": "1135800000.00",
          "accountsPayable": "1307200000.00",
          "totalPermanentEquity": null,
          "noncontrollingInterestInConsolidatedEntity": null,
          "temporaryEquityRedeemableNoncontrollingInterests": null,
          "accumulatedOtherComprehensiveIncome": "-2423600000.00",
          "additionalPaidInCapital": null,
          "commonStockTotalEquity": "450600000.00",
          "preferredStockTotalEquity": null,
          "retainedEarningsTotalEquity": null,
          "treasuryStock": null,
          "accumulatedAmortization": null,
          "nonCurrrentAssetsOther": "178000000.00",
          "deferredLongTermAssetCharges": null,
          "nonCurrentAssetsTotal": "4843700000.00",
          "capitalLeaseObligations": "843400000.00",
          "longTermDebtTotal": null,
          "nonCurrentLiabilitiesOther": "1157500000.00",
          "nonCurrentLiabilitiesTotal": "3337700000.00",
          "negativeGoodwill": null,
          "warrants": null,
          "preferredStockRedeemable": null,
          "capitalSurpluse": "9144700000.00",
          "liabilitiesAndStockholdersEquity": "10727900000.00",
          "cashAndShortTermInvestments": "1614900000.00",
          "propertyPlantAndEquipmentGross": "3491700000.00",
          "accumulatedDepreciation": null,
          "netWorkingCapital": "2170600000.00",
          "netInvestedCapital": "6064300000.00",
          "commonStockSharesOutstanding": "450600000.00",
          "EnterpriseValue": "4814126080",
          "MarketCapitalization": "2979127040"
        }    
""".trimIndent()

fun main() {


    val message = JsonHelper.mapper.readTree(str)

    val map = message.fields().asSequence().associateBy({ it.key }, { it.value.textValue() })

    val mp: List<Pair<String, Double>> = map.mapValues {
        try {
            it.value.toDouble()
        } catch (e: Exception) {
            Double.NaN
        }
    }.filterValues { !it.isNaN() }.toList()



    val stack = Stack<String>()
    mp.forEach {
        stack.push(it.first)
        find(it.second, stack, 4, mp, mutableSetOf())
        stack.pop()
    }

}

fun find(sum : Double, stack : Stack<String>, lvl : Int, data : List<Pair<String, Double>>, result : MutableSet<Set<String>>){
    if(sum.absoluteValue < 1000 && lvl < 2){
        if(result.add(stack.toSet())){
            println(stack + " diff is ${sum.absoluteValue}")
        }

        return
    }
    if(lvl == 0){
        return
    }
    data.forEach {
        val neg = "-${it.first}"
        if(!stack.contains(it.first) && !stack.contains(neg)){
            stack.push(it.first)
            find(sum - it.second, stack , lvl - 1, data, result)
            stack.pop()
        }

        if(!stack.contains(neg) && !stack.contains(it.first)){
            stack.push(neg)
            find(sum + it.second, stack , lvl - 1, data, result)
            stack.pop()
        }

    }


}