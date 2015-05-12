package com.ikota.connectfour.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ikota.connectfour.R;


/**
 * Created by kota on 2015/03/04.
 * Material theme dialog without title.
 *
 *  -- How to use --
 * MaterialAlertDialog.newInstance(
 *      String message, String positive_text, String negative_text,
 *      MaterialAlertDialog.onButtonClickListener positive_listener,
 *      MaterialAlertDialog.onButtonClickListener negative_listener
 *      ).show(getFragmentManager(), "tutorial");
 *
 * If you do not need negative button then
 * pass empty string to negative_text and pass null to negative_listener.
 */
public class MaterialAlertDialog extends android.support.v4.app.DialogFragment{

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_POSITIVE_TITLE = "positive";
    public static final String EXTRA_NEGATIVE_TITLE = "negative";

    public interface onButtonClickListener {
        public void onClicked();
    }
    private onButtonClickListener mPositiveListener, mNegativeListener;
    public void setPositiveListener(onButtonClickListener mPositiveListener) {
        this.mPositiveListener = mPositiveListener;
    }
    public void setNegativeListener(onButtonClickListener mNegativeListener) {
        this.mNegativeListener = mNegativeListener;
    }

    // static factory method with title
    public static MaterialAlertDialog newInstance(String title, String message, String positive, String negative,
                                                  onButtonClickListener positiveListener, onButtonClickListener negativeListener) {
        MaterialAlertDialog f = new MaterialAlertDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_TITLE, title);
        args.putString(EXTRA_MESSAGE, message);
        args.putString(EXTRA_POSITIVE_TITLE, positive);
        args.putString(EXTRA_NEGATIVE_TITLE, negative);
        f.setPositiveListener(positiveListener);
        f.setNegativeListener(negativeListener);
        f.setArguments(args);
        return f;
    }

    // static factory method without title
    public static MaterialAlertDialog newInstance(String message, String positive, String negative,
            onButtonClickListener positiveListener, onButtonClickListener negativeListener) {
        MaterialAlertDialog f = new MaterialAlertDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_MESSAGE, message);
        args.putString(EXTRA_POSITIVE_TITLE, positive);
        args.putString(EXTRA_NEGATIVE_TITLE, negative);
        f.setPositiveListener(positiveListener);
        f.setNegativeListener(negativeListener);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get arguments to set dialog
        Bundle arg = getArguments();
        String title = arg.getString(EXTRA_TITLE, "");
        String message = arg.getString(EXTRA_MESSAGE, "Are you sure?");
        final String positive = arg.getString(EXTRA_POSITIVE_TITLE, "YES");
        String negative = arg.getString(EXTRA_NEGATIVE_TITLE, "NO");

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.material_dialog, null);
        ((TextView)layout.findViewById(R.id.message)).setText(message);
        ((Button)layout.findViewById(R.id.btn_positive)).setText(positive);
        ((Button)layout.findViewById(R.id.btn_negative)).setText(negative);

        // if this dialog doesn't have title, remove title view
        TextView title_view = (TextView)layout.findViewById(R.id.title);
        if(title.isEmpty()) {
            title_view.setVisibility(View.GONE);
        } else {
            title_view.setText(title);
        }

        //set listener
        layout.findViewById(R.id.btn_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPositiveListener!=null) mPositiveListener.onClicked();
                dismiss();
            }
        });
        layout.findViewById(R.id.btn_negative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNegativeListener!=null) mNegativeListener.onClicked();
                dismiss();
            }
        });
        builder.setView(layout);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}
