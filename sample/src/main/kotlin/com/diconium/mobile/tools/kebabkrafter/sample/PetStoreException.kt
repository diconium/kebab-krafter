package com.diconium.mobile.tools.kebabkrafter.sample

import io.ktor.http.*

class PetStoreException(
	code: HttpStatusCode,
) : Exception(code.toString())
