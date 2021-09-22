package com.firelib.techbot.command

import firelib.core.misc.JsonHelper
import java.util.*
import kotlin.math.absoluteValue


fun main() {
    val message = JsonHelper.mapper.readTree(incomeChunk)
    val mp: List<Pair<String, Double>> = message
        .fields()
        .asSequence()
        .associateBy({ it.key }, { it.value["value"].numberValue().toDouble() })
        .mapValues {
        try {
            it.value.toDouble()
        } catch (e: Exception) {
            Double.NaN
        }
    }.filterValues { !it.isNaN() && it.absoluteValue > 0.0005 }.toList()

    find(Stack<Pair<String,Double>>(), 5, mp, mutableSetOf())
}

fun find(stack : Stack<Pair<String,Double>>, lvl : Int, data : List<Pair<String, Double>>, result : MutableSet<Set<String>>){
    val diff = stack.sumOf { it.second }.absoluteValue
    if(diff < 0.005 && lvl < 5){
        val kkNorm = stack.map { it.first.replace("-", "") }.toSet()
        if(result.add(kkNorm)){
            println(stack.map { it.first } + " diff is ${diff}")
        }
        return
    }

    if(lvl == 0){
        return
    }
    data.forEach { chk: Pair<String, Double> ->
        if(!stack.any {  (it.second.absoluteValue - chk.second.absoluteValue).absoluteValue < 0.0001 }){
            val neg = "-${chk.first}" to -chk.second
            stack.push(neg)
            find( stack , lvl - 1, data, result)
            stack.pop()

            stack.push(chk)
            find( stack , lvl - 1, data, result)
            stack.pop()

        }


    }


}

val cashFlowChunk = """
{
        "net_cash_flow_from_investing_activities_continuing": {
          "label": "Net Cash Flow From Investing Activities, Continuing",
          "value": 1442900000,
          "unit": "USD",
          "order": 500
        },
        "net_cash_flow_continuing": {
          "label": "Net Cash Flow, Continuing",
          "value": 728400000,
          "unit": "USD",
          "order": 1200
        },
        "net_cash_flow": {
          "label": "Net Cash Flow",
          "value": 772400000,
          "unit": "USD",
          "order": 1100
        },
        "net_cash_flow_from_investing_activities": {
          "label": "Net Cash Flow From Investing Activities",
          "value": 1442900000,
          "unit": "USD",
          "order": 400
        },
        "net_cash_flow_from_operating_activities_continuing": {
          "label": "Net Cash Flow From Operating Activities, Continuing",
          "value": 151000000,
          "unit": "USD",
          "order": 200
        },
        "net_cash_flow_from_financing_activities": {
          "label": "Net Cash Flow From Financing Activities",
          "value": -865500000,
          "unit": "USD",
          "order": 700
        },
        "net_cash_flow_from_operating_activities": {
          "label": "Net Cash Flow From Operating Activities",
          "value": 151000000,
          "unit": "USD",
          "order": 100
        },
        "net_cash_flow_from_financing_activities_continuing": {
          "label": "Net Cash Flow From Financing Activities, Continuing",
          "value": -865500000,
          "unit": "USD",
          "order": 800
        },
        "exchange_gains_losses": {
          "label": "Exchange Gains/Losses",
          "value": 44000000,
          "unit": "USD",
          "order": 1000
        }
      }    
""".trimIndent()

val incomeChunk = """
    {
        "benefits_costs_expenses": {
          "label": "Benefits Costs and Expenses",
          "value": 1175400000,
          "unit": "USD",
          "order": 200
        },
        "income_loss_before_equity_method_investments": {
          "label": "Income/Loss Before Equity Method Investments",
          "value": 448900000,
          "unit": "USD",
          "order": 1300
        },
        "preferred_stock_dividends_and_other_adjustments": {
          "label": "Preferred Stock Dividends And Other Adjustments",
          "value": 0,
          "unit": "USD",
          "order": 3900
        },
        "diluted_earnings_per_share": {
          "label": "Diluted Earnings Per Share",
          "value": 0.81,
          "unit": "USD / shares",
          "order": 4300
        },
        "net_income_loss_available_to_common_stockholders_basic": {
          "label": "Net Income/Loss Available To Common Stockholders, Basic",
          "value": 368200000,
          "unit": "USD",
          "order": 3700
        },
        "operating_income_loss": {
          "label": "Operating Income/Loss",
          "value": 45200000,
          "unit": "USD",
          "order": 1100
        },
        "revenues": {
          "label": "Revenues",
          "value": 1632000000,
          "unit": "USD",
          "order": 100
        },
        "income_tax_expense_benefit": {
          "label": "Income Tax Expense/Benefit",
          "value": 24500000,
          "unit": "USD",
          "order": 2200
        },
        "basic_earnings_per_share": {
          "label": "Basic Earnings Per Share",
          "value": 0.82,
          "unit": "USD / shares",
          "order": 4200
        },
        "income_loss_from_continuing_operations_before_tax": {
          "label": "Income/Loss From Continuing Operations Before Tax",
          "value": 456600000,
          "unit": "USD",
          "order": 1500
        },
        "income_loss_from_equity_method_investments": {
          "label": "Income/Loss From Equity Method Investments",
          "value": 7700000,
          "unit": "USD",
          "order": 2100
        },
        "operating_expenses": {
          "label": "Operating Expenses",
          "value": -45200000,
          "unit": "USD",
          "order": 1000
        },
        "net_income_loss_attributable_to_parent": {
          "label": "Net Income/Loss Attributable To Parent",
          "value": 368200000,
          "unit": "USD",
          "order": 3500
        },
        "income_loss_from_continuing_operations_after_tax": {
          "label": "Income/Loss From Continuing Operations After Tax",
          "value": 432100000,
          "unit": "USD",
          "order": 1400
        },
        "interest_expense_operating": {
          "label": "Interest Expense, Operating",
          "value": 38600000,
          "unit": "USD",
          "order": 2700
        },
        "net_income_loss": {
          "label": "Net Income/Loss",
          "value": 371900000,
          "unit": "USD",
          "order": 3200
        },
        "costs_and_expenses": {
          "label": "Costs And Expenses",
          "value": 1630800000,
          "unit": "USD",
          "order": 600
        },
        "income_tax_expense_benefit_deferred": {
          "label": "Income Tax Expense/Benefit, Deferred",
          "value": -31900000,
          "unit": "USD",
          "order": 2400
        },
        "income_loss_from_discontinued_operations_net_of_tax": {
          "label": "Income/Loss From Discontinued Operations Net Of Tax",
          "value": -60200000,
          "unit": "USD",
          "order": 1600
        },
        "net_income_loss_attributable_to_noncontrolling_interest": {
          "label": "Net Income/Loss Attributable To Noncontrolling Interest",
          "value": 1800000,
          "unit": "USD",
          "order": 3300
        },
        "participating_securities_distributed_and_undistributed_earnings_loss_basic": {
          "label": "Participating Securities, Distributed And Undistributed Earnings/Loss, Basic",
          "value": 0,
          "unit": "USD",
          "order": 3800
        }
      }
""".trimIndent()

val bsChunk = """
{
        "noncurrent_liabilities": {
          "label": "Noncurrent Liabilities",
          "value": 4514300000,
          "unit": "USD",
          "order": 800
        },
        "other_than_fixed_noncurrent_assets": {
          "label": "Other Than Fixed Noncurrent Assets",
          "value": 11860200000,
          "unit": "USD",
          "order": 500
        },
        "fixed_assets": {
          "label": "Fixed Assets",
          "value": 3975500000,
          "unit": "USD",
          "order": 400
        },
        "assets": {
          "label": "Assets",
          "value": 29570500000,
          "unit": "USD",
          "order": 100
        },
        "equity_attributable_to_parent": {
          "label": "Equity Attributable To Parent",
          "value": 13552800000,
          "unit": "USD",
          "order": 1600
        },
        "equity": {
          "label": "Equity",
          "value": 13559200000,
          "unit": "USD",
          "order": 1400
        },
        "liabilities_and_equity": {
          "label": "Liabilities And Equity",
          "value": 29570500000,
          "unit": "USD",
          "order": 1900
        },
        "liabilities": {
          "label": "Liabilities",
          "value": 16011300000,
          "unit": "USD",
          "order": 600
        },
        "equity_attributable_to_noncontrolling_interest": {
          "label": "Equity Attributable To Noncontrolling Interest",
          "value": 6400000,
          "unit": "USD",
          "order": 1500
        },
        "noncurrent_assets": {
          "label": "Noncurrent Assets",
          "value": 15835700000,
          "unit": "USD",
          "order": 300
        },
        "current_liabilities": {
          "label": "Current Liabilities",
          "value": 11497000000,
          "unit": "USD",
          "order": 700
        },
        "current_assets": {
          "label": "Current Assets",
          "value": 13734800000,
          "unit": "USD",
          "order": 200
        }
      }
""".trimIndent()

