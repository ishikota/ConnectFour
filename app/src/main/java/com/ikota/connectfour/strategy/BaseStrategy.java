package com.ikota.connectfour.strategy;

import android.util.Log;

import com.ikota.connectfour.ui.Board;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by kota on 2014/12/19.
 * This is the parent class of all strategy class.
 * Concrete strategy class extends this class and override
 * the method "makeANextMove(Board board)".
 */
public abstract class BaseStrategy {
    private static final String TAG = BaseStrategy.class.getSimpleName();

    /**
     * Concrete class can publish their thinking progress
     * to the ProgressBar which is displayed in screen.
     * In concrete strategy class, do not forget to do null-check
     * before use this interface.
     */
    public interface ThinkingProgressListener {
        void publishProgress(int progress);
    }
    ThinkingProgressListener mThinkingProgressListener;

    protected boolean D = false;
    // constant which represents each player
    public final int ME;
    public final int OPPO;
    private final Heuristic HEURISTIC;
    private boolean use_heuristic = false;
    private boolean use_strong_heuristic = false;
    // use in randomChoice method
    protected final Random mRandom;

    BaseStrategy(int me, int oppo){
        ME = me;
        OPPO = oppo;
        HEURISTIC = new Heuristic(me, oppo);
        mRandom = new Random();
    }

    /**
     * This method works as main thinking process.
     * Concrete class implements this method by using their algorithm and
     * return next best moves in current board state.
     *
     * @param board : Board instance which represents current board state.
     * @return best_moves : column numbers which this strategy think best for next move.
     */
    abstract public int[] makeANextMove(Board board);

    /**
     * if you have parameters which should be set in main process
     * like MAX-DEPTH, override this method and main process use
     * this method to set some parameter.
     */
    //public void setParameter(){}

    public void setProgressListener(ThinkingProgressListener listener) {
        mThinkingProgressListener = listener;
    }

    /**
     * Return a next move (column) which is calculated in makeANextMove.
     * If makeANextMove returns multiple best moves then uniformly choose
     * next move.
     *
     * @param board   : Board instance which represents current board state.
     * @return column : column number to make a next move.
     */
    public int think(Board board) {
        int[] best_moves = makeANextMove(board);
        if(D) Log.i(TAG, "Best moves : "+ Arrays.toString(best_moves));

        if(use_strong_heuristic) {
            best_moves = HEURISTIC.choiceByStrongHeuristic(board, best_moves);
        } else if (use_heuristic) {
            best_moves = HEURISTIC.choiceByHeuristic(board, best_moves);
            if(D) Log.i(TAG, "Best Heuristic moves : "+Arrays.toString(best_moves));
        }

        return randomChoice(board, best_moves);
    }

    /**
     * Choice next move (column) uniformly from best_moves.
     * Do not choose invalid column.(already full-stacked)
     * @param board      : Board instance which represents current board state.
     * @param best_moves : the column numbers to uniformly select from.
     * @return col    : column number which is chosen from best_moves.
     */
    protected int randomChoice(Board board, int[] best_moves) {
        if(best_moves.length==0) throw new IllegalArgumentException();

        int len = best_moves.length, tmp;
        // shuffle array by swapping
        for(int i=len-1; i>0; i--) {
            int index = mRandom.nextInt(best_moves.length);
            tmp = best_moves[i];
            best_moves[i] = best_moves[index];
            best_moves[index] = tmp;
        }

        // choose valid move by traversing shuffled best_moves
        int[] position = board.getPosition();
        for (int col : best_moves) {
            if(position[col]!=board.ROW) return col;
        }

        throw new IllegalStateException("All best_moves are full-stacked.");
    }

    public void useHeuristic(boolean state) {
        use_heuristic = state;
    }

    public void userStrongHeuristic(boolean state) {
        use_strong_heuristic = state;
    }

//    public void setHeuristicParameter(int w1, int w2, int w3, int w4) {
//        HEURISTIC.setParameter(w1, w2, w3, w4);
//    }

}
