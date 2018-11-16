package firelib.common.core

import scala.collection.mutable.ArrayBuffer

/**
 * runs backtest for provided model config
 * uses default behaviour
 * to customize reader factory how time bounds calculated need to reimplement factories
 */

trait OnContextInited{
    val initMethods = new ArrayBuffer[()=>Unit]

    def init() : Unit = initMethods.foreach(_())
}
