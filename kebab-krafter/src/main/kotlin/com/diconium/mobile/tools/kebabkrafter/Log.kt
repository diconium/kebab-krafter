package com.diconium.mobile.tools.kebabkrafter

import org.gradle.api.logging.Logger

object Log {
    var verbose = false
    fun d(msg: String) {
        if (verbose) println(msg)
        logger?.let { it.debug(msg) }
    }

    internal var logger: Logger? = null
}
