package com.diconium.mobile.tools.kebabkrafter.models

import com.squareup.kotlinpoet.ClassName

sealed class BaseSpecModel {
    abstract val id: String
    abstract val description: String?
    abstract val relativePackageName: String
    abstract val name: String

    val ref: String
        get() = "$relativePackageName/$name"

    fun getFullPackageName(basePackageName: String) = "$basePackageName.$relativePackageName"

    fun getClassName(basePackageName: String): ClassName =
        ClassName.bestGuess(getClassNameFromRef("$basePackageName.$relativePackageName/$name"))

    private fun getClassNameFromRef(ref: String): String = ref.replace("/", ".")
}

data class SealedSpecModel(
    override val id: String,
    override val description: String?,
    override val relativePackageName: String,
    override val name: String,
    val types: List<String>,
) : BaseSpecModel()

data class SpecModel(
    override val id: String,
    override val description: String?,
    override val relativePackageName: String,
    override val name: String,
    val fields: List<SpecField>,
    val parentId: String? = null,
) : BaseSpecModel()

data class SpecField(val name: String, val type: Type, val isRequired: Boolean, val description: String?) {
    sealed interface Type {
        data object String : Type
        data object Int : Type
        data object Boolean : Type
        data object Float : Type
        data object Date : Type
        data class SealedSerializationDiscriminator(val value: kotlin.String) : Type
        data class DataModel(val id: kotlin.String) : Type
        data class DataArray(val type: Type) : Type
        data class Enum(val values: List<kotlin.String>) : Type
    }
}
