/*
 * Decompiled with CFR 0.152.
 */
package org.saig.core.renderer3;

import java.util.concurrent.CountDownLatch;
import org.saig.core.renderer3.IRenderer;
import org.saig.core.renderer3.RenderParams;

class LayerRendererWorker
implements Runnable {
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;
    private final IRenderer renderer;
    private final RenderParams renderParams;

    LayerRendererWorker(CountDownLatch startSignal, CountDownLatch doneSignal, IRenderer renderer, RenderParams renderParams) {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
        this.renderer = renderer;
        this.renderParams = renderParams;
    }

    @Override
    public void run() {
        try {
            this.startSignal.await();
            this.doWork();
            this.doneSignal.countDown();
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    void doWork() {
        this.renderer.render(this.renderParams.getImage(), this.renderParams.getEnvelope(), this.renderParams.getLayer(), this.renderParams.getAngle(), this.renderParams.getPanelScale(), this.renderParams.isStrategy(), this.renderParams.getUnits(), this.renderParams.getRenderingHints());
    }
}

