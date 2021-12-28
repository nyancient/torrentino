package com.github.foxolotl.torrentino

import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.net.URL
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class Source(private val httpClient: HttpClient, private val config: Config.Source) {
    fun search(term: String): List<Pair<Episode, URL>> {
        val logger = LoggerFactory.getLogger(javaClass)
        logger.info("Searching source {} for term '{}'", config.name, term)
        return try {
            runBlocking {
                val response = httpClient.get<HttpResponse>(config.url(term))
                val feed = response.content.toInputStream().use {
                    SyndFeedInput().build(XmlReader(it))
                }
                feed.entries.map { Episode.parse(it.title) to URL(it.link) }
            }
        } catch (e: Exception) {
            logger.warn("Search failed", e)
            emptyList()
        }
    }
}
