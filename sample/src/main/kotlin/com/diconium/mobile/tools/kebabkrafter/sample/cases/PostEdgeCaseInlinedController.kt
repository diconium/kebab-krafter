package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.controllers.PostEdgeCaseInlined
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.installCaseInlinedGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.models.InlinedResponse
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class PostEdgeCaseInlinedController : PostEdgeCaseInlined {
    override suspend fun CallScope.execute(body: InlinedResponse): InlinedResponse {
        return body
    }
}

fun Route.installPostEdgeCaseInlined() {
    installCaseInlinedGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T {
                return PostEdgeCaseInlinedController() as T
            }
        },
    )
}

