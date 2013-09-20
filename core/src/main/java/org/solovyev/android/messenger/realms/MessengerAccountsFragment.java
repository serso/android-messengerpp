package org.solovyev.android.messenger.realms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.GuiEventType;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MessengerAccountsFragment extends AbstractMessengerListFragment<Account, AccountListItem> implements DetachableFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "accounts";

	@Inject
	@Nonnull
	private AccountService accountService;

	private ActivityMenu<Menu, MenuItem> menu;

	@Nullable
	private JEventListener<AccountEvent> accountEventListener;

	public MessengerAccountsFragment() {
		super("Accounts", false, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = super.onCreateView(inflater, container, savedInstanceState);

		final View realmsFooter = ViewFromLayoutBuilder.<RelativeLayout>newInstance(R.layout.mpp_realms_footer).build(this.getActivity());

		final View addRealmButton = realmsFooter.findViewById(R.id.mpp_add_realm_button);
		addRealmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final EventManager eventManager = RoboGuice.getInjector(getActivity()).getInstance(EventManager.class);
				eventManager.fire(GuiEventType.show_realm_defs.newEvent());
			}
		});

		final ListView lv = getListView(root);

		lv.addFooterView(realmsFooter, null, false);

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		accountEventListener = new UiRealmEventListener();
		accountService.addListener(accountEventListener);
	}

	@Override
	public void onDestroyView() {
		if (accountEventListener != null) {
			accountService.removeListener(accountEventListener);
		}
		super.onDestroyView();
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Nonnull
	@Override
	protected MessengerListItemAdapter<AccountListItem> createAdapter() {
		final List<AccountListItem> listItems = new ArrayList<AccountListItem>();
		for (Account account : accountService.getAccounts()) {
			if (account.getState() != AccountState.removed) {
				listItems.add(new AccountListItem(account));
			}
		}
		return new AccountsAdapter(getActivity(), listItems);
	}

	@Nullable
	@Override
	protected MessengerAsyncTask<Void, Void, List<Account>> createAsyncLoader(@Nonnull MessengerListItemAdapter<AccountListItem> adapter, @Nonnull Runnable onPostExecute) {
		return null;
	}

    /*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (this.menu == null) {
			this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_realms, RealmsMenu.class, SherlockMenuHelper.getInstance());
		}

		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item) || super.onOptionsItemSelected(item);
	}

	private static enum RealmsMenu implements IdentifiableMenuItem<MenuItem> {
		realm_add(R.id.mpp_menu_realm_add) {
			@Override
			public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
				final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
				eventManager.fire(GuiEventType.show_realm_defs.newEvent());
			}
		};

		private final int menuItemId;

		RealmsMenu(int menuItemId) {
			this.menuItemId = menuItemId;
		}

		@Nonnull
		@Override
		public Integer getItemId() {
			return this.menuItemId;
		}
	}

	@Nonnull
	@Override
	protected AccountsAdapter getAdapter() {
		return (AccountsAdapter) super.getAdapter();
	}

	private class UiRealmEventListener extends AbstractJEventListener<AccountEvent> {

		private UiRealmEventListener() {
			super(AccountEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final AccountEvent accountEvent) {
			Threads2.tryRunOnUiThread(MessengerAccountsFragment.this, new Runnable() {
				@Override
				public void run() {
					getAdapter().onRealmEvent(accountEvent);
				}
			});
		}
	}
}
