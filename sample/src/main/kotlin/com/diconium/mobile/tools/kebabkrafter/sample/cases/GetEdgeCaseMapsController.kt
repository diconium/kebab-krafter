package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.controllers.GetEdgeCaseMaps
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.installCaseMapsGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.models.MapResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.models.MapResponse.TypeOfMap
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.models.color.Color
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.maps.models.color.ColorType
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class GetEdgeCaseMapsController : GetEdgeCaseMaps {
    override suspend fun CallScope.execute(): MapResponse {
        return MapResponse(
            mapOfStrings = mapOf("one" to "two"),
            mapOfDef = mapOf("one" to TypeOfMap(.1f, 2)),
            mapOfRef = mapOf("red" to Color(0xFFFF0000, Color.Name.GREY_00)),
            mapWithEnumKeys = mapOf(MapResponse.CountryType.TYPE_0 to MapResponse.Tree("", "")),
            mapWithEnumKeysReferenced = mapOf(ColorType.Bright to MapResponse.Tree("", "")),
        )
    }
}

fun Route.installGetEdgeCaseMaps() {
    installCaseMapsGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T {
                return GetEdgeCaseMapsController() as T
            }
        },
    )
}
