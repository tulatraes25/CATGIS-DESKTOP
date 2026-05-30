/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.util;

import org.saig.core.util.Temporizador;

public class Timer
implements Runnable {
    private Thread thread = null;
    private int duracion = 0;
    private Temporizador handler = null;

    Timer() {
    }

    Timer(int tiempo) {
        this.setDuracion(tiempo);
    }

    Timer(Temporizador Handler2) {
        this.setHandler(Handler2);
    }

    public Timer(int tiempo, Temporizador Handler2) {
        this.setDuracion(tiempo);
        this.setHandler(Handler2);
    }

    public void setDuracion(int tiempo) {
        this.duracion = tiempo;
    }

    public void setHandler(Temporizador Handler2) {
        this.handler = Handler2;
    }

    public int getDuration() {
        return this.duracion;
    }

    public Temporizador getHandler() {
        return this.handler;
    }

    public void start() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {
        try {
            this.esperar(this.duracion);
        }
        catch (InterruptedException e) {
            return;
        }
        if (this.handler != null) {
            this.handler.timerMuerto(this);
        }
    }

    private synchronized void esperar(int lapso) throws InterruptedException {
        this.wait(lapso);
    }
}

