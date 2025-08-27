package com.diconium.mobile.tools.kebabkrafter.sample.controllers

import com.diconium.mobile.tools.kebabkrafter.sample.FakeCallScope
import com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1.GetPetController
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PetsResponse
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
