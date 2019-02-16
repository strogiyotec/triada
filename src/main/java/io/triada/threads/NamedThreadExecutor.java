package io.triada.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class NamedThreadExecutor extends ThreadPoolExecutor {

    private final List<String> threadNames;

    public NamedThreadExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit unit, final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.threadNames = new ArrayList<>(maximumPoolSize);
    }

    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        threadNames.add(t.getName());
    }

    public boolean exists(final String name) {
        return threadNames.contains(name);
    }
}
