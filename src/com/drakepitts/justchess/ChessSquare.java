package com.drakepitts.justchess;

/**
 * ChessSquare.java
 */

/**
 * @author Drake Pitts
 */
public class ChessSquare implements Cloneable {
    private String name;
    private int index;
    private char color;
    private ChessPiece occupant;

    public static final String INVALID_SQUARE = "INVALID_SQUARE";

    public ChessSquare() {
        color = 'n';
        occupant = new ChessPiece();
        name = INVALID_SQUARE;
        index = -1;
    }

    public ChessSquare(char color, ChessPiece occupant) {
        if (occupant != null) {
            this.occupant = new ChessPiece(occupant);
        }
        this.color = color;
        name = INVALID_SQUARE;
        index = -1;
    }

    public ChessSquare(char color, ChessPiece occupant, String name, int index) {
        if (occupant != null) {
            this.occupant = new ChessPiece(occupant);
        }
        this.color = color;
        this.name = name;
        this.index = index;
    }

    public ChessSquare(ChessSquare source) {
        if (occupant != null) {
            occupant = new ChessPiece(source.occupant);
        } else {
            occupant = new ChessPiece();
        }
        color = source.color;
        index = source.index;
        name = source.name;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new ChessSquare(this);
    }

    @Override
    public String toString() {
        return String.format("ChessSquare[\n\tname: \"%s\";\n\tindex: %03o;"
                + "\n\tcolor: '%s';\n\toccupant: %s;\n]", name, index, color,
                occupant.toString(1));
    }

    public String toString(int indent) {
        String prefix = "\n";
        for (int ii = 0; ii < indent; ii++) {
            prefix += "\t";
        }
        return toString().replace("\n", prefix);
    }

    /**
     * @return true if square has a piece occupying it. false otherwise.
     */
    public boolean isOccupied() {
        return (occupant.getType() != ChessPieceType.NONE)
                && ((occupant.getSide() == 'w') || (occupant.getSide() == 'b'));
    }

    /**
     * @param type the piece type being checked for
     * @param side the side being checked for
     * @return true if square has a piece occupying matching the parameters.
     *         false otherwise.
     */
    public boolean isOccupiedBy(ChessPieceType type, char side) {
        return ((occupant.getType() == type) && (occupant.getSide() == side));
    }

    /**
     * @param side the side being checked for
     * @return true if square is occupied by a side piece. false otherwise.
     */
    public boolean isOccupiedBy(char side) {
        if (occupant == null) {
            return false;
        }
        return (occupant.getSide() == side);
    }

    public boolean isHostileTo(char toSide) {
        if (!isOccupied()) {
            return false;
        }
        switch (occupant.getSide()) {
        case 'w':
            if (toSide == 'b') {
                return true;
            }
            return false;
        case 'b':
            if (toSide == 'w') {
                return true;
            }
            return false;
        default:
            return false;
        }
    }

    /**
     * @return the color
     */
    public char getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(char color) {
        this.color = color;
    }

    /**
     * @return the occupant
     */
    public ChessPiece getOccupant() {
        return occupant;
    }

    /**
     * @param occupant the occupant to set
     */
    public void setOccupant(ChessPiece occupant) {
        if (occupant != null) {
            this.occupant = new ChessPiece(occupant);
        } else {
            occupant = new ChessPiece();
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
