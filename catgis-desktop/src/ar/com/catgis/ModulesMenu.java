package ar.com.catgis;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModulesMenu extends JMenu {

    public ModulesMenu() {
        super(I18n.t("Modulos"));
        rebuildMenu();
        ModuleRegistry.addChangeListener(this::rebuildMenu);
        addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                rebuildMenu();
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
    }

    public final void rebuildMenu() {
        removeAll();
        setText(I18n.t("Modulos"));

        JMenuItem managerItem = new JMenuItem(I18n.t("Gestor de modulos..."), AppIcons.propertiesIcon());
        managerItem.addActionListener(e -> ModuleManagerDialog.open());
        add(managerItem);
        addSeparator();

        Map<ModuleCategory, JMenu> byCategory = new LinkedHashMap<>();
        for (ModuleCategory category : ModuleCategory.values()) {
            byCategory.put(category, new JMenu(category.getDisplayName()));
        }

        for (CatgisModule module : ModuleRegistry.getModules()) {
            JMenu categoryMenu = byCategory.get(module.getCategory());
            if (categoryMenu == null) {
                continue;
            }

            JMenu moduleMenu = new JMenu(module.getName() + (module.isEnabled() ? "" : " (" + I18n.t("Desactivado") + ")"));
            moduleMenu.setToolTipText(buildModuleTooltip(module));
            if (!module.isEnabled()) {
                JMenuItem statusItem = new JMenuItem(I18n.t("Modulo desactivado. Activarlo desde el Gestor de modulos."));
                statusItem.setEnabled(false);
                moduleMenu.add(statusItem);
                moduleMenu.addSeparator();
            }
            for (CatgisModuleAction action : module.getActions()) {
                JMenuItem item = new JMenuItem(action.getName(), action.getIcon());
                boolean availableByContext = action.isAvailable();
                item.setToolTipText(buildActionTooltip(module, action, availableByContext));
                item.setEnabled(module.isEnabled() && availableByContext);
                item.addActionListener(e -> action.run());
                moduleMenu.add(item);
            }
            categoryMenu.add(moduleMenu);
        }

        for (JMenu categoryMenu : byCategory.values()) {
            if (categoryMenu.getItemCount() > 0) {
                add(categoryMenu);
            }
        }

        revalidate();
        repaint();
    }

    private String buildModuleTooltip(CatgisModule module) {
        String state = module.isEnabled() ? I18n.t("Activo") : I18n.t("Desactivado");
        String origin = module.getKosmoOrigin() != null && !module.getKosmoOrigin().isBlank()
                ? "<br><b>" + escape(I18n.t("Origen:")) + "</b> " + escape(module.getKosmoOrigin())
                : "";
        return "<html><b>" + escape(module.getName()) + "</b><br>"
                + escape(module.getDescription())
                + "<br><b>" + escape(I18n.t("Estado:")) + "</b> " + state
                + origin
                + "</html>";
    }

    private String buildActionTooltip(CatgisModule module, CatgisModuleAction action, boolean availableByContext) {
        String status;
        if (!module.isEnabled()) {
            status = I18n.t("Modulo desactivado. Activarlo desde el Gestor de modulos.");
        } else if (!availableByContext) {
            status = I18n.t("No disponible con el contexto actual.");
        } else {
            status = I18n.t("Disponible.");
        }

        return "<html><b>" + escape(action.getName()) + "</b><br>"
                + escape(action.getDescription())
                + "<br><b>" + escape(I18n.t("Estado:")) + "</b> " + status
                + "</html>";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
