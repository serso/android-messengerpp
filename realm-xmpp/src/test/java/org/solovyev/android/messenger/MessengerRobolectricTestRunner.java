package org.solovyev.android.messenger;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 9:02 PM
 */
public class MessengerRobolectricTestRunner extends RobolectricTestRunner {
	public MessengerRobolectricTestRunner(Class<?> testClass) throws InitializationError {
		super(testClass, new RobolectricConfig(new File("realm-xmpp")));
	}
}
