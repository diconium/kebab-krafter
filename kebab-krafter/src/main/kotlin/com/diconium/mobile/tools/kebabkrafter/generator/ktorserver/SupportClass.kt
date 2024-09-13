package com.diconium.mobile.tools.kebabkrafter.generator.ktorserver

import com.diconium.mobile.tools.kebabkrafter.generator.KtorController

internal val KtorController.Response.serverRequiresSupportClass: Boolean
	get() = this.headers.isNotEmpty()
