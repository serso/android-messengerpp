package org.solovyev.android.tasks;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFutureTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 9:31 PM
 */
public class AsyncTaskServiceImpl implements AsyncTaskService {

    @GuardedBy("tasks")
    @Nonnull
    private final Map<String, ListenableFutureTask> tasks = new HashMap<String, ListenableFutureTask>();

    @Nonnull
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public <T> void tryRun(@Nonnull String taskName, @Nonnull Callable<T> callable) throws TaskIsAlreadyRunningException {
        tryRun(taskName, callable, null);
    }

    @Override
    public boolean isRunning(@Nonnull String taskName) {
        synchronized (tasks) {
            final ListenableFutureTask<?> task = tasks.get(taskName);
            if (task != null && !task.isDone()) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public <T> void addListener(@Nonnull String taskName, @Nonnull FutureCallback<T> listener) throws NoSuchTaskException, TaskFinishedException {
        synchronized (tasks) {
            final ListenableFutureTask<T> task = tasks.get(taskName);
            if (task == null) {
                throw new NoSuchTaskException();
            } else {
                if (task.isDone()) {
                    throw new TaskFinishedException();
                } else {
                    Futures.addCallback(task, listener);
                }
            }
        }
    }


    @Override
    public <T> void tryRun(@Nonnull String taskName, @Nonnull Callable<T> callable, @Nullable final FutureCallback<T> listener) throws TaskIsAlreadyRunningException {
        final ListenableFutureTask<T> task = ListenableFutureTask.create(callable);
        synchronized (tasks) {
            final ListenableFutureTask<T> oldTask = tasks.get(taskName);
            if (oldTask == null || oldTask.isDone()) {
                tasks.put(taskName, task);
                if (listener != null) {
                    Futures.addCallback(task, listener);
                }
                executor.execute(task);
            } else {
                throw new TaskIsAlreadyRunningException();
            }
        }
    }

    @Override
    public <T> void run(@Nonnull String taskName, @Nonnull Callable<T> callable, @Nullable FutureCallback<T> listener) {
        final ListenableFutureTask<T> task = ListenableFutureTask.create(callable);
        synchronized (tasks) {
            final ListenableFutureTask<T> oldTask = tasks.get(taskName);
            if ( oldTask != null && !oldTask.isDone() ) {
                oldTask.cancel(false);
            }
            tasks.put(taskName, task);
            if (listener != null) {
                Futures.addCallback(task, listener);
            }
            executor.execute(task);
        }
    }
}
