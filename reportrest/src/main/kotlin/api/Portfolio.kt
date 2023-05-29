package firelib.stockviz.api

import javax.validation.constraints.Size

/**
 *
 * @param name
 * @param description
 * @param createdDate
 * @param benchmark
 */
data class Portfolio(

    val name: String,

    val createdDate: Long,

)

