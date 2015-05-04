package com.example.kota.connectfour;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

import com.example.kota.connectfour.ui.HowToPlayDialogFragment;
import com.example.kota.connectfour.ui.HowToTrainingDialogFragment;
import com.example.kota.connectfour.ui.MaterialAlertDialog;
import com.example.kota.connectfour.ui.Question;
import com.example.kota.connectfour.ui.QuestionHolder;
import com.example.kota.connectfour.ui.TutorialDialogFragment;
import com.google.gson.Gson;

/**
 * Created by kota on 2015/02/19.
 * Sample Activity for development.
 * Use this activity when you want to test DialogFragment or something.
 */
public class Sample extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_training);

        Question q = QuestionHolder.getQuestion(QuestionHolder.EASY[0]);
        Gson gson = new Gson();
        Log.d("test",gson.toJson(q, Question.class));

        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#212121")));
        String message = getResources().getString(R.string.training_intro);
        String positive_text = getResources().getString(R.string.yes);
        MaterialAlertDialog.newInstance(
                message, positive_text, "",
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        HowToTrainingDialogFragment.newInstance(
                                new HowToPlayDialogFragment.OnButtonClickListener() {
                                    @Override
                                    public void onClicked() {
                                        // show dialog once more
                                        TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_FORTH)
                                                .show(getSupportFragmentManager(), "tutorial");
                                    }
                                })
                                .show(getSupportFragmentManager(), "missiles");
                    }
                },
                null
        ).show(getSupportFragmentManager(), "tutorial");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_list_single; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        getActionBar().setTitle(getResources().getString(R.string.activity_menu));
        return true;
    }

}
