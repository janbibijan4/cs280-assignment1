import java.io.File;
import java.util.*;

public class DataLoader {
    public static void loadData(
            Map<String, Potato> potatoMap,
            Map<String, Launcher> launcherMap,
            List<LaunchAttempt> attemptList) {
        try {
            Scanner scanner = new Scanner(new File("input/potatoes.csv"));
            if (scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 3) {
                    String id = parts[0];
                    String name = parts[1];
                    String type = parts[2];
                    Potato potato = createPotato(id, name, type);
                    potatoMap.put(id, potato);
                }
            }
            scanner.close();

            scanner = new Scanner(new File("input/launchers.csv"));
            if (scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 3) {
                    Launcher launcher = new Launcher(parts[0], parts[1], Integer.parseInt(parts[2]));
                    launcherMap.put(parts[0], launcher);
                }
            }
            scanner.close();

            scanner = new Scanner(new File("input/launch_attempts.csv"));
            if (scanner.hasNextLine()) scanner.nextLine();
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length >= 5) {
                    String attemptId = parts[0];
                    Potato potato = potatoMap.get(parts[1]);
                    Launcher launcher = launcherMap.get(parts[2]);
                    int payload = Integer.parseInt(parts[3]);
                    int rating = Integer.parseInt(parts[4]);
                    LaunchAttempt attempt = new LaunchAttempt(attemptId, potato, launcher, payload, rating);
                    attemptList.add(attempt);
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Potato createPotato(String id, String name, String type) {
        return switch (type) {
            case "FINGERLING" -> new FingerlingPotato(id, name);
            case "PURPLE" -> new PurplePotato(id, name);
            case "RUSSET" -> new RussetPotato(id, name);
            case "YUKON_GOLD" -> new YukonGoldPotato(id, name);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}