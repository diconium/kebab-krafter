package com.diconium.mobile.tools.kebabkrafter.generator.dataclasses

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.*
import com.diconium.mobile.tools.kebabkrafter.models.*
import com.diconium.mobile.tools.kebabkrafter.models.PrimitiveJsonSpec.Primitive
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.nameWithoutExtension
import kotlin.time.ExperimentalTime

class DataClassGenerator(
    private val outputDirectory: File,
    private val basePackageName: String,
    private val path: String,
    private val root: JsonSpecFile,
) {

    fun generate() {
        Log.d("packageName($packageName), className($fileClassName)")
        FileSpec
            .builder(packageName, fileClassName)
            .indent()
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addType(
                // main type of this file
                model(root.model)
                    // subtypes added as #/$defs/
                    .addTypes(root.definitions.models())
                    .build(),
            )
            .build()
            .writeTo(outputDirectory)
    }

    private fun Map<String, BaseJsonSpec>.models() = this.map { (_, spec) ->
        model(spec as RootJsonType).build()
    }

    private fun model(model: RootJsonType): TypeSpec.Builder = when (model) {
        is ConcreteJsonType -> concreteModel(model, listOf(root.name.toPascalCase()))
        is SealedJsonType -> sealedModel(model)
        is EnumJsonType -> enumModel(model)
        is PrimitiveJsonSpec ->
            throw IllegalStateException(
                "Cannot generate root models for primitive types in $path " +
                    "and type ${(model as BaseJsonSpec).smartToString()}.",
            )
    }

    private fun concreteModel(model: ConcreteJsonType, parents: List<String>): TypeSpec.Builder {
        // handler for concrete models inside concrete models
        // to export is as Kotlin, we need to refer the classname by their parent classes too
        //
        val nextParents = if (parents.contains(model.name.toPascalCase())) {
            parents
        } else {
            parents + model.name.toPascalCase()
        }

        val types = model.fields.associateWith { field ->
            field.type.asPoetType(nextParents).required(field.isRequired)
        }

        return TypeSpec
            .classBuilder(ClassName(packageName, model.name.toPascalCase()))
            .apply { model.description?.let(::addKdoc) }
            .addAnnotation(Serializable::class)
            .dataClass()
            .primaryConstructor(
                FunSpec.constructorBuilder().apply {
                    model.fields.forEach { field ->
                        val name = field.name.toCamelCase()
                        val param = ParameterSpec
                            .builder(name, types[field]!!)
                            .apply { if (field.isRequired.not()) defaultValue("null") }
                            .build()
                        addParameter(param)
                    }
                }.build(),
            )
            .addProperties(
                model.fields.map { field ->
                    val name = field.name.toCamelCase()
                    PropertySpec
                        .builder(name, types[field]!!)
                        .initializer(name)
                        .addAnnotation(serialNameAnnotation(field.name))
                        .apply {
                            if (field.type is PrimitiveJsonSpec && field.type.primitive is Primitive.DateSpec) {
                                addAnnotation(optInExperimentalTime)
                            }
                            field.description?.let(::addKdoc)
                        }
                        .build()
                },
            )
            .addTypes(
                model.fields.mapNotNull { jsonSpecField ->
                    when (jsonSpecField.type) {
                        is ConcreteJsonType -> concreteModel(jsonSpecField.type, nextParents)
                        is EnumJsonType -> enumModel(jsonSpecField.type)
                        is SealedJsonType -> sealedModel(jsonSpecField.type)
                        is DefJsonSpec -> null
                        is PrimitiveJsonSpec -> null
                        is RefJsonSpec -> null
                        is SealedJsonType.JsonDiscriminator -> null
                    }?.build()
                },
            )
    }

    private fun sealedModel(model: SealedJsonType): TypeSpec.Builder {
        val sealedClass = ClassName(packageName, model.name.toPascalCase())
        val jsonDiscriminatorKey = mutableSetOf<String>()
        return TypeSpec
            .classBuilder(sealedClass)
            .apply { model.description?.let(::addKdoc) }
            .addAnnotation(Serializable::class)
            .addModifiers(KModifier.SEALED)
            .addTypes(
                model.types.map { (discriminator, type) ->
                    jsonDiscriminatorKey.add(discriminator.key)
                    concreteModel(type, emptyList())
                        .superclass(sealedClass)
                        .addAnnotation(serialNameAnnotation(discriminator.value))
                        .build()
                },
            )
            .apply {
                require(jsonDiscriminatorKey.size == 1) {
                    "Sealed class with more than 1 discriminator ${model.name}: $jsonDiscriminatorKey"
                }
            }
            .addAnnotation(optInExperimentalSerializationApi)
            .addAnnotation(
                AnnotationSpec
                    .builder(jsonDiscriminator)
                    .addMember("\"${jsonDiscriminatorKey.first()}\"")
                    .build(),
            )
    }

    private fun enumModel(model: EnumJsonType): TypeSpec.Builder = TypeSpec
        .enumBuilder(ClassName(packageName, model.name.toPascalCase()))
        .apply { model.description?.let(::addKdoc) }
        .addAnnotation(Serializable::class)
        .apply {
            val startsLowerCase = model.values.any { it.first().isLowerCase() }
            model.values.forEach {
                val serialName = AnnotationSpec.builder(SerialName::class).addMember("\"$it\"").build()

                // https://kotlinlang.org/docs/coding-conventions.html#property-names
                // Kotlin can use either PascalCase or SCREAMING_SNAKE_CASE for enums fields,
                // here we just validate the first letter, if it's not upper, we go for SCREAMING_SNAKE_CASE
                // because it's the most commonly used.
                // TODO: gradle config parameter for this decision
                val field = if (startsLowerCase) {
                    it.toScreamingSnakeCase()
                }
                // if it was already upper case, we assume the author already wrote exactly as they wanted
                else {
                    it
                }
                addEnumConstant(field, TypeSpec.anonymousClassBuilder().addAnnotation(serialName).build())
            }
        }

    private val packageName: String by lazy {
        Path(basePackageName.replace(".", File.separator), path).parent.toString().replace(File.separator, ".")
    }

    private val fileClassName: String by lazy { Path(path).toFile().nameWithoutExtension.toPascalCase() }

    private fun classNameOfFile(filePath: String): ClassName =
        with(Path(basePackageName.replace(".", File.separator) + File.separator + filePath)) {
            ClassName(parent.toString().replace("/", "."), nameWithoutExtension.toPascalCase())
        }

    private fun packageFor(name: String): String = basePackageName + "." + name.replace("/", ".")

    private fun BaseJsonSpec.asPoetType(parents: List<String>): TypeName = when (this) {
        is BaseJsonType -> if (parents.isEmpty()) {
            ClassName(packageFor(relativePackageName), name.toPascalCase())
        } else {
            ClassName(packageName, parents + name.toPascalCase())
        }

        is DefJsonSpec -> ClassName(packageName, root.name.toPascalCase(), def.toPascalCase())

        is PrimitiveJsonSpec -> when (this.primitive) {
            is Primitive.ArraySpec ->
                List::class
                    .asTypeName()
                    .parameterizedBy(this.primitive.type.asPoetType(parents))

            is Primitive.BooleanSpec -> Boolean::class.asTypeName()
            is Primitive.DateSpec -> kotlinTimeInstant
            is Primitive.DoubleSpec -> Double::class.asTypeName()
            is Primitive.FloatSpec -> Float::class.asTypeName()
            is Primitive.IntSpec -> Int::class.asTypeName()
            is Primitive.StringSpec -> String::class.asTypeName()
            is Primitive.LongSpec -> Long::class.asTypeName()
            is Primitive.MapSpec -> Map::class.asTypeName()
                .parameterizedBy(String::class.asTypeName())
                .plusParameter(this.primitive.type.asPoetType(parents))
        }

        is RefJsonSpec -> classNameOfFile(path)
        is SealedJsonType.JsonDiscriminator ->
            throw IllegalStateException("JsonDiscriminator found in $path -> ${this.smartToString()}")
    }

    private fun TypeName.required(isRequired: Boolean): TypeName = this.copy(nullable = isRequired.not())

    private fun TypeSpec.Builder.dataClass() = apply {
        modifiers.remove(KModifier.PUBLIC)
        modifiers.add(KModifier.DATA)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val jsonDiscriminator = JsonClassDiscriminator::class.asTypeName()

// kotlin.time.Instant::class.asTypeName()
// directly reference fails with: "Unable to load class 'kotlin.time.Instant'."
private val kotlinTimeInstant = ClassName.bestGuess("kotlin.time.Instant")

// OptIn::class.asTypeName()
// directly reference fails with: "This class can only be used as an annotation."
private val optIn = ClassName.bestGuess("kotlin.OptIn")

private val optInExperimentalTime = AnnotationSpec
    .builder(optIn)
    .addMember("%T::class", ExperimentalTime::class.asTypeName())
    .build()

private val optInExperimentalSerializationApi = AnnotationSpec
    .builder(optIn)
    .addMember("%T::class", ExperimentalSerializationApi::class.asTypeName())
    .build()

private fun serialNameAnnotation(name: String) = AnnotationSpec
    .builder(SerialName::class)
    .addMember("\"${name}\"")
    .build()
