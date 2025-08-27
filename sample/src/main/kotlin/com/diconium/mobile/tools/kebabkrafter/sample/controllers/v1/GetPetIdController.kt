package com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.PetStoreException
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPetId
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPetIdResponse
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.Pet
import io.ktor.http.*
import io.ktor.util.encodeBase64
import kotlin.random.Random

class GetPetIdController(
    private val findPetInDb: suspend (String) -> Pet?,
) : GetPetId {

    override suspend fun CallScope.execute(id: String): GetPetIdResponse {
        return findPetInDb(id)?.let {
            GetPetIdResponse(Random.nextBytes(36).encodeBase64(), it)
        } ?: throw PetStoreException(HttpStatusCode.NotFound)
    }
}
