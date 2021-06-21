package bg.server.coinche.model;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private final ArrayList<Card> cards, populated;

    public Deck() {
        cards = new ArrayList<>();
        populated = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            if (suit == Suit.SA || suit == Suit.TA) continue;
            for (Rank rank : Rank.values()) populated.add(new Card(suit, rank));
        }
    }

    public void prepare() {
        cards.clear();
        cards.addAll(populated);
        Collections.shuffle(cards);
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

}
