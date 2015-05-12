package com.ikota.connectfour.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/03/21.
 * Dialog of training tutorial.
 */
public class HowToTrainingDialogFragment extends DialogFragment{
    private BoardHostForTraining mBoardHost;
    private int question_id=0;
    private Question current_question;

    private HowToPlayDialogFragment.OnButtonClickListener mCallback;
    public void setCallback(HowToPlayDialogFragment.OnButtonClickListener callback) {this.mCallback = callback;}

    private final Board.OnUpdateListener mAnswerUpdateListener = new Board.OnUpdateListener() {
        @Override
        public void onUpdated(int who, Pair<Integer, Integer> pos, boolean if_win) {
            if(getActivity()==null) return;
            current_question.forwardState();
            int left_turn_num = current_question.limit_turn - current_question.getCurrentTurn();
            mBoardHost.changeTurnNum(left_turn_num);
            if(if_win) {
                mBoardHost.displayGameResult(true);
                startNextQuestion(2500);
            } else {
                if(left_turn_num == 0) {
                    mBoardHost.displayGameResult(false);
                    startNextQuestion(2500);
                    return;
                }
                try {
                    mBoardHost.getBoard().update(current_question.getNextPlayer(), current_question.getAnswerMove());
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static HowToTrainingDialogFragment newInstance(
            HowToPlayDialogFragment.OnButtonClickListener callback) {
        HowToTrainingDialogFragment f = new HowToTrainingDialogFragment();
        f.setCallback(callback);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String description = getResources().getString(R.string.training_rule);
        String ok_text = "Got it !!";

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.how_to_play_dialog, null);
        ((TextView)layout.findViewById(R.id.message)).setText(description);

        // add board to the layout
        int connect_k=4;int width=7; int height=6;
        mBoardHost = new BoardHostForTraining(getActivity(), connect_k, width, height,false);
        mBoardHost.getBoard().setOnUpdateListener(mAnswerUpdateListener);
        ((FrameLayout)layout.findViewById(R.id.board_container)).addView(mBoardHost);

        // start demonstration
        showAnswer();

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

    private void showAnswer() {
        current_question = QuestionHolder.getQuestion(QuestionHolder.DEMO[question_id]);
        mBoardHost.getBoard().setBoard(current_question.table, current_question.position, new Board.BoardAnimListener() {
            @Override
            public void onAnimationFinished() {
                mBoardHost.getBoard().changeGameState(true);
                mBoardHost.getBoard().setOnUpdateListener(mAnswerUpdateListener);
                mBoardHost.getBoard().update(current_question.getNextPlayer(), current_question.getAnswerMove());
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBoardHost.changeTurnNum(current_question.limit_turn);
            }
        }, 1000);
    }

    private void startNextQuestion(int start_delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // start next question
                question_id = question_id^1;
                current_question = QuestionHolder.getQuestion(QuestionHolder.DEMO[question_id]);
                showAnswer();
            }
        }, start_delay);
    }

}
