package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.controllers.GetEdgeCaseEnumArray
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.installCaseEnumArrayGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.enumarray.models.EnumArrayResponse
import io.github.budius.kebabkrafter.ServiceLocator
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class GetEdgeCaseEnumArrayController : GetEdgeCaseEnumArray {
    override suspend fun CallScope.execute(): EnumArrayResponse = EnumArrayResponse(
        sizes = listOf(EnumArrayResponse.SizesItems.SMALL, EnumArrayResponse.SizesItems.LARGE),
        sizesByGroup = mapOf("tops" to listOf(EnumArrayResponse.SizesByGroupItems.M)),
    )
}

fun Route.installGetEdgeCaseEnumArray() {
    installCaseEnumArrayGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = GetEdgeCaseEnumArrayController() as T
        },
    )
}
