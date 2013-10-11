package com.drakepitts.justchess;

/**
 * ChessMove.java
 */

/**
 * @author Drake Pitts
 */
public class ChessMove {
    ChessSquare fromSquare;
    ChessSquare toSquare;

    public ChessMove() {
        fromSquare = new ChessSquare();
        toSquare = new ChessSquare();
    }

    public ChessMove(ChessSquare fromSquare, ChessSquare toSquare) {
        if (fromSquare != null) {
            this.fromSquare = new ChessSquare(fromSquare);
        } else {
            this.fromSquare = new ChessSquare();
        }
        if (toSquare != null) {
            this.toSquare = new ChessSquare(toSquare);
        } else {
            this.fromSquare = new ChessSquare();
        }
    }

    @Override
    public String toString() {
        return "ChessMove [\n\tfromSquare: " + fromSquare.toString(1)
                + ";\n\ttoSquare: " + toSquare.toString(1) + ";\n]";
    }

    /**
     * @return the fromSquare
     */
    public ChessSquare getFromSquare() {
        return fromSquare;
    }

    /**
     * @param fromSquare the fromSquare to set
     */
    public void setFromSquare(ChessSquare fromSquare) {
        this.fromSquare = fromSquare;
    }

    /**
     * @return the toSquare
     */
    public ChessSquare getToSquare() {
        return toSquare;
    }

    /**
     * @param toSquare the toSquare to set
     */
    public void setToSquare(ChessSquare toSquare) {
        this.toSquare = toSquare;
    }

    /**
     * @return the ChessPieceType of the moving piece
     */
    public ChessPieceType getMoverType() {
        return toSquare.getOccupant().getType();
    }

    /**
     * @return the side of the moving piece as a char
     */
    public char getMoverSide() {
        return toSquare.getOccupant().getSide();
    }
}
