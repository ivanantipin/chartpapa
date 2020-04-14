package firelib.core.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.core.ModelFactory
import firelib.core.backtest.opt.OptimizedParameter
import firelib.core.Model
import kotlin.reflect.KClass

class ModelConfig(
    @get:JsonIgnore
    val modelKClass: KClass<out Model>,
    val runConfig : ModelBacktestConfig
) {


    @get:JsonIgnore
    var factory: ModelFactory =
        defaultModelFactory(modelKClass)

    /**
     * params passed to model apply method
     * can not be optimized
     */
    val modelParams: MutableMap<String, String> = mutableMapOf()

    /*
    * optimization config, used only for BacktestMode.Optimize
     */
    val optConfig: OptimizationConfig =
        OptimizationConfig()

    fun opt(name: String, start: Int, end: Int, step: Int) {
        optConfig.params += OptimizedParameter(name, start, end, step)

    }

    fun param(name: String, value: Int) {
        modelParams += (name to value.toString())
    }

}