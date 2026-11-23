import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, Potato> potatoes = new HashMap<>();
        Map<String, Launcher> launchers = new HashMap<>();
        List<LaunchAttempt> attempts = new ArrayList<>();

        DataLoader.loadData(potatoes, launchers, attempts);

        try {
            ReportGenerator.writeAllReports(potatoes, launchers, attempts);
            System.out.println("All 5 reports written to output/");
        } catch (IOException e) {
            System.err.println("Error writing reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
}