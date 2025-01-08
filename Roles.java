package roles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Roles {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the application name: ");
        String appName = scanner.nextLine().trim();

        String csvFile = "> RbacRoles_Updatedfiltered_expor csv file location <"; 
        String workgroupsFile = "> WorkGroups & Member Apprvrs csv file location <"; 
        String line;
        String csvSplitBy = ",";
        int expectedColumns = 10;

        String outputFilePath = System.getProperty("user.home") + "\\Desktop\\create_roles_" + appName + ".csv";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             FileWriter writer = new FileWriter(outputFilePath)) {

            // Write the header for the output
            writer.write("name,displayName,description,role_parent_folder,application,groups,special_groups,owner_group\n");

            // Read and skip the header line
            String headerLine = br.readLine();
            String[] headers = headerLine.split(csvSplitBy);

            while ((line = br.readLine()) != null) {
                StringBuilder entryBuilder = new StringBuilder(line);

                // Keep reading lines until we have the correct number of columns
                while (entryBuilder.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").length < expectedColumns) {
                    line = br.readLine();
                    if (line != null) {
                        entryBuilder.append("\n").append(line);
                    } else {
                        break;
                    }
                }

                line = entryBuilder.toString().replaceAll(" {2,}", " ");
                // Use a regular expression to handle quoted commas
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (values.length < expectedColumns) {
                    continue; // Skip lines that don't have enough columns
                }

                // Trim all entries
                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim().replaceAll(" {2,}", " ");
                }

                String roleDisplayName = values[0];
                String roleNumber = values[2];
                String description = values[1];
                String groups = values[3].replace("\n", " ").replaceAll(" {2,}", " ");
                String specialGroups = values[4].replace("\n", " ").replaceAll(" {2,}", " ");

                String[] splitGroups = groups.split(" ");
                String modifiedGroups = String.join(",", splitGroups);
                String[] splitSpecialGroups = specialGroups.split(" ");
                for (int i = 0; i < splitSpecialGroups.length; i++) {
                    splitSpecialGroups[i] = splitSpecialGroups[i].toUpperCase();
                }
                String modifiedSpecialGroups = String.join(",", splitSpecialGroups);

                String ownerGroup = getOwnerGroup(modifiedGroups, workgroupsFile);

                String name = appName + "\\" + roleNumber + ": " + roleDisplayName;
                String displayName = name;
                String roleParentFolder = appName;
                String application = appName;

                writer.write(name + "," +
                             displayName + "," +
                             description + "," +
                             roleParentFolder + "," +
                             application + "," +
                             modifiedGroups + "," +
                             modifiedSpecialGroups + "," +
                             ownerGroup + "\n");
            }

            System.out.println("Wrote to file " + outputFilePath + " on the desktop");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getOwnerGroup(String groups, String workgroupsFile) {
        String[] splitGroups = groups.split(",");
        String targetGroup = null;

        // Find the group with an 'X' in it
        for (String group : splitGroups) {
            if (group.contains("X")) {
                targetGroup = group;
                break;
            }
        }

        if (targetGroup == null) {
            return "None Found"; // Default placeholder if no target group found
        }

        targetGroup = targetGroup.replaceAll("\"$", "");

        try (BufferedReader br = new BufferedReader(new FileReader(workgroupsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                String approvers = values[1];
                if (approvers.contains(targetGroup)) {
                    return values[0]; // Return the name of the owner group
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "None Found"; // Default placeholder if no matching approver found
    }
}
