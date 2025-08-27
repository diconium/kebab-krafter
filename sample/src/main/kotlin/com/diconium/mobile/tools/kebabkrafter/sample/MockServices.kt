package com.diconium.mobile.tools.kebabkrafter.sample

import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.ServiceLocator
import com.diconium.mobile.tools.kebabkrafter.sample.gen.petstore.controllers.v1.GetPet
import com.diconium.mobile.tools.kebabkrafter.sample.mock.v1.MockGetPetController
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass

object MockServices : ServiceLocator {

    private val controllers = module {
        single<GetPet> { MockGetPetController() }
    }

    private val koin = koinApplication { modules(controllers) }.koin

    override fun <T : Any> getService(type: KClass<T>): T = koin.get(type)
}
