import java.sql.*;
import java.util.Scanner;

public class Main {

    static Connection connection;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            // To Connect to Database
            connection = DriverManager.getConnection("jdbc:sqlite:database/event_registration.db");
            System.out.println("Connected to DATABASE successfully\n");

            // Create table if not exist
            connection.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS participants (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "age INTEGER, " +
                "registration_date TEXT DEFAULT (datetime('now','localtime'))" +
                ");"
            );

            // Main loop
            while (true) {
                int total = countParticipants();

                System.out.println("=========================");
                System.out.println("Event Registration System");
                System.out.println("=========================");
                System.out.println("Total participants: " + total);
                System.out.println("\n[1] Register participant");
                System.out.println("[2] Remove participant");
                System.out.println("[3] Display all participants");
                System.out.println("[4] Search participant");
                System.out.println("[5] Exit Program\n");

                int command = askInt("Enter command: ");

                if (command == 5) {
                    System.out.println("\nSaving changes...");
                    connection.close();
                    scanner.close();
                    System.out.println("Goodbye!");
                    break;
                }

                switch (command) {
                    case 1: 
                        registerParticipant();
                        break;
                    case 2:
                        removeParticipant();
                        break;
                    case 3:
                        displayParticipants();
                        break;
                    case 4:
                        searchParticipant();
                        break;
                    default:
                        System.out.println("Invalid command.\n");
                }

            }

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    // VALIDATION INTEGER INPUT
    static int askInt(String m) {
        while (true) {
            try {
                System.out.print(m);
                int n = scanner.nextInt();
                scanner.nextLine();
                return n;

            } catch (Exception e) {
                System.out.println("Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }


    // REGISTER
    static void registerParticipant() {

        System.out.print("Enter first name: ");
        String first = scanner.nextLine();

        System.out.print("Enter last name: ");
        String last = scanner.nextLine();

        int age;
        
        while (true) {
            age = askInt("Enter age: ");
            if (age >= 0 && age <= 100) {
                break;
            }
            System.out.println("Invalid age.");
        }

        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO participants(first_name, last_name, age) VALUES (?, ?, ?)"
            );
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setInt(3, age);
            ps.executeUpdate();

            System.out.println("\nParticipant added successfully!\n");

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }


    // REMOVE
    static void removeParticipant() {
        int id = askInt("Enter ID to remove: ");

        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM participants WHERE id = ?"
            );
            ps.setInt(1, id);

            int row = ps.executeUpdate();

            if (row > 0)
                System.out.println("Participant removed.\n");
            else
                System.out.println("ID not found.\n");

        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }


    // DISPLAY
    static void displayParticipants() {

        String sort = "id"; //default sorted by id

        while (true) {
            try {
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM participants ORDER BY " + sort
                );
                ResultSet rs = ps.executeQuery();

                System.out.println("\n--- Participants ---");
                System.out.printf("%-4s %-12s %-12s %-5s %-20s\n",
                        "ID", "First", "Last", "Age", "Registered");

                boolean empty = true;

                while (rs.next()) {
                    empty = false;
                    System.out.printf("%-4d %-12s %-12s %-5d %-20s\n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("registration_date")
                    );
                }

                if (empty) {
                    System.out.println("(No participants found)\n");
                    return;
                }

                System.out.println("\nSort by:");
                System.out.println("[1] First name");
                System.out.println("[2] Last name");
                System.out.println("[3] Age");
                System.out.println("[4] Back to menu");

                int ch = askInt("Enter choice: ");

                if (ch == 1) sort = "first_name";
                else if (ch == 2) sort = "last_name";
                else if (ch == 3) sort = "age";
                else if (ch == 4) return;
                else System.out.println("Invalid choice.");

            } catch (Exception e) {
                System.out.println("Error: " + e);
                return;
            }
        }
    }

    // SEARCH
    static void searchParticipant() {

        System.out.println("\nSearch by:");
        System.out.println("[1] Search by ID");
        System.out.println("[2] Search by Name\n");

        int choice = askInt("Enter choice: ");

        // Search by ID
        if (choice == 1) {
            int id = askInt("Enter ID: ");

            try {
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM participants WHERE id = ?"
                );
                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();

                System.out.println("\n--- Search Result ---");

                if (rs.next()) {
                    System.out.printf("%d | %s %s | Age: %d | %s\n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("registration_date")
                    );
                } else {
                    System.out.println("No participant with ID " + id + ".\n");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

            return;
        }

        // Search by NAME
        else if (choice == 2) {
            System.out.print("Enter name to search: ");
            String search = scanner.nextLine();

            try {
                PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM participants WHERE first_name LIKE ? OR last_name LIKE ?"
                );
                ps.setString(1, "%" + search + "%");
                ps.setString(2, "%" + search + "%");

                ResultSet rs = ps.executeQuery();

                System.out.println("\n--- Search Results ---");

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%d | %s %s | Age: %d | %s\n",
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("registration_date")
                    );
                }

                if (!found) {
                    System.out.println("No name found that matches \"" + search + "\".\n");
                }

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

            return;
        }
        else {
            System.out.println("Invalid choice.\n");
        }
    }

    // COUNT
    static int countParticipants() {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM participants"
            );
            ResultSet rs = ps.executeQuery();
            return rs.getInt(1);

        } catch (Exception e) {
            System.out.println("Error: " + e);
            return 0;
        }
    }
}
