/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.geom.Envelope
 *  org.cresques.cts.ICoordTrans
 *  org.cresques.cts.IProjection
 */
package com.vividsolutions.jump.workbench.model;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.workbench.model.AbstractLayerable;
import java.util.List;
import java.util.Locale;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.saig.core.dao.datasource.AbstractTextBalloonDataSource;
import org.saig.core.model.globes.TextBalloon;
import org.saig.core.styling.Style;
import org.saig.core.util.I18NUnsupportedOperationException;

public class TextBalloonLayer
extends AbstractLayerable
implements Cloneable {
    private Blackboard blackboard = new Blackboard();
    private AbstractTextBalloonDataSource datasource;
    private String xmlFile;

    @Override
    public Blackboard getBlackboard() {
        return this.blackboard;
    }

    public void addBalloon(TextBalloon balloon) {
        this.datasource.addTextBalloon(balloon);
        this.fireAppearanceChanged();
    }

    public void removeBalloon(TextBalloon balloon) {
        this.datasource.getTextBalloons().remove(balloon);
        this.fireAppearanceChanged();
    }

    public List<TextBalloon> getBalloons() {
        return this.datasource.getTextBalloons();
    }

    @Override
    public Style getModelStyle() {
        return null;
    }

    @Override
    public boolean isRaster() {
        return false;
    }

    public Envelope getFullEnvelope() {
        return this.datasource.getFullEnvelope();
    }

    public AbstractTextBalloonDataSource getDataSource() {
        return this.datasource;
    }

    public void setDataSource(AbstractTextBalloonDataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public ICoordTrans getCoordTrans() {
        return null;
    }

    @Override
    public IProjection getProjection() {
        return null;
    }

    @Override
    public void setCoordTrans(ICoordTrans coordTrans) {
    }

    @Override
    public void setProjection(IProjection projection) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public void addLocale(Locale locale) {
        throw new I18NUnsupportedOperationException();
    }

    @Override
    public void removeLocale(Locale locale) {
        throw new I18NUnsupportedOperationException();
    }
}

