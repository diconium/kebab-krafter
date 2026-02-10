package com.diconium.mobile.tools.kebabkrafter.parser.json

import com.diconium.mobile.tools.kebabkrafter.models.JsonSpecFile
import java.io.File

class JsonParser(private val root: File) {

    init {
        assert(root.isDirectory) { "'rootFolder' must be a directory" }
    }

    fun parse(file: File): JsonSpecFile {
        val relativeFile = file.relativeToOrNull(root)
        requireNotNull(relativeFile) { "${file.absolutePath} is not in ${root.absolutePath}" }
        val fileHandler = FileHandler(root, file)
        return JsonFileParser(fileHandler).parse()
    }
}
