package com.github.foxolotl.torrentino

import com.github.foxolotl.torrentino.persistence.TomlSeenList
import com.github.foxolotl.torrentino.persistence.TomlWatchList
import com.github.foxolotl.torrentino.persistence.tomlTranscoder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.coroutines.runBlocking

class CommandLine(parser: ArgParser) {
    val config: String? by parser.option(
        type = ArgType.String,
        shortName = "c",
        description = "File from which to read configuration"
    )

    val watch: Boolean by parser.option(
        type = ArgType.Boolean,
        shortName = "w",
        description = "Start the watcher (i.e. just sit around checking for new episodes every N minutes) " +
                "instead of the user interface"
    ).default(false)
}

fun main(args: Array<String>) {
    // Handle command line arguments
    val argParser = ArgParser("torrentino")
    val commandLine = CommandLine(argParser)
    argParser.parse(args)

    // Read configuration
    val configPath = commandLine.config?.let { Paths.get(it) }
        ?: Paths.get("config.toml")
    val config = Config.load(configPath)

    // Set up dependencies
    val httpClient = HttpClient(CIO)
    val sources = config.sources.map { Source(httpClient, it) }
    val watchList = TomlWatchList(tomlTranscoder, Path.of(config.globalSettings.watchFile ?: "watch.toml"))
    val seenList = TomlSeenList(tomlTranscoder, Path.of(config.globalSettings.seenFile ?: "seen.toml"))
    val downloader = Downloader(httpClient, config.globalSettings, sources, seenList, watchList)

    if (commandLine.watch) {
        val scheduler = Scheduler.create()
        scheduler.schedule(Duration.ofMinutes(config.globalSettings.refresh ?: 60), true) {
            runBlocking {
                downloader.downloadAll()
            }
        }
    } else {
        val client = CommandLineClient(watchList, sources, downloader)
        client.run()
    }
}
