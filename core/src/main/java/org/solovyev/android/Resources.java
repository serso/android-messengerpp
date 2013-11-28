package org.solovyev.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.common.text.Strings.LINE_SEPARATOR;

public final class Resources {

	private Resources() {
	}

	@Nonnull
	public static String readRawResourceAsString(int resourceId, @Nonnull android.content.res.Resources resources) throws IOException {
		return readRawResourceAsString(resourceId, resources, null);
	}

	@Nonnull
	public static String readRawResourceAsString(int resourceId, @Nonnull android.content.res.Resources resources, @Nullable LineProcessor lineProcessor) throws IOException {
		final StringBuilder result = new StringBuilder();

		final InputStream inputStream = resources.openRawResource(resourceId);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line = readLine(reader, lineProcessor);
			while (line != null) {
				result.append(line);
				result.append(LINE_SEPARATOR);
				line = readLine(reader, lineProcessor);
			}
		} finally {
			if (reader != null) {
				reader.close();
			} else {
				inputStream.close();
			}
		}


		return result.toString();
	}

	@Nullable
	private static String readLine(@Nonnull BufferedReader reader, @Nullable LineProcessor lineProcessor) throws IOException {
		final String line = reader.readLine();
		if(line != null && lineProcessor != null) {
			return lineProcessor.process(line);
		} else {
			return line;
		}
	}

	public static interface LineProcessor {
		@Nonnull
		String process(@Nonnull String line);
	}
}
