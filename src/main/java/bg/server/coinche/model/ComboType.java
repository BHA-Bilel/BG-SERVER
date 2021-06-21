package bg.server.coinche.model;

public enum ComboType {
    JackCarre(200), NineCarre(150),
    Carre(100), Suite(100),
    Annonce(50), Tierce(20),
    Belote(20);

    private final int value;

    ComboType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
