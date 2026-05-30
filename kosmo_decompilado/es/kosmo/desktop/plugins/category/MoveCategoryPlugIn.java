/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.category;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Category;
import com.vividsolutions.jump.workbench.model.UndoableCommand;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.LayerNamePanel;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.utils.CategoryUtil;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.saig.jump.lang.I18N;

public class MoveCategoryPlugIn
extends AbstractPlugIn {
    public static final int TO_FIRST = -9999;
    public static final int TO_LAST = 9999;
    public static final MoveCategoryPlugIn UP = new MoveCategoryPlugIn(-1){
        public final String NAME = I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.Move-up");
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
            solucion.add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1));
            solucion.add(super.createEnableCheck(workbenchContext).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedCategory(workbenchContext.getLayerNamePanel())) == 0 ? I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.The-category-is-already-at-the-top") : null;
                }
            }));
            return solucion;
        }
    };
    public static final MoveCategoryPlugIn DOWN = new MoveCategoryPlugIn(1){
        public final String NAME = I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.Move-down");
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
            solucion.add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1));
            solucion.add(super.createEnableCheck(workbenchContext).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedCategory(workbenchContext.getLayerNamePanel())) == workbenchContext.getLayerViewPanel().getLayerManager().getCategories().size() - 1 ? I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.The-category-is-already-at-the-bottom") : null;
                }
            }));
            return solucion;
        }
    };
    public static final MoveCategoryPlugIn FIRST = new MoveCategoryPlugIn(-9999){
        public final String NAME = I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.Move-to-the-top");
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
            return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1)).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedCategory(workbenchContext.getLayerNamePanel())) == 0 ? I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.The-category-is-already-at-the-top") : null;
                }
            });
        }
    };
    public static final MoveCategoryPlugIn LAST = new MoveCategoryPlugIn(9999){
        public final String NAME = I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.Move-to-the-bottom");
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
            return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1)).add(new EnableCheck(){

                @Override
                public String check(JComponent component) {
                    return this.index(this.selectedCategory(workbenchContext.getLayerNamePanel())) == workbenchContext.getLayerViewPanel().getLayerManager().getCategories().size() - 1 ? I18N.getString("es.kosmo.desktop.plugins.category.MoveCategoryPlugIn.The-category-is-already-at-the-bottom") : null;
                }
            });
        }
    };
    protected int displacement;

    private MoveCategoryPlugIn(int displacement) {
        this.displacement = displacement;
    }

    protected Category selectedCategory(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Category.class).iterator().next();
    }

    protected Collection<Category> selectedCategories(LayerNamePanel layerNamePanel) {
        return layerNamePanel.selectedNodes(Category.class);
    }

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
        List<Category> categories = CategoryUtil.orderByIndex(this.selectedCategories(context.getLayerNamePanel()));
        if (categories.size() == 1) {
            final Category category = categories.iterator().next();
            final int index = this.index(category);
            this.execute(new UndoableCommand(this.getName()){

                @Override
                public void execute() {
                    switch (MoveCategoryPlugIn.this.displacement) {
                        case -9999: {
                            this.moveCategory(0);
                            break;
                        }
                        case 9999: {
                            this.moveCategory(context.getLayerManager().getCategories().size() - 1);
                            break;
                        }
                        default: {
                            this.moveCategory(index + MoveCategoryPlugIn.this.displacement);
                        }
                    }
                }

                @Override
                public void unexecute() {
                    this.moveCategory(index);
                }

                private void moveCategory(int newIndex) {
                    context.getLayerManager().remove(category);
                    context.getLayerManager().addCategory(category, newIndex);
                }
            }, context);
        }
        return true;
    }

    protected int index(Category cat) {
        return cat.getLayerManager().getCategories().indexOf(cat);
    }

    public MultiEnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        return new MultiEnableCheck().add(checkFactory.createWindowWithLayerNamePanelMustBeActiveCheck()).add(checkFactory.createExactlyNCategoriesMustBeSelectedCheck(1));
    }

    /* synthetic */ MoveCategoryPlugIn(int n, MoveCategoryPlugIn moveCategoryPlugIn) {
        this(n);
    }
}

