public class Launcher {
    private String launcherId;
    private String name;
    private int maxPayloadKg;

    public Launcher(String launcherId, String name, int maxPayloadKg) {
        this.launcherId = launcherId;
        this.name = name;
        this.maxPayloadKg = maxPayloadKg;
    }

    public String getLauncherId() { return launcherId; }
    public String getName() { return name; }
    public int getMaxPayloadKg() { return maxPayloadKg; }
}