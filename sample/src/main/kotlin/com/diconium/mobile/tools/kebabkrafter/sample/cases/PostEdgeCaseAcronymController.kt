package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym.controllers.PostEdgeCaseAcronym
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym.installCaseAcronymGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym.models.EcAcronymResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.acronym.models.YmcaDetail
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class PostEdgeCaseAcronymController : PostEdgeCaseAcronym {
    override suspend fun CallScope.execute(body: EcAcronymResponse): EcAcronymResponse =
        EcAcronymResponse(YmcaDetail("a", "c", "m", "y"))
}

fun Route.installPostEdgeCaseAcronym() {
    installCaseAcronymGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = PostEdgeCaseAcronymController() as T
        },
    )
}
