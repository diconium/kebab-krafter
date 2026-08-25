package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.controllers.PostEdgeCaseInlined
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.installCaseInlinedGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.models.InlinedResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.models.InlinedResponse.DefExtra.Recursive.ReRecursive
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.inlined.models.InlinedResponse.DefInDef
import io.ktor.server.routing.*
import kotlin.reflect.KClass

class PostEdgeCaseInlinedController : PostEdgeCaseInlined {
    override suspend fun CallScope.execute(body: InlinedResponse): InlinedResponse = InlinedResponse(
        ecValue = "ecValue",
        extra = InlinedResponse.Extra(
            foo = "foo",
            bar = InlinedResponse.DefExtra(
                recursive = InlinedResponse.DefExtra.Recursive(
                    reRecursive = ReRecursive(variation = ReRecursive.Variation.Value2),
                    value1 = "value1",
                ),
                defInDef = DefInDef("value3"),
                defArrayInDef = listOf(DefInDef(value3 = "list of value3")),
            ),
        ),
    )
}

fun Route.installPostEdgeCaseInlined() {
    installCaseInlinedGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = PostEdgeCaseInlinedController() as T
        },
    )
}
