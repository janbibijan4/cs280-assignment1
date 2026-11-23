import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ReportGenerator {

    public static void writeAllReports(
            Map<String, Potato> potatoes,
            Map<String, Launcher> launchers,
            List<LaunchAttempt> attempts) throws IOException {

        File outDir = new File("output");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        writeReport1(potatoes, attempts, new File(outDir, "01_potato_flight_log.csv"));
        writeReport2(launchers, attempts, new File(outDir, "02_launcher_activity.csv"));
        writeReport3(attempts, new File(outDir, "03_launch_results_by_pair.csv"));
        writeReport4(attempts, new File(outDir, "04_payload_violations.csv"));
        writeReport5(attempts, new File(outDir, "05_best_test_flights.csv"));
    }

    // Report 1: potatoId,name,type,attemptCount,totalPoints (sorted by potatoId ascending)
    private static void writeReport1(
            Map<String, Potato> potatoes,
            List<LaunchAttempt> attempts,
            File file) throws IOException {

        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        for (LaunchAttempt attempt : attempts) {
            String pid = attempt.getPotato().getPotatoId();
            counts.put(pid, counts.getOrDefault(pid, 0) + 1);
            points.put(pid, points.getOrDefault(pid, 0) + attempt.getPoints());
        }

        List<String> sortedIds = new ArrayList<>(potatoes.keySet());
        Collections.sort(sortedIds);

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("potatoId,name,type,attemptCount,totalPoints");
            for (String pid : sortedIds) {
                Potato p = potatoes.get(pid);
                int c = counts.getOrDefault(pid, 0);
                int pts = points.getOrDefault(pid, 0);
                out.println(p.getPotatoId() + "," + p.getName() + "," + p.getType() + "," + c + "," + pts);
            }
        }
    }

    // Report 2: launcherId,name,attemptCount,totalPayloadKg,overLimitCount (sorted by launcherId ascending)
    private static void writeReport2(
            Map<String, Launcher> launchers,
            List<LaunchAttempt> attempts,
            File file) throws IOException {

        Map<String, Integer> counts = new HashMap<>();
        Map<String, Integer> payloads = new HashMap<>();
        Map<String, Integer> overLimits = new HashMap<>();

        for (LaunchAttempt attempt : attempts) {
            String lid = attempt.getLauncher().getLauncherId();
            counts.put(lid, counts.getOrDefault(lid, 0) + 1);
            payloads.put(lid, payloads.getOrDefault(lid, 0) + attempt.getPayloadKg());
            if (attempt.isOverLimit()) {
                overLimits.put(lid, overLimits.getOrDefault(lid, 0) + 1);
            }
        }

        List<String> sortedIds = new ArrayList<>(launchers.keySet());
        Collections.sort(sortedIds);

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("launcherId,name,attemptCount,totalPayloadKg,overLimitCount");
            for (String lid : sortedIds) {
                Launcher l = launchers.get(lid);
                int c = counts.getOrDefault(lid, 0);
                int pay = payloads.getOrDefault(lid, 0);
                int over = overLimits.getOrDefault(lid, 0);
                out.println(l.getLauncherId() + "," + l.getName() + "," + c + "," + pay + "," + over);
            }
        }
    }

    // Report 3: potatoId,launcherId,attemptCount,totalPoints (sorted by potatoId asc, then launcherId asc)
    private static void writeReport3(List<LaunchAttempt> attempts, File file) throws IOException {
        Map<String, int[]> pairStats = new HashMap<>(); // key: "P01|L01" -> [attemptCount, totalPoints]

        for (LaunchAttempt attempt : attempts) {
            String pid = attempt.getPotato().getPotatoId();
            String lid = attempt.getLauncher().getLauncherId();
            String key = pid + "|" + lid;
            int[] stats = pairStats.getOrDefault(key, new int[]{0, 0});
            stats[0] += 1;
            stats[1] += attempt.getPoints();
            pairStats.put(key, stats);
        }

        List<String> keys = new ArrayList<>(pairStats.keySet());
        keys.sort((a, b) -> {
            String[] pa = a.split("\\|");
            String[] pb = b.split("\\|");
            int cmpPotato = pa[0].compareTo(pb[0]);
            if (cmpPotato != 0) return cmpPotato;
            return pa[1].compareTo(pb[1]);
        });

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("potatoId,launcherId,attemptCount,totalPoints");
            for (String key : keys) {
                String[] parts = key.split("\\|");
                int[] stats = pairStats.get(key);
                out.println(parts[0] + "," + parts[1] + "," + stats[0] + "," + stats[1]);
            }
        }
    }

    // Report 4: attemptId,potatoId,launcherId,payloadKg,maxPayloadKg,excessKg (excessKg desc, attemptId asc)
    private static void writeReport4(List<LaunchAttempt> attempts, File file) throws IOException {
        List<LaunchAttempt> violations = new ArrayList<>();
        for (LaunchAttempt attempt : attempts) {
            if (attempt.isOverLimit()) {
                violations.add(attempt);
            }
        }

        violations.sort((a, b) -> {
            int cmpExcess = Integer.compare(b.getExcessKg(), a.getExcessKg()); // descending
            if (cmpExcess != 0) return cmpExcess;
            return a.getAttemptId().compareTo(b.getAttemptId()); // ascending
        });

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("attemptId,potatoId,launcherId,payloadKg,maxPayloadKg,excessKg");
            for (LaunchAttempt a : violations) {
                out.println(
                        a.getAttemptId() + "," +
                                a.getPotato().getPotatoId() + "," +
                                a.getLauncher().getLauncherId() + "," +
                                a.getPayloadKg() + "," +
                                a.getLauncher().getMaxPayloadKg() + "," +
                                a.getExcessKg()
                );
            }
        }
    }

    // Report 5: attemptId,potatoName,launcherName,flightRating,payloadKg,points (top 3 via Comparable)
    private static void writeReport5(List<LaunchAttempt> attempts, File file) throws IOException {
        List<LaunchAttempt> sorted = new ArrayList<>(attempts);
        Collections.sort(sorted); // uses LaunchAttempt.compareTo(...)

        int limit = Math.min(3, sorted.size());

        try (PrintWriter out = new PrintWriter(file)) {
            out.println("attemptId,potatoName,launcherName,flightRating,payloadKg,points");
            for (int i = 0; i < limit; i++) {
                LaunchAttempt a = sorted.get(i);
                out.println(
                        a.getAttemptId() + "," +
                                a.getPotato().getName() + "," +
                                a.getLauncher().getName() + "," +
                                a.getFlightRating() + "," +
                                a.getPayloadKg() + "," +
                                a.getPoints()
                );
            }
        }
    }
}