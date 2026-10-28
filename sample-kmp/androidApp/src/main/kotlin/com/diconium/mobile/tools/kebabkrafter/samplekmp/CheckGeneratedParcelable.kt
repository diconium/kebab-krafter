package com.diconium.mobile.tools.kebabkrafter.samplekmp

import android.os.Bundle
import com.diconium.mobile.tools.kebabkrafter.gen.android.petstore.services.v1.deletePetId
import com.diconium.mobile.tools.kebabkrafter.gen.android.petstore.services.v1.getPetId
import io.ktor.client.HttpClient

suspend fun checkGeneratedParcelable(client: HttpClient) {
	val bundle = Bundle()
	val pet = client.getPetId("123")
	bundle.putParcelable("pet", pet.getOrThrow().body)

	val deletePet = client.deletePetId
	deletePet("123")
}
