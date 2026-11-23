public abstract class Potato implements Scoreable {
    private String potatoId;
    private String name;
    private String type;

    public Potato(String potatoId, String name, String type) {
        this.potatoId = potatoId;
        this.name = name;
        this.type = type;
    }

    public String getPotatoId() { return potatoId; }
    public String getName() { return name; }
    public String getType() { return type; }
}