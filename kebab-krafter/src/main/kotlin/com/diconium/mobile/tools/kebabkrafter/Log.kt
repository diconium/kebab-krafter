package com.diconium.mobile.tools.kebabkrafter

import com.diconium.mobile.tools.kebabkrafter.Log.verbose
import org.gradle.api.logging.Logger

internal fun Logger.named(name: String): KebabLogger = NamedKebabLogger(this, name)

internal interface KebabLogger {
    fun d(msg: String)
    fun l(msg: String)
}

private class NamedKebabLogger(private val logger: Logger, name: String) : KebabLogger {
    private val prefix = "$PREFIX.$name |"
    override fun d(msg: String) {
        if (verbose) println("$prefix $msg")
        logger.debug("$prefix $msg")
    }

    override fun l(msg: String) {
        if (verbose) println("$prefix $msg")
        logger.lifecycle("$prefix $msg")
    }
}

private object Log {
    // for testing purposes only
    var verbose = false
}

private const val PREFIX = "KebabKrafter"
