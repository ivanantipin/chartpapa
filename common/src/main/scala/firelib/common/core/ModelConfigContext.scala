package firelib.common.core

import firelib.common.config.ModelBacktestConfig

/**
 * component of BacktestEnvironment factory for dependency injection
 */

trait ModelConfigContext{
    val modelConfig : ModelBacktestConfig
}

