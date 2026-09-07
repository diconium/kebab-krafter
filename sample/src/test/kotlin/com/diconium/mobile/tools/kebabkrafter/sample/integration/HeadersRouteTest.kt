package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.headersRoute.services.v1.some.postPath
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.headersRoute.controllers.v1.some.PostPath
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.headersRoute.installHeadersRouteGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.headersRoute.models.NotImportant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.headersRoute.models.NotImportant as NotImportantClient

class HeadersRouteTest {

    @Test
    fun postPath() {
        // given
        val fakeResponse = NotImportant("world")
        val counter = mutableListOf<NotImportant>()
        val ctrl = object : PostPath {
            override suspend fun CallScope.execute(body: NotImportant): NotImportant {
                counter += body
                return fakeResponse
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.postPath(NotImportantClient("hello"))

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf(NotImportant("hello")), counter)
            assertEquals(fakeResponse.toString(), result.getOrThrow().toString())
        }
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installHeadersRouteGeneratedRoutes(it) },
        block = block,
    )
}
