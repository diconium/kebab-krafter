package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.enumarray.services.getEdgeCaseEnumArray
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.controllers.GetEdgeCaseEnumArray
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.installCaseEnumArrayGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.models.EnumArrayResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.models.EnumArrayResponse.OptionalSizesItems
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.models.EnumArrayResponse.SizesByGroupItems
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.models.EnumArrayResponse.SizesItems
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * https://github.com/diconium/kebab-krafter/issues/8
 * Arrays (and map values) whose items are enums declared in place must
 * generate the enum as a nested type of the containing data class.
 */
class CaseEnumArrayTest {

    @Test
    fun testCase() {
        var counter = 0
        val ctrl = object : GetEdgeCaseEnumArray {
            override suspend fun CallScope.execute(): EnumArrayResponse {
                counter++
                return FakeServer.response
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.getEdgeCaseEnumArray()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(FakeServer.response.toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer {
        val response = EnumArrayResponse(
            sizes = listOf(SizesItems.SMALL, SizesItems.MEDIUM, SizesItems.LARGE),
            sizesByGroup = mapOf(
                "tops" to listOf(SizesByGroupItems.S, SizesByGroupItems.M),
                "bottoms" to listOf(SizesByGroupItems.L),
            ),
            optionalSizes = listOf(OptionalSizesItems.XS, OptionalSizesItems.XL),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseEnumArrayGeneratedRoutes(it) },
        block = block,
    )
}
