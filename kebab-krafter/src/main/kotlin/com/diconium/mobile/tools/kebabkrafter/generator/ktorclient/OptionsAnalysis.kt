package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import java.io.File


// GET /data/a
data class A(val id: String)

// GET /data/b
data class B(val id: String)


// option.1
interface ApiData {

	suspend fun getDataA(): A
	suspend fun getDataB(): B

	companion object Factory {

		fun build(client: HttpClient): ApiData {
			return ApiDataImpl(client)
		}
	}

	private class ApiDataImpl(private val client: HttpClient) : ApiData {
		override suspend fun getDataA(): A {
			return client.get("data/a").body()
		}

		override suspend fun getDataB(): B {
			return client.get("data/b").body()
		}
	}
}


// option.2
fun interface GetDataA {

	suspend fun getDataA(): A

	companion object Factory {
		fun build(client: HttpClient): GetDataA {
			return GetDataA {
				client.get("data/a").body()
			}
		}
	}
}


// option.2 (b)
fun interface GetDataB {

	suspend operator fun invoke(input: String, other: Int?): B

	companion object Factory {
		fun build(client: HttpClient): GetDataB {
			return GetDataB { input, other ->
				val request = client.get {
					url {
						appendPathSegments("v1")
						appendPathSegments("pet")
						parameters.append("foo", "bar")

					}

				}
				request.body()
			}
		}
	}
}


fun interface GetPdfFile {

	suspend operator fun invoke(file: File, id: String): B

	companion object Factory {
		fun build(client: HttpClient): GetPdfFile {
			return GetPdfFile { file, id ->
				val response: HttpResponse = client.get("data/b?id=${id}")
				val channel: ByteReadChannel = response.body()



				TODO()
			}
		}
	}
}


// option.3
suspend fun HttpClient.getDataB(): B {
	return get("data/b").body()
}


// usage
suspend fun sample(client: HttpClient) {

	// option.1 - interface with methods and factory
	val api: ApiData = ApiData.build(client)
	val aOption1: A = api.getDataA()
	val bOption1: B = api.getDataB()

	// option.2 - functional interface with factory
	val getA: GetDataA = GetDataA.build(client)
	val bOption2: A = getA.getDataA()

	// option.2 (b) - functional interface with factory using an operator
	val getB: GetDataB = GetDataB.build(client)
	val bOption2B: B = getB(input = "", other = 2)

	// option.3 - extension method
	val bOption3: B = client.getDataB()

}


class MyRepoOrViewModel(
	private val api: ApiData,
	private val getA: GetDataA,
	private val getB: GetDataB,
	private val getB3: (suspend () -> B),
) {

	suspend fun execute() {
		// option.1 - interface with methods and factory
		val aOption1: A = api.getDataA()
		val bOption1: B = api.getDataB()

		// option.2 - functional interface with factory
		val bOption2: A = getA.getDataA()

		// option.2 (b) - functional interface with factory using an operator
		val bOption2B: B = getB("", 2)

		// option.3 - extension method
		val bOption3: B = getB3()

	}

}

private fun dependencyInjectionRepoOrViewModel(
	client: HttpClient
): MyRepoOrViewModel = MyRepoOrViewModel(
	api = ApiData.build(client),
	getA = GetDataA.build(client),
	getB = GetDataB.build(client),
	getB3 = { client.getDataB() },
)


// testing
fun testingWithFakes() {
	// option.1
	val fakeApi: ApiData = FakeApiData(Result.success(A("id")), Result.success(B("id")))

	// option.2
	val fake2GetA = GetDataA { A("id") }

	// option.2 (b)
	val fake2GetB = GetDataB { input, other -> B("$input - $other") }

	// option.3
	val fake3GetB: (suspend () -> B) = { B("id-b") }

}

class FakeApiData(
	var a: Result<A>,
	var b: Result<B>
) : ApiData {
	override suspend fun getDataA(): A = a.getOrThrow()
	override suspend fun getDataB(): B = b.getOrThrow()
}
