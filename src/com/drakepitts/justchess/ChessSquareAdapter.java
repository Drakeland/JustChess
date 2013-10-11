package com.drakepitts.justchess;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class ChessSquareAdapter extends BaseAdapter {
    private static final String TAG = "ChessSquareAdapter";

    private ChessGameActivity activity;
    private SharedPreferences prefs;
    private ImageView imageView;
    private View squareContainer;
    private ChessSquare currSquare;
    private boolean dragAndDrop;
    private int side;
    private int pieceResId;
    private String bgColorValue;
    private String[] bgColorValues;

    public ChessSquareAdapter(Context c) {
        activity = (ChessGameActivity) c;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        updateContext(activity);
    }

    @Override
    public int getCount() {
        return 0100;
    }

    @Override
    public Object getItem(int position) {
        return activity.getChessSquareAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateContext(ChessGameActivity c) {
        activity = c;
        dragAndDrop = prefs.getBoolean(
                activity.getString(R.string.pref_drag_and_drop), false);
        bgColorValues = activity.getResources().getStringArray(
                R.array.pref_bg_color_values);
        bgColorValue = prefs.getString(
                activity.getString(R.string.pref_bg_color), bgColorValues[0]);
    }

    private int getChessPieceResId(ChessPiece piece) {
        switch (piece.toChar()) {
        case 'K':
            return R.drawable.wk;
        case 'Q':
            return R.drawable.wq;
        case 'R':
            return R.drawable.wr;
        case 'B':
            return R.drawable.wb;
        case 'N':
            return R.drawable.wn;
        case 'P':
            return R.drawable.wp;
        case 'k':
            return R.drawable.bk;
        case 'q':
            return R.drawable.bq;
        case 'r':
            return R.drawable.br;
        case 'b':
            return R.drawable.bb;
        case 'n':
            return R.drawable.bn;
        case 'p':
            return R.drawable.bp;
        default:
            return 0;
        }
    }

    private int getDarkColorResId(String bgColorValue) {
        if (bgColorValue.equals(bgColorValues[0])) {
            return R.drawable.dark_square_classic;
        } else if (bgColorValue.equals(bgColorValues[1])) {
            return R.drawable.dark_square_azure;
        } else if (bgColorValue.equals(bgColorValues[2])) {
            return R.drawable.dark_square_mint;
        } else if (bgColorValue.equals(bgColorValues[3])) {
            return R.drawable.dark_square_flame;
        } else if (bgColorValue.equals(bgColorValues[4])) {
            return R.drawable.dark_square_jester;
        }
        return 0;
    }

    private int getLightColorResId(String bgColorValue) {
        if (bgColorValue.equals(bgColorValues[0])) {
            return R.drawable.light_square_classic;
        } else if (bgColorValue.equals(bgColorValues[1])) {
            return R.drawable.light_square_azure;
        } else if (bgColorValue.equals(bgColorValues[2])) {
            return R.drawable.light_square_mint;
        } else if (bgColorValue.equals(bgColorValues[3])) {
            return R.drawable.light_square_flame;
        } else if (bgColorValue.equals(bgColorValues[4])) {
            return R.drawable.light_square_jester;
        }
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        squareContainer = convertView;
        side = parent.getWidth() / 010;
        currSquare = activity.getChessSquareAt(position);
        pieceResId = getChessPieceResId(currSquare.getOccupant());

        if (convertView == null) {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            squareContainer = layoutInflater.inflate(R.layout.chess_square,
                    null);
            imageView = (ImageView) squareContainer
                    .findViewById(R.id.chess_piece);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(side, side));
            imageView.setImageResource(pieceResId);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.handleClickAt(position, (View) view.getParent());
                }
            });
            squareContainer.setPadding(0, 0, 0, 0);
        } else {
            imageView = (ImageView) squareContainer
                    .findViewById(R.id.chess_piece);
            imageView.setImageResource(pieceResId);
        }

        if (currSquare.getColor() == 'b') {
            squareContainer
                    .setBackgroundResource(getDarkColorResId(bgColorValue));
        } else if (currSquare.getColor() == 'w') {
            squareContainer
                    .setBackgroundResource(getLightColorResId(bgColorValue));
        }

        if (dragAndDrop) {
            // set the image's drag listener
            imageView.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder shadowBuilder = new DragShadowBuilder(
                            view);
                    if (view.startDrag(data, shadowBuilder, view, 0)) {
                        activity.handleClickAt(position,
                                (View) view.getParent());
                        return true;
                    }
                    return false;
                }
            });
            // set the container's drag listener
            squareContainer.setOnDragListener(new OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent event) {
                    switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        view.setHovered(true);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        view.setHovered(false);
                        return true;
                    case DragEvent.ACTION_DRAG_STARTED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        View targetView = (View) event.getLocalState();
                        ViewGroup targetOwner = (ViewGroup) targetView
                                .getParent();
                        if (targetOwner == view.getParent()) {
                            activity.handleClickAt(position, targetView);
                            break;
                        }
                        activity.handleClickAt(position, view);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        break;
                    default:
                        break;
                    }
                    return true;
                }
            });
        } else {
            imageView.setLongClickable(false);
            squareContainer.setOnDragListener(null);
        }

        return squareContainer;
    }
}
