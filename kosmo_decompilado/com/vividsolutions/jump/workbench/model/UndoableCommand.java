/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.model;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public abstract class UndoableCommand {
    private String name;
    public static final UndoableCommand DUMMY = new UndoableCommand("Dummy"){

        @Override
        public void execute() throws Exception {
        }

        @Override
        public void unexecute() throws Exception {
        }
    };

    public UndoableCommand(String name) {
        this.name = name;
    }

    protected void dispose() {
    }

    public abstract void execute() throws Exception;

    public abstract void unexecute() throws Exception;

    public UndoableEdit toUndoableEdit() {
        return new AbstractUndoableEdit(){
            private static final long serialVersionUID = 1L;

            @Override
            public String getPresentationName() {
                return UndoableCommand.this.name;
            }

            @Override
            public void redo() throws CannotRedoException {
                try {
                    UndoableCommand.this.execute();
                }
                catch (Exception e) {
                    CannotRedoException cre = new CannotRedoException();
                    cre.initCause(e);
                    throw cre;
                }
                super.redo();
            }

            @Override
            public void die() {
                UndoableCommand.this.dispose();
                super.die();
            }

            @Override
            public void undo() throws CannotUndoException {
                super.undo();
                try {
                    UndoableCommand.this.unexecute();
                }
                catch (Exception e) {
                    CannotUndoException cue = new CannotUndoException();
                    cue.initCause(e);
                    throw cue;
                }
            }
        };
    }

    public String getName() {
        return this.name;
    }
}

