package org.solovyev.android.messenger.messages;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AndroidUtils;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatListItem;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.StringUtils;
import org.solovyev.common.utils.StringUtils2;

import java.util.List;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 10:42 PM
 */
public class MessengerMessagesActivity extends MessengerFragmentActivity implements ViewPager.OnPageChangeListener, UserEventListener {

    @NotNull
    private static final String CHAT_ID = "chat_id";

    public static void startActivity(@NotNull Activity activity, @NotNull Chat chat) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerMessagesActivity.class);
        result.putExtra(CHAT_ID, chat.getId());
        activity.startActivity(result);
    }

    private int pagerPosition = 0;

    @Nullable
    private ViewPager pager;

    @NotNull
    private Chat chat;

    @Nullable
    private User contact;

    public MessengerMessagesActivity() {
        super(R.layout.msg_main_view_pager_grid, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = this.getIntent();
        if (intent != null) {
            final String chatId = intent.getExtras().getString(CHAT_ID);
            if (chatId != null) {
                final Chat chatFromService = getServiceLocator().getChatService().getChatById(chatId, this);
                if (chatFromService != null) {
                    this.chat = chatFromService;
                } else {
                    this.finish();
                }
            } else {
                this.finish();
            }
        } else {
            this.finish();
        }

        getServiceLocator().getUserService().addUserEventListener(this);

        final List<User> participants = getChatService().getParticipantsExcept(chat.getId(), getUser().getId(), this);
        if (chat.isPrivate()) {
            if (!participants.isEmpty()) {
                contact = participants.get(0);
            }
        }

        final MessagesFragmentPagerAdapter adapter = new MessagesFragmentPagerAdapter(getSupportFragmentManager(),
                getString(R.string.c_messages), chat);

        pager = initTitleForViewPager(this, this, adapter);

        final ImageButton attachButton = createFooterImageButton(R.drawable.msg_attach_icon, R.string.c_attach);
        getFooterLeft().addView(attachButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        final EditText messageBox = ViewFromLayoutBuilder.<EditText>newInstance(R.layout.msg_message_box).build(this);
        getFooterCenter().addView(messageBox, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Button sendButton = createFooterButton(R.string.c_send);
        getFooterRight().addView(sendButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageText = StringUtils2.toHtml(messageBox.getText());

                if (!StringUtils.isEmpty(messageText)) {
                    Toast.makeText(MessengerMessagesActivity.this, "Sending...", Toast.LENGTH_SHORT).show();

                    new SendMessageAsyncTask(MessengerMessagesActivity.this, chat) {
                        @Override
                        protected void onSuccessPostExecute(@Nullable List<ChatMessage> result) {
                            super.onSuccessPostExecute(result);
                            messageBox.setText("");
                        }
                    }.execute(new SendMessageAsyncTask.Input(getUser(), messageText, chat));
                }
            }
        });

        final ImageButton backButton = createFooterImageButton(R.drawable.msg_back, R.string.c_back);
        getHeaderLeft().addView(backButton, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessengerMessagesActivity.this.finish();
            }
        });

        final View headerCenterView = ViewFromLayoutBuilder.newInstance(R.layout.msg_message_header_title).build(this);

        if (AndroidUtils.getScreenOrientation(this) != Configuration.ORIENTATION_LANDSCAPE) {
            // message title
            final TextView messageTitle = (TextView) headerCenterView.findViewById(R.id.message_header_title);
            messageTitle.setText(ChatListItem.getDisplayName(chat, getChatService().getLastMessage(chat.getId(), this), getUser()));
        }
        getHeaderCenter().addView(headerCenterView);

        // online icon
        if (contact != null) {
            changeOnlineStatus(contact.isOnline());
        }

        // contact icon
        if (contact != null) {
            final ImageView contactIcon = createFooterImageButton(R.drawable.empty_icon, R.string.c_contact);

            final String imageUri = contact.getPropertyValueByName("photo");
            if (!StringUtils.isEmpty(imageUri)) {
                MessengerConfigurationImpl.getInstance().getServiceLocator().getRemoteFileService().loadImage(imageUri, contactIcon, R.drawable.empty_icon);
            }

            getHeaderRight().addView(contactIcon, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void changeOnlineStatus(boolean online) {
        if (AndroidUtils.getScreenOrientation(this) != Configuration.ORIENTATION_LANDSCAPE) {
            final TextView contactOnline = (TextView) getHeaderCenter().findViewById(R.id.contact_online);
            if (online) {
                contactOnline.setText("·");
            } else {
                contactOnline.setText("");
            }
        }
    }

    @NotNull
    private ChatService getChatService() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator().getChatService();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        this.pagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getServiceLocator().getUserService().removeUserEventListener(this);
    }

    @Override
    public void onUserEvent(@NotNull final User eventUser, @NotNull final UserEventType userEventType, @Nullable final Object data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (contact != null) {
                    if (userEventType == UserEventType.contact_online) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            changeOnlineStatus(true);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.contact_offline) {
                        final User eventContact = (User) data;
                        if (contact.equals(eventContact)) {
                            changeOnlineStatus(false);
                            contact = eventContact;
                        }
                    }

                    if (userEventType == UserEventType.changed) {
                        if (eventUser.equals(contact)) {
                            contact = eventUser;
                        }
                    }
                }
            }
        });
    }

    public static class MessagesFragmentPagerAdapter extends FragmentPagerAdapter {

        @NotNull
        private String chatsTitle;

        @NotNull
        private Chat chat;

        public MessagesFragmentPagerAdapter(@NotNull FragmentManager fm, @NotNull String chatsTitle, @NotNull Chat chat) {
            super(fm);
            this.chatsTitle = chatsTitle;
            this.chat = chat;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MessengerMessagesFragment(chat);
                default:
                    return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return chatsTitle;
                default:
                    return null;
            }
        }
    }
}

