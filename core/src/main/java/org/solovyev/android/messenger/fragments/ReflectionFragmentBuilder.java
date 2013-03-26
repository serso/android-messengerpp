package org.solovyev.android.messenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import org.solovyev.common.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* User: serso
* Date: 3/7/13
* Time: 8:18 PM
*/
public class ReflectionFragmentBuilder<F extends Fragment> implements Builder<F> {

    @Nonnull
    private Context context;

    @Nonnull
    private Class<? extends F> fragmentClass;

    @Nullable
    private Bundle fragmentArgs;

    private ReflectionFragmentBuilder(@Nonnull Context context, @Nonnull Class<? extends F> fragmentClass, @Nullable Bundle fragmentArgs) {
        this.context = context;
        this.fragmentClass = fragmentClass;
        this.fragmentArgs = fragmentArgs;
    }

    @Nonnull
    public static <F extends Fragment> ReflectionFragmentBuilder<F> forClass(@Nonnull Context context, @Nonnull Class<? extends F> fragmentClass, @Nullable Bundle fragmentArgs) {
        return new ReflectionFragmentBuilder<F>(context, fragmentClass, fragmentArgs);
    }

    @Nonnull
    @Override
    public F build() {
        return (F) Fragment.instantiate(context, fragmentClass.getName(), fragmentArgs);
    }
}
