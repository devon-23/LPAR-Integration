import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CustomAttributes {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the application name: ");
        String appName = scanner.nextLine();

        String csvFile = "> CSV FILE <";
        String line;
        String csvSplitBy = ",";
        int expectedColumns = 10; // Adjust this to match the number of columns in your CSV

        // Define the chart for OMVS.UID values
        Map<String, String> uidChart = new HashMap<>();
        uidChart.put("1000", "SA");
        uidChart.put("1001", "SY");
        uidChart.put("1002", "OP");
        uidChart.put("1060", "OP");

        String outputFilePath = System.getProperty("user.home") + "\\Desktop " + appName + " Custom Attributes Custom.xml";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile));
             FileWriter writer = new FileWriter(outputFilePath)) {

            writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            writer.write("<!DOCTYPE Custom PUBLIC \"sailpoint.dtd\" \"sailpoint.dtd\">\n");
            writer.write("<Custom name=\"XXX " + appName + " Custom Attributes\">\n");
            writer.write("  <Attributes>\n");
            writer.write("    <Map>\n");

            String headerLine = br.readLine();
            String[] headers = headerLine.split(csvSplitBy);

            while ((line = br.readLine()) != null) {
                StringBuilder entryBuilder = new StringBuilder(line);

                while (entryBuilder.toString().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").length < expectedColumns) {
                    line = br.readLine();
                    if (line != null) {
                        entryBuilder.append("\n").append(line);
                    } else {
                        break;
                    }
                }

                line = entryBuilder.toString().replaceAll(" {2,}", " ");

                String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (values.length < expectedColumns) {
                    continue; // Skip lines that don't have enough columns
                }

                String roleDisplayName = values[0];
                String roleNumber = values[2];
                String tsoInfo = values[6];

                String omvsUid = "defaultUID";
                for (String key : uidChart.keySet()) {
                    if (roleNumber.startsWith(key)) {
                        omvsUid = uidChart.get(key);
                        break;
                    }
                }

                // Parse TSO Info into a list of key-value pairs
                List<String[]> tsoList = new ArrayList<>();
                tsoInfo = tsoInfo.replace("TSO(", "").replace(")", "").replaceAll(" {2,}", " ");
                String[] tsoParts;
                if (tsoInfo.startsWith("\"") && tsoInfo.endsWith("\"")) {
                    tsoParts = tsoInfo.substring(1, tsoInfo.length() - 1).split(" ");
                } else {
                    tsoParts = tsoInfo.split(" ");
                }
                for (String part : tsoParts) {
                    String[] keyValue = part.split("\\(");
                    if (keyValue.length == 2) {
                        tsoList.add(new String[]{keyValue[0].trim(), "\"" + keyValue[1].replace(")", "").replace("'", "").trim() + "\""});
                    }
                }

                writer.write("      <entry key=\"" + appName + "\\" + roleNumber + ": " + roleDisplayName + "\">\n");
                writer.write("        <value>\n");
                writer.write("          <Map>\n");
                writer.write("            <entry key=\"OMVS.UID\" value=\"" + omvsUid + "\"/>\n");
                for (String[] tsoEntry : tsoList) {
                    String key = tsoEntry[0];
                    String value = tsoEntry[1];

                    if (value.matches("\"\\d+\"")) {
                        value = "\"" + Integer.parseInt(value.replaceAll("\"", "")) + "\"";
                    }
                    writer.write("            <entry key=\"TSO." + key + "\" value=" + value + "/>\n");
                }
                writer.write("            <entry key=\"UG_DEF\" value=\"" + values[5].replaceAll(" {2,}", " ") + "\"/>\n");
                writer.write("            <entry key=\"WORKATTR.WAADDR1\" value=\"" + roleNumber + "\"/>\n");
                writer.write("          </Map>\n");
                writer.write("        </value>\n");
                writer.write("      </entry>\n");
            }

            // Print the XML footer
            writer.write("    </Map>\n");
            writer.write("  </Attributes>\n");
            writer.write("</Custom>\n");

            System.out.println("Wrote to file " + outputFilePath + " on the desktop");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
