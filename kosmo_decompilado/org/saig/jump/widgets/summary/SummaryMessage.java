/*
 * Decompiled with CFR 0.152.
 */
package org.saig.jump.widgets.summary;

import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import javax.swing.Icon;

public class SummaryMessage {
    private String basicMessage;
    private String extendedMessage;
    private int messageType = -1;
    public static final int INFORMATION_MESSAGE = 0;
    public static final int WARNING_MESSAGE = 1;
    public static final int ERROR_MESSAGE = 2;
    private static final Icon INFORMATION_ICON = IconLoader.icon("info_msg.gif");
    private static final Icon WARNING_ICON = IconLoader.icon("warn_msg.gif");
    private static final Icon ERROR_ICON = IconLoader.icon("error_msg.gif");
    private static final Icon DEFAULT_ICON = IconLoader.icon("default_msg.gif");
    public static final Icon EXTENDED_MESSAGE_ICON = IconLoader.icon("extended_msg.gif");

    public SummaryMessage(String basicMessage, String extendedMessage) {
        this.basicMessage = basicMessage;
        this.extendedMessage = extendedMessage;
    }

    public SummaryMessage(String basicMessage, String extendedMessage, int messageType) {
        this.basicMessage = basicMessage;
        this.extendedMessage = extendedMessage;
        this.messageType = messageType;
    }

    public String getBasicMessage() {
        return this.basicMessage;
    }

    public void setBasicMessage(String basicMessage) {
        this.basicMessage = basicMessage;
    }

    public String getExtendedMessage() {
        return this.extendedMessage;
    }

    public void setExtendedMessage(String extendedMessage) {
        this.extendedMessage = extendedMessage;
    }

    public Icon getMessageIcon() {
        switch (this.messageType) {
            case 0: {
                return INFORMATION_ICON;
            }
            case 1: {
                return WARNING_ICON;
            }
            case 2: {
                return ERROR_ICON;
            }
        }
        return DEFAULT_ICON;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }
}

