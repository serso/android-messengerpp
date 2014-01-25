/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import com.google.inject.Singleton;
import org.solovyev.android.TimeLoggingExecutor;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Singleton
public final class Background implements Executor {

	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int POOL_SIZE = CPU_COUNT + 1;

	private final AtomicInteger count = new AtomicInteger();

	@Nonnull
	private Executor lowPriorityExecutor = newFixedThreadPool(POOL_SIZE, new BackgroundThreadFactory(Thread.MIN_PRIORITY));

	@Nonnull
	private Executor highPriorityExecutor = newFixedThreadPool(POOL_SIZE, new BackgroundThreadFactory(Thread.MAX_PRIORITY));

	@Override
	public void execute(@Nonnull Runnable command) {
		TimeLoggingExecutor.executeOnExecutor(lowPriorityExecutor, command);
	}

	@Nonnull
	public Executor getLowPriorityExecutor() {
		return lowPriorityExecutor;
	}

	@Nonnull
	public Executor getHighPriorityExecutor() {
		return highPriorityExecutor;
	}

	public void setLowPriorityExecutor(@Nonnull Executor lowPriorityExecutor) {
		this.lowPriorityExecutor = lowPriorityExecutor;
	}

	public void setHighPriorityExecutor(@Nonnull Executor highPriorityExecutor) {
		this.highPriorityExecutor = highPriorityExecutor;
	}

	private class BackgroundThreadFactory implements ThreadFactory {

		private final int priority;

		public BackgroundThreadFactory(int priority) {
			this.priority = priority;
		}

		@Override
		public Thread newThread(@Nonnull Runnable r) {
			final Thread thread = new Thread(r, "Background thread #" + count.getAndIncrement() + " (priority=" + priority + ")");
			thread.setPriority(priority);
			return thread;
		}
	}
}
