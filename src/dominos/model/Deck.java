package dominos.model;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
	private final ArrayList<Domino> dominos;
	private final Random rand;

	public Deck() {
		dominos = new ArrayList<>();
		rand = new Random();
	}

	public void populate() {
		for (int i = 0; i < 7; i++) {
			for (int j = i; j < 7; j++) {
				dominos.add(new Domino(i, j, Position.CENTER));
			}
		}
	}

	public void shuffle() {
		for (int i = dominos.size() - 1; i > 0; i--) {
			int randIndex = rand.nextInt(i);
			Domino randCard = dominos.set(randIndex, dominos.get(i));
			dominos.set(i, randCard);
		}
	}

	public ArrayList<Domino> getDominos() {
		return dominos;
	}

	public void remove(Domino selected) {
		dominos.remove(selected);
	}
}
