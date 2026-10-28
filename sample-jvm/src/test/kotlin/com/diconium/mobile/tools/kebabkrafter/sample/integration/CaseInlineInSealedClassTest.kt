package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.inlinesealedclass.services.enuminside.getSealedClass
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.controllers.enuminside.GetSealedClass
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.installCaseInlineInSealedClassGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse.Option1.Type1
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse.Option2.Extra
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CaseInlineInSealedClassTest {

    @Test
    fun testCase1() {
        // given
        var counter = 0
        val ctrl = object : GetSealedClass {
            override suspend fun CallScope.execute(): InlineInSealedClassResponse {
                counter++
                return FakeServer.response1
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getSealedClass()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(FakeServer.response1.toString(), result.getOrThrow().toString())
        }
    }

    @Test
    fun testCase2() {
        // given
        var counter = 0
        val ctrl = object : GetSealedClass {
            override suspend fun CallScope.execute(): InlineInSealedClassResponse {
                counter++
                return FakeServer.response2
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getSealedClass()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(FakeServer.response2.toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer {
        val response1 = InlineInSealedClassResponse.Option1(
            Type1.VALUE_A,
            InlineInSealedClassResponse.Type2.VALUE_X,
        )
        val response2 = InlineInSealedClassResponse.Option2(
            id = "id",
            extra = Extra(
                foo = "foo",
                bar = InlineInSealedClassResponse.DefExtra(
                    recursive = InlineInSealedClassResponse.DefExtra.Recursive(
                        value1 = "value11",
                        reRecursive = InlineInSealedClassResponse.DefExtra.Recursive.ReRecursive(
                            value2 = "value20",
                            variation = InlineInSealedClassResponse.DefExtra.Recursive.ReRecursive.Variation.Value2,
                        ),
                    ),
                    defArrayInDef = listOf(InlineInSealedClassResponse.DefInDef("value33")),
                    defInDef = InlineInSealedClassResponse.DefInDef("value111"),
                ),
            ),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseInlineInSealedClassGeneratedRoutes(it) },
        block = block,
    )
}
