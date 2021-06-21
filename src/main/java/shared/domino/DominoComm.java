package shared.domino;

public enum DominoComm {
    // client sent msg
    /** player passed his turn */
    PASS,
    /** player of id=1 tells the server that the game is blocked */
    BLOCKED,
    /** player played a domino on the right side of the deck (left+right values in adt_data) */
    PLAYED_LEFT,
    /** player played a domino on the left side of the deck (left+right values in adt_data) */
    PLAYED_RIGHT,

    // server sent msg
    /** player at current position has drawn a domino
     * (server only send domino to the player who draws in adt_data) */
    DRAW,
    /** server send an int[14/28] containing player dominoes for the upcoming game */
    NEW_GAME,
    /** server sends won value in adt_data */
    GAME_END,
    /** server sends winner id + won value in adt_data */
    BLOCK_RESULT
}
