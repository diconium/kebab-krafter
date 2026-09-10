package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.getPathWithBytes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.getPathWithSupportClassHeader
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.getPathWithSupportClassHeaderBody
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.getPathWithSupportClassHeaderBytes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.supportclass.controllers.v1.some.*
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.supportclass.installCaseSupportClassGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.supportclass.models.NotImportant
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.models.NotImportant as NotImportantClient
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.GetPathWithSupportClassHeaderBodyResponse as GetPathWithSupportClassHeaderBodyResponseClient
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.supportclass.services.v1.some.GetPathWithSupportClassHeaderResponse as GetPathWithSupportClassHeaderResponseClient

class CaseSupportClassTest {

    @Test
    fun testClassHeader() {
        // given
        var counter = 0
        val ctrl = object : GetPathWithSupportClassHeader {
            override suspend fun CallScope.execute(): GetPathWithSupportClassHeaderResponse {
                counter++
                return GetPathWithSupportClassHeaderResponse("random-string")
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPathWithSupportClassHeader()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(
                GetPathWithSupportClassHeaderResponseClient("random-string").toString(),
                result.getOrThrow().toString(),
            )
        }
    }

    @Test
    fun testClassHeaderAndBody() {
        // given
        var counter = 0
        val ctrl = object : GetPathWithSupportClassHeaderBody {
            override suspend fun CallScope.execute(): GetPathWithSupportClassHeaderBodyResponse {
                counter++
                return GetPathWithSupportClassHeaderBodyResponse("random-string", NotImportant())
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPathWithSupportClassHeaderBody()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(
                GetPathWithSupportClassHeaderBodyResponseClient("random-string", NotImportantClient()).toString(),
                result.getOrThrow().toString(),
            )
        }
    }

    @Test
    fun testClassHeaderAndBytes() {
        // given
        var counter = 0
        val ctrl = object : GetPathWithSupportClassHeaderBytes {
            override suspend fun CallScope.execute(): GetPathWithSupportClassHeaderBytesResponse {
                counter++
                return GetPathWithSupportClassHeaderBytesResponse(
                    "random-string",
                    ByteArrayInputStream("here are some bytes".toByteArray()),
                )
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPathWithSupportClassHeaderBytes()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(
                "random-string",
                result.getOrThrow().headerContentDisposition,
            )
            assertContentEquals(
                "here are some bytes".toByteArray(),
                result.getOrThrow().body.readAllBytes(),
            )
        }
    }

    @Test
    fun testJustBytes() {
        // given
        var counter = 0
        val ctrl = object : GetPathWithBytes {
            override suspend fun CallScope.execute(): InputStream {
                counter++
                return ByteArrayInputStream("here are some bytes".toByteArray())
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPathWithBytes()

            // then
            assertTrue(result.isSuccess)
            assertContentEquals(
                "here are some bytes".toByteArray(),
                result.getOrThrow().readAllBytes(),
            )
        }
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseSupportClassGeneratedRoutes(it) },
        block = block,
    )
}
