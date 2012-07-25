package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.viewpagerindicator.TitlePageIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.AndroidUtils;
import org.solovyev.android.AndroidUtils2;
import org.solovyev.android.RuntimeIoException;
import org.solovyev.android.ext.FooterButtonBuilder;
import org.solovyev.android.ext.FooterImageButtonBuilder;
import org.solovyev.android.messenger.chats.MessengerChatsActivity;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.messages.MessengerMessagesActivity;
import org.solovyev.android.messenger.security.UserIsNotLoggedInException;
import org.solovyev.android.messenger.users.MessengerContactsActivity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.TextViewBuilder;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:41 PM
 */
public class MessengerCommonActivityImpl implements MessengerCommonActivity {

    @NotNull
    private User user;

    private int layoutId;

    @Nullable
    private View.OnClickListener syncButtonListener;

    private boolean createFooterButtons = true;

    public MessengerCommonActivityImpl(int layoutId, @Nullable View.OnClickListener syncButtonListener) {
        this.layoutId = layoutId;
        this.syncButtonListener = syncButtonListener;
    }

    public MessengerCommonActivityImpl(int layoutId, @Nullable View.OnClickListener syncButtonListener, boolean createFooterButtons) {
        this.layoutId = layoutId;
        this.syncButtonListener = syncButtonListener;
        this.createFooterButtons = createFooterButtons;
    }

    @Override
    public void onCreate(@NotNull final Activity activity) {

        checkUserLoggedIn(activity);

        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

        activity.setContentView(layoutId);

        if (createFooterButtons) {
            final MsgImageButton messagesButton;
            if (activity instanceof MessengerChatsActivity || activity instanceof MessengerMessagesActivity) {
                messagesButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_messages_icon_active, R.string.c_messages, activity), true);
            } else {
                messagesButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_messages_icon, R.string.c_messages, activity), false);
            }
            addCenterButton(activity, messagesButton);
            messagesButton.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessengerChatsActivity.startActivity(activity);
                }
            });

            final MsgImageButton contactsButton;
            if (activity instanceof MessengerContactsActivity) {
                contactsButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_contacts_icon_active, R.string.c_contacts, activity), true);
            } else {
                contactsButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_contacts_icon, R.string.c_contacts, activity), false);
            }
            addCenterButton(activity, contactsButton);
            contactsButton.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessengerContactsActivity.startActivity(activity);
                }
            });

            final MsgImageButton searchButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_search_icon, R.string.c_search, activity), false);
            addCenterButton(activity, searchButton);
            searchButton.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "Not implemented yet!", Toast.LENGTH_SHORT).show();
                }
            });

            final MsgImageButton settingsButton = new MsgImageButton(createFooterImageButton(R.drawable.msg_footer_settings_icon, R.string.c_settings, activity), false);
            addCenterButton(activity, settingsButton);
            settingsButton.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "Not implemented yet!", Toast.LENGTH_SHORT).show();
                }
            });
        }

/*        final ImageButton logoutButton = createFooterButton(org.solovyev.android.ext.R.drawable.home, org.solovyev.android.ext.R.string.c_home, activity);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getServiceLocator().getAuthServiceFacade().logoutUser(activity);
                MessengerLoginActivity.startActivity(activity);
            }
        });
        getFooterLeft(activity).addView(logoutButton);

        if (syncButtonListener != null) {
            final ImageButton syncButton = createFooterButton(R.drawable.refresh, R.string.c_refresh, activity);
            syncButton.setOnClickListener(syncButtonListener);
            getFooterRight(activity).addView(syncButton);
        }*/

    }

    private static final class MsgImageButton {
        @NotNull
        private final ImageButton button;

        private final boolean selected;

        private MsgImageButton(@NotNull ImageButton button, boolean selected) {
            this.button = button;
            this.selected = selected;
        }
    }

    private void addCenterButton(@NotNull Activity activity, @NotNull MsgImageButton msgImageButton) {
        addFooterButton(activity, msgImageButton, 10, 35, FooterPosition.center);
    }

    private void addLeftButton(@NotNull Activity activity, @NotNull MsgImageButton msgImageButton) {
        addFooterButton(activity, msgImageButton, 10, 35, FooterPosition.left);
    }

    private void addRightButton(@NotNull Activity activity, @NotNull MsgImageButton msgImageButton) {
        addFooterButton(activity, msgImageButton, 10, 35, FooterPosition.right);
    }

    private static enum FooterPosition {
        left,
        center,
        right;
    }

    private void addFooterButton(@NotNull Activity activity, @NotNull MsgImageButton msgImageButton, int padding, int imageSize, @NotNull FooterPosition position) {
        final LinearLayout imageWithLabel = new LinearLayout(activity);
        imageWithLabel.setOrientation(LinearLayout.VERTICAL);
        imageWithLabel.setGravity(Gravity.CENTER);
        imageWithLabel.setPadding(padding, 0, padding, 0);

        int size = AndroidUtils.toPixels(activity.getResources().getDisplayMetrics(), imageSize);
        msgImageButton.button.setScaleType(ImageView.ScaleType.FIT_XY);
        imageWithLabel.addView(msgImageButton.button, new ViewGroup.LayoutParams(size, size));

        final TextView label = TextViewBuilder.newInstance(R.layout.msg_footer_image_label, null).build(activity);
        label.setText(msgImageButton.button.getContentDescription());
        if (msgImageButton.selected) {
            label.setTextColor(activity.getResources().getColor(R.color.image_button_selected));
        } else {
            label.setTextColor(activity.getResources().getColor(R.color.image_button_unselected));
        }
        imageWithLabel.addView(label, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        if (position == FooterPosition.center) {
            getFooterCenter(activity).addView(imageWithLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else if (position == FooterPosition.left) {
            getFooterLeft(activity).addView(imageWithLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            getFooterRight(activity).addView(imageWithLabel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void checkUserLoggedIn(@NotNull Activity activity) {
        try {
            this.user = getServiceLocator().getAuthServiceFacade().getUser(activity);
        } catch (UserIsNotLoggedInException e) {
            MessengerLoginActivity.startActivity(activity);
        }
    }

    @Override
    public void onRestart(@NotNull Activity activity) {
        checkUserLoggedIn(activity);
    }

    @Override
    @NotNull
    public User getUser() {
        return user;
    }

    @Override
    @NotNull
    public ServiceLocator getServiceLocator() {
        return MessengerConfigurationImpl.getInstance().getServiceLocator();
    }

    @Override
    @NotNull
    public ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId, @NotNull Activity activity) {
        final ImageButton result = FooterImageButtonBuilder.newInstance(imageResId, contentDescriptionResId).build(activity);
        result.setScaleType(ImageView.ScaleType.FIT_XY);
        return result;
    }

    @NotNull
    @Override
    public Button createFooterButton(int captionResId, @NotNull Activity activity) {
        return FooterButtonBuilder.newInstance(captionResId).build(activity);
    }

    @Override
    @NotNull
    public ViewGroup getFooterLeft(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(org.solovyev.android.ext.R.id.footer_left);
    }

    @Override
    @NotNull
    public ViewGroup getFooterCenter(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(org.solovyev.android.ext.R.id.footer_center);
    }

    @Override
    @NotNull
    public ViewGroup getFooterRight(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(org.solovyev.android.ext.R.id.footer_right);
    }

    @NotNull
    @Override
    public ViewGroup getHeaderLeft(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_left);
    }

    @NotNull
    @Override
    public ViewGroup getHeaderCenter(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_center);
    }

    @NotNull
    @Override
    public ViewGroup getHeaderRight(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_right);
    }

    @NotNull
    @Override
    public ViewGroup getCenter(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.center);
    }

    @NotNull
    @Override
    public ViewPager initTitleForViewPager(@NotNull Activity activity,
                                           @NotNull ViewPager.OnPageChangeListener listener,
                                           @NotNull PagerAdapter adapter) {
        final ViewPager pager = (ViewPager) activity.findViewById(R.id.viewpager);
        pager.setAdapter(adapter);

        final TitlePageIndicator titleIndicator = (TitlePageIndicator) activity.findViewById(R.id.viewpager_title);
        titleIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.None);
        titleIndicator.setSelectedColor(R.color.text);
        titleIndicator.setTextColor(R.color.text);
        titleIndicator.setFooterColor(R.color.text);
        titleIndicator.setViewPager(pager);
        titleIndicator.setOnPageChangeListener(listener);
        if (adapter.getCount() <= 1) {
            titleIndicator.setVisibility(View.GONE);
        }

        return pager;
    }

    @Override
    public void handleException(@NotNull Activity activity, @NotNull Exception e) {
        handleExceptionStatic(activity, e);
    }

    public static void handleExceptionStatic(@NotNull Context context, @NotNull Exception e) {
        if (e instanceof RuntimeIoException) {
            if (AndroidUtils2.isUiThread()) {
                Toast.makeText(context, "No internet connection available: connect to the network and try again!", Toast.LENGTH_LONG).show();
            }
            Log.d("Msg_NoInternet", e.getMessage(), e);
        } else if (e instanceof IllegalJsonRuntimeException) {
            if (AndroidUtils2.isUiThread()) {
                Toast.makeText(context, "The response from server is not valid!", Toast.LENGTH_LONG).show();
            }
            Log.e("Msg_InvalidJson", e.getMessage(), e);
        } else {
            if (AndroidUtils2.isUiThread()) {
                Toast.makeText(context, "Something is going wrong!", Toast.LENGTH_LONG).show();
            }
            Log.e("Msg_Exception", e.getMessage(), e);
        }
    }
}
