package service;

import model.Trip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * ExportService handles exporting trip itineraries to plain-text files.
 * 
 * Demonstrates:
 * - Package organization: service layer separating business file-writing logic from Swing UI.
 * - Java File I/O: java.io.File, java.io.FileWriter, java.io.BufferedWriter, java.io.PrintWriter.
 * - Resource Management: try-with-resources statement ensuring streams are automatically flushed and closed.
 * - String sanitization: removing filesystem-illegal characters to guarantee valid file creation across operating systems.
 * - Exception Handling: throws IOException for caller (UI/Service/Tests) to catch and present informative messages.
 */
public class ExportService {

    /**
     * Sanitizes a trip name for safe usage as a filesystem filename.
     * Removes illegal characters (\ / : * ? " < > |) and replaces whitespace with underscores.
     * 
     * @param tripName the raw trip name string
     * @return a safe filename base string, or "itinerary" if the name is blank or invalid
     */
    public static String sanitizeFilename(String tripName) {
        if (tripName == null || tripName.trim().isEmpty()) {
            return "itinerary";
        }

        // Strip illegal filename characters on Windows and UNIX filesystems
        String sanitized = tripName.trim().replaceAll("[\\\\/:*?\"<>|]", "");

        // Collapse multiple whitespace characters into single underscore
        sanitized = sanitized.replaceAll("\\s+", "_");

        // Trim leading and trailing periods or underscores
        sanitized = sanitized.replaceAll("^[._]+|[._]+$", "");

        if (sanitized.isEmpty()) {
            return "itinerary";
        }
        return sanitized;
    }

    /**
     * Ensures that the specified File has a '.txt' extension (case-insensitive).
     * If missing, appends '.txt' to the filename.
     * 
     * @param file the File to verify
     * @return a File ending in '.txt'
     */
    public static File ensureTxtExtension(File file) {
        if (file == null) {
            return null;
        }

        String name = file.getName();
        if (!name.toLowerCase().endsWith(".txt")) {
            File parent = file.getParentFile();
            if (parent != null) {
                return new File(parent, name + ".txt");
            } else {
                return new File(name + ".txt");
            }
        }
        return file;
    }

    /**
     * Exports the complete itinerary of the given Trip to a target text file.
     * 
     * @param trip the Trip whose itinerary should be exported
     * @param targetFile the destination text file
     * @throws IOException if an error occurs during file creation or writing
     */
    public static void exportItinerary(Trip trip, File targetFile) throws IOException {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null for itinerary export.");
        }
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null for itinerary export.");
        }

        // Ensure parent directory exists if a path is specified
        File parentDir = targetFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // Obtain latest itinerary summary text from model
        String content = trip.generateItinerarySummary();

        // Write using try-with-resources with BufferedWriter and PrintWriter
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)))) {
            writer.print(content);
            writer.flush();
        }
    }
}
