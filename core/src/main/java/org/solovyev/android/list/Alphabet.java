package org.solovyev.android.list;


import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * User: serso
 * Date: 5/29/13
 * Time: 11:19 PM
 */
public final class Alphabet {

	private static final String EN = "en";
	private static final String EN_ALPHABET_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTVUWXYZ";
	private static final Alphabet EN_ALPHABET = Alphabet.forCharacters(EN_ALPHABET_CHARACTERS);

	private static final String RU = "ru";
	private static final String RU_ALPHABET_CHARACTERS = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЫЭЮЯ";

	/**
	 * The string of letters that make up the indexing sections.
	 */
	@Nonnull
	private final CharSequence characters;

	/**
	 * Cached length of the characters array.
	 */
	private final int length;

	/**
	 * The section array converted from the characters string.
	 */
	private final String[] letters;

	private Alphabet(@Nonnull CharSequence characters) {
		this.characters = characters;
		this.length = this.characters.length();
		this.letters = new String[length];
		for (int i = 0; i < length; i++) {
			this.letters[i] = Character.toString(this.characters.charAt(i));
		}
	}

	@Nonnull
	static Alphabet forCharacters(@Nonnull CharSequence characters) {
		return new Alphabet(characters);
	}

	@Nonnull
	public static Alphabet forDefaultLocale() {
		return Alphabet.forLocale(Locale.getDefault());
	}

	@Nonnull
	public static Alphabet forLocale(@Nonnull Locale locale) {
		final String language = locale.getLanguage();
		if (language.equals(EN)) {
			return EN_ALPHABET;
		} else {
			String alphabet = null;
			if (language.equals(RU)) {
				alphabet = RU_ALPHABET_CHARACTERS;
			}

			if (alphabet != null) {
				return Alphabet.forCharacters(alphabet + EN_ALPHABET_CHARACTERS);
			} else {
				return EN_ALPHABET;
			}
		}
	}

	@Nonnull
	CharSequence getCharacters() {
		return characters;
	}

	int getLength() {
		return length;
	}

	@Nonnull
	Character getCharacterAt(int position) {
		return this.characters.charAt(position);
	}

	@Nonnull
	String getLetterAt(int position) {
		return this.letters[position];
	}

	@Nonnull
	String[] getLetters() {
		return letters;
	}
}
