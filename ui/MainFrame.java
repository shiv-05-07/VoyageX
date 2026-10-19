package ui;

import model.Trip;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MainFrame is the primary Swing window for VoyageX.
 * 
 * Demonstrates:
 * - GUI Components: JFrame, JPanel, JLabel, JButton, JTextField, JTable, JScrollPane, JOptionPane.
 * - Layout Managers: CardLayout (screen switching), BorderLayout, GridLayout, FlowLayout.
 * - Event Handling: ActionListeners with lambda expressions for button actions.
 * - Input Validation: Checking empty fields, positive numeric budget, and valid chronological dates.
 * - Model Integration: Instantiates and stores Trip objects in an in-memory List.
 */
public class MainFrame extends JFrame {

    // In-memory list to store trips during this session (Phase 2 requirement)
    private final List<Trip> tripList = new ArrayList<Trip>();

    // CardLayout and container for multi-screen navigation
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    // Screen names
    public static final String SCREEN_HOME = "HOME";
    public static final String SCREEN_CREATE = "CREATE";
    public static final String SCREEN_VIEW = "VIEW";

    // Create Trip Form Input Fields
    private JTextField txtTripName;
    private JTextField txtDestination;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JButton btnPickStartDate;
    private JButton btnPickEndDate;
    private JTextField txtBudget;

    // View Trips Table Components
    private DefaultTableModel tableModel;
    private JTable tripsTable;
    private JLabel lblTableStatus;

    public MainFrame() {
        super("VoyageX — Travel Planner");
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 520);
        setMinimumSize(new Dimension(580, 460));
        setLocationRelativeTo(null); // Center window on screen

        // Build individual screen panels
        JPanel homePanel = createHomePanel();
        JPanel createTripPanel = createCreateTripPanel();
        JPanel viewTripsPanel = createViewTripsPanel();

        // Add screens to the card container
        cardsPanel.add(homePanel, SCREEN_HOME);
        cardsPanel.add(createTripPanel, SCREEN_CREATE);
        cardsPanel.add(viewTripsPanel, SCREEN_VIEW);

        // Add card container to JFrame
        add(cardsPanel);

        // Show Home screen initially
        showScreen(SCREEN_HOME);
    }

    /**
     * Switches visible screen using CardLayout.
     */
    public void showScreen(String screenName) {
        if (SCREEN_VIEW.equals(screenName)) {
            refreshTripsTable();
        }
        cardLayout.show(cardsPanel, screenName);
    }

    /**
     * Builds the Home/Dashboard Screen.
     */
    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Top Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel lblTitle = new JLabel("VoyageX", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitle.setForeground(new Color(25, 45, 80));

        JLabel lblSubtitle = new JLabel("Desktop Travel Planner & Itinerary Manager", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitle.setForeground(Color.DARK_GRAY);

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center Buttons Panel
        JPanel centerContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel buttonsGrid = new JPanel(new GridLayout(3, 1, 15, 18));
        buttonsGrid.setPreferredSize(new Dimension(280, 180));

        JButton btnCreateTrip = new JButton("Create New Trip");
        btnCreateTrip.setFont(new Font("Arial", Font.BOLD, 14));
        btnCreateTrip.addActionListener(e -> {
            clearCreateTripForm();
            showScreen(SCREEN_CREATE);
        });

        JButton btnViewTrips = new JButton("View Trips");
        btnViewTrips.setFont(new Font("Arial", Font.BOLD, 14));
        btnViewTrips.addActionListener(e -> showScreen(SCREEN_VIEW));

        JButton btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Arial", Font.BOLD, 14));
        btnExit.addActionListener(e -> handleExit());

        buttonsGrid.add(btnCreateTrip);
        buttonsGrid.add(btnViewTrips);
        buttonsGrid.add(btnExit);
        centerContainer.add(buttonsGrid);

        panel.add(centerContainer, BorderLayout.CENTER);

        // Bottom Footer
        JLabel lblFooter = new JLabel("College Semester Project — Core Java & Swing", SwingConstants.CENTER);
        lblFooter.setFont(new Font("Arial", Font.ITALIC, 11));
        lblFooter.setForeground(Color.GRAY);
        panel.add(lblFooter, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the Create Trip Form Screen.
     */
    private JPanel createCreateTripPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // Header
        JLabel lblTitle = new JLabel("Create New Trip", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(25, 45, 80));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Form Fields (GridLayout with 5 rows and 2 columns)
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 14));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblName = new JLabel("Trip Name:");
        lblName.setFont(new Font("Arial", Font.PLAIN, 13));
        txtTripName = new JTextField();

        JLabel lblDestination = new JLabel("Destination:");
        lblDestination.setFont(new Font("Arial", Font.PLAIN, 13));
        txtDestination = new JTextField();

        // Start Date with text field + calendar date picker button
        JLabel lblStartDate = new JLabel("Start Date (DD-MM-YYYY):");
        lblStartDate.setFont(new Font("Arial", Font.PLAIN, 13));
        txtStartDate = new JTextField();
        btnPickStartDate = new JButton("📅 Pick");
        btnPickStartDate.setFont(new Font("Arial", Font.PLAIN, 11));
        btnPickStartDate.setToolTipText("Select Start Date from Calendar");
        btnPickStartDate.addActionListener(e -> {
            String selected = DatePickerDialog.showDatePicker(this, txtStartDate.getText().trim());
            if (selected != null) {
                txtStartDate.setText(selected);
            }
        });
        JPanel startDatePanel = new JPanel(new BorderLayout(5, 0));
        startDatePanel.add(txtStartDate, BorderLayout.CENTER);
        startDatePanel.add(btnPickStartDate, BorderLayout.EAST);

        // End Date with text field + calendar date picker button
        JLabel lblEndDate = new JLabel("End Date (DD-MM-YYYY):");
        lblEndDate.setFont(new Font("Arial", Font.PLAIN, 13));
        txtEndDate = new JTextField();
        btnPickEndDate = new JButton("📅 Pick");
        btnPickEndDate.setFont(new Font("Arial", Font.PLAIN, 11));
        btnPickEndDate.setToolTipText("Select End Date from Calendar");
        btnPickEndDate.addActionListener(e -> {
            String selected = DatePickerDialog.showDatePicker(this, txtEndDate.getText().trim());
            if (selected != null) {
                txtEndDate.setText(selected);
            }
        });
        JPanel endDatePanel = new JPanel(new BorderLayout(5, 0));
        endDatePanel.add(txtEndDate, BorderLayout.CENTER);
        endDatePanel.add(btnPickEndDate, BorderLayout.EAST);

        JLabel lblBudget = new JLabel("Budget (INR):");
        lblBudget.setFont(new Font("Arial", Font.PLAIN, 13));
        txtBudget = new JTextField();

        formPanel.add(lblName);
        formPanel.add(txtTripName);
        formPanel.add(lblDestination);
        formPanel.add(txtDestination);
        formPanel.add(lblStartDate);
        formPanel.add(startDatePanel);
        formPanel.add(lblEndDate);
        formPanel.add(endDatePanel);
        formPanel.add(lblBudget);
        formPanel.add(txtBudget);

        panel.add(formPanel, BorderLayout.CENTER);

        // Buttons Panel (FlowLayout)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnSubmit = new JButton("Create Trip");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 13));
        btnSubmit.addActionListener(e -> handleCreateTrip());

        JButton btnBack = new JButton("Back");
        btnBack.setFont(new Font("Arial", Font.PLAIN, 13));
        btnBack.addActionListener(e -> showScreen(SCREEN_HOME));

        buttonsPanel.add(btnSubmit);
        buttonsPanel.add(btnBack);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the View Trips Screen showing the session trips table.
     */
    private JPanel createViewTripsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header
        JLabel lblTitle = new JLabel("My Trips (Current Session)", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(new Color(25, 45, 80));
        panel.add(lblTitle, BorderLayout.NORTH);

        // Center Table in JScrollPane
        String[] columns = {"Trip Name", "Destination", "Start Date", "End Date", "Budget (INR)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells read-only
            }
        };

        tripsTable = new JTable(tableModel);
        tripsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        tripsTable.setRowHeight(24);
        tripsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(tripsTable);

        // Center container with table and status label
        JPanel centerPanel = new JPanel(new BorderLayout(5, 8));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        lblTableStatus = new JLabel("No trips created yet in this session.", SwingConstants.CENTER);
        lblTableStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblTableStatus.setForeground(Color.GRAY);
        centerPanel.add(lblTableStatus, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Navigation Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnAddAnother = new JButton("Create Another Trip");
        btnAddAnother.setFont(new Font("Arial", Font.BOLD, 13));
        btnAddAnother.addActionListener(e -> {
            clearCreateTripForm();
            showScreen(SCREEN_CREATE);
        });

        JButton btnBack = new JButton("Back to Home");
        btnBack.setFont(new Font("Arial", Font.PLAIN, 13));
        btnBack.addActionListener(e -> showScreen(SCREEN_HOME));

        buttonsPanel.add(btnAddAnother);
        buttonsPanel.add(btnBack);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Validates raw input strings for creating a trip.
     * 
     * @return null if valid, or a descriptive error message if invalid.
     */
    public String validateTripInputs(String tripName, String destination, String startDateStr, String endDateStr, String budgetStr) {
        // 1. Validate required fields (cannot be empty)
        if (tripName == null || tripName.trim().isEmpty()
                || destination == null || destination.trim().isEmpty()
                || startDateStr == null || startDateStr.trim().isEmpty()
                || endDateStr == null || endDateStr.trim().isEmpty()
                || budgetStr == null || budgetStr.trim().isEmpty()) {
            return "All fields are required. Please fill in all fields.";
        }

        // 2. Validate numeric budget
        try {
            double budget = Double.parseDouble(budgetStr.trim());
            if (budget <= 0) {
                return "Budget must be greater than zero.";
            }
        } catch (NumberFormatException ex) {
            return "Budget must be a valid number (e.g., 15000 or 25000.50).";
        }

        // 3. Validate dates (format and chronological order)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);

        try {
            Date startDate = dateFormat.parse(startDateStr.trim());
            Date endDate = dateFormat.parse(endDateStr.trim());

            if (endDate.before(startDate)) {
                return "End date cannot be earlier than start date.";
            }
        } catch (ParseException ex) {
            return "Dates must be in valid DD-MM-YYYY format (e.g., 19-10-2026).";
        }

        return null; // All checks passed
    }

    /**
     * Adds a trip to the session list and updates the table.
     */
    public void addTrip(Trip trip) {
        if (trip != null) {
            tripList.add(trip);
            refreshTripsTable();
        }
    }

    /**
     * Handles the Create Trip action triggered from the form button.
     */
    public boolean handleCreateTrip() {
        String tripName = txtTripName.getText();
        String destination = txtDestination.getText();
        String startDateStr = txtStartDate.getText();
        String endDateStr = txtEndDate.getText();
        String budgetStr = txtBudget.getText();

        String validationError = validateTripInputs(tripName, destination, startDateStr, endDateStr, budgetStr);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this,
                    validationError,
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double budget = Double.parseDouble(budgetStr.trim());
        Trip trip = new Trip(tripName.trim(), destination.trim(), startDateStr.trim(), endDateStr.trim(), budget);
        addTrip(trip);

        JOptionPane.showMessageDialog(this,
                "Trip \"" + trip.getTripName() + "\" created successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        clearCreateTripForm();
        showScreen(SCREEN_VIEW);
        return true;
    }

    /**
     * Refreshes the JTable with current session trips.
     */
    public void refreshTripsTable() {
        tableModel.setRowCount(0); // Clear existing table rows

        for (Trip trip : tripList) {
            Object[] rowData = {
                trip.getTripName(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                String.format("%.2f", trip.getBudget())
            };
            tableModel.addRow(rowData);
        }

        if (tripList.isEmpty()) {
            lblTableStatus.setText("No trips created yet in this session.");
            lblTableStatus.setForeground(Color.GRAY);
        } else {
            lblTableStatus.setText("Total trips in session: " + tripList.size());
            lblTableStatus.setForeground(new Color(25, 45, 80));
        }
    }

    /**
     * Clears all input text fields in the form.
     */
    public void clearCreateTripForm() {
        txtTripName.setText("");
        txtDestination.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");
        txtBudget.setText("");
    }

    /**
     * Handles exit confirmation.
     */
    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit VoyageX?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Accessors for testing and programmatic interaction
    public List<Trip> getTripList() {
        return tripList;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTextField getTxtTripName() {
        return txtTripName;
    }

    public JTextField getTxtDestination() {
        return txtDestination;
    }

    public JTextField getTxtStartDate() {
        return txtStartDate;
    }

    public JTextField getTxtEndDate() {
        return txtEndDate;
    }

    public JButton getBtnPickStartDate() {
        return btnPickStartDate;
    }

    public JButton getBtnPickEndDate() {
        return btnPickEndDate;
    }

    public JTextField getTxtBudget() {
        return txtBudget;
    }
}
