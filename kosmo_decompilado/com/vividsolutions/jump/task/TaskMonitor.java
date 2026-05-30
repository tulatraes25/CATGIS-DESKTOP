/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.task;

public interface TaskMonitor {
    public void report(String var1);

    public void report(int var1, int var2, String var3);

    public void report(Exception var1);

    public void allowCancellationRequests();

    public boolean isCancelRequested();
}

