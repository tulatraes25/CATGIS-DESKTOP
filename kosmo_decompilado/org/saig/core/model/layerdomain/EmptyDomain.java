/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.log4j.Logger
 *  org.dom4j.Element
 *  org.dom4j.tree.DefaultElement
 */
package org.saig.core.model.layerdomain;

import es.kosmo.desktop.utils.Dom4JUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.saig.core.model.layerdomain.Domain;
import org.saig.core.model.layerdomain.actions.Action;
import org.saig.core.model.layerdomain.actions.ActionFactory;
import org.saig.core.model.layerdomain.managers.ActionManager;

public class EmptyDomain
implements Domain {
    private static final String VISIBLE_ATTR = "visible";
    private static final String GROUP_ATTR = "group";
    Logger LOGGER = Logger.getLogger(EmptyDomain.class);
    private boolean visible = true;
    private String group;
    private static final String ACTIONS_TAG = "Actions";
    protected List<Action> actions = new ArrayList<Action>();

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void addAction(Action action) {
        this.actions.add(action);
    }

    @Override
    public List<Action> getActions() {
        return this.actions;
    }

    public String getActionsXml() {
        DefaultElement element = new DefaultElement(ACTIONS_TAG);
        this.addActionsToActionsTag((Element)element);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Dom4JUtils.writeXml((Element)element, out);
        }
        catch (IOException e) {
            this.LOGGER.error((Object)"", (Throwable)e);
        }
        return out.toString();
    }

    public void setActionsXml(String actionsXml) {
        try {
            Element actionsElement = Dom4JUtils.readXml("UTF-8", actionsXml);
            this.readActionsTag(actionsElement);
        }
        catch (Exception e) {
            this.LOGGER.error((Object)"", (Throwable)e);
        }
    }

    @Override
    public boolean test(Object obj) {
        return false;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Element createSAXElement() {
        return null;
    }

    protected void addActionsToSAXElement(Element element) {
        element.addAttribute(VISIBLE_ATTR, this.isVisible() ? "true" : "false");
        element.addAttribute(GROUP_ATTR, this.getGroup());
        DefaultElement actionsElement = new DefaultElement(ACTIONS_TAG);
        this.addActionsToActionsTag((Element)actionsElement);
        element.add((Element)actionsElement);
    }

    private void addActionsToActionsTag(Element actionsElement) {
        for (Action action : this.actions) {
            actionsElement.add(action.createSAXElement());
        }
    }

    protected void readActionsFromSAXElement(Element element) {
        String attribute = element.attributeValue(VISIBLE_ATTR);
        if (attribute == null || attribute.equals("true")) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
        String group = element.attributeValue(GROUP_ATTR);
        if (group == null) {
            group = "";
        }
        this.setGroup(group);
        Element actionsElement = element.element(ACTIONS_TAG);
        if (actionsElement != null) {
            this.readActionsTag(actionsElement);
        }
    }

    private void readActionsTag(Element actionsElement) {
        List actions = actionsElement.elements();
        ActionManager aMan = ActionManager.getInstance();
        for (Element actionElement : actions) {
            Action action = null;
            ActionFactory actionFactory = aMan.getActionFactoryByTag(actionElement.getName());
            if (actionFactory != null) {
                action = actionFactory.createActionFromDom4jElement(actionElement);
            }
            if (action == null) continue;
            this.addAction(action);
        }
    }

    @Override
    public void fillFromElement(Element elem) {
    }

    @Override
    public void fireActions() {
        for (Action action : this.actions) {
            action.fireAction();
        }
    }
}

