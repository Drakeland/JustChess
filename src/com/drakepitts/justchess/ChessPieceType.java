package com.drakepitts.justchess;

/**
 * ChessPieceType.java
 */

/**
 * @author Drake Pitts
 */
public enum ChessPieceType {
    NONE, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public char toChar() {
        if (this == KNIGHT) {
            return 'n';
        }
        return toString().charAt(0);
    }

    public static ChessPieceType getType(char type) {
        type = Character.toLowerCase(type);
        switch (type) {
        case 'p':
            return PAWN;
        case 'n':
            return KNIGHT;
        case 'b':
            return BISHOP;
        case 'r':
            return ROOK;
        case 'q':
            return QUEEN;
        case 'k':
            return KING;
        default:
            return NONE;
        }
    }
}
