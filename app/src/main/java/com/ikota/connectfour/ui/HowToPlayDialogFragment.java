package com.ikota.connectfour.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ikota.connectfour.R;

/**
 * Created by kota on 2015/03/04.
 * This dialog shows 2 types of tutorial.
 *
 */
public class HowToPlayDialogFragment extends android.support.v4.app.DialogFragment{
    // pass this flg when you create this dialog.
    public static final String EXTRA_DEMO_MODE = "demo_mode";
    public static final int FLG_DEMO_RULE = 1;
    public static final int FLG_DEMO_HOW_TO_WIN = 2;

    public interface OnButtonClickListener {
        public void onClicked();
    }
    private OnButtonClickListener mCallback;
    public void setCallback(OnButtonClickListener callback) {this.mCallback = callback;}

    public static HowToPlayDialogFragment newInstance(
            int flg,  OnButtonClickListener callback) {
        HowToPlayDialogFragment f = new HowToPlayDialogFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        f.setCallback(callback);
        args.putInt(EXTRA_DEMO_MODE, flg);
        return f;
    }

    private final int[] demo_play = {0,0,4,4,3,1,2,5,0,0,5,4,5,4,4,4,5,5,0,0,1,5,1,1,3,3,2,3,1,1,3,3,2,2};
    private final int[] win_demo_horizontal = {3,3,2,2,4,5,1};
    private final int[] win_demo_vertical = {3,2,3,2,3,2,3};
    private final int[] win_demo_diagonal = {0,3,2,4,3,4,5,5,4,5,5};

    private int[][] demo_moves;
    private int demo_index = 0;
    private int demo_move_count = 0;

    private Board.OnUpdateListener mUpdateListener = new Board.OnUpdateListener() {
        @Override
        public void onUpdated(int who, Pair<Integer, Integer> pos, boolean if_win) {
            Board board = mBoardHost.getBoard();
            // loop demonstration if it finished.
            // demonstration interval is 3 seconds
            if(if_win|| board.checkIfDraw()) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBoardHost.getBoard().refresh(new Board.BoardAnimListener() {
                            @Override
                            public void onAnimationFinished() {
                                // set next demo moves, if demo finished then restart demo again
                                demo_index = (demo_index+1)%demo_moves.length;

                                // init game state and start demonstration
                                demo_move_count = 0;
                                mBoardHost.getBoard().changeGameState(true);
                                mBoardHost.changePlayer(Board.FIRST_PLAYER);
                                mBoard.update(Board.FIRST_PLAYER, demo_moves[demo_index][demo_move_count++]);
                            }
                        });
                    }
                }, 1500);
            } else {
                // if the player who has updated is first player, then next player is second player
                if (who == Board.FIRST_PLAYER) {
                    mBoardHost.changePlayer(Board.SECOND_PLAYER);
                    mBoard.update(Board.SECOND_PLAYER, demo_moves[demo_index][demo_move_count++]);
                } else {
                    mBoardHost.changePlayer(Board.FIRST_PLAYER);
                    mBoard.update(Board.FIRST_PLAYER, demo_moves[demo_index][demo_move_count++]);
                }
            }
        }
    };

    private BoardHostForVS mBoardHost;
    private Board mBoard;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // get information about what this dialog will demonstrate
        String description;
        String ok_text;
        int demo_flg = getArguments().getInt(EXTRA_DEMO_MODE);
        if (demo_flg==FLG_DEMO_RULE) {
            demo_moves = new int[][] {demo_play};
            description = getResources().getString(R.string.rule1);
            ok_text = "How to Win?";
        } else {
            demo_moves = new int[][] {win_demo_horizontal, win_demo_vertical, win_demo_diagonal};
            description = getResources().getString(R.string.rule2);
            ok_text = "Got it !!";
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.how_to_play_dialog, null);
        ((TextView)layout.findViewById(R.id.message)).setText(description);

        // add board to the layout
        int connect_k=4;int width=7; int height=6;
        String first_player_name = getResources().getString(R.string.player1);
        String second_player_name = getResources().getString(R.string.player2);
        boolean is_hard = false;
        mBoardHost = new BoardHostForVS(getActivity(),
                connect_k, width, height, first_player_name, second_player_name, is_hard);
        mBoardHost.getBoard().setOnUpdateListener(mUpdateListener);
        mBoard = mBoardHost.getBoard();
        ((FrameLayout)layout.findViewById(R.id.board_container)).addView(mBoardHost);

        // start demonstration
        mBoard.update(Board.FIRST_PLAYER, demo_moves[demo_index][demo_move_count++]);

        builder.setView(layout)
                // Add action buttons
                .setPositiveButton(ok_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                       if(mCallback!=null) mCallback.onClicked();
                    }
                });

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return builder.create();
    }
}
