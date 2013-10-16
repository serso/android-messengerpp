package org.solovyev.android.messenger.realms.sms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.solovyev.android.messenger.realms.sms.SmsAccountConnection.millisToMinutesAndSeconds;

public class SmsAccountConnectionTest {

	@Test
	public void testShouldFormatTimeCorrectly() throws Exception {
		assertEquals("2m 20s", millisToMinutesAndSeconds(140 * 1000L));
		assertEquals("1m 1s", millisToMinutesAndSeconds(61 * 1000L));
	}
}
