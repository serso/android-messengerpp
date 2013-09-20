package org.solovyev.android.messenger;

import android.os.Bundle;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import org.solovyev.android.Threads;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Account;
import org.solovyev.android.messenger.realms.AccountException;
import org.solovyev.android.messenger.realms.AccountService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 5:44 PM
 */
public class MessengerTestActivity extends RoboSherlockActivity {

	@Inject
	@Nonnull
	private AccountService accountService;

	@Nonnull
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Nonnull
	private TextView console;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mpp_test_activity);

		console = (TextView) findViewById(R.id.mpp_test_console_textview);

		executorService.execute(new Runnable() {
			@Override
			public void run() {
				for (Account account : accountService.getAccounts()) {
					try {
						final User user = account.getAccountUserService().getUserById("se.solovyev@gmail.com");
						Threads.tryRunOnUiThread(MessengerTestActivity.this, new Runnable() {
							@Override
							public void run() {
								if (user == null) {
									console.setText("null");
								} else {
									console.setText(user.getDisplayName());
								}
							}
						});
					} catch (AccountException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
