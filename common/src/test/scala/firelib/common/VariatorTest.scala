package firelib.common

import firelib.common.opt.{OptimizedParameter, ParamsVariator}
import org.junit.{Assert, Test}

import scala.collection.mutable

/**

 */
class VariatorTest {

    @Test
    def TestOptVariator() = {
        val variator = new ParamsVariator(List(

            new OptimizedParameter("p0", 0, 2),
            new OptimizedParameter("p1", 0, 3)
        ))

        Assert.assertEquals(6, variator.combinations)

        var set = new mutable.HashSet[(Int, Int)]() ++ List(
            (0, 0),
            (0, 1),
            (1, 0),
            (1, 1),
            (0, 2),
            (1, 2))


        while (variator.hasNext()) {
            val dd = variator.next
            var key = (dd("p0"), dd("p1"))
            Assert.assertTrue(set.contains(key))
            set -= key
        }
        Assert.assertEquals(0, set.size)
    }

}
