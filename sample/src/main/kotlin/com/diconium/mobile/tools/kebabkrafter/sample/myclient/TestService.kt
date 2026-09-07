package com.diconium.mobile.tools.kebabkrafter.sample.myclient

import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.models.v1.PostPetRequest
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.services.v1.DeletePetId
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.services.v1.deletePetId
import com.diconium.mobile.tools.kebabkrafter.sample.gen.client.petstore.services.v1.postPet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun testClient(client: HttpClient) {
    // option - 1
    val useCase: DeletePetId = client.deletePetId
    val deletePet1: Result<Unit> = useCase("123")

    // option - 2
    val deletePet2: Result<Unit> = client.deletePetId("234")

    client.postPet(PostPetRequest(name = "Fluffy", petType = PostPetRequest.PetType.CAT))
    val getFooBarUseCase: GetFooBarUseCaseV1 = client.getFooBarUseCaseV1()

    // retrofit style
    val service = TestService(client)
    service.api.v2.getFooBar
    val getFooBar: GetFooBarUseCaseV1 = service.api.v1.name.getFooBar
    val result: Result<GetFooBarUseCaseV1.SomethingElse> = getFooBar("")
    val getFooBar2 = service.api.v2.getFooBar
}

class TestService(client: HttpClient) {
    val api = object : Api {
        override val v1 = object : Api.V1 {
            override val name = object : Api.V1.Name {
                override val getFooBar = client.getFooBarUseCaseV1()
            }
        }
        override val v2: Api.V2 = object : Api.V2 {
            override val getFooBar: GetFooBarUseCaseV2 = client.GetFooBarUseCaseV2()
        }
    }
}

interface Api {

    val v1: V1
    val v2: V2

    interface V1 {
        val name: Name

        interface Name {
            val getFooBar: GetFooBarUseCaseV1
        }
    }

    interface V2 {
        val getFooBar: GetFooBarUseCaseV2
    }
}

fun interface GetFooBarUseCaseV1 {
    suspend operator fun invoke(id: String): Result<SomethingElse>

    data class SomethingElse(val foo: String, val bar: Int)
}

internal fun HttpClient.getFooBarUseCaseV1() = GetFooBarUseCaseV1 { id: String ->
    runCatching {
        val response = get {
            header("a", "b")
            url {
                appendPathSegments("foo")
                appendEncodedPathSegments(id)
            }
            parameter("", "")
            setBody("")
        }
        response.headers.contains("foo")
        response.headers.get("foo")
        response.body()
    }
}

fun interface GetFooBarUseCaseV2 {
    suspend operator fun invoke(): Result<List<Int>>
}

fun HttpClient.GetFooBarUseCaseV2() = GetFooBarUseCaseV2 {
    runCatching {
        this.get("v1/foo/bar") {
            header("a", "b")
        }.body()
    }
}

val HttpClient.getFooBarUseCaseV2: GetFooBarUseCaseV2
    get() = GetFooBarUseCaseV2 {
        runCatching {
            this.get("v1/foo/bar") {
                header("a", "b")
            }.body()
        }
    }
