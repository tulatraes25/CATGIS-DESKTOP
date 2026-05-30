/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.summary;

import com.vividsolutions.jump.util.FileUtil;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class SummaryDialog
extends JDialog
implements TreeSelectionListener {
    private static final long serialVersionUID = 1L;
    private Map<String, List<SummaryMessage>> messagesMap;
    private JPanel mainPanel;
    private JTabbedPane tabbedMainPanel;
    private JPanel buttonPanel;
    public static final String SUMMARY_TEXT_FILE_EXTENSION = "txt";
    public static final FileFilter SUMMARY_TEXT_FILE_FILTER = GUIUtil.createFileFilter(I18N.getString(SummaryDialog.class, "text-file"), new String[]{"txt"});
    private JFileChooser fileChooser = GUIUtil.createJFileChooserWithOverwritePrompting();

    public JPanel getButtonPanel() {
        if (this.buttonPanel == null) {
            this.buttonPanel = new JPanel();
            this.buttonPanel.setLayout(new FlowLayout());
            JButton closeButton = new JButton(I18N.getString("org.saig.jump.widgets.summary.SummaryDialog.close"));
            closeButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    SummaryDialog.this.dispose();
                }
            });
            JButton saveSummaryAsButton = new JButton(I18N.getString(this.getClass(), "save-summary-as"));
            saveSummaryAsButton.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        SummaryDialog.this.saveSummary();
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                }
            });
            this.buttonPanel.add(closeButton);
            this.buttonPanel.add(saveSummaryAsButton);
        }
        return this.buttonPanel;
    }

    protected void saveSummary() throws IOException {
        File textFile = null;
        if (this.fileChooser.showSaveDialog(this) != 0) {
            return;
        }
        textFile = this.fileChooser.getSelectedFile();
        textFile = FileUtil.addValidExtension(textFile, SUMMARY_TEXT_FILE_EXTENSION);
        StringBuffer messageBuffer = new StringBuffer();
        for (String messageKey : this.messagesMap.keySet()) {
            messageBuffer.append(String.valueOf(messageKey) + "\n\n");
            List<SummaryMessage> messages = this.messagesMap.get(messageKey);
            for (SummaryMessage currentMessage : messages) {
                messageBuffer.append(String.valueOf(currentMessage.getBasicMessage()) + "\n\n");
                messageBuffer.append(String.valueOf(currentMessage.getExtendedMessage()) + "\n\n\n");
            }
        }
        FileUtil.setContents(textFile.getAbsolutePath(), messageBuffer.toString());
    }

    public SummaryDialog(JFrame owner, boolean modal, String title, Map<String, List<SummaryMessage>> msMap) {
        super((Frame)owner, modal);
        this.setTitle(title);
        this.messagesMap = msMap;
        this.setSize(new Dimension(600, 400));
        this.initialize();
        GUIUtil.centreOnScreen(this);
    }

    private void initialize() {
        int numCategories = this.messagesMap.keySet().size();
        if (numCategories == 1) {
            this.initializeBasicSummaryDialog();
        } else {
            this.initializeMultiSummaryDialog();
        }
        this.fileChooser.setDialogTitle(this.getTitle());
        GUIUtil.removeChoosableFileFilters(this.fileChooser);
        this.fileChooser.addChoosableFileFilter(SUMMARY_TEXT_FILE_FILTER);
        this.fileChooser.setFileFilter(SUMMARY_TEXT_FILE_FILTER);
    }

    private void initializeBasicSummaryDialog() {
        this.getContentPane().add(this.getMainPanel());
    }

    private JPanel getMainPanel() {
        if (this.mainPanel == null) {
            this.mainPanel = new JPanel();
            this.mainPanel.setLayout(new BorderLayout());
            this.mainPanel.setSize(new Dimension(500, 300));
            String categoryName = this.messagesMap.keySet().iterator().next();
            List<SummaryMessage> messageList = this.messagesMap.get(categoryName);
            this.mainPanel.add((Component)this.buildCategoryPanel(categoryName, messageList, true), "Center");
            this.mainPanel.add((Component)this.getButtonPanel(), "South");
        }
        return this.mainPanel;
    }

    private void initializeMultiSummaryDialog() {
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add((Component)this.getTabbedMainPanel(), "Center");
        this.getContentPane().add((Component)this.getButtonPanel(), "South");
    }

    private JTabbedPane getTabbedMainPanel() {
        if (this.tabbedMainPanel == null) {
            this.tabbedMainPanel = new JTabbedPane();
            for (String categoryName : this.messagesMap.keySet()) {
                List<SummaryMessage> messageList = this.messagesMap.get(categoryName);
                this.tabbedMainPanel.addTab(categoryName, this.buildCategoryPanel(categoryName, messageList, false));
            }
        }
        return this.tabbedMainPanel;
    }

    private JPanel buildCategoryPanel(String categoryName, List<SummaryMessage> messageList, boolean titledBorder) {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setSize(new Dimension(500, 300));
        categoryPanel.setLayout(new BorderLayout());
        if (titledBorder) {
            categoryPanel.setBorder(BorderFactory.createTitledBorder(categoryName));
        } else {
            categoryPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        JTree messageTree = this.buildMessageTree(messageList);
        messageTree.setCellRenderer(new SummaryTreeCellRenderer());
        JScrollPane scrollPane = new JScrollPane(messageTree, 22, 30);
        JTextArea messageArea = new JTextArea();
        messageArea.setFont(new JLabel().getFont());
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setRows(8);
        messageArea.setEditable(false);
        JScrollPane messagePane = new JScrollPane(messageArea, 22, 30);
        categoryPanel.add((Component)scrollPane, "Center");
        categoryPanel.add((Component)messagePane, "South");
        return categoryPanel;
    }

    private JTree buildMessageTree(List<SummaryMessage> messageList) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(I18N.getString("org.saig.jump.widgets.summary.SummaryDialog.messages"));
        this.createNodes(rootNode, messageList);
        JTree messageTree = new JTree(rootNode);
        messageTree.setRootVisible(false);
        messageTree.setLargeModel(true);
        messageTree.getSelectionModel().setSelectionMode(1);
        messageTree.addTreeSelectionListener(this);
        return messageTree;
    }

    private void createNodes(DefaultMutableTreeNode rootNode, List<SummaryMessage> messageList) {
        for (SummaryMessage message : messageList) {
            DefaultMutableTreeNode basicNode = new DefaultMutableTreeNode(message);
            rootNode.add(basicNode);
        }
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        JTree treeSource = (JTree)e.getSource();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeSource.getLastSelectedPathComponent();
        Object userObject = node.getUserObject();
        if (userObject instanceof SummaryMessage) {
            SummaryMessage message = (SummaryMessage)userObject;
            JPanel treePanel = (JPanel)treeSource.getParent().getParent().getParent();
            JTextArea textArea = (JTextArea)((JViewport)((JScrollPane)treePanel.getComponent(1)).getComponent(0)).getComponent(0);
            textArea.setText(message.getExtendedMessage());
            textArea.moveCaretPosition(0);
        }
    }

    private class SummaryTreeCellRenderer
    extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if (userObject instanceof SummaryMessage) {
                SummaryMessage message = (SummaryMessage)userObject;
                this.setText("<HTML>" + message.getBasicMessage() + "</HTML>");
                this.setIcon(message.getMessageIcon());
            }
            return this;
        }
    }
}

