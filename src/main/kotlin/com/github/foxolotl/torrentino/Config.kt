package com.github.foxolotl.torrentino

import cc.ekblad.toml.TomlValue
import cc.ekblad.toml.serialization.from
import cc.ekblad.toml.transcoding.TomlTranscoder
import cc.ekblad.toml.transcoding.decode
import java.net.URLEncoder
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.Paths

data class Config(
    val globalSettings: GlobalSettings,
    val sources: List<Source>,
) {
    companion object {
        fun load(configFile: Path = Paths.get("config.toml")): Config =
            TomlValue.from(configFile).decode(configTranscoder.decoder)
    }

    data class GlobalSettings(
        /**
         * Download torrent files to this directory.
         */
        val torrentDirectory: String,

        /**
         * Save seen episodes to this file.
         */
        val seenFile: String?,

        /**
         * Save watched series to this file.
         */
        val watchFile: String?,

        /**
         * Refresh time of the source, in minutes.
         */
        val refresh: Long?,
    )

    data class Source(
        /**
         * Human-readable name of the source.
         */
        val name: String,

        /**
         * URL template of the source. The following variables are substituted:
         *
         * - `%S`: URL-encoded search term
         */
        val urlTemplate: String
    ) {
        fun url(searchTerm: String = ""): String =
            urlTemplate.replace("%S", URLEncoder.encode(searchTerm, CHARSET))
    }
}

val configTranscoder = TomlTranscoder.default
    .withMapping<Config>("source" to "sources", "settings" to "globalSettings")
    .withMapping<Config.Source>("url" to "urlTemplate")

/**
 * Charset to use throughout the application.
 */
val Config.GlobalSettings.charset: Charset
    get() = CHARSET

private val CHARSET: Charset = Charset.forName("UTF-8")