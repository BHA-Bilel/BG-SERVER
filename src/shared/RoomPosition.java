package shared;

public enum RoomPosition {
    BOTTOM, RIGHT, TOP, LEFT;

    public RoomPosition nextPlayerToNotify() {
        return switch (this) {
            case BOTTOM -> RIGHT;
            case RIGHT -> TOP;
            case TOP -> LEFT;
            default -> null;
        };
    }

    public RoomPosition teammate_with() {
        return switch (this) {
            case BOTTOM -> TOP;
            case LEFT -> RIGHT;
            case TOP -> BOTTOM;
            case RIGHT -> LEFT;
        };
    }

}
