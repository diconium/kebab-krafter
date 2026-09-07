package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.acronym.services.postEdgeCaseAcronym
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.acronym.controllers.PostEdgeCaseAcronym
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.acronym.installCaseAcronymGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.acronym.models.EcAcronymResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.acronym.models.YmcaDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.acronym.models.EcAcronymResponse as EcAcronymResponseClient
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.acronym.models.YmcaDetail as YmcaDetailClient

class CaseAcronymTest {

    @Test
    fun testCase() {
        // given
        val counter = mutableListOf<EcAcronymResponse>()
        val ctrl = object : PostEdgeCaseAcronym {
            override suspend fun CallScope.execute(body: EcAcronymResponse): EcAcronymResponse {
                counter += body
                return FakeServer.body
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.postEdgeCaseAcronym(FakeClient.request)

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf(FakeServer.response), counter)
            assertEquals(FakeServer.body.toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer {
        val details = YmcaDetail("y", "m", "c", "a")
        val body = EcAcronymResponse(details)
        val response = EcAcronymResponse(details)
    }

    private object FakeClient {
        val details = YmcaDetailClient("y", "m", "c", "a")
        val request = EcAcronymResponseClient(details)
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseAcronymGeneratedRoutes(it) },
        block = block,
    )
}
