package ui;

import model.Activity;
import model.Bus;
import model.Expense;
import model.Flight;
import model.Train;
import model.Transport;
import model.Trip;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MainFrame is the primary Swing window for VoyageX.
 * 
 * Demonstrates:
 * - GUI Components: JFrame, JPanel, JTabbedPane, JLabel, JButton, JTextField,
 *   JComboBox, JCheckBox, JTable, JScrollPane, JOptionPane.
 * - Layout Managers: CardLayout (screen switching), BorderLayout, GridLayout, FlowLayout.
 * - Event Handling: ActionListeners with lambda expressions and MouseListener adapters.
 * - Input Validation: Checking empty fields, positive numeric budget/costs, and valid chronological dates.
 * - Model Integration: Instantiates and stores Trip, Transport (Flight/Train/Bus),
 *   Activity, and Expense objects in-memory.
 * - Polymorphism: Calls transport.calculateCost() polymorphically regardless of transport type.
 */
public class MainFrame extends JFrame {

    // In-memory list to store trips during this session
    private final List<Trip> tripList = new ArrayList<Trip>();

    // Reference to currently selected trip for management in Phase 3
    private Trip selectedTrip;

    // Controls whether modal message dialogs are shown (set to false during automated tests)
    private boolean showDialogs = true;

    public boolean isShowDialogs() {
        return showDialogs;
    }

    public void setShowDialogs(boolean showDialogs) {
        this.showDialogs = showDialogs;
    }

    private void showMessage(String message, String title, int messageType) {
        if (showDialogs) {
            JOptionPane.showMessageDialog(this, message, title, messageType);
        }
    }

    // CardLayout and container for multi-screen navigation
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);

    // Screen names
    public static final String SCREEN_HOME = "HOME";
    public static final String SCREEN_CREATE = "CREATE";
    public static final String SCREEN_VIEW = "VIEW";
    public static final String SCREEN_MANAGE = "MANAGE";

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
    private JButton btnManageTrip;

    // Manage Trip Screen Components
    private JLabel lblManageTripTitle;
    private JLabel lblManageTripDates;
    private JTabbedPane manageTabbedPane;
    private JLabel lblBottomSummary;
    private JButton btnBackToTrips;

    // Transport Tab Components
    private JComboBox<String> cmbTransportType;
    private JTextField txtTransportSource;
    private JTextField txtTransportDestination;
    private JTextField txtTransportBaseFare;
    private JLabel lblTransportExtra;
    private JTextField txtTransportExtra;
    private JCheckBox chkBusAc;
    private JButton btnSaveTransport;
    private JButton btnRemoveTransport;
    private JLabel lblCurrentTransport;
    private JLabel lblTransportCostBreakdown;

    // Activity Tab Components
    private JTextField txtActivityName;
    private JTextField txtActivityDate;
    private JButton btnPickActivityDate;
    private JTextField txtActivityCost;
    private JButton btnAddActivity;
    private JButton btnRemoveActivity;
    private DefaultTableModel activitiesTableModel;
    private JTable activitiesTable;
    private JLabel lblTotalActivitiesCost;

    // Expense Tab Components
    private JComboBox<String> cmbExpenseCategory;
    private JTextField txtExpenseDescription;
    private JTextField txtExpenseAmount;
    private JButton btnAddExpense;
    private JButton btnRemoveExpense;
    private DefaultTableModel expensesTableModel;
    private JTable expensesTable;
    private JLabel lblTotalExpensesCost;

    // Budget Summary Tab Components
    private JLabel lblSummaryBudget;
    private JLabel lblSummaryTransport;
    private JLabel lblSummaryActivities;
    private JLabel lblSummaryExpenses;
    private JLabel lblSummaryTotalCost;
    private JLabel lblSummaryRemaining;
    private JLabel lblSummaryStatus;

    public MainFrame() {
        super("VoyageX — Travel Planner");
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 560);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null); // Center window on screen

        // Build individual screen panels
        JPanel homePanel = createHomePanel();
        JPanel createTripPanel = createCreateTripPanel();
        JPanel viewTripsPanel = createViewTripsPanel();
        JPanel manageTripPanel = createManageTripPanel();

        // Add screens to the card container
        cardsPanel.add(homePanel, SCREEN_HOME);
        cardsPanel.add(createTripPanel, SCREEN_CREATE);
        cardsPanel.add(viewTripsPanel, SCREEN_VIEW);
        cardsPanel.add(manageTripPanel, SCREEN_MANAGE);

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
        if (SCREEN_MANAGE.equals(screenName) && selectedTrip != null) {
            populateManageTripData(selectedTrip);
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
        JLabel lblFooter = new JLabel("College Semester Project \u2014 Core Java & Swing", SwingConstants.CENTER);
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
        btnPickStartDate = new JButton("Pick...");
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
        btnPickEndDate = new JButton("Pick...");
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

        // Enable double-clicking a row to manage the trip
        tripsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tripsTable.getSelectedRow() != -1) {
                    handleManageTrip();
                }
            }
        });

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

        btnManageTrip = new JButton("Manage Selected Trip");
        btnManageTrip.setFont(new Font("Arial", Font.BOLD, 13));
        btnManageTrip.setToolTipText("Configure transport, activities, expenses and budget for the selected trip");
        btnManageTrip.addActionListener(e -> handleManageTrip());

        JButton btnAddAnother = new JButton("Create Another Trip");
        btnAddAnother.setFont(new Font("Arial", Font.PLAIN, 13));
        btnAddAnother.addActionListener(e -> {
            clearCreateTripForm();
            showScreen(SCREEN_CREATE);
        });

        JButton btnBack = new JButton("Back to Home");
        btnBack.setFont(new Font("Arial", Font.PLAIN, 13));
        btnBack.addActionListener(e -> showScreen(SCREEN_HOME));

        buttonsPanel.add(btnManageTrip);
        buttonsPanel.add(btnAddAnother);
        buttonsPanel.add(btnBack);

        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the Manage Trip Screen with tabs for Transport, Activities, Expenses, and Budget Summary.
     */
    private JPanel createManageTripPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. North: Selected Trip Header Banner
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        headerPanel.setBackground(new Color(243, 247, 253));

        lblManageTripTitle = new JLabel("Managing Trip: None", SwingConstants.LEFT);
        lblManageTripTitle.setFont(new Font("Arial", Font.BOLD, 17));
        lblManageTripTitle.setForeground(new Color(25, 45, 80));

        lblManageTripDates = new JLabel("Dates: - | Allocated Budget: INR 0.00", SwingConstants.LEFT);
        lblManageTripDates.setFont(new Font("Arial", Font.PLAIN, 12));
        lblManageTripDates.setForeground(Color.DARK_GRAY);

        headerPanel.add(lblManageTripTitle);
        headerPanel.add(lblManageTripDates);
        panel.add(headerPanel, BorderLayout.NORTH);

        // 2. Center: JTabbedPane
        manageTabbedPane = new JTabbedPane();
        manageTabbedPane.setFont(new Font("Arial", Font.BOLD, 12));

        manageTabbedPane.addTab("Transport", createTransportTab());
        manageTabbedPane.addTab("Activities", createActivitiesTab());
        manageTabbedPane.addTab("Expenses", createExpensesTab());
        manageTabbedPane.addTab("Budget Summary", createBudgetSummaryTab());

        panel.add(manageTabbedPane, BorderLayout.CENTER);

        // 3. South: Live Summary & Navigation Strip
        JPanel southPanel = new JPanel(new BorderLayout(10, 5));
        southPanel.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 4));

        lblBottomSummary = new JLabel("Total Cost: INR 0.00  |  Remaining Budget: INR 0.00", SwingConstants.LEFT);
        lblBottomSummary.setFont(new Font("Arial", Font.BOLD, 12));
        lblBottomSummary.setForeground(new Color(25, 45, 80));

        btnBackToTrips = new JButton("Back to My Trips");
        btnBackToTrips.setFont(new Font("Arial", Font.PLAIN, 12));
        btnBackToTrips.addActionListener(e -> showScreen(SCREEN_VIEW));

        southPanel.add(lblBottomSummary, BorderLayout.WEST);
        southPanel.add(btnBackToTrips, BorderLayout.EAST);

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Builds the Transportation tab panel.
     */
    private JPanel createTransportTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Form panel
        JPanel formContainer = new JPanel(new BorderLayout(5, 8));
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 195, 215)),
                "Configure Transportation (Flight / Train / Bus)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(25, 45, 80)
        ));

        JPanel formGrid = new JPanel(new GridLayout(5, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblType = new JLabel("Transport Type:");
        String[] types = {"Flight", "Train", "Bus"};
        cmbTransportType = new JComboBox<String>(types);

        JLabel lblSource = new JLabel("Source City:");
        txtTransportSource = new JTextField();

        JLabel lblDest = new JLabel("Destination City:");
        txtTransportDestination = new JTextField();

        JLabel lblBase = new JLabel("Base Fare (INR):");
        txtTransportBaseFare = new JTextField();

        lblTransportExtra = new JLabel("Baggage Fee (INR):");
        txtTransportExtra = new JTextField();
        chkBusAc = new JCheckBox("AC Bus");
        chkBusAc.setVisible(false);

        // Sub-panel for extra fee + AC checkbox when Bus
        JPanel extraPanel = new JPanel(new BorderLayout(5, 0));
        extraPanel.add(txtTransportExtra, BorderLayout.CENTER);
        extraPanel.add(chkBusAc, BorderLayout.EAST);

        formGrid.add(lblType);
        formGrid.add(cmbTransportType);
        formGrid.add(lblSource);
        formGrid.add(txtTransportSource);
        formGrid.add(lblDest);
        formGrid.add(txtTransportDestination);
        formGrid.add(lblBase);
        formGrid.add(txtTransportBaseFare);
        formGrid.add(lblTransportExtra);
        formGrid.add(extraPanel);

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        btnSaveTransport = new JButton("Save Transport");
        btnSaveTransport.setFont(new Font("Arial", Font.BOLD, 12));
        btnSaveTransport.addActionListener(e -> handleSaveTransport());

        btnRemoveTransport = new JButton("Remove Transport");
        btnRemoveTransport.setFont(new Font("Arial", Font.PLAIN, 12));
        btnRemoveTransport.addActionListener(e -> handleRemoveTransport());

        btnPanel.add(btnSaveTransport);
        btnPanel.add(btnRemoveTransport);
        formContainer.add(btnPanel, BorderLayout.SOUTH);

        tab.add(formContainer, BorderLayout.NORTH);

        // Current transport status display card
        JPanel statusCard = new JPanel(new GridLayout(2, 1, 4, 4));
        statusCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 195, 215)),
                        "Current Transport Details",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 12),
                        new Color(25, 45, 80)
                ),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        statusCard.setBackground(new Color(248, 250, 252));

        lblCurrentTransport = new JLabel("No transport assigned yet.");
        lblCurrentTransport.setFont(new Font("Arial", Font.PLAIN, 13));

        lblTransportCostBreakdown = new JLabel("Estimated Transport Cost: INR 0.00");
        lblTransportCostBreakdown.setFont(new Font("Arial", Font.BOLD, 13));
        lblTransportCostBreakdown.setForeground(new Color(25, 45, 80));

        statusCard.add(lblCurrentTransport);
        statusCard.add(lblTransportCostBreakdown);

        tab.add(statusCard, BorderLayout.CENTER);

        // Dropdown switch listener
        cmbTransportType.addActionListener(e -> updateTransportTypeView());

        return tab;
    }

    /**
     * Updates field labels and visibility when transport type dropdown changes.
     */
    private void updateTransportTypeView() {
        String selected = (String) cmbTransportType.getSelectedItem();
        if ("Flight".equals(selected)) {
            lblTransportExtra.setText("Baggage Fee (INR):");
            chkBusAc.setVisible(false);
        } else if ("Train".equals(selected)) {
            lblTransportExtra.setText("Reservation Fee (INR):");
            chkBusAc.setVisible(false);
        } else if ("Bus".equals(selected)) {
            lblTransportExtra.setText("AC Surcharge (INR):");
            chkBusAc.setVisible(true);
        }
        revalidate();
        repaint();
    }

    /**
     * Builds the Activities tab panel.
     */
    private JPanel createActivitiesTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Form panel
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 195, 215)),
                "Add Activity to Itinerary",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(25, 45, 80)
        ));

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblName = new JLabel("Activity Name:");
        txtActivityName = new JTextField();

        JLabel lblDate = new JLabel("Date (DD-MM-YYYY):");
        txtActivityDate = new JTextField();
        btnPickActivityDate = new JButton("Pick...");
        btnPickActivityDate.setFont(new Font("Arial", Font.PLAIN, 11));
        btnPickActivityDate.addActionListener(e -> {
            String initial = txtActivityDate.getText().trim();
            if (initial.isEmpty() && selectedTrip != null) {
                initial = selectedTrip.getStartDate();
            }
            String selected = DatePickerDialog.showDatePicker(this, initial);
            if (selected != null) {
                txtActivityDate.setText(selected);
            }
        });
        JPanel datePanel = new JPanel(new BorderLayout(4, 0));
        datePanel.add(txtActivityDate, BorderLayout.CENTER);
        datePanel.add(btnPickActivityDate, BorderLayout.EAST);

        JLabel lblCost = new JLabel("Cost (INR):");
        txtActivityCost = new JTextField();

        formGrid.add(lblName);
        formGrid.add(txtActivityName);
        formGrid.add(lblDate);
        formGrid.add(datePanel);
        formGrid.add(lblCost);
        formGrid.add(txtActivityCost);

        formContainer.add(formGrid, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAddActivity = new JButton("Add Activity");
        btnAddActivity.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddActivity.addActionListener(e -> handleAddActivity());
        btnPanel.add(btnAddActivity);
        formContainer.add(btnPanel, BorderLayout.SOUTH);

        tab.add(formContainer, BorderLayout.NORTH);

        // Center Table of Activities
        String[] columns = {"#", "Activity Name", "Date", "Cost (INR)"};
        activitiesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activitiesTable = new JTable(activitiesTableModel);
        activitiesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        activitiesTable.setRowHeight(22);
        activitiesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        activitiesTable.getColumnModel().getColumn(0).setMaxWidth(40);

        JScrollPane scrollPane = new JScrollPane(activitiesTable);
        tab.add(scrollPane, BorderLayout.CENTER);

        // Bottom summary & remove button
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        lblTotalActivitiesCost = new JLabel("Total Activity Costs: INR 0.00 (0 activities)");
        lblTotalActivitiesCost.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalActivitiesCost.setForeground(new Color(25, 45, 80));

        btnRemoveActivity = new JButton("Remove Selected Activity");
        btnRemoveActivity.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRemoveActivity.addActionListener(e -> handleRemoveActivity());

        bottomPanel.add(lblTotalActivitiesCost, BorderLayout.WEST);
        bottomPanel.add(btnRemoveActivity, BorderLayout.EAST);

        tab.add(bottomPanel, BorderLayout.SOUTH);

        return tab;
    }

    /**
     * Builds the Expenses tab panel.
     */
    private JPanel createExpensesTab() {
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Form panel
        JPanel formContainer = new JPanel(new BorderLayout(5, 5));
        formContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 195, 215)),
                "Add Incidental Expense (Food, Hotel, Shopping, etc.)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(25, 45, 80)
        ));

        JPanel formGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        formGrid.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel lblCat = new JLabel("Category:");
        String[] categories = {"Food & Dining", "Accommodation", "Shopping", "Local Transit", "Sightseeing", "Miscellaneous"};
        cmbExpenseCategory = new JComboBox<String>(categories);

        JLabel lblDesc = new JLabel("Description:");
        txtExpenseDescription = new JTextField();

        JLabel lblAmount = new JLabel("Amount (INR):");
        txtExpenseAmount = new JTextField();

        formGrid.add(lblCat);
        formGrid.add(cmbExpenseCategory);
        formGrid.add(lblDesc);
        formGrid.add(txtExpenseDescription);
        formGrid.add(lblAmount);
        formGrid.add(txtExpenseAmount);

        formContainer.add(formGrid, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnAddExpense = new JButton("Add Expense");
        btnAddExpense.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddExpense.addActionListener(e -> handleAddExpense());
        btnPanel.add(btnAddExpense);
        formContainer.add(btnPanel, BorderLayout.SOUTH);

        tab.add(formContainer, BorderLayout.NORTH);

        // Center Table of Expenses
        String[] columns = {"#", "Category", "Description", "Amount (INR)"};
        expensesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        expensesTable = new JTable(expensesTableModel);
        expensesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        expensesTable.setRowHeight(22);
        expensesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        expensesTable.getColumnModel().getColumn(0).setMaxWidth(40);

        JScrollPane scrollPane = new JScrollPane(expensesTable);
        tab.add(scrollPane, BorderLayout.CENTER);

        // Bottom summary & remove button
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        lblTotalExpensesCost = new JLabel("Total Other Expenses: INR 0.00 (0 items)");
        lblTotalExpensesCost.setFont(new Font("Arial", Font.BOLD, 12));
        lblTotalExpensesCost.setForeground(new Color(25, 45, 80));

        btnRemoveExpense = new JButton("Remove Selected Expense");
        btnRemoveExpense.setFont(new Font("Arial", Font.PLAIN, 11));
        btnRemoveExpense.addActionListener(e -> handleRemoveExpense());

        bottomPanel.add(lblTotalExpensesCost, BorderLayout.WEST);
        bottomPanel.add(btnRemoveExpense, BorderLayout.EAST);

        tab.add(bottomPanel, BorderLayout.SOUTH);

        return tab;
    }

    /**
     * Builds the Budget Summary tab panel.
     */
    private JPanel createBudgetSummaryTab() {
        JPanel tab = new JPanel(new BorderLayout(15, 15));
        tab.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        // Center card with summary breakdown
        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 195, 215)),
                        "Trip Financial & Budget Overview",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 13),
                        new Color(25, 45, 80)
                ),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setBackground(Color.WHITE);

        // Grid for values
        JPanel grid = new JPanel(new GridLayout(6, 2, 10, 10));
        grid.setBackground(Color.WHITE);

        Font labelFont = new Font("Arial", Font.PLAIN, 13);
        Font valueFont = new Font("Arial", Font.BOLD, 13);

        JLabel l1 = new JLabel("Allocated Trip Budget:");
        l1.setFont(labelFont);
        lblSummaryBudget = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryBudget.setFont(valueFont);
        lblSummaryBudget.setForeground(new Color(25, 45, 80));

        JLabel l2 = new JLabel("Transportation Cost:");
        l2.setFont(labelFont);
        lblSummaryTransport = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryTransport.setFont(valueFont);

        JLabel l3 = new JLabel("Total Activities Cost:");
        l3.setFont(labelFont);
        lblSummaryActivities = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryActivities.setFont(valueFont);

        JLabel l4 = new JLabel("Total Other Expenses:");
        l4.setFont(labelFont);
        lblSummaryExpenses = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryExpenses.setFont(valueFont);

        JLabel l5 = new JLabel("Grand Total Cost:");
        l5.setFont(new Font("Arial", Font.BOLD, 13));
        lblSummaryTotalCost = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryTotalCost.setFont(new Font("Arial", Font.BOLD, 13));
        lblSummaryTotalCost.setForeground(new Color(180, 40, 20));

        JLabel l6 = new JLabel("Remaining Budget:");
        l6.setFont(new Font("Arial", Font.BOLD, 13));
        lblSummaryRemaining = new JLabel("INR 0.00", SwingConstants.RIGHT);
        lblSummaryRemaining.setFont(new Font("Arial", Font.BOLD, 13));

        grid.add(l1);
        grid.add(lblSummaryBudget);
        grid.add(l2);
        grid.add(lblSummaryTransport);
        grid.add(l3);
        grid.add(lblSummaryActivities);
        grid.add(l4);
        grid.add(lblSummaryExpenses);
        grid.add(l5);
        grid.add(lblSummaryTotalCost);
        grid.add(l6);
        grid.add(lblSummaryRemaining);

        card.add(grid, BorderLayout.CENTER);

        // Status banner at the bottom of the card
        lblSummaryStatus = new JLabel("Status: Within Budget", SwingConstants.CENTER);
        lblSummaryStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblSummaryStatus.setOpaque(true);
        lblSummaryStatus.setBackground(new Color(230, 245, 230));
        lblSummaryStatus.setForeground(new Color(30, 120, 40));
        lblSummaryStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 210, 160)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        card.add(lblSummaryStatus, BorderLayout.SOUTH);
        tab.add(card, BorderLayout.CENTER);

        return tab;
    }

    /**
     * Handles the Manage Trip action when user clicks the button or double clicks a table row.
     */
    public boolean handleManageTrip() {
        int selectedRow = tripsTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage(
                    "Please select a trip from the table to manage.",
                    "No Trip Selected",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Trip trip = tripList.get(selectedRow);
        openManageTrip(trip);
        return true;
    }

    /**
     * Opens the Manage Trip screen for a given trip.
     */
    public void openManageTrip(Trip trip) {
        if (trip == null) return;
        this.selectedTrip = trip;
        populateManageTripData(trip);
        manageTabbedPane.setSelectedIndex(0); // Start on Transport tab
        showScreen(SCREEN_MANAGE);
    }

    /**
     * Populates all Manage Trip panels with data from the specified trip.
     */
    public void populateManageTripData(Trip trip) {
        if (trip == null) return;
        this.selectedTrip = trip;

        lblManageTripTitle.setText("Managing Trip: " + trip.getTripName() + " (" + trip.getDestination() + ")");
        lblManageTripDates.setText("Dates: " + trip.getStartDate() + " to " + trip.getEndDate()
                + "  |  Allocated Budget: INR " + String.format("%.2f", trip.getBudget()));

        refreshTransportTab();
        refreshActivitiesTable();
        refreshExpensesTable();
        refreshBudgetSummary();
    }

    /**
     * Refreshes the Transportation tab with current trip's transport data.
     */
    public void refreshTransportTab() {
        if (selectedTrip == null) return;
        Transport t = selectedTrip.getTransport();
        if (t != null) {
            lblCurrentTransport.setText("Assigned: " + t.getTransportType() + " [" + t.getSource() + " -> " + t.getDestination() + "]");
            lblTransportCostBreakdown.setText(String.format("Calculated Transport Cost: INR %.2f  (%s)", t.calculateCost(), t.toString()));
            txtTransportSource.setText(t.getSource());
            txtTransportDestination.setText(t.getDestination());
            txtTransportBaseFare.setText(String.valueOf(t.getBaseFare()));

            if (t instanceof Flight) {
                cmbTransportType.setSelectedItem("Flight");
                txtTransportExtra.setText(String.valueOf(((Flight) t).getBaggageFee()));
                chkBusAc.setSelected(false);
            } else if (t instanceof Train) {
                cmbTransportType.setSelectedItem("Train");
                txtTransportExtra.setText(String.valueOf(((Train) t).getReservationFee()));
                chkBusAc.setSelected(false);
            } else if (t instanceof Bus) {
                cmbTransportType.setSelectedItem("Bus");
                Bus bus = (Bus) t;
                txtTransportExtra.setText(String.valueOf(bus.getAcSurcharge()));
                chkBusAc.setSelected(bus.isAc());
            }
            updateTransportTypeView();
        } else {
            lblCurrentTransport.setText("No transport assigned yet.");
            lblTransportCostBreakdown.setText("Estimated Transport Cost: INR 0.00");
            txtTransportSource.setText("");
            txtTransportDestination.setText(selectedTrip.getDestination());
            txtTransportBaseFare.setText("");
            txtTransportExtra.setText("");
            chkBusAc.setSelected(false);
            cmbTransportType.setSelectedItem("Flight");
            updateTransportTypeView();
        }
    }

    /**
     * Refreshes the Activities table and total activity cost for the selected trip.
     */
    public void refreshActivitiesTable() {
        activitiesTableModel.setRowCount(0);
        if (selectedTrip == null) return;

        List<Activity> list = selectedTrip.getActivities();
        for (int i = 0; i < list.size(); i++) {
            Activity a = list.get(i);
            activitiesTableModel.addRow(new Object[]{
                    (i + 1),
                    a.getName(),
                    a.getDate(),
                    String.format("%.2f", a.getCost())
            });
        }
        lblTotalActivitiesCost.setText(String.format("Total Activity Costs: INR %.2f (%d activities)",
                selectedTrip.calculateTotalActivityCost(), list.size()));
    }

    /**
     * Refreshes the Expenses table and total expense cost for the selected trip.
     */
    public void refreshExpensesTable() {
        expensesTableModel.setRowCount(0);
        if (selectedTrip == null) return;

        List<Expense> list = selectedTrip.getExpenses();
        for (int i = 0; i < list.size(); i++) {
            Expense e = list.get(i);
            expensesTableModel.addRow(new Object[]{
                    (i + 1),
                    e.getCategory(),
                    e.getDescription(),
                    String.format("%.2f", e.getAmount())
            });
        }
        lblTotalExpensesCost.setText(String.format("Total Other Expenses: INR %.2f (%d items)",
                selectedTrip.calculateTotalExpenseCost(), list.size()));
    }

    /**
     * Refreshes the budget summary breakdown and status banners.
     */
    public void refreshBudgetSummary() {
        if (selectedTrip == null) return;

        double budget = selectedTrip.getBudget();
        double transportCost = selectedTrip.calculateTotalTransportCost();
        double activityCost = selectedTrip.calculateTotalActivityCost();
        double expenseCost = selectedTrip.calculateTotalExpenseCost();
        double totalCost = selectedTrip.calculateTotalCost();
        double remaining = selectedTrip.calculateRemainingBudget();

        // Update Budget Summary Tab
        lblSummaryBudget.setText(String.format("INR %.2f", budget));
        lblSummaryTransport.setText(String.format("INR %.2f", transportCost));
        lblSummaryActivities.setText(String.format("INR %.2f", activityCost));
        lblSummaryExpenses.setText(String.format("INR %.2f", expenseCost));
        lblSummaryTotalCost.setText(String.format("INR %.2f", totalCost));
        lblSummaryRemaining.setText(String.format("INR %.2f", remaining));

        if (remaining >= 0) {
            lblSummaryRemaining.setForeground(new Color(20, 120, 30));
            lblSummaryStatus.setText(String.format("Status: Within Budget - INR %.2f remaining", remaining));
            lblSummaryStatus.setBackground(new Color(232, 248, 232));
            lblSummaryStatus.setForeground(new Color(20, 120, 30));
            lblSummaryStatus.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(160, 210, 160)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            lblBottomSummary.setText(String.format("Total Cost: INR %.2f  |  Remaining Budget: INR %.2f (Within Budget)", totalCost, remaining));
            lblBottomSummary.setForeground(new Color(20, 110, 30));
        } else {
            lblSummaryRemaining.setForeground(new Color(180, 20, 20));
            lblSummaryStatus.setText(String.format("Status: OVER BUDGET by INR %.2f!", Math.abs(remaining)));
            lblSummaryStatus.setBackground(new Color(254, 235, 235));
            lblSummaryStatus.setForeground(new Color(180, 20, 20));
            lblSummaryStatus.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 160, 160)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            lblBottomSummary.setText(String.format("Total Cost: INR %.2f  |  Remaining Budget: -INR %.2f (OVER BUDGET!)", totalCost, Math.abs(remaining)));
            lblBottomSummary.setForeground(new Color(180, 20, 20));
        }
    }

    /**
     * Handles saving or updating the transport for the selected trip.
     */
    public boolean handleSaveTransport() {
        if (selectedTrip == null) {
            showMessage( "No trip selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String type = (String) cmbTransportType.getSelectedItem();
        String source = txtTransportSource.getText().trim();
        String dest = txtTransportDestination.getText().trim();
        String baseFareStr = txtTransportBaseFare.getText().trim();
        String extraStr = txtTransportExtra.getText().trim();
        boolean isAc = chkBusAc.isSelected();

        String error = validateTransportInputs(type, source, dest, baseFareStr, extraStr, isAc);
        if (error != null) {
            showMessage( error, "Invalid Transport Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double baseFare = Double.parseDouble(baseFareStr);
        double extraFee = extraStr.isEmpty() ? 0.0 : Double.parseDouble(extraStr);

        Transport transport;
        if ("Flight".equals(type)) {
            transport = new Flight(source, dest, baseFare, extraFee);
        } else if ("Train".equals(type)) {
            transport = new Train(source, dest, baseFare, extraFee);
        } else {
            transport = new Bus(source, dest, baseFare, isAc, extraFee);
        }

        selectedTrip.setTransport(transport);
        refreshTransportTab();
        refreshBudgetSummary();

        showMessage(
                type + " transport saved successfully!\nEstimated Cost: INR " + String.format("%.2f", transport.calculateCost()),
                "Transport Saved",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * Handles removing transport from the selected trip.
     */
    public boolean handleRemoveTransport() {
        if (selectedTrip == null) return false;
        selectedTrip.setTransport(null);
        refreshTransportTab();
        refreshBudgetSummary();
        showMessage( "Transport removed from trip.", "Transport Removed", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * Handles adding an activity to the selected trip.
     */
    public boolean handleAddActivity() {
        if (selectedTrip == null) return false;

        String name = txtActivityName.getText();
        String date = txtActivityDate.getText();
        String costStr = txtActivityCost.getText();

        String error = validateActivityInputs(name, date, costStr);
        if (error != null) {
            showMessage( error, "Invalid Activity Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double cost = Double.parseDouble(costStr.trim());
        Activity activity = new Activity(name.trim(), date.trim(), cost);
        selectedTrip.addActivity(activity);

        refreshActivitiesTable();
        refreshBudgetSummary();

        txtActivityName.setText("");
        txtActivityDate.setText("");
        txtActivityCost.setText("");

        showMessage(
                "Activity \"" + activity.getName() + "\" added successfully!",
                "Activity Added",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * Handles removing an activity from the selected trip.
     */
    public boolean handleRemoveActivity() {
        if (selectedTrip == null) return false;
        int row = activitiesTable.getSelectedRow();
        if (row == -1) {
            showMessage(
                    "Please select an activity from the table to remove.",
                    "No Activity Selected",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Activity removed = selectedTrip.getActivities().get(row);
        selectedTrip.removeActivity(removed);
        refreshActivitiesTable();
        refreshBudgetSummary();
        return true;
    }

    /**
     * Handles adding an expense to the selected trip.
     */
    public boolean handleAddExpense() {
        if (selectedTrip == null) return false;

        String category = (String) cmbExpenseCategory.getSelectedItem();
        String description = txtExpenseDescription.getText();
        String amountStr = txtExpenseAmount.getText();

        String error = validateExpenseInputs(category, description, amountStr);
        if (error != null) {
            showMessage( error, "Invalid Expense Input", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double amount = Double.parseDouble(amountStr.trim());
        Expense expense = new Expense(category, description.trim(), amount);
        selectedTrip.addExpense(expense);

        refreshExpensesTable();
        refreshBudgetSummary();

        txtExpenseDescription.setText("");
        txtExpenseAmount.setText("");

        showMessage(
                "Expense \"" + expense.getDescription() + "\" added successfully!",
                "Expense Added",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    /**
     * Handles removing an expense from the selected trip.
     */
    public boolean handleRemoveExpense() {
        if (selectedTrip == null) return false;
        int row = expensesTable.getSelectedRow();
        if (row == -1) {
            showMessage(
                    "Please select an expense from the table to remove.",
                    "No Expense Selected",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Expense removed = selectedTrip.getExpenses().get(row);
        selectedTrip.removeExpense(removed);
        refreshExpensesTable();
        refreshBudgetSummary();
        return true;
    }

    /**
     * Validates raw input strings for creating a transport.
     */
    public String validateTransportInputs(String type, String source, String dest, String baseFareStr, String extraStr, boolean isAc) {
        if (source == null || source.trim().isEmpty() || dest == null || dest.trim().isEmpty()) {
            return "Source and Destination cannot be empty.";
        }
        if (baseFareStr == null || baseFareStr.trim().isEmpty()) {
            return "Base fare is required.";
        }
        try {
            double baseFare = Double.parseDouble(baseFareStr.trim());
            if (baseFare < 0) {
                return "Base fare cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Base fare must be a valid number.";
        }

        if (extraStr != null && !extraStr.trim().isEmpty()) {
            try {
                double extra = Double.parseDouble(extraStr.trim());
                if (extra < 0) {
                    return "Fee or surcharge cannot be negative.";
                }
            } catch (NumberFormatException e) {
                return "Fee or surcharge must be a valid number.";
            }
        }
        return null;
    }

    /**
     * Validates raw input strings for adding an activity.
     */
    public String validateActivityInputs(String name, String dateStr, String costStr) {
        if (name == null || name.trim().isEmpty()
                || dateStr == null || dateStr.trim().isEmpty()
                || costStr == null || costStr.trim().isEmpty()) {
            return "All fields (Activity Name, Date, Cost) are required.";
        }
        try {
            double cost = Double.parseDouble(costStr.trim());
            if (cost < 0) {
                return "Activity cost cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Activity cost must be a valid number.";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(dateStr.trim());
        } catch (ParseException e) {
            return "Activity date must be in valid DD-MM-YYYY format.";
        }

        return null;
    }

    /**
     * Validates raw input strings for adding an expense.
     */
    public String validateExpenseInputs(String category, String description, String amountStr) {
        if (description == null || description.trim().isEmpty()
                || amountStr == null || amountStr.trim().isEmpty()) {
            return "Description and Amount are required.";
        }
        try {
            double amount = Double.parseDouble(amountStr.trim());
            if (amount < 0) {
                return "Expense amount cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Expense amount must be a valid number.";
        }
        return null;
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
            showMessage(
                    validationError,
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double budget = Double.parseDouble(budgetStr.trim());
        Trip trip = new Trip(tripName.trim(), destination.trim(), startDateStr.trim(), endDateStr.trim(), budget);
        addTrip(trip);

        showMessage(
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

    public Trip getSelectedTrip() {
        return selectedTrip;
    }

    public void setSelectedTrip(Trip selectedTrip) {
        this.selectedTrip = selectedTrip;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JTable getTripsTable() {
        return tripsTable;
    }

    public JButton getBtnManageTrip() {
        return btnManageTrip;
    }

    public JTabbedPane getManageTabbedPane() {
        return manageTabbedPane;
    }

    public JLabel getLblManageTripTitle() {
        return lblManageTripTitle;
    }

    public JLabel getLblManageTripDates() {
        return lblManageTripDates;
    }

    public JLabel getLblBottomSummary() {
        return lblBottomSummary;
    }

    public JButton getBtnBackToTrips() {
        return btnBackToTrips;
    }

    public JComboBox<String> getCmbTransportType() {
        return cmbTransportType;
    }

    public JTextField getTxtTransportSource() {
        return txtTransportSource;
    }

    public JTextField getTxtTransportDestination() {
        return txtTransportDestination;
    }

    public JTextField getTxtTransportBaseFare() {
        return txtTransportBaseFare;
    }

    public JLabel getLblTransportExtra() {
        return lblTransportExtra;
    }

    public JTextField getTxtTransportExtra() {
        return txtTransportExtra;
    }

    public JCheckBox getChkBusAc() {
        return chkBusAc;
    }

    public JButton getBtnSaveTransport() {
        return btnSaveTransport;
    }

    public JButton getBtnRemoveTransport() {
        return btnRemoveTransport;
    }

    public JLabel getLblCurrentTransport() {
        return lblCurrentTransport;
    }

    public JLabel getLblTransportCostBreakdown() {
        return lblTransportCostBreakdown;
    }

    public JTextField getTxtActivityName() {
        return txtActivityName;
    }

    public JTextField getTxtActivityDate() {
        return txtActivityDate;
    }

    public JButton getBtnPickActivityDate() {
        return btnPickActivityDate;
    }

    public JTextField getTxtActivityCost() {
        return txtActivityCost;
    }

    public JButton getBtnAddActivity() {
        return btnAddActivity;
    }

    public JButton getBtnRemoveActivity() {
        return btnRemoveActivity;
    }

    public DefaultTableModel getActivitiesTableModel() {
        return activitiesTableModel;
    }

    public JTable getActivitiesTable() {
        return activitiesTable;
    }

    public JLabel getLblTotalActivitiesCost() {
        return lblTotalActivitiesCost;
    }

    public JComboBox<String> getCmbExpenseCategory() {
        return cmbExpenseCategory;
    }

    public JTextField getTxtExpenseDescription() {
        return txtExpenseDescription;
    }

    public JTextField getTxtExpenseAmount() {
        return txtExpenseAmount;
    }

    public JButton getBtnAddExpense() {
        return btnAddExpense;
    }

    public JButton getBtnRemoveExpense() {
        return btnRemoveExpense;
    }

    public DefaultTableModel getExpensesTableModel() {
        return expensesTableModel;
    }

    public JTable getExpensesTable() {
        return expensesTable;
    }

    public JLabel getLblTotalExpensesCost() {
        return lblTotalExpensesCost;
    }

    public JLabel getLblSummaryBudget() {
        return lblSummaryBudget;
    }

    public JLabel getLblSummaryTransport() {
        return lblSummaryTransport;
    }

    public JLabel getLblSummaryActivities() {
        return lblSummaryActivities;
    }

    public JLabel getLblSummaryExpenses() {
        return lblSummaryExpenses;
    }

    public JLabel getLblSummaryTotalCost() {
        return lblSummaryTotalCost;
    }

    public JLabel getLblSummaryRemaining() {
        return lblSummaryRemaining;
    }

    public JLabel getLblSummaryStatus() {
        return lblSummaryStatus;
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
