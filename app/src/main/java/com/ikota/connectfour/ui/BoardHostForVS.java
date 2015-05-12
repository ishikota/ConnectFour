package com.ikota.connectfour.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/03/17.
 * BoardHost child class which has header for VS activity.
 * Board initialization is done in parent BoardHost class.
 * This class just handles header view.(initialization and animation)
 */
public class BoardHostForVS extends BoardHost{
    private TextView first_player, second_player;
    private ObjectAnimator first_p_anim, second_p_anim;
    private boolean IS_HARD_MODE;

    public BoardHostForVS(Context context, int k, int column, int row,
                          String first_player_name, String second_player_name, boolean is_hard) {
        super(context, k, column, row, is_hard);
        // init header
        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View header = inflater.inflate(R.layout.vs_header, this);
        first_player = (TextView)header.findViewById(R.id.text1);
        second_player = (TextView)header.findViewById(R.id.text2);

        // change color scheme to dark theme
        if(is_hard) {
            header.findViewById(R.id.root_view).setBackgroundColor(Color.parseColor("#DD212121"));
            first_player.setTextColor(Color.parseColor("#FFFFFF"));
            second_player.setTextColor(Color.parseColor("#55FFFFFF"));
            TextView vs_text = (TextView)header.findViewById(R.id.vs_text);
            vs_text.setBackgroundResource(R.drawable.move_test3);
            vs_text.setTextColor(Color.BLACK);
            ((ImageView)header.findViewById(R.id.icon1)).setImageResource(R.drawable.move_test2);
            ((ImageView)header.findViewById(R.id.icon2)).setImageResource(R.drawable.move_test1);
        }
        first_player.setText(first_player_name);
        second_player.setText(second_player_name);

        initAnim();
        IS_HARD_MODE = is_hard;
    }

    private void initAnim() {
        PropertyValuesHolder pvhsx = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f,1.1f);
        PropertyValuesHolder pvhsy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f,1.1f);

        first_p_anim = ObjectAnimator.ofPropertyValuesHolder(first_player, pvhsx, pvhsy);
        first_p_anim.setDuration(500);
        first_p_anim.setRepeatMode(ValueAnimator.REVERSE);
        first_p_anim.setRepeatCount(-1);

        second_p_anim = ObjectAnimator.ofPropertyValuesHolder(second_player, pvhsx, pvhsy);
        second_p_anim.setDuration(500);
        second_p_anim.setRepeatMode(ValueAnimator.REVERSE);
        second_p_anim.setRepeatCount(-1);
    }

    /**
     * animate player's text to indicate who is the next player.
     * @param player : next player (Board.FIRST_PLAYER or Board.SECOND_PLAYER)
     */
    public void changePlayer(int player) {
        ObjectAnimator start_anim = player == Board.FIRST_PLAYER ? first_p_anim : second_p_anim;
        ObjectAnimator stop_anim  = player == Board.FIRST_PLAYER ? second_p_anim : first_p_anim;
        TextView start_player = player == Board.FIRST_PLAYER ? first_player : second_player;
        TextView stop_player = player == Board.FIRST_PLAYER ? second_player : first_player;

        int start_text_color = IS_HARD_MODE ? Color.parseColor("#FFFFFF") : Color.parseColor("#DE000000");
        int stop_text_color = IS_HARD_MODE ? Color.parseColor("#55FFFFFF") : Color.parseColor("#42000000");
        start_player.setTextColor(start_text_color);
        stop_player.setTextColor(stop_text_color);
        start_anim.start();
        stop_anim.cancel();
    }
}
