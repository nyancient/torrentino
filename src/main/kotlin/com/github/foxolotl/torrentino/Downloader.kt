package com.github.foxolotl.torrentino

import com.github.foxolotl.torrentino.persistence.SeenList
import com.github.foxolotl.torrentino.persistence.WatchList
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.net.URLEncoder
import java.nio.file.Path
import kotlin.io.path.outputStream
import org.slf4j.LoggerFactory

class Downloader(
    private val httpClient: HttpClient,
    private val settings: Config.GlobalSettings,
    private val sources: List<Source>,
    private val seenList: SeenList,
    private val watchList: WatchList
) {
    suspend fun downloadAll(): Set<Episode> {
        val logger = LoggerFactory.getLogger(javaClass)
        logger.info("Checking for new episodes")

        val terms = watchList.getWatched().map { it.pretty() }
        val episodes = sources.flatMap { source ->
            terms.flatMap { source.search(it) }
        }
        val filteredEpisodes = seenList.filterEpisodes(episodes) { it.first }

        logger.info("Found new {} episodes; downloading", filteredEpisodes.size)
        val downloadedEpisodes = filteredEpisodes.mapNotNull { (episode, torrent) ->
            try {
                logger.info("Downloading torrent file '{}' (episode '{}')", torrent, episode.pretty())
                val response = httpClient.get<HttpResponse>(torrent)
                response.content.toInputStream().use { input ->
                    val torrentFile = episode.torrentFileName()
                    torrentFile.outputStream().use { input.copyTo(it) }
                }
                episode
            } catch (e: Exception) {
                logger.warn("Failed to download episode '$episode'", e)
                null
            }
        }.toSet()
        logger.info("Downloaded {} episodes", downloadedEpisodes.size)
        seenList.markAsSeen(downloadedEpisodes)
        return downloadedEpisodes
    }

    private fun Episode.torrentFileName(): Path {
        val urlEncodedName = URLEncoder.encode(pretty(), settings.charset)
        return Path.of(settings.torrentDirectory, "$urlEncodedName.torrent")
    }
}
