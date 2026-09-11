package io.github.budius.kraftkebabandroidlibary

import android.os.Parcelable
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.Pet
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.PetsResponse
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.PostPetRequest
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.common.Location
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.error.Error1
import io.github.budius.kraftkebabandroidlibary.gen.androidLib.petstore.models.v1.error.Error2
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.reflect.full.isSubclassOf

class GeneratedIsParcelableTest {

	@Test
	fun should_not_generated_parcelable() {
		listOf(
			Location::class,
			Error1::class,
			Error2::class,
			Pet::class,
			PetsResponse::class,
			PostPetRequest::class,
		).forEach {
			assertFalse(it.isSubclassOf(Parcelable::class))
		}
	}
}
