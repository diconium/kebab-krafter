package com.diconium.mobile.tools.kebabkrafter.generator;

import org.jetbrains.annotations.NotNull;

import static java.lang.Character.*;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Code below was originally developed by krasa/StringManipulation
 * for the String manipulation IntelliJ IDEA plugin available under Apache 2 license:
 * - <a href="https://github.com/krasa/StringManipulation/">Source Code</a>
 * - <a href="https://github.com/krasa/StringManipulation/blob/master/LICENSE">License</a>
 * <p>
 * Necessary functions were copy-pasted to here without further alteration.
 */
public class StringUtil {
    private static final char EMPTY_CHAR = 0;

    private StringUtil() {
    }

    static String capitalizeFirstWord2(String str) {
        if (isEmpty(str)) {
            return str;
        } else {
            StringBuilder buf = new StringBuilder();
            boolean upperNext = true;
            char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (isLetter(c) && upperNext) {
                    buf.append(toUpperCase(c));
                    upperNext = false;
                } else {
                    if (!isLetterOrDigit(c)) {
                        upperNext = true;
                    }
                    buf.append(c);
                }

            }

            return buf.toString();
        }
    }

    static String toCamelCase(String s) {
        String[] words = splitByCharacterTypeCamelCase(s);

        boolean firstWord = true;
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (firstWord && startsWithLetter(word)) {
                words[i] = word.toLowerCase();
                firstWord = false;
                if (i > 1 && isBlank(words[i - 1]) && isAllLetterOrDigit(words[i - 2])) {
                    words[i - 1] = "";
                }
            } else if (specialWord(word)) { // multiple camelCases
                firstWord = true;
            } else {
                words[i] = capitalize(word.toLowerCase());
                if (i > 1 && isBlank(words[i - 1]) && isAllLetterOrDigit(words[i - 2])) {
                    words[i - 1] = "";
                }
            }
        }
        String join = join(words);
        join = replaceSeparatorBetweenLetters(join, '_', EMPTY_CHAR);
        join = replaceSeparatorBetweenLetters(join, '-', EMPTY_CHAR);
        join = replaceSeparatorBetweenLetters(join, '.', EMPTY_CHAR);
        return join;
    }

    private static boolean isAllLetterOrDigit(String word) {
        for (char c : word.toCharArray()) {
            if (!isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean specialWord(String word) {
        if (isBlank(word)) {
            return false;
        }
        for (char c : word.toCharArray()) {
            if (isDigit(c) || isLetter(c) || isSeparator(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWithLetter(String word) {
        return word.length() > 0 && isLetter(word.charAt(0));
    }

    @NotNull
    private static String replaceSeparatorBetweenLetters(String s, char from, char to) {
        StringBuilder buf = new StringBuilder();
        char lastChar = ' ';
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            if (c == from) {
                boolean lastDigit = isDigit(lastChar);
                boolean lastLetterOrDigit = isLetterOrDigit(lastChar);
                boolean nextDigit = nextIsDigit(s, i);
                boolean nextLetterOrDigit = nextIsLetterOrDigit(s, i);

                if (lastDigit && nextDigit) {
                    buf.append(c);
                } else if (lastLetterOrDigit && nextLetterOrDigit) {
                    if (to != EMPTY_CHAR) {
                        buf.append(to);
                    }
                } else {
                    buf.append(c);
                }
            } else {
                buf.append(c);
            }
            lastChar = c;
        }

        return buf.toString();
    }

    private static boolean nextIsDigit(String s, int i) {
        if (i + 1 >= s.length()) {
            return false;
        } else {
            return Character.isDigit(s.charAt(i + 1));
        }
    }

    private static boolean nextIsLetterOrDigit(String s, int i) {
        if (i + 1 >= s.length()) {
            return false;
        } else {
            return Character.isLetterOrDigit(s.charAt(i + 1));
        }
    }

    private static boolean isSeparator(char c) {
        return c == '.' || c == '-' || c == '_';
    }

}
