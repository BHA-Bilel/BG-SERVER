package coinche.model;

public enum Rank {
	Seven(0, 0, 0), Eight(1, 0, 0), Nine(2, 0, 14), Ten(3, 10, 10), Jack(4, 2, 20), Queen(5, 3, 3), King(6, 4, 4),
	Ace(7, 11, 11);
	private final int index;
	private final int value;
	private final int atout;

	Rank(int index, int value, int atout) {
		this.index = index;
		this.value = value;
		this.atout = atout;
	}

	public int getIndex() {
		return index;
	}

	public static Rank get(int index) {
		for (Rank name : Rank.values()) {
			if (name.getIndex() == index) {
				return name;
			}
		}
		return null;
	}
}
