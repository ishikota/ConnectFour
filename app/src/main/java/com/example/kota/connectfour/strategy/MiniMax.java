package com.example.kota.connectfour.strategy;

import android.util.Log;

import com.example.kota.connectfour.ui.Board;

import java.util.ArrayList;

/**
 * Created by kota on 2014/12/20.
 * Basic MiniMax algorithm.
 */
public class MiniMax extends BaseStrategy {
    private static final String TAG = MiniMax.class.getSimpleName();

    // max depth of recursive function.
    private int MAX_DEPTH = 3;

    public MiniMax(int me, int oppo) {
        super(me, oppo);
    }

    /**
     * set max-depth of recursive in move evaluation method.
     * sample execution time on GalaxyS3
     *  depth = 1 : 0.013  (s)
     *  depth = 2 : 0.125  (s)
     *  depth = 3 : 0.297  (s)
     *  depth = 4 : 0.814  (s)
     *  depth = 5 : 4.048  (s)
     *  depth = 6 : 49.394 (s)
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
            scores[c] = evalMove(0, board, ME, r, c);
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

    /**
     * Calculate the score of passed move.
     * Higher score is better for FIRST_PLAYER.
     * Shallow depth result gets high weighted score.
     * ex) win in next move is better than win in 2-move later.
     * @param depth  : current recursive depth.
     * @param board  : Board instance which represents current game state.
     * @param player : next player to make a move.
     * @param row    : the row of move to evaluate.
     * @param col    : the column of move to evaluate .
     * @return score : the score of move on (row, col).
     */
    private int evalMove(int depth, Board board, int player, int row, int col) {

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
            int temp_score = evalMove(depth+1, board, next_player, r, c);
            table[r][c] = Board.NONE;
            position[c]--;

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
