package com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.PetStoreException
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPetIdPdf
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPetIdPdfResponse
import io.ktor.http.*

class GetPetIdPdfController : GetPetIdPdf {
    override suspend fun CallScope.execute(id: String): GetPetIdPdfResponse {
        val stream = this::class.java.classLoader.getResourceAsStream("petstore/cat.pdf")
            ?: throw PetStoreException(HttpStatusCode.NotFound)
        return GetPetIdPdfResponse(
            headerContentDisposition = "cat.pdf",
            body = stream,
        )
    }
}
