package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.deprecated.services.v1.some.getDeprecatedPath
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.deprecated.controllers.v1.some.GetDeprecatedPath
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.deprecated.installCaseDeprecatedGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.deprecated.models.NotImportant
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.deprecated.models.NotImportant as NotImportantClient
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.deprecated.services.v1.some.GetDeprecatedPath as GetDeprecatedPathClient

class CaseDeprecatedTest {

    @Test
    fun testCase() {
        // given
        var counter = 0
        val ctrl = object : GetDeprecatedPath {
            @Deprecated("This endpoint is deprecated")
            override suspend fun CallScope.execute(): NotImportant {
                counter++
                return NotImportant("one")
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getDeprecatedPath()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(NotImportantClient("one").toString(), result.getOrThrow().toString())
        }
    }

    @Test
    fun testDeprecatedAnnotation() {
        // server
        assertTrue(GetDeprecatedPath::class.hasAnnotation<Deprecated>())
        assertTrue(GetDeprecatedPath::class.functions.first().hasAnnotation<Deprecated>())
        assertTrue(NotImportant::type.hasAnnotation<Deprecated>())

        // client
        assertTrue(GetDeprecatedPathClient::class.hasAnnotation<Deprecated>())
        assertTrue(GetDeprecatedPathClient::class.functions.first().hasAnnotation<Deprecated>())
        assertTrue(NotImportantClient::type.hasAnnotation<Deprecated>())
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseDeprecatedGeneratedRoutes(it) },
        block = block,
    )
}
