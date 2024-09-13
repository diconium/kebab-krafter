package com.diconium.mobile.tools.kebabkrafter.generator

import com.diconium.mobile.tools.kebabkrafter.generator.StringUtil.capitalizeFirstWord2
import com.diconium.mobile.tools.kebabkrafter.generator.StringUtil.toCamelCase

internal fun String.toPascalCase() = capitalizeFirstWord2(toCamelCase(this))
