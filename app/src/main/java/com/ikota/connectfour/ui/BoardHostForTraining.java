package com.ikota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/03/17.
 * Board background for training activity.
 * This boardHost implements header for training activity.
 */
public class BoardHostForTraining extends BoardHost{
    private View mHeader;
    private TextView mFirstTextView, mTurnNum, mLastTextView;
    // if header displaying the number of left turn or not(displaying question result).
    private boolean mDisplayingTurnNum = true;

    public BoardHostForTraining(Context context, int k, int column, int row, boolean is_hard) {
        super(context, k, column, row, is_hard);
        // init header
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.training_header, this);
        mHeader = header.findViewById(R.id.root_view);
        mFirstTextView = (TextView)header.findViewById(R.id.text1);
        mTurnNum = (TextView)header.findViewById(R.id.turn_num);
        mLastTextView = (TextView)header.findViewById(R.id.text3);

        // change color scheme to dark theme
        if(is_hard) {
            header.findViewById(R.id.root_view).setBackgroundColor(Color.parseColor("#DD212121"));
        }
    }

    /**
     * Change the number of left turn with animation.
     * @param next_turn_num : new number of turn to set
     */
    public void changeTurnNum(final int next_turn_num) {
        if(!mDisplayingTurnNum) {
            mDisplayingTurnNum = true;
            displayTurnNum(next_turn_num);
            return;
        }
        PropertyValuesHolder pvhsx2 = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f,0f);
        PropertyValuesHolder pvhsy2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f,0f);
        final ObjectAnimator disappear_anim = ObjectAnimator.ofPropertyValuesHolder(mTurnNum, pvhsx2, pvhsy2);
        disappear_anim.setInterpolator(new AnticipateInterpolator());
        disappear_anim.setDuration(300);

        PropertyValuesHolder pvhsx1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f,1f);
        PropertyValuesHolder pvhsy1 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f,1f);
        final ObjectAnimator appear_anim = ObjectAnimator.ofPropertyValuesHolder(mTurnNum, pvhsx1, pvhsy1);
        appear_anim.setDuration(300);
        appear_anim.setInterpolator(new OvershootInterpolator());

        disappear_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTurnNum.setText(String.valueOf(next_turn_num));
                appear_anim.start();
            }
        });
        disappear_anim.start();
    }

    /**
     * Display the number of turn allowed in question.
     * This method is called when switch header from displaying result to turn number.
     * @param next_turn_num : the number of left turn to display.
     */
    public void displayTurnNum(final int next_turn_num) {
        PropertyValuesHolder pvhsx2 = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f,0f);
        PropertyValuesHolder pvhsy2 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f,0f);
        final ObjectAnimator disappear_anim = ObjectAnimator.ofPropertyValuesHolder(mHeader, pvhsx2, pvhsy2);
        disappear_anim.setInterpolator(new AnticipateInterpolator());
        disappear_anim.setDuration(300);

        PropertyValuesHolder pvhsx1 = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f,1f);
        PropertyValuesHolder pvhsy1 = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f,1f);
        final ObjectAnimator appear_anim = ObjectAnimator.ofPropertyValuesHolder(mHeader, pvhsx1, pvhsy1);
        appear_anim.setDuration(300);
        appear_anim.setInterpolator(new OvershootInterpolator());

        disappear_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                Resources r = getResources();
                mFirstTextView.setText(r.getString(R.string.win_in));
                mTurnNum.setText(String.valueOf(next_turn_num));
                mLastTextView.setText(r.getString(R.string.turn));
                appear_anim.start();
            }
        });
        disappear_anim.start();
    }

    /**
     * Display question result(success/failure) on header with animation
     * @param success : if succeeded question or not.
     */
    public void displayGameResult(final boolean success) {
        mDisplayingTurnNum = false;
        // shrink animation
        ObjectAnimator anim1 = ObjectAnimator.ofPropertyValuesHolder(mHeader,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.1f)
        );
        anim1.setDuration(300);

        // expand animation
        final ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(mHeader,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.1f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.1f, 1.0f)
        );
        anim2.setDuration(300);
        anim2.setInterpolator(new OvershootInterpolator());

         /* add listener to change background during animation transition */
        anim1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                int id_message = success ? R.string.success : R.string.failure;
                mTurnNum.setText(getResources().getString(id_message));
                mFirstTextView.setText("");
                mLastTextView.setText("");
                anim2.start();
            }
        });

        anim1.start();
    }

}
