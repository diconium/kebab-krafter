package com.diconium.mobile.tools.kebabkrafter.sample.cases.security

import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.controllers.PostLogin
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.models.LoginRequest
import com.diconium.mobile.tools.kebabkrafter.sample.gen.case.security.models.LoginResponse
import kotlinx.coroutines.delay
import kotlin.io.encoding.Base64
import kotlin.time.Duration.Companion.milliseconds

class PostLoginController : PostLogin {
    override suspend fun SecureCallScope.execute(body: LoginRequest): LoginResponse {
        delay(500.milliseconds)
        // IMPORTANT REMINDER:
        // This is in absolutely no way a valid implementation of any type of security.
        // That is just a simple example of an endpoint that returns "something"
        val token = Base64.UrlSafe.encode("${body.u}-${body.p}".toByteArray())
        return LoginResponse(token)
    }
}
