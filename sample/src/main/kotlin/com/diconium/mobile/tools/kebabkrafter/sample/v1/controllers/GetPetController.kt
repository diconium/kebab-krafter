package com.diconium.mobile.tools.kebabkrafter.sample.v1.controllers

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPet
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.models.v1.PetsResponse

class GetPetController(
    private val getPetsFromDb: suspend () -> PetsResponse,
) : GetPet {

    override suspend fun CallScope.execute(type: String?, page: Int?): PetsResponse {
        // data from the call scope can easily be used here
        println("GetPet called using ${locale.toLanguageTag()}")

        // access data source (database, 3rd party systems, etc) to get real values
        return getPetsFromDb()
    }
}
