package com.diconium.mobile.tools.kebabkrafter.sample.cases

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.controllers.enuminside.GetSealedClass
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.installCaseInlineInSealedClassGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse.DefExtra.Recursive
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.inlinesealedclass.models.InlineInSealedClassResponse.Option1.Type1
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import kotlin.random.Random
import kotlin.reflect.KClass

class GetInlineInSealedClassController : GetSealedClass {
    override suspend fun CallScope.execute(): InlineInSealedClassResponse = if (Random.nextBoolean()) {
        InlineInSealedClassResponse.Option1(
            type1 = Type1.VALUE_B,
            type2 = InlineInSealedClassResponse.Type2.VALUE_Y,
        )
    } else {
        InlineInSealedClassResponse.Option2(
            id = "15E1B39E-E1AC-4AD3-81D2-E610CA320E16",
            extra = InlineInSealedClassResponse.Option2.Extra(
                foo = "foo",
                bar = InlineInSealedClassResponse.DefExtra(
                    recursive = Recursive(
                        reRecursive = Recursive.ReRecursive(variation = Recursive.ReRecursive.Variation.Value2),
                        value1 = "value1",
                    ),
                    defInDef = InlineInSealedClassResponse.DefInDef("value3"),
                    defArrayInDef = listOf(InlineInSealedClassResponse.DefInDef(value3 = "list of value3")),
                ),
            ),
        )
    }
}

fun Route.installGetSealedClass() {
    installCaseInlineInSealedClassGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T =
                GetInlineInSealedClassController() as T
        },
    )
}
