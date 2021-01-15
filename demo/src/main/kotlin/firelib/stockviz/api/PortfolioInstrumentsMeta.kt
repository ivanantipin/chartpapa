package firelib.stockviz.api

import javax.validation.Valid

/**
 *
 * @param continuousMetas
 * @param discreteMetas
 */
data class PortfolioInstrumentsMeta(

    @field:Valid
    val continuousMetas: List<ContinuousMeta>,

    @field:Valid
    val discreteMetas: List<DiscreteMeta>
)

