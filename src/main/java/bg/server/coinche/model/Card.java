package bg.server.coinche.model;

import shared.RoomPosition;

import java.util.ArrayList;
import java.util.List;

public class Card {

    private final Suit suit;
    private final Rank rank;
    private RoomPosition position;

    // server
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    // declaration
    public Card(RoomPosition position, Suit suit, Rank rank) {
        this.position = position;
        this.suit = suit;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (this.getClass() != obj.getClass()) return false;
        Card other = (Card) obj;
        return other.rank == rank && other.suit == suit;
    }

    /**
     * for server to send new game dominoes in adt_data
     */
    public static Integer[] to_array(Hand hand) {
        Integer[] array = new Integer[hand.getCards().size() * 2];
        int i = 0;
        for (Card card : hand.getCards()) {
            array[i++] = card.getSuit().ordinal();
            array[i++] = card.getRank().ordinal();
        }
        return array;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public RoomPosition getPosition() {
        return position;
    }

    public void setPosition(RoomPosition position) {
        this.position = position;
    }

}
