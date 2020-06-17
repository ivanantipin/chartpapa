package firelib.model

import org.apache.commons.math3.distribution.NormalDistribution
import org.jtransforms.fft.DoubleFFT_1D


data class Participant(val volume : Int, val frac : Int, val start : Int)

fun getPart() : List<Participant>{
    return (0 until 100).map {
        Participant(10, 3, 0)
    }
}

fun genVector() : DoubleArray{
    val ret = DoubleArray(90, { 0.0 })

    val pp = getPart()

    val distr = NormalDistribution(1.0, 0.5)


    for( i in 0 until ret.size){
        ret[i] = pp.map {
            if(i % (it.frac + it.start) == 0){
                it.volume * distr.sample()
            }else{
                0.0
            }
        }.sum()
    }

    ret.forEachIndexed {idx, v->
        println("$idx  -> ${v}")
    }

//    ret.forEach {
//        println(it)
//    }


    // 10 % - every day - randow with mean 100
    // 20 % - once in 3 days - split in 3 parts - randow w
    // 70 % - once in 7 days



    return ret;
}

fun periods(signal : DoubleArray) : List<Pair<Double, Float>> {
    val floatFFT_1D = DoubleFFT_1D(signal.size.toLong())
    floatFFT_1D.realForward(signal)
    var localMax = Float.MIN_VALUE

    val result = FloatArray(signal.size / 2)

    for (s in result.indices) {
        val re = signal[s * 2]
        val im = signal[s * 2 + 1]
        result[s] = Math.sqrt(re * re + im * im).toFloat() / result.size
        localMax = Math.max(localMax, result[s])
    }

    return result.mapIndexed{idx, v->
        signal.size.toDouble()/idx to v
    }.sortedBy { -it.second }.subList(0,10)
}

fun main() {
    // Make 50 Hz signal
    // Make 50 Hz signal
//    val sampleRate = 44100
//    val signalFrequency = 50
//    val signal = FloatArray(sampleRate)
//    for (s in 0 until sampleRate) {
//        val t = s * (1 / sampleRate.toDouble())
//        signal[s] = Math.sin(2 * Math.PI * signalFrequency * t).toFloat()
//    }

    // Execute FFT

    val sig = genVector()

    val signal = sig.flatMap { e->DoubleArray(1, {e}).toList() }.toDoubleArray()

    println(periods(signal))


}