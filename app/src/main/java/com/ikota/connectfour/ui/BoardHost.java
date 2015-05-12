package com.ikota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/01/16.
 * BoardHost contains Board and board background.
 * We left 88dp top margin in the layout
 * so child class can add header view to the space.
 */
public class BoardHost extends FrameLayout{
    private Board mBoard;
    private LinearLayout mBoard_bg;

    public BoardHost(Context context, int k, int column, int row,boolean is_hard) {
        super(context);
        // init layout
        int board_width = (int)(getResources().getDisplayMetrics().widthPixels-getResources().getDisplayMetrics().density*32);
        int board_height = (int)( (row)*(getResources().getDisplayMetrics().widthPixels*1.0/column));
        int header_height = (int)(getResources().getDimension(R.dimen.header_height));

        // init board background
        mBoard_bg = createBoardBackground(context, header_height, board_width, board_height, column, row, is_hard);
        // init board
        mBoard = new Board(context,k,column,row, is_hard);
        mBoard.setColumnTouchListener(new Board.ColumnTouchListener() {
            @Override
            public void onTouch(int column) {
                int row = mBoard.getPosition()[column];
                LinearLayout columnView = (LinearLayout)mBoard_bg.getChildAt(column);
                ImageView move = (ImageView)columnView.getChildAt(mBoard.ROW-1-row);
                if(move!=null && mBoard.checkIfAccepting()) animateMove(move);
            }
        });
        FrameLayout.LayoutParams board_param = new FrameLayout.LayoutParams(board_width, board_height);
        board_param.setMargins(0, header_height, 0, 0);
        mBoard.setLayoutParams(board_param);

        addView(mBoard_bg);
        addView(mBoard);
    }

    /**
     *  create background of board which is filled with blank moves.
     * @param context      : for creating LinearLayout instance.
     * @param width_px     : width of board in px
     * @param height_px    : width on board in px
     * @param board_width  : the number of column in board
     * @param board_height : the number of row in board
     * @return parent      : LinearLayout which is filled with blank moves
     */
    public LinearLayout createBoardBackground(
            Context context, int header_height,
            int width_px, int height_px, int board_width, final int board_height, boolean is_hard) {

        // initialize parent layout with passed size.
        LinearLayout parent = new LinearLayout(context);
        FrameLayout.LayoutParams parent_param = new LayoutParams(width_px, height_px);
        parent_param.setMargins(0, header_height, 0, 0);
        parent.setLayoutParams(parent_param);
        parent.setOrientation(LinearLayout.HORIZONTAL);

        // add blank moves on all square.
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        for(int i=0;i<board_width;i++) {
            LinearLayout child = new LinearLayout(context);
            child.setGravity(Gravity.BOTTOM);
            child.setOrientation(LinearLayout.VERTICAL);
            child.setLayoutParams(params);
            for(int j=0;j<board_height;j++) {
                child.addView(Util.getInstance().createMove(context, Board.NONE, is_hard));
            }
            //child.setOnTouchListener(touchListener);
            parent.addView(child);
        }

        return parent;
    }

    public Board getBoard(){return mBoard;}
    public LinearLayout getBoardBackground(){return mBoard_bg;}

    /**
     * When player touches the column then emphasize next possible move in the column.
     * @param v : the view in next possible column in the column player is touching.
     */
    private void animateMove(final ImageView v) {
        //v.setImageResource(R.drawable.move_hint);
        PropertyValuesHolder holderSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.5f);
        PropertyValuesHolder holderSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.5f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(v, holderSX, holderSY);
        animator.setInterpolator(new OvershootInterpolator());
        animator.setDuration(300);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                v.setImageResource(R.drawable.move_blank);
            }
        });
        animator.start();
    }

}
