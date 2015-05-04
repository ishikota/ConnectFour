package com.example.kota.connectfour.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kota on 2014/12/16.
 * This class represents board of ConnectFour.
 * Game information like table info or game result are
 * stored and managed in this class.
 * Useful methods are
 * - update
 * - refresh
 * - checkIfWin/checkIfDraw
 */
//TODO make this class available not only from runtime but layout xml.(so add constructor)
public class Board extends LinearLayout {
    private static final String TAG = Board.class.getSimpleName();

    // game parameter
    public final int COLUMN;
    public final int ROW;
    public final int CONNECT_K;
    // constant which represents each player
    // blank space is represented by 0.
    public static final int FIRST_PLAYER = 1;
    public static final int SECOND_PLAYER = -1;
    public static final int NONE = 0;
    private int next_player = FIRST_PLAYER;
    // save game state in this 2D array
    private int[][] mTable;
    // hold next row to put a move of each column.
    private int[] mPosition;

    private final Context mContext;
    // width and height of this board in the display(px).
    private int PARENT_HEIGHT;
    // hold LinearLayout which represents each column in UI.
    private final LinearLayout[] mColumns;

    private final boolean IS_HARD_MODE;
    // if this board is accepting user input(make a new move) or not.
    private boolean is_accepting = true;
    // if game is in progress or not.
    private boolean is_playing = true;
    // current game id on this board
    private AtomicInteger game_id = new AtomicInteger();

    private UIHelper mUIHelper;
    private OnUpdateListener mUpdateCallback;
    private ColumnTouchListener mColumnTouchListener;

    public interface OnUpdateListener {
        /**
         * notice board update to callback.
         * @param who : player who made a move in this update.
         * @param pos : pair of row and column number which is updated.
         * @param if_win : if did this game finish in this update.
         */
        void onUpdated(int who, Pair<Integer,Integer> pos, boolean if_win);
    }
    public void setOnUpdateListener(OnUpdateListener listener){
        mUpdateCallback = listener;
    }

    public interface BoardAnimListener {
        void onAnimationFinished();
    }

    /**
     * pass column touch event to BoardHost class.
     * BoardHost class use passed information to animate background.
     */
    public interface ColumnTouchListener {
        void onTouch(int column);
    }
    public void setColumnTouchListener(ColumnTouchListener listener) {mColumnTouchListener = listener;}

    // This method is called from strategy classes.
    public int[] getPosition() {return mPosition;}
    public int[][] getTable() {return mTable;}
    public AtomicInteger getGameId() {return game_id;}

    public synchronized void acceptInput(boolean state) {is_accepting = state;}
    public synchronized boolean checkIfAccepting() {return is_accepting;}
    public synchronized void changeGameState(boolean state) {is_playing = state;}
    //public boolean checkGameState() {return is_playing;}

    /**
     * Define basic game information for this board.
     * You cannot modify these basic 3 information once you defined.
     *
     * @param context : the context of this board.
     * @param k       : k defines how many moves do you need to win.
     * @param column  : the number of column in this board.
     * @param row     : the number of row in this board.
     */
    public Board(Context context, int k, int column, int row, boolean is_hard) {
        super(context);
        mContext = context;
        CONNECT_K = k;
        COLUMN = column;
        ROW = row;
        mTable = new int[ROW][COLUMN];
        mPosition = new int[COLUMN];
        mColumns = new LinearLayout[COLUMN];
        mUIHelper = new UIHelper();
        IS_HARD_MODE = is_hard;
        initColumns();
    }

    /**
     * Constructor for copyBoard method
     * @param context : the context of this board.
     * @param board : the board instance to copy from.
     */
    private Board(Context context, Board board) {
        super(context);
        mContext = context;
        CONNECT_K = board.CONNECT_K;
        COLUMN = board.COLUMN;
        ROW = board.ROW;
        mTable = new int[ROW][COLUMN];
        mPosition = new int[COLUMN];
        mColumns = new LinearLayout[COLUMN];
        IS_HARD_MODE = false;
    }

    private void initColumns() {
        // initialize LinearLayout which represents each columns.
        for(int i=0;i<COLUMN;i++) {
            LinearLayout.LayoutParams params =
                    new LayoutParams(0, LayoutParams.MATCH_PARENT);
            params.weight = 1;
            LinearLayout column = new LinearLayout(mContext);
            column.setGravity(Gravity.BOTTOM);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setLayoutParams(params);
            mColumns[i] = column;
            mColumns[i].setTag(i);
            mColumns[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!is_accepting) return;
                    Integer col = (Integer)v.getTag();
                    update(next_player, col);
                }
            });
            final int pos = i;
            mColumns[i].setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // pass touch event to BoardHost
                    if(event.getAction()==MotionEvent.ACTION_DOWN) {
                        if(mColumnTouchListener!=null)
                            mColumnTouchListener.onTouch(pos);
                    }
                    return false;
                }
            });
            this.addView(mColumns[i]);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int PARENT_WIDTH = MeasureSpec.getSize(widthMeasureSpec);
        PARENT_HEIGHT = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(PARENT_WIDTH, PARENT_HEIGHT);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * TODO copy board initialize UI component. its waste of time.
     * create new board object which has same board state.(size, table and position).(deep copy)
     * copied board does not have UI element, so do not touch its UI.
     * Be careful not to call update method which starts animation
     * instead you use updateLogically method which do not animate view.
     * @return copied board which has same information of this Board instance.
     */
    public Board copyBoard() {
        Board copy = new Board(mContext, this);
        for(int r=0; r<ROW; r++) {
            for(int c=0; c<COLUMN; c++) {
                copy.getTable()[r][c] = mTable[r][c];
            }
        }
        for(int c=0;c<COLUMN;c++) {
            copy.getPosition()[c] = mPosition[c];
        }
        return copy;
    }

    /**
     * update board without animation(touching UI elements).
     * This is used in MCTS search process.
     * @param player : the player to make this move.
     * @param col    : column which player make a move.
     */
//    public void updateLogically(int player, int col) {
//        int row = mPosition[col]++;
//        mTable[row][col] = player;
//    }

    /**
     * first, update the board state by putting a new move.
     * next, checks if this moves connected-K or not.
     * Finally add moves on UI by calling UIHelper.addMove(...).
     * During animation, board should refuse input. so Before animation starts,
     * prohibit to access the board.
     * !! Do not forget re-accept input after animation if you need !!
     * @param player : the player to make this move.
     * @param col    : column which player make a move.
     */
    public void update(final int player, final int col) {
        if(!is_playing) return;
        // if the move is illegal one, show notification.
        if(col>=COLUMN || mPosition[col]==ROW) {
            Toast.makeText(mContext, "You cannot put column "+col, Toast.LENGTH_SHORT).show();
            return;
        }
        // below code prevents user's two successive moves which is caused by fast two taps.
        acceptInput(false);
        final int row = mPosition[col]++;
        mTable[row][col] = player;
        // pass listener which is called after move-falling animation ends.
        mUIHelper.addMove(player, col, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                boolean if_win = checkIfWin(player, row, col, true);
                if(mUpdateCallback!=null)
                    mUpdateCallback.onUpdated(player, new Pair<Integer,Integer>(row,col),if_win );
            }
        });
        changePlayer();
    }

    /**
     * Check if the player wins the game of not.
     * This method is called every time player put the move
     * on table[row][col].
     * @param player : the player to make this move.
     * @param row    : row which player make a move.
     * @param col    : column which player make a move.
     * @param do_anim: if do animation on connected moves.
     * @return       : if this player wins or not.
     */
    public boolean checkIfWin(int player, int row, int col, boolean do_anim) {
        int[] line_nums = {1,1,1,1};
        int[] di = {1,-1,0,0,-1,1,-1};
        int[] dj = {1,-1,1,-1,1,-1,0};
        // move_pos holds the position(Pair of row and col) of connected moves.
        ArrayList<Pair<Integer,Integer>> move_pos = new ArrayList<Pair<Integer,Integer>>();
        move_pos.add(new Pair<Integer, Integer>(row, col));

        // check 7 direction if each line connected-K.
        // do not need to check upper direction.
        for(int d=0;d<7;d++) {
            int ni = row; int nj = col;
            for(int k=0;k<CONNECT_K-1;k++) {
                ni += di[d];
                nj += dj[d];
                if(ni<0 || ROW<=ni || nj<0 || COLUMN<=nj ||
                        mTable[ni][nj] != player) break;
                move_pos.add(new Pair<Integer, Integer>(ni,nj));
                line_nums[d/2]++;
            }

            if (d%2==1 || d==6) {
                if (line_nums[d / 2] >= CONNECT_K) {
                    // When this method called in strategy calculation,
                    // do not animate.
                    if(do_anim) mUIHelper.animateMoves(move_pos);
                    return true;
                }
                move_pos = new ArrayList<Pair<Integer,Integer>>();
                move_pos.add(new Pair<Integer, Integer>(row, col));
            }
        }
        return false;
    }

    /**
     * If all squares are filled with moves and not finished the game,
     * then this game is draw.
     * @return : if this game finished in draw or not.
     */
    public boolean checkIfDraw() {
        for(int i=0;i<COLUMN;i++) {
            if(mPosition[i] != ROW) return false;
        }
        return true;
    }

    /*
        reset board state with animation.
     */
    public void refresh() {this.refresh(null);}
    public void refresh(BoardAnimListener callback) {
        // change game id to prevent old thinking task from updating this board.
        game_id.incrementAndGet();
        next_player = FIRST_PLAYER;
        mTable = new int[ROW][COLUMN];
        mUIHelper.refreshAnim(callback);
    }

    public void changePlayer() {
        next_player = next_player == FIRST_PLAYER ? SECOND_PLAYER : FIRST_PLAYER;
    }

    public void setBoard(final int[][] goal_table, final int[] goal_position, final BoardAnimListener callback) {
        refresh(new BoardAnimListener() {
            @Override
            public void onAnimationFinished() {
                mUIHelper.initAnim(goal_table, goal_position, callback);
            }
        });
    }

    /**
     *  This class holds methods which related to
     *  UI components like animation.
     *  Board class do animation through this class.
     */
    private class UIHelper {

        /**
         * Add a new move in display with animation.
         * This method is called from a method update.
         * After animation, notice finish of update to main process.
         *
         * @param player : the player to make this move.
         * @param col : the column to make a move.
         *
         */
        public void addMove(final int player, final int col,Animator.AnimatorListener listener) {
            ImageView imageView = Util.getInstance().createMove(mContext, player, IS_HARD_MODE);
            mColumns[col].addView(imageView,0);

            // animation
            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    imageView, View.TRANSLATION_Y, -1000, 0);
            animator.setDuration(1000);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(listener);
            animator.start();
        }

        /**
         * Animate the moves passed in argument.
         * Use this method to animate connected K moves.
         * @param moves : list of row and column pair of moves to animate.
         */
        private void animateMoves(ArrayList<Pair<Integer,Integer>> moves) {
            for(Pair<Integer,Integer> pair : moves) {
                View view;
                int row = pair.first; int col = pair.second;
                try {
                    view = mColumns[col].getChildAt(mColumns[col].getChildCount() - 1 - row);
                    if(view==null) throw new Exception();
                } catch (Exception e) {
                    for(Pair<Integer,Integer> p:moves)
                        Log.e(TAG, p.toString());
                    Log.e(TAG, "r:"+row+"c:"+col);
                    e.printStackTrace();
                    return;
                }
                PropertyValuesHolder holderSX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 0.5f);
                PropertyValuesHolder holderSY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 0.5f);
                ObjectAnimator animator = ObjectAnimator.
                        ofPropertyValuesHolder(view, holderSX, holderSY);
                animator.setInterpolator(new OvershootInterpolator());
                animator.setDuration(300);
                animator.setRepeatCount(-1);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.start();
            }
        }

        /**
         * You must refresh this board before starting init animation. Otherwise Exception would occur.
         * @param goal_table    : goal state of table
         * @param goal_position : goal state of position
         * @param callback      : callback which called after init animation end
         */
        private void initAnim(final int[][] goal_table, final int[] goal_position, final BoardAnimListener callback) {
            is_accepting = false;
            int temp = 0;
            for(int num: goal_position) temp+= num;
            final int move_num = temp;

            final Random rand = new Random();
            final int END_FLG = 10000000;

            Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    if(message.what == END_FLG) {
                        mTable = goal_table.clone(); //todo shallow copy would cause bug
                        is_accepting = true;
                        if(callback!=null) callback.onAnimationFinished();
                        return;
                    }
                    if(message.what>=move_num) {
                        this.sendEmptyMessageDelayed(END_FLG, 1000);
                        return;
                    }
                    final int col = rand.nextInt(COLUMN);
                    if(mPosition[col]==goal_position[col]) {
                        this.sendEmptyMessageDelayed(message.what, 1);
                        return;
                    }
                    final int row = mPosition[col]++;
                    ImageView view = Util.getInstance().createMove(mContext, goal_table[row][col], IS_HARD_MODE);
                    mColumns[col].addView(view,0);

                    // animation
                    ObjectAnimator animator = ObjectAnimator.ofFloat(
                            view, View.TRANSLATION_Y, -1000, 0);
                    animator.setDuration(300);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.start();

                    this.sendEmptyMessageDelayed(message.what+1, 1);
                }
            };
            handler.sendEmptyMessageDelayed(0, 5);
        }

        private void refreshAnim(final BoardAnimListener callback) {
            is_accepting = false;
            int temp = 0;
            for(int num: mPosition) temp+= num;
            final int move_num = temp;

            final Random rand = new Random();
            final int END_FLG = 10000000;
            Handler handler = new Handler() {
                public void handleMessage(Message message) {
                    if(message.what == END_FLG) {
                        for(int i=0;i<COLUMN;i++)
                            mColumns[i].removeAllViews();
                        is_accepting = true;
                        if(callback!=null) callback.onAnimationFinished();
                        return;
                    }
                    if(message.what>=move_num) {
                        this.sendEmptyMessageDelayed(END_FLG, 1000);
                        return;
                    }
                    final int col = rand.nextInt(COLUMN);
                    if(mPosition[col]==0) {
                        this.sendEmptyMessageDelayed(message.what, 1);
                        return;
                    }
                    final int row = --mPosition[col];
                    ImageView view = (ImageView)mColumns[col].getChildAt(row);
                    //view.setImageResource(R.drawable.ic_launcher);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(
                            view, View.TRANSLATION_Y, 0, PARENT_HEIGHT);
                    animator.setDuration(300);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();

                    this.sendEmptyMessageDelayed(message.what+1, 1);
                }
            };
            handler.sendEmptyMessageDelayed(0, 5);
        }

    }
}
