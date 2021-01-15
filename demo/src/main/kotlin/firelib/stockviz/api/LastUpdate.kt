package firelib.stockviz.api

import javax.validation.constraints.Max
import javax.validation.constraints.Size

data class LastUpdate(

    val what: What,

    @get:Size(min = 1, max = 25)
    val portfolio: String,

    val id: kotlin.Int? = null,

    @get:Max(9223372036854775807)
    val `when`: kotlin.Int? = null
) {

    /**
     *
     * Values: instruments,trades,orders
     */
    enum class What(val value: String) {

        instruments("instruments"),

        trades("trades"),

        orders("orders");

    }

}

