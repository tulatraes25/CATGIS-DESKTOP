/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Coordinate
 *  com.vividsolutions.jts.geom.Envelope
 */
package org.saig.core.dao.datasource;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.util.Collection;
import java.util.List;
import org.saig.core.model.globes.TextBalloon;

public abstract class AbstractTextBalloonDataSource {
    protected boolean saved = false;
    protected Envelope fullEnvelope = new Envelope();

    public boolean commit() {
        this.saved = true;
        return false;
    }

    public void addTextBalloon(TextBalloon balloon) {
        this.saved = false;
        this.expandFullEnvelope(balloon);
    }

    public Envelope getFullEnvelope() {
        return this.fullEnvelope;
    }

    public void expandFullEnvelope(TextBalloon balloon) {
        this.fullEnvelope.expandToInclude(balloon.getBalloonEnd());
        this.fullEnvelope.expandToInclude(balloon.getBalloonTextZone());
    }

    public void addTextBalloons(Collection<TextBalloon> balloons) {
        this.saved = false;
        for (TextBalloon tb : balloons) {
            this.expandFullEnvelope(tb);
        }
    }

    public abstract List<TextBalloon> getTextBalloons();

    public boolean isSaved() {
        return this.saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public abstract TextBalloon query(Coordinate var1);

    public abstract List<TextBalloon> query(Envelope var1);
}

