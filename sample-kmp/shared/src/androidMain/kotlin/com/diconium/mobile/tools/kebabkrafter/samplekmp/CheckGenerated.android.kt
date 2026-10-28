package com.diconium.mobile.tools.kebabkrafter.samplekmp

import com.diconium.mobile.tools.kebabkrafter.gen.shared.petstore.services.v1.deletePetId
import com.diconium.mobile.tools.kebabkrafter.gen.shared.petstore.services.v1.getPetId
import io.ktor.client.HttpClient

// the Parcelable check lives in :androidApp (see checkGeneratedParcelable)
actual suspend fun checkGenerated() {
	val client = HttpClient()

	val pet = client.getPetId("123")

	// generated for all platforms
	val deletePet = client.deletePetId
	deletePet("123")
}
