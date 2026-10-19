package ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * DatePickerDialog provides a lightweight, dependency-free calendar popup dialog in Java Swing.
 * 
 * Demonstrates:
 * - Custom JDialog: Modal popup dialog attached to parent frame.
 * - Layout Managers: BorderLayout, GridLayout (7x7 day grid), FlowLayout.
 * - Calendar & Date API: java.util.Calendar for date calculation and java.text.SimpleDateFormat for formatting.
 * - Event Handling: ActionListeners with lambdas for navigation and date selection.
 * - Standard Format: Consistently produces and accepts "dd-MM-yyyy" formatted dates.
 */
public class DatePickerDialog extends JDialog {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };
    private static final String[] DAY_NAMES = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

    private final Calendar calendar = Calendar.getInstance();
    private final JButton[] dayButtons = new JButton[42]; // 6 weeks x 7 days
    private JComboBox<String> cmbMonth;
    private JComboBox<Integer> cmbYear;
    private String selectedDate = null;
    private boolean isUpdating = false;

    public DatePickerDialog(Frame parent, String initialDateStr) {
        super(parent, "Select Date", true); // Modal dialog
        initializeCalendar(initialDateStr);
        initializeUI();
    }

    /**
     * Initializes the calendar state using the initial date string if valid;
     * otherwise defaults to the current system date.
     */
    private void initializeCalendar(String initialDateStr) {
        if (initialDateStr != null && !initialDateStr.trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            sdf.setLenient(false);
            try {
                Date parsedDate = sdf.parse(initialDateStr.trim());
                calendar.setTime(parsedDate);
                return;
            } catch (ParseException ignored) {
                // If invalid date string provided, fallback to current date
            }
        }
        calendar.setTime(new Date());
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setSize(360, 320);
        setResizable(false);
        setLocationRelativeTo(getParent());

        // 1. Top Navigation Bar (Month & Year selection)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 2. Center Calendar Grid (7x7 table of day headers + day buttons)
        JPanel calendarPanel = createCalendarGridPanel();
        add(calendarPanel, BorderLayout.CENTER);

        // 3. Bottom Control Bar (Today and Cancel buttons)
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // Populate days for initial month/year
        refreshCalendarGrid();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 8));
        panel.setBackground(new Color(245, 247, 250));

        JButton btnPrev = new JButton("<");
        btnPrev.setFont(new Font("Arial", Font.BOLD, 12));
        btnPrev.setToolTipText("Previous Month");
        btnPrev.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            syncComboBoxes();
            refreshCalendarGrid();
        });

        cmbMonth = new JComboBox<>(MONTH_NAMES);
        cmbMonth.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbMonth.setSelectedIndex(calendar.get(Calendar.MONTH));
        cmbMonth.addActionListener(e -> {
            if (!isUpdating) {
                calendar.set(Calendar.MONTH, cmbMonth.getSelectedIndex());
                refreshCalendarGrid();
            }
        });

        // Populate year range (from 2020 to 2040)
        Integer[] years = new Integer[26];
        for (int i = 0; i < years.length; i++) {
            years[i] = 2020 + i;
        }
        cmbYear = new JComboBox<>(years);
        cmbYear.setFont(new Font("Arial", Font.PLAIN, 12));
        cmbYear.setSelectedItem(calendar.get(Calendar.YEAR));
        cmbYear.addActionListener(e -> {
            if (!isUpdating && cmbYear.getSelectedItem() != null) {
                calendar.set(Calendar.YEAR, (Integer) cmbYear.getSelectedItem());
                refreshCalendarGrid();
            }
        });

        JButton btnNext = new JButton(">");
        btnNext.setFont(new Font("Arial", Font.BOLD, 12));
        btnNext.setToolTipText("Next Month");
        btnNext.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            syncComboBoxes();
            refreshCalendarGrid();
        });

        panel.add(btnPrev);
        panel.add(cmbMonth);
        panel.add(cmbYear);
        panel.add(btnNext);

        return panel;
    }

    private JPanel createCalendarGridPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // Day of Week Headers
        JPanel headerPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        for (String dayName : DAY_NAMES) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(new Font("Arial", Font.BOLD, 11));
            if ("Su".equals(dayName)) {
                lbl.setForeground(new Color(180, 40, 40)); // Red for Sundays
            } else {
                lbl.setForeground(new Color(50, 60, 80));
            }
            headerPanel.add(lbl);
        }
        panel.add(headerPanel, BorderLayout.NORTH);

        // 6 rows x 7 columns grid for days
        JPanel daysGrid = new JPanel(new GridLayout(6, 7, 2, 2));
        for (int i = 0; i < dayButtons.length; i++) {
            JButton btn = new JButton();
            btn.setFont(new Font("Arial", Font.PLAIN, 11));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                String text = btn.getText();
                if (!text.isEmpty()) {
                    int day = Integer.parseInt(text);
                    int month = calendar.get(Calendar.MONTH) + 1; // Calendar month is 0-indexed
                    int year = calendar.get(Calendar.YEAR);
                    selectedDate = String.format("%02d-%02d-%04d", day, month, year);
                    dispose();
                }
            });
            dayButtons[i] = btn;
            daysGrid.add(btn);
        }
        panel.add(daysGrid, BorderLayout.CENTER);

        return panel;
    }

    private JButton btnToday;

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));

        btnToday = new JButton("Today");
        btnToday.setFont(new Font("Arial", Font.PLAIN, 12));
        btnToday.setToolTipText("Select Today's Date");
        btnToday.addActionListener(e -> selectToday());

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> {
            selectedDate = null;
            dispose();
        });

        panel.add(btnToday);
        panel.add(btnCancel);

        return panel;
    }

    /**
     * Selects the current system date in dd-MM-yyyy format and closes the dialog.
     */
    public void selectToday() {
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        selectedDate = sdf.format(today);
        dispose();
    }

    public JButton getBtnToday() {
        return btnToday;
    }

    /**
     * Refreshes the calendar day buttons according to the current month and year.
     */
    public void refreshCalendarGrid() {
        Calendar temp = (Calendar) calendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);

        int startDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday, 1 = Monday, etc.
        int maxDaysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar today = Calendar.getInstance();
        boolean isCurrentMonthAndYear = (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR))
                && (today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
        int currentDay = today.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < dayButtons.length; i++) {
            int dayNumber = i - startDayOfWeek + 1;
            if (dayNumber >= 1 && dayNumber <= maxDaysInMonth) {
                dayButtons[i].setText(String.valueOf(dayNumber));
                dayButtons[i].setEnabled(true);
                dayButtons[i].setVisible(true);

                // Highlight today's date
                if (isCurrentMonthAndYear && dayNumber == currentDay) {
                    dayButtons[i].setBackground(new Color(220, 235, 252));
                    dayButtons[i].setForeground(new Color(15, 60, 150));
                    dayButtons[i].setFont(new Font("Arial", Font.BOLD, 11));
                } else {
                    dayButtons[i].setBackground(Color.WHITE);
                    dayButtons[i].setForeground(Color.BLACK);
                    dayButtons[i].setFont(new Font("Arial", Font.PLAIN, 11));
                }
            } else {
                dayButtons[i].setText("");
                dayButtons[i].setEnabled(false);
                dayButtons[i].setVisible(false);
            }
        }
    }

    private void syncComboBoxes() {
        isUpdating = true;
        cmbMonth.setSelectedIndex(calendar.get(Calendar.MONTH));
        cmbYear.setSelectedItem(calendar.get(Calendar.YEAR));
        isUpdating = false;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    /**
     * Static utility to open the DatePicker dialog and return the chosen "dd-MM-yyyy" date string.
     * 
     * @param parent The parent component/window
     * @param initialDateStr Existing date string to initialize the calendar (optional)
     * @return Formatted date string "dd-MM-yyyy", or null if cancelled.
     */
    public static String showDatePicker(Component parent, String initialDateStr) {
        Frame parentFrame = null;
        if (parent instanceof Frame) {
            parentFrame = (Frame) parent;
        } else if (parent != null) {
            parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        }

        DatePickerDialog dialog = new DatePickerDialog(parentFrame, initialDateStr);
        dialog.setVisible(true); // Blocks until dismissed because it's modal
        return dialog.getSelectedDate();
    }
}
