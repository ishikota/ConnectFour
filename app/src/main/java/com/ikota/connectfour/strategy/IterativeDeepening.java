package com.ikota.connectfour.strategy;

import android.util.Log;

import com.ikota.connectfour.ui.Board;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kota on 2015/01/08.
 * IterativeDeepening is improved algorithm of AlphaBetaCut
 * in its calculation speed.
 * TODO this implementation doesn't do A*. So This IterativeDeepening would not improvement of AlphaBetaCut
 */
public class IterativeDeepening extends BaseStrategy {
    private static final String TAG = IterativeDeepening.class.getSimpleName();
    //public static final String EXTRA_MAX_DEPTH = "max_depth";
    public static final int MODE_DEPTH_LIMIT = 1;
    public static final int MODE_TIME_LIMIT = 3;

    // the type of computational budget(time or depth limit)
    protected int MODE;
    // computational budget of iteration
    protected int LIMIT = 3;

    public IterativeDeepening(int me, int oppo) {
        super(me, oppo);
    }

    /**
     *
     * Sample Execution Time on NEXUS 5
     * depth = 1 : 0.0(s)
     * depth = 2 : 0.002(s)
     * depth = 3 : 0.01(s)
     * depth = 4 : 0.032(s)
     * depth = 5 : 0.216(s)
     * depth = 6 : 0.561(s)
     * depth = 7 : 1.414(s)
     * depth = 8 : 5.673(s)
     * depth = 9 : 18.638(s)
     *
     * @param mode  : type of computational budget
     * @param limit : value of computational value. (unit:second)
     */
    public void setParameter(int mode, int limit) {
        this.MODE = mode;
        this.LIMIT = limit;
    }

    /**
     * Calculate the score of next possible moves.
     * Main calculation process is done in evalMove method.
     *
     * NOTE
     * we put loop break condition "current_depth == 20". because,
     * If all calculation has done, it should break search-loop.
     * But if it puts column 'n' and this leads the game draw,
     * then evalMove returns 0 so column 'n' is never added to solved
     * nevertheless it's solved.
     *
     * So when we use MODE_TIME_LIMIT, this problem prevents us to break
     * the search loop even if we got all of result. To avoid this,
     * we added that break condition.()
     *
     * @param board : Board instance which represents current board state.
     * @return best_moves : array of column number of best next move.
     */
    @Override
    public int[] makeANextMove(Board board) {
        // change game state not to do animation.
        board.acceptInput(false);

        int current_depth = 0;
        long st = System.currentTimeMillis(); // start time
        int alpha = -100; int beta = 100;
        ArrayList<Integer> solved = new ArrayList<Integer>();
        int scores[] = new int[board.COLUMN];
        // do not touch original Board variable during thinking task.
        Board copy = board.copyBoard();
        int[] position = copy.getPosition();
        int[][] table = copy.getTable();

        while(true) {
            // if it reaches computational budget, break search loop
            if (checkIfReachBudget(st, current_depth)) break;

            for (int c = 0; c < board.COLUMN; c++) {
                if (solved.contains(c)) continue;
                int r = position[c];
                if (r == board.ROW) {
                    scores[c] = -100;
                    solved.add(c);
                    continue;
                }

                table[r][c] = ME;
                position[c]++;
                scores[c] = evalMove(current_depth, 0, copy, ME, r, c, alpha, beta);
                table[r][c] = Board.NONE;
                position[c]--;

                if(scores[c]!=0) solved.add(c);
            }

            // see above comment to know why condition 'current_depth==20'is needed
            if (solved.size()==board.COLUMN || current_depth == 20) break;
            // update info for next search iteration
            current_depth+=1;
        }
        Log.i(TAG, "SEARCH DEPTH WAS "+current_depth);
        Log.i(TAG, "SCORES"+ Arrays.toString(scores));
        return selectBestMoves(board, position, scores);
    }

    /**
     * check if search process reached to defined computational budget.
     * @param start_time  : pass search-started time if MODE_TIME_LIMIT
     * @param current_depth : pass current search depth if MODE_DEPTH_LIMIT
     * @return result : return true if it reaches computational budget
     */
    protected boolean checkIfReachBudget(long start_time, int current_depth) {
        int budget;
        if (MODE == MODE_TIME_LIMIT) {
            long ct = System.currentTimeMillis();
            budget = (int) ((ct - start_time) / 1000.0);
        } else {
            budget = current_depth;
        }

        // update progress bar
        if(mThinkingProgressListener!=null) {
            int progress = (int)(100.0*budget/LIMIT);
            //Log.i(TAG, "progress:"+progress);
            mThinkingProgressListener.publishProgress(progress);
        }
        return budget>=LIMIT;
    }

    /**
     * @param scores : score of each column
     * @return array of columns which got highest score in search.
     */
    protected int[] selectBestMoves(Board board, int[] position, int[] scores) {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int best_score = 0;
        boolean is_first = true;
        for(int c=0;c<board.COLUMN;c++) {
            if(position[c]==board.ROW) continue;
            if(is_first || scores[c]>best_score) {
                is_first = false;
                best_score = scores[c];
                temp = new ArrayList<Integer>();
                temp.add(c);
            } else if(scores[c]==best_score) {
                temp.add(c);
            }
        }

        // push moves which gets highest score into this array and return it.
        int[] best_moves = new int[temp.size()];
        for(int i=0;i<best_moves.length;i++) best_moves[i]=temp.get(i);
        if(this.D && best_moves.length == 0)
            throw new IllegalStateException("no best_moves found in makeANextMove");
        return best_moves;
    }

    protected int evalMove(int depth_limit, int depth, Board board,
                         int player, int row, int col, int alpha, int beta) {

        if(board.checkIfWin(player,row,col,false)) {
            if (depth%2==0)
                return board.ROW*board.COLUMN-depth;
            else
                return -(board.ROW*board.COLUMN-depth);
        }
        if(depth==depth_limit) return 0;
        if(depth>depth_limit)
            throw new StackOverflowError("evalmove recursive depth becomes "+depth
                    +" which exceeds MAX_DEPTH "+depth_limit);

        int next_player = player == ME ? OPPO : ME;
        int best_score = 0;
        boolean is_first = true;
        // do not touch original Board variable during thinking task.
        Board copy = board.copyBoard();
        int[] position = copy.getPosition();
        int[][] table = copy.getTable();

        for(int c=0;c<board.COLUMN;c++) {
            int r = position[c];
            if(r==board.ROW) continue;

            table[r][c] = next_player;
            position[c]++;
            int temp_score = evalMove(depth_limit, depth+1, copy, next_player, r, c, alpha, beta);
            table[r][c] = Board.NONE;
            position[c]--;

            // do alpha beta cut here
            // alpha is lowest assured score of maximizer node
            // beta is highest assured score of minimizer node
            if(depth%2==0) {
                // if this node is minimizer node
                if(temp_score<=alpha) return temp_score;
                if(temp_score<beta) beta = temp_score;
            } else {
                // if this node is maximizer node
                if(temp_score>=beta) return temp_score;
                if(temp_score>alpha) alpha = temp_score;
            }

            if(is_first) {
                is_first = false;
                best_score = temp_score;
            } else if(depth%2==0) {
                best_score = Math.min(best_score, temp_score);
            } else {
                best_score = Math.max(best_score, temp_score);
            }
        }

        return best_score;
    }
}

