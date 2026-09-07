package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.maps.services.getEdgeCaseMaps
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.controllers.GetEdgeCaseMaps
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.installCaseMapsGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.models.MapResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.models.MapResponse.CountryType
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.models.color.Color
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.maps.models.color.ColorType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CaseMaps {

    @Test
    fun testCase() {
        var counter = 0
        val ctrl = object : GetEdgeCaseMaps {
            override suspend fun CallScope.execute(): MapResponse {
                counter++
                return FakeServer.response
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.getEdgeCaseMaps()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(FakeServer.response.toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer {
        val response = MapResponse(
            mapOfStrings = mapOf(
                "this" to "that",
                "foo" to "bar",
                "abc" to "xyz",
            ),
            mapOfDef = mapOf("def" to MapResponse.TypeOfMap(0.42f, 42)),
            mapOfRef = mapOf(
                "grey00" to Color(0xFFFFFF, Color.Name.GREY_00),
                "grey5f" to Color(0x5F5F5F, Color.Name.GREY_5F),
            ),
            mapWithEnumKeys = mapOf(
                CountryType.TYPE_0 to MapResponse.Tree("a", "b"),
                CountryType.TYPE_O_NEGATIVE to MapResponse.Tree("x", "y"),
            ),
            mapWithEnumKeysReferenced = mapOf(ColorType.Bright to MapResponse.Tree("0", "1")),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installCaseMapsGeneratedRoutes(it) },
        block = block,
    )
}
