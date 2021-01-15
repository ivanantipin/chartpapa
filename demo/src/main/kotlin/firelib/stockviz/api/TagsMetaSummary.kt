package firelib.stockviz.api

data class TagsMetaSummary(
    val continuousMetas: List<ContinuousMeta>,
    val discreteMetas: List<DiscreteMeta>
)

