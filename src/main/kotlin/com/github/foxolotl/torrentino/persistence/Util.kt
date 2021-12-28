package com.github.foxolotl.torrentino.persistence

import cc.ekblad.toml.TomlValue
import cc.ekblad.toml.transcoding.TomlDecoder
import cc.ekblad.toml.transcoding.TomlEncoder
import cc.ekblad.toml.transcoding.TomlTranscoder
import com.github.foxolotl.torrentino.Resolution
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.moveTo
import kotlin.io.path.outputStream

fun safelyOverwrite(filePath: Path, write: (OutputStream) -> Unit) {
    require(!filePath.isDirectory()) {
        "path $filePath is a directory"
    }
    val tempFile = kotlin.io.path.createTempFile(filePath.parent)
    try {
        tempFile.outputStream().use(write)
        tempFile.moveTo(filePath, overwrite = true)
    } finally {
        tempFile.deleteIfExists()
    }
}

val tomlEncoder = TomlEncoder.default.with { it: Resolution -> TomlValue.String(it.name) }
val tomlDecoder = TomlDecoder.default.with { it: TomlValue.String -> Resolution.valueOf(it.value) }
val tomlTranscoder = TomlTranscoder(tomlEncoder, tomlDecoder)
