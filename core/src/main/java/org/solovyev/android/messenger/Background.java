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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public final class Background implements Executor {

	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

	@Nonnull
	private final Executor executor = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE, new BackgroundThreadFactory());

	@Override
	public void execute(@Nonnull Runnable command) {
		TimeLoggingExecutor.executeOnExecutor(executor, command);
	}

	@Nonnull
	public Executor getExecutor() {
		return executor;
	}

	private static class BackgroundThreadFactory implements ThreadFactory {

		private final AtomicInteger count = new AtomicInteger();

		@Override
		public Thread newThread(@Nonnull Runnable r) {
			return new Thread(r, "Background thread #" + count.getAndIncrement());
		}
	}
}
