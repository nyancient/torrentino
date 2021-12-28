package com.github.foxolotl.torrentino.persistence

import cc.ekblad.toml.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.serialization.write
import cc.ekblad.toml.transcoding.TomlTranscoder
import cc.ekblad.toml.transcoding.decode
import com.github.foxolotl.torrentino.Series
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import org.slf4j.LoggerFactory

/**
 * Watch list backed by a TOML file.
 * The file is re-read upon each update and request for watched series; external systems may add new entries to an
 * existing `TomlWatchList` by editing the backing file.
 */
class TomlWatchList(
    private val transcoder: TomlTranscoder,
    private val filePath: Path
) : WatchList {
    private data class WatchList(val series: Set<Series>)
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getWatched(): Set<Series> = try {
        require(!filePath.isDirectory()) {
            "path $filePath is a directory"
        }
        TomlValue.from(filePath).decode<WatchList>(transcoder.decoder).series
    } catch (e: Exception) {
        logger.error("Unable to read watch list", e)
        emptySet()
    }

    override fun watch(items: Set<Series>) {
        if (items.isNotEmpty()) {
            update { it + items }
        }
    }

    override fun unwatch(items: Set<Series>) {
        if (items.isNotEmpty()) {
            update { it - items }
        }
    }

    private fun update(update: (Set<Series>) -> Set<Series>) = try {
        val watchList = getWatched()
        val newWatchList = update(watchList)
        if (newWatchList != watchList) {
            logger.info("Watch list was updated; writing to disk")
            safelyOverwrite(filePath) {
                (transcoder.encode(WatchList(newWatchList)) as TomlValue.Map).write(it)
            }
        } else {
            logger.info("Watch list unchanged; not writing it out")
        }
    } catch (e: Exception) {
        logger.error("Unable to persist watch list", e)
    }
}
