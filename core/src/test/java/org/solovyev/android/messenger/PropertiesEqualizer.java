package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.Objects;
import org.solovyev.common.equals.CollectionEqualizer;
import org.solovyev.common.equals.Equalizer;

import static org.solovyev.common.Objects.areEqual;

public class PropertiesEqualizer implements Equalizer<AProperties> {
	@Override
	public boolean areEqual(@Nonnull AProperties p1, @Nonnull AProperties p2) {
		return Objects.areEqual(p1.getPropertiesCollection(), p2.getPropertiesCollection(), new CollectionEqualizer<AProperty>(null));
	}
}
