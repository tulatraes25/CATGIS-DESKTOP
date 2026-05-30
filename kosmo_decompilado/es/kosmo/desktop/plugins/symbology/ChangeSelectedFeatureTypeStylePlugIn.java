/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.plugins.symbology;

import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.Layerable;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import es.kosmo.desktop.gui.components.MenuScroller;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.saig.core.styling.FeatureTypeStyle;
import org.saig.core.styling.FeatureTypeStyleImpl;
import org.saig.core.styling.Style;
import org.saig.core.styling.StyleImpl;
import org.saig.core.util.LocaleManager;
import org.saig.jump.lang.I18N;

public class ChangeSelectedFeatureTypeStylePlugIn
extends AbstractPlugIn {
    public static final String NAME = String.valueOf(I18N.getString("es.kosmo.desktop.plugins.symbology.ChangeSelectedFeatureTypeStylePlugIn.Select-the-default-layer-style")) + "...";
    public static final Icon ICON = IconLoader.icon("blank.png");

    @Override
    public void initialize(PlugInContext context) throws Exception {
    }

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        return true;
    }

    @Override
    public Icon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public EnableCheck getCheck() {
        return ChangeSelectedFeatureTypeStylePlugIn.createEnableCheck(JUMPWorkbench.getFrameInstance().getContext());
    }

    public static EnableCheck createEnableCheck(WorkbenchContext workbenchContext) {
        MultiEnableCheck solucion = new MultiEnableCheck();
        EnableCheckFactory checkFactory = new EnableCheckFactory(workbenchContext);
        solucion.add(checkFactory.createExactlyNLayersMustBeSelectedCheck(1));
        solucion.add(checkFactory.createSelectedLayerMustHaveOnlyOneFeatureTypeStyleCheck());
        return solucion;
    }

    public JMenu getMenu() {
        JMenu changeSelectedFeatureTypeStyleMenu = new JMenu(NAME);
        changeSelectedFeatureTypeStyleMenu.setIcon(GUIUtil.toSmallIcon(ICON));
        changeSelectedFeatureTypeStyleMenu.addMenuListener(new MenuListener(){

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                JMenu source = (JMenu)e.getSource();
                source.removeAll();
                Layerable[] layerables = JUMPWorkbench.getFrameInstance().getContext().getLayerNamePanel().getSelectedLayers();
                final Layer selectedLayer = (Layer)layerables[0];
                final Style cloneStyle = (Style)((StyleImpl)selectedLayer.getModelStyle()).clone();
                FeatureTypeStyle[] fTStyles = cloneStyle.getFeatureTypeStyles();
                final FeatureTypeStyle selectedStyle = cloneStyle.getSelectedFeatureTypeStyle();
                int i = 0;
                while (i < fTStyles.length) {
                    final FeatureTypeStyle fts = (FeatureTypeStyle)((FeatureTypeStyleImpl)fTStyles[i]).clone();
                    JCheckBoxMenuItem item = new JCheckBoxMenuItem(fts.getTitle(LocaleManager.getActiveLocale()));
                    source.add(item);
                    if (fts.equals(selectedStyle)) {
                        item.setSelected(true);
                    }
                    item.addActionListener(new ActionListener(){

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JCheckBoxMenuItem item = (JCheckBoxMenuItem)e.getSource();
                            if (!item.getText().equals(selectedStyle.getTitle(LocaleManager.getActiveLocale()))) {
                                cloneStyle.setSelectedFeatureTypeStyle(fts);
                                selectedLayer.setModelStyle(cloneStyle);
                                selectedLayer.fireAppearanceChanged();
                            }
                        }
                    });
                    ++i;
                }
            }
        });
        MenuScroller.setScrollerFor(changeSelectedFeatureTypeStyleMenu, 5);
        return changeSelectedFeatureTypeStyleMenu;
    }
}

