package com.diconium.mobile.tools.kebabkrafter.sample.v1.controllers

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.PetStoreException
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPetIdPhoto
import io.ktor.http.*
import java.io.InputStream

class GetPetIdPhotoController : GetPetIdPhoto {
    override suspend fun CallScope.execute(id: String): InputStream {
        return this::class.java.classLoader.getResourceAsStream("petstore/cat.png")
            ?: throw PetStoreException(HttpStatusCode.NotFound)
    }
}
