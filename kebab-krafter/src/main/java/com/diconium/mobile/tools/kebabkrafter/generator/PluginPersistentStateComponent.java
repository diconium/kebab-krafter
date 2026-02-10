package com.diconium.mobile.tools.kebabkrafter.generator;

/**
 * The methods here are helpers to the `StringUtil.java`.
 * In the original plugin this got copied from there's this "plugin settings manager"
 * that provided some flags for the processing.
 * We didn't need all that and just stubbed them with hardcoded answers that seem to work reliably for our use case.
 * In case of any issues on the `toScreamingSnakeCase()`, those flags could be checked.
 * src/main/java/osmedile/intellij/stringmanip/config/PluginPersistentStateComponent.java
 *
 */
public class PluginPersistentStateComponent {
    private PluginPersistentStateComponent() {
    }

    static boolean isSeparatorBeforeDigit() {
        return true;
    }

    static boolean isSeparatorAfterDigit() {
        return false;
    }

    static boolean putSeparatorBetweenUppercases() {
        return false;
    }
}
