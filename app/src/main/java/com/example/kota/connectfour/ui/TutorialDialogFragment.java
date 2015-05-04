package com.example.kota.connectfour.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.example.kota.connectfour.R;

/**
 * Created by kota on 2015/03/04.
 * This Fragment represents tutorial dialog.
 * To specify which tutorial to display, pass CONSTANT like TUTORIAL_OO
 * when you create this Fragment by TutorialFragment.newInstance(index_here).
 *
 * For example, if you want to show tutorial of first one,
 * TutorialDialogFragment.newInstance(TutorialDialogFragment.TUTORIAL_FIRST).show(...);
 */
public class TutorialDialogFragment extends android.support.v4.app.DialogFragment{
    private static final String IMG_KEY = "img_key";
    public static final int TUTORIAL_FIRST = 1;
    public static final int TUTORIAL_SECOND = 2;
    public static final int TUTORIAL_THIRD = 3;
    public static final int TUTORIAL_FORTH = 4;

    public static TutorialDialogFragment newInstance(int index) {
        TutorialDialogFragment f = new TutorialDialogFragment();
        Bundle args = new Bundle();
        args.putInt(IMG_KEY, index);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get image of tutorial to show
        Bundle args = getArguments();
        int index = args.getInt(IMG_KEY);
        int img_id = 0;
        switch (index) {
            case TUTORIAL_FIRST: img_id = R.drawable.tutorial_img1; break;
            case TUTORIAL_SECOND: img_id = R.drawable.tutorial_img2; break;
            case TUTORIAL_THIRD: img_id = R.drawable.tutorial_img3; break;
            case TUTORIAL_FORTH: img_id = R.drawable.tutorial_img4; break;
        }

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View layout = inflater.inflate(R.layout.tutorial_dialog, null);
        ((ImageView)layout.findViewById(R.id.image)).setImageResource(img_id);
        builder.setView(layout)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                });

        return builder.create();
    }
}
