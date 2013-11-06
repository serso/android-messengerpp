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

package org.solovyev.android.messenger.realms.sms;

import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.os.SystemClock.elapsedRealtime;
import static java.lang.Math.min;
import static org.joda.time.DateTime.now;

final class Call {

	@Nullable
	private final String number;

	private final boolean incoming;

	@Nonnull
	private final DateTime date;

	private final long startTime;

	private long endTime;

	private Call(@Nullable String number, boolean incoming) {
		this.number = number;
		this.incoming = incoming;
		this.date = now();
		this.startTime = elapsedRealtime();
	}

	@Nonnull
	static Call newNoCall() {
		return new Call(null, true);
	}

	@Nonnull
	static Call newIncomingCall(@Nullable String number) {
		return new Call(number, true);
	}

	@Nonnull
	public static Call newOutgoingCall(@Nonnull String number) {
		return new Call(number, false);
	}

	public void onEnd() {
		this.endTime = elapsedRealtime();
	}

	@Nullable
	public String getNumber() {
		return number;
	}

	@Nonnull
	public DateTime getDate() {
		return date;
	}

	public long getDurationMillis() {
		return min(endTime - startTime, 0);
	}

	public boolean isIncoming() {
		return incoming;
	}
}
