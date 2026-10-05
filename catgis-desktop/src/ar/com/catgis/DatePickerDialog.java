package ar.com.catgis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

public class DatePickerDialog extends JDialog {

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysPanel = new JPanel(new GridLayout(0, 7, 4, 4));

    private DatePickerDialog(Window owner, Date initialDate) {
        super(owner, "Seleccionar fecha", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        LocalDate startDate = initialDate != null
                ? initialDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        this.selectedDate = startDate;
        this.currentMonth = YearMonth.from(startDate);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCalendarPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        refreshCalendar();

        setSize(360, 320);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public static Date showDialog(Component parent, Date initialDate) {
        Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        DatePickerDialog dialog = new DatePickerDialog(owner, initialDate);
        dialog.setVisible(true);

        if (dialog.selectedDate == null) {
            return null;
        }

        return Date.from(dialog.selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));

        JButton prevButton = new JButton("<");
        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 14f));

        panel.add(prevButton, BorderLayout.WEST);
        panel.add(monthLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildCalendarPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));

        JPanel header = new JPanel(new GridLayout(1, 7, 4, 4));
        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        }) {
            JLabel lbl = new JLabel(day.getDisplayName(TextStyle.SHORT, new Locale("es", "AR")), SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
            header.add(lbl);
        }

        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(daysPanel, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton todayButton = new JButton("Hoy");
        todayButton.addActionListener(e -> {
            selectedDate = LocalDate.now();
            currentMonth = YearMonth.from(selectedDate);
            refreshCalendar();
        });

        JButton clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> {
            selectedDate = null;
            dispose();
        });

        JButton okButton = new JButton("Aceptar");
        okButton.addActionListener(e -> dispose());

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(e -> dispose());

        panel.add(todayButton);
        panel.add(clearButton);
        panel.add(okButton);
        panel.add(cancelButton);

        return panel;
    }

    private void refreshCalendar() {
        monthLabel.setText(capitalize(currentMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "AR")))
                + " " + currentMonth.getYear());

        daysPanel.removeAll();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int firstDayColumn = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i < firstDayColumn; i++) {
            daysPanel.add(new JLabel(""));
        }

        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setMargin(new Insets(2, 2, 2, 2));
            dayButton.setFocusPainted(false);

            if (selectedDate != null && selectedDate.equals(date)) {
                dayButton.setBackground(new Color(33, 120, 210));
                dayButton.setForeground(Color.WHITE);
                dayButton.setOpaque(true);
            }

            if (date.equals(LocalDate.now())) {
                dayButton.setBorder(BorderFactory.createLineBorder(new Color(33, 120, 210)));
            }

            dayButton.addActionListener(e -> {
                selectedDate = date;
                dispose();
            });

            daysPanel.add(dayButton);
        }

        int totalCells = firstDayColumn - 1 + daysInMonth;
        int remainder = totalCells % 7;
        if (remainder != 0) {
            for (int i = remainder; i < 7; i++) {
                daysPanel.add(new JLabel(""));
            }
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase(new Locale("es", "AR")) + s.substring(1);
    }
}
