package com.diconium.mobile.tools.kebabkrafter.sample.example

import com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1.GetPetController
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.models.v1.PetsResponse
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * That is an example unit test for a controller generated with Kebab-Krafter
 */
class GetPetControllerTest {

    @Test
    fun `happy path`() = runBlocking {
        // given
        val getPetsFromDb: suspend () -> PetsResponse = { PetsResponse(emptyList(), 0, 0) }

        // when
        val scope = FakeCallScope()
        val sut = create(getPetsFromDb)
        val response = with(sut) { scope.execute(null, null) }

        // then
        assertEquals(PetsResponse(emptyList(), 0, 0), response)
    }

    @Test
    fun `failure case`() = runBlocking {
        // given
        val getPetsFromDb: suspend () -> PetsResponse = { throw IOException("no internet") }

        // when
        val scope = FakeCallScope()
        val sut = create(getPetsFromDb)
        val response = runCatching { with(sut) { scope.execute(null, null) } }

        // then
        assertTrue(response.isFailure)
    }

    private fun create(getPetsFromDb: suspend () -> PetsResponse) = GetPetController(
        getPetsFromDb = getPetsFromDb,
    )
}
