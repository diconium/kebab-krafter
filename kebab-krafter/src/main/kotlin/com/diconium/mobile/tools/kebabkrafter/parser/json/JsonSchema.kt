package com.diconium.mobile.tools.kebabkrafter.parser.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonSchema(
    /** type is null when there's only a `$ref` to another object */
    val type: Type?,
    /** format is used to classify the `type` */
    val format: String?,
    val description: String?,
    /** required properties */
    val required: List<String> = emptyList(),
    val properties: Map<String, JsonSchema> = emptyMap(),
    @SerialName(DEFS)
    val defs: Map<String, JsonSchema> = emptyMap(),
    val oneOf: List<JsonSchema> = emptyList(),
    val anyOf: List<JsonSchema> = emptyList(),
    @SerialName(REF)
    val ref: String?,
    /** only used for type:array */
    val items: JsonSchema?,

    /** only used for sealed types */
    val const: String?,

    /** only used for enum types */
    val enum: List<String>?,

    /** max/min are used to determine if a number should be Integer or Long */
    val minimum: Long?,
    val exclusiveMinimum: Long?,
    val maximum: Long?,
    val exclusiveMaximum: Long?,
) {

    enum class Type {
        @SerialName("object")
        Object,

        @SerialName("boolean")
        Boolean,

        @SerialName("array")
        Array,

        @SerialName("integer")
        Integer,

        @SerialName("number")
        Number,

        @SerialName("string")
        String,
    }

    companion object {
        const val DEFS = $$"$defs"
        const val REF = $$"$ref"
    }
}
