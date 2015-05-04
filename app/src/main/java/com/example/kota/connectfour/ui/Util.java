package com.example.kota.connectfour.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.kota.connectfour.R;
import com.example.kota.connectfour.strategy.BaseStrategy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by kota on 2015/01/07.
 *  This class is utility method holder.
 */
public class Util {
    private static Util instance;
    private Util(){}
    public static synchronized Util getInstance() {
        if(instance == null) {
            instance = new Util();
        }
        return instance;
    }

    /**
     * Reset all flg to default value.
     * @param context context
     */
    public void resetData(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("is_newone", true);
        editor.putBoolean("has_demo_done", false);
        editor.putBoolean(TrainingActivity.FLG_TRAINING_TIMING, false);
        editor.putBoolean(TrainingActivity.FLG_HAS_PLAYED_TRAINING, false);
        editor.apply();
    }

    /**
     * Make a ImageView of move.
     * @param player     : make a move in this player color.
     * @param is_hard    : if hard mode then change color scheme to dark theme
     * @return imageView : ImageView of move which is specified user color and proper size.
     */
    public ImageView createMove(Context context, int player, boolean is_hard) {
        ImageView imageView = new ImageView(context);
        int WP = ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WP,WP);
        int dp8 = (int)(8*context.getResources().getDisplayMetrics().density);
        params.setMargins(dp8,dp8,dp8,dp8);
        imageView.setLayoutParams(params);

        if (player==Board.FIRST_PLAYER)
            imageView.setImageResource(is_hard ? R.drawable.move_test2 : R.drawable.move_red);
        else if(player==Board.SECOND_PLAYER)
            imageView.setImageResource(is_hard ? R.drawable.move_test1 : R.drawable.move_yellow);
        else
            imageView.setImageResource(R.drawable.move_blank);

        return imageView;
    }

    /**
     * this method execute CPU thinking process in background.
     * Work flow
     * 1. visible progress bar and prohibit to board access.
     * 2. execute thinking process in background.
     * 3. invisible progress bar and update board.
     * 4. update listener set in Board instance is called if you have set.
     */
    public void startThinking(BaseStrategy strategy, Board board, ProgressBar progressBar) {
        CPUThinkingTask task  = new CPUThinkingTask(strategy, board, progressBar);
        task.execute();
    }

    public ArrayList<String> readQuestionFile(Context context) {
        ArrayList<String> q_json_list = new ArrayList<String>();
        if(context == null) return q_json_list;
        BufferedReader in = null;
        String str;
        try {
            in = new BufferedReader(new InputStreamReader(context.getAssets().open("data.txt")));
            while ((str = in.readLine())!=null) q_json_list.add(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return q_json_list;
    }

    private static String EXTRA_GOOD_QUESTION = "qood_question";

    public void resetGoodQuestion(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(EXTRA_GOOD_QUESTION, "");
        editor.apply();
    }

    public void memoGoodQuestion(Context context, String json) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder builder = new StringBuilder();
        builder.append(pref.getString(EXTRA_GOOD_QUESTION, ""));
        builder.append("&");
        builder.append(json);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(EXTRA_GOOD_QUESTION, builder.toString());
        editor.apply();
        Log.i("GoodQuestion", "good Question : "+json);
    }

    public void logGoodQuestions(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String str = pref.getString(EXTRA_GOOD_QUESTION, "");
        String[] questions = str.split("&",0);
        for (String q: questions) {
            Log.i("Good questions", q);
        }
    }

    /**
     * This class execute CPU thinking process in background.
     * When you receive its result (the column to put next),
     * add Board.OnUpdateListener on your board.
     * Callback will be called when board is updated.
     */
    // TODO : do not update board when the game finished and new game starting during thinking process.
    public class CPUThinkingTask extends AsyncTask<Void,Void,Integer> {
        private final ProgressBar mProgressBar;
        private final Board mBoard;
        private final BaseStrategy mStrategy;
        private final int game_id;

        /**
         * If you don't need to show progress bar then pass null as argument.
         * @param strategy    : the strategy which execute thinking process.
         * @param board       : the board to start thinking process.
         * @param progressBar : the progressbar to show during thinking process.
         */
        public CPUThinkingTask(BaseStrategy strategy, Board board, ProgressBar progressBar) {
            mStrategy = strategy;
            mBoard = board;
            mProgressBar = progressBar;
            game_id = mBoard.getGameId().intValue();
        }

        @Override
        protected void onPreExecute() {
            mBoard.acceptInput(false);
            if(mProgressBar!=null) {
                mProgressBar.setVisibility(View.VISIBLE);
                mStrategy.setProgressListener(new BaseStrategy.ThinkingProgressListener() {
                    @Override
                    public void publishProgress(int progress) {
                        mProgressBar.setProgress(progress);
                    }
                });
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            return mStrategy.think(mBoard);
        }

        @Override
        protected void onPostExecute(Integer column) {
            // if game was restarted during this thinking task, then do not update.
            if(game_id == mBoard.getGameId().intValue()) {
                mBoard.update(mStrategy.ME, column);
            } else {
                Log.e("deprecate_menu_activity", "illegal board access");
            }
            if(mProgressBar!=null) mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}
