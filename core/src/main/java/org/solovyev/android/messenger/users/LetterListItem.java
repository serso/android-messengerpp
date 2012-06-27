package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.R;
import org.solovyev.android.view.TextViewBuilder;
import org.solovyev.common.utils.StringUtils2;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * User: serso
 * Date: 6/5/12
 * Time: 6:51 PM
 */
public class LetterListItem implements ListItem<View> {

    @NotNull
    private static final String TAG_PREFIX = "letter_list_item_";

    @NotNull
    private String letter;

    private static final Multimap<String, Character> alphabets = HashMultimap.create();

    static {
        alphabets.putAll("ru", StringUtils2.getRussianAlphabet());
        alphabets.putAll("en", StringUtils2.getEnglishAlphabet());
    }

    @NotNull
    public static List<ListItem<? extends View>> getForLocale(@NotNull Locale locale) {
        Collection<Character> characters = alphabets.get(locale.getLanguage());
        if (characters == null) {
            characters = alphabets.get("en");
        }

        return Lists.newArrayList(Iterables.transform(characters, new Function<Character, ListItem<? extends View>>() {
            @Override
            public ListItem<View> apply(@Nullable Character input) {
                assert input != null;
                return new LetterListItem(input);
            }
        }));
    }

    private LetterListItem(@NotNull Character character) {
        this.letter = character.toString();
    }

    @Override
    public OnClickAction getOnClickAction() {
        return null;
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @NotNull
    @Override
    public View updateView(@NotNull Context context, @NotNull View view) {
        if (createTag().equals(view.getTag())) {
            ((TextView) view).setText(letter);
            return view;
        } else {
            return build(context);
        }
    }

    @NotNull
    @Override
    public View build(@NotNull Context context) {
        final TextView result = TextViewBuilder.newInstance(R.layout.msg_list_item_letter, createTag()).build(context);
        result.setText(letter);
        return result;
    }

    @NotNull
    private String createTag() {
        return TAG_PREFIX + letter;
    }

    @Override
    public String toString() {
        return letter;
    }
}
