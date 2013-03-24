package org.solovyev.android.messenger.preferences;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.v4.app.ListFragment;
import android.view.*;
import android.widget.ListView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/17/13
 * Time: 6:43 PM
 */
public class PreferenceListFragment extends ListFragment {

    private static final int NO_THEME = -1;

    public static final String FRAGMENT_TAG = "preferences";
    public static final String PREFERENCES_RES_ID = "preferences_res_id";
    public static final String LAYOUT_RES_ID = "layout_res_id";

    private PreferenceManagerCompat preferenceManager;

    private View root;

    private int preferencesResId;

    private int layoutResId;

    private int themeResId = NO_THEME;
    private Context themeContext;

    public PreferenceListFragment(int preferencesResId, int layoutResId) {
        this.preferencesResId = preferencesResId;
        this.layoutResId = layoutResId;
    }

    public PreferenceListFragment(int preferencesResId, int layoutResId, int themeResId) {
        this.preferencesResId = preferencesResId;
        this.layoutResId = layoutResId;
        this.themeResId = themeResId;
    }

    //must be provided
    public PreferenceListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            preferencesResId = savedInstanceState.getInt(PREFERENCES_RES_ID);
            layoutResId = savedInstanceState.getInt(LAYOUT_RES_ID);
        }

        preferenceManager = new PreferenceManagerCompat(this);
    }

    protected void prepareListView(@Nonnull ListView lv) {
        lv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b) {
        final LayoutInflater themeInflater;

        if (themeResId == NO_THEME) {
            themeContext = getActivity();
            themeInflater = inflater;
        } else {
            themeContext = new ContextThemeWrapper(getActivity(), themeResId);
            themeInflater = LayoutInflater.from(themeContext);
        }

        root = themeInflater.inflate(layoutResId, null);

        final ListView lv = (ListView) root.findViewById(R.id.list);
        prepareListView(lv);

        onCreateView(themeContext, themeInflater, root, container, b);

        return root;
    }

    protected void onCreateView(@Nonnull Context context, @Nonnull LayoutInflater inflater, @Nonnull View root, @Nonnull ViewGroup container, @Nonnull Bundle b) {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferenceManager.addPreferencesFromResource(preferencesResId, themeContext);
        preferenceManager.postBindPreferences();

        final Activity activity = getActivity();
        if (activity instanceof OnPreferenceAttachedListener) {
            final PreferenceScreen preferenceScreen = preferenceManager.getPreferenceScreen();
            if (preferenceScreen != null) {
                ((OnPreferenceAttachedListener) activity).onPreferenceAttached(preferenceScreen, preferencesResId);
            }
        }
    }

    @Nullable
    public PreferenceScreen getPreferenceScreen() {
        return preferenceManager.getPreferenceScreen();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PREFERENCES_RES_ID, preferencesResId);
        outState.putInt(LAYOUT_RES_ID, layoutResId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        final ViewParent rootParent = root.getParent();
        if (rootParent != null) {
            ((ViewGroup) rootParent).removeView(root);
        }
    }

    @Override
    public void onPause() {
        preferenceManager.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        preferenceManager.onStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        root = null;
        preferenceManager.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        preferenceManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Adds preferences from activities that match the given {@link Intent}.
     *
     * @param intent The {@link Intent} to query activities.
     */
    public void addPreferencesFromIntent(Intent intent) {
        throw new RuntimeException("too lazy to include this bs");
    }


    /**
     * Finds a {@link Preference} based on its key.
     *
     * @param key The key of the preference to retrieve.
     * @return The {@link Preference} with the key, or null.
     * @see android.preference.PreferenceGroup#findPreference(CharSequence)
     */
    public Preference findPreference(CharSequence key) {
        return preferenceManager.findPreference(key);
    }

    public interface OnPreferenceAttachedListener {
        public void onPreferenceAttached(PreferenceScreen preferenceScreen, int preferenceResId);
    }

    public int getPreferencesResId() {
        return preferencesResId;
    }
}