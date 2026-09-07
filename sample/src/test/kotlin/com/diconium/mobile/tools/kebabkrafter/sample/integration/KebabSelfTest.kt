package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.budius.kebabkrafter.ServiceLocator
import io.ktor.client.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.ktor.util.reflect.*
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

/**
 * This function is the basis for the `integration` package,
 * it setups a fake KtorServer and matching KtorClient for the test to use.
 *
 * On those tests we use the generated client to call the generated server
 * and validate routing, parameters and results are all happening as expected.
 *
 * This validates that:
 * - interfaces, data models and implementations were all generated as expected
 * - the `installRoutes` function correctly set up the routes in the server
 * - the client path and headers are built correctly
 */
internal fun kebabSelfTest(
    vararg controllers: Any,
    install: Route.(ServiceLocator) -> Unit,
    setup: ApplicationTestBuilder.() -> Unit = {},
    block: suspend KebabSelfTest.() -> Unit,
) = testApplication {
    application {
        install(ContentNegotiation) {
            json(testJson)
        }
    }
    setup()
    routing {
        install(SimpleServiceLocator(controllers))
    }
    val jsonClient = createClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(testJson)
        }
    }
    block.invoke(KebabSelfTest(jsonClient))
}

private class SimpleServiceLocator(private val controllers: Array<out Any>) : ServiceLocator {
    override fun <T : Any> RoutingContext.getService(type: KClass<T>): T =
        (controllers.find { it.instanceOf(type) } ?: throw IllegalArgumentException("Unsupported type: $type")) as T
}

class KebabSelfTest(val client: HttpClient)

private val testJson = Json {
    prettyPrint = false
    isLenient = false
    ignoreUnknownKeys = true
    coerceInputValues = true
}
