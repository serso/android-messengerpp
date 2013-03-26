package org.solovyev.android.messenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class MultiPaneFragmentDef {

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    @Nonnull
    private final String tag;

    private final boolean addToBackStack;

    @Nullable
    private /*final*/ JPredicate<Fragment> reuseCondition;

    @Nonnull
    private /*final*/ Builder<Fragment> builder;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    private MultiPaneFragmentDef(@Nonnull String tag, boolean addToBackStack) {
        this.tag = tag;
        this.addToBackStack = addToBackStack;
    }

    @Nonnull
    public static MultiPaneFragmentDef forClass(@Nonnull String tag, boolean addToBackStack, @Nonnull Class<? extends Fragment> fragmentClass, @Nonnull Context context, @Nullable Bundle args) {
        return newInstance(tag, addToBackStack, ReflectionFragmentBuilder.forClass(context, fragmentClass, args), SimpleFragmentReuseCondition.forClass(fragmentClass));
    }

    @Nonnull
    public static MultiPaneFragmentDef newInstance(@Nonnull String tag, boolean addToBackStack, @Nonnull Builder<Fragment> builder, @Nullable JPredicate<Fragment> reuseCondition) {
        final MultiPaneFragmentDef result = new MultiPaneFragmentDef(tag, addToBackStack);
        result.builder = builder;
        result.reuseCondition = reuseCondition;
        return result;
    }

    @Nonnull
    public static MultiPaneFragmentDef fromFragmentDef(@Nonnull FragmentDef fragmentDef, @Nullable Bundle fragmentArgs, @Nonnull Context context) {
        final MultiPaneFragmentDef result = new MultiPaneFragmentDef(fragmentDef.getFragmentTag(), fragmentDef.isAddToBackStack());

        final Class<? extends Fragment> fragmentClass = fragmentDef.getFragmentClass();
        result.builder = ReflectionFragmentBuilder.forClass(context, fragmentClass, fragmentArgs);
        result.reuseCondition = SimpleFragmentReuseCondition.forClass(fragmentClass);

        return result;
    }

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

    public boolean isAddToBackStack() {
        return addToBackStack;
    }

    @Nonnull
    public String getTag() {
        return tag;
    }

    @Nonnull
    public Fragment build() {
        return builder.build();
    }

    public boolean canReuse(@Nonnull Fragment fragment) {
        return reuseCondition != null && reuseCondition.apply(fragment);
    }
}
