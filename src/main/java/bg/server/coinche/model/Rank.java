package bg.server.coinche.model;

public enum Rank {
    Seven(0, 0), Eight(0, 0), Nine(0, 14),
    Ten(10, 10), Jack(2, 20), Queen(3, 3),
    King(4, 4), Ace(11, 11);
    private final int value;
    private final int atout;

    Rank(int value, int atout) {
        this.value = value;
        this.atout = atout;
    }

    public int getValue(boolean isAtout) {
        return isAtout ? atout : value;
    }

}
