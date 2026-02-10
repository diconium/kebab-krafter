package com.diconium.mobile.tools.kebabkrafter.parser.json

import java.io.File
import kotlin.io.path.Path

class FileHandler(private val root: File, private val file: File) {

    init {
        assert(file.exists()) { "'file' must exist" }
        assert(file.isFile) { "'file' must not be a directory" }
    }

    /** utf-8 contents of the file */
    val contents: String by lazy { file.readText() }

    /** name of the file (without extensions), used for class name */
    val name: String by lazy { file.nameWithoutExtension }

    /**
     * If root is `/root`
     * and the file is `/root/child/data.json`
     * then the relative file is `child/data.json`
     */
    private val findRelativeFile: File by lazy {
        val relativeFile = file.relativeToOrNull(root)
        requireNotNull(relativeFile) { "${file.absolutePath} is not in ${root.absolutePath}" }
    }

    /**
     * Relative parent path (always with dot/slash)
     *
     * if child is in the same path as the root, it will be `./`
     * if root is `/root`
     * and the file is `/root/child/data.json`
     * then the relative file is `child/`
     */
    val relativeParent: String by lazy {
        val parent = findRelativeFile.parent
        if (parent == null) {
            dotSlash
        } else {
            dotSlash + parent + File.separator
        }
    }

    /** Package name to use for this file */
    val relativePackageName: String by lazy {
        relativeParent.replace("/", ".").replace("..", ".").trim('.')
    }

    /**
     * Relative path for the file (always with dot/slash)
     *
     * if root is `/root`
     * and the file is `/root/child/data.json`
     * then the relative file is `./child/data.json`
     */
    val relativeFile: String by lazy {
        relativeParent + file.name
    }

    /**
     * Creates a new [FileHandler] for a file based on the relative path to this handlers file
     */
    fun handlerOf(path: String): FileHandler {
        val otherPath = if (path.startsWith(".")) {
            path
        } else {
            dotSlash + path
        }

        val other = Path(root.path + File.separator + relativeParent + otherPath).normalize()
        val otherFile = other.toFile()
        return FileHandler(root, otherFile)
    }

    fun handlerFromRoot(path: String): FileHandler {
        val otherPath = if (path.startsWith(".")) {
            path
        } else {
            dotSlash + path
        }
        val other = Path(root.path + File.separator + otherPath).normalize()
        val otherFile = other.toFile()
        return FileHandler(root, otherFile)
    }

    //region equals/hashCode
    override fun equals(other: Any?): Boolean = (other as? FileHandler)?.let {
        it.root.absolutePath == root.absolutePath &&
            it.file.absolutePath == file.absolutePath
    } ?: false

    override fun hashCode(): Int {
        var result = root.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + contents.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + findRelativeFile.hashCode()
        result = 31 * result + relativeParent.hashCode()
        result = 31 * result + relativePackageName.hashCode()
        result = 31 * result + relativeFile.hashCode()
        return result
    }

    override fun toString(): String = "FileHandler(root=$root, file=$file)"
    //endregion
}

private val dotSlash = "." + File.separator
