package com.github.foxolotl.torrentino

import cc.ekblad.kotline.Kotline
import cc.ekblad.kotline.kotline
import cc.ekblad.kotline.menu.ask
import cc.ekblad.kotline.menu.select
import cc.ekblad.kotline.terminal.clearScreen
import cc.ekblad.kotline.terminal.cursorUp
import com.github.foxolotl.torrentino.persistence.WatchList
import kotlinx.coroutines.runBlocking

class CommandLineClient(
    private val watchList: WatchList,
    private val sources: List<Source>,
    private val downloader: Downloader
) {
    enum class Command { Search, Manage, Download, Quit }

    fun run() = kotline {
        while (true) {
            when (ask(Command.Search, Command.Manage, Command.Download, Command.Quit)) {
                Command.Search -> findSeries()
                Command.Manage -> manageWatchList()
                Command.Download -> downloadAll()
                Command.Quit -> return@kotline
            }
        }
    }

    private fun Kotline.findSeries() {
        val searchString = readLine("Search: ") ?: ""
        val series = sources.flatMap { it.search(searchString) }.map { it.first.series }.toSet()
        watchList.watch(selectSeries(series).toSet())
    }

    private fun Kotline.manageWatchList() {
        val series = watchList.getWatched()
        val selectedSeries = selectSeries(series, defaultSelectionState = true)
        val seriesToUnwatch = series - selectedSeries.toSet()
        watchList.unwatch(seriesToUnwatch)
    }

    private fun Kotline.selectSeries(series: Collection<Series>, defaultSelectionState: Boolean = false): List<Series> {
        val seriesList = series.sortedBy { it.seriesName }
        val seriesNameList = seriesList.map { it.pretty() }
        val selectedSeriesIndices = select(
            seriesNameList,
            prompt = "Select series to add to watch list",
            defaultSelectionState = defaultSelectionState
        )
        return selectedSeriesIndices.map { seriesList[it] }
    }

    private fun Kotline.downloadAll() {
        val downloadedEpisodes = runBlocking { downloader.downloadAll() }
        val sortedEpisodes = downloadedEpisodes.sortedBy { "${it.series.seriesName} - ${it.episodeNumber}" }
        if (sortedEpisodes.isNotEmpty()) {
            println("The following episodes were downloaded:")
            sortedEpisodes.forEach { println("  " + it.pretty()) }
        } else {
            println("No new episodes were found")
        }
        readLine("Press <return> to continue...")
        cursorUp(sortedEpisodes.size + 1)
        clearScreen()
    }
}
