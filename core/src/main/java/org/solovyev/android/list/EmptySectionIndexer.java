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

package org.solovyev.android.list;

import android.widget.SectionIndexer;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/30/13
 * Time: 12:28 AM
 */
public final class EmptySectionIndexer implements SectionIndexer {

	private static final Object[] SECTIONS = new Object[0];

	@Nonnull
	private static final EmptySectionIndexer instance = new EmptySectionIndexer();

	private EmptySectionIndexer() {
	}

	@Nonnull
	public static EmptySectionIndexer getInstance() {
		return instance;
	}

	@Override
	public Object[] getSections() {
		return SECTIONS;
	}

	@Override
	public int getPositionForSection(int section) {
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}
}
