package io.github.budius.kraftkebabandroidlibary


import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.services.base.v1.getPetId
import io.ktor.client.HttpClient

suspend fun checkGenerated(client: HttpClient) {
	client.getPetId("123")
}
