package org.solovyev.android.messenger.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.ListFragment;
import android.widget.ListView;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 8:09 PM
 */
public class PreferenceManagerCompat {

    private static final String TAG = "M++/PreferenceManagerCompat";

    /**
     * The starting request code given out to preference framework.
     */
    private static final int FIRST_REQUEST_CODE = 100;
    private static final int MSG_BIND_PREFERENCES = 0;
    private final Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_BIND_PREFERENCES:
                    bindPreferences();
                    break;
            }
        }
    };

    @Nonnull
    private final PreferenceManager preferenceManager;

    @Nonnull
    private final ListFragment fragment;

    public PreferenceManagerCompat(@Nonnull ListFragment fragment) {
        this.fragment = fragment;
        preferenceManager = newPreferenceManager(fragment.getActivity());
    }

    /**
     * Creates the {@link android.preference.PreferenceManager}.
     *
     * @return The {@link android.preference.PreferenceManager} used by this activity.
     * @param activity activity
     */
    @Nonnull
    private static PreferenceManager newPreferenceManager(@Nonnull Activity activity) {
        try {
            Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
            c.setAccessible(true);
            return c.newInstance(activity, FIRST_REQUEST_CODE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    /**
     * Inflates the given XML resource and adds the preference hierarchy to the current
     * preference hierarchy.
     *
     * @param preferencesResId The XML resource ID to inflate.
     * @param themeContext context holding the theme
     */
    public void addPreferencesFromResource(int preferencesResId, @Nonnull Context themeContext) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class, PreferenceScreen.class);
            m.setAccessible(true);
            final PreferenceScreen preferenceScreen = (PreferenceScreen) m.invoke(preferenceManager, themeContext, preferencesResId, getPreferenceScreen());
            if (preferenceScreen != null) {
                setPreferenceScreen(preferenceScreen);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addPreferencesFromResource(int preferencesResId) {
        addPreferencesFromResource(preferencesResId, fragment.getActivity());
    }


    /**
     * Sets the root of the preference hierarchy that this activity is showing.
     *
     * @param preferenceScreen The root {@link PreferenceScreen} of the preference hierarchy.
     */
    public void setPreferenceScreen(@Nonnull PreferenceScreen preferenceScreen) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            m.setAccessible(true);
            boolean result = (Boolean) m.invoke(preferenceManager, preferenceScreen);
            if (result) {
                postBindPreferences();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the root of the preference hierarchy that this activity is showing.
     *
     * @return The {@link PreferenceScreen} that is the root of the preference
     *         hierarchy.
     */
    public PreferenceScreen getPreferenceScreen() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            m.setAccessible(true);
            return (PreferenceScreen) m.invoke(preferenceManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Posts a message to bind the preferences to the list view.
     * <p/>
     * Binding late is preferred as any custom preference types created in
     * {@link Activity#onCreate(android.os.Bundle)} are able to have their views recycled.
     */
    void postBindPreferences() {
        if (!uiHandler.hasMessages(MSG_BIND_PREFERENCES)) {
            uiHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
        }
    }

    private void bindPreferences() {
        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            final ListView lv = fragment.getListView();
            if (lv != null) {
                preferenceScreen.bind(lv);
            }
        }
    }

    public Preference findPreference(CharSequence key) {
        return preferenceManager.findPreference(key);
    }

    public void onDestroy() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            m.setAccessible(true);
            m.invoke(preferenceManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
            m.setAccessible(true);
            m.invoke(preferenceManager, requestCode, resultCode, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onStop() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            m.setAccessible(true);
            m.invoke(preferenceManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void onPause() {
        uiHandler.removeMessages(MSG_BIND_PREFERENCES);
    }
}
