/*
 * Decompiled with CFR 0.152.
 */
package com.vividsolutions.jump.workbench.datasource;

import com.vividsolutions.jump.io.datasource.DataSource;
import com.vividsolutions.jump.io.datasource.DataSourceQuery;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.CollectionUtil;
import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.util.LangUtil;
import com.vividsolutions.jump.workbench.datasource.DataSourceQueryChooser;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;
import org.saig.core.gui.swing.sldeditor.util.FormUtils;

public abstract class FileDataSourceQueryChooser
implements DataSourceQueryChooser {
    protected String description;
    protected Class<?> dataSourceClass;
    protected FileFilter fileFilter;
    protected JPanel southComponent1 = new JPanel();
    protected JPanel southComponent2 = new JPanel();
    protected String[] extensions;

    public FileDataSourceQueryChooser(Class<?> dataSourceClass, String description, String[] extensions) {
        this.dataSourceClass = dataSourceClass;
        this.description = description;
        this.extensions = extensions;
        this.fileFilter = GUIUtil.createFileFilter(description, extensions);
    }

    @Override
    public String toString() {
        return this.description;
    }

    @Override
    public boolean isInputValid() {
        final Boolean[] actionPerformed = new Boolean[]{Boolean.FALSE};
        ActionListener listener = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                actionPerformed[0] = Boolean.TRUE;
            }
        };
        this.getFileChooserPanel().getChooser().addActionListener(listener);
        try {
            if (this.getFileChooserPanel().getChooser().getUI() instanceof BasicFileChooserUI) {
                BasicFileChooserUI ui = (BasicFileChooserUI)this.getFileChooserPanel().getChooser().getUI();
                ui.getApproveSelectionAction().actionPerformed(null);
            }
        }
        finally {
            this.getFileChooserPanel().getChooser().removeActionListener(listener);
        }
        return actionPerformed[0] == Boolean.TRUE;
    }

    @Override
    public Collection<DataSourceQuery> getDataSourceQueries() {
        ArrayList<DataSourceQuery> queries = new ArrayList<DataSourceQuery>();
        File[] files = GUIUtil.selectedFiles(this.getFileChooserPanel().getChooser());
        boolean addExtensionIfNone = this.extensions != null && this.extensions.length == 1;
        int i = 0;
        while (i < files.length) {
            File currentFile = files[i];
            if (addExtensionIfNone) {
                currentFile = FileUtil.addExtensionIfNone(currentFile, this.extensions[0]);
            }
            queries.addAll(this.toDataSourceQueries(currentFile));
            ++i;
        }
        return queries;
    }

    protected Collection<DataSourceQuery> toDataSourceQueries(File file) {
        return Collections.singleton(this.toDataSourceQuery(file));
    }

    protected abstract FileChooserPanel getFileChooserPanel();

    @Override
    public Component getComponent() {
        this.setFileFilters();
        if (this.getFileChooserPanel().getSouthComponent1() != this.getSouthComponent1()) {
            this.getFileChooserPanel().setSouthComponent1(this.getSouthComponent1());
        }
        if (this.getFileChooserPanel().getSouthComponent2() != this.getSouthComponent2()) {
            this.getFileChooserPanel().setSouthComponent2(this.getSouthComponent2());
        }
        this.getFileChooserPanel().revalidate();
        this.getFileChooserPanel().repaint();
        return this.getFileChooserPanel();
    }

    private void setFileFilters() {
        Object[] filters = this.getFileChooserPanel().getChooser().getChoosableFileFilters();
        if (!CollectionUtil.containsReference(filters, this.getFileFilter())) {
            GUIUtil.removeChoosableFileFilters(this.getFileChooserPanel().getChooser());
            this.addFileFilters(this.getFileChooserPanel().getChooser());
            this.getFileChooserPanel().getChooser().setFileFilter(this.getFileFilter());
        }
    }

    protected void addFileFilters(JFileChooser chooser) {
        chooser.addChoosableFileFilter(this.getFileFilter());
        chooser.addChoosableFileFilter(chooser.getAcceptAllFileFilter());
    }

    protected DataSourceQuery toDataSourceQuery(File file) {
        DataSource dataSource = (DataSource)LangUtil.newInstance(this.dataSourceClass);
        dataSource.setProperties(this.toProperties(file));
        return new DataSourceQuery(dataSource, (String)dataSource.getProperties().get("File"), GUIUtil.nameWithoutPathAndExtension(file));
    }

    protected Map<String, Object> toProperties(File file) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("File", FileUtil.convertPathToSystemIndependentPath(file.getPath()));
        return properties;
    }

    private FileFilter getFileFilter() {
        return this.fileFilter;
    }

    protected Component getSouthComponent1() {
        return this.southComponent1;
    }

    protected Component getSouthComponent2() {
        return this.southComponent2;
    }

    public void refreshPath() {
    }

    public static class FileChooserPanel
    extends JPanel {
        private static final long serialVersionUID = 1L;
        private JFileChooser chooser;
        private Component southComponent1;
        private Component southComponent2;
        private JPanel southComponent1Container = new JPanel(new BorderLayout());
        private JPanel southComponent2Container = new JPanel(new BorderLayout());

        public FileChooserPanel(JFileChooser chooser, Blackboard blackboard) {
            this.setLayout(new BorderLayout());
            this.chooser = chooser;
            this.chooser.setMinimumSize(new Dimension(500, 450));
            this.chooser.setPreferredSize(new Dimension(500, 450));
            JPanel southPanel = new JPanel(new GridBagLayout());
            FormUtils.addRowInGBL(southPanel, 0, 0, this.southComponent1Container);
            FormUtils.addRowInGBL(southPanel, 1, 0, this.southComponent2Container);
            this.add((Component)chooser, "Center");
            this.add((Component)southPanel, "South");
            this.setSouthComponent1(new JPanel());
            this.setSouthComponent2(new JPanel());
        }

        private void setSouthComponent1(Component southComponent1) {
            this.southComponent1Container.removeAll();
            this.southComponent1 = southComponent1;
            this.southComponent1Container.add(southComponent1, "Center");
        }

        private void setSouthComponent2(Component southComponent2) {
            this.southComponent2Container.removeAll();
            this.southComponent2 = southComponent2;
            this.southComponent2Container.add(southComponent2, "Center");
        }

        public JFileChooser getChooser() {
            return this.chooser;
        }

        private Component getSouthComponent1() {
            return this.southComponent1;
        }

        private Component getSouthComponent2() {
            return this.southComponent2;
        }
    }
}

