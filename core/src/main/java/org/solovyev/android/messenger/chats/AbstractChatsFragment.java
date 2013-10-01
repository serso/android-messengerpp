package org.solovyev.android.messenger.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import org.solovyev.android.fragments.DetachableFragment;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.Threads2;
import org.solovyev.android.messenger.ToggleFilterInputMenuItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static org.solovyev.android.messenger.UiEventType.new_message;

public abstract class AbstractChatsFragment extends AbstractMessengerListFragment<UiChat, ChatListItem> implements DetachableFragment {

	@Nonnull
	protected static final String TAG = "ChatsFragment";

	@Nullable
	private JEventListener<ChatEvent> chatEventListener;

	public AbstractChatsFragment() {
		super(TAG, true, true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup root = super.onCreateView(inflater, container, savedInstanceState);

		addFooterButton(root, R.string.mpp_new_message, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getEventManager().fire(new_message.newEvent());
			}
		});

		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		chatEventListener = new UiThreadUserChatListener();
		getChatService().addListener(chatEventListener);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (chatEventListener != null) {
			getChatService().removeListener(chatEventListener);
		}
	}

	private class UiThreadUserChatListener extends AbstractJEventListener<ChatEvent> {

		private UiThreadUserChatListener() {
			super(ChatEvent.class);
		}

		@Override
		public void onEvent(@Nonnull final ChatEvent event) {
			Threads2.tryRunOnUiThread(AbstractChatsFragment.this, new Runnable() {
				@Override
				public void run() {
					getAdapter().onEvent(event);
				}
			});
		}
	}

	@Nonnull
	@Override
	protected abstract AbstractChatsAdapter createAdapter();

	@Nonnull
	protected AbstractChatsAdapter getAdapter() {
		return (AbstractChatsAdapter) super.getAdapter();
	}

    /*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	private ActivityMenu<Menu, MenuItem> menu;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		menuItems.add(new ToggleFilterInputMenuItem(this));

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_chats, menuItems, SherlockMenuHelper.getInstance());
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}
}
