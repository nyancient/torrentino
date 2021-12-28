package com.github.foxolotl.torrentino.persistence

import cc.ekblad.toml.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.serialization.write
import cc.ekblad.toml.transcoding.TomlTranscoder
import cc.ekblad.toml.transcoding.decode
import com.github.foxolotl.torrentino.Episode
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import org.slf4j.LoggerFactory

/**
 * Seen list backed by a TOML file.
 * The file is only read on initialization, and is overwritten periodically; new entries can not be added to an
 * existing `TomlSeenList` by editing its backing file.
 */
class TomlSeenList(
    private val transcoder: TomlTranscoder,
    private val filePath: Path
) : SeenList {
    private data class SeenList(val seen: MutableSet<Episode>)
    private val logger = LoggerFactory.getLogger(javaClass)

    private val seenEpisodes: MutableSet<Episode> by lazy {
        require(!filePath.isDirectory()) {
            "path $filePath is a directory"
        }

        if (filePath.isRegularFile()) {
            TomlValue.from(filePath).decode<SeenList>(transcoder.decoder).seen
        } else {
            mutableSetOf()
        }
    }

    override fun <T> filterEpisodes(episodes: List<T>, selector: (T) -> Episode): Set<T> =
        episodes.filter { selector(it) !in seenEpisodes }.toSet()

    override fun markAsSeen(episodes: Set<Episode>) {
        val newEpisodes = episodes - seenEpisodes
        if (newEpisodes.isNotEmpty()) {
            logger.info("Marking episodes as seen: {}", newEpisodes.joinToString { it.pretty() })
            seenEpisodes += episodes
            try {
                safelyOverwrite(filePath) {
                    (transcoder.encode(SeenList(seenEpisodes)) as TomlValue.Map).write(it)
                }
            } catch (e: Exception) {
                logger.error("Unable to persist seen list", e)
            }
        } else {
            logger.debug("No new episodes to mark as seen")
        }
    }
}
