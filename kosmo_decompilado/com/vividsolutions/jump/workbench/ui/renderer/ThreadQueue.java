/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.ui.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ThreadQueue {
    private volatile int runningThreads = 0;
    private Vector<Runnable> queuedRunnables = new Vector();
    private int maxRunningThreads;
    private List<Listener> listeners = new ArrayList<Listener>();
    private volatile boolean enabled = true;

    public ThreadQueue(int maxRunningThreads) {
        this.maxRunningThreads = maxRunningThreads;
    }

    public void clear() {
        this.queuedRunnables.clear();
    }

    private void processQueue() {
        while (!this.queuedRunnables.isEmpty() && this.runningThreads < this.maxRunningThreads && this.enabled) {
            this.setRunningThreads(this.getRunningThreads() + 1);
            try {
                Thread thread = new Thread(this.queuedRunnables.remove(0));
                thread.setPriority(1);
                thread.start();
            }
            catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                // empty catch block
            }
        }
    }

    private void setRunningThreads(int runningThreads) {
        this.runningThreads = runningThreads;
        if (runningThreads == 0) {
            this.fireAllRunningThreadsFinished();
        }
    }

    public void add(final Runnable runnable) {
        this.queuedRunnables.add(new Runnable(){

            @Override
            public void run() {
                try {
                    runnable.run();
                }
                finally {
                    ThreadQueue.this.setRunningThreads(ThreadQueue.this.getRunningThreads() - 1);
                    ThreadQueue.this.processQueue();
                }
            }
        });
        this.processQueue();
    }

    public int getRunningThreads() {
        return this.runningThreads;
    }

    public int getNumThreads() {
        return this.queuedRunnables.size();
    }

    public void add(Listener listener) {
        this.listeners.add(listener);
    }

    public void remove(Listener listener) {
        this.listeners.remove(listener);
    }

    private void fireAllRunningThreadsFinished() {
        for (Listener listener : new ArrayList<Listener>(this.listeners)) {
            listener.allRunningThreadsFinished();
        }
    }

    public void dispose() {
        this.enabled = false;
    }

    public static interface Listener {
        public void allRunningThreadsFinished();
    }
}

