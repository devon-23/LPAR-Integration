import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class wg {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Prompt the user for application name and members
        System.out.print("Enter the application name: ");
        String appName = scanner.nextLine().trim();

        System.out.print("Enter the members (comma-separated): ");
        String userMembers = scanner.nextLine().trim();

        String inputFilePath = "> Path to workgrops CSV <"; // Path to your input CSV file
        String line;
        String csvSplitBy = ",";

        String outputFilePath = System.getProperty("user.home") + "\\Desktop\\import_workgroups_" + appName + ".csv";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
             FileWriter writer = new FileWriter(outputFilePath)) {

            // Write the header
            writer.write("Workgroup Name,Workgroup Description,Members,Owner,Capabilities,Email,Notification Option\n");

            // Write the user-inputted first entry
            writer.write(String.format("XXX %s XXX,Application Owner Group for %s,\"%s\",XXXX,,,Both\n",
                    appName, appName, userMembers.replaceAll(" {2,}", " ")));

            while ((line = br.readLine()) != null) {
                // Use comma as separator, but handle quoted fields
                String[] columns = parseCSVLine(line);

                // Check if the row has at least 6 columns (Notification Option is optional)
                if (columns.length >= 6) {
                    // Process the Members field
                    String members = columns[2].replace("E", "").replace(";", ",").trim().replaceAll(" {2,}", " ");
                    // Add quotes if there is more than one workgroup
                    if (members.contains(",")) {
                        members = "\"" + members + "\"";
                    }

                    // Write the formatted output with trimmed inputs and quotes around Workgroup Description
                    writer.write(String.format("%s,\"%s\",%s,%s,%s,%s,%s\n",
                            columns[0].trim().replaceAll(" {2,}", " "), // Workgroup Name
                            columns[1].trim().replaceAll(" {2,}", " "), // Workgroup Description
                            members,           // Members
                            columns[3].trim().replaceAll(" {2,}", " "), // Owner
                            "",                // Capabilities (empty)
                            "",                // Email (empty)
                            columns.length > 6 ? columns[6].trim().replaceAll(" {2,}", " ") : ""  // Notification Option (if present)
                    ));
                } else {
                    System.err.println("Skipping malformed row: " + line);
                }
            }

            System.out.println("Wrote to file " + outputFilePath + " on the desktop");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] parseCSVLine(String line) {
        // This method handles quoted fields and splits the line correctly
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        java.util.List<String> tokens = new java.util.ArrayList<>();

        for (char ch : line.toCharArray()) {
            if (ch == '\"') {
                inQuotes = !inQuotes; // toggle state
            } else if (ch == ',' && !inQuotes) {
                tokens.add(sb.toString().trim().replaceAll(" {2,}", " "));
                sb.setLength(0); // reset the buffer
            } else {
                sb.append(ch);
            }
        }
        tokens.add(sb.toString().trim().replaceAll(" {2,}", " ")); // add the last token

        return tokens.toArray(new String[0]);
    }
}
