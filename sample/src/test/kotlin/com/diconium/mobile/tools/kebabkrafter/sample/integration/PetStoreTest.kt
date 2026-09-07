package com.diconium.mobile.tools.kebabkrafter.sample.integration

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.services.v1.*
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.services.v2.getPets
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.DeletePetId
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPetId
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPetIdPdf
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPetIdPdfResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPetIdPhoto
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.GetPetIdResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v1.PostPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.controllers.v2.GetPets
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.installPetStoreGeneratedRoutes
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.models.v1.Pet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.models.v1.PetsResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.models.v1.PostPetRequest
import com.diconium.mobile.tools.kebabkrafter.sample.gen.server.petstore.models.v1.common.Color
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.models.v1.PostPetRequest as PostClientPetRequest

class PetStoreTest {
    @Test
    fun deletePetId() {
        // given
        val counter = mutableListOf<String>()
        val ctrl = object : DeletePetId {
            override suspend fun CallScope.execute(id: String) {
                counter += id
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.deletePetId("abc")

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf("abc"), counter)
        }
    }

    @Test
    fun getPet() {
        // given
        val counter = mutableListOf<Pair<String?, Int?>>()
        val ctrl = object : GetPet {
            override suspend fun CallScope.execute(type: String?, page: Int?): PetsResponse {
                counter += type to page
                return FakeServer.petsResponse
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPet("abc", 2)

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf<Pair<String?, Int?>>("abc" to 2), counter)
            assertEquals(FakeServer.petsResponse.toString(), result.getOrThrow().toString())
        }
    }

    @Test
    fun getPetId() {
        // given
        val counter = mutableListOf<String>()
        val ctrl = object : GetPetId {
            override suspend fun CallScope.execute(id: String): GetPetIdResponse {
                counter += id
                return FakeServer.getPetIdResponse
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPetId("qwe")

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf("qwe"), counter)
            assertEquals(FakeServer.getPetIdResponse.toString(), result.getOrThrow().toString())
        }
    }

    @Test
    fun getPetIdPdf() {
        // given
        val bytes = "hello-world".toByteArray(Charsets.UTF_8)
        val counter = mutableListOf<String>()
        val ctrl = object : GetPetIdPdf {
            override suspend fun CallScope.execute(id: String): GetPetIdPdfResponse {
                counter += id
                val body = ByteArrayInputStream(bytes)
                return GetPetIdPdfResponse("header.pdf", body)
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPetIdPdf("mwi")

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf("mwi"), counter)
            val header = result.getOrThrow().headerContentDisposition
            val body = result.getOrThrow().body.readAllBytes()
            assertEquals("header.pdf", header)
            assertContentEquals(bytes, body)
        }
    }

    @Test
    fun getPetIdPhoto() {
        // given
        val bytes = "this is a photo of a cat".toByteArray(Charsets.UTF_8)
        val counter = mutableListOf<String>()
        val ctrl = object : GetPetIdPhoto {
            override suspend fun CallScope.execute(id: String): InputStream {
                counter += id
                return ByteArrayInputStream(bytes)
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.getPetIdPhoto("mqo")

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf("mqo"), counter)
            assertContentEquals(bytes, result.getOrThrow().readAllBytes())
        }
    }

    @Test
    fun postPet() {
        // given
        val counter = mutableListOf<PostPetRequest>()
        val ctrl = object : PostPet {
            override suspend fun CallScope.execute(body: PostPetRequest): Pet {
                counter += body
                return FakeServer.dog
            }
        }

        kebabSelfTest(ctrl) {
            // when
            val result = client.postPet(FakeClient.postPetRequest)

            // then
            assertTrue(result.isSuccess)
            assertEquals(listOf(FakeServer.postPetRequest), counter)
            assertEquals(FakeServer.dog.toString(), result.getOrThrow().toString())
        }
    }

    @Test
    fun getPetsV2() {
        // given
        var counter = 0
        val ctrl = object : GetPets {
            override suspend fun CallScope.execute(): PetsResponse {
                counter++
                return FakeServer.petsResponse
            }
        }
        kebabSelfTest(ctrl) {
            // when
            val result = client.getPets()

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, counter)
            assertEquals(FakeServer.petsResponse.toString(), result.getOrThrow().toString())
        }
    }

    private object FakeServer {
        val dog = Pet.Dog("123", "Toto", Color.GREY_5F, 3.7f, 4, true, "Berger Picard")
        val petsResponse = PetsResponse(pets = listOf(dog), count = 1, page = 2)
        val getPetIdResponse = GetPetIdResponse("this is a header", dog)
        val postPetRequest = PostPetRequest(
            "foo",
            PostPetRequest.PetType.DOG,
            Instant.fromEpochSeconds(47368982),
        )
    }

    private object FakeClient {
        val postPetRequest = PostClientPetRequest(
            "foo",
            PostClientPetRequest.PetType.DOG,
            Instant.fromEpochSeconds(47368982),
        )
    }
}

private fun kebabSelfTest(controller: Any, block: suspend KebabSelfTest.() -> Unit) {
    kebabSelfTest(
        controller,
        install = { installPetStoreGeneratedRoutes(it) },
        block = block,
    )
}
