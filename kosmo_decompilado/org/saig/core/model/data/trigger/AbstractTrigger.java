/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 */
package org.saig.core.model.data.trigger;

import org.apache.log4j.Logger;
import org.saig.core.model.data.trigger.ITrigger;
import org.saig.jump.lang.I18N;

public abstract class AbstractTrigger
implements ITrigger {
    protected static final Logger LOGGER = Logger.getLogger(AbstractTrigger.class);
    public static final String DEFAULT_TRIGGER_NAME = I18N.getString("org.saig.core.model.data.trigger.AbstractTrigger.without-name");
    public static final String DEFAULT_TRIGGER_DESCRIPTION = I18N.getString("org.saig.core.model.data.trigger.AbstractTrigger.without-description");
    protected String name = DEFAULT_TRIGGER_NAME;
    protected String description = DEFAULT_TRIGGER_DESCRIPTION;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }
}

