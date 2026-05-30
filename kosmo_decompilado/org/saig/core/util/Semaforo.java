/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

public class Semaforo {
    private boolean libre = true;

    public Semaforo() {
    }

    public Semaforo(boolean estado) {
        this.libre = estado;
    }

    public synchronized void block() {
        while (!this.libre) {
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.libre = false;
    }

    public synchronized void unblock() {
        this.libre = true;
        this.notifyAll();
    }

    public synchronized boolean tryblock() {
        if (this.libre) {
            this.libre = false;
            return true;
        }
        return false;
    }
}

