package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.descriptions.services.postEdgeCaseDescription
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.descriptions.controllers.PostEdgeCaseDescription
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.descriptions.installCaseDescriptionsGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.descriptions.models.DescriptionResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.descriptions.models.DescriptionResponse as DescriptionResponseClient

class CaseDescriptionsTest {

    @Test
    fun testCase() {
        // given
        val counter = mutableListOf<String>()
        val ctrl = object : PostEdgeCaseDescription {
            override suspend fun CallScope.execute(body: DescriptionResponse): DescriptionResponse {
                counter += body.toString()
                return body.copy(city = "Neverland")
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.postEdgeCaseDescription(FakeClient.data)

            // then
            assertTrue(result.isSuccess)
            assertEquals(FakeClient.data.copy(city = "Neverland"), result.getOrThrow())
            assertEquals(listOf(FakeClient.data.toString()), counter)
        }
    }

    private object FakeClient {
        val data = DescriptionResponseClient(
            "first",
            "second",
            DescriptionResponseClient.Country("XX", "uuid", "uuid", "yolo"),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseDescriptionsGeneratedRoutes(it) },
        block = block,
    )
}
