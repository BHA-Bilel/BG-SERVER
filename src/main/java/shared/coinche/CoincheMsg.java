package shared.coinche;

import java.io.Serializable;

public class CoincheMsg implements Serializable {

    public int comm;
    public final Object[] adt_data;

    public CoincheMsg(CoincheComm comm) {
        this.comm = comm.ordinal();
        adt_data = new Object[0];
    }

    public CoincheMsg(CoincheComm comm, Object[] adt_data) {
        this.comm = comm.ordinal();
        this.adt_data = adt_data;
    }

}
