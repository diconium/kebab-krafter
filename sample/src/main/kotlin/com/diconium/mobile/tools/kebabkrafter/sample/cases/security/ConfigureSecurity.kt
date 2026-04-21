package com.diconium.mobile.tools.kebabkrafter.sample.cases.security

import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.controllers.GetData
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.controllers.PostLogin
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.installSecurityGeneratedRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import kotlin.io.encoding.Base64
import kotlin.reflect.KClass

fun Application.configureSecurity() {
    authentication {
        bearer("myLogin") {
            authenticate { bearer ->
                // IMPORTANT REMINDER:
                // This is in absolutely no way a valid implementation of any type of security.
                // That is just a simple example of an 'authenticate' block that returns something
                Base64.UrlSafe
                    .decode(bearer.token)
                    .toString(Charsets.UTF_8)
                    .split("-")
                    .takeIf { it.size == 2 }
                    ?.let { BearerPrincipal(it[0], it[1]) }
            }
        }
    }
}

data class BearerPrincipal(val u: String, val p: String)

interface SecureCallScope {

    val user: String

    companion object {
        fun from(call: ApplicationCall): SecureCallScope = Impl(call)
    }

    private class Impl(call: ApplicationCall) : SecureCallScope {
        override val user: String by lazy {
            requireNotNull(call.principal<BearerPrincipal>()) { "User not authorized" }.u
        }
    }
}

fun Route.installSecurityGeneratedRoutes() {
    installSecurityGeneratedRoutes(
        object : ServiceLocator {
            override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = when (type) {
                GetData::class -> GetDataController() as T
                PostLogin::class -> PostLoginController() as T
                else -> throw IllegalArgumentException("Unknown Controller")
            }
        },
    )
}
