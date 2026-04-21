package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.headersRoute.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.headersRoute.controllers.v1.some.PostPath
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.headersRoute.installHeadersRouteGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.headersRoute.models.NotImportant
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class PostPathController : PostPath {
    override suspend fun CallScope.execute(body: NotImportant): NotImportant = NotImportant("value")
}

fun Route.installHeadersRouteGeneratedRoutes() {
    installHeadersRouteGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = PostPathController() as T
        },
    )
}
