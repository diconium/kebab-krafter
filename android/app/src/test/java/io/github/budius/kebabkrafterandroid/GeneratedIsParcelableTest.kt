package io.github.budius.kebabkrafterandroid

import android.os.Parcelable
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.Pet
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.PetsResponse
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.PostPetRequest
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.common.Location
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.error.Error1
import io.github.budius.kebabkrafterandroid.gen.android.petstore.models.v1.error.Error2
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.reflect.full.isSubclassOf

class GeneratedIsParcelableTest {

	@Test
	fun should_generated_parcelable() {
		listOf(
			Location::class,
			Error1::class,
			Error2::class,
			Pet::class,
			PetsResponse::class,
			PostPetRequest::class,
		).forEach {
			assertTrue(it.isSubclassOf(Parcelable::class))
		}
	}
}
