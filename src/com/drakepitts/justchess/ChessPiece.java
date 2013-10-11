package com.drakepitts.justchess;

import java.util.HashSet;

/**
 * ChessPiece.java
 */

/**
 * @author Drake Pitts
 */
public class ChessPiece {
    private char side;
    private ChessPieceType type;
    private HashSet<Integer> potentialMoves;
    private HashSet<Integer> attackArea;

    public static final char NO_SIDE = '-';

    public ChessPiece() {
        side = NO_SIDE;
        type = ChessPieceType.NONE;
        potentialMoves = new HashSet<Integer>(0);
        attackArea = new HashSet<Integer>(0);
    }

    public ChessPiece(ChessPiece source) {
        side = source.side;
        type = source.type;
        if (potentialMoves != null) {
            potentialMoves = source.potentialMoves;
        } else {
            potentialMoves = new HashSet<Integer>(0);
        }
        if (attackArea != null) {
            attackArea = source.attackArea;
        } else {
            attackArea = new HashSet<Integer>(0);
        }
    }

    public ChessPiece(Character rep) {
        if (Character.isUpperCase(rep)) {
            side = 'w';
        } else if (Character.isLowerCase(rep)) {
            side = 'b';
        } else {
            side = NO_SIDE;
        }
        type = ChessPieceType.getType(rep);
        potentialMoves = new HashSet<Integer>(0);
        attackArea = new HashSet<Integer>(0);
    }

    public ChessPiece(char side, ChessPieceType type) {
        this.side = side;
        this.type = type;
        potentialMoves = new HashSet<Integer>(0);
        attackArea = new HashSet<Integer>(0);
    }

    @Override
    public String toString() {
        return String.format("ChessPiece[\n\tside: '%s';\n\ttype: %s;\n]",
                side, type);
    }

    public String toString(int indent) {
        String prefix = "\n";
        for (int ii = 0; ii < indent; ii++) {
            prefix += "\t";
        }
        return toString().replace("\n", prefix);
    }

    /**
     * @return uppercase initial for white, lowercase initial for black, and 'n'
     *         otherwise
     */
    public char toChar() {
        if (side == 'b') {
            return type.toChar();
        } else if (side == 'w') {
            return Character.toUpperCase(type.toChar());
        } else {
            return NO_SIDE;
        }
    }

    /**
     * @return Unicode char for chess piece or ' '
     */
    public char toUnicodeChar() {
        switch (type) {
        case PAWN:
            return (side == 'b') ? '\u265F' : '\u2659';
        case KNIGHT:
            return (side == 'b') ? '\u265E' : '\u2658';
        case BISHOP:
            return (side == 'b') ? '\u265D' : '\u2657';
        case ROOK:
            return (side == 'b') ? '\u265C' : '\u2656';
        case QUEEN:
            return (side == 'b') ? '\u265B' : '\u2655';
        case KING:
            return (side == 'b') ? '\u265A' : '\u2654';
        case NONE:
        default:
            return ' ';
        }
    }

    /**
     * @return Unicode char for chess piece or 'n'
     */
    public String toUnicodeString() {
        switch (type) {
        case PAWN:
            return (side == 'b') ? "\\u265F" : "\\u2659";
        case KNIGHT:
            return (side == 'b') ? "\\u265E" : "\\u2658";
        case BISHOP:
            return (side == 'b') ? "\\u265D" : "\\u2657";
        case ROOK:
            return (side == 'b') ? "\\u265C" : "\\u2656";
        case QUEEN:
            return (side == 'b') ? "\\u265B" : "\\u2655";
        case KING:
            return (side == 'b') ? "\\u265A" : "\\u2654";
        case NONE:
        default:
            return "-";
        }
    }

    /**
     * @return the side
     */
    public char getSide() {
        return side;
    }

    /**
     * @param side the side to set
     */
    public void setSide(char side) {
        this.side = side;
    }

    /**
     * @return the type
     */
    public ChessPieceType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ChessPieceType type) {
        this.type = type;
    }

    /**
     * @return the potentialMoves
     */
    public HashSet<Integer> getPotentialMoves() {
        return potentialMoves;
    }

    /**
     * @param potentialMoves the potentialMoves to set
     */
    public void setPotentialMoves(HashSet<Integer> potentialMoves) {
        this.potentialMoves = potentialMoves;
    }

    /**
     * @return the attackArea
     */
    public HashSet<Integer> getAttackArea() {
        return attackArea;
    }

    /**
     * @param attackArea the attackArea to set
     */
    public void setAttackArea(HashSet<Integer> attackArea) {
        this.attackArea = attackArea;
    }

}
