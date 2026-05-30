/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.l2fprod.common.swing.JTipOfTheDay
 *  com.l2fprod.common.swing.JTipOfTheDay$ShowOnStartupChoice
 *  com.l2fprod.common.swing.TipModel
 *  com.l2fprod.common.swing.TipModel$Tip
 *  com.l2fprod.common.swing.tips.DefaultTip
 *  com.l2fprod.common.swing.tips.DefaultTipModel
 */
package es.kosmo.desktop.widgets.utils;

import com.l2fprod.common.swing.JTipOfTheDay;
import com.l2fprod.common.swing.TipModel;
import com.l2fprod.common.swing.tips.DefaultTip;
import com.l2fprod.common.swing.tips.DefaultTipModel;
import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.UIManager;
import org.saig.jump.lang.I18N;

public class BasicTipOfTheDay
extends JTipOfTheDay {
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_TITLE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.tip-of-the-day");
    private static final String DIALOG_MESSAGE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.do-you-know");
    private static final String SHOW_ON_STARTUP_MESSAGE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.show-on-startup");
    private static final String PREVIOUS_TIP_MESSAGE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.next-tip");
    private static final String NEXT_TIP_MESSAGE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.previous-tip");
    private static final String CLOSE_MESSAGE = I18N.getString("org.saig.jump.widgets.utils.tipoftheday.BasicTipOfTheDay.close");
    protected boolean showOnStartup = true;

    static {
        UIManager.put("TipOfTheDay.didYouKnowText", DIALOG_MESSAGE);
        UIManager.put("TipOfTheDay.showOnStartupText", SHOW_ON_STARTUP_MESSAGE);
        UIManager.put("TipOfTheDay.previousTipText", PREVIOUS_TIP_MESSAGE);
        UIManager.put("TipOfTheDay.nextTipText", NEXT_TIP_MESSAGE);
        UIManager.put("TipOfTheDay.closeText", CLOSE_MESSAGE);
    }

    public BasicTipOfTheDay() {
        this.setModel(this.loadModel());
    }

    private TipModel loadModel() {
        DefaultTip[] tips = new DefaultTip[3];
        DefaultTip tip1 = new DefaultTip("Tip1", (Object)"Tip 1 de prueba");
        DefaultTip tip2 = new DefaultTip("Tip2", (Object)"Tip 2 de prueba");
        DefaultTip tip3 = new DefaultTip("Tip3", (Object)"Tip 3 de prueba");
        tips[0] = tip1;
        tips[1] = tip2;
        tips[2] = tip3;
        DefaultTipModel model = new DefaultTipModel((TipModel.Tip[])tips);
        return model;
    }

    public boolean isShowOnStartup() {
        return this.showOnStartup;
    }

    public void setShowOnStartup(boolean showOnStartup) {
        this.showOnStartup = showOnStartup;
    }

    public JDialog buildDialog(Component component, JTipOfTheDay.ShowOnStartupChoice choice) {
        JDialog dialog = this.createDialog(component, choice);
        dialog.setTitle(DEFAULT_TITLE);
        return dialog;
    }
}

