package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.descriptions.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.descriptions.controllers.PostEdgeCaseDescription
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.descriptions.installCaseDescriptionsGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.descriptions.models.DescriptionResponse
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class PostEdgeCaseDescriptionController : PostEdgeCaseDescription {
    override suspend fun CallScope.execute(body: DescriptionResponse): DescriptionResponse = DescriptionResponse()
}

fun Route.installPostEdgeCaseDescription() {
    installCaseDescriptionsGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T =
                PostEdgeCaseDescriptionController() as T
        },
    )
}
