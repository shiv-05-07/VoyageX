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

                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
