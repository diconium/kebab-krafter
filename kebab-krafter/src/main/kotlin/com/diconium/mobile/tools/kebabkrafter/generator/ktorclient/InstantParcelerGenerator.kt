package com.diconium.mobile.tools.kebabkrafter.generator.ktorclient

import com.diconium.mobile.tools.kebabkrafter.generator.AUTO_GENERATOR_WARNING
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import java.io.File

object InstantParcelerGenerator {
    fun generate(outputDirectory: File, packageName: String) {
        val folder = File(outputDirectory, packageName.replace(".", "/"))
        folder.mkdirs()
        File(folder, "InstantParceler.kt").writeText(CODE.replace(PACKAGE_NAME, packageName))
    }
}

private fun instantParcelerClassName(packageName: String) = ClassName(packageName, "InstantParceler")

private fun nullableInstantParcelerClassName(packageName: String) = ClassName(packageName, "NullableInstantParceler")

fun PropertySpec.Builder.instantParceler(
    parcelable: Boolean,
    isRequired: Boolean,
    outputDirectory: File,
    basePackageName: String,
) = apply {
    if (parcelable) {
        val packageName = "$basePackageName.parceler"
        InstantParcelerGenerator.generate(outputDirectory, packageName)
        addAnnotation(
            AnnotationSpec.builder(
                ClassName("kotlinx.parcelize", "TypeParceler").parameterizedBy(
                    ClassName("kotlin.time", "Instant"),
                    if (isRequired) {
                        instantParcelerClassName(packageName)
                    } else {
                        nullableInstantParcelerClassName(packageName)
                    },
                ),
            ).build(),
        )
    }
}

private const val PACKAGE_NAME = "<INSERT_HERE>"
private const val CODE = """@file:OptIn(ExperimentalTime::class)
package $PACKAGE_NAME
/*
$AUTO_GENERATOR_WARNING
*/

import android.os.Parcel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.parcelize.Parceler

object InstantParceler : Parceler<Instant> {

    override fun create(parcel: Parcel) = Instant.fromEpochMilliseconds(parcel.readLong())

    override fun Instant.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this.toEpochMilliseconds())
    }
}

object NullableInstantParceler : Parceler<Instant?> {

    override fun create(parcel: Parcel): Instant? = parcel.readLong()
        .takeIf { it != NULL }
        ?.let { Instant.fromEpochMilliseconds(it) }

    override fun Instant?.write(parcel: Parcel, flags: Int) {
        parcel.writeLong(this?.toEpochMilliseconds() ?: NULL)
    }

    private val NULL = Instant.DISTANT_PAST.toEpochMilliseconds()
}
"""
