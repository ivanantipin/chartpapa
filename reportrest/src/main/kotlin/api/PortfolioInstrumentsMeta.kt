package firelib.stockviz.api

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.Valid

/**
 *
 * @param continuousMetas
 * @param discreteMetas
 */
data class PortfolioInstrumentsMeta(

    @field:Valid
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val continuousMetas: List<ContinuousMeta>,

    @field:Valid
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val discreteMetas: List<DiscreteMeta>
)

