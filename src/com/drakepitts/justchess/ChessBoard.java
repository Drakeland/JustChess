package com.drakepitts.justchess;

/**
 * ChessBoard.java
 */
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Drake Pitts
 */
public class ChessBoard {
    private ChessSquare[] grid;
    private ChessMove prevMove;
    private boolean whiteKingsideCastling;
    private boolean whiteQueensideCastling;
    private boolean blackKingsideCastling;
    private boolean blackQueensideCastling;
    private String enPassantTargetSquare;
    private ChessPieceType promotionType;
    private HashSet<ChessMove> blackMoves;
    private HashSet<ChessMove> whiteMoves;
    private int blackKingIndex;
    private int whiteKingIndex;
    private GameState state;

    public enum GameState {
        IN_PROGRESS, ONE_ZERO, ZERO_ONE, HALF_HALF
    };

    /**
     * Initializes the board with a checkered layout (dark a1 square)
     */
    public ChessBoard() {
        grid = new ChessSquare[0100];
        for (int ii = 0; ii < 0100; ii++) {
            if ((((ii % 010) + (ii / 010)) % 2) == 0) {
                grid[ii] = new ChessSquare('b', null, indexToSquare(ii), ii);
            } else {
                grid[ii] = new ChessSquare('w', null, indexToSquare(ii), ii);
            }
        }
        prevMove = new ChessMove();
    }

    public ChessBoard(ChessBoard source) {
        grid = new ChessSquare[0100];
        prevMove = new ChessMove();
        if (source.grid != null) {
            for (int ii = 0; ii < 0100; ii++) {
                try {
                    grid[ii] = (ChessSquare) source.grid[ii].clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }
        if ((source.prevMove != null) && (source.prevMove.fromSquare != null)
                && (source.prevMove.toSquare != null)) {
            prevMove = new ChessMove(source.prevMove.fromSquare,
                    source.prevMove.toSquare);
        }
        whiteKingsideCastling = source.whiteKingsideCastling;
        whiteQueensideCastling = source.whiteQueensideCastling;
        blackKingsideCastling = source.blackKingsideCastling;
        blackQueensideCastling = source.blackQueensideCastling;
        enPassantTargetSquare = source.enPassantTargetSquare;
        promotionType = source.promotionType;
        state = source.state;
        whiteKingIndex = source.whiteKingIndex;
        blackKingIndex = source.blackKingIndex;
    }

    /**
     * Initializes the board with a standard chess setup.
     */
    public void setPieces() {
        ChessPieceType[] pieces = new ChessPieceType[] { ChessPieceType.ROOK,
                ChessPieceType.KNIGHT, ChessPieceType.BISHOP,
                ChessPieceType.QUEEN, ChessPieceType.KING,
                ChessPieceType.BISHOP, ChessPieceType.KNIGHT,
                ChessPieceType.ROOK };
        ;
        char[] sides = { 'w', 'w', 'n', 'n', 'n', 'n', 'b', 'b' };

        for (int ii = 0; ii < 010; ii++) {
            for (int jj = 0; jj < 010; jj++) {
                if ((jj == 0) || (jj == 7)) {
                    grid[(010 * ii) + jj].setOccupant(new ChessPiece(sides[jj],
                            pieces[ii]));
                } else if ((jj == 1) || (jj == 6)) {
                    grid[(010 * ii) + jj].setOccupant(new ChessPiece(sides[jj],
                            ChessPieceType.PAWN));
                } else {
                    grid[(010 * ii) + jj].setOccupant(new ChessPiece(sides[jj],
                            ChessPieceType.NONE));
                }
            }
        }
        prevMove = new ChessMove();
        whiteKingsideCastling = true;
        whiteQueensideCastling = true;
        blackKingsideCastling = true;
        blackQueensideCastling = true;
        state = GameState.IN_PROGRESS;
        whiteKingIndex = 040;
        blackKingIndex = 047;
    }

    /**
     * Constructs the grid from a FEN representation
     * @param fen a String containing a FEN representation of a chess state
     */
    public void setPieces(String fen) {
        String[] ranks = fen.split("/");
        for (int ii = 0; ii < ranks.length; ii++) {
            System.out.printf("ranks[%d]: \"%s\"\n", ii, ranks[ii]);
        }

        // Read through ranks and fill the grid one by one
        int currIndex;
        char currChar;
        int rr;
        int countDown = 0;
        for (int ii = 0; ii < 010; ii++) { // Cycle through ranks
            rr = 0;
            for (int jj = 0; jj < 010; jj++) { // Cycle through files
                currIndex = (010 * jj) + (7 - ii);
                currChar = ranks[ii].charAt(rr);
                if (Character.isDigit(currChar)) {
                    if (countDown == 0) {
                        countDown = Integer.parseInt("" + currChar);
                    }
                    grid[currIndex].setOccupant(new ChessPiece());
                    countDown--;
                    if (countDown == 0) {
                        rr++;
                    }
                } else {
                    grid[currIndex].setOccupant(new ChessPiece(currChar));
                    rr++;
                }
            }
        }
    }

    /**
     * Sets possibleMoves, attackArea arrays of each occupant of a grid square.
     */
    public void setMoves() {
        for (ChessSquare square : grid) {
            square.getOccupant().setPotentialMoves(movesFrom(square));
        }
    }

    public boolean isGameOver() {
        if (whiteMoves.size() == 0) {
            if (isInCheck('w')) {
                state = GameState.ZERO_ONE;
            } else {
                state = GameState.HALF_HALF;
            }
            return true;
        } else if (blackMoves.size() == 0) {
            if (isInCheck('b')) {
                state = GameState.ONE_ZERO;
            } else {
                state = GameState.HALF_HALF;
            }
            return true;
        }
        return false;
    }

    public void setAttacks(char side) {
        for (ChessSquare square : grid) {
            if ((square == null) || (square.getOccupant() == null)) {
                continue;
            }
            square.getOccupant().setAttackArea(attacksFrom(square));
        }
    }

    @Override
    public String toString() {
        return "ChessBoard [\n\tgrid: " + Arrays.toString(grid)
                + ";\n\tprevMove: " + prevMove + ";\n]";
    }

    /**
     * @return a String containing a representation of the chess board from the
     *         view of the black side
     */
    public String whiteView() {
        String s = "";
        for (int ii = 0; ii < 010; ii++) {
            for (int jj = 0; jj < 010; jj++) {
                ChessSquare currSquare = grid[(010 * jj) + (7 - ii)];
                if (currSquare.isOccupied()) {
                    s += currSquare.getOccupant().toChar();
                } else if (currSquare.getColor() == 'w') {
                    s += " ";
                } else if (currSquare.getColor() == 'b') {
                    s += ".";
                }
                s += " ";
            }
            s += "\n";
        }
        return s;
    }

    public String whiteViewArray() {
        String s = "{\n";
        for (int ii = 0; ii < 010; ii++) {
            for (int jj = 0; jj < 010; jj++) {
                ChessSquare currSquare = grid[(010 * jj) + (7 - ii)];
                s += String.format("\"%s\", ", currSquare.getOccupant()
                        .toUnicodeChar());
            }
            s += "\n";
        }
        s += "}";
        return s;
    }

    public void printWhiteView() {
        System.out.print("\n" + whiteView());
    }

    public void printWhiteViewArray() {
        System.out.print("\n" + whiteViewArray());
    }

    /**
     * @return a String containing a representation of the chess board from the
     *         view of the black side
     */
    public String blackView() {
        String s = "";
        for (int ii = 0; ii < 010; ii++) {
            for (int jj = 0; jj < 010; jj++) {
                ChessSquare currSquare = grid[(010 * (7 - jj)) + (001 * ii)];
                if (currSquare.isOccupied()) {
                    s += currSquare.getOccupant().toChar();
                } else if (currSquare.getColor() == 'w') {
                    s += " ";
                } else if (currSquare.getColor() == 'b') {
                    s += ".";
                }
                s += " ";
            }
            s += "\n";
        }
        return s;
    }

    public void printBlackView() {
        System.out.print("\n" + blackView());
    }

    /**
     * @return a String containing the FEN representation of the chess board
     */
    public String toFEN() {
        int counter = 0;
        String s = "";
        for (int ii = 0; ii < 010; ii++) {
            for (int jj = 0; jj < 010; jj++) {
                ChessSquare currSquare = grid[(010 * jj) + (7 - ii)];
                if (currSquare.isOccupied()) {
                    if (counter != 0) {
                        s += counter;
                        counter = 0;
                    }
                    s += currSquare.getOccupant().toChar();
                } else {
                    counter++;
                }
            }
            if (counter != 0) {
                s += counter;
                counter = 0;
            }
            s += (ii != 7) ? "/" : "";
        }
        return s;
    }

    /**
     * Prints the FEN representation of the chess board
     */
    public void printFEN() {
        System.out.println("\n" + toFEN());
    }

    public HashSet<Integer> attacksFrom(ChessSquare square) {
        if ((square == null) || (square.getOccupant() == null)) {
            return new HashSet<Integer>(0);
        }
        return attacksFrom(square.getIndex(), square.getOccupant().getType(),
                square.getOccupant().getSide());
    }

    public HashSet<Integer> attacksFrom(int fromIndex, ChessPieceType type,
            char side) {
        if ((side != 'w') && (side != 'b')) {
            return new HashSet<Integer>(0);
        }
        switch (type) {
        case PAWN:
            return pawnAttacksFrom(fromIndex, side);
        case KNIGHT:
            return knightMovesFrom(fromIndex, side);
        case BISHOP:
            return bishopMovesFrom(fromIndex, side);
        case ROOK:
            return rookMovesFrom(fromIndex, side);
        case QUEEN:
            return queenMovesFrom(fromIndex, side);
        case KING:
            return kingMovesFrom(fromIndex, side);
        default:
            return new HashSet<Integer>(0);
        }
    }

    public HashSet<Integer> movesFrom(ChessSquare square) {
        if ((square == null) || (square.getOccupant() == null)) {
            return new HashSet<Integer>(0);
        }
        return movesFrom(square.getIndex(), square.getOccupant().getType(),
                square.getOccupant().getSide());
    }

    public HashSet<Integer> movesFrom(int fromIndex, ChessPieceType type,
            char side) {
        if ((side != 'w') && (side != 'b')) {
            return new HashSet<Integer>(0);
        }
        switch (type) {
        case PAWN:
            return pawnMovesFrom(fromIndex, side);
        case KNIGHT:
            return knightMovesFrom(fromIndex, side);
        case BISHOP:
            return bishopMovesFrom(fromIndex, side);
        case ROOK:
            return rookMovesFrom(fromIndex, side);
        case QUEEN:
            return queenMovesFrom(fromIndex, side);
        case KING:
            return kingMovesFrom(fromIndex, side);
        default:
            return new HashSet<Integer>(0);
        }
    }

    /**
     * @param fromIndex the square initially occupied by a moving pawn in AlgNot
     * @param side the side the pawn moving is on
     * @return an array of indices of potential moves of a pawn
     */
    public HashSet<Integer> pawnAttacksFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingPawn = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(5, 1.f);
        int[] xDirs = new int[] { -1, 1 };
        int yDir = (movingPawn.getSide() == 'w') ? 1
                : ((movingPawn.getSide() == 'b') ? -1 : 0);
        int checkIndex = 0;

        for (int xDir : xDirs) {
            checkIndex = fromIndex + ((010 * xDir) + yDir);
            if (isInRange(checkIndex)) {
                indices.add(checkIndex);
            }
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving pawn in AlgNot
     * @param side the side the pawn moving is on
     * @return an array of indices of potential moves of a pawn
     */
    public HashSet<Integer> pawnMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingPawn = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(5, 1.f);
        int[] xDirs = new int[] { -1, 1 };
        int yDir = (movingPawn.getSide() == 'w') ? 1
                : ((movingPawn.getSide() == 'b') ? -1 : 0);
        int checkIndex = 0;

        checkIndex = fromIndex + yDir;
        if (!grid[checkIndex].isOccupied()) {
            indices.add(checkIndex);

            checkIndex = fromIndex + (2 * yDir);
            if (!grid[checkIndex].isOccupied() && pawnUnmoved(fromIndex)) {
                indices.add(checkIndex);
            }
        }

        for (int xDir : xDirs) {
            checkIndex = fromIndex + ((010 * xDir) + yDir);
            if (isInRange(checkIndex)) {
                if (grid[checkIndex].isHostileTo(movingPawn.getSide())) {
                    indices.add(checkIndex);
                }
                if (isValidEnPassant(fromIndex, checkIndex)) {
                    indices.add(checkIndex);
                }
            }
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving knight in
     *        AlgNot
     * @param side the side the knight moving is on
     * @return an array of indices of potential moves of a knight
     */
    public HashSet<Integer> knightMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingKnight = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(9, 1.f);
        int checkIndex = 0;
        int[] dirs = new int[] { -1, 1 };
        int[] mods = new int[] { 1, 2 };

        for (int xMod : mods) {
            for (int yMod : mods) {
                if (xMod == yMod) {
                    continue;
                }
                for (int xDir : dirs) {
                    for (int yDir : dirs) {
                        checkIndex = fromIndex + (010 * xMod * xDir)
                                + (yMod * yDir);
                        if (isInRange(checkIndex)
                                && !isOverflown(fromIndex, checkIndex, xMod,
                                        yMod)) {
                            if (grid[checkIndex].isOccupied()) {
                                if (grid[checkIndex].isHostileTo(movingKnight
                                        .getSide())) {
                                    indices.add(checkIndex);
                                }
                            } else {
                                indices.add(checkIndex);
                            }
                        }
                    }
                }
            }
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving bishop in
     *        AlgNot
     * @param side the side the bishop moving is on
     * @return an array of indices of potential moves of a bishop
     */
    public HashSet<Integer> bishopMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingBishop = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(14, 1.f);
        int checkIndex = 0;
        int[] dirs = new int[] { -1, 1 };

        for (int xDir : dirs) {
            for (int yDir : dirs) {
                for (int ii = 1; ii <= 7; ii++) {
                    checkIndex = fromIndex + (ii * ((010 * xDir) + yDir));
                    if (isInRange(checkIndex)) {
                        if (!isOverflown(fromIndex, checkIndex, ii, ii)) {
                            if (!grid[checkIndex].isOccupied()) {
                                indices.add(checkIndex);
                            } else if (grid[checkIndex]
                                    .isHostileTo(movingBishop.getSide())) {
                                indices.add(checkIndex);
                                break;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving rook in AlgNot
     * @param side the side the rook moving is on
     * @return an array of indices of potential moves of a rook
     */
    public HashSet<Integer> rookMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingRook = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(15, 1.f);
        int checkIndex = 0;
        int[] dirs = new int[] { -1, 1 };
        int[] mods = new int[] { 0, 1 };

        for (int xMod : mods) {
            for (int yMod : mods) {
                if (xMod == yMod) {
                    continue;
                }
                for (int dir : dirs) {
                    for (int ii = 1; ii <= 7; ii++) {
                        checkIndex = fromIndex
                                + (ii * dir * ((010 * xMod) + yMod));
                        if (isInRange(checkIndex)) {
                            if (!isOverflown(fromIndex, checkIndex, ii * xMod,
                                    ii * yMod)) {
                                if (!grid[checkIndex].isOccupied()) {
                                    indices.add(checkIndex);
                                } else if (grid[checkIndex]
                                        .isHostileTo(movingRook.getSide())) {
                                    indices.add(checkIndex);
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving queen in
     *        AlgNot
     * @param side the side the queen moving is on
     * @return an array of indices of potential moves of a queen
     */
    public HashSet<Integer> queenMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        HashSet<Integer> indices = new HashSet<Integer>(28, 1.f);

        for (int index : bishopMovesFrom(fromIndex, side)) {
            indices.add(index);
        }
        for (int index : rookMovesFrom(fromIndex, side)) {
            indices.add(index);
        }

        return indices;
    }

    /**
     * @param fromIndex the square initially occupied by a moving king in AlgNot
     * @param side the side the king moving is on
     * @return an array of indices of potential moves of a king
     */
    public HashSet<Integer> kingMovesFrom(int fromIndex, char side) {
        if (!isInRange(fromIndex)) {
            return new HashSet<Integer>(0);
        }
        ChessPiece movingKing = grid[fromIndex].getOccupant();
        HashSet<Integer> indices = new HashSet<Integer>(9, 1.f);
        int checkIndex = 0;
        int[] mods = new int[] { -1, 0, 1 };

        for (int xMod : mods) {
            for (int yMod : mods) {
                if ((xMod == 0) && (yMod == 0)) {
                    continue;
                }
                checkIndex = fromIndex + (010 * xMod) + yMod;
                if (isInRange(checkIndex)
                        && !isOverflown(fromIndex, checkIndex, xMod, yMod)) {
                    if (!grid[checkIndex].isOccupied()) {
                        indices.add(checkIndex);
                    } else if (grid[checkIndex].isHostileTo(movingKing
                            .getSide())) {
                        indices.add(checkIndex);
                    }
                }
            }
        }

        // Check for castling moves
        switch (fromIndex) {
        case 040: // e1: white king's starting position
            checkIndex = squareToIndex("c1");
            if (whiteQueensideCastling
                    && isUnblockedCastling(fromIndex, checkIndex)) { // queen-side
                indices.add(checkIndex);
            }
            checkIndex = squareToIndex("g1");
            if (whiteKingsideCastling
                    && isUnblockedCastling(fromIndex, checkIndex)) { // king-side
                indices.add(checkIndex);
            }
            break;
        case 047: // e8: black king's starting position
            checkIndex = squareToIndex("c8");
            if (blackQueensideCastling
                    && isUnblockedCastling(fromIndex, checkIndex)) { // queen-side
                indices.add(checkIndex);
            }
            checkIndex = squareToIndex("g8");
            if (blackKingsideCastling
                    && isUnblockedCastling(fromIndex, checkIndex)) { // king-side
                indices.add(checkIndex);
            }
            break;
        }

        return indices;
    }

    public ChessPiece moveFromTo(ChessSquare fromSquare, ChessSquare toSquare) {
        return moveFromTo(fromSquare.getIndex(), toSquare.getIndex());
    }

    /**
     * @param from the square to move a piece from in AlgNot
     * @param to the square to move a piece to in AlgNot
     * @return the taken piece. May be (read: usually is) the trivial piece.
     */
    public ChessPiece moveFromTo(String from, String to) {
        return moveFromTo(squareToIndex(from), squareToIndex(to));

    }

    /**
     * @param fromIndex the index of square to move a piece from
     * @param toIndex the index square to move a piece to
     * @return the taken piece. May be (read: usually is) the trivial piece.
     */
    public ChessPiece moveFromTo(int fromIndex, int toIndex) {
        ChessPiece takenPiece = grid[toIndex].getOccupant();
        ChessPiece movingPiece = grid[fromIndex].getOccupant();
        boolean moveCastlingRook = isKingCastling(fromIndex, toIndex);

        if (movingPiece == null) {
            return new ChessPiece();
        }

        if (isValidEnPassant(fromIndex, toIndex)) {
            int yDir = (prevMove.getMoverSide() == 'w') ? -1 : ((prevMove
                    .getMoverSide() == 'b') ? 1 : 0);
            moveFromTo(prevMove.getToSquare().getName(), indexToSquare(prevMove
                    .getToSquare().getIndex() + yDir));
        }

        grid[toIndex].setOccupant(movingPiece);
        grid[fromIndex].setOccupant(new ChessPiece());
        prevMove = new ChessMove(grid[fromIndex], grid[toIndex]);

        if (moveCastlingRook) {
            if (movingPiece.getSide() == 'w') {
                if (toIndex == 020) {
                    grid[030].setOccupant(grid[000].getOccupant());
                    grid[000].setOccupant(new ChessPiece());
                } else if (toIndex == 060) {
                    grid[050].setOccupant(grid[070].getOccupant());
                    grid[070].setOccupant(new ChessPiece());
                }
            } else if (movingPiece.getSide() == 'b') {
                if (toIndex == 027) {
                    grid[037].setOccupant(grid[007].getOccupant());
                    grid[007].setOccupant(new ChessPiece());
                } else if (toIndex == 067) {
                    grid[057].setOccupant(grid[077].getOccupant());
                    grid[077].setOccupant(new ChessPiece());
                }
            }
        }

        boolean noPawnMove = true;
        switch (movingPiece.getType()) {
        case PAWN:
            if (Math.abs((toIndex / 010) - (fromIndex / 010)) == 2) {
                enPassantTargetSquare = indexToSquare((toIndex + fromIndex) / 2);
                noPawnMove = false;
            }
            break;
        case KING:
            if (movingPiece.getSide() == 'w') {
                whiteKingsideCastling = false;
                whiteQueensideCastling = false;
                whiteKingIndex = toIndex;
            } else if (movingPiece.getSide() == 'b') {
                blackKingsideCastling = false;
                blackQueensideCastling = false;
                blackKingIndex = toIndex;
            }
            break;
        case ROOK:
            if (movingPiece.getSide() == 'w') {
                if (fromIndex == 000) {
                    whiteQueensideCastling = false;
                } else if (fromIndex == 070) {
                    whiteKingsideCastling = false;
                }
            } else if (movingPiece.getSide() == 'b') {
                if (fromIndex == 007) {
                    blackQueensideCastling = false;
                } else if (fromIndex == 077) {
                    blackKingsideCastling = false;
                }
            }
            break;
        default:
            break;
        }
        if (noPawnMove) {
            enPassantTargetSquare = "-";
        }

        return takenPiece;
    }

    /**
     * @param index the index to check
     * @return true if the index is in range of the grid
     */
    public static boolean isInRange(int index) {
        return ((index >= 0) && (index < 0100));
    }

    /**
     * @param i1 the index the piece is at
     * @param i2 the index checking to moves to
     * @param xDist the x-distance the indices should be apart
     * @param yDist the y-distance the indices should be apart
     * @return true only if the indices aren't dist apart in either direction.
     */
    public static boolean isOverflown(int i1, int i2, int xDist, int yDist) {
        return ((Math.abs(xDist) != Math.abs((i1 / 010) - (i2 / 010))) || (Math
                .abs(yDist) != Math.abs((i1 % 010) - (i2 % 010))));
    }

    /**
     * @param square a String representing the square in AlgNot
     * @return index of grid[] that represents square on the chess board
     */
    public static int squareToIndex(String square) {
        if (square.length() != 2) {
            return -1;
        }
        return (010 * (square.charAt(0) - 97))
                + (001 * (square.charAt(1) - 49));
    }

    /**
     * @param index index of grid[] that represents the square on the board
     * @return the square's representation in AlgNot
     */
    public static String indexToSquare(int index) {
        if ((index > 0100) || (index < 0)) {
            return ChessSquare.INVALID_SQUARE;
        }
        return "" + ((char) (97 + (index / 010)))
                + ((char) (49 + (index % 010)));
    }

    /**
     * @param side the side
     * @return the enemy of the side if 'w' or 'b'. 'n' otherwise.
     */
    static public char enemyOf(char side) {
        return ((side == 'w') ? 'b' : ((side == 'b') ? 'w' : 'n'));
    }

    /**
     * Tests the move function by outputting the valid moves array generated at
     * each square and printing to the console
     * @param type the piece type we want to check moves for
     */
    public void testMoves(ChessPieceType type) {
        HashSet<Integer> moves;
        String[] moveSquares;
        int moveSquaresIndex;
        for (int ii = 0; ii < 0100; ii++) {
            if (grid[ii].getOccupant().getType() != type) {
                continue;
            }
            System.out.printf("calling: movesFrom(\"%s\", %s, '%s')\n",
                    indexToSquare(ii), type, grid[ii].getOccupant().getSide());
            moves = movesFrom(ii, type, grid[ii].getOccupant().getSide());
            System.out.printf("moves: %s\n", Arrays.toString(moves.toArray()));
            moveSquares = new String[moves.size()];
            moveSquaresIndex = 0;
            for (int move : moves) {
                moveSquares[moveSquaresIndex++] = indexToSquare(move);
            }
            System.out.println(indexToSquare(ii) + ": "
                    + Arrays.toString(moveSquares));
            System.out.printf("type: %s, side:%s\n", type, grid[ii]
                    .getOccupant().getSide());
        }
    }

    /**
     * @return the grid
     */
    public ChessSquare[] getGrid() {
        return grid;
    }

    /**
     * @param grid the grid to set
     */
    public void setGrid(ChessSquare[] grid) {
        this.grid = grid;
    }

    /**
     * @return the prevMove
     */
    public ChessMove getPrevMove() {
        return prevMove;
    }

    /**
     * @param prevMove the prevMove to set
     */
    public void setPrevMove(ChessMove prevMove) {
        this.prevMove = prevMove;
    }

    /**
     * @return the enPassantTargetSquare
     */
    public String getEnPassantTargetSquare() {
        return enPassantTargetSquare;
    }

    /**
     * @param enPassantTargetSquare the enPassantTargetSquare to set
     */
    public void setEnPassantTargetSquare(String enPassantTargetSquare) {
        this.enPassantTargetSquare = enPassantTargetSquare;
        int enPassantTargetIndex = squareToIndex(enPassantTargetSquare);
        if ((enPassantTargetIndex / 010) == 2) {
            prevMove.setFromSquare(grid[enPassantTargetIndex - 1]);
            prevMove.setToSquare(grid[enPassantTargetIndex + 1]);
        } else if ((enPassantTargetIndex / 010) == 5) {
            prevMove.setFromSquare(grid[enPassantTargetIndex + 1]);
            prevMove.setToSquare(grid[enPassantTargetIndex - 1]);
        }
    }

    /**
     * @return the state
     */
    public GameState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(GameState state) {
        this.state = state;
    }

    /**
     * @param castlings an array of K/Q/k/q or -
     */
    public void setAvailableCastlings(char[] castlings) {
        for (char c : castlings) {
            switch (c) {
            case '-':
                whiteKingsideCastling = false;
                whiteQueensideCastling = false;
                blackKingsideCastling = false;
                blackQueensideCastling = false;
                return;
            case 'K':
                whiteKingsideCastling = true;
                break;
            case 'Q':
                whiteQueensideCastling = true;
                break;
            case 'k':
                blackKingsideCastling = true;
                break;
            case 'q':
                blackQueensideCastling = true;
                break;
            }
        }
    }

    /**
     * @return the blackMoves
     */
    public HashSet<ChessMove> getBlackMoves() {
        return blackMoves;
    }

    /**
     * @param blackMoves the blackMoves to set
     */
    public void setBlackMoves(HashSet<ChessMove> blackMoves) {
        this.blackMoves = blackMoves;
    }

    /**
     * @return the whiteMoves
     */
    public HashSet<ChessMove> getWhiteMoves() {
        return whiteMoves;
    }

    /**
     * @param whiteMoves the whiteMoves to set
     */
    public void setWhiteMoves(HashSet<ChessMove> whiteMoves) {
        this.whiteMoves = whiteMoves;
    }

    /**
     * @param addend the set of moves to add to whiteMoves
     */
    public void whiteMovesAddAll(HashSet<ChessMove> addend) {
        whiteMoves.addAll(addend);
    }

    /**
     * @param addend the set of moves to add to blackMoves
     */
    public void blackMovesAddAll(HashSet<ChessMove> addend) {
        blackMoves.addAll(addend);
    }

    /**
     * @return the promotionType
     */
    public ChessPieceType getPromotionType() {
        return promotionType;
    }

    /**
     * @param promotionType the promotionType to set
     */
    public void setPromotionType(ChessPieceType promotionType) {
        this.promotionType = promotionType;
    }

    public void setPotentialMovesAt(int atIndex, HashSet<Integer> moves) {
        grid[atIndex].getOccupant().setPotentialMoves(moves);
    }

    public ChessSquare squareAt(int index) {
        return grid[index];
    }

    public void promote(int atIndex) {
        if (atIndex != -1) {
            grid[atIndex].setOccupant(new ChessPiece(grid[atIndex]
                    .getOccupant().getSide(), promotionType));
        }
    }

    public boolean isValidPromotion(int fromIndex, int toIndex) {
        ChessPiece movingPiece = grid[toIndex].getOccupant();
        if (movingPiece.getType() != ChessPieceType.PAWN) {
            return false;
        }
        if (movingPiece.getSide() == 'w') {
            if (((fromIndex % 010) == 06) && ((toIndex % 010) == 07)) {
                return true;
            }
        } else if (movingPiece.getSide() == 'b') {
            if (((fromIndex % 010) == 01) && ((toIndex % 010) == 00)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param fromIndex the index of the moving pawn
     * @param toIndex the index of the pawn after potential en passant
     * @return true if pawn at fromIndex (and) can en passant
     */
    private boolean isValidEnPassant(int fromIndex, int toIndex) {
        int prevToIndex = prevMove.getToSquare().getIndex();
        int prevFromIndex = prevMove.getFromSquare().getIndex();
        if ((prevMove.getFromSquare().getOccupant() == null)
                || (prevMove.getToSquare().getOccupant() == null)) {
            return false;
        }
        return (prevMove.getMoverType() == ChessPieceType.PAWN)
                && (Math.abs(prevToIndex - prevFromIndex) == 2)
                && (indexToSquare((prevToIndex + prevFromIndex) / 2) == enPassantTargetSquare)
                && prevMove.getToSquare().isHostileTo(
                        grid[fromIndex].getOccupant().getSide());
    }

    private boolean isUnblockedCastling(int fromIndex, int toIndex) {
        if (!isInRange(fromIndex) || !isInRange(toIndex)) {
            return false;
        }
        int midIndex = -1;
        int otherMidIndex = -1;
        switch (fromIndex) {
        case 040: // e1: white king's starting position
            if (toIndex == squareToIndex("c1")) { // queen-side castling
                midIndex = squareToIndex("d1");
                otherMidIndex = squareToIndex("b1");
            } else if (toIndex == squareToIndex("g1")) { // king-side castling
                midIndex = squareToIndex("f1");
            } else {
                return false;
            }
            break;
        case 047: // e8: black king's starting position
            if (toIndex == squareToIndex("c8")) { // queen-side castling
                midIndex = squareToIndex("d8");
                otherMidIndex = squareToIndex("b8");
            } else if (toIndex == squareToIndex("g8")) { // king-side castling
                midIndex = squareToIndex("f8");
            } else {
                return false;
            }
            break;
        default:
            return false;
        }

        return !grid[midIndex].isOccupied()
                && !grid[toIndex].isOccupied()
                && ((otherMidIndex == -1) ? true : !grid[otherMidIndex]
                        .isOccupied());
    }

    private boolean isKingCastling(int fromIndex, int toIndex) {
        ChessPiece movingPiece = grid[fromIndex].getOccupant();
        if ((movingPiece != null)
                && (movingPiece.getType() == ChessPieceType.KING)) {

            if (movingPiece.getSide() == 'w') {
                if (fromIndex != squareToIndex("e1")) {
                    return false;
                }
            } else if (movingPiece.getSide() == 'b') {
                if (fromIndex != squareToIndex("e8")) {
                    return false;
                }
            } else {
                return false;
            }
            if (Math.abs((fromIndex / 010) - (toIndex / 010)) == 2) {
                return true;
            }
        }
        return false;
    }

    private boolean pawnUnmoved(int fromIndex) {
        return (grid[fromIndex].isOccupiedBy(ChessPieceType.PAWN, 'w')) ? ((fromIndex % 010) == 01)
                : ((grid[fromIndex].isOccupiedBy(ChessPieceType.PAWN, 'b')) ? ((fromIndex % 010) == 006)
                        : false);
    }

    /**
     * @param side the side in question
     * @param kind the kind of castling (KING or QUEEN)
     * @return whether side is able to castle, given there are no checks or
     *         pieces in the way
     */
    public boolean canCastle(char side, ChessPieceType kind) {
        if (side == 'w') {
            if (kind == ChessPieceType.QUEEN) {
                return whiteQueensideCastling;
            } else if (kind == ChessPieceType.KING) {
                return whiteKingsideCastling;
            }
        } else if (side == 'b') {
            if (kind == ChessPieceType.QUEEN) {
                return blackQueensideCastling;
            } else if (kind == ChessPieceType.KING) {
                return whiteKingsideCastling;
            }
        }
        return false;
    }

    /**
     * @param side the side in question
     * @return whether side king is checked
     */
    public boolean isInCheck(char side) {
        if (side == 'w') {
            System.out.printf("isInCheck(w): whiteKingIndex=%03o:%s\n",
                    whiteKingIndex, indexToSquare(whiteKingIndex));
            return isAttackedBy(whiteKingIndex, 'b');
        } else if (side == 'b') {
            System.out.printf("isInCheck(b): blackKingIndex=%03o:%s\n",
                    blackKingIndex, indexToSquare(blackKingIndex));
            return isAttackedBy(blackKingIndex, 'w');
        }
        return false;
    }

    /**
     * @param atIndex the square in question
     * @param side the side attacking
     * @return whether side attacks square of atIndex
     */
    public boolean isAttackedBy(int atIndex, char side) {
        if (!isInRange(atIndex)) {
            return false;
        }
        HashSet<Integer> area = attackArea(side);
        Iterator<Integer> areaIt = area.iterator();
        while (areaIt.hasNext()) {
            if (areaIt.next() == atIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param side the side attacking
     * @return an array of indices of squares attacked by side
     */
    public HashSet<Integer> attackArea(char side) {
        HashSet<Integer> area = new HashSet<Integer>(64, 1.f);
        for (ChessSquare square : grid) {
            if (square.isOccupiedBy(side)) {
                HashSet<Integer> attacks = attacksFrom(square);
                if ((attacks != null) && (attacks.size() != 0)) {
                    area.addAll(attacks);
                }
            }
        }
        return area;
    }

    /**
     * @param index the index in question
     * @return the side of the piece occupying the square at index
     */
    public char getOccupyingSide(int index) {
        return grid[index].getOccupant().getSide();
    }
}
