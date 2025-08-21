package homework.org.app.model;

public enum Status {
    ACTIVE("Активный"),
    UNDER_REVIEW("На проверке"),
    DELETED("Удалён");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
