package nl.ericmulder.krantenwijk.domain.model

/** The deliverer's route (wijk). There is exactly one in v1. */
data class Route(
    val name: String,
    val town: String? = null,
    val createdAtMillis: Long,
    val id: Long = 0,
)
