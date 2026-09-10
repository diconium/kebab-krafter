package io.github.budius.kebabkrafterandroid

import io.github.budius.kebabkrafterandroid.gen.android.petstore.services.v1.getPetId
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.services.base.v1.deletePetId
import io.ktor.client.HttpClient

suspend fun checkGenerated(client: HttpClient) {
	// generated with the app
	client.getPetId("123")

	// generated in the library
	val deletePet = client.deletePetId
	deletePet("123")
}
