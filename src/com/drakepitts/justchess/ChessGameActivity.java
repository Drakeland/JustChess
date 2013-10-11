package com.drakepitts.justchess;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class ChessGameActivity extends Activity {
    private static final String TAG = "ChessGameActivity";

    private ChessBoard chessboard;
    private GridView chessboardView;
    private ChessSquareAdapter chessboardAdapter;
    private TextView alertView;
    private SharedPreferences prefs;
    private boolean flipBoard;
    private int selectedPosition;
    private char sideToMove;
    private String enPassantTargetSquare;
    private int halfMoveClock;
    private int wholeMoveNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_game);
        setupActionBar();

        Intent intent = getIntent();
        chessboard = new ChessBoard();
        alertView = (TextView) findViewById(R.id.alert_view);
        if (intent.getBooleanExtra(MainMenu.START_NEW_GAME, true)) {
            startNewGame();
        } else {
            continueGame();
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        chessboardView = (GridView) findViewById(R.id.chessboard);
        chessboardAdapter = new ChessSquareAdapter(this);
        chessboardView.setAdapter(chessboardAdapter);
        handleClickAt(-1, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FileOutputStream fos;
        try {
            fos = openFileOutput(getString(R.string.save_file), MODE_PRIVATE);
            fos.write(gameStateToFEN().getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        flipBoard = prefs
                .getBoolean(getString(R.string.pref_flip_board), false);
        setBorderColor(prefs.getString(getString(R.string.pref_bg_color),
                getString(R.string.pref_color_azure_value)));
        setPromotionType(prefs.getString(
                getString(R.string.pref_promotion_type),
                getString(R.string.pref_promotion_queen_value)));
        chessboardAdapter.updateContext(this);
        chessboardAdapter.notifyDataSetChanged();
        handleClickAt(-1, null);
    }

    private void setBorderColor(String borderColorValue) {
        if (borderColorValue
                .equals(getString(R.string.pref_color_classic_value))) {
            chessboardView.setBackgroundResource(R.color.board_border_classic);
        } else if (borderColorValue
                .equals(getString(R.string.pref_color_azure_value))) {
            chessboardView.setBackgroundResource(R.color.board_border_azure);
        } else if (borderColorValue
                .equals(getString(R.string.pref_color_mint_value))) {
            chessboardView.setBackgroundResource(R.color.board_border_mint);
        } else if (borderColorValue
                .equals(getString(R.string.pref_color_flame_value))) {
            chessboardView.setBackgroundResource(R.color.board_border_flame);
        } else if (borderColorValue
                .equals(getString(R.string.pref_color_jester_value))) {
            chessboardView.setBackgroundResource(R.color.board_border_jester);
        }
    }

    private void setPromotionType(String promotionTypeValue) {
        if (promotionTypeValue
                .equals(getString(R.string.pref_promotion_queen_value))) {
            chessboard.setPromotionType(ChessPieceType.QUEEN);
        } else if (promotionTypeValue
                .equals(getString(R.string.pref_promotion_rook_value))) {
            chessboard.setPromotionType(ChessPieceType.ROOK);
        } else if (promotionTypeValue
                .equals(getString(R.string.pref_promotion_bishop_value))) {
            chessboard.setPromotionType(ChessPieceType.BISHOP);
        } else if (promotionTypeValue
                .equals(getString(R.string.pref_promotion_knight_value))) {
            chessboard.setPromotionType(ChessPieceType.KNIGHT);
        }
    }

    /**
     * If no square is selected then select the clicked square if it's occupied
     * by a piece. If a square is selected then: - if the piece occupying the
     * selected square can move to the clicked square, then move it to the
     * clicked square; otherwise, deselect the selected square.
     * @param position the position a click is made at
     */
    public void handleClickAt(int position, View view) {
        if ((position == -1) || (view == null)
                || (selectedPosition == position)) {
            chessboardView.setSelection(-1);
            selectedPosition = -1;
        } else if ((position >= 0) && (position <= 077)) {
            if ((selectedPosition >= 0) && (selectedPosition <= 077)) {
                int fromIndex = positionToIndex(selectedPosition), toIndex = positionToIndex(position);
                HashSet<Integer> moves = chessboard.getGrid()[fromIndex]
                        .getOccupant().getPotentialMoves();
                if (moves != null) {
                    boolean legalMove = false;
                    Iterator<Integer> movesIt = moves.iterator();
                    while (movesIt.hasNext()) {
                        int checkIndex = movesIt.next();
                        if (checkIndex == toIndex) {
                            move(fromIndex, toIndex);
                            legalMove = true;
                            break;
                        }
                    }
                    if (!legalMove) {
                        Toast.makeText(this, R.string.illegal_move,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                selectedPosition = -1;
                view.setSelected(false);
            } else if (chessboard.getOccupyingSide(positionToIndex(position)) == sideToMove) {
                selectedPosition = position;
                view.setSelected(true);
            } else {
                selectedPosition = -1;
                view.setSelected(false);
            }
        }
        // Log.d(TAG, String.format(
        // "position[%02o] clicked. selectedPosition[%02o]\n", position,
        // selectedPosition));
        // Log.d(TAG, gameStateToFEN());

    }

    private void move(int fromIndex, int toIndex) {
        chessboard.moveFromTo(fromIndex, toIndex);
        if (chessboard.isValidPromotion(fromIndex, toIndex)) {
            chessboard.promote(toIndex);
        }
        switchSideToMove();
        setLegalMoves();
        updateBoard();
        if (chessboard.isGameOver()) {
            setGameOverAlert();
        }
    }

    /**
     * @param square square with an occupant
     * @return legal moves the occupant can make
     */
    private HashSet<Integer> legalMovesFrom(ChessSquare square) {
        if ((square == null) || (square.getOccupant() == null)) {
            return new HashSet<Integer>(0);
        }
        return legalMovesFrom(square.getIndex(),
                square.getOccupant().getType(), square.getOccupant().getSide());
    }

    /**
     * @param fromIndex moves from this index
     * @param type type of piece at this index
     * @param side side the piece is on
     * @return a set of legel moves this piece came make from fromIndex
     */
    private HashSet<Integer> legalMovesFrom(int fromIndex, ChessPieceType type,
            char side) {
        HashSet<Integer> moves = chessboard.movesFrom(fromIndex, type, side);
        Iterator<Integer> movesIt = moves.iterator();
        HashSet<Integer> toRemove = new HashSet<Integer>(moves.size() + 1, 1.f);
        // remove the moves that leave side in check
        ChessBoard tempBoard = new ChessBoard(chessboard);
        while (movesIt.hasNext()) {
            int checkIndex = movesIt.next();
            tempBoard.moveFromTo(fromIndex, checkIndex);
            tempBoard.setAttacks(ChessBoard.enemyOf(side));
            if (tempBoard.isInCheck(side)) {
                toRemove.add(checkIndex);
            }
            tempBoard = new ChessBoard(chessboard);
        }
        moves.removeAll(toRemove);
        return moves;
    }

    /**
     * Iterates over each ChessSquare in chessboard grid and sets the legal
     * moves for each occupant ChessPiece
     */
    public void setLegalMoves() {
        chessboard.setWhiteMoves(new HashSet<ChessMove>(64));
        chessboard.setBlackMoves(new HashSet<ChessMove>(64));
        HashSet<Integer> indices;
        Iterator<Integer> indicesIt;
        HashSet<ChessMove> moves = new HashSet<ChessMove>(16);
        for (int ii = 0; ii < chessboard.getGrid().length; ii++) {
            if (!chessboard.squareAt(ii).isOccupied()) {
                continue;
            }
            indices = legalMovesFrom(chessboard.squareAt(ii));
            chessboard.setPotentialMovesAt(ii, indices);
            indicesIt = indices.iterator();
            while (indicesIt.hasNext()) {
                moves.add(new ChessMove(chessboard.squareAt(ii), chessboard
                        .squareAt(indicesIt.next())));
            }
            if (chessboard.getOccupyingSide(ii) == 'w') {
                chessboard.whiteMovesAddAll(moves);
            } else if (chessboard.getOccupyingSide(ii) == 'b') {
                chessboard.blackMovesAddAll(moves);
            }
            moves = new HashSet<ChessMove>(16);
        }
    }

    private void updateBoard() {
        chessboardAdapter.updateContext(this);
        chessboardAdapter.notifyDataSetChanged();
        handleClickAt(-1, null); // Reset selected cell
    }

    private void setGameOverAlert() {
        switch (chessboard.getState()) {
        case ONE_ZERO:
            alertView.setText(R.string.white_wins);
            return;
        case ZERO_ONE:
            alertView.setText(R.string.black_wins);
            return;
        case HALF_HALF:
            alertView.setText(R.string.game_drawn);
        }
    }

    private void startNewGame() {
        chessboard.setPieces();
        setSideToMove('w');
        setLegalMoves();
    }

    private void continueGame() {
        boolean successful = true;
        StringBuffer s = new StringBuffer("");
        int ch;
        try {
            FileInputStream fis = openFileInput(getString(R.string.save_file));
            while ((ch = fis.read()) != -1) {
                s.append((char) ch);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.load_game_failed_fnfex),
                    Toast.LENGTH_LONG).show();
            successful = false;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.load_game_failed_ioex),
                    Toast.LENGTH_LONG).show();
            successful = false;
        }
        if (!successful) {
            startNewGame();
        } else {
            loadGameStateFromFEN(s.toString());
        }
    }

    private void setSideToMove(char side) {
        sideToMove = side;
        if (sideToMove == 'w') {
            chessboard.setAttacks('b');
            alertView.setText(R.string.white_to_move);
        } else if (sideToMove == 'b') {
            chessboard.setAttacks('w');
            alertView.setText(R.string.black_to_move);
        }
    }

    public char getSideToMove() {
        return sideToMove;
    }

    private void switchSideToMove() {
        if (sideToMove == 'w') {
            setSideToMove('b');
        } else if (sideToMove == 'b') {
            setSideToMove('w');
        }
    }

    private String gameStateToFEN() {
        // Example
        // s += " w KQkq - 0 1";

        // 1. Piece placement (from white's perspective)
        String s = chessboard.toFEN();
        // 2. Active color
        s += " " + sideToMove + " ";
        // 3. Castling availability
        ChessPieceType[] kinds = new ChessPieceType[] { ChessPieceType.KING,
                ChessPieceType.QUEEN };
        char[] sides = new char[] { 'w', 'b' };
        String t = "";
        for (ChessPieceType kind : kinds) {
            for (char side : sides) {
                if (chessboard.canCastle(side, kind)) {
                    t += (new ChessPiece(side, kind)).toChar();
                }
            }
        }
        s += (t == "") ? "-" : t;
        // 4. En passant target square
        // 5. Halfmove clock
        // 6. Fullmove number
        s += String.format(" %s %s %s", enPassantTargetSquare, halfMoveClock,
                wholeMoveNumber);
        return s;
    }

    private void loadGameStateFromFEN(String fen) {
        String[] fenParts = fen.split(" ");
        chessboard.setPieces(fenParts[0]);
        setSideToMove(fenParts[1].charAt(0));
        setLegalMoves();
        chessboard.setAvailableCastlings(fenParts[2].toCharArray());
        enPassantTargetSquare = fenParts[3];
        chessboard.setEnPassantTargetSquare(enPassantTargetSquare);
        halfMoveClock = Integer.parseInt(fenParts[4]);
        wholeMoveNumber = Integer.parseInt(fenParts[5]);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chessgame_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        case R.id.action_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void testMoves(View view) {
        System.out
                .println("***** testMoves() ************************************************************");
        HashSet<Integer> moves;
        Iterator<Integer> movesIt;
        ChessPiece currPiece;
        int currMove;
        for (int ii = 0; ii < 0100; ii++) {
            currPiece = chessboard.getGrid()[ii].getOccupant();
            if (chessboard.getGrid()[ii].getOccupant().getType() == ChessPieceType.NONE) {
                continue;
            }
            moves = chessboard.movesFrom(ii, currPiece.getType(),
                    currPiece.getSide());
            movesIt = moves.iterator();
            System.out.printf("-----movesFrom(%03o:%s, %s, '%s'): [", ii,
                    ChessBoard.indexToSquare(ii), currPiece.getType(),
                    currPiece.getSide());
            while (movesIt.hasNext()) {
                currMove = movesIt.next();
                System.out.printf("%03o:%s, ", currMove,
                        ChessBoard.indexToSquare(currMove));
            }
            System.out.print("]\n");
            moves = chessboard.attacksFrom(ii, currPiece.getType(),
                    currPiece.getSide());
            movesIt = moves.iterator();
            System.out.printf("---attacksFrom(%03o:%s, %s, '%s'): [", ii,
                    ChessBoard.indexToSquare(ii), currPiece.getType(),
                    currPiece.getSide());
            while (movesIt.hasNext()) {
                currMove = movesIt.next();
                System.out.printf("%03o:%s, ", currMove,
                        ChessBoard.indexToSquare(currMove));
            }
            System.out.print("]\n");

            moves = legalMovesFrom(ii, currPiece.getType(), currPiece.getSide());
            movesIt = moves.iterator();
            System.out.printf("legalMovesFrom(%03o:%s, %s, '%s'): [", ii,
                    ChessBoard.indexToSquare(ii), currPiece.getType(),
                    currPiece.getSide());
            while (movesIt.hasNext()) {
                currMove = movesIt.next();
                System.out.printf("%03o:%s, ", currMove,
                        ChessBoard.indexToSquare(currMove));
            }
            System.out.print("]\n");

            if (currPiece.getType() == ChessPieceType.KING) {
                System.out.println(currPiece.toString());
            }
        }
        moves = chessboard.attackArea('w');
        movesIt = moves.iterator();
        System.out.print("attackArea(w): [");
        while (movesIt.hasNext()) {
            currMove = movesIt.next();
            System.out.printf("%03o:%s, ", currMove,
                    ChessBoard.indexToSquare(currMove));
        }
        System.out.print("]\n");

        moves = chessboard.attackArea('b');
        movesIt = moves.iterator();
        System.out.print("attackArea(b): [");
        while (movesIt.hasNext()) {
            currMove = movesIt.next();
            System.out.printf("%03o:%s, ", currMove,
                    ChessBoard.indexToSquare(currMove));
        }
        System.out.print("]\n");

        System.out
                .println("**** /testMoves() ************************************************************");
    }

    public ChessBoard getChessboard() {
        return chessboard;
    }

    public ChessSquare getChessSquareAt(int position) {
        if (position == -1) {
            return null;
        }
        if (flipBoard) { // black view
            return chessboard.getGrid()[((7 - (position % 010)) * 010)
                    + (position / 010)];
        } else { // white view
            return chessboard.getGrid()[((position % 010) * 010)
                    + (7 - (position / 010))];
        }
    }

    public ChessSquare getSelectedSquare() {
        return getChessSquareAt(chessboardView.getSelectedItemPosition());
    }

    /**
     * Converts a GridView position to the index of the grid it's representing
     * @param position the GridView position
     * @return the index that position is represting
     */
    public int positionToIndex(int position) {
        if (position == -1) {
            return -1;
        }
        return getChessSquareAt(position).getIndex();
    }

}
