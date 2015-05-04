package com.example.kota.connectfour.strategy;

import android.util.Log;

import com.example.kota.connectfour.ui.Board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by kota on 2015/02/11.
 * This class calculates an approximate value by using odd/even threat.
 */
public class Heuristic {
    private static final String TAG = Heuristic.class.getSimpleName();
    private static final boolean D = false;
    public final int ME,OPPO;

    // weights for heuristic parameter
    private int[] weights = {10000, 1000, 100, 10};

    public Heuristic(int me, int oppo) {
        this.ME = me;
        this.OPPO = oppo;

        // set best weights of heuristic parameter for each player.
        final int[] PARAM_FOR_FIRST = {24,18,62,1};
        final int[] PARAM_FOR_SECOND = {1000,18,62,1};
        int[] p = me==Board.FIRST_PLAYER ? PARAM_FOR_FIRST : PARAM_FOR_SECOND;
        System.arraycopy(p,0,weights,0,4);
    }

    public void setParameter(int w1, int w2, int w3, int w4) {
        weights[0] = w1;
        weights[1] = w2;
        weights[2] = w3;
        weights[3] = w4;
    }

    /**
     * return array of columns which is selected from possible_columns by heuristic function.
     * @param board : Board instance
     * @param possible_columns : columns to calculate heuristic value
     * @return columns of moves which got highest heuristic value in possible_columns.
     */
    public int[] choiceByHeuristic(Board board, int[] possible_columns) {
        ArrayList<Integer> heuristic_moves = new ArrayList<Integer>(board.COLUMN);
        int best_heuristic_val = -1;

        for(int col : possible_columns) {
            int row = board.getPosition()[col];

            int[] kti = checkWinningLine(board, OPPO, row, col); // killed threat info
            int[] cti = checkWinningLine(board, ME, row, col);  // create threat info
            int temp_val = calcHeuristicValue(kti[0], kti[1], kti[2], cti[0], cti[1], cti[2]);
            if(D) Log.i(TAG, String.format("column %d: Heuristic Value = %d", col, temp_val));
            if(D) Log.i(TAG, "ktn,kot,ket = "+Arrays.toString(kti)+", ctn,cot,cet = "+Arrays.toString(cti));
            if (temp_val > best_heuristic_val) {
                heuristic_moves.clear();
                heuristic_moves.add(col);
                best_heuristic_val = temp_val;
            } else if(temp_val == best_heuristic_val) {
                heuristic_moves.add(col);
            }
        }

        // convert ArrayList to int array
        int res_size = heuristic_moves.size();
        int[] cols = new int[heuristic_moves.size()];
        for(int j=0; j<res_size;j++) {
            cols[j] = heuristic_moves.get(j);
        }
        if(D) Log.i(TAG,"selected columns"+ Arrays.toString(cols));
        return cols;
    }

    private final Random mRand = new Random();
    public int[] choiceByStrongHeuristic(Board board, int[] possible_columns) {
        int col_num = possible_columns.length;
        int[] h_val = new int[col_num]; //heuristic value for each column
        int[] critical_height = new int[col_num];

        // save all possible columns of heuristic value and critical threat height
        for(int i=0;i<col_num;i++) {
            int col = possible_columns[i];
            int row = board.getPosition()[col];

            int[] kti = checkWinningLine(board, OPPO, row, col); // killed threat info
            int[] cti = checkWinningLine(board, ME, row, col);  // create threat info
            if(kti[4] == 1 || cti[4] == 1) return new int[]{col}; // if this is finish move
            h_val[i] = calcHeuristicValue(kti[0], kti[1], kti[2], cti[0], cti[1], cti[2]);
            critical_height[i] = Math.min(kti[3], cti[3]);

            if(row+1!=board.ROW && board.checkIfWin(OPPO, row+1, col, false)) {
                // never put here. You lose !!
                critical_height[i] = 200;
                h_val[i] = -3;
            } else if(row+1!=board.ROW && board.checkIfWin(ME, row+1, col, false)) {
                critical_height[i] = 150;
                h_val[i] = -2;
            }
        }

        // if STMH solver found then choose best move from there.
        ArrayList<Integer> STMHSolver = findSTMHSolver(board);
        if(STMHSolver!=null) {
            HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
            int s = possible_columns.length;
            for(int i=0;i<s;i++) {
                map.put(possible_columns[i], i);
            }
            int best_solver = -1, best_h_val = -10;
            for(int col: STMHSolver) {
                int index = map.get(col);
                if(h_val[index] > best_h_val) {
                    best_solver = col;
                    best_h_val = h_val[index];
                }
            }
            return new int[]{best_solver};
        }

        // if critical move found then choose best move from there,
        int best_height = 100;
        int best_move_index = -1;
        for(int i=0;i<col_num;i++) {
            if(critical_height[i]<best_height) {
                best_move_index = i;
                best_height = critical_height[i];
            } else if(critical_height[i] == best_height && best_height!=100) {
                best_move_index = h_val[best_move_index] < h_val[i] ? i : best_move_index;
            }
        }
        if(best_move_index != -1)
            return new int[]{possible_columns[best_move_index]};

        // choose best move by heuristic value.
        int best_score = -10;
        for(int i=0;i<col_num;i++) {
            if(h_val[i] > best_score) {
                best_move_index = i;
                best_score = h_val[i];
            } else if(h_val[i] == best_score) {
                if(mRand.nextBoolean()) best_move_index = i;
            }
        }
        return new int[]{possible_columns[best_move_index]};
    }

    /**
     * calculate heuristic value by using passed heuristic parameter and defined weight.
     * @param ktn : killed threat number
     * @param kot : killed odd threat number
     * @param ket : killed even threat number
     * @param ctn : create threat number
     * @param cot : create odd threat number
     * @param cet : create even threat number
     * @return      calculated heuristic value
     */
    private int calcHeuristicValue(int ktn, int kot, int ket, int ctn, int cot, int cet) {
        int kcln = ME == Board.FIRST_PLAYER ? ket : kot; // killed critical threat number
        int ccln = ME == Board.FIRST_PLAYER ? cot : cet; // create critical threat number
        return kcln*weights[0] + ktn*weights[1] + ccln*weights[2] + ctn*weights[3];
    }

    /**
     * check if passed square creates winning line.
     * @param board  Board instance
     * @param player player who uses heuristic
     * @param row    the row of square to calculate heuristic value
     * @param col    the column of square to calculate heuristic value
     * @return array of these values
     *          [win_line_count, odd_threat_num, even_threat_num, critical_best_height]
     *          the number of threat for passed player,
     *          the number of each odd/even threat,
     *          lowest height of critical threat (lower threat would be filled earlier).
     */
    private int[] checkWinningLine(Board board, int player, int row, int col) {
        // check these 4 lines {line_upper_right, line_horizontal, line_lower_right, line_bottom }
        int[] line_elem_num = {1,1,1,1};
        int[] threat_count = {0,0,0,0}; // flg -> 0:none, 1:odd, 2:even, 3:both

        // 7 direction (because do not need to count above line)
        final int[] di = {1,-1,0,0,-1,1,-1};
        final int[] dj = {1,-1,1,-1,1,-1,0};

        int win_line_count = 0;
        int odd_threat_num = 0;
        int even_threat_num = 0;
        int critical_best_height = 100;
        int temp_height = -1;
        int opponent = player == ME ? OPPO : ME;

        int is_finish_move = 0; // 0 -> not finish move , 1 -> finish move
        int[] line_move_num = {1,1,1,1}; // number of successive moves in the line

        // check 7 direction
        for(int d=0; d<7;d++) {
            int ni = row; int nj = col;
            boolean successive = true;

            for(int k=0; k<board.CONNECT_K-1;k++) {
                ni += di[d];
                nj += dj[d];

                // if (ni,nj) is out of board or opponent already interrupting this winning line
                if ((ni<0 || board.ROW<=ni) || (nj<0 || board.COLUMN<=nj)
                        || board.getTable()[ni][nj] == opponent) {
                    break;
                }

                // count how many successive moves does player have in this line.
                if (successive && board.getTable()[ni][nj] == player)
                    line_move_num[d/2]++;
                else
                    successive = false;

                // if this square is empty and below square is not empty then this line is not winning line.
                if (ni==0 || (board.getTable()[ni][nj]==Board.NONE &&
                        board.getTable()[ni-1][nj]!=Board.NONE)) {
                    continue; // TODO : this should be break?
                }

                // if (ni,nj) is empty then its threat. write down if it's odd or even threat.
                if (board.getTable()[ni][nj] == Board.NONE) {
                    temp_height = Math.max(temp_height, ni);
                    if (ni%2==0) threat_count[d/2] |= 1; // check if it's odd threat
                    else threat_count[d/2] |= 2; // check if it's even threat
                }
                line_elem_num[d/2] += 1;
            }

            if(d%2==1 || d==6) {
                // player win if he makes a move here.
                if (line_move_num[d/2] >= board.CONNECT_K) is_finish_move = 1;

                if (line_elem_num[d/2] >= board.CONNECT_K) {
                    win_line_count += 1;
                    if(threat_count[d/2]==1) odd_threat_num += 1;
                    else if(threat_count[d/2]==2) even_threat_num += 1;
                    // count critical threat
                    if((player==Board.FIRST_PLAYER && threat_count[d/2]==1) ||
                            (player==Board.SECOND_PLAYER && threat_count[d/2]==2) ) {
                        critical_best_height = Math.min(critical_best_height, temp_height);
                    }
                }
                temp_height = -1;
            }
        }

        return new int[]{win_line_count, odd_threat_num, even_threat_num, critical_best_height, is_finish_move};
    }

    /**
    if find STMH(Succesive Three Moves in Horizontal) then return columns of moves
    which create or solve this STMH.
           (if found my STMH then create, otherwise kill it)
           if not found then return null as flag.
    EX.)
        [move 'X' on column 2 or 5 solve STMH and 'O' on 5 create STMH]
        - - - - -     - - - - -
        - - O O -  => - O O O -
        1 2 3 4 5     1 2 3 4 5
        [move 'X' on column 1,3,5 solve STMH and 'O' on 3 create STMH]
        - - - - -     - - - - -
        - O - O -  => - O O O -
        1 2 3 4 5     1 2 3 4 5
    */
    private ArrayList<Integer> findSTMHSolver(Board board) {
        ArrayList<Integer> ans = null;
        for (int i=0;i<board.COLUMN-5+1;i++) {
            if(!STMHTest1(board, i)) continue;
            ArrayList<Integer> temp = STMHTest2(board, i);
            if(temp!=null) {
                if(ans == null) {
                    temp.remove(temp.size()-1);
                    ans = new ArrayList<Integer>();
                    ans.addAll(temp);
                } else {
                    if(temp.get(temp.size()-1) == -1) {
                        // winning STMH
                        for(int col: temp) {
                            if(col<0) continue;
                            if(!ans.contains(col)) ans.add(col);
                        }
                    } else {
                        // if already STMH is found, then take intersection of solver.
                        ArrayList<Integer> new_ans = new ArrayList<Integer>();
                        for (int col : ans) {
                            if(col<0) continue;
                            if (temp.contains(col)) {
                                new_ans.add(col);
                            }
                        }
                        ans = new_ans;
                    }
                }
            }
        }
        return ans;
    }

    // check if 5 horizontal squares contain 3 blank square which is available
    private boolean STMHTest1(Board board, int i) {
        int[] p = board.getPosition();
        int h = p[i];
        if (h==board.ROW) return false;
        // check if edge of 5 horizontal squares are blank (must condition for STMH)
        int[] tb = board.getTable()[h];
        if(h == p[i+4] && tb[i] == Board.NONE && tb[i] == tb[i+4]) {
            int blank_num = 0;
            int flg = 0;
            for (int j=1;j<4;j++) {
                if(tb[i+j] == Board.NONE) {
                    if(p[i+j] == h) {
                        blank_num += 1;
                    } else {
                        blank_num = 2;
                        break;
                    }
                } else {
                    flg |= tb[i+j] == Board.FIRST_PLAYER ? 1 : 2;
                }
            }
            if(blank_num != 1 || flg ==3) return false;
        } else {
            return false;
        }
        return true;
    }

    // check if it's STMH in detail
    // if STMH is found then return columns which can solve STMH.
    private ArrayList<Integer> STMHTest2(Board board, int i) {
        int[] p = board.getPosition();
        int h = p[i];
        int[] tb = board.getTable()[h];
        ArrayList<Integer> solver = null;
        // find pattern 1 (center of 5 horizontal squares is not blank)
        if(tb[i+2] != Board.NONE) {
            int player = tb[i+2];
            // add flg on the end of solver. ( -1 -> winning(my) STMH, -2 -> losing(opponent's) STMH )
            if(tb[i+1] == tb[i+2]) {
                solver = new ArrayList<Integer>();
                if (player == ME) {solver.add(i + 3);}
                else {solver.add(i); solver.add(i + 3);solver.add(i + 4);}
                solver.add(player==ME ? -1 : -2);
            } else if(tb[i+2] == tb[i+3]) {
                solver = new ArrayList<Integer>();
                if(player == ME) {solver.add(i+1);}
                else {solver.add(i);solver.add(i+1);solver.add(i+4);}
                solver.add(player==ME ? -1 : -2);
            }
        } else if(tb[i+2] == Board.NONE && tb[i+1] != Board.NONE &&  tb[i+1] == tb[i+3]) {
            int player = tb[i+1];
            solver = new ArrayList<Integer>();
            // find pattern 2 ( center of 5 horizontal square is blank)
            if(player == ME) {solver.add(i+2);}
            else {solver.add(i);solver.add(i+2);solver.add(i+4);}
            solver.add(player==ME ? -1 : -2);
        }
        return solver;
    }

}
