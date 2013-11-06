/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
