package com.diconium.mobile.tools.kebabkrafter.plugin

import com.diconium.mobile.tools.kebabkrafter.*
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@KebabKrafterUnstableApi
interface TransformerSpec {
    @get:Optional
    @get:Input
    val endpointTransformer: Property<Class<out EndpointTransformer>>

    /**
     * Endpoints are the server routes extracted from the original swagger YML.
     * Endpoints contains HTTP related data such as path/method/headers/body.
     * This transformer allows to modify the endpoints before they are processed by the code generators.
     */
    fun endpointTransformer(block: EndpointTransformer) {
        endpointTransformer.set(block::class.java)
    }

    /**
     * Mapper that converts the Endpoint to KtorController. The controller is the basis for the code generator,
     * Controller contains code related data such as package/class/kdoc.
     *
     * The mapper is the most complex (and powerful) part of the transformer API, hence use is discouraged.
     * There is a [com.diconium.mobile.tools.kebabkrafter.DefaultKtorControllerMapper] available that is used internally, but accessible for other mappers.
     */
    @get:Optional
    @get:Input
    val ktorMapper: Property<Class<out KtorMapper>>

    fun ktorMapper(block: KtorMapper) {
        ktorMapper.set(block::class.java)
    }

    @get:Optional
    @get:Input
    val ktorTransformer: Property<Class<out KtorTransformer>>

    /**
     * Transforms individual [com.diconium.mobile.tools.kebabkrafter.KtorController] after they have been mapped.
     * This is the last step before the actual code generator.
     *
     * The [com.diconium.mobile.tools.kebabkrafter.models.Endpoint] provided here in this callback is for reference only.
     */
    fun ktorTransformer(block: KtorTransformer) {
        ktorTransformer.set(block::class.java)
    }
}

internal fun TransformerSpec.buildTransformers(): Transformers {
    // implementation notes:
    //
    // That's a very cheeky piece of code that I'm still unsure if genius or stupid.
    // Gradle tasks uses input/outputs to define UP-TO-DATE information,
    // and those inputs/outputs must be some type serializable
    // but the transformers/mappers are lambas (`fun interface`) and that was my problem.
    // The workaround here is that we set those inputs to `Class<out TYPE>` that are serializable.
    // in the extension object we capture the anonymous inner class from the lambda
    // and here we build a new instance of that lambda.
    //
    // This seems to work fine for simple lambdas, but, I can imagine on more complex scenario,
    // (e.g. if a lambda access components from the encompassing `Project`)
    // that it might not work so good, or produce unknown side effects.
    //
    // I think it's good that the transformers API is wrapped in an OptIn(KebabKrafterUnstableApi)

    val et = endpointTransformer.get().getDeclaredConstructor()
    et.isAccessible = true

    val km = ktorMapper.get().getDeclaredConstructor()
    km.isAccessible = true

    val kt = ktorTransformer.get().getDeclaredConstructor()
    kt.isAccessible = true

    return Transformers(et.newInstance(), km.newInstance(), kt.newInstance())
}
