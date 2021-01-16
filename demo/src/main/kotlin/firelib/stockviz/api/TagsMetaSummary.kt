package firelib.stockviz.api

import com.fasterxml.jackson.annotation.JsonInclude

data class TagsMetaSummary(
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val continuousMetas: List<ContinuousMeta>,
    @JsonInclude(JsonInclude.Include.ALWAYS)
    val discreteMetas: List<DiscreteMeta>
)

