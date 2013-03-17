package org.solovyev.android.messenger.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerEntity;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 11:57 PM
 */
public abstract class AbstractMessengerListItem<D extends MessengerEntity> implements ListItem, Checkable, Comparable<AbstractMessengerListItem<D>> {

    private boolean checked;

    @Nonnull
    private final String tagPrefix;

    private final int layoutResId;

    @Nonnull
    private CharSequence displayName = "";

    @Nonnull
    private D data;

    private boolean dataChanged = true;

    protected AbstractMessengerListItem(@Nonnull String tagPrefix, int layoutResId, @Nonnull D data) {
        this.tagPrefix = tagPrefix;
        this.layoutResId = layoutResId;
        this.data = data;
    }

    @Nonnull
    @Override
    public final View updateView(@Nonnull Context context, @Nonnull View view) {
        final Object tag = view.getTag();
        if ( tag instanceof ViewAwareTag && ((ViewAwareTag) tag).getTag().startsWith(tagPrefix)) {
            fillView(context, (ViewGroup) view);
            return view;
        } else {
            return build(context);
        }
    }


    @Nonnull
    @Override
    public final View build(@Nonnull Context context) {
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(layoutResId).build(context);
        fillView(context, view);
        return view;
    }

    @Nonnull
    private ViewAwareTag createTag(@Nonnull ViewGroup view) {
        return new ViewAwareTag(tagPrefix + this.data.getId(), view);
    }

    private void fillView(@Nonnull Context context, @Nonnull final ViewGroup view) {
        final ViewAwareTag tag = createTag(view);

        view.setActivated(checked);

        ViewAwareTag viewTag = (ViewAwareTag) view.getTag();
        if (!tag.equals(viewTag) || dataChanged) {
            if (viewTag != null) {
                viewTag.update(tag);
            } else {
                viewTag = tag;
                view.setTag(viewTag);
            }
            displayName = getDisplayName(this.data, context);
            fillView(this.data, context, viewTag);
            dataChanged = false;
        }
    }

    @Nonnull
    protected abstract CharSequence getDisplayName(@Nonnull D data, @Nonnull Context context);

    protected abstract void fillView(@Nonnull D data, @Nonnull Context context, @Nonnull ViewAwareTag viewTag);

    @Override
    public final void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public final boolean isChecked() {
        return checked;
    }

    @Override
    public final void toggle() {
        this.checked = !checked;
    }

    @Nonnull
    protected final D getData() {
        return data;
    }

    protected final void setData(@Nonnull D data) {
        this.data = data;
        // view update will be done by adapter if necessary
        onDataChanged();
    }

    /**
     * Should be called when inner state of data class is changed (for example, properties)
     * Method is called inside {@link AbstractMessengerListItem#setData(D)} => no need to call this method after setting new data
     */
    public void onDataChanged() {
        dataChanged = true;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractMessengerListItem)) {
            return false;
        }

        final AbstractMessengerListItem that = (AbstractMessengerListItem) o;

        if (!data.equals(that.data)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        return data.hashCode();
    }

    @Override
    public final String toString() {
        // NOTE: this code is used inside the ArrayAdapter for filtering
        return this.displayName.toString();
    }

    @Nonnull
    protected final CharSequence getDisplayName() {
        return displayName;
    }

    @Override
    public final int compareTo(@Nonnull AbstractMessengerListItem<D> another) {
        return this.toString().compareTo(another.toString());
    }
}
