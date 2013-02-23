package org.solovyev.common.utils;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.text.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 8:14 PM
 */
public class Strings2 {

    @NotNull
    private static final List<Character> ru = Arrays.asList('А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я');
    private static final List<Character> en = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'V', 'U', 'W', 'X', 'Y', 'Z');

    @NotNull
    public static List<Character> getRussianAlphabet() {
        return ru;
    }

    public static List<Character> getEnglishAlphabet() {
        return en;
    }

    @NotNull
    public static String getAllEnumValues(@NotNull Class<? extends Enum> enumClass) {
        final StringBuilder result = new StringBuilder(500);

        boolean first = true;
        for (Enum enumValue : enumClass.getEnumConstants()) {
            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append(enumValue);
        }

        return result.toString();
    }

    @NotNull
    public static String getAllValues(@NotNull List<?> elements) {
        final StringBuilder result = new StringBuilder(10 * elements.size());

        boolean first = true;
        for (Object element : elements) {
            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append(element);
        }

        return result.toString();
    }

    @NotNull
    public static String toHtml(@NotNull CharSequence text) {
        final String newLineStr = Strings.LINE_SEPARATOR;
        assert newLineStr.length() == 1;

        final char newline = newLineStr.charAt(0);

        final StringBuilder result = new StringBuilder(text.length());
        for ( int i = 0; i < text.length(); i++ ) {
            final char ch = text.charAt(i);
            if ( newline == ch ) {
                result.append("<br>");
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }
}
