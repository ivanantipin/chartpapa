package firelib.common

import java.time.Instant

import firelib.common.misc.RollingQuantile
import firelib.indicators.Donchian
import org.apache.commons.math3.distribution.NormalDistribution
import org.junit.{Assert, Test}

class QuantilesTest {
    @Test
    def TestHeapOnNormal() = {

        var calc = new RollingQuantile(0.1, 30000)

        val gaussian = new NormalDistribution(0, 1)



        for (i <- 0 until 60000) {
            calc.addMetric(gaussian.sample())
        }

        Assert.assertEquals(gaussian.inverseCumulativeProbability(0.1), calc.value, 0.05);

    }
}


class DonchianTest {
    @Test
    def TestDonchian() = {

        var calc = new Donchian(2,false)

        val start = Instant.ofEpochSecond(Instant.now().getEpochSecond)

        calc.addMetric(start,1.0)
        calc.addMetric(start.plusMillis(500),2.0)
        Assert.assertEquals(1.0,calc.value,0.0001)

        calc.addMetric(start.plusMillis(1000),0.5)
        Assert.assertEquals(0.5,calc.value,0.0001)

        calc.addMetric(start.plusMillis(4001),1.0)
        Assert.assertEquals(1.0,calc.value,0.0001)


    }
}
