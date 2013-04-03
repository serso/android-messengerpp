package org.solovyev.android.tasks;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 9:26 PM
 */
public interface AsyncTaskService {

    <T> void tryRun(@Nonnull String taskName, @Nonnull Callable<T> callable) throws TaskIsAlreadyRunningException;

    <T> void tryRun(@Nonnull String taskName, @Nonnull Callable<T> callable, @Nullable FutureCallback<T> listener) throws TaskIsAlreadyRunningException;

    <T> void run(@Nonnull String taskName, @Nonnull Callable<T> callable, @Nullable FutureCallback<T> listener);

    boolean isRunning(@Nonnull String taskName);

    <T> void addListener(@Nonnull String taskName, @Nonnull FutureCallback<T> listener) throws NoSuchTaskException, TaskFinishedException;
}
