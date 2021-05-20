package bg.server.coinche.model;

public enum Suit {
	Hearts(0), Spades(1), Diamonds(2), Clubs(3), SA(4), TA(5);

	private final int index;

	Suit(int index) {
		this.index = index;
	}

	public static Suit get(int suitIndex) {
		for (Suit name : Suit.values()) {
			if (suitIndex == name.getIndex()) {
				return name;
			}
		}
		return null;
	}

	public int getIndex() {
		return index;
	}
}
