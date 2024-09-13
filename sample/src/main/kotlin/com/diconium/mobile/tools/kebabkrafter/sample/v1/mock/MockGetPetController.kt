package com.diconium.mobile.tools.kebabkrafter.sample.v1.mock

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.Pet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PetsResponse

class MockGetPetController : GetPet {
	override suspend fun CallScope.execute(type: String?, page: Int?): PetsResponse {
		// mock data
		return PetsResponse(
			count = 2,
			page = 0,
			pets = listOf(
				Pet.Cat("id.0", "Miau", "brown", 2.3f, null),
				Pet.Dog("id.1", "Toto", "white", 7.3f, 6, "bulldog", null),
			),
		)
	}
}
