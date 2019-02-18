package io.triada.threads;

import lombok.experimental.Delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class NamedThreadExecutor extends ThreadPoolExecutor {

    private final List<String> threadNames;

    @Delegate
    private final ThreadPoolExecutor executor;

    public NamedThreadExecutor(final ThreadPoolExecutor executor) {
        super(
                executor.getCorePoolSize(),
                executor.getMaximumPoolSize(),
                executor.getKeepAliveTime(TimeUnit.NANOSECONDS),
                TimeUnit.NANOSECONDS,
                executor.getQueue()
        );
        this.executor = executor;
        this.threadNames = new ArrayList<>(this.executor.getPoolSize());
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        threadNames.add(t.getName());
    }

    public boolean exists(final String name) {
        return threadNames.contains(name);
    }
}
