package coinche.model;

public class Card {
	// LOGIC
	private final Suit suit;
	private final Rank rank;
	private Position position;

	public Card(Position position, Suit suit, Rank rank) { // FOR SERVER ONLY (deck)
		this.position = position;
		this.suit = suit;
		this.rank = rank;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (this.getClass() != obj.getClass())
			return false;
		Card other = (Card) obj;
		return other.rank == rank && other.suit == suit;
	}

	// GETTERS SETTERS

	public Suit getSuit() {
		return suit;
	}

	public Rank getRank() {
		return rank;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
