import dao.DatabaseManager;
import ui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main is the entry point for the VoyageX travel planner application.
 * 
 * Demonstrates:
 * - Thread Safety in Swing: Uses SwingUtilities.invokeLater() to ensure the GUI is built
 *   and displayed on the Event Dispatch Thread (EDT).
 * - System Look and Feel: Adapts to the operating system's native look and feel.
 * - Database Lifecycle: Initializes SQLite database schema and loads persisted trips on startup.
 */
public class Main {
    public static void main(String[] args) {
        // Safe GUI launch on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // Set system look and feel for a clean native desktop appearance
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Fallback to default Swing Look and Feel if system theme is unavailable
                }

                // Initialize local SQLite database schema
                DatabaseManager.initializeDatabase();

                MainFrame mainFrame = new MainFrame();
                mainFrame.enablePersistence();
                mainFrame.loadTripsFromDatabase();
                mainFrame.setVisible(true);
            }
        });
    }
}
