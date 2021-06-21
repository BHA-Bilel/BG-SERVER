package shared.coinche;

public enum CoincheComm {
    // client sent msg
    /** player passed his turn */
    PASSED,
    /** player bought (suit+value+capot in adt_data) */
    BOUGHT,
    /** player coinched */
    COINCHED,
    /** player surcoinched */
    SURCOINCHED,
    /** player played a card (suit+rank values in adt_data) */
    PLAYED,
    /** player of id=1 informs the server that the game has ended */
    GAME_END,
    /** player of id=1 informs the server of the round trump (at the beginning of play phase) */
    TRUMP,

    // server sent msg
    /** server send an int[16] containing player cards for the upcoming game */
    NEW_GAME,
    /** server sends declarations of the current round (after trump msg)
     * if adt_data is null, that means end of declarations */
    DECLARATIONS,
}
