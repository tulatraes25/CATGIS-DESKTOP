/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.workbench.ui.plugin;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;
import org.saig.jump.util.LayerUtil;

public class MoveLayerablePlugIn
extends AbstractPlugIn {
    public static final int TO_FIRST = -9999;
    public static final int TO_LAST = 9999;
    public static final MoveLayerablePlugIn UP = new MoveLayerablePlugIn(-1){
        public final String NAME = I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.move-layer-up");
        public final Icon ICON = IconLoader.icon("Up3.gif");

        @Override
        public String getName() {
            return this.NAME;
        }

        @Override
        public Icon getIcon() {
            return this.ICON;
        }

        @Override
        public EnableCheck getCheck() {
            return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
        }

        @Override
        public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
            MultiEnableCheck solucion = new MultiEnableCheck();
            EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
            solucion.add(super.createEnableCheck(workbenchContext).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedLayerable(workbenchContext.getLayerNamePanel())) == 0 ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layer-is-already-at-the-top") : null;
                }
            }));
            solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
            return solucion;
        }
    };
    public static final MoveLayerablePlugIn DOWN = new MoveLayerablePlugIn(1){
        public final String NAME = I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.move-layer-down");
        public final Icon ICON = IconLoader.icon("Down3.gif");

        @Override
        public String getName() {
            return this.NAME;
        }

        @Override
        public Icon getIcon() {
            return this.ICON;
        }

        @Override
        public EnableCheck getCheck() {
            return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
        }

        @Override
        public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
            MultiEnableCheck solucion = new MultiEnableCheck();
            EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
            solucion.add(super.createEnableCheck(workbenchContext).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedLayerable(workbenchContext.getLayerNamePanel())) == workbenchContext.getLayerViewPanel().getLayerManager().getCategory(this.selectedLayerable(workbenchContext.getLayerNamePanel())).getLayerables().size() - 1 ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layer-is-already-at-the-bottom") : null;
                }
            }));
            solucion.add(checkFactory.createSelectedLayerMustBeActiveCheck());
            return solucion;
        }
    };
    public static final MoveLayerablePlugIn FIRST = new MoveLayerablePlugIn(-9999){
        public final String NAME = I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.move-layers-to-first");
        public final Icon ICON = IconLoader.icon("VCRUp.gif");

        @Override
        public String getName() {
            return this.NAME;
        }

        @Override
        public Icon getIcon() {
            return this.ICON;
        }

        @Override
        public EnableCheck getCheck() {
            return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
        }

        @Override
        public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
            EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
            return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayerablesMustBeSelectedCheck(1, Layerable.class)).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedLayerable(workbenchContext.getLayerNamePanel())) == 0 ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layer-is-already-at-the-top") : null;
                }
            }).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return !LayerUtil.sameCategory(this.selectedLayerables(workbenchContext.getLayerNamePanel())) ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layers-from-different-categories") : null;
                }
            });
        }
    };
    public static final MoveLayerablePlugIn LAST = new MoveLayerablePlugIn(9999){
        public final String NAME = I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.move-layers-to-last");
        public final Icon ICON = IconLoader.icon("VCRDown.gif");

        @Override
        public String getName() {
            return this.NAME;
        }

        @Override
        public Icon getIcon() {
            return this.ICON;
        }

        @Override
        public EnableCheck getCheck() {
            return this.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
        }

        @Override
        public MultiEnableCheck createEnableCheck(final WorkbenchContext workbenchContext) {
            EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
            return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createAtLeastNLayerablesMustBeSelectedCheck(1, Layerable.class)).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedLayerable(workbenchContext.getLayerNamePanel())) == workbenchContext.getLayerViewPanel().getLayerManager().getCategory(this.selectedLayerable(workbenchContext.getLayerNamePanel())).getLayerables().size() - 1 ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layer-is-already-at-the-bottom") : null;
                }
            }).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return !LayerUtil.sameCategory(this.selectedLayerables(workbenchContext.getLayerNamePanel())) ? I18N.getString("workbench.ui.plugin.MoveLayerablePlugIn.layers-from-different-categories") : null;
                }
            });
        }
    };
    private int displacement;

    private MoveLayerablePlugIn(int displacement) {
        this.displacement = displacement;
    }

    protected Layerable selectedLayerable(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Layerable.class).iterator().next();
    }

    protected Collection selectedLayerables(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Layerable.class);
    }

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        List<Layerable> layerables = LayerUtil.orderByIndex(this.selectedLayerables(context.getLayerNamePanel()));
        if (layerables.size() == 1) {
            final Layerable layerable = layerables.iterator().next();
            final int index = this.index(layerable);
            final Category category = context.getLayerManager().getCategory(layerable);
            this.execute(new UndoableCommand(this.getName()){

                @Override
                public void execute() {
                    switch (MoveLayerablePlugIn.this.displacement) {
                        case -9999: {
                            this.moveLayerable(0);
                            break;
                        }
                        case 9999: {
                            this.moveLayerable(category.getLayerables().size() - 1);
                            break;
                        }
                        default: {
                            this.moveLayerable(index + MoveLayerablePlugIn.this.displacement);
                        }
                    }
                }

                @Override
                public void unexecute() {
                    this.moveLayerable(index);
                }

                private void moveLayerable(int newIndex) {
                    context.getLayerManager().remove(layerable);
                    context.getLayerManager().addLayerable(category.getName(), layerable, newIndex);
                }
            }, context);
            return true;
        }
        final Category category = context.getLayerManager().getCategory(layerables.iterator().next());
        final HashMap<Integer, Layerable> oldIndexes = new HashMap<Integer, Layerable>();
        final HashMap<Integer, Layerable> newIndexes = new HashMap<Integer, Layerable>();
        int indexValue = 0;
        switch (this.displacement) {
            case -9999: {
                break;
            }
            case 9999: {
                indexValue = category.getLayerables().size() - layerables.size();
                break;
            }
            default: {
                Assert.shouldNeverReachHere();
            }
        }
        for (Layerable element : layerables) {
            oldIndexes.put(new Integer(this.index(element)), element);
            newIndexes.put(new Integer(indexValue++), element);
        }
        this.execute(new UndoableCommand(this.getName()){

            @Override
            public void execute() {
                ArrayList keys = new ArrayList(newIndexes.keySet());
                if (MoveLayerablePlugIn.this.displacement == -9999) {
                    Collections.sort(keys);
                } else {
                    Collections.sort(keys, Collections.reverseOrder());
                }
                for (Integer index : keys) {
                    Layerable element = (Layerable)newIndexes.get(index);
                    this.moveLayerable(index, element);
                }
            }

            @Override
            public void unexecute() {
                ArrayList keys = new ArrayList(oldIndexes.keySet());
                if (MoveLayerablePlugIn.this.displacement == -9999) {
                    Collections.sort(keys, Collections.reverseOrder());
                } else {
                    Collections.sort(keys);
                }
                for (Integer index : keys) {
                    Layerable element = (Layerable)oldIndexes.get(index);
                    this.moveLayerable(index, element);
                }
            }

            private void moveLayerable(int newIndex, Layerable layerable) {
                context.getLayerManager().remove(layerable);
                context.getLayerManager().addLayerable(category.getName(), layerable, newIndex);
            }
        }, context);
        return true;
    }

    protected int index(Layerable layerable) {
        return layerable.getLayerManager().getCategory(layerable).indexOf(layerable);
    }

    public MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNLayerablesMustBeSelectedCheck(1, Layerable.class));
    }

    /* synthetic */ MoveLayerablePlugIn(int n, MoveLayerablePlugIn moveLayerablePlugIn) {
        this(n);
    }
}

