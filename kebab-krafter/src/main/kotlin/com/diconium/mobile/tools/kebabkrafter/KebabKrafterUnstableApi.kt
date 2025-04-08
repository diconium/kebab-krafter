package com.diconium.mobile.tools.kebabkrafter

@RequiresOptIn(message = MESSAGE, level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.FIELD,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.CONSTRUCTOR,
)
annotation class KebabKrafterUnstableApi

private const val MESSAGE = """
	The transformers API exposes data models and functionalities internal to the core generator code. 
	Those can be changed at anytime without warning. 
	Developers using it understand the risk and must opt-in to this behavior.
"""
