package celsiuss.wynnquestmap;

public class Quest {
    private String name;
    private boolean started;
    private String coords;
    private String description;

    public Quest(String name, boolean started, String coords, String description) {
        this.name = name;
        this.started = started;
        this.coords = coords;
        this.description = description;
    }
}
