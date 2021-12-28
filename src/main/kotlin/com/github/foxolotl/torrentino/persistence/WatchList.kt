package com.github.foxolotl.torrentino.persistence

import com.github.foxolotl.torrentino.Series

interface WatchList {
    fun getWatched(): Set<Series>
    fun watch(items: Set<Series>)
    fun unwatch(items: Set<Series>)
}
