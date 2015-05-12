package com.ikota.connectfour.strategy;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.ikota.connectfour.ui.Board;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by kota on 2015/02/13.
 * deprecate_menu_activity Heuristic function.
 * deprecate_menu_activity is done by first player point of view.
 */
public class HeuristicTest extends InstrumentationTestCase {
    Context mContext;
    Heuristic heuristic;
    BaseStrategy strategy;
    Board board;

    private final int FIRST = Board.FIRST_PLAYER;
    private final int SECOND = Board.SECOND_PLAYER;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
        heuristic = new Heuristic(SECOND, FIRST);
        heuristic.setParameter(1000,18,62,0);
        strategy = new IterativeDeepening(SECOND, FIRST);
        ((IterativeDeepening)strategy).setParameter(IterativeDeepening.MODE_DEPTH_LIMIT, 0);
        strategy.useHeuristic(true);
        board = new Board(mContext, 4, 7, 6, false);
    }

    /**
     * check if correctly calculate heuristic parameter in this situation
     * - - - X - - -
     * - - - O - - -
     * - - - X - - -
     * - - - O - - -
     * - - - X - - -
     * - - - O O - -
     * 1 2 3 4 5 6 7
     */
    public void testWinningLineCount() {
        // set up board
        board.refresh();
        int[] setup_moves = {3,3,3,3,3,3,4};
        int player = FIRST;
        for(int col : setup_moves) {
            board.update(player, col);
            player = player == FIRST ? SECOND : FIRST;
        }

        int[][] answer = {
                // {ktn,kot,ket}, {ctn,cot,cet}
                {0,0,0},{1,0,0},
                {1,0,1},{0,0,0},
                {0,0,0},{1,0,0},
                {0},{0},// dummy
                {1,0,0},{1,0,1},
                {0,0,0},{0,0,0},
                {0,0,0},{1,0,0}
        };
        Class[] args = {Board.class, int.class, int.class, int.class};
        try {
            for(int i=0;i<board.COLUMN;i++) {
                if(board.getPosition()[i] == board.ROW) continue;
                Method method1 = Heuristic.class.getDeclaredMethod("checkWinningLine", args);
                Method method2 = Heuristic.class.getDeclaredMethod("checkWinningLine", args);
                method1.setAccessible(true);
                method2.setAccessible(true);
                int[] killinfo = (int[])method1.invoke(heuristic,  board, FIRST, board.getPosition()[i], i);
                int[] createinfo = (int[])method2.invoke(heuristic,  board, SECOND, board.getPosition()[i], i);
                killinfo = Arrays.copyOfRange(killinfo, 0, killinfo.length-2);
                createinfo = Arrays.copyOfRange(createinfo, 0, createinfo.length-2);
                assertEquals("move : kill column "+(i+1),Arrays.toString(answer[2*i]), Arrays.toString(killinfo));
                assertEquals("create column "+(i+1),Arrays.toString(answer[2*i+1]), Arrays.toString(createinfo));
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test these situation.
     * - - - - - - -    - - - - - - -   - - - - - - -
     * - - - - - - -    - - - - - - -   - - - - - - -
     * - - - M - - -    - - - - - - -   - - - - M - -
     * - - - O X - -    - - - - - - -   - - - O O - -
     * - - - O X - -    - - - O O - -   - - O O X - -
     * - - - O X - -    - - O X X X M   - O X X X - X
     * 1 2 3 4 5 6 7    1 2 3 4 5 6 7   1 2 3 4 5 6 7
     */
    public void testFinishMoveCount() {
        int[][] setup_moves = {
                {3,4,3,4,3,4},
                {2,3,3,4,4,5},
                {1,2,2,3,3,4,3,4,4,6}
        };
        int[] finish_moves = {3,6,4};
        int[] answer1 = {1,0,1};
        int[] answer2 = {0,1,0};

        for(int t=0;t<3;t++) {
            // set up board
            board.refresh();
            for(int i=0;i<board.COLUMN;i++) board.getPosition()[i] =0;
            int player = FIRST;
            for (int col : setup_moves[t]) {
                board.update(player, col);
                player = player == FIRST ? SECOND : FIRST;
            }

            Class[] args = {Board.class, int.class, int.class, int.class};
            try {
                Method method1 = Heuristic.class.getDeclaredMethod("checkWinningLine", args);
                Method method2 = Heuristic.class.getDeclaredMethod("checkWinningLine", args);
                method1.setAccessible(true);
                method2.setAccessible(true);
                int col = finish_moves[t]; int row = board.getPosition()[col];
                int[] create_info = (int[])method1.invoke(heuristic,  board, FIRST, row, col);
                int[] kill_info = (int[])method2.invoke(heuristic,  board, SECOND, row, col);
                assertEquals(String.format("Test Case %d (Finish Move)", t), answer1[t], create_info[4]);
                assertEquals(String.format("Test Case %d (Not Finish Move)", t), answer2[t], kill_info[4]);
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }

    public void testHeuristicChoice() {
        board.refresh();
        int[] stab_first_player = {3,3,3,4,4,4,1,1,6,1,0,6,6};
        int[][] second_player_answer = {
                {3},{3},{3},{4},{4},{4},{1},{1},{1},{6},{6},{6},{0}
        };
        int len = stab_first_player.length;
        for(int i=0;i<len;i++) {
            board.update(FIRST, stab_first_player[i]);
            int[] choose = heuristic.choiceByHeuristic(board, getPossibleColumns(board));
            assertEquals((i+1)+"th move",second_player_answer[i][0], choose[0]);
            board.update(SECOND, second_player_answer[i][0]);
        }
        board.refresh();
    }

    /**
     * Test first 12 th move of StrongHeuristicChoice
     */
    public void testStrongHeuristicChoice() {
        Heuristic first_heuristic = new Heuristic(FIRST, SECOND);
        Heuristic second_heuristic = new Heuristic(SECOND, FIRST);
        // deprecate_menu_activity 1 (check if first 6 moves are put in center column)
        board.refresh();
        for(int i=0;i<board.COLUMN;i++) board.getPosition()[i] =0;
        int player = FIRST;
        for(int i=0;i<6;i++) {
            Heuristic h = player == FIRST ? first_heuristic : second_heuristic;
            int[] choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
            assertEquals(String.format("Answer length (%d/6)",i), 1, choice.length);
            assertEquals(String.format("First 6 move(%d/6)",i), 3, choice[0]);
            board.update(player, choice[0]);
            player = player == FIRST ? SECOND : FIRST;
        }
        // 7 th move
        //Heuristic h = player == FIRST ? first_heuristic : second_heuristic;
        //int[] choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        //assertTrue("7 th move", 2 == choice[0] || 4 == choice[0]);
        board.update(player, 4);
        player = player == FIRST ? SECOND : FIRST;

        // 8 th move
        Heuristic h = player == FIRST ? first_heuristic : second_heuristic;
        int[] choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        assertEquals("8 th move", 2, choice[0]);
        board.update(player, 2);
        player = player == FIRST ? SECOND : FIRST;

        // 9 th move
        h = player == FIRST ? first_heuristic : second_heuristic;
        choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        assertEquals("9 th move", 2, choice[0]);
        board.update(player, 4);
        player = player == FIRST ? SECOND : FIRST;

        // 10 th move
        h = player == FIRST ? first_heuristic : second_heuristic;
        choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        assertEquals("10 th move", 2,choice[0]);
        board.update(player, 2);
        player = player == FIRST ? SECOND : FIRST;

        // 11 th move
        h = player == FIRST ? first_heuristic : second_heuristic;
        choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        assertEquals("11 th move", 2, choice[0]);
        board.update(player, 4);
        player = player == FIRST ? SECOND : FIRST;

        // 12 th move
        h = player == FIRST ? first_heuristic : second_heuristic;
        choice = h.choiceByStrongHeuristic(board, getPossibleColumns(board));
        assertEquals("12 th move", 4, choice[0]);
        board.update(player, 4);
    }

    private int[] getPossibleColumns(Board board) {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        int[] pos = board.getPosition();
        for(int i=0;i<board.COLUMN;i++) {
            if(pos[i]!=board.ROW) temp.add(i);
        }
        int[] possible_columns = new int[temp.size()];
        int len = possible_columns.length;
        for(int i=0;i<len;i++) {
            possible_columns[i] = temp.get(i);
        }
        return possible_columns;
    }

    /**
     * Test if [findSTMHSolver] method found these patterns.
     *                                                       M <- error
     *  - - - - - - -    - - - - - - -   - - - - - - -   - - X - - - -
     *  - - - - - - -    - - - - - - -   - - - - - - -   - - O - - - -
     *  - - - - - - -    - - - - - - -   - - - - - - -   - - X - - - -
     *  - - - - - - -    - - - - - - -   - - - - - - -   - - O X - - -
     *  - - - X X - -    - - X - - - -   X - - X - - -   - - X O - - -
     *  - - - O O - -    - - O - O - -   O - - O O - -   - X O O X O -
     *  1 2 3 4 5 6 7    1 2 3 4 5 6 7   1 2 3 4 5 6 7   1 2 3 4 5 6 7
     */
    public void testSTMHSolver() {
        int[][] setup_moves = {
                {3,3,4,4},
                {2,2,4},
                {0,0,3,3,4},
                {2,1,3,4,5,2,2,2,2,2,3,3}
        };
        int[][] answer_for_First = {
                {2,5},
                {3},
                {2,5},
                null
        };
        int[][] answer_for_second = {
                {2,5},
                {1,3,5},
                {2,5},
                null
        };
        Heuristic first_heuristic = new Heuristic(FIRST, SECOND);
        Heuristic second_heuristic = new Heuristic(SECOND, FIRST);

        for(int t=3;t<setup_moves.length;t++) {
            // set up board
            board.refresh();
            for(int i=0;i<board.COLUMN;i++) board.getPosition()[i]=0;
            int player = FIRST;
            for (int col : setup_moves[t]) {
                board.update(player, col);
                player = player == FIRST ? SECOND : FIRST;
            }

            Class[] args = {Board.class};
            try {
                Method method1 = Heuristic.class.getDeclaredMethod("findSTMHSolver", args);
                Method method2 = Heuristic.class.getDeclaredMethod("findSTMHSolver", args);
                method1.setAccessible(true);
                method2.setAccessible(true);
                ArrayList<Integer> first_solver = (ArrayList<Integer>)method1.invoke(first_heuristic,  board);
                ArrayList<Integer> second_solver = (ArrayList<Integer>)method2.invoke(second_heuristic, board);

                if(answer_for_First[t] == null ) {
                    if(first_solver!=null)
                        fail(String.format("Test Case %d - solver is %s", t, first_solver.toString()));
                    continue;
                }

                assertEquals(String.format("Test Case %d - first (answer length is not same)", t),
                        answer_for_First[t].length, first_solver.size());
                for(int ans : answer_for_First[t]) {
                    assertTrue(String.format("Test Case %d - first (doesn't contain column=%d)",t, ans),
                            first_solver.contains(ans));
                }

                assertEquals(String.format("Test Case %d - second (answer length is not same)", t),
                         answer_for_second[t].length,second_solver.size());
                for(int ans : answer_for_second[t]) {
                    assertTrue(String.format("Test Case %d - first (doesn't contain column=%d)", t, ans),
                            second_solver.contains(ans));
                }

            } catch (Exception e) {
                fail(String.format("deprecate_menu_activity %d : ",t)+e.toString());
            }
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


}
