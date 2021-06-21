package bg.server.coinche.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Hand {

    private ArrayList<Combination> declarations;
    private Combination highestComb;
    private ArrayList<Card> cards;

    public Hand(ArrayList<Card> cards) {
        this.cards = cards;
        arrange();
    }

    public List<Card> getSpades() {
        List<Card> spades = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == Suit.Spades) {
                spades.add(card);
            }
        }
        for (int i = 0; i < spades.size(); i++) {
            int index = i;
            for (int j = i + 1; j < spades.size(); j++) {
                if (spades.get(j).getRank().ordinal() < spades.get(index).getRank().ordinal())
                    index = j;
            }
            if (index != i) {
                Card other = spades.set(i, spades.get(index));
                spades.set(index, other);
            }
        }
        return spades;
    }

    public List<Card> getHearts() {
        List<Card> hearts = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == Suit.Hearts) {
                hearts.add(card);
            }
        }
        for (int i = 0; i < hearts.size(); i++) {
            int index = i;
            for (int j = i + 1; j < hearts.size(); j++) {
                if (hearts.get(j).getRank().ordinal() < hearts.get(index).getRank().ordinal())
                    index = j;
            }
            if (index != i) {
                Card other = hearts.set(i, hearts.get(index));
                hearts.set(index, other);
            }
        }
        return hearts;
    }

    public List<Card> getClubs() {
        List<Card> clubs = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == Suit.Clubs) {
                clubs.add(card);
            }
        }
        for (int i = 0; i < clubs.size(); i++) {
            int index = i;
            for (int j = i + 1; j < clubs.size(); j++) {
                if (clubs.get(j).getRank().ordinal() < clubs.get(index).getRank().ordinal())
                    index = j;
            }
            if (index != i) {
                Card other = clubs.set(i, clubs.get(index));
                clubs.set(index, other);
            }
        }
        return clubs;
    }

    public List<Card> getDiamonds() {
        List<Card> diamonds = new ArrayList<>();
        for (Card card : cards) {
            if (card.getSuit() == Suit.Diamonds) {
                diamonds.add(card);
            }
        }
        for (int i = 0; i < diamonds.size(); i++) {
            int index = i;
            for (int j = i + 1; j < diamonds.size(); j++) {
                if (diamonds.get(j).getRank().ordinal() < diamonds.get(index).getRank().ordinal())
                    index = j;
            }
            if (index != i) {
                Card other = diamonds.set(i, diamonds.get(index));
                diamonds.set(index, other);
            }
        }
        return diamonds;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void remove(Card card) {
        cards.remove(card);
    }

    @Override
    public String toString() {
        return Arrays.toString(cards.toArray());
    }

    public void arrange() {
        List<Card> spades = getSpades();
        List<Card> hearts = getHearts();
        List<Card> clubs = getClubs();
        List<Card> diamonds = getDiamonds();
        cards = new ArrayList<>();
        if (!hearts.isEmpty() && spades.isEmpty() && !diamonds.isEmpty() && !clubs.isEmpty()) {
            cards.addAll(hearts);
            cards.addAll(clubs);
            cards.addAll(diamonds);
        } else if (!hearts.isEmpty() && !spades.isEmpty() && diamonds.isEmpty() && !clubs.isEmpty()) {
            cards.addAll(spades);
            cards.addAll(hearts);
            cards.addAll(clubs);
        } else {
            cards.addAll(hearts);
            cards.addAll(spades);
            cards.addAll(diamonds);
            cards.addAll(clubs);
        }
    }

    public void check(Suit trump) {
        List<Card> spades = getSpades();
        List<Card> hearts = getHearts();
        List<Card> clubs = getClubs();
        List<Card> diamonds = getDiamonds();

        declarations = new ArrayList<>();
        Combination comb;
        ArrayList<Combination> tierces;

        comb = Combination.checkForSuite(hearts);
        if (comb != null) {
            declarations.add(comb);
            updateHighest(comb);
        } else {
            comb = Combination.checkForAnnonce(hearts);
            if (comb != null) {
                declarations.add(comb);
                updateHighest(comb);
            } else {
                tierces = Combination.checkForTierce(hearts);
                for (Combination tierce : tierces) {
                    declarations.add(tierce);
                    updateHighest(tierce);
                }
            }
        }

        comb = Combination.checkForSuite(spades);
        if (comb != null) {
            declarations.add(comb);
            updateHighest(comb);
        } else {
            comb = Combination.checkForAnnonce(spades);
            if (comb != null) {
                declarations.add(comb);
                updateHighest(comb);
            } else {
                tierces = Combination.checkForTierce(spades);
                for (Combination tierce : tierces) {
                    declarations.add(tierce);
                    updateHighest(tierce);
                }
            }
        }

        comb = Combination.checkForSuite(diamonds);
        if (comb != null) {
            declarations.add(comb);
            updateHighest(comb);
        } else {
            comb = Combination.checkForAnnonce(diamonds);
            if (comb != null) {
                declarations.add(comb);
                updateHighest(comb);
            } else {
                tierces = Combination.checkForTierce(diamonds);
                for (Combination tierce : tierces) {
                    declarations.add(tierce);
                    updateHighest(tierce);
                }
            }
        }

        comb = Combination.checkForSuite(clubs);
        if (comb != null) {
            declarations.add(comb);
            updateHighest(comb);
        } else {
            comb = Combination.checkForAnnonce(clubs);
            if (comb != null) {
                declarations.add(comb);
                updateHighest(comb);
            } else {
                tierces = Combination.checkForTierce(clubs);
                for (Combination tierce : tierces) {
                    declarations.add(tierce);
                    updateHighest(tierce);
                }
            }
        }

        ArrayList<Combination> carres = Combination.checkForCarre(hearts, spades, diamonds, clubs);
        for (Combination carre : carres) {
            declarations.add(carre);
            updateHighest(carre);
        }
        Combination belote = Combination.checkForBelote(trump == Suit.Hearts ? hearts
                : trump == Suit.Spades ? spades : trump == Suit.Diamonds ? diamonds : clubs);
        if (belote != null) {
            declarations.add(belote);
            updateHighest(belote);
        }
        orderComb(trump);
        removeNullComb();
    }

    private void removeNullComb() {
        declarations.removeIf(Objects::isNull);
        if (declarations.isEmpty())
            declarations = null;
    }

    /**
     * order the combinations after checking for them
     */
    private void orderComb(Suit trump) {
        for (int i = 0; i < declarations.size(); i++) {
            int index = i;
            for (int j = i + 1; j < declarations.size(); j++) {
                Combination IComb = declarations.get(index);
                Combination JComb = declarations.get(j);
                boolean higher_type = IComb.getType().ordinal() > JComb.getType().ordinal();
                boolean same_type = IComb.getType().equals(JComb.getType());
                boolean higher_rank = IComb.getRank().ordinal() < JComb.getRank().ordinal();
                boolean same_rank = IComb.getRank().equals(JComb.getRank());
                boolean higher_value = IComb.getType().getValue() < JComb.getType().getValue();
                boolean isTrump = JComb.getSuit().equals(trump);
                if (higher_type || same_type && higher_rank && higher_value || same_type && same_rank && isTrump) {
                    index = j;
                }
            }
            if (index != i) {
                Combination other = declarations.set(i, declarations.get(index));
                declarations.set(index, other);
            }
        }
    }

    private void updateHighest(Combination comb) {
        if (highestComb == null) {
            highestComb = comb;
        } else {
            boolean higher_type = highestComb.getType().ordinal() > comb.getType().ordinal();
            boolean same_type = highestComb.getType().equals(comb.getType());
            boolean higher_rank = highestComb.getRank().ordinal() < comb.getRank().ordinal();
            boolean highest_value = highestComb.getType().getValue() < comb.getType().getValue();
            if (higher_type || same_type && higher_rank && highest_value) {
                highestComb = comb;
            }
        }
    }

    public List<Combination> getDeclarations() {
        return declarations;
    }

    /**
     * remove all declarations except belote
     */
    public void removeDeclarations() {
        if (declarations != null) declarations.removeIf(comb -> comb.getType() != ComboType.Belote);
    }

    public boolean have_declarations() {
        return declarations != null && !declarations.isEmpty();
    }
}
