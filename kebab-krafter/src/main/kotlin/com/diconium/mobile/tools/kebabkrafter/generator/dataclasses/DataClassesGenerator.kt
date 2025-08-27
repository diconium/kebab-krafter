package com.diconium.mobile.tools.kebabkrafter.generator.dataclasses

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.generator.toPascalCase
import com.diconium.mobile.tools.kebabkrafter.models.BaseSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.SealedSpecModel
import com.diconium.mobile.tools.kebabkrafter.models.SpecField
import com.diconium.mobile.tools.kebabkrafter.models.SpecModel
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import java.io.File

class DataClassesGenerator(
    private val basePackageName: String,
    private val dataSpecsMap: Map<String, BaseSpecModel>,
    private val outputDirectory: File,
) {
    fun generateDataModelFiles() {
        dataSpecsMap
            .values
            .filterIsInstance<SpecModel>()
            .filter { it.parentId == null }
            .forEach { spec ->
                Log.d("- data class ${spec.relativePackageName}.${spec.name}")
                generateDataModel(spec).writeTo(outputDirectory)
            }

        dataSpecsMap
            .values
            .filterIsInstance<SealedSpecModel>()
            .associateWith { sealedType ->
                dataSpecsMap
                    .values
                    .filterIsInstance<SpecModel>()
                    .filter { it.parentId == sealedType.id }
            }.forEach { (parent: SealedSpecModel, children: List<SpecModel>) ->
                Log.d("- sealed class ${parent.relativePackageName}.${parent.name}")
                children.forEach { child ->
                    Log.d("  - data class ${child.name}")
                }
                generateSealedDataModel(parent, children).writeTo(outputDirectory)
            }
    }

    private val BaseSpecModel.asClassName: ClassName
        get() = ClassName(getFullPackageName(basePackageName), name)

    private var addJsonDiscriminator: String? = null

    private fun generateTypeSpecBuilder(dataClass: SpecModel): TypeSpec.Builder =
        TypeSpec.classBuilder(dataClass.asClassName).apply {
            dataClass.description?.let(::addKdoc)

            // Modifiers
            modifiers.run {
                remove(KModifier.PUBLIC)
                add(KModifier.DATA)
            }

            // add enum classes
            val enumLookup = mutableMapOf<SpecField.Type.Enum, TypeName>()
            val enums = dataClass
                .fields
                .filter { it.type is SpecField.Type.Enum }
                .map {
                    val enum = it.type as SpecField.Type.Enum

                    val type = (
                        dataClass.parentId?.let { parentId ->
                            dataSpecsMap[parentId]!!.asClassName.nestedClass(dataClass.name.toPascalCase())
                        } ?: dataClass.asClassName
                        ).nestedClass(it.name.toPascalCase())

                    enumLookup[enum] = type
                    TypeSpec.enumBuilder(type).apply {
                        it.description?.let(::addKdoc)
                        addAnnotation(Serializable::class)
                        enum.values.forEach {
                            val serialName = AnnotationSpec.builder(SerialName::class).addMember("\"$it\"").build()
                            addEnumConstant(it, TypeSpec.anonymousClassBuilder().addAnnotation(serialName).build())
                        }
                    }.build()
                }
            addTypes(enums)

            // used for SerialName annotation
            var serialName: String? = null

            // Constructor
            primaryConstructor(
                FunSpec.constructorBuilder().apply {
                    // Constructor Parameters
                    dataClass.fields.forEach { field ->
                        val type = field.type.toTypeName(enumLookup)
                        if (type == jsonDiscriminator) {
                            addJsonDiscriminator = field.name
                            serialName = (field.type as SpecField.Type.SealedSerializationDiscriminator).value
                        } else {
                            val param = ParameterSpec
                                .builder(field.name, type.copy(nullable = !field.isRequired))
                                .apply {
                                    if (!field.isRequired) {
                                        defaultValue("null")
                                    }
                                }.build()
                            addParameter(param)
                        }
                    }
                }.build(),
            )

            // Annotation
            addAnnotation(Serializable::class)
            serialName?.let {
                val annotation = AnnotationSpec.builder(SerialName::class).addMember("\"$it\"")
                addAnnotation(annotation.build())
            }

            // Properties
            dataClass.fields.forEach { field ->
                val type = field.type.toTypeName(enumLookup)
                if (type != jsonDiscriminator) {
                    addProperty(
                        PropertySpec.builder(
                            field.name,
                            type.copy(nullable = !field.isRequired),
                        ).apply {
                            initializer(field.name)
                            field.description?.let(::addKdoc)
                            addAnnotation(
                                AnnotationSpec.builder(SerialName::class).addMember("\"${field.name}\"").build(),
                            )
                        }.build(),
                    )
                }
            }
        }

    private fun generateDataModel(dataClass: SpecModel): FileSpec {
        // Kotlin File
        return FileSpec
            .builder(dataClass.asClassName)
            .addFileComment(AUTO_GENERATOR_WARNING)
            .addType(generateTypeSpecBuilder(dataClass).build())
            .build()
    }

    private fun generateSealedDataModel(parent: SealedSpecModel, children: List<SpecModel>): FileSpec {
        addJsonDiscriminator = null // not thread safe, but should work

        val parentClassName = parent.asClassName
        val childTypes = children.map {
            generateTypeSpecBuilder(it).superclass(parentClassName).build()
        }

        val parentType = TypeSpec.classBuilder(parentClassName).apply {
            addModifiers(KModifier.SEALED)
            addAnnotation(Serializable::class)
            addJsonDiscriminator?.let {
                val annotation = AnnotationSpec.builder(jsonDiscriminator).addMember("\"$it\"")
                addAnnotation(annotation.build())
            }
            addTypes(childTypes)
        }.build()

        return FileSpec
            .builder(parentClassName)
            .addType(parentType)
            .addFileComment(AUTO_GENERATOR_WARNING)
            .build()
    }

    private fun SpecField.Type.toTypeName(enumLookup: Map<SpecField.Type.Enum, TypeName>): TypeName = when (this) {
        SpecField.Type.Boolean -> Boolean::class.asTypeName()
        SpecField.Type.Float -> Float::class.asTypeName()
        SpecField.Type.Int -> Int::class.asTypeName()
        SpecField.Type.String -> String::class.asTypeName()
        SpecField.Type.Date -> Instant::class.asTypeName()
        is SpecField.Type.DataArray -> List::class.asTypeName().parameterizedBy(type.toTypeName(enumLookup))
        is SpecField.Type.DataModel -> dataSpecsMap[id]!!.getClassName(basePackageName)
        is SpecField.Type.SealedSerializationDiscriminator -> jsonDiscriminator
        is SpecField.Type.Enum -> enumLookup[this]!!
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val jsonDiscriminator = JsonClassDiscriminator::class.asTypeName()

private const val AUTO_GENERATOR_WARNING = """

   Please note:
   This class is auto generated by DataClasses Generator
   DO NOT edit this file manually.

"""
