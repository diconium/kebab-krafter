package com.diconium.mobile.tools.kebabkrafter.parser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsonSchema(
	val type: String,
	val description: String?,
	val required: List<String> = emptyList(),
	val properties: Map<String, Item> = emptyMap(),
	@SerialName("\$defs")
	val defs: Map<String, JsonSchema> = emptyMap(),
	val oneOf: List<OneAnyOf> = emptyList(),
	val anyOf: List<OneAnyOf> = emptyList(),
) {
	@Serializable
	data class Item(
		/** type is null when there's only a `$ref` to another object */
		val type: String?,

		val description: String?,

		/** format is used to classic the `number` type */
		val format: String?,
		@SerialName("\$ref")
		val ref: String?,

		/** only used for type:array */
		val items: Items?,

		/** only used for sealed types */
		val const: String?,

		/** only used for enum types */
		val enum: List<String>?,
	)

	@Serializable
	data class Items(
		val type: String?,
		/** format is used to classic the `number` type */
		val format: String?,
		@SerialName("\$ref")
		val ref: String?,
	)

	@Serializable
	data class OneAnyOf(
		@SerialName("\$ref")
		val ref: String,
	)

	@Serializable
	data class OneOfItem(
		val const: String?,
		@SerialName("\$ref")
		val ref: String?,
	)
}
