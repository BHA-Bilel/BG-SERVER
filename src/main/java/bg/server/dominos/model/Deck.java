package bg.server.dominos.model;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    private final ArrayList<Domino> dominos;
    private final ArrayList<Domino> populated;

    public Deck() {
        dominos = new ArrayList<>();
        populated = new ArrayList<>();
        for (int i = 0; i < 7; i++) for (int j = i; j < 7; j++) populated.add(new Domino(i, j));
    }

    public void prepare() {
        dominos.clear();
        dominos.addAll(populated);
        Collections.shuffle(dominos);
    }

    public ArrayList<Domino> getDominos() {
        return dominos;
    }

    public void remove(Domino selected) {
        dominos.remove(selected);
    }
}
