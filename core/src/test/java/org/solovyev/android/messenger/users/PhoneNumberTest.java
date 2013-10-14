package org.solovyev.android.messenger.users;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertTrue;
import static org.solovyev.android.messenger.users.PhoneNumber.newPhoneNumber;

@RunWith(RobolectricTestRunner.class)
public class PhoneNumberTest {

	@Test
	public void testShouldBeTheSame() throws Exception {
		assertTrue(newPhoneNumber("345-67-89").same(newPhoneNumber("3456789")));
		assertTrue(newPhoneNumber("+345  67-89").same(newPhoneNumber("+345  6789")));
	}
}
