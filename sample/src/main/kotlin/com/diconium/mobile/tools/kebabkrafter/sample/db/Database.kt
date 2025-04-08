package com.diconium.mobile.tools.kebabkrafter.sample.db

import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.Pet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PetsResponse

// here's some fake database responses
// just for illustration purposes
object Database {

    private var ids = 1

    private val data = mutableListOf(
        Pet.Cat("id.0", "Miau", "brown", 2.3f, null),
        Pet.Dog("id.1", "Toto", "white", 7.3f, 6, "bulldog", null),
    )

    val getAllPets: suspend () -> PetsResponse = {
        PetsResponse(
            count = data.size,
            page = 0,
            pets = data,
        )
    }

    val findPet: suspend (String) -> Pet? = { id ->
        data.firstOrNull { it.id == id }
    }

    val deletePet: suspend (String) -> Unit = { id ->
        data.removeIf { it.id == id }
    }

    val createPet: suspend (Pet) -> Pet = { pet ->
        ids++
        pet.setId("id.$ids")
    }
}

private val Pet.id: String
    get() {
        return when (this) {
            is Pet.Cat -> this.id
            is Pet.Dog -> this.id
            is Pet.Parrot -> this.id
        }
    }

private fun Pet.setId(id: String): Pet {
    return when (this) {
        is Pet.Cat -> this.copy(id = id)
        is Pet.Dog -> this.copy(id = id)
        is Pet.Parrot -> this.copy(id = id)
    }
}
