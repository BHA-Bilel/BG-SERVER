package bg.server.dominos.model;

import java.util.List;

public class Domino {
    private final int leftValue;
    private final int rightValue;

    public Domino(int left, int right) {
        this.leftValue = left;
        this.rightValue = right;
    }

    public int getRightValue() {
        return rightValue;
    }

    public int getLeftValue() {
        return leftValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;
        Domino other = (Domino) obj;
        return (other.leftValue == leftValue && other.rightValue == rightValue)
                || (other.leftValue == rightValue && other.rightValue == leftValue);
    }

    /**
     * to send drawn domino from server
     */
    public Integer[] to_array() {
        return new Integer[]{leftValue, rightValue};
    }

    /**
     * for server to send new game dominoes in adt_data
     */
    public static Integer[] to_array(List<Domino> list) {
        Integer[] array = new Integer[list.size() * 2];
        int i = 0;
        for (Domino domino : list) {
            array[i++] = domino.getLeftValue();
            array[i++] = domino.getRightValue();
        }
        return array;
    }

}
