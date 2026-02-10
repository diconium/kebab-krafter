package com.diconium.mobile.tools.kebabkrafter.v2.parser.json

import com.diconium.mobile.tools.kebabkrafter.parser.json.FileHandler
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class FileHandlerTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var root: File
    private lateinit var file: File

    @BeforeEach
    fun setup() {
        folder.create()
        root = folder.newFolder("root")
        file = File(root, "someJson.json")
        file.writeText(SOME_JSON)
    }

    private fun touch(path: String): File = File(root, path).apply {
        parentFile.mkdirs()
        writeText("")
    }.normalize()

    @Test
    fun `should read the file text`() {
        val sut = FileHandler(root, file)
        assertEquals(SOME_JSON, sut.contents)
    }

    @Test
    fun `should find relative parent`() {
        assertEquals("./", FileHandler(root, file).relativeParent)

        val child1 = touch("child/someJson.json")
        assertEquals("./child/", FileHandler(root, child1).relativeParent)

        val child2 = touch("child0/child1/someJson.json")
        assertEquals("./child0/child1/", FileHandler(root, child2).relativeParent)
    }

    @Test
    fun `should find relative file`() {
        assertEquals("./someJson.json", FileHandler(root, file).relativeFile)

        val child1 = touch("child/someJson.json")
        assertEquals("./child/someJson.json", FileHandler(root, child1).relativeFile)

        val child2 = touch("child0/child1/someJson.json")
        assertEquals("./child0/child1/someJson.json", FileHandler(root, child2).relativeFile)
    }

    @Test
    fun `should find file name`() {
        assertEquals("someJson", FileHandler(root, file).name)

        val child1 = touch("child/someJson.json")
        assertEquals("someJson", FileHandler(root, child1).name)

        val child2 = touch("child0/child1/someJson.json")
        assertEquals("someJson", FileHandler(root, child2).name)
    }

    @Test
    fun `should compare based on file paths`() {
        val f1 = touch("child/someJson.json")
        val h1 = FileHandler(root, f1)

        val f2 = touch("child/someJson.json")
        val h2 = FileHandler(root, f2)

        assertEquals(h1, h2)
    }

    @Test
    fun `should return file handler for different path`() {
        fun case(base: String, relative: String, expected: String) {
            val root = folder.newFolder("root-${UUID.randomUUID()}")
            val expected = FileHandler(root, touch(expected))
            val relative = FileHandler(root, touch(base)).handlerOf(relative)
            assertEquals(expected, relative)
        }

        // both on root
        case("file1.json", "file2.json", "file2.json")
        case("file1.json", "./file2.json", "file2.json")

        // both on child
        case("./child/file1.json", "file2.json", "./child/file2.json")
        case("./child/file1.json", "./file2.json", "./child/file2.json")

        // source on root, other on child
        case("file1.json", "child/file2.json", "./child/file2.json")
        case("file1.json", "./child/file2.json", "./child/file2.json")

        // other on a sibling
        case("./child1/file1.json", "../child2/file2.json", "./child2/file2.json")

        // source on child, other on sub-child
        case("./child/file1.json", "sub-child/file2.json", "./child/sub-child/file2.json")
        case("./child/file1.json", "./sub-child/file2.json", "./child/sub-child/file2.json")
    }

    @Test
    fun `should return file handler for same root`() {
        val handler = FileHandler(root, touch("child1/sub/file.json"))
        val file2 = touch("./child2/file.json")
        val expected = FileHandler(root, file2)
        val output = handler.handlerFromRoot("child2/file.json")
        assertEquals(expected, output)
    }
}

private const val SOME_JSON = """{ "helo":"world" }"""
