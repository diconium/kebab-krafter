package com.diconium.mobile.tools.kebabkrafter.sample.mock.v1

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.Pet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PetsResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.common.Color
import kotlin.time.Instant

class MockGetPetController : GetPet {
    override suspend fun CallScope.execute(type: String?, page: Int?): PetsResponse {
        // mock data
        return PetsResponse(
            count = 2,
            page = 0,
            pets = listOf(
                Pet.Cat("id.0", "Miau", Color.BROWN, 2.3f, Instant.fromEpochMilliseconds(0)),
                Pet.Dog("id.1", "Toto", Color.GREY_40, 7.3f, 6, false, "bulldog", Instant.fromEpochMilliseconds(1)),
            ),
        )
    }
}
