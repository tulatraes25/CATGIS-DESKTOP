/*
 * Decompiled with CFR 0.152.
 */
package es.kosmo.desktop.widgets.symbology.sldeditor.property.std;

import com.vividsolutions.jump.workbench.ui.OKCancelPanel;
import es.kosmo.core.styling.Gradient;
import es.kosmo.core.styling.LinearGradientImpl;
import es.kosmo.core.styling.RadialGradientImpl;
import es.kosmo.desktop.widgets.symbology.sldeditor.property.GradientFillEditor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXGradientChooser;
import org.jdesktop.swingx.color.GradientPreviewPanel;
import org.saig.core.renderer.LegendIconMaker;
import org.saig.core.styling.Fill;
import org.saig.core.styling.PolygonSymbolizer;
import org.saig.core.styling.Symbolizer;
import org.saig.jump.lang.I18N;

public class DefaultGradientFillEditor
extends GradientFillEditor {
    private static final long serialVersionUID = 1L;
    private JButton openChooserButton;
    private JLabel exampleLabel;
    private Dimension iconDimension;
    private Gradient gradient;

    public DefaultGradientFillEditor(Dimension d) {
        this(d, styleFactory.createLinearGradient(50.0f, 50.0f, 100.0f, 100.0f, new float[]{0.0f, 1.0f}, new Color[]{Color.WHITE, Color.BLACK}, Gradient.DEFAULT_CYCLE_METHOD));
    }

    public DefaultGradientFillEditor(Dimension d, Gradient g) {
        this.setLayout(new GridBagLayout());
        this.setAlignmentX(0.0f);
        this.iconDimension = d;
        this.setPreferredSize(this.iconDimension);
        this.setMinimumSize(this.iconDimension);
        this.exampleLabel = new JLabel();
        this.exampleLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.openChooserButton = new JButton("...");
        this.openChooserButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent event) {
                GradientDialog dialog = new GradientDialog(DefaultGradientFillEditor.this, DefaultGradientFillEditor.this.gradient);
                dialog.setVisible(true);
                if (dialog.wasOkPressed()) {
                    DefaultGradientFillEditor.this.setGradient(dialog.getGradient());
                }
            }
        });
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.exampleLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        this.add((Component)this.openChooserButton, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.fill = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        this.add((Component)new JLabel(), gridBagConstraints);
        this.setGradient(g);
    }

    @Override
    public void setGradient(Gradient g) {
        Fill fill = styleBuilder.createFill();
        fill.setGradientFill(g);
        PolygonSymbolizer symbolizer = styleBuilder.createPolygonSymbolizer(styleBuilder.createStroke(), fill);
        Icon graphicIcon = LegendIconMaker.makeLegendIcon(this.iconDimension.width, this.iconDimension.width, Color.WHITE, new Symbolizer[]{symbolizer}, null, true);
        this.exampleLabel.setIcon(graphicIcon);
        this.gradient = g;
    }

    @Override
    public Gradient getGradient() {
        return this.gradient;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.exampleLabel.setEnabled(enabled);
        this.openChooserButton.setEnabled(enabled);
    }

    private class GradientDialog
    extends JDialog
    implements ActionListener {
        private static final long serialVersionUID = 1L;
        private JXGradientChooser chooser;
        private OKCancelPanel okCancelPanel;

        public GradientDialog(Component parent, Gradient gr) {
            super(JOptionPane.getFrameForComponent(parent), true);
            this.setTitle(I18N.getString("es.kosmo.desktop.widgets.symbology.sldeditor.property.std.DefaultGradientFillEditor.Configure-gradient"));
            JPanel content = new JPanel(new BorderLayout());
            this.chooser = new JXGradientChooser();
            Dimension d = new Dimension(250, 250);
            this.resizeComponents((Container)((Object)this.chooser), d);
            this.chooser.setGradient(this.fromGradient(gr));
            this.okCancelPanel = new OKCancelPanel();
            this.okCancelPanel.addActionListener(this);
            content.add((Component)((Object)this.chooser), "Center");
            content.add((Component)this.okCancelPanel, "South");
            this.setContentPane(content);
            this.pack();
            this.setLocationRelativeTo(parent);
        }

        public boolean wasOkPressed() {
            return this.okCancelPanel.wasOKPressed();
        }

        public Gradient getGradient() {
            MultipleGradientPaint paint = this.chooser.getGradient();
            return this.toGradient(paint);
        }

        private MultipleGradientPaint fromGradient(Gradient gr) {
            MultipleGradientPaint p;
            switch (gr.getType()) {
                case RADIAL: {
                    RadialGradientImpl rg = (RadialGradientImpl)gr;
                    p = new RadialGradientPaint(rg.getCenterX(), rg.getCenterY(), rg.getRadius(), rg.getFractions(), rg.getColors(), this.convertToCycleMethod(gr.getCycleMethod()));
                    break;
                }
                default: {
                    LinearGradientImpl lg = (LinearGradientImpl)gr;
                    p = new LinearGradientPaint(lg.getStartX(), lg.getStartY(), lg.getEndX(), lg.getEndY(), lg.getFractions(), lg.getColors(), this.convertToCycleMethod(gr.getCycleMethod()));
                }
            }
            return p;
        }

        private MultipleGradientPaint.CycleMethod convertToCycleMethod(Gradient.GradientCycleMethod cycleMethod) {
            MultipleGradientPaint.CycleMethod c;
            switch (cycleMethod) {
                case REFLECT: {
                    c = MultipleGradientPaint.CycleMethod.REFLECT;
                    break;
                }
                case REPEAT: {
                    c = MultipleGradientPaint.CycleMethod.REPEAT;
                    break;
                }
                default: {
                    c = MultipleGradientPaint.CycleMethod.NO_CYCLE;
                }
            }
            return c;
        }

        private Gradient toGradient(MultipleGradientPaint paint) {
            Gradient gr = null;
            if (paint instanceof RadialGradientPaint) {
                RadialGradientPaint rgp = (RadialGradientPaint)paint;
                gr = styleFactory.createRadialGradient((float)rgp.getCenterPoint().getX(), (float)rgp.getCenterPoint().getY(), rgp.getRadius(), rgp.getFractions(), rgp.getColors(), this.getCycleMethod(rgp.getCycleMethod()));
            } else if (paint instanceof LinearGradientPaint) {
                LinearGradientPaint lgp = (LinearGradientPaint)paint;
                gr = styleFactory.createLinearGradient((float)lgp.getStartPoint().getX(), (float)lgp.getStartPoint().getY(), (float)lgp.getEndPoint().getX(), (float)lgp.getEndPoint().getY(), lgp.getFractions(), lgp.getColors(), this.getCycleMethod(lgp.getCycleMethod()));
            }
            return gr;
        }

        private Gradient.GradientCycleMethod getCycleMethod(MultipleGradientPaint.CycleMethod cycleMethod) {
            Gradient.GradientCycleMethod m;
            switch (cycleMethod) {
                case REFLECT: {
                    m = Gradient.GradientCycleMethod.REFLECT;
                    break;
                }
                case REPEAT: {
                    m = Gradient.GradientCycleMethod.REPEAT;
                    break;
                }
                default: {
                    m = Gradient.GradientCycleMethod.NONE;
                }
            }
            return m;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.setVisible(false);
        }

        @Override
        public void setVisible(boolean visible) {
            if (visible) {
                this.okCancelPanel.setOKPressed(false);
            }
            super.setVisible(visible);
        }

        public void resizeComponents(Container c, Dimension d) {
            Component[] componentArray = c.getComponents();
            int n = componentArray.length;
            int n2 = 0;
            while (n2 < n) {
                Component comp = componentArray[n2];
                if (comp instanceof GradientPreviewPanel) {
                    comp.setPreferredSize(d);
                    comp.setMinimumSize(d);
                    comp.setMaximumSize(d);
                } else if (comp instanceof Container) {
                    this.resizeComponents((Container)comp, d);
                }
                ++n2;
            }
        }
    }
}

