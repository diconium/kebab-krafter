package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.generator.StringUtil.*

internal fun String.toPascalCase() = capitalizeFirstWord2(toCamelCase(this))
internal fun String.toCamelCase() = toCamelCase(this)
internal fun String.toScreamingSnakeCase() = wordsAndHyphenAndCamelToConstantCase(this)
