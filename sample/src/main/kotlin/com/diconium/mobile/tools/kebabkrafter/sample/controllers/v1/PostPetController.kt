package com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.PostPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.Pet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PostPetRequest

class PostPetController(
    private val createPet: suspend (Pet) -> Pet,
) : PostPet {
    override suspend fun CallScope.execute(body: PostPetRequest): Pet {
        val newPet = when (body.petType) {
            PostPetRequest.PetType.DOG -> Pet.Dog("", body.name, "", 0f, 0, "", body.dateOfBirth)
            PostPetRequest.PetType.CAT -> Pet.Cat("", body.name, "", 0f, body.dateOfBirth)
            PostPetRequest.PetType.PARROT -> Pet.Parrot("", body.name, 0, body.dateOfBirth)
        }
        return createPet(newPet)
    }
}
