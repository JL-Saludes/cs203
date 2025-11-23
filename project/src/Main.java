import java.sql.*;
import java.util.Scanner;

public class Main {

    static Connection connection;
    static Statement statement;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database/event_registration.db");
            statement = connection.createStatement();
            System.out.println("Connected to DATABASE successfully\n");

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS participants (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "age INTEGER, " +
                "registration_date TEXT DEFAULT (datetime('now','localtime'))" +
                ");"
            );

            while (true) {
                int participant = count();
                System.out.print(
                    "=========================\n" +
                    "Event Registration System\n" +
                    "=========================\n" +
                    "Total participant: " + participant +
                    "\n\n[1] Register participant \t:working\n" +
                    "[2] Remove Participant \t\t:working\n" +
                    "[3] Display all participants \t:working\n" +
                    "[4] Search Participant \t\t:underconstruction\n" +
                    "[5] Exit Program\n\n"
                );

                int command = 0;
                try {
                    System.out.print("Enter a Command: ");
                    command = scanner.nextInt();
                    scanner.nextLine(); // clear buffer
                } catch (Exception e) {
                    System.out.println("\nOnly Enter a number\n");
                    scanner.nextLine();
                    continue;
                }

                if (command < 1 || command > 5) {
                    System.out.println(command + " is not a valid command.");
                    continue;
                }

                if (command == 5) { // Exit
                    if (statement != null) statement.close();
                    if (connection != null) connection.close();
                    if (scanner != null) scanner.close();
                    System.out.println("--Saving Changes\n--Terminating\nGoodbye!");
                    break;
                }

                registration(command);
            }

        } catch (SQLException e) {
            System.out.println("An Error occurred: ");
            System.out.println(e);
        }
    }

    static void registration(int i) {

        switch (i) {

            case 1: // Register participant
                String first_name, last_name;

                System.out.print("Enter First Name: ");
                first_name = scanner.nextLine();

                System.out.print("Enter Last Name: ");
                last_name = scanner.nextLine();

                int age = -1;

                while (true) {
                    try {
                        System.out.print("Enter age: ");
                        age = scanner.nextInt();

                        if (age < 0 || age > 100) {
                            System.out.println("Invalid age! Try again.");
                            scanner.nextLine();
                            continue;
                        }

                        scanner.nextLine(); // clear buffer
                        break;

                    } catch (Exception e) {
                        System.out.println("Not a valid age. Try again.");
                        scanner.nextLine();
                    }
                }

                try {
                    String sql = "INSERT INTO participants(first_name, last_name, age) VALUES(?, ?, ?)";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);

                    preparedStatement.setString(1, first_name);
                    preparedStatement.setString(2, last_name);
                    preparedStatement.setInt(3, age);
                    preparedStatement.executeUpdate();

                    System.out.println("\nParticipant registered successfully!\n");

                } catch (Exception e) {
                    System.out.println(e);
                }
                break;

            case 2: // Remove participant
                int id = -1;

                while (true) {
                    try {
                        System.out.print("Enter ID to Remove: ");
                        id = scanner.nextInt();
                        scanner.nextLine();
                        break;
                    } catch (Exception e) {
                        System.out.println("Invalid. Try again");
                        scanner.nextLine();
                    }
                }

                try {
                    String sql = "DELETE FROM participants WHERE id = ?";
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, id);

                    int row = preparedStatement.executeUpdate();

                    if (row > 0) {
                        System.out.println("Participant ID: " + id + " was removed");
                    } else {
                        System.out.println("ID: " + id + " was NOT found");
                    }

                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
                break;

            case 3: // Display & Sort
                String sort = "id"; // default sort
                while (true) {
                    try {
                        String sql = "SELECT * FROM participants ORDER BY " + sort + " ASC";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        ResultSet result = preparedStatement.executeQuery();

                        System.out.println("\n--- Participants ---");
                        System.out.printf("%-4s | %-12s | %-12s | %-4s | %-20s%n",
                                "ID", "First Name", "Last Name", "Age", "Registration Date");
                        System.out.println("-----+--------------+--------------+------+----------------------");

                        boolean empty = true;
                        while (result.next()) {
                            empty = false;
                            System.out.printf("%-4d | %-12s | %-12s | %-4d | %-20s%n",
                                    result.getInt("id"),
                                    result.getString("first_name"),
                                    result.getString("last_name"),
                                    result.getInt("age"),
                                    result.getString("registration_date")
                            );
                        }

                        result.close();
                        preparedStatement.close();

                        if (empty) {
                            System.out.println("(No participants found)");
                            break;
                        }

                        System.out.print("\nSort By:\n[1] First name\n[2] Last name\n[3] Age\n[4] Return to Menu\nEnter a Command: ");
                        int sortChoice = 0;

                        try {
                            sortChoice = scanner.nextInt();
                            scanner.nextLine(); // clear buffer
                        } catch (Exception e) {
                            System.out.println("Invalid choice");
                            scanner.nextLine();
                            continue;
                        }

                        if (sortChoice == 1) {
                            sort = "first_name";
                        } else if (sortChoice == 2) {
                            sort = "last_name";
                        } else if (sortChoice == 3) {
                            sort = "age";
                        } else if (sortChoice == 4) {
                            break; // exit sort loop
                        } else {
                            sort = "id";
                            System.out.println("Invalid choice. Keeping default order by ID.");
                        }

                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }
                }
                break;

            case 4: // Search participant (empty, left as is)
            default:
                break;
        }
    }

    static int count() {
        try {
            String sql = "SELECT COUNT(*) FROM participants";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet result = preparedStatement.executeQuery();

            result.next();
            return result.getInt(1);

        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }
}
