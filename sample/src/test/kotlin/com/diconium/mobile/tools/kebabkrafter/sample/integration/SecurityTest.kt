package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.cases.security.SecureCallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.security.services.getData
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.security.services.postLogin
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.controllers.GetData
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.controllers.PostLogin
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.installSecurityGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.models.LoginRequest
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.models.LoginResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.case.security.models.UserData
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.server.auth.*
import io.ktor.server.auth.apikey.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.case.security.models.LoginRequest as LoginRequestClient

class SecurityTest {

    @Test
    fun testLogin() {
        // given
        val loginCounter = mutableListOf<LoginRequest>()
        val login = object : PostLogin {
            override suspend fun SecureCallScope.execute(body: LoginRequest): LoginResponse {
                loginCounter += body
                return LoginResponse(TOKEN)
            }
        }

        var dataCounter = 0
        val ctrl = object : GetData {
            override suspend fun SecureCallScope.execute(): UserData {
                dataCounter++
                return FakeServer.userData
            }
        }

        kebabSelfTest(login, ctrl) {
            // when - success login
            val loginResult = client.postLogin(LoginRequestClient("username", "password"))

            // then
            assertTrue(loginResult.isSuccess)
            assertEquals(listOf(LoginRequest("username", "password")), loginCounter)
            assertEquals(TOKEN, loginResult.getOrThrow().token)

            // when - success call with header
            val authClient = client.config { defaultRequest { header(HEADER, TOKEN) } }
            val result = authClient.getData()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, dataCounter)
            assertEquals(FakeServer.userData.toString(), result.getOrThrow().toString())

            // when - failed called without header
            val failResult = client.getData()
            assertTrue(failResult.isFailure)
            assertTrue(
                failResult.exceptionOrNull()!!.toString().contains("401 Unauthorized"),
                failResult.exceptionOrNull()!!.toString(),
            )
        }
    }

    private object FakeServer {
        val userData = UserData("hello world")
    }
}

private const val TOKEN = "gv85gtb2875tvh02834nrc891y4t92=="
private const val HEADER = "apiKey"

private fun kebabSelfTest(controller0: Any, controller1: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller0,
        controller1,
        setup = {
            application {
                authentication {
                    apiKey("myLogin") {
                        headerName = HEADER
                        validate { token ->
                            Unit.takeIf { token == TOKEN }
                        }
                    }
                }
            }
        },
        install = { installSecurityGeneratedRoutes(it) },
        block = block,
    )
}
