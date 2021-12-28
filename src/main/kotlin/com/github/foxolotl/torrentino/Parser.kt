package com.github.foxolotl.torrentino

private data class EpisodeInfo(
    val season: Int?,
    val episodeNumber: Int?,
    val episodeFraction: Int?,
    val stripEndCrap: Regex
)

fun Episode.Companion.parse(title: String): Episode {
    val titleWithoutExtension = title.replace(Regexes.fileExtension, "")
    val releaseGroup = Regexes.releaseGroup.extractFrom(titleWithoutExtension)

    val seasonAndEpisode = Regexes.seasonAndEpisode.find(titleWithoutExtension)
    val (season, episodeNumber, episodeFraction, stripEndCrap) = if (seasonAndEpisode != null) {
        EpisodeInfo(
            seasonAndEpisode.groupValues[1].toIntOrNull(),
            seasonAndEpisode.groupValues[2].toIntOrNull(),
            null,
            Regexes.seasonAndEpisode
        )
    } else {
        val episodeNumber = Regexes.episodeNumber.extractFrom(titleWithoutExtension)?.toIntOrNull()
        val episodeFraction =
            Regexes.episodeNumber.first.find(titleWithoutExtension)?.groupValues?.get(6)?.drop(1)?.toIntOrNull()
        val season = Regexes.season.extractFrom(titleWithoutExtension)?.toIntOrNull()
        EpisodeInfo(season, episodeNumber, episodeFraction, Regexes.episodeNumber.first)
    }
    val resolution = Regexes.resolution.extractFrom(titleWithoutExtension)?.toResolutionOrNull()
    val seriesName = titleWithoutExtension
        .replace(Regexes.releaseGroup, "")
        .replace(stripEndCrap, "")
        .trim()

    return Episode(
        Series(
            seriesName,
            season,
            releaseGroup,
            resolution
        ),
        episodeNumber,
        episodeFraction
    )
}

private fun String.toResolutionOrNull(): Resolution? =
    when (filter { it.isDigit() }.toIntOrNull()) {
        4 -> Resolution.`4k`
        480 -> Resolution.`480p`
        540 -> Resolution.`540p`
        720 -> Resolution.`720p`
        1080 -> Resolution.`1080p`
        else -> null
    }

private object Regexes {
    private const val encodingTechnoBabble: String =
        "((hevc|[hx]\\.?26[45]|[0-9]+-?bit|[0-9]+fps|nvenc|flac|bd|bdrip|web|aac|aac-eac3|[0-9]+p)\\s*)"
    private const val endCrap: String =
        "((\\s*(\\[[^\\[\\]]+]|$encodingTechnoBabble|\\([^()]+\\))\\s*)+.*)"

    val fileExtension = Regex("\\.[a-zA-Z0-9]+$")
    val releaseGroup = Regex("^\\[([a-zA-Z0-9]+)]")
    val season = Regex("S([0-9]+)(\\s*-\\s*|E)[0-9]+$endCrap$") to 1
    val episodeNumber = Regex(
        "((-\\s*)?episode|-|(S[0-9]+\\s*-)|(-\\s*)?S[0-9]+E)\\s*([0-9]+)(\\.5)?(\\s*v[0-9])?(\\s*END)?$endCrap?$",
        RegexOption.IGNORE_CASE
    ) to 5
    val resolution = Regex(
        "[$encodingTechnoBabble*(\\[](480p|540p|720p|1080p|4k)[])$encodingTechnoBabble*]",
        RegexOption.IGNORE_CASE
    )
    val seasonAndEpisode = Regex("[\\s-]*S([0-9]+)E([0-9]+).*")
}

private fun Regex.extractFrom(string: String): String? =
    (this to 1).extractFrom(string)

private fun Pair<Regex, Int>.extractFrom(string: String): String? =
    first.find(string)?.groupValues?.get(second)
