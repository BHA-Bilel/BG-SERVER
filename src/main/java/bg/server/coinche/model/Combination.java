package bg.server.coinche.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Combination {

	private final ComboType type;
	private final Rank rank;
	private final List<Card> cards;
	private Position position;

	private Combination(Position position, ComboType type, List<Card> cards, Rank rank) {
		this.position = position;
		this.type = type;
		this.cards = cards;
		this.rank = rank;
	}

	public static Combination checkForSuite(List<Card> anySuit) {
		if (anySuit.size() < 5)
			return null;
		Combination suite = null;
		for (int i = 0; i + 4 < anySuit.size(); i++) {
			int index1 = anySuit.get(i).getRank().getIndex();
			int index2 = anySuit.get(i + 1).getRank().getIndex();
			int index3 = anySuit.get(i + 2).getRank().getIndex();
			int index4 = anySuit.get(i + 3).getRank().getIndex();
			int index5 = anySuit.get(i + 4).getRank().getIndex();
			if (index1 + 1 == index2 && index2 + 1 == index3 && index3 + 1 == index4 && index4 + 1 == index5) {
				ArrayList<Card> cards = new ArrayList<>();
				cards.add(anySuit.get(i));
				cards.add(anySuit.get(i + 1));
				cards.add(anySuit.get(i + 2));
				cards.add(anySuit.get(i + 3));
				cards.add(anySuit.get(i + 4));
				suite = new Combination(anySuit.get(0).getPosition(), ComboType.Suite, cards,
						anySuit.get(i + 4).getRank());
			}
		}
		return suite;
	}

	public static Combination checkForAnnonce(List<Card> anySuit) {
		if (anySuit.size() < 4)
			return null;
		Combination annonce = null;
		for (int i = 0; i + 3 < anySuit.size(); i++) {
			int index1 = anySuit.get(i).getRank().getIndex();
			int index2 = anySuit.get(i + 1).getRank().getIndex();
			int index3 = anySuit.get(i + 2).getRank().getIndex();
			int index4 = anySuit.get(i + 3).getRank().getIndex();
			if (index1 + 1 == index2 && index2 + 1 == index3 && index3 + 1 == index4) {
				ArrayList<Card> cards = new ArrayList<>();
				cards.add(anySuit.get(i));
				cards.add(anySuit.get(i + 1));
				cards.add(anySuit.get(i + 2));
				cards.add(anySuit.get(i + 3));
				annonce = new Combination(anySuit.get(0).getPosition(), ComboType.Annonce, cards,
						anySuit.get(i + 3).getRank());
			}
		}
		return annonce;
	}

	public static ArrayList<Combination> checkForTierce(List<Card> anySuit) {
		ArrayList<Combination> tierces = new ArrayList<>();
		if (anySuit.size() < 3)
			return tierces;
		for (int i = 0; i + 2 < anySuit.size(); i++) {
			int index1 = anySuit.get(i).getRank().getIndex();
			int index2 = anySuit.get(i + 1).getRank().getIndex();
			int index3 = anySuit.get(i + 2).getRank().getIndex();
			if (index1 + 1 == index2 && index2 + 1 == index3) {
				ArrayList<Card> cards = new ArrayList<>();
				cards.add(anySuit.get(i));
				cards.add(anySuit.get(i + 1));
				cards.add(anySuit.get(i + 2));
				tierces.add(new Combination(anySuit.get(0).getPosition(), ComboType.Tierce, cards,
						anySuit.get(i + 2).getRank()));
			}
		}
		return tierces;
	}

	public static ArrayList<Combination> checkForCarre(List<Card> hearts, List<Card> spades, List<Card> diamonds,
													   List<Card> clubs) {
		ArrayList<Combination> carres = new ArrayList<>();
		if (hearts.isEmpty() || spades.isEmpty() || clubs.isEmpty() || diamonds.isEmpty())
			return carres;
		for (Card spade : spades) {
			for (Card heart : hearts) {
				for (Card diamond : diamonds) {
					for (Card club : clubs) {
						if (spade.getRank() != Rank.Seven && spade.getRank() != Rank.Eight
								&& spade.getRank() == heart.getRank() && heart.getRank() == diamond.getRank()
								&& diamond.getRank() == club.getRank()) {
							ArrayList<Card> cards = new ArrayList<>();
							cards.add(spade);
							cards.add(heart);
							cards.add(diamond);
							cards.add(club);
							carres.add(new Combination(spade.getPosition(),
									spade.getRank() == Rank.Jack ? ComboType.JackCarre
											: spade.getRank() == Rank.Nine ? ComboType.NineCarre : ComboType.Carre,
									cards, spade.getRank()));
						}
					}
				}
			}
		}
		return carres;
	}

	public static Combination checkForBelote(List<Card> trumpSuit) {
		if (trumpSuit.size() < 2)
			return null;
		int queen = -1;
		int king = -1;
		for (int i = 0; i < trumpSuit.size(); i++) {
			if (trumpSuit.get(i).getRank() == Rank.Queen)
				queen = i;
			else if (trumpSuit.get(i).getRank() == Rank.King)
				king = i;
		}
		if (queen != -1 && king != -1) {
			ArrayList<Card> cards = new ArrayList<>();
			cards.add(trumpSuit.get(queen));
			cards.add(trumpSuit.get(king));
			return new Combination(trumpSuit.get(0).getPosition(), ComboType.Belote, cards, Rank.King);
		} else
			return null;
	}

	public ComboType getType() {
		return type;
	}

	public Rank getRank() {
		return rank;
	}

	@Override
	public String toString() {
		return "Position :" + position + " type : " + type + " rank : " + rank + "\ncards : "
				+ Arrays.toString(cards.toArray());
	}

	public List<Card> getCards() {
		return cards;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
