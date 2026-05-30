package ar.com.catgis;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.KeyStroke;
import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class CRSSelectorDialog extends JDialog {

    private static final int MAX_VISIBLE_RESULTS = 250;

    private JTextField searchField;
    private JTextField manualCodeField;
    private JTextArea manualDefinitionArea;
    private DefaultListModel<CRSDefinitions.CrsCatalogEntry> listModel;
    private JList<CRSDefinitions.CrsCatalogEntry> crsList;
    private JLabel loadingLabel;
    private JLabel selectedNameLabel;
    private JLabel selectedCodeLabel;
    private JLabel selectedTypeLabel;
    private JLabel selectedDatumLabel;
    private JLabel selectedUnitLabel;
    private JLabel selectedAreaLabel;
    private JLabel selectedMethodLabel;
    private JTextArea parametersArea;
    private AreaPreviewPanel previewPanel;
    private JButton useSelectedButton;
    private JButton useManualButton;
    private JButton closeButton;
    private JButton favButton;
    private final Consumer<String> onSelect;

    private final List<CRSDefinitions.CrsCatalogEntry> featuredEntries;
    private List<CRSDefinitions.CrsCatalogEntry> allEntries;
    private final String currentCode;
    private boolean worldCatalogLoading;
    private boolean worldCatalogLoaded;
    private boolean syncingManualCode;
    private boolean syncingListSelection;
    private Timer searchDebounceTimer;
    private int activeTabIndex = 0;

    public CRSSelectorDialog(Window owner, String title, String currentCode, Consumer<String> onSelect) {
        super(
                owner,
                title != null ? title : I18n.t("Selector de CRS"),
                Dialog.ModalityType.MODELESS
        );
        this.currentCode = CRSDefinitions.normalizeCode(currentCode);
        this.onSelect = onSelect;
        this.featuredEntries = new ArrayList<>(CRSDefinitions.getFeaturedEntries());
        this.allEntries = null;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        content.setBackground(new Color(244, 247, 251));

        content.add(buildHeaderPanel(title), BorderLayout.NORTH);
        content.add(buildCenterPanel(), BorderLayout.CENTER);
        content.add(buildFooterPanel(), BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);
        pack();
        WindowLayoutSupport.fitDialogToScreen(this, 1120, 720, 900, 580);
        setLocationRelativeTo(owner);

        installListeners();
        installKeyboardBehavior();
        refreshListNow();
        preloadCurrentSelection();
        updateActionState();
        SwingUtilities.invokeLater(() -> {
            if (searchField != null) {
                searchField.requestFocusInWindow();
                searchField.selectAll();
            }
        });
    }

    private JPanel buildHeaderPanel(String title) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(true);
        header.setBackground(new Color(247, 250, 252));
        header.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JPanel left = new JPanel(new BorderLayout(0, 6));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel(title != null && !title.isBlank() ? title : I18n.t("Selector de CRS"));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        titleLabel.setForeground(new Color(21, 40, 74));

        JLabel subtitleLabel = new JLabel(
                "<html>" + I18n.t("Selector global de sistemas de referencia con cobertura mundial, panel tecnico, area de uso y carga manual.") + "</html>"
        );
        subtitleLabel.setForeground(new Color(72, 86, 104));
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 12.5f));

        left.add(titleLabel, BorderLayout.NORTH);
        left.add(subtitleLabel, BorderLayout.CENTER);

        JPanel searchCard = new JPanel(new BorderLayout(6, 6));
        searchCard.setOpaque(false);
        JLabel searchLabel = new JLabel(I18n.t("Buscar por nombre, EPSG, zona o region"));
        searchLabel.setFont(searchLabel.getFont().deriveFont(Font.BOLD, 12f));
        searchLabel.setForeground(new Color(44, 57, 75));

        searchField = new JTextField();
        searchField.setToolTipText(I18n.t("Busca por codigo, nombre, datum, region o zona UTM."));
        searchField.putClientProperty("JTextField.placeholderText", I18n.t("Ej.: 22182, UTM 19S, Chos Malal, China, Africa"));

        loadingLabel = new JLabel(I18n.t("Catalogo destacado listo. Usa la busqueda para ampliar."), SwingConstants.RIGHT);
        loadingLabel.setForeground(new Color(33, 120, 210));
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 11.5f));

        searchCard.add(searchLabel, BorderLayout.NORTH);
        searchCard.add(searchField, BorderLayout.CENTER);
        searchCard.add(loadingLabel, BorderLayout.SOUTH);

        header.add(left, BorderLayout.CENTER);
        header.add(searchCard, BorderLayout.EAST);
        return header;
    }

    private Component buildCenterPanel() {
        JScrollPane detailsScrollPane = WindowLayoutSupport.createVerticalScrollPane(buildDetailsPanel(), 520, 560);
        detailsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildCatalogPanel(), detailsScrollPane);
        splitPane.setResizeWeight(0.46d);
        splitPane.setDividerLocation(470);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setContinuousLayout(true);
        splitPane.setPreferredSize(new Dimension(1040, 560));
        return splitPane;
    }

    private JPanel buildCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(createCardBorder());

        JLabel title = new JLabel(I18n.t("Catalogo de CRS"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(29, 45, 71));

        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        tabBar.setOpaque(false);
        JButton featuredTab = createTabButton(I18n.t("Destacados"), true);
        JButton favoritesTab = createTabButton(I18n.t("Favoritos"), false);
        JButton recentTab = createTabButton(I18n.t("Recientes"), false);
        tabBar.add(featuredTab);
        tabBar.add(favoritesTab);
        tabBar.add(recentTab);

        final int[] activeTab = {0};
        featuredTab.addActionListener(e -> { activeTab[0] = 0; selectTab(0, featuredTab, favoritesTab, recentTab); });
        favoritesTab.addActionListener(e -> { activeTab[0] = 1; selectTab(1, featuredTab, favoritesTab, recentTab); });
        recentTab.addActionListener(e -> { activeTab[0] = 2; selectTab(2, featuredTab, favoritesTab, recentTab); });

        JLabel hint = new JLabel(I18n.t("Lista con EPSG destacados, favoritos, recientes y catalogo mundial completo"));
        hint.setForeground(new Color(91, 103, 121));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11.5f));

        JPanel top = new JPanel(new BorderLayout(0, 2));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(tabBar, BorderLayout.CENTER);
        top.add(hint, BorderLayout.SOUTH);

        listModel = new DefaultListModel<>();
        crsList = new JList<>(listModel);
        crsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        crsList.setCellRenderer(new CrsCellRenderer());
        crsList.setFixedCellHeight(30);

        JScrollPane scrollPane = new JScrollPane(crsList);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(top, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JButton createTabButton(String text, boolean selected) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 12f));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? new Color(33, 120, 210) : new Color(200, 208, 218)),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        btn.setBackground(selected ? new Color(235, 245, 255) : Color.WHITE);
        btn.setForeground(selected ? new Color(21, 101, 192) : new Color(80, 90, 110));
        btn.setOpaque(true);
        return btn;
    }

    private void selectTab(int index, JButton... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            boolean sel = i == index;
            buttons[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(sel ? new Color(33, 120, 210) : new Color(200, 208, 218)),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)
            ));
            buttons[i].setBackground(sel ? new Color(235, 245, 255) : Color.WHITE);
            buttons[i].setForeground(sel ? new Color(21, 101, 192) : new Color(80, 90, 110));
        }
        refreshListNow();
    }

    private List<CRSDefinitions.CrsCatalogEntry> resolveTabSource() {
        int tab = activeTabIndex();
        if (tab == 1) return getFavoritesList();
        if (tab == 2) return getRecentsList();
        return featuredEntries;
    }

    private int activeTabIndex() {
        return activeTabIndex;
    }

    private List<CRSDefinitions.CrsCatalogEntry> getFavoritesList() {
        return CRSDefinitions.getFavoriteEntries();
    }

    private List<CRSDefinitions.CrsCatalogEntry> getRecentsList() {
        return CRSDefinitions.getRecentEntries();
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(true);
        panel.setBackground(new Color(244, 247, 251));

        JPanel topCard = new JPanel(new BorderLayout(8, 8));
        topCard.setOpaque(false);
        topCard.setBorder(createCardBorder());

        JLabel title = new JLabel(I18n.t("Panel tecnico"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(new Color(29, 45, 71));

        favButton = new JButton(I18n.t("★"));
        favButton.setFont(favButton.getFont().deriveFont(Font.BOLD, 16f));
        favButton.setFocusPainted(false);
        favButton.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        favButton.setContentAreaFilled(false);
        favButton.setToolTipText(I18n.t("Agregar o quitar de favoritos"));
        favButton.addActionListener(e -> {
            String code = getSelectedListCode();
            if (code != null && !code.isBlank()) {
                CRSDefinitions.toggleFavorite(code);
                updateFavoriteButtonStyle(favButton, code);
                if (activeTabIndex == 1) refreshListNow();
            }
        });

        JPanel topCardHeader = new JPanel(new BorderLayout(4, 0));
        topCardHeader.setOpaque(false);
        topCardHeader.add(title, BorderLayout.WEST);
        topCardHeader.add(favButton, BorderLayout.EAST);
        topCard.add(topCardHeader, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridBagLayout());
        detailsGrid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(3, 0, 3, 8);
        gc.anchor = GridBagConstraints.NORTHWEST;

        selectedNameLabel = createValueLabel();
        selectedCodeLabel = createValueLabel();
        selectedTypeLabel = createValueLabel();
        selectedDatumLabel = createValueLabel();
        selectedUnitLabel = createValueLabel();
        selectedAreaLabel = createMultilineValueLabel();
        selectedMethodLabel = createMultilineValueLabel();

        addTechnicalField(detailsGrid, gc, I18n.t("Nombre"), selectedNameLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Codigo"), selectedCodeLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Tipo"), selectedTypeLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Datum"), selectedDatumLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Unidad"), selectedUnitLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Area de uso"), selectedAreaLabel);
        addTechnicalField(detailsGrid, gc, I18n.t("Metodo"), selectedMethodLabel);

        parametersArea = new JTextArea(5, 24);
        parametersArea.setEditable(false);
        parametersArea.setLineWrap(true);
        parametersArea.setWrapStyleWord(true);
        parametersArea.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, 11.5f));
        parametersArea.setBackground(new Color(248, 250, 252));
        parametersArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        JLabel paramsLabel = new JLabel(I18n.t("Parametros"));
        paramsLabel.setFont(paramsLabel.getFont().deriveFont(Font.BOLD, 12f));
        paramsLabel.setForeground(new Color(55, 65, 81));
        detailsGrid.add(paramsLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        detailsGrid.add(new JScrollPane(parametersArea), gc);

        topCard.add(detailsGrid, BorderLayout.CENTER);

        JPanel bottomArea = new JPanel(new BorderLayout(8, 8));
        bottomArea.setOpaque(false);

        previewPanel = new AreaPreviewPanel();
        previewPanel.setPreferredSize(new Dimension(320, 248));
        previewPanel.setBorder(createCardBorder());

        JPanel manualCard = new JPanel(new BorderLayout(6, 6));
        manualCard.setOpaque(false);
        manualCard.setBorder(createCardBorder());

        JLabel manualTitle = new JLabel(I18n.t("Carga manual"));
        manualTitle.setFont(manualTitle.getFont().deriveFont(Font.BOLD, 15f));
        manualTitle.setForeground(new Color(29, 45, 71));

        JPanel manualFields = new JPanel(new GridBagLayout());
        manualFields.setOpaque(false);
        GridBagConstraints mgc = new GridBagConstraints();
        mgc.gridx = 0;
        mgc.gridy = 0;
        mgc.insets = new Insets(3, 0, 3, 8);
        mgc.anchor = GridBagConstraints.NORTHWEST;

        manualCodeField = new JTextField(currentCode != null ? currentCode : "", 24);
        manualCodeField.putClientProperty("JTextField.placeholderText", "EPSG:22182, EPSG:4490, ESRI:102100...");
        addTechnicalField(manualFields, mgc, I18n.t("Codigo manual"), manualCodeField);

        manualDefinitionArea = new JTextArea(6, 24);
        manualDefinitionArea.setLineWrap(true);
        manualDefinitionArea.setWrapStyleWord(true);
        manualDefinitionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        manualDefinitionArea.setToolTipText(I18n.t("Pega aqui una definicion WKT si necesitas una proyeccion personalizada."));

        mgc.gridx = 0;
        mgc.weightx = 0;
        mgc.fill = GridBagConstraints.NONE;
        JLabel definitionLabel = new JLabel(I18n.t("Definicion WKT"));
        definitionLabel.setFont(definitionLabel.getFont().deriveFont(Font.BOLD, 12f));
        definitionLabel.setForeground(new Color(55, 65, 81));
        manualFields.add(definitionLabel, mgc);

        mgc.gridx = 1;
        mgc.weightx = 1;
        mgc.fill = GridBagConstraints.HORIZONTAL;
        manualFields.add(new JScrollPane(manualDefinitionArea), mgc);

        JLabel manualHelp = new JLabel("<html>" + I18n.t("Admite codigos EPSG/ESRI/OGC y definiciones WKT para casos personalizados.") + "</html>");
        manualHelp.setForeground(new Color(91, 103, 121));
        manualHelp.setFont(manualHelp.getFont().deriveFont(Font.PLAIN, 11.2f));

        manualCard.add(manualTitle, BorderLayout.NORTH);
        manualCard.add(manualFields, BorderLayout.CENTER);
        manualCard.add(manualHelp, BorderLayout.SOUTH);

        bottomArea.add(previewPanel, BorderLayout.CENTER);
        bottomArea.add(manualCard, BorderLayout.SOUTH);

        panel.add(topCard, BorderLayout.NORTH);
        panel.add(bottomArea, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(520, 760));
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panel.setOpaque(true);
        panel.setBackground(new Color(244, 247, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(10, 0, 2, 0)
        ));

        useSelectedButton = new JButton(I18n.t("Aplicar seleccionado"));
        useSelectedButton.addActionListener(e -> applySelected());

        useManualButton = new JButton(I18n.t("Aplicar manual"));
        useManualButton.addActionListener(e -> applyManual());

        closeButton = new JButton(I18n.t("Cerrar"));
        closeButton.addActionListener(e -> dispose());

        panel.add(useSelectedButton);
        panel.add(useManualButton);
        panel.add(closeButton);
        return panel;
    }

    private void installListeners() {
        searchDebounceTimer = new Timer(140, e -> refreshListNow());
        searchDebounceTimer.setRepeats(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSearchRefresh();
            }
        });

        crsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !syncingListSelection) {
                updateDetailsForSelection();
            }
        });

        crsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && crsList.getSelectedValue() != null) {
                    applySelected();
                }
            }
        });

        manualCodeField.addActionListener(e -> applyManual());
        manualCodeField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }
        });
        manualDefinitionArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                syncFromManualInputs();
            }
        });
    }

    private void installKeyboardBehavior() {
        DialogKeyboardSupport.install(this, useSelectedButton, this::dispose);

        manualDefinitionArea.getInputMap().put(
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                "catgis.applyManual"
        );
        manualDefinitionArea.getActionMap().put("catgis.applyManual", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                applyManual();
            }
        });
    }

    private void loadFullCatalogAsync() {
        if (worldCatalogLoading || worldCatalogLoaded) {
            return;
        }
        worldCatalogLoading = true;
        updateLoadingLabel(true, 0, 0);
        SwingWorker<List<CRSDefinitions.CrsCatalogEntry>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CRSDefinitions.CrsCatalogEntry> doInBackground() {
                return CRSDefinitions.getCatalogEntries();
            }

            @Override
            protected void done() {
                try {
                    allEntries = get();
                    worldCatalogLoaded = true;
                } catch (Exception ex) {
                    allEntries = null;
                    worldCatalogLoaded = false;
                }
                worldCatalogLoading = false;
                refreshListNow();
                preloadCurrentSelection();
            }
        };
        worker.execute();
    }

    private void preloadCurrentSelection() {
        if (currentCode == null || currentCode.isBlank()) {
            if (!listModel.isEmpty() && crsList.getSelectedIndex() < 0) {
                crsList.setSelectedIndex(0);
            }
            updateActionState();
            return;
        }

        for (int i = 0; i < listModel.size(); i++) {
            CRSDefinitions.CrsCatalogEntry entry = listModel.get(i);
            if (entry != null && currentCode.equalsIgnoreCase(entry.code())) {
                crsList.setSelectedIndex(i);
                crsList.ensureIndexIsVisible(i);
                updateDetailsForSelection();
                return;
            }
        }

        crsList.clearSelection();
        setManualCodeSilently(currentCode);
        updateDetailsFromManualState();
        updateActionState();
    }

    private void scheduleSearchRefresh() {
        if (searchDebounceTimer == null) {
            refreshListNow();
            return;
        }
        searchDebounceTimer.restart();
    }

    private void refreshListNow() {
        String previousSelectedCode = getSelectedListCode();
        listModel.clear();
        String query = searchField != null ? searchField.getText() : "";
        boolean hasQuery = query != null && !query.isBlank();
        if (hasQuery && !worldCatalogLoaded) {
            loadFullCatalogAsync();
        }
        List<CRSDefinitions.CrsCatalogEntry> tabSource = hasQuery ? featuredEntries : resolveTabSource();
        List<CRSDefinitions.CrsCatalogEntry> source = hasQuery
                ? (allEntries != null ? allEntries : tabSource)
                : tabSource;
        List<CRSDefinitions.CrsCatalogEntry> filtered = CRSDefinitions.filterEntries(query, source);
        int visibleCount = Math.min(filtered.size(), MAX_VISIBLE_RESULTS);
        for (int i = 0; i < visibleCount; i++) {
            listModel.addElement(filtered.get(i));
        }
        updateLoadingLabel(hasQuery, filtered.size(), visibleCount);
        applySelectionAfterRefresh(hasQuery, previousSelectedCode);
        if (hasQuery && crsList.getSelectedValue() == null && !listModel.isEmpty()) {
            setSelectedIndexSilently(0);
            crsList.ensureIndexIsVisible(0);
        }
        if (crsList.getSelectedValue() != null) {
            updateDetailsForSelection();
        } else if (!hasQuery) {
            updateDetailsFromManualState();
        } else {
            updateDetailsUnselected();
        }
        updateActionState();
    }

    private void updateLoadingLabel(boolean hasQuery, int totalMatches, int visibleCount) {
        if (loadingLabel == null) {
            return;
        }
        if (worldCatalogLoading) {
            loadingLabel.setText(I18n.t("Cargando catalogo mundial EPSG..."));
            return;
        }
        if (hasQuery && !worldCatalogLoaded) {
            loadingLabel.setText(I18n.t("Buscando primero en destacados. El catalogo mundial se esta preparando..."));
            return;
        }
        if (hasQuery && totalMatches > visibleCount) {
            loadingLabel.setText(I18n.t("Mostrando primeros resultados:") + " " + visibleCount + " / " + totalMatches);
            return;
        }
        if (worldCatalogLoaded && allEntries != null) {
            loadingLabel.setText(I18n.t("Catalogo mundial listo:") + " " + allEntries.size() + " CRS");
            return;
        }
        loadingLabel.setText(I18n.t("Catalogo destacado listo. Usa la busqueda para ampliar."));
    }

    private void updateDetailsForSelection() {
        CRSDefinitions.CrsCatalogEntry entry = crsList.getSelectedValue();
        if (entry == null) {
            updateDetailsFromManualState();
            return;
        }
        setManualCodeSilently(entry.code());
        updateDetails(CRSDefinitions.describe(entry.code()));
        updateFavoriteButtonStyle(entry.code());
        updateActionState();
    }

    private void updateDetails(CRSDefinitions.CrsTechnicalDetails details) {
        selectedNameLabel.setText(details.name());
        selectedCodeLabel.setText(details.code());
        selectedTypeLabel.setText(details.type());
        selectedDatumLabel.setText(details.datum());
        selectedUnitLabel.setText(details.unit());
        selectedAreaLabel.setText("<html>" + escape(details.areaOfUse()) + "</html>");
        selectedMethodLabel.setText("<html>" + escape(details.projectionMethod()) + "</html>");
        parametersArea.setText(details.parameters());
        previewPanel.setDetails(details, resolveCurrentProjectMarker());
    }

    private void updateDetailsUnselected() {
        selectedNameLabel.setText(I18n.t("Sin seleccion"));
        selectedCodeLabel.setText("-");
        selectedTypeLabel.setText("-");
        selectedDatumLabel.setText("-");
        selectedUnitLabel.setText("-");
        selectedAreaLabel.setText("<html>" + escape(I18n.t("Selecciona un CRS de la lista para ver detalles.")) + "</html>");
        selectedMethodLabel.setText("<html>" + escape(I18n.t("Sin metodo")) + "</html>");
        parametersArea.setText(I18n.t("Sin parametros. Selecciona un CRS para cargar detalle tecnico."));
        previewPanel.setDetails(null, resolveCurrentProjectMarker());
    }

    private void updateDetailsFromManualState() {
        String manualDefinition = manualDefinitionArea != null && manualDefinitionArea.getText() != null
                ? manualDefinitionArea.getText().trim()
                : "";
        if (!manualDefinition.isBlank()) {
            updateDetails(CRSDefinitions.describe(CRSDefinitions.buildManualDefinitionValue(manualDefinition)));
            return;
        }

        String manualCode = normalizedManualCode();
        if (!manualCode.isBlank()) {
            updateDetails(CRSDefinitions.describe(manualCode));
            return;
        }

        if (currentCode != null && !currentCode.isBlank()) {
            updateDetails(CRSDefinitions.describe(currentCode));
            return;
        }

        updateDetails(CRSDefinitions.describe("EPSG:4326"));
    }

    private void syncFromManualInputs() {
        if (syncingManualCode) {
            return;
        }

        String manualDefinition = manualDefinitionArea != null && manualDefinitionArea.getText() != null
                ? manualDefinitionArea.getText().trim()
                : "";
        if (!manualDefinition.isBlank()) {
            clearListSelectionSilently();
            updateDetails(CRSDefinitions.describe(CRSDefinitions.buildManualDefinitionValue(manualDefinition)));
            updateActionState();
            return;
        }

        String manualCode = normalizedManualCode();
        if (!manualCode.isBlank()) {
            if (!selectEntryByCode(manualCode)) {
                clearListSelectionSilently();
                updateDetails(CRSDefinitions.describe(manualCode));
            }
            updateActionState();
            return;
        }

        if (!selectEntryByCode(currentCode)) {
            if (!listModel.isEmpty()) {
                setSelectedIndexSilently(0);
                updateDetailsForSelection();
            } else {
                clearListSelectionSilently();
                updateDetailsFromManualState();
            }
        }
        updateActionState();
    }

    private void applySelectionAfterRefresh(boolean hasQuery, String previousSelectedCode) {
        if (hasQuery) {
            if (selectEntryByCode(previousSelectedCode)
                    || selectEntryByCode(normalizedManualCode())
                    || selectEntryByCode(currentCode)) {
                return;
            }
            if (listModel != null && !listModel.isEmpty()) {
                setSelectedIndexSilently(0);
                if (crsList != null) {
                    crsList.ensureIndexIsVisible(0);
                }
            } else {
                clearListSelectionSilently();
            }
            return;
        }

        if (selectEntryByCode(previousSelectedCode)
                || selectEntryByCode(normalizedManualCode())
                || selectEntryByCode(currentCode)) {
            return;
        }

        boolean hasExplicitCode = !normalizedManualCode().isBlank() || (currentCode != null && !currentCode.isBlank());
        if (hasExplicitCode) {
            clearListSelectionSilently();
        } else if (!listModel.isEmpty()) {
            setSelectedIndexSilently(0);
        }
    }

    private String normalizedManualCode() {
        return manualCodeField != null ? CRSDefinitions.normalizeCode(manualCodeField.getText()) : "";
    }

    private String getSelectedListCode() {
        CRSDefinitions.CrsCatalogEntry selected = crsList != null ? crsList.getSelectedValue() : null;
        return selected != null ? selected.code() : "";
    }

    private boolean selectEntryByCode(String code) {
        String normalized = CRSDefinitions.normalizeCode(code);
        if (normalized.isBlank() || listModel == null) {
            return false;
        }
        for (int i = 0; i < listModel.size(); i++) {
            CRSDefinitions.CrsCatalogEntry entry = listModel.get(i);
            if (entry != null && normalized.equalsIgnoreCase(entry.code())) {
                setSelectedIndexSilently(i);
                if (crsList != null) {
                    crsList.ensureIndexIsVisible(i);
                }
                return true;
            }
        }
        return false;
    }

    private void clearListSelectionSilently() {
        if (crsList == null) {
            return;
        }
        syncingListSelection = true;
        try {
            crsList.clearSelection();
        } finally {
            syncingListSelection = false;
        }
    }

    private void setSelectedIndexSilently(int index) {
        if (crsList == null) {
            return;
        }
        syncingListSelection = true;
        try {
            crsList.setSelectedIndex(index);
        } finally {
            syncingListSelection = false;
        }
    }

    private void setManualCodeSilently(String code) {
        if (manualCodeField == null) {
            return;
        }
        syncingManualCode = true;
        try {
            manualCodeField.setText(code != null ? code : "");
        } finally {
            syncingManualCode = false;
        }
    }

    private void updateActionState() {
        boolean hasSelection = crsList != null && crsList.getSelectedValue() != null;
        boolean hasManualDefinition = manualDefinitionArea != null
                && manualDefinitionArea.getText() != null
                && !manualDefinitionArea.getText().trim().isBlank();
        boolean hasManualCode = !normalizedManualCode().isBlank();

        if (useSelectedButton != null) {
            useSelectedButton.setEnabled(hasSelection);
        }
        if (useManualButton != null) {
            useManualButton.setEnabled(hasManualDefinition || hasManualCode);
        }
        if (getRootPane() != null) {
            if (hasSelection && useSelectedButton != null && useSelectedButton.isEnabled()) {
                getRootPane().setDefaultButton(useSelectedButton);
            } else if (useManualButton != null && useManualButton.isEnabled()) {
                getRootPane().setDefaultButton(useManualButton);
            } else if (closeButton != null) {
                getRootPane().setDefaultButton(closeButton);
            }
        }
    }

    private void applySelected() {
        CRSDefinitions.CrsCatalogEntry entry = crsList.getSelectedValue();
        if (entry == null) {
            JOptionPane.showMessageDialog(this, I18n.t("Selecciona un CRS de la lista."));
            return;
        }
        applyValue(entry.code());
    }

    private void applyManual() {
        String manualDefinition = manualDefinitionArea.getText() != null ? manualDefinitionArea.getText().trim() : "";
        String value = !manualDefinition.isBlank()
                ? CRSDefinitions.buildManualDefinitionValue(manualDefinition)
                : CRSDefinitions.normalizeCode(manualCodeField.getText());

        if (value.isBlank()) {
            JOptionPane.showMessageDialog(this, I18n.t("Ingresa un codigo CRS o una definicion WKT."));
            return;
        }
        applyValue(value);
    }

    private void applyValue(String value) {
        try {
            String normalized = CRSDefinitions.normalizeCode(value);
            CRSDefinitions.decode(normalized, true);
            CRSDefinitions.addRecentCode(normalized);
            if (onSelect != null) {
                onSelect.accept(normalized);
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    I18n.t("No se pudo validar el CRS indicado:") + "\n" + ex.getMessage(),
                    I18n.t("Selector de CRS"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateFavoriteButtonStyle(String code) {
        if (favButton == null) return;
        boolean isFav = CRSDefinitions.isFavorite(code);
        favButton.setText(isFav ? I18n.t("★") : I18n.t("☆"));
        favButton.setForeground(isFav ? new Color(240, 160, 20) : new Color(140, 150, 170));
        favButton.setToolTipText(isFav ? I18n.t("Quitar de favoritos") : I18n.t("Agregar a favoritos"));
    }

    private void updateFavoriteButtonStyle(JButton btn, String code) {
        boolean isFav = CRSDefinitions.isFavorite(code);
        btn.setText(isFav ? I18n.t("★") : I18n.t("☆"));
        btn.setForeground(isFav ? new Color(240, 160, 20) : new Color(140, 150, 170));
        btn.setToolTipText(isFav ? I18n.t("Quitar de favoritos") : I18n.t("Agregar a favoritos"));
    }
    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setForeground(new Color(31, 41, 55));
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12.2f));
        return label;
    }

    private JLabel createMultilineValueLabel() {
        JLabel label = createValueLabel();
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    private void addTechnicalField(JPanel panel, GridBagConstraints gc, String title, Component component) {
        JLabel fieldLabel = new JLabel(title);
        fieldLabel.setFont(fieldLabel.getFont().deriveFont(Font.BOLD, 12f));
        fieldLabel.setForeground(new Color(55, 65, 81));

        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        panel.add(fieldLabel, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gc);
        gc.gridy++;
    }

    private CompoundBorder createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 236)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private double[] resolveCurrentProjectMarker() {
        if (CatgisDesktopApp.currentProject == null || CatgisDesktopApp.mapPanel == null) {
            return null;
        }
        var envelope = CatgisDesktopApp.mapPanel.getCurrentViewEnvelope();
        if (envelope == null || envelope.isNull()) {
            return null;
        }
        double centerX = envelope.getMinX() + (envelope.getWidth() / 2d);
        double centerY = envelope.getMinY() + (envelope.getHeight() / 2d);
        try {
            var source = CRSDefinitions.decode(CatgisDesktopApp.currentProject.getProjectCRS(), true);
            var target = CRSDefinitions.decode("EPSG:4326", true);
            var transform = org.geotools.referencing.CRS.findMathTransform(source, target, true);
            double[] src = new double[]{centerX, centerY};
            double[] dst = new double[2];
            transform.transform(src, 0, dst, 0, 1);
            if (Double.isNaN(dst[0]) || Double.isNaN(dst[1])
                    || Double.isInfinite(dst[0]) || Double.isInfinite(dst[1])) {
                return null;
            }
            if (dst[0] < -180d || dst[0] > 180d || dst[1] < -90d || dst[1] > 90d) {
                return null;
            }
            return dst;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void open(String title, String currentCode, Consumer<String> onSelect) {
        Runnable openTask = () -> {
            Window owner = resolveOwnerWindow();
            CRSSelectorDialog dialog = new CRSSelectorDialog(owner, title, currentCode, onSelect);
            if (owner != null && owner.isDisplayable()) {
                dialog.setLocationRelativeTo(owner);
            }
            dialog.toFront();
            dialog.setVisible(true);
            dialog.requestFocus();
        };
        if (SwingUtilities.isEventDispatchThread()) {
            openTask.run();
        } else {
            SwingUtilities.invokeLater(openTask);
        }
    }

    public static String chooseBlocking(Window owner, String title, String currentCode) {
        final String[] selected = new String[1];
        Window resolvedOwner = owner;
        if (resolvedOwner == null || !resolvedOwner.isDisplayable()) {
            resolvedOwner = resolveOwnerWindow();
        }
        if (SwingUtilities.isEventDispatchThread()) {
            var secondaryLoop = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
            CRSSelectorDialog dialog = new CRSSelectorDialog(resolvedOwner, title, currentCode, code -> selected[0] = code);
            if (resolvedOwner != null && resolvedOwner.isDisplayable()) {
                dialog.setLocationRelativeTo(resolvedOwner);
            }
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    secondaryLoop.exit();
                }
            });
            dialog.toFront();
            dialog.setVisible(true);
            dialog.requestFocus();
            secondaryLoop.enter();
            return selected[0];
        }
        try {
            final Window finalResolvedOwner = resolvedOwner;
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                CRSSelectorDialog dialog = new CRSSelectorDialog(finalResolvedOwner, title, currentCode, code -> selected[0] = code);
                if (finalResolvedOwner != null && finalResolvedOwner.isDisplayable()) {
                    dialog.setLocationRelativeTo(finalResolvedOwner);
                }
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        latch.countDown();
                    }
                });
                dialog.toFront();
                dialog.setVisible(true);
                dialog.requestFocus();
            });
            latch.await();
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo abrir el selector de CRS.", ex);
        }
        return selected[0];
    }

    private static Window resolveOwnerWindow() {
        Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (activeWindow != null && activeWindow.isDisplayable()) {
            return activeWindow;
        }
        Window focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
        if (focusedWindow != null && focusedWindow.isDisplayable()) {
            return focusedWindow;
        }
        Frame mainFrame = CatgisDesktopApp.getMainFrame();
        if (mainFrame != null && mainFrame.isDisplayable()) {
            return mainFrame;
        }
        return null;
    }

    private static class CrsCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof CRSDefinitions.CrsCatalogEntry entry) {
                String text = entry.code() + " - " + compactDescription(entry.description());
                label.setText(text);
                if (!isSelected) {
                    label.setForeground(entry.featured() ? new Color(30, 64, 175) : new Color(31, 41, 55));
                }
            }
            label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return label;
        }

        private String compactDescription(String description) {
            if (description == null) {
                return "";
            }
            String value = description;
            int sep = value.indexOf(" | ");
            if (sep > 0) {
                value = value.substring(0, sep);
            }
            value = value.trim();
            if (value.length() > 86) {
                return value.substring(0, 83) + "...";
            }
            return value;
        }
    }

    private static class AreaPreviewPanel extends JPanel {
        private static final String[] PLANISPHERE_IMAGE_RESOURCES = new String[]{
                "crs/world-planisphere.png",
                "main/resources/crs/world-planisphere.png",
                "help/assets/world-planisphere.png",
                "help/assets/world-planisphere.jpg",
                "main/resources/crs/world-planisphere.jpg",
                "crs/world-planisphere.jpg"
        };
        private static volatile BufferedImage cachedPlanisphere;
        private static volatile boolean planisphereLoadAttempted;
        private CRSDefinitions.CrsTechnicalDetails details;
        private double[] marker;

        private AreaPreviewPanel() {
            setBackground(Color.WHITE);
        }

        private void setDetails(CRSDefinitions.CrsTechnicalDetails details, double[] marker) {
            this.details = details;
            this.marker = marker;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int pad = 14;
                int w = Math.max(1, getWidth() - (pad * 2));
                int h = Math.max(1, getHeight() - (pad * 2));
                int x = pad;
                int y = pad;
                Rectangle mapRect = new Rectangle(x + 8, y + 26, Math.max(20, w - 16), Math.max(20, h - 44));

                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(x, y, w, h, 16, 16);

                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(x, y, w, h, 16, 16);

                g2.setColor(new Color(226, 232, 240));
                for (int i = 1; i < 6; i++) {
                    int xx = x + Math.round((w / 6f) * i);
                    int yy = y + Math.round((h / 6f) * i);
                    g2.drawLine(xx, y + 6, xx, y + h - 6);
                    g2.drawLine(x + 6, yy, x + w - 6, yy);
                }

                // El recurso PNG se genera desde Natural Earth en coordenadas lon/lat equirectangulares.
                // Si no esta disponible, se usa el fallback vectorial simple para no romper el selector.
                if (!drawPlanisphereImage(g2, mapRect)) {
                    drawPlanisphere(g2, mapRect.x, mapRect.y, mapRect.width, mapRect.height);
                }

                if (details != null && details.hasBounds()) {
                    boolean globalCoverage = details.west() <= -179d
                            && details.east() >= 179d
                            && details.south() <= -89d
                            && details.north() >= 89d;
                    if (globalCoverage) {
                        g2.setColor(new Color(29, 78, 216, 185));
                        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                        g2.drawString(I18n.t("Cobertura global"), x + w - 126, y + 18);
                    } else {
                        drawAreaBounds(g2, mapRect, details.west(), details.south(), details.east(), details.north());
                    }
                } else {
                    g2.setColor(new Color(100, 116, 139));
                    g2.drawString(I18n.t("Sin area de uso reportada"), x + 12, y + 24);
                }

                if (marker != null && marker.length >= 2) {
                    int mx = lonToX(marker[0], mapRect.x, mapRect.width);
                    int my = latToY(marker[1], mapRect.y, mapRect.height);
                    g2.setColor(new Color(220, 38, 38));
                    g2.fillOval(mx - 4, my - 4, 8, 8);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(mx - 8, my - 8, 16, 16);
                }

                g2.setColor(new Color(55, 65, 81));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                g2.drawString(I18n.t("Area de uso"), x + 10, y + 18);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                g2.setColor(new Color(91, 103, 121));
                g2.drawString(I18n.t("Planisferio / franja / region EPSG"), x + 10, y + h - 10);
            } finally {
                g2.dispose();
            }
        }

        private void drawAreaBounds(Graphics2D g2, Rectangle mapRect, double west, double south, double east, double north) {
            if (Double.isNaN(west) || Double.isNaN(south) || Double.isNaN(east) || Double.isNaN(north)) {
                return;
            }
            if (west <= east) {
                drawSingleAreaBounds(g2, mapRect, west, south, east, north);
            } else {
                drawSingleAreaBounds(g2, mapRect, west, south, 180d, north);
                drawSingleAreaBounds(g2, mapRect, -180d, south, east, north);
            }
        }

        private void drawSingleAreaBounds(Graphics2D g2, Rectangle mapRect, double west, double south, double east, double north) {
            int rx1 = lonToX(west, mapRect.x, mapRect.width);
            int rx2 = lonToX(east, mapRect.x, mapRect.width);
            int ry1 = latToY(north, mapRect.y, mapRect.height);
            int ry2 = latToY(south, mapRect.y, mapRect.height);
            int left = Math.min(rx1, rx2);
            int top = Math.min(ry1, ry2);
            int rw = Math.max(8, Math.abs(rx2 - rx1));
            int rh = Math.max(8, Math.abs(ry2 - ry1));

            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(37, 99, 235, 64));
                copy.fillRoundRect(left, top, rw, rh, 10, 10);
                copy.setColor(new Color(29, 78, 216, 230));
                copy.setStroke(new BasicStroke(2.4f));
                copy.drawRoundRect(left, top, rw, rh, 10, 10);

                int cx = left + rw / 2;
                int cy = top + rh / 2;
                copy.setColor(new Color(220, 38, 38));
                copy.fillOval(cx - 4, cy - 4, 8, 8);
                copy.setStroke(new BasicStroke(1.4f));
                copy.drawOval(cx - 8, cy - 8, 16, 16);
            } finally {
                copy.dispose();
            }
        }

        private boolean drawPlanisphereImage(Graphics2D g2, Rectangle mapRect) {
            BufferedImage image = getPlanisphereImage();
            if (image == null) {
                return false;
            }

            g2.drawImage(image, mapRect.x, mapRect.y, mapRect.width, mapRect.height, null);
            g2.setColor(new Color(148, 163, 184, 130));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(mapRect.x, mapRect.y, mapRect.width, mapRect.height);
            return true;
        }

        private BufferedImage getPlanisphereImage() {
            if (cachedPlanisphere != null) {
                return cachedPlanisphere;
            }
            if (planisphereLoadAttempted) {
                return null;
            }
            synchronized (AreaPreviewPanel.class) {
                if (cachedPlanisphere != null) {
                    return cachedPlanisphere;
                }
                if (planisphereLoadAttempted) {
                    return null;
                }
                planisphereLoadAttempted = true;
                for (String resource : PLANISPHERE_IMAGE_RESOURCES) {
                    try (InputStream in = CRSSelectorDialog.class.getClassLoader().getResourceAsStream(resource)) {
                        if (in != null) {
                            BufferedImage image = ImageIO.read(in);
                            if (image != null) {
                                cachedPlanisphere = image;
                                return cachedPlanisphere;
                            }
                        }
                    } catch (Exception ignored) {
                        // Intenta con la siguiente ruta.
                    }
                }
                cachedPlanisphere = loadPlanisphereFromSourceTree();
                return cachedPlanisphere;
            }
        }

        private BufferedImage loadPlanisphereFromSourceTree() {
            String[] paths = new String[]{
                    "src/main/resources/crs/world-planisphere.png",
                    "src/help/assets/world-planisphere.png",
                    "src/help/assets/world-planisphere.jpg"
            };
            for (String path : paths) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        return ImageIO.read(file);
                    }
                } catch (Exception ignored) {
                    // Sin planisferio desde esta ruta; intenta con la siguiente.
                }
            }
            return null;
        }

        private int lonToX(double lon, int x, int width) {
            double clamped = Math.max(-180d, Math.min(180d, lon));
            return x + (int) Math.round(((clamped + 180d) / 360d) * width);
        }

        private int latToY(double lat, int y, int height) {
            double clamped = Math.max(-90d, Math.min(90d, lat));
            return y + (int) Math.round(((90d - clamped) / 180d) * height);
        }

        private void drawPlanisphere(Graphics2D g2, int x, int y, int width, int height) {
            double[][][] landMasses = new double[][][]{
                    {
                            {-168, 71}, {-158, 58}, {-145, 60}, {-132, 54}, {-126, 49}, {-124, 43},
                            {-117, 34}, {-107, 28}, {-99, 22}, {-90, 20}, {-84, 15}, {-82, 9},
                            {-78, 9}, {-75, 18}, {-80, 26}, {-81, 31}, {-76, 36}, {-73, 43},
                            {-64, 47}, {-58, 54}, {-66, 59}, {-82, 63}, {-94, 69}, {-112, 73},
                            {-132, 72}, {-150, 71}
                    },
                    {
                            {-54, 82}, {-38, 83}, {-23, 79}, {-20, 72}, {-28, 64}, {-44, 60},
                            {-58, 66}, {-63, 75}
                    },
                    {
                            {-82, 12}, {-76, 8}, {-72, 2}, {-79, -6}, {-75, -14}, {-70, -20},
                            {-71, -30}, {-66, -39}, {-70, -50}, {-66, -55}, {-58, -52},
                            {-52, -44}, {-49, -35}, {-43, -22}, {-39, -10}, {-45, 1},
                            {-55, 6}, {-66, 10}
                    },
                    {
                            {-11, 72}, {4, 65}, {9, 58}, {18, 56}, {25, 61}, {36, 58},
                            {50, 57}, {67, 61}, {83, 58}, {101, 61}, {123, 58}, {145, 51},
                            {166, 47}, {172, 42}, {162, 35}, {145, 36}, {132, 40}, {124, 34},
                            {113, 30}, {104, 22}, {105, 12}, {113, 3}, {105, -6}, {97, 6},
                            {88, 19}, {78, 8}, {69, 22}, {58, 26}, {47, 30}, {40, 27},
                            {34, 31}, {29, 37}, {20, 41}, {12, 45}, {4, 50}, {-3, 51},
                            {-8, 58}
                    },
                    {
                            {-17, 36}, {-5, 37}, {9, 37}, {23, 32}, {32, 24}, {42, 12},
                            {50, 2}, {43, -10}, {36, -22}, {28, -34}, {18, -35}, {8, -30},
                            {2, -20}, {-7, -4}, {-15, 12}
                    },
                    {
                            {112, -11}, {124, -17}, {139, -16}, {153, -27}, {150, -37},
                            {137, -43}, {121, -38}, {113, -25}
                    },
                    {
                            {47, -13}, {51, -20}, {48, -27}, {44, -24}, {44, -17}
                    },
                    {
                            {138, 45}, {143, 39}, {141, 34}, {135, 32}, {130, 34}, {132, 40}
                    },
                    {
                            {-8, 58}, {-3, 55}, {-5, 50}, {-10, 51}, {-12, 56}
                    },
                    {
                            {166, -34}, {178, -38}, {174, -45}, {166, -47}, {160, -42}
                    }
            };

            Graphics2D copy = (Graphics2D) g2.create();
            try {
                copy.setColor(new Color(255, 252, 242, 245));
                for (double[][] land : landMasses) {
                    fillContinent(copy, x, y, width, height, land);
                }
                copy.setColor(new Color(18, 24, 38, 230));
                copy.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (double[][] land : landMasses) {
                    drawContinentOutline(copy, x, y, width, height, land);
                }
            } finally {
                copy.dispose();
            }

            g2.setColor(new Color(29, 78, 216, 210));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            g2.drawString(I18n.t("Planisferio equirectangular"), x + width - 166, y + height - 14);
        }

        private void fillContinent(Graphics2D g2, int x, int y, int width, int height, double[][] points) {
            Path2D.Double path = new Path2D.Double();
            for (int i = 0; i < points.length; i++) {
                double lon = points[i][0];
                double lat = points[i][1];
                double px = lonToX(lon, x, width);
                double py = latToY(lat, y, height);
                if (i == 0) {
                    path.moveTo(px, py);
                } else {
                    path.lineTo(px, py);
                }
            }
            path.closePath();
            g2.fill(path);
        }

        private void drawContinentOutline(Graphics2D g2, int x, int y, int width, int height, double[][] points) {
            Path2D.Double path = new Path2D.Double();
            for (int i = 0; i < points.length; i++) {
                double lon = points[i][0];
                double lat = points[i][1];
                double px = lonToX(lon, x, width);
                double py = latToY(lat, y, height);
                if (i == 0) {
                    path.moveTo(px, py);
                } else {
                    path.lineTo(px, py);
                }
            }
            path.closePath();
            g2.draw(path);
        }
    }
}
