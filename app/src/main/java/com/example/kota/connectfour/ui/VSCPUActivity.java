package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnticipateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.example.kota.connectfour.R;
import com.example.kota.connectfour.strategy.BaseMCTS;
import com.example.kota.connectfour.strategy.BaseStrategy;
import com.example.kota.connectfour.strategy.IterativeDeepening;
import com.example.kota.connectfour.strategy.TunedMCTS;

/**
 * Created by kota on 2014/12/20.
 * This activity plays User vs CPU game.
 * You pass CPU settings like what strategy to use in Intent
 * with proper key defined in this class.
 *
 */
public class VSCPUActivity extends FragmentActivity {
    public static final String EXTRA_FIRST_PLAYER_STRENGTH = "first_player_strength";
    public static final String EXTRA_SECOND_PLAYER_STRENGTH = "second_player_strength";
    public static final String EXTRA_FIRST_PLAYER_NAME = "first_player_name";
    public static final String EXTRA_SECOND_PLAYER_NAME = "second_player_name";
    //private static final String TAG = VSCPUActivity.class.getSimpleName();
    public static final String EXTRA_HAS_DEMO_DONE = "has_demo_done";
    private boolean is_demo = false;

    private BoardHostForVS mBoardHost;
    private BaseStrategy mFirstStrategy, mSecondStrategy;
    private ProgressBar mProgressBar;
    String temp = "";
    private Board.OnUpdateListener mUpdateListener = new Board.OnUpdateListener() {
        @Override
        public void onUpdated(int who, Pair<Integer, Integer> pos, boolean if_win) {
            temp += pos.second+",";
            Board board = mBoardHost.getBoard();
            // if this game has finished, close the access to the board.
            if(if_win|| board.checkIfDraw()) {
                Log.i("TEST", temp);
                board.changeGameState(false);
                board.acceptInput(false);
                // show dialog if this game is demo
                if(is_demo) {
                    showLastTutorial(who==Board.FIRST_PLAYER);
                } else {
                    // if user lose the game(or draw), then recommend training mode.
                    BaseStrategy lose_player = who == Board.FIRST_PLAYER ? mSecondStrategy : mFirstStrategy;
                    if(lose_player==null || board.checkIfDraw()) {
                        PreferenceManager.getDefaultSharedPreferences(VSCPUActivity.this)
                                .edit().putBoolean(TrainingActivity.FLG_TRAINING_TIMING, true).apply();
                    }
                }
            } else {
                // if the player who has updated is first player, then next player is second player
                if (who == Board.FIRST_PLAYER) {
                    mBoardHost.changePlayer(Board.SECOND_PLAYER);
                    // if mSecondStrategy is null, this means second player is human player. so just wait his next move.
                    if (mSecondStrategy == null) {
                        board.acceptInput(true);
                    } else {
                        Util.getInstance().startThinking(mSecondStrategy, board, mProgressBar);
                    }
                } else {
                    mBoardHost.changePlayer(Board.FIRST_PLAYER);
                    // if mFirstStrategy is null, this means fist player is human player. so just wait his next move.
                    if(mFirstStrategy==null) {
                        board.acceptInput(true);
                    } else {
                        Util.getInstance().startThinking(mFirstStrategy, board, mProgressBar);
                    }
                }
            }
        }
    };


    // fixme : when thinking task continues long time, CPU thinkingTask should stop thinking and return some replacement.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein_transition, R.anim.fadeout_transition);
        // prevents screen from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.vs);

        // TODO retrieve game information passed in Intent.
        int connect_k=4;int width=7; int height=6;
        String first_player_name = getIntent().getStringExtra(EXTRA_FIRST_PLAYER_NAME);
        String second_player_name = getIntent().getStringExtra(EXTRA_SECOND_PLAYER_NAME);
        int first_player_strength = getIntent().getIntExtra(EXTRA_FIRST_PLAYER_STRENGTH, 8);
        int second_player_strength = getIntent().getIntExtra(EXTRA_SECOND_PLAYER_STRENGTH, 8);
        boolean is_hard = first_player_strength>8 || second_player_strength>8;
        is_demo = !PreferenceManager.getDefaultSharedPreferences(VSCPUActivity.this)
                .getBoolean(EXTRA_HAS_DEMO_DONE, false);

        // init layout
        mBoardHost = new BoardHostForVS(this,
                connect_k, width, height, first_player_name, second_player_name, is_hard);
        mBoardHost.getBoardBackground().setVisibility(View.INVISIBLE);
        mBoardHost.getBoard().setOnUpdateListener(mUpdateListener);
        ((FrameLayout)findViewById(R.id.board_container)).addView(mBoardHost);
        mBoardHost.changePlayer(Board.FIRST_PLAYER);
        mProgressBar = (ProgressBar)findViewById(R.id.progress);
        mProgressBar.setIndeterminate(true);

        // init strategy (if strength == 8 -> this player is human player.)
        mFirstStrategy = initStrategy(Board.FIRST_PLAYER, first_player_strength);
        mSecondStrategy = initStrategy(Board.SECOND_PLAYER, second_player_strength);
        //Toast.makeText(this, first_player_name+" VS "+second_player_name, Toast.LENGTH_SHORT).show();

        // switch theme color if this is hard mode.
        if(is_hard) {
            findViewById(R.id.root_view).setBackgroundColor(Color.parseColor("#212121"));
            getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#212121")));
        }

        startBoardAppearAnim();
    }

    private void showFirstTutorial() {
        MaterialAlertDialog.newInstance(
                getResources().getString(R.string.do_you_know_connect_four),
                getResources().getString(R.string.i_know),
                getResources().getString(R.string.i_dont),
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        showThirdTutorial();
                    }
                },
                new MaterialAlertDialog.onButtonClickListener() {
                    @Override
                    public void onClicked() {
                        showSecondTutorial();
                    }
                }
        ).show(getSupportFragmentManager(), "tutorial");
    }

    private void showSecondTutorial() {
        HowToPlayDialogFragment.newInstance(HowToPlayDialogFragment.FLG_DEMO_RULE,
                new HowToPlayDialogFragment.OnButtonClickListener() {
                    @Override
                    public void onClicked() {
                        // show second tutorial dialog
                        HowToPlayDialogFragment.newInstance(
                                HowToPlayDialogFragment.FLG_DEMO_HOW_TO_WIN,
                                new HowToPlayDialogFragment.OnButtonClickListener() {
                                    @Override
                                    public void onClicked() {
                                        showThirdTutorial();
                                    }
                                })
                                .show(getSupportFragmentManager(), "tutorial");
                    }
                })
                .show(getSupportFragmentManager(), "tutorial");
    }

    private void showThirdTutorial() {
        MaterialAlertDialog.newInstance(
                getResources().getString(R.string.how_to_make_a_move),
                getResources().getString(R.string.ok),
                "",
                null,
                null
        ).show(getSupportFragmentManager(), "tutorial");
    }

    private void showLastTutorial(boolean if_win) {
        String title;
        String message;
        String positive_text;
        MaterialAlertDialog.onButtonClickListener listener;

        if(if_win) {
            title = getResources().getString(R.string.message_win_title);
            message = getResources().getString(R.string.message_win_message);
            positive_text = getResources().getString(R.string.yes);
            listener = new MaterialAlertDialog.onButtonClickListener() {
                @Override
                public void onClicked() {
                    onBackPressed();
                }
            };
        } else {
            title = getResources().getString(R.string.message_lose_title);
            message = getResources().getString(R.string.message_lose_message);
            positive_text = getResources().getString(R.string.yes);
            listener = new MaterialAlertDialog.onButtonClickListener() {
                @Override
                public void onClicked() {
                    resetGame();
                }
            };
        }

        MaterialAlertDialog.newInstance(
                title,
                message,
                positive_text,
                "",
                listener,
                null
        ).show(getSupportFragmentManager(), "tutorial");
    }

    /**
     * After animation, if first player is CPU then start thinking.
     */
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
                // if first player is CPU then start thinking.
                if(mFirstStrategy!=null) {
                    mBoardHost.getBoard().acceptInput(false);
                    Util.getInstance().startThinking(mFirstStrategy, mBoardHost.getBoard(), mProgressBar);
                }

                if(is_demo) {
                    showFirstTutorial();
                    // show demo dialog only first time
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences(VSCPUActivity.this).edit();
                    editor.putBoolean(EXTRA_HAS_DEMO_DONE, true);
                    editor.apply();
                }
            }
        });
        anim.start();
    }

    /**
     * create specified strength Strategy for player.
     * This method will return null if level is not expected one.
     * @param player     : player who use this Strategy
     * @param level      : strength of this Strategy
     * @return proper strength Strategy
     */
    private BaseStrategy initStrategy(int player, int level) {
        int opponent = player == Board.FIRST_PLAYER ? Board.SECOND_PLAYER : Board.FIRST_PLAYER;
        int[] params = getStrategyParameter(level);
        if(params==null) return null;

        BaseStrategy strategy;
        if(level<8) {
            strategy = new IterativeDeepening(player, opponent);
            ((IterativeDeepening)strategy).setParameter(params[0], params[1]);
            strategy.useHeuristic(params[2] != 0);
        } else if (level<12){
            strategy = new BaseMCTS(player, opponent);
            ((BaseMCTS)strategy).setParameter(params[0], params[1]);
        } else {
            strategy = new TunedMCTS(player, opponent);
            ((TunedMCTS)strategy).setParameter(params[0], params[1]);
        }

        return strategy;
    }

    /**
     * return proper parameter of Strategy for specified level.
     * This method will return null if passed level is unexpected one.
     * CPU LEVEL -> Strength
     *  1 -> Read Depth 2
     *  2 -> Read Depth 3
     *  3 -> Think 2 seconds without heuristic
     *  4 -> Read Depth 4 with heuristic
     *  5 -> Read Depth 6 with heuristic
     *  6 -> Think 2 seconds with heuristic
     * @param level : the level of strategy you need
     * @return parameter of Strategy
     */
    private int[] getStrategyParameter(int level) {
        switch (level) {
            case 1: // Easy
                return new int[]{IterativeDeepening.MODE_DEPTH_LIMIT, 2, 0};
            case 2: // Medium
                return new int[]{IterativeDeepening.MODE_DEPTH_LIMIT, 3, 0};
            case 3: // Hard
                return new int[]{IterativeDeepening.MODE_TIME_LIMIT, 1, 0};
            case 4: // Expert
                return new int[]{IterativeDeepening.MODE_DEPTH_LIMIT, 4, 1};
            case 5: // Pro
                return new int[]{IterativeDeepening.MODE_DEPTH_LIMIT, 7, 1};
            case 6: // Master
                return new int[]{IterativeDeepening.MODE_TIME_LIMIT, 2, 1};
            case 9: // Hard1
                return new int[]{BaseMCTS.SIMULATION_NUM_LIMIT, 100};
            case 10: // Hard2
                return new int[]{BaseMCTS.SIMULATION_NUM_LIMIT, 2000};
            case 11: // Hard3
                return new int[]{BaseMCTS.TIME_LIMIT, 3};
            case 12: // Hard4
                return new int[]{BaseMCTS.SIMULATION_NUM_LIMIT, 100};
            case 13: // Hard5
                return new int[]{BaseMCTS.TIME_LIMIT, 3};
            case 14: // Hard6
                return new int[]{BaseMCTS.TIME_LIMIT, 5};
            default:
                return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_list_single; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vs, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }

    @Override
    public void onBackPressed() {
        startBoardDisappearAnim();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // TODO home button is not displayed now
            case android.R.id.home:
                startBoardDisappearAnim();
                break;
            case R.id.reset:
                resetGame();
                break;
            case R.id.tutorial:
                showSecondTutorial();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetGame() {
        mBoardHost.getBoard().refresh(new Board.BoardAnimListener() {
            @Override
            public void onAnimationFinished() {
                mBoardHost.getBoard().changeGameState(true);
                mBoardHost.changePlayer(Board.FIRST_PLAYER);
                // if first player is CPU then start thinking.
                if(mFirstStrategy!=null) {
                    mBoardHost.getBoard().acceptInput(false);
                    Util.getInstance().startThinking(mFirstStrategy, mBoardHost.getBoard(), mProgressBar);
                }
            }
        });
    }

    /**
     * this method is called when user pressed back button.
     * finish this Activity with animation.
     */
    private void startBoardDisappearAnim() {
        PropertyValuesHolder pvhsx = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f,0f);
        PropertyValuesHolder pvhsy = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f,0f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(mBoardHost.getBoardBackground(), pvhsx, pvhsy);
        ObjectAnimator anim2 = ObjectAnimator.ofPropertyValuesHolder(mBoardHost.getBoard(), pvhsx, pvhsy);
        anim.setDuration(300);
        anim2.setDuration(300);
        anim.setInterpolator(new AnticipateInterpolator());
        anim2.setInterpolator(new AnticipateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setResult(123);
                finish();
                overridePendingTransition(R.anim.fadein_transition, R.anim.fadeout_transition);
            }
        });
        anim.start();
        anim2.start();
    }

}
