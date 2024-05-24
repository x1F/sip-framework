package ${package}.connectorgroups.${connectorGroup1}.models;

public enum NobelPrizeCategory {
    PHYSICS("phy"),
    CHEMISTRY("che"),
    MEDICINE("med"),
    LITERATURE("lit"),
    PEACE("pea"),
    ECONOMICS("eco");

    private final String value;

    NobelPrizeCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
