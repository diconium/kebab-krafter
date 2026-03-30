package com.diconium.mobile.tools.kebabkrafter.sample

import com.diconium.mobile.tools.kebabkrafter.sample.controllers.v1.*
import com.diconium.mobile.tools.kebabkrafter.sample.db.Database
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.*
import io.ktor.server.routing.*
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass

object RealServices : ServiceLocator {

    private val controllers = module {
        single<GetPet> { GetPetController(Database.getAllPets) }
        single<GetPetId> { GetPetIdController(Database.findPet) }
        single<DeletePetId> { DeletePetIdController(Database.deletePet) }
        single<PostPet> { PostPetController(Database.createPet) }
        single<GetPetIdPhoto> { GetPetIdPhotoController() }
        single<GetPetIdPdf> { GetPetIdPdfController() }
    }

    private val koin = koinApplication { modules(controllers) }.koin
    override fun <T : Any> RoutingContext.getService(type: KClass<T>): T = koin.get(type)
}
