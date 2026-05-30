/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.util;

public interface IForm {
    public boolean isInputValid();

    public void update() throws Exception;

    public void rollback();

    public void disable();

    public void commit() throws Exception;
}

