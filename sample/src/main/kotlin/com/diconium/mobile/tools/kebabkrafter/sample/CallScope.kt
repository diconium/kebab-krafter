package com.diconium.mobile.tools.kebabkrafter.sample

import io.ktor.server.application.*
import io.ktor.server.request.*
import java.util.*

interface CallScope {
	val locale: Locale

	companion object {
		fun from(call: ApplicationCall): CallScope = CallScopeImpl(call)
	}
}

private class CallScopeImpl(private val call: ApplicationCall) : CallScope {
	override val locale: Locale by lazy {
		call.request.acceptLanguage().toLocale()
	}
}

private fun String?.toLocale() = this?.let { header ->
	val parts = header.split("-")
	if (header.length == 5 && parts.size == 2) {
		Locale(parts[0].lowercase(), parts[1].uppercase())
	} else {
		Locale.UK
	}
} ?: Locale.UK

class FakeCallScope(
	override val locale: Locale = Locale.UK,
) : CallScope
