package bg.server.coinche.model;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
	private final ArrayList<Card> cards;
	private final Random rand;

	public Deck() {
		cards = new ArrayList<>();
		rand = new Random();
	}

	public void populate() {
		for (Suit suit : Suit.values()) {
			if (suit != Suit.SA && suit != Suit.TA) {
				for (Rank rank : Rank.values()) {
					cards.add(new Card(Position.BOTTOM, suit, rank));
				}
			}
		}
	}

	public void shuffle() {
		for (int i = cards.size() - 1; i > 0; i--) {
			int randIndex = rand.nextInt(i);
			Card randCard = cards.set(randIndex, cards.get(i));
			cards.set(i, randCard);
		}
	}

	public ArrayList<Card> getCards() {
		return cards;
	}

	public void remove(Card selected) {
		cards.remove(selected);
	}
}
