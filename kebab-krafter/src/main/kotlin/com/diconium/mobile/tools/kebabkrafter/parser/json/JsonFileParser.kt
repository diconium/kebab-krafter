package com.diconium.mobile.tools.kebabkrafter.parser.json

import com.diconium.mobile.tools.kebabkrafter.Log
import com.diconium.mobile.tools.kebabkrafter.models.*
import com.diconium.mobile.tools.kebabkrafter.models.PrimitiveJsonSpec.Primitive
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class JsonFileParser(private val handler: FileHandler) {

    private val rootSchema: JsonSchema = json.decodeFromString<JsonSchema>(handler.contents)
    private val imports = mutableSetOf<RefJsonSpec>()
    private val definitions = mutableMapOf<String, BaseJsonSpec>()

    fun parse(): JsonSpecFile {
        Log.d("- Parsing: ${handler.relativeFile}")
        val model = parse(rootSchema, handler.name)

        // validation
        if (model !is RootJsonType) {
            throw IllegalArgumentException("Validation fail. Root schema was ${model::class.simpleName}")
        }

        if (model is SealedJsonType && model.types.keys.map { it.key }.toSet().size != 1) {
            throw IllegalArgumentException(
                "Validation fail. " +
                    "Sealed type contains more than one discriminator. ${model.types.keys}",
            )
        }

        Log.d("  Found ${handler.relativeFile} found ${model::class.java.simpleName}:")
        if (definitions.isNotEmpty()) {
            Log.d("  |- contains ${definitions.size} extra definition(s)")
            definitions.forEach { (name, type) ->
                Log.d("     |- $name is ${type::class.java.simpleName}")
            }
        }
        if (imports.isNotEmpty()) {
            Log.d("  |- contains ${imports.size} external import(s)")
            imports.forEach { ref ->
                Log.d("     |- ${ref.path}")
            }
        }
        return JsonSpecFile(handler.name, model, definitions.toMap(), imports)
    }

    private fun parse(schema: JsonSchema, objectName: String): BaseJsonSpec = when {
        schema.additionalProperties != null -> parseMap(schema, objectName)
        schema.ref != null -> parseRef(schema)
        schema.const != null -> SealedJsonType.JsonDiscriminator(objectName, schema.const, schema.description)
        schema.anyOf.isNotEmpty() -> parseSealed(schema, schema.anyOf, objectName)
        schema.oneOf.isNotEmpty() -> parseSealed(schema, schema.oneOf, objectName)
        schema.type != null -> when (schema.type) {
            JsonSchema.Type.Object -> parseObject(schema, objectName)
            JsonSchema.Type.Boolean -> PrimitiveJsonSpec(Primitive.BooleanSpec, schema.description)
            JsonSchema.Type.Array -> parseArray(schema, objectName)
            JsonSchema.Type.Number -> parseNumber(schema)
            JsonSchema.Type.String -> parseString(schema, objectName)
            JsonSchema.Type.Integer -> parseNumber(schema)
        }

        else -> throw IllegalArgumentException("Cannot parse ${json.encodeToString(schema)}")
    }

    private fun parseString(schema: JsonSchema, objectName: String): BaseJsonSpec = when {
        schema.enum.isNullOrEmpty().not() -> EnumJsonType(
            schema.description,
            handler.relativePackageName,
            objectName,
            schema.enum,
        )

        schema.format == "date-time" -> PrimitiveJsonSpec(Primitive.DateSpec, schema.description)
        else -> PrimitiveJsonSpec(Primitive.StringSpec, schema.description)
    }

    private fun parseNumber(schema: JsonSchema): BaseJsonSpec {
        fun parseInteger(schema: JsonSchema): BaseJsonSpec {
            // integer in JSON schema only means "no decimal points"
            // so we just define which to use Int or Long depending on the max/min values
            return if (
                schema.minimum.isTooBigOrTooSmall() ||
                schema.exclusiveMinimum.isTooBigOrTooSmall() ||
                schema.maximum.isTooBigOrTooSmall() ||
                schema.exclusiveMaximum.isTooBigOrTooSmall()
            ) {
                PrimitiveJsonSpec(Primitive.LongSpec, schema.description)
            } else {
                PrimitiveJsonSpec(Primitive.IntSpec, schema.description)
            }
        }

        // numeric have a type of either Integer or Number
        return if (schema.type == JsonSchema.Type.Integer) {
            // integer directly indicates no decimal points,  so we jump to parseInteger
            parseInteger(schema)
        } else {
            // if it's a plain Type.Number, we'll also check for the format
            when (schema.format) {
                "integer" -> parseInteger(schema)
                "double" -> PrimitiveJsonSpec(Primitive.DoubleSpec, schema.description)
                else -> PrimitiveJsonSpec(Primitive.FloatSpec, schema.description)
            }
        }
    }

    private fun parseArray(schema: JsonSchema, objectName: String): BaseJsonSpec {
        requireNotNull(schema.items) { "'type = array' requires that 'items' is not null" }
        val type = parse(schema.items, "${objectName}Items")
        return PrimitiveJsonSpec(Primitive.ArraySpec(type), schema.description)
    }

    private fun parseObject(schema: JsonSchema, objectName: String): BaseJsonSpec {
        require(schema.properties.isNotEmpty()) { "'type = object' requires that 'properties' is not empty" }
        val fields = schema.properties.map { (name, definition) ->
            val type: BaseJsonSpec = parse(definition, name)
            val description = definition.description ?: type.description
            val isRequired = schema.required.contains(name)
            ConcreteJsonType.JsonSpecField(name, type, description, isRequired)
        }
        return ConcreteJsonType(schema.description, handler.relativePackageName, objectName, fields)
    }

    private fun parseRef(schema: JsonSchema): BaseJsonSpec {
        requireNotNull(schema.ref) { "Cannot parse null 'ref'" }
        return when {
            schema.ref.startsWith("#/${JsonSchema.DEFS}/") -> {
                // extra definitions on the same file
                val key = schema.ref.replace("#/${JsonSchema.DEFS}/", "")
                val defType: JsonSchema? = rootSchema.defs[key]
                requireNotNull(defType) { "Referenced schema '${schema.ref}' not found" }

                // https://github.com/budius/kebab-krafter/issues/3
                // before was `definitions.computeIfAbsent(key) { parse(defType, key) }`
                // but that caused ConcurrentModificationException when
                // an element in $defs pointed to another element in $defs.
                val computed = definitions[key] ?: run {
                    parse(defType, key).also { definitions[key] = it }
                }

                if (computed is PrimitiveJsonSpec) {
                    // replace primitives directly
                    definitions.remove(key)
                    PrimitiveJsonSpec(
                        computed.primitive,
                        schema.description ?: computed.description,
                    )
                } else {
                    DefJsonSpec(key, schema.description)
                }
            }

            else -> {
                // external definitions with relative path
                val handler = handler.handlerOf(schema.ref)
                val ref = JsonFileParser(handler).parse()
                if (ref.model is PrimitiveJsonSpec) {
                    // pre-parse any primitive external reference
                    PrimitiveJsonSpec(
                        ref.model.primitive,
                        schema.description ?: ref.model.description,
                    )
                } else {
                    val refSpec = RefJsonSpec(handler.relativeFile, schema.description)
                    imports.add(refSpec)
                    refSpec
                }
            }
        }
    }

    private fun parseSealed(root: JsonSchema, schemas: List<JsonSchema>, objectName: String): BaseJsonSpec {
        val types: Map<SealedJsonType.JsonDiscriminator, ConcreteJsonType> = schemas.associate { schema ->

            var result: BaseJsonSpec = parse(schema, "${objectName}Item")

            // case it's a definition, let's grab it out of definitions map
            if (result is DefJsonSpec) {
                val defined = definitions.remove(result.def)
                requireNotNull(defined) { "Cannot find sealed definition ${result.smartToString()}" }
                require(defined is ConcreteJsonType) { "Defined ${defined.smartToString()} is not valid sealed type" }
                result = defined
            }

            // case it's a reference, let's parse that reference
            if (result is RefJsonSpec) {
                val handler = handler.handlerFromRoot(result.path)
                val ref = JsonFileParser(handler).parse()
                require(ref.model is ConcreteJsonType) {
                    throw IllegalArgumentException("Referenced ${ref.model.smartToString()} is not valid sealed type")
                }

                imports.remove(result)
                imports.addAll(ref.imports)

                if (ref.definitions.isNotEmpty()) {
                    Log.d("PARSER: adding ${ref.definitions.size} from ${ref.name} / ${ref.model.smartToString()}")
                    ref.definitions.forEach { (string, spec) ->
                        Log.d("PARSER: $string / ${spec.smartToString()}")
                    }
                }
                definitions.putAll(ref.definitions)

                result = ref.model
            }

            // all sealed types must be concrete types
            require(result is ConcreteJsonType) {
                throw IllegalArgumentException("${result.smartToString()} is not valid child of a sealed type")
            }

            // all sealed types must contain 'const` to differentiate
            val discriminator = result.fields.firstOrNull { it.type is SealedJsonType.JsonDiscriminator }?.type
            requireNotNull(discriminator) { "Cannot find `const` JSON discriminator for ${result.smartToString()}" }
            require(discriminator is SealedJsonType.JsonDiscriminator) {
                throw IllegalArgumentException("${result.smartToString()} is not valid child of a sealed type")
            }

            discriminator to result.copy(
                fields = result.fields.filter { it.type !is SealedJsonType.JsonDiscriminator },
            )
        }
        return SealedJsonType(root.description, handler.relativePackageName, objectName, types)
    }

    private fun parseMap(schema: JsonSchema, objectName: String): BaseJsonSpec {
        val properties = schema.additionalProperties!!
        val valueType = parse(properties, objectName)

        // Parse and validate map keys
        // The keys can only be string (default fallback) or an enum type
        val keyType: BaseJsonSpec? = when {
            schema.enum != null -> {
                EnumJsonType(
                    null,
                    handler.relativePackageName,
                    schema.const ?: objectName,
                    schema.enum,
                ).also { type ->
                    definitions["enum/${type.name}"] = type
                }
            }

            schema.type == JsonSchema.Type.String -> {
                // string is default fallback
                null
            }

            schema.ref != null -> {
                when (val result = parseRef(schema)) {
                    is DefJsonSpec -> {
                        val defined = definitions[result.def]
                        requireNotNull(defined) { "Cannot find sealed definition ${result.smartToString()}" }
                        require(defined is EnumJsonType) {
                            "Defined ${defined.smartToString()} as part of $objectName must be enum type"
                        }
                        result
                    }

                    is RefJsonSpec -> {
                        val handler = handler.handlerFromRoot(result.path)
                        val ref = JsonFileParser(handler).parse()
                        require(ref.model is EnumJsonType) {
                            throw IllegalArgumentException(
                                "Defined ${ref.model.smartToString()} " +
                                    "as part of $objectName must be enum type",
                            )
                        }
                        result
                    }

                    else -> throw IllegalStateException("${result.smartToString()} must be a enum type ")
                }
            }

            else -> null
        }

        val mapType = if (keyType != null) {
            Primitive.EnumMapSpec(keyType, valueType)
        } else {
            Primitive.MapSpec(valueType)
        }

        return PrimitiveJsonSpec(
            primitive = mapType,
            description = schema.description ?: properties.description,
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun Long?.isTooBigOrTooSmall(): Boolean = this?.let { number ->
    (number >= Int.MAX_VALUE) || (number <= Int.MIN_VALUE)
} ?: false
