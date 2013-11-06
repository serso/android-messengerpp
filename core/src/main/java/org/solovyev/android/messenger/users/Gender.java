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

package org.solovyev.android.messenger.users;

import org.solovyev.android.Labeled;
import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:46 PM
 */
public enum Gender implements Labeled {
	male(R.string.mpp_male),
	female(R.string.mpp_female);

	private int captionResId;

	private Gender(int captionResId) {
		this.captionResId = captionResId;
	}


	@Override
	public int getCaptionResId() {
		return this.captionResId;
	}
}
