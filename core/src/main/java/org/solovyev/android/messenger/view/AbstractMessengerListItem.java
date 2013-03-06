package org.solovyev.android.messenger.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 11:57 PM
 */
public abstract class AbstractMessengerListItem<D> implements ListItem, Checkable {

    private boolean checked;

    @Nonnull
    private final String tagPrefix;

    private final int layoutResId;

    @Nonnull
    private ViewAwareTag viewAwareTag;

    @Nonnull
    private D data;

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
        return new ViewAwareTag(tagPrefix + getDataId(this.data), view);
    }

    @Nonnull
    protected abstract String getDataId(@Nonnull D data);

    private void fillView(@Nonnull Context context, @Nonnull final ViewGroup view) {
        final ViewAwareTag tag = createTag(view);

        // todo serso: view.setSelected() doesn't work
        toggleSelected(view, isChecked());

        ViewAwareTag viewTag = (ViewAwareTag) view.getTag();
        if (!tag.equals(viewTag)) {
            if (viewTag != null) {
                viewTag.update(tag);
            } else {
                viewTag = tag;
                view.setTag(viewTag);
            }
            viewAwareTag = viewTag;

            fillView(this.data, context, viewTag);
        }
    }

    protected abstract void fillView(@Nonnull D data, @Nonnull Context context, @Nonnull ViewAwareTag viewTag);

    public static void toggleSelected(@Nonnull ViewGroup view, boolean checked) {
        if (checked) {
            view.setBackgroundResource(R.drawable.item_states_selected);
        } else {
            view.setBackgroundResource(R.drawable.item_states);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        this.checked = !checked;
    }

    @Nonnull
    public ViewAwareTag getViewAwareTag() {
        return viewAwareTag;
    }

    @Nonnull
    protected D getData() {
        return data;
    }

    protected void setData(@Nonnull D data) {
        this.data = data;
        fillView(data, viewAwareTag.getView().getContext(), viewAwareTag);
    }

    @Override
    public boolean equals(Object o) {
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
    public int hashCode() {
        return data.hashCode();
    }
}
