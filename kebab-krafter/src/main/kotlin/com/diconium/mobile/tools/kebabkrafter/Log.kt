package com.diconium.mobile.tools.kebabkrafter

import org.gradle.api.logging.Logger

object Log {

    var verbose = false

    fun d(msg: String) {
        if (verbose) println("$PREFIX $msg")
        logger?.debug("$PREFIX $msg")
    }

    fun l(msg: String) {
        if (verbose) println("$PREFIX $msg")
        logger?.lifecycle("$PREFIX $msg")
    }

    internal var logger: Logger? = null
}

private const val PREFIX = "KebabKrafter|"
