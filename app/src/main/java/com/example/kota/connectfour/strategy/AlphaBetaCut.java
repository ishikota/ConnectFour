package com.example.kota.connectfour.strategy;

import android.util.Log;

import com.example.kota.connectfour.ui.Board;

import java.util.ArrayList;

/**
 * Created by kota on 2015/01/08.
 * AlphaBeta cut is improved algorithm of basic MiniMax
 * in its calculation speed.
 */
public class AlphaBetaCut extends BaseStrategy {
    private static final String TAG = MiniMax.class.getSimpleName();
    //public static final String EXTRA_MAX_DEPTH = "max_depth";

    // max depth of recursive function.
    private int MAX_DEPTH = 3;

    public AlphaBetaCut(int me, int oppo) {
        super(me, oppo);
    }

    /**
     * set max-depth of recursive in move evaluation method.
     * sample execution time on GalaxyS3
     *   depth = 1 : 0.004  (s)
     *   depth = 2 : 0.01   (s)
     *   depth = 3 : 0.026  (s)
     *   depth = 4 : 0.096  (s)
     *   depth = 5 : 0.23   (s)
     *   depth = 6 : 0.658  (s)
     *   depth = 7 : 2.037  (s)
     *   depth = 8 : 6.037  (s)
     *   depth = 9 : 19.407 (s)
     *
     * @param max_depth : max depth of recursive.
     */
    public void setParameter(int max_depth) {
        this.MAX_DEPTH = max_depth;
    }

    /**
     * Calculate the score of next possible moves.
     * Main calculation process is done in evalMove method.
     * @param board : Board instance which represents current board state.
     * @return best_moves : array of column number of best next move.
     */
    @Override
    public int[] makeANextMove(Board board) {
        // change game state not to do animation.
        board.acceptInput(false);

        int best_score = 0;
        boolean is_first = true;
        int scores[] = new int[board.COLUMN];
        int[] position = board.getPosition();
        int[][] table = board.getTable();

        for(int c=0;c<board.COLUMN;c++) {
            int r = position[c];
            if(r==board.ROW) continue;

            table[r][c] = ME;
            position[c]++;
            scores[c] = evalMove(0, board, ME, r, c, -100, 100);
            table[r][c] = Board.NONE;
            position[c]--;

            if(is_first || scores[c]>best_score) {
                is_first = false;
                best_score = scores[c];
            }
            if(this.D) Log.i(TAG, "score("+c+") = "+scores[c]);
        }

        ArrayList<Integer> temp = new ArrayList<Integer>();
        for(int c=0;c<board.COLUMN;c++) {
            if(scores[c]==best_score && position[c]!=board.ROW) {
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

    private int evalMove(int depth, Board board,
                         int player, int row, int col, int alpha, int beta) {

        if(board.checkIfWin(player,row,col,false)) {
            if (depth%2==0)
                return board.ROW*board.COLUMN-depth;
            else
                return -(board.ROW*board.COLUMN-depth);
        }
        if(depth==MAX_DEPTH) return 0;
        if(depth>MAX_DEPTH)
            throw new StackOverflowError("evalmove recursive depth becomes larger than 100");

        int next_player = player == ME ? OPPO : ME;
        int best_score = 0;
        boolean is_first = true;
        int[] position = board.getPosition();
        int[][] table = board.getTable();

        for(int c=0;c<board.COLUMN;c++) {
            int r = position[c];
            if(r==board.ROW) continue;

            table[r][c] = next_player;
            position[c]++;
            int temp_score = evalMove(depth+1, board, next_player, r, c, alpha, beta);
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
