package com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1

import com.diconium.mobile.tools.kebabkrafter.sample.CallScope
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.DeletePetId

class DeletePetIdController(
    private val deletePetFromDb: suspend (String) -> Unit,
) : DeletePetId {
    override suspend fun CallScope.execute(id: String) {
        deletePetFromDb(id)
    }
}
