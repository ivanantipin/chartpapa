package firelib.common.model

import firelib.common.ModelInitResult
import firelib.common.ordermanager.OrderManager

/**

 */
trait Model {

    def properties: Map[String, String]

    def name: String

    def orderManagers: Seq[OrderManager]

    /**
     * init model method
     * returns false in case properties are invalid (sometimes this happen during optimization grid)
     * and model will be ignored during optimization
     */
    def initModel(modelProps: Map[String, String]) : ModelInitResult

    /**
     * called after backtest end
     */
    def onBacktestEnd()
}
