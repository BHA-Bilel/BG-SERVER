package shared.domino;

import java.io.Serializable;

public class DominoMsg implements Serializable {

    public int comm;
    public Object[] adt_data;

    /** DRAW PASS BLOCKED */
    public DominoMsg(DominoComm comm) {
        this.comm = comm.ordinal();
    }


    public DominoMsg(DominoComm comm, Object[] adt_data) {
        this.comm = comm.ordinal();
        this.adt_data = adt_data;
    }

}
