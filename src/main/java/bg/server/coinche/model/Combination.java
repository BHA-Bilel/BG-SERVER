package bg.server.coinche.model;

import shared.RoomPosition;

import java.util.ArrayList;
import java.util.List;

public class Combination {

    private final ComboType type;
    private final Rank rank;
    private final Suit suit;
    private List<Card> cards;
    private RoomPosition position;

    // client
    public Combination(RoomPosition position, int suit, int rank, int type) {
        this.position = position;
        this.suit = Suit.values()[suit];
        this.rank = Rank.values()[rank];
        this.type = ComboType.values()[type];
        this.cards = new ArrayList<>();
        switch (this.type) {
            case JackCarre: {
                cards.add(new Card(position, Suit.Hearts, Rank.Jack));
                cards.add(new Card(position, Suit.Spades, Rank.Jack));
                cards.add(new Card(position, Suit.Diamonds, Rank.Jack));
                cards.add(new Card(position, Suit.Clubs, Rank.Jack));
                break;
            }
            case NineCarre: {
                cards.add(new Card(position, Suit.Hearts, Rank.Nine));
                cards.add(new Card(position, Suit.Spades, Rank.Nine));
                cards.add(new Card(position, Suit.Diamonds, Rank.Nine));
                cards.add(new Card(position, Suit.Clubs, Rank.Nine));
                break;
            }
            case Carre: {
                cards.add(new Card(position, Suit.Hearts, Rank.values()[rank]));
                cards.add(new Card(position, Suit.Spades, Rank.values()[rank]));
                cards.add(new Card(position, Suit.Diamonds, Rank.values()[rank]));
                cards.add(new Card(position, Suit.Clubs, Rank.values()[rank]));
                break;
            }
            case Suite:
            case Annonce:
            case Tierce: {
                cards.add(new Card(position, Suit.values()[suit], Rank.values()[rank]));
                cards.add(new Card(position, Suit.values()[suit], Rank.values()[rank - 1]));
                cards.add(new Card(position, Suit.values()[suit], Rank.values()[rank - 2]));
                if (this.type == ComboType.Tierce) break;
                cards.add(new Card(position, Suit.values()[suit], Rank.values()[rank - 3]));
                if (this.type == ComboType.Suite)
                    cards.add(new Card(position, Suit.values()[suit], Rank.values()[rank - 4]));
                break;
            }
            case Belote: {
                cards.add(new Card(position, Suit.values()[suit], Rank.Queen));
                cards.add(new Card(position, Suit.values()[suit], Rank.King));
                break;
            }
        }
    }

    // server
    private Combination(RoomPosition roomPosition, Suit suit, Rank rank, ComboType type) {
        this.position = roomPosition;
        this.suit = suit;
        this.rank = rank;
        this.type = type;
    }

    public static Combination checkForSuite(List<Card> anySuit) {
        if (anySuit.size() < 5) return null;
        Combination suite = null;
        for (int i = 0; i + 4 < anySuit.size(); i++) {
            int index1 = anySuit.get(i).getRank().ordinal();
            int index2 = anySuit.get(i + 1).getRank().ordinal();
            int index3 = anySuit.get(i + 2).getRank().ordinal();
            int index4 = anySuit.get(i + 3).getRank().ordinal();
            int index5 = anySuit.get(i + 4).getRank().ordinal();
            if (index1 + 1 == index2 && index2 + 1 == index3 && index3 + 1 == index4 && index4 + 1 == index5) {
                Card any_card = anySuit.get(0);
                suite = new Combination(any_card.getPosition(), any_card.getSuit(),
                        anySuit.get(i + 4).getRank(), ComboType.Suite);
            }
        }
        return suite;
    }

    public static Combination checkForAnnonce(List<Card> anySuit) {
        if (anySuit.size() < 4) return null;
        Combination annonce = null;
        for (int i = 0; i + 3 < anySuit.size(); i++) {
            int index1 = anySuit.get(i).getRank().ordinal();
            int index2 = anySuit.get(i + 1).getRank().ordinal();
            int index3 = anySuit.get(i + 2).getRank().ordinal();
            int index4 = anySuit.get(i + 3).getRank().ordinal();
            if (index1 + 1 == index2 && index2 + 1 == index3 && index3 + 1 == index4) {
                Card any_card = anySuit.get(0);
                annonce = new Combination(any_card.getPosition(), any_card.getSuit(),
                        anySuit.get(i + 3).getRank(), ComboType.Annonce);
            }
        }
        return annonce;
    }

    public static ArrayList<Combination> checkForTierce(List<Card> anySuit) {
        ArrayList<Combination> tierces = new ArrayList<>();
        if (anySuit.size() < 3) return tierces;
        for (int i = 0; i + 2 < anySuit.size(); i++) {
            int index1 = anySuit.get(i).getRank().ordinal();
            int index2 = anySuit.get(i + 1).getRank().ordinal();
            int index3 = anySuit.get(i + 2).getRank().ordinal();
            if (index1 + 1 == index2 && index2 + 1 == index3) {
                Card any_card = anySuit.get(0);
                tierces.add(new Combination(any_card.getPosition(), any_card.getSuit(),
                        anySuit.get(i + 2).getRank(), ComboType.Tierce));
            }
        }
        return tierces;
    }

    public static ArrayList<Combination> checkForCarre(List<Card> hearts, List<Card> spades,
                                                       List<Card> diamonds, List<Card> clubs) {
        ArrayList<Combination> carres = new ArrayList<>();
        if (hearts.isEmpty() || spades.isEmpty() || clubs.isEmpty() || diamonds.isEmpty()) return carres;
        for (Card spade : spades) {
            for (Card heart : hearts) {
                for (Card diamond : diamonds) {
                    for (Card club : clubs) {
                        if (spade.getRank() != Rank.Seven && spade.getRank() != Rank.Eight
                                && spade.getRank() == heart.getRank() && heart.getRank() == diamond.getRank()
                                && diamond.getRank() == club.getRank()) {
                            Rank temp_rank = spade.getRank();
                            carres.add(new Combination(spade.getPosition(), spade.getSuit(), temp_rank,
                                    temp_rank.equals(Rank.Jack) ? ComboType.JackCarre
                                            : temp_rank.equals(Rank.Nine) ? ComboType.NineCarre : ComboType.Carre
                            ));
                        }
                    }
                }
            }
        }
        return carres;
    }

    public static Combination checkForBelote(List<Card> trumpSuit) {
        if (trumpSuit.size() < 2) return null;
        Suit trump_suit = trumpSuit.get(0).getSuit();
        Card queen = new Card(trump_suit, Rank.Queen);
        Card king = new Card(trump_suit, Rank.King);
        boolean contain_queen = trumpSuit.contains(queen);
        boolean contain_king = trumpSuit.contains(king);
        if (contain_queen && contain_king) return new Combination(trumpSuit.get(0).getPosition(),
                trump_suit, Rank.King, ComboType.Belote);
        else return null;
    }

    /**
     * for server to send new game dominoes in adt_data
     */
    public static Integer[] to_array(List<Combination> combinations) {
        Integer[] array = new Integer[combinations.size() * 3 + 1];
        int i = 0;
        array[i++] = combinations.get(0).position.ordinal();
        for (Combination combination : combinations) {
            array[i++] = combination.suit.ordinal();
            array[i++] = combination.rank.ordinal();
            array[i++] = combination.type.ordinal();
        }
        return array;
    }

    /**
     * for clients to get new game dominoes from game start adt_data
     */
    public static ArrayList<Combination> to_list(RoomPosition decl_position, Integer[] array) {
        ArrayList<Combination> combos = new ArrayList<>();
        int i = 1;
        while (i < array.length) combos.add(new Combination(decl_position,
                array[i++], array[i++], array[i++]));
        return combos;
    }

    public ComboType getType() {
        return type;
    }

    public Rank getRank() {
        return rank;
    }

    public List<Card> getCards() {
        return cards;
    }

    public RoomPosition getPosition() {
        return position;
    }

    public void setPosition(RoomPosition roomPosition) {
        this.position = roomPosition;
    }

    public Suit getSuit() {
        return suit;
    }
}
