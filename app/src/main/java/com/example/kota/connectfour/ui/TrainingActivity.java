package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.kota.connectfour.R;
import com.example.kota.connectfour.strategy.BaseStrategy;
import com.example.kota.connectfour.strategy.IterativeDeepening;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by kota on 2015/03/17.
 * This Activity works as Training mode.
 */
public class TrainingActivity extends FragmentActivity{
    // if the value of preference with this key is true, recommend training mode to user.
    // recommend timing is when user win the game and not in demo(!is_new_one).
    public static final String FLG_TRAINING_TIMING = "training_timing";
    // check if this is the first time of training mode.
    public static final String FLG_HAS_PLAYED_TRAINING = "has_played_training";

    private ProgressBar mProgress;
    private BoardHostForTraining mBoardHost;
    private BaseStrategy mFirstStrategy, mSecondStrategy;

    // difficulty range is start inclusive and end exclusive ( Pair(a,b) => [a,b) )
    private static final int ALL=0, EASY = 1, NORMAL = 2, HARD = 3;
    private static final Pair<Integer, Integer> ALL_RANGE = new Pair<Integer, Integer>(0,39);
    private static final Pair<Integer, Integer> EASY_RANGE = new Pair<Integer, Integer>(0, 10);
    private static final Pair<Integer, Integer> NORMAL_RANGE = new Pair<Integer, Integer>(10, 20);
    private static final Pair<Integer, Integer> HARD_RANGE = new Pair<Integer, Integer>(20, 39);

    private static final int INDEX_DEMO = -2;
    private int mCurrentMode = ALL;
    private int mCurrentQIndex=-1;
    private ArrayList<String> mQHolder;
    private Question current_question;

    private LinearLayout mFooter;
    private static final int STATE_FINISHED = 1;
    private static final int STATE_PLAYING = 2;
    //private static final int STATE_SUCCESS = 3;
    private static final int STATE_ANSWER = 4;

    private final Board.OnUpdateListener mUpdateListener = new Board.OnUpdateListener() {
        @Override
        public void onUpdated(int who, Pair<Integer, Integer> pos, boolean if_win) {
            boolean is_player_first = current_question.first_player == Board.FIRST_PLAYER;
            int left_turn_num = current_question.limit_turn - ++current_question.current_turn_num;
            mBoardHost.changeTurnNum(left_turn_num);
            Board board = mBoardHost.getBoard();

            if(if_win|| board.checkIfDraw()) {
                // if this game has finished, close the access to the board.
                board.changeGameState(false);
                board.acceptInput(false);

                boolean solved = who == current_question.first_player;
                mBoardHost.displayGameResult(solved);
//                if(solved) {
//                    startNextQuestion(2500);
//                } else {
                    changeFooter(STATE_FINISHED);
//                }

            } else {
                if(left_turn_num == 0) {
                    mBoardHost.displayGameResult(false);
                    changeFooter(STATE_FINISHED);
                    return;
                }

                // if next player is USER then accept board touch , else CPU starts to think.
                if( (who == Board.FIRST_PLAYER && !is_player_first) ||
                        (who == Board.SECOND_PLAYER && is_player_first)) {
                    board.acceptInput(true);
                } else {
                    BaseStrategy s = who == Board.FIRST_PLAYER ? mSecondStrategy : mFirstStrategy;
                    Util.getInstance().startThinking(s, board, mProgress);
                }
            }
        }
    };

    private final Board.OnUpdateListener mAnswerUpdateListener = new Board.OnUpdateListener() {
        @Override
        public void onUpdated(int who, Pair<Integer, Integer> pos, boolean if_win) {
            current_question.forwardState();
            int left_turn_num = current_question.limit_turn - current_question.getCurrentTurn();
            mBoardHost.changeTurnNum(left_turn_num);

            // if left turn is less than 10 ,computer doesn't take long time to think.
            // So don't need ProgressBar.
            if(left_turn_num < 10) mProgress = null;

            if(if_win) {
                // finished answer demo
                changeFooter(STATE_FINISHED);
                mBoardHost.displayGameResult(true);
                mBoardHost.getBoard().setOnUpdateListener(mUpdateListener);
            } else {
                try {
                    mBoardHost.getBoard().update(current_question.getNextPlayer(), current_question.getAnswerMove());
                } catch (ArrayIndexOutOfBoundsException e) {
                    Toast.makeText(TrainingActivity.this, "Sorry, an Error Occurred.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevents screen from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.training);
        int connect_k=4;int width=7; int height=6;

        // init strategy
        mFirstStrategy = new IterativeDeepening(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
        ((IterativeDeepening)mFirstStrategy).setParameter(IterativeDeepening.MODE_TIME_LIMIT, 3);
        mSecondStrategy = new IterativeDeepening(Board.SECOND_PLAYER, Board.FIRST_PLAYER);
        ((IterativeDeepening)mSecondStrategy).setParameter(IterativeDeepening.MODE_TIME_LIMIT, 3);

        // init layout
        mBoardHost = new BoardHostForTraining(this,connect_k, width, height, false);
        mBoardHost.getBoardBackground().setVisibility(View.INVISIBLE);
        mBoardHost.getBoard().setOnUpdateListener(mUpdateListener);
        ((RelativeLayout)findViewById(R.id.board_container)).addView(mBoardHost);

        // init question data
        mQHolder = Util.getInstance().readQuestionFile(getApplicationContext());
        if(mQHolder.size() == 0) Toast.makeText(this, "Failed in reading question", Toast.LENGTH_SHORT).show();

        mFooter = (LinearLayout)findViewById(R.id.footer);
//        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                changeFooter(STATE_FINISHED);
//            }
//        });

        startBoardAppearAnim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fadein_depth, R.anim.slide_out_to_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                //startBoardDisappearAnim();
                onBackPressed();
                break;
            case R.id.tutorial:
                startTutorial();
                break;
            case R.id.easy:
                mCurrentMode = EASY;
                startNextQuestion(0);
                break;
            case R.id.normal:
                mCurrentMode = NORMAL;
                startNextQuestion(0);
                break;
            case R.id.hard:
                mCurrentMode = HARD;
                startNextQuestion(0);
                break;
            case R.id.random:
                mCurrentMode = ALL;
                startNextQuestion(0);
                break;
            case R.id.action_reset_data:
                Util.getInstance().resetData(TrainingActivity.this);
                break;
//            case R.id.memo_good:
//                String json = mQHolder.get(mCurrentQIndex);
//                Util.getInstance().memoGoodQuestion(TrainingActivity.this, json);
//                break;
//            case R.id.log_good:
//                Util.getInstance().logGoodQuestions(TrainingActivity.this);
//                break;
//            case R.id.good_reset:
//                Util.getInstance().resetGoodQuestion(TrainingActivity.this);
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // currently not used, because automatically start next question may be annoying user
    // and start delay causes conflict when user manually started next question during delay.
    private void startNextQuestion(int start_delay) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurrentQIndex = getNextQuestionIndex(mCurrentMode);
                current_question = getQuestion(mQHolder, mCurrentQIndex);
                Log.i("startNextQuestion", "Next Question ID : "+mCurrentQIndex);
                startQuestion(current_question);
            }
        }, start_delay);
    }

    private Question getQuestion(ArrayList<String> holder, int index) {
        // demo question is stored in QuestionHolder as Question class object
        if(index == INDEX_DEMO) return QuestionHolder.getQuestion(QuestionHolder.DEMO[1]);
        // other question is stored in assets/data.txt by json format
        String json = holder.get(index);
        Gson gson = new Gson();
        return gson.fromJson(json, Question.class);
    }

    private static final Random mRandom = new Random();
    private int getNextQuestionIndex(int mode) {
        Pair<Integer, Integer> range;
        switch (mode) {
            case EASY: range = EASY_RANGE; break;
            case NORMAL: range = NORMAL_RANGE; break;
            case HARD: range = HARD_RANGE; break;
            default: range = ALL_RANGE; break;
        }
        int num = mRandom.nextInt(range.second - range.first);
        return num+range.first;
    }

    /**
     * set board to the question state and display left turn.
     * @param q : instance of Question object which stores the information of question.
     */
    private void startQuestion(final Question q) {
        changeFooter(STATE_PLAYING);
        current_question = q;
        // init cpu strategy : Change strategy thinking limit by question's difficulty.
        BaseStrategy s = q.first_player ==Board.FIRST_PLAYER ? mSecondStrategy: mFirstStrategy;
        if(q.limit_turn >= 10) {
            ((IterativeDeepening)s).setParameter(IterativeDeepening.MODE_TIME_LIMIT, 3);
            mProgress = (ProgressBar) findViewById(R.id.progress);
        } else {
            ((IterativeDeepening)s).setParameter(IterativeDeepening.MODE_DEPTH_LIMIT, q.limit_turn);
            mProgress = null;
        }

        mBoardHost.getBoard().setBoard(q.table, q.position, new Board.BoardAnimListener() {
            @Override
            public void onAnimationFinished() {
                mBoardHost.getBoard().changeGameState(true);
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBoardHost.changeTurnNum(q.limit_turn);
            }
        }, 1000);
    }

    private void showAnswer() {
        changeFooter(STATE_ANSWER);
        current_question = getQuestion(mQHolder, mCurrentQIndex);
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

    private View.OnClickListener[] listeners = {
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retry
                    current_question = getQuestion(mQHolder, mCurrentQIndex);
                    startQuestion(current_question);
                }
            },
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAnswer();
                }
            },
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startNextQuestion(0);
                }
            }
    };

    private void changeFooter(int state) {
        mFooter.removeAllViewsInLayout();
        switch (state) {
            case STATE_FINISHED:
                String[] texts = {"Retry","Answer","Next"};
                for(int i=0;i<texts.length;i++) {
                    boolean need_margin = i==1;
                    Button button = createButton(texts[i], need_margin);
                    button.setOnClickListener(listeners[i]);
                    mFooter.addView(button);
                }
                break;
            case STATE_PLAYING:
                Button button1 = createButton("Give up", false);
                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeFooter(1);
                    }
                });
                mFooter.addView(button1);
                break;
        }
    }

    private Button createButton(String text, boolean need_margin) {
        final int WP = LinearLayout.LayoutParams.WRAP_CONTENT;
        Button button = new Button(TrainingActivity.this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(WP, WP);
        if(need_margin) {
            int dp_8 = (int)getResources().getDimension(R.dimen.dp8);
            p.setMargins(dp_8, 0, dp_8, 0);
        }
        button.setLayoutParams(p);
        button.setBackgroundColor(getResources().getColor(R.color.red));
        button.setTextColor(Color.WHITE);
        button.setText(text);
        return button;
    }

    private void startTutorial() {
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
                                    }
                                })
                                .show(getSupportFragmentManager(), "missiles");
                    }
                },
                null
        ).show(getSupportFragmentManager(), "tutorial");
    }

    private void startBoardAppearAnim() {
        final View target = mBoardHost.getBoardBackground();
        PropertyValuesHolder pvhsx = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f,1f);
        PropertyValuesHolder pvhsy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f,1f);
        final ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(target, pvhsx, pvhsy);
        anim.setDuration(300);
        anim.setStartDelay(500);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                target.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // if this is first time of training mode, show dialog.
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(TrainingActivity.this);
                if(!pref.getBoolean(FLG_HAS_PLAYED_TRAINING, false)) {
                    startTutorial();
                    pref.edit().putBoolean(FLG_HAS_PLAYED_TRAINING, true).apply();
                    startDemoQuestion();
                } else {
                    startNextQuestion(0);
                }
            }
        });
        anim.start();
    }

    private void startDemoQuestion() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurrentQIndex = INDEX_DEMO;
                current_question = getQuestion(mQHolder, mCurrentQIndex);
                Log.i("startNextQuestion", "Next Question ID : "+mCurrentQIndex);
                startQuestion(current_question);
            }
        }, 0);
    }
}