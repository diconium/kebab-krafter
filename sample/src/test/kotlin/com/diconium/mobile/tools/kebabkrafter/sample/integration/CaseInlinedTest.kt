package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.inlined.services.postEdgeCaseInlined
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlined.controllers.PostEdgeCaseInlined
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlined.installCaseInlinedGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlined.models.InlinedResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.inlined.models.InlinedResponse as InlinedResponseClient

class CaseInlinedTest {

    @Test
    fun testCase() {
        // given
        val counter = mutableListOf<String>()
        val ctrl = object : PostEdgeCaseInlined {
            override suspend fun CallScope.execute(body: InlinedResponse): InlinedResponse {
                counter += body.toString()
                return body.copy(ecValue = "serverEcValue")
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.postEdgeCaseInlined(FakeClient.data)

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf(FakeClient.data.toString()), counter)
            assertEquals(FakeClient.data.copy(ecValue = "serverEcValue").toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer
    private object FakeClient {
        val data = InlinedResponseClient(
            ecValue = "ecValue",
            extra = InlinedResponseClient.Extra(
                foo = "foo",
                bar = InlinedResponseClient.DefExtra(
                    recursive = InlinedResponseClient.DefExtra.Recursive(
                        value1 = "value1",
                        reRecursive = InlinedResponseClient.DefExtra.Recursive.ReRecursive(
                            value2 = "value2",
                            variation = InlinedResponseClient.DefExtra.Recursive.ReRecursive.Variation.Value3,
                        ),
                    ),
                    defArrayInDef = listOf(InlinedResponseClient.DefInDef("value3")),
                    defInDef = InlinedResponseClient.DefInDef("value1"),
                ),
            ),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseInlinedGeneratedRoutes(it) },
        block = block,
    )
}
