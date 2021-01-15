package firelib.stockviz.api

import javax.validation.constraints.Size

/**
 *
 * @param status
 * @param message
 */
data class AddResponse(

    val status: Status,

    @get:Size(min = 1)
    val message: String
) {

    /**
     *
     * Values: ok,error
     */
    enum class Status(val value: String) {

        ok("ok"),

        error("error");

    }

}

