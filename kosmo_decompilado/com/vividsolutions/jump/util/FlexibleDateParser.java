/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vividsolutions.jts.util.Assert
 */
package com.vividsolutions.jump.util;

import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.util.FileUtil;
import java.awt.Color;
import java.awt.Component;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import org.saig.jump.lang.I18N;

public class FlexibleDateParser {
    private static Collection<SimpleDateFormat> lenientFormatters = null;
    private static Collection<SimpleDateFormat> unlenientFormatters = null;
    private boolean verbose = false;

    private Collection<Pattern> sortByComplexity(Collection<Pattern> patterns) {
        TreeSet<Pattern> sortedPatterns = new TreeSet<Pattern>(new Comparator<Pattern>(){
            private TreeSet<String> uniqueCharacters = new TreeSet();

            @Override
            public int compare(Pattern o1, Pattern o2) {
                int result = this.complexity(o1.toString()) - this.complexity(o2.toString());
                if (result == 0) {
                    result = o1.index - o2.index;
                }
                return result;
            }

            private int complexity(String pattern) {
                this.uniqueCharacters.clear();
                int i = 0;
                while (i < pattern.length()) {
                    if (("" + pattern.charAt(i)).trim().length() > 0) {
                        this.uniqueCharacters.add("" + pattern.charAt(i));
                    }
                    ++i;
                }
                return this.uniqueCharacters.size();
            }
        });
        sortedPatterns.addAll(patterns);
        return sortedPatterns;
    }

    private Collection<SimpleDateFormat> lenientFormatters() {
        if (lenientFormatters == null) {
            this.load();
        }
        return lenientFormatters;
    }

    private Collection<SimpleDateFormat> unlenientFormatters() {
        if (unlenientFormatters == null) {
            this.load();
        }
        return unlenientFormatters;
    }

    public Date parse(String s, boolean lenient) throws ParseException {
        if (s.trim().length() == 0) {
            return null;
        }
        try {
            if (this.verbose) {
                System.out.println(String.valueOf(s) + " -- Date constructor");
            }
            return new Date(s);
        }
        catch (Exception exception) {
            try {
                return this.parse(s, this.unlenientFormatters());
            }
            catch (ParseException e) {
                if (lenient) {
                    return this.parse(s, this.lenientFormatters());
                }
                throw e;
            }
        }
    }

    private Date parse(String s, Collection<SimpleDateFormat> formatters) throws ParseException {
        ParseException firstParseException = null;
        for (SimpleDateFormat formatter : formatters) {
            if (this.verbose) {
                System.out.println(String.valueOf(s) + " -- " + formatter.toPattern() + (formatter.isLenient() ? "lenient" : ""));
            }
            try {
                return this.parse(s, formatter);
            }
            catch (ParseException e) {
                if (firstParseException != null) continue;
                firstParseException = e;
            }
        }
        throw firstParseException;
    }

    private Date parse(String s, SimpleDateFormat formatter) throws ParseException {
        ParsePosition pos = new ParsePosition(0);
        Date date = formatter.parse(s, pos);
        if (pos.getIndex() == 0) {
            throw new ParseException(String.valueOf(I18N.getString("com.vividsolutions.jump.util.FlexibleDateParser.unparseable-date")) + ": \"" + s + "\"", pos.getErrorIndex());
        }
        if (pos.getIndex() != s.length()) {
            throw new ParseException(String.valueOf(I18N.getString("com.vividsolutions.jump.util.FlexibleDateParser.unparseable-date")) + ": \"" + s + "\"", pos.getErrorIndex());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(1) == 1970 && s.indexOf("70") == -1) {
            calendar.set(1, Calendar.getInstance().get(1));
        }
        return calendar.getTime();
    }

    private void load() {
        if (lenientFormatters == null) {
            InputStream inputStream = this.getClass().getResourceAsStream("FlexibleDateParser.txt");
            try {
                try {
                    ArrayList<Pattern> patterns = new ArrayList<Pattern>();
                    int index = 0;
                    Iterator<String> i = FileUtil.getContents(inputStream).iterator();
                    while (i.hasNext()) {
                        String line = i.next().trim();
                        if (line.startsWith("#") || line.length() == 0) continue;
                        patterns.add(new Pattern(line, index));
                        ++index;
                    }
                    unlenientFormatters = this.toFormatters(false, patterns);
                    lenientFormatters = this.toFormatters(true, patterns);
                }
                finally {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                Assert.shouldNeverReachHere((String)e.toString());
            }
        }
    }

    private Collection<SimpleDateFormat> toFormatters(boolean lenient, Collection<Pattern> patterns) {
        ArrayList<SimpleDateFormat> formatters = new ArrayList<SimpleDateFormat>();
        for (Pattern pattern : this.sortByComplexity(patterns)) {
            SimpleDateFormat formatter = new SimpleDateFormat(pattern.pattern);
            formatter.setLenient(lenient);
            formatters.add(formatter);
        }
        return formatters;
    }

    public void setVerbose(boolean b) {
        this.verbose = b;
    }

    public static final class CellEditor
    extends DefaultCellEditor {
        private static final long serialVersionUID = 1L;
        private Object value;
        private FlexibleDateParser parser = new FlexibleDateParser();
        private DateFormat formatter = DateFormat.getDateInstance();

        public CellEditor() {
            super(new JTextField());
        }

        @Override
        public boolean stopCellEditing() {
            try {
                this.value = this.parser.parse((String)super.getCellEditorValue(), true);
            }
            catch (Exception e) {
                ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.value = null;
            ((JComponent)this.getComponent()).setBorder(new LineBorder(Color.black));
            return super.getTableCellEditorComponent(table, this.format((Date)value), isSelected, row, column);
        }

        private String format(Date date) {
            return date == null ? "" : this.formatter.format(date);
        }

        @Override
        public Object getCellEditorValue() {
            return this.value;
        }
    }

    private static class Pattern {
        private String pattern;
        private int index;

        public Pattern(String pattern, int index) {
            this.pattern = pattern;
            this.index = index;
        }

        public String toString() {
            return this.pattern;
        }
    }
}

