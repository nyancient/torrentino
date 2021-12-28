package com.github.foxolotl.torrentino.persistence

import com.github.foxolotl.torrentino.Episode

/**
 * Keeps track of which episodes have already been seen by the client.
 */
interface SeenList {
    fun <T> filterEpisodes(episodes: List<T>, selector: (T) -> Episode): Set<T>
    fun markAsSeen(episodes: Set<Episode>)
}
