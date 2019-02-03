package com.funstat.sequenta

import firelib.common.misc.atUtc
import firelib.domain.Ohlc
import java.time.LocalDateTime
import java.util.*


class Sequenta {
     var counts = intArrayOf(13, 21)

     var data: MutableList<Ohlc> = ArrayList()

     var pendingSetups: MutableList<Setup> = ArrayList()

     val currentTrend: Boolean
        get() {
            return past(1).close > past(5).close
        }

     var currentSetup: Setup = Setup(0,0,false)

     fun past(bars: Int): Ohlc {
        return data[data.size - bars]
    }

    inner class Setup(var start: Int, var end: Int, var up: Boolean) {
        var countDowns: MutableList<Int> = ArrayList()
         var pendingSignal = 0
         var cancelledRef: Setup? = null
         var recycleRef: Setup? = null

         var closesBeyondTdst = 0

         var min: Double = 0.toDouble()
         var max: Double = 0.toDouble()

        val tdst: Double
            get() = if (up) min else max

         val isCancelled: Boolean
            get() = cancelledRef != null

         val isCompleted: Boolean
            get() = pendingSignal == counts.size

         val isExpired: Boolean
            get() = data.size - start > 150


        val completedSignal: Int
            get() = if (pendingSignal == 0) {
                -1
            } else counts[pendingSignal - 1]

         fun updateEnd(idx: Int) {
            end = idx
            calcRange()
        }

        fun getStart(): LocalDateTime {
            return data[start].dtGmtEnd.atUtc()
        }

        fun getEnd(): LocalDateTime {
            return data[end].dtGmtEnd.atUtc()
        }

        fun setupSize(): Int {
            return end - start
        }


         fun calcRange() {
            this.min = java.lang.Double.MAX_VALUE
            this.max = java.lang.Double.MIN_VALUE
            for (i in start..end) {
                min = Math.min(data[i].low, min)
                max = Math.max(data[i].high, max)
            }
        }

         fun range(): Double {
            return max - min
        }

         fun length(): Int {
            return end - start
        }

        fun recycleRatio(): Optional<Double> {

            return if (recycleRef == null) {
                Optional.empty()
            } else Optional.of(recycleRef!!.range() / this.range())
        }

         fun reached(): Boolean {
            return end - start >= SETUP_LENGTH - 1
        }

         fun invalidated(): Boolean {
            return closesBeyondTdst > 5
        }

         fun checkCountDown(): List<Signal> {
            val idx = data.size - 1
            val ret = ArrayList<Signal>()

            checkClosesBeyondTdst(idx)

            if (isCntdn(idx)) {
                countDowns.add(idx)
                ret.add(Signal(SignalType.Cdn, this))
                if (countDowns.size >= counts[pendingSignal]) {
                    if (up && data[idx].high > data[countDowns[8]].close || !up && data[idx].low < data[countDowns[8]].close) {
                        pendingSignal++
                        ret.add(Signal(SignalType.Signal, this))
                    } else {
                        ret.add(Signal(SignalType.Deffered, this))
                    }
                }
            }
            if (isCompleted) {
                ret.add(Signal(SignalType.Completed, this))
            }
            return ret
        }

        private fun checkClosesBeyondTdst(idx: Int) {
            if (up) {
                if (data[idx].close < tdst) {
                    closesBeyondTdst++
                }
            } else {
                if (data[idx].close > tdst) {
                    closesBeyondTdst++
                }
            }
        }


        private fun isCntdn(idx: Int): Boolean {
            return up && getClose(idx) > data[idx - 2].high || !up && getClose(idx) < data[idx - 2].low
        }

        private fun getClose(idx: Int): Double {
            return data[idx].close
        }
    }

     fun last(idx: Int): Ohlc {
        return data[data.size - idx]
    }

    fun onOhlc(ohlc: Ohlc): List<Signal> {
        data.add(ohlc)
        if (data.size < 5) {
            return emptyList()
        }
        if (data.size == 5) {
            currentSetup = Setup(data.size - 1, data.size - 1, currentTrend)
            return emptyList()
        }
        val ret = ArrayList<Signal>()
        ret.addAll(runCurrent())

        this.pendingSetups.forEach { ps -> ret.addAll(ps.checkCountDown()) }
        this.pendingSetups = this.pendingSetups.filter{ ps -> !ps.isCompleted && !ps.isExpired && !ps.invalidated() }.toMutableList()
        return ret
    }


    private fun runCurrent(): List<Signal> {
        val ret = ArrayList<Signal>()
        val idx = data.size - 1
        currentSetup.updateEnd(idx)
        if (currentTrend != currentSetup.up) {
            if (!currentSetup.reached()) {
                ret.add(Signal(SignalType.SetupUnreach, currentSetup))
            } else {
                ret.add(Signal(SignalType.Flip, currentSetup))
            }
            currentSetup = Setup(idx, idx, currentTrend)
            return ret
        } else {
            ret.add(Signal(SignalType.SetupCount, currentSetup))
        }
        if (currentSetup.reached() && !pendingSetups.contains(currentSetup)) {
            pendingSetups.add(currentSetup)
            ret.add(Signal(SignalType.SetupReach, currentSetup))
        }

        for (i in 0 until pendingSetups.size - 1) {
            val tchk = pendingSetups[i]
            if (tchk === currentSetup) {
                break
            }
            if (currentSetup.up == tchk.up && currentSetup.range() > tchk.range()) {
                if (tchk.recycleRef == null || tchk.recycleRef!!.range() < currentSetup.range()) {
                    tchk.recycleRef = currentSetup
                    ret.add(Signal(SignalType.Recycling, tchk, currentSetup))
                }
            }

            if (currentSetup.reached() && currentSetup.up != tchk.up) {
                if (tchk.cancelledRef == null || tchk.cancelledRef!!.range() < currentSetup.range()) {
                    tchk.cancelledRef = currentSetup
                    ret.add(Signal(SignalType.Cancel, tchk, currentSetup))
                }
            }
        }
        return ret
    }

    companion object {
        val SETUP_LENGTH = 9
    }
}
