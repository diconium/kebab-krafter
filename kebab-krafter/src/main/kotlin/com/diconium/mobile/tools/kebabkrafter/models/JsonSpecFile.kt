package com.diconium.mobile.tools.kebabkrafter.models

/** Types allowed for the root model of the file */
sealed interface RootJsonType

/** Definition of one JSON schema file */
data class JsonSpecFile(
    val name: String,
    /** Main model content of the parsed file */
    val model: RootJsonType,
    /** Extra models defined in this file using `$defs` */
    val definitions: Map<String, BaseJsonSpec>,
    /** Models referenced from this file (relative file path) */
    val imports: Set<RefJsonSpec>,
)

/** root definition for the JSON types */
sealed class BaseJsonSpec {
    abstract val description: String?
}

/** defines a type */
sealed class BaseJsonType : BaseJsonSpec() {
    abstract val relativePackageName: String
    abstract val name: String
}

/** defines a sealed class (used with oneOf/anyOf) */
data class SealedJsonType(
    override val description: String?,
    override val relativePackageName: String,
    override val name: String,
    val types: Map<JsonDiscriminator, ConcreteJsonType>,
) : BaseJsonType(),
    RootJsonType {

    /** Special field for the 'const' JSON value used in oneOf/anyOf*/
    data class JsonDiscriminator(
        val key: String,
        val value: String,
        /** unused on JSON discriminator*/
        override val description: String?,
    ) : BaseJsonSpec()
}

/** defines a concrete class */
data class ConcreteJsonType(
    override val description: String?,
    override val relativePackageName: String,
    override val name: String,
    val fields: List<JsonSpecField>,
) : BaseJsonType(),
    RootJsonType {
    data class JsonSpecField(
        val name: String,
        val type: BaseJsonSpec,
        /** description added to the field should override the one in the source */
        val description: String?,
        val isRequired: Boolean,
    )
}

/** defines an enum class*/
data class EnumJsonType(
    override val description: String?,
    override val relativePackageName: String,
    override val name: String,
    val values: List<String>,
) : BaseJsonType(),
    RootJsonType

/** define primitive data types with their formating */
data class PrimitiveJsonSpec(val primitive: Primitive, override val description: String?) :
    BaseJsonSpec(),
    RootJsonType {
    sealed interface Primitive {
        data object StringSpec : Primitive
        data object IntSpec : Primitive
        data object LongSpec : Primitive
        data object BooleanSpec : Primitive
        data object FloatSpec : Primitive
        data object DoubleSpec : Primitive
        data object DateSpec : Primitive
        data class ArraySpec(val type: BaseJsonSpec) : Primitive
        data class MapSpec(val type: BaseJsonSpec) : Primitive
        data class EnumMapSpec(val keyType: BaseJsonSpec, val valueType: BaseJsonSpec) : Primitive
    }
}

/** points to additional definitions in the same file */
data class DefJsonSpec(
    val def: String,
    /** Description added to the ref should override the one in the source */
    override val description: String?,
) : BaseJsonSpec()

/** points to types defined in a different file */
data class RefJsonSpec(
    val path: String,
    /** Description added to the ref should override the one in the source */
    override val description: String?,
) : BaseJsonSpec()

fun BaseJsonSpec.smartToString(): String = when (this) {
    is SealedJsonType -> "Sealed class(${relativePackageName}$name)"
    is ConcreteJsonType -> "Concrete class(${relativePackageName}$name)"
    is RefJsonSpec -> "External ($path)"
    is DefJsonSpec -> "Definition ($def)"
    is PrimitiveJsonSpec -> "Primitives ($primitive)"
    is EnumJsonType -> "Enum ($values)"
    is SealedJsonType.JsonDiscriminator -> "Const($key/$value)"
}

fun RootJsonType.smartToString(): String = (this as BaseJsonSpec).smartToString()
fun ConcreteJsonType.smartToString(): String = (this as BaseJsonSpec).smartToString()
fun Map.Entry<SealedJsonType.JsonDiscriminator, ConcreteJsonType>.smartToString(): String =
    "${key.value}:${(value).smartToString()}"
