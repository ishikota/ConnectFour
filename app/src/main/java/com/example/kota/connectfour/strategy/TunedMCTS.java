package com.example.kota.connectfour.strategy;

import android.util.Log;
import android.util.Pair;

import com.example.kota.connectfour.ui.Board;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kota on 2015/03/31.
 * Improved MCTS strategy. Implements these two features.
 * 1. Prune lose move before start MCTS search.
 * 2. Use strong heuristic in play out.
 */
public class TunedMCTS extends BaseMCTS{
    private static final String TAG = TunedMCTS.class.getSimpleName();
    private final PruneStrategy mPruneStrategy;

    public TunedMCTS(int me, int oppo) {
        super(me, oppo);
        // init Prune strategy
        mPruneStrategy = new PruneStrategy(me, oppo);
        mPruneStrategy.setParameter(IterativeDeepening.MODE_TIME_LIMIT, 0.5);
        // init strategy which is used in play out
        mySimulationStrategy.userStrongHeuristic(true);
        oppoSimulationStrategy.userStrongHeuristic(true);
    }

    /**
     * set computational budget of MCTS search
     * @param mode : pass TIME_LIMIT or SIMULATION_NUM_LIMIT (these constants are defined above)
     * @param budget : the number of computational budget. if you use TIME_LIMIT then used unit is second.
     */
    public void setParameter(int mode, int budget, double prune_time_limit) {
        super.setParameter(mode, budget);
        mPruneStrategy.setParameter(IterativeDeepening.MODE_TIME_LIMIT, prune_time_limit);
    }

    /**
     * grow search tree and choose best root child with UCT algorithm.
     * @param origin_board : current board
     * @return best move(column)
     */
    @Override
    protected int UCTSearch(Board origin_board) {
        // create root node v_0 with state s_0
        Tree v_0 = new Tree(origin_board.COLUMN);
        v_0.parent = null;

        // if left move is less than
        int[] best_move = pruneLoseMove(origin_board, v_0);
        if(best_move!=null) return best_move[0];

        // iterate MCTS search within computational budget
        int play_counter = 0;
        long st = System.currentTimeMillis(); // start time
        while (!super.checkIfReachBudget(st, play_counter)) {
            Board cp_board = origin_board.copyBoard();
            Pair<Tree, Integer> tmp = treePolicy(v_0, cp_board);// leaf node and next player
            double delta = defaultPolicy(tmp.first, cp_board, tmp.second);
            backPropagation(tmp.first, delta, tmp.second);
            play_counter += 1;
        }
        Log.i(TAG, String.format("PLAYOUT NUM : %d",play_counter));
        // choose best child of root node
        return bestChild(v_0, 0);
    }

    /**
     * @param board : board instance
     * @param v_0   : root node of tree
     * @return if do not need MCTS search then return best_move else return null
     *         after pruned lose move.
     */
    private int[] pruneLoseMove(Board board, Tree v_0) {
        ArrayList<Integer> result = mPruneStrategy.myMakeANextMove(board);
        int best_score = result.get(result.size()-1);
        result.remove(result.size()-1); // remove best score

        // do not need MCTS search
        if(best_score!=0) {
            int s = result.size();
            int[] best_move = new int[s];
            for(int i=0;i<s;i++) best_move[i] = result.get(i);
            return best_move;
        }

        // remove moves which is negative score when best score is 0.
        for(int col=0;col<board.COLUMN;col++) {
            if(board.getPosition()[col] == board.ROW) continue;
            // if col is not contained in best move, then do not need to search this move.
            if(!result.contains(col)) {
                v_0.unvisited_child_num -=1;
                Tree v_child = new Tree(v_0.children.length);
                v_child.parent = v_0;
                v_child.is_terminal = true;
                v_child.value = -10;
                v_child.update_num = 1;
                v_0.children[col] = v_child;
            }
        }

        return null;
    }

    /**
     * This Strategy is almost same to IterativeDeepening.
     * Only deference is return type of each method.
     * This strategy will be used to prune lose move before MCTS search.
     */
    private class PruneStrategy extends IterativeDeepening {
        double mPruneLimit = 0.5;

        public PruneStrategy(int me, int oppo) {
            super(me, oppo);
        }

        @Override
        public int think(Board board) {
            throw new IllegalAccessError(
                    "Do not call think method in PruneStrategy. Use myMakeANextMove method instead.");
        }

        public void setParameter(int mode, double limit) {
            MODE = mode;
            mPruneLimit = limit;
        }

        public ArrayList<Integer> myMakeANextMove(Board board) {
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
                    scores[c] = super.evalMove(current_depth, 0, copy, ME, r, c, alpha, beta);
                    table[r][c] = Board.NONE;
                    position[c]--;

                    if(scores[c]!=0) solved.add(c);
                }

                // see above comment to know why condition 'current_depth==20'is needed
                if (solved.size()==board.COLUMN || current_depth == 20) break;
                // update info for next search iteration
                current_depth+=1;
            }
            Log.i(TAG, "Prune DEPTH WAS " + current_depth);
            if(D) Log.i(TAG, "SCORES"+ Arrays.toString(scores));
            Log.i(TAG, String.format("prune strategy took time : %f(s)", ((System.currentTimeMillis()-st*1.0)/1000)));
            return mySelectBestMove(board, position, scores);
        }

        /**
         * check if search process reached to defined computational budget.
         * @param start_time  : pass search-started time if MODE_TIME_LIMIT
         * @param current_depth : pass current search depth if MODE_DEPTH_LIMIT
         * @return result : return true if it reaches computational budget
         */
        @Override
        protected boolean checkIfReachBudget(long start_time, int current_depth) {
            double budget;
            if (MODE == MODE_TIME_LIMIT) {
                long ct = System.currentTimeMillis();
                budget = (ct - start_time)*1.0 / 1000;
            } else {
                budget = current_depth;
            }

            // update progress bar
            if(mThinkingProgressListener!=null) {
                int progress = (int)(100.0*budget/LIMIT);
                //Log.i(TAG, "progress:"+progress);
                mThinkingProgressListener.publishProgress(progress);
            }
            return budget>=mPruneLimit;
        }

        /**
         * @param scores : score of each column
         * @return column and score of best moves are packed in this ArrayList.
         *         so the number of elements in this array is number of best_move+1.
         *         Like this,
         *              (column1, column2, ..., score)
         */
        private ArrayList<Integer> mySelectBestMove(Board board, int[] position, int[] scores) {
            int best_score = 0;
            boolean is_first = true;
            ArrayList<Integer> best_move_info = new ArrayList<Integer>(board.COLUMN+1);
            for(int c=0;c<board.COLUMN;c++) {
                if(position[c]==board.ROW) continue;
                if(is_first || scores[c]>best_score) {
                    is_first = false;
                    best_move_info.clear();
                    best_move_info.add(c);
                    best_score = scores[c];
                } else if(scores[c] == best_score) {
                    best_move_info.add(c);
                }
            }
            best_move_info.add(best_score);
            return best_move_info;
        }

    }
}
