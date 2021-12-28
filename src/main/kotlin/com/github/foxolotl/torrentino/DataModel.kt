package com.github.foxolotl.torrentino

enum class Resolution {
    `480p`,
    `540p`,
    `720p`,
    `1080p`,
    `4k`
}

/**
 * Metadata for an episode of some series.
 */
data class Episode(
    val series: Series,
    val episodeNumber: Int?,
    val episodeFraction: Int?,
) {
    fun pretty(): String = listOfNotNull(
        series.releaseGroup?.let { "[$it]" },
        series.seriesName,
        episodeNumber?.let { "- $it${episodeFraction?.let { frac -> ".$frac" } ?: ""}" },
        series.resolution?.let { "($it)" }
    ).joinToString(" ")

    // For extension methods
    companion object
}

/**
 * Metadata for an instance of some series, including release group and resolution if available.
 */
data class Series(
    val seriesName: String,
    val season: Int?,
    val releaseGroup: String?,
    val resolution: Resolution?
) {
    fun pretty(): String = listOfNotNull(
        releaseGroup?.let { "[$it]" },
        seriesName,
        resolution?.let { "($it)" }
    ).joinToString(" ")
}
