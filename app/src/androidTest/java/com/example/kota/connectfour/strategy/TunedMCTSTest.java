package com.example.kota.connectfour.strategy;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.example.kota.connectfour.ui.Board;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by kota on 2015/03/31.
 * Test Tuned MCTS Strategy.
 */
public class TunedMCTSTest extends InstrumentationTestCase {
    Context mContext;
    Board board;

    private final int FIRST = Board.FIRST_PLAYER;
    private final int SECOND = Board.SECOND_PLAYER;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
        board = new Board(mContext, 4, 7, 6, false);
    }

    /**
     * ???? Could not find out the reason of this problem so pruneLoseMove resolve this problem.
     *
     * MCTS(O) vs MiniMax(X)
     * In this situation, MCTS made a move on column 6 not 2.
     * UCT score was like this,
     * column 1: update = 10990, uct_value : 0.002730
     * column 2: update = 9158, uct_value : -0.000218
     * column 6: update = 11155, uct_value : 0.002958
     * => Overflow would occur.
     * - - O X X - X
     * - - X O X - O
     * - - X X X - O
     * - X X O O - X
     * - O O X O - O
     * O X O O X - O
     * 1 2 3 4 5 6 7
     */
    public void testOverFlowSituation() {
        TunedMCTS strategy = new TunedMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
        strategy.setParameter(BaseMCTS.TIME_LIMIT, 5);
        int[] setup_move = {3,3,3,3,2,4,3,3,2,2,4,2,4,2,2,4,6,4,6,6,6,4,6,6,0,1,1,1};
        // set up board
        Board board = new Board(mContext, 4, 7, 6, false);
        int player = FIRST;
        for (int col : setup_move) {
            board.update(player, col);
            player = player == FIRST ? SECOND : FIRST;
        }

        for(int i=0;i<10;i++) {
            int col = strategy.think(board);
            assertEquals(String.format("choosed column %d", col), 1, col);
        }
    }

    public void testMainFunction() {
        int[] choose_num = new int[board.COLUMN];
        int ok_num = 0;
        TunedMCTS strategy = new TunedMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
        for(int i=0;i<10;i++) {
            Board board = new Board(mContext, 4, 7, 6, false);
            int col = strategy.think(board);
            choose_num[col]++;
            if(col==3) ok_num++;
        }
        if(ok_num<5) fail(String.format("choose num = %s", Arrays.toString(choose_num)));
    }

    /**
     * Test these situation.
     * - - - - - - -    - - - - - - -   - - - - - - -
     * - - - - - - -    - - - - - - -   - - - - - - -
     * - - - - - - -    - - - - - - -   X - - - - - -
     * - - - - - - -    - - - - - - -   O - - - - - -
     * - - - X X - -    O - - X X X -   O - - - - - -
     * - M O O O X -    O - M O O X M   O - X X X - O
     * 1 2 3 4 5 6 7    1 2 3 4 5 6 7   1 2 3 4 5 6 7
     */
    public void testPruneMove() {
        int[][] setup_moves = {
                {2,5,3,3,4,4},
                {0,5,0,5,3,3,4,4},
                {0,2,0,3,0,4,6,0}
        };
        int[][] answer = {
                null,
                {2,6},
                null
        };

        for(int t=0;t<2;t++) {
            // set up board
            board.refresh();
            for(int i=0;i<board.COLUMN;i++) board.getPosition()[i] =0;
            int player = FIRST;
            for (int col : setup_moves[t]) {
                board.update(player, col);
                player = player == FIRST ? SECOND : FIRST;
            }

            Class[] args = {Board.class, BaseMCTS.Tree.class};
            TunedMCTS strategy = new TunedMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            try {
                Class treeClazz = Class.forName("com.example.kota.connectfour.strategy.BaseMCTS$Tree");
                Class[] constructorParamTypes = {BaseMCTS.class, int.class}; // first arg is Outer class object
                Constructor constructor = treeClazz.getDeclaredConstructor(constructorParamTypes);
                constructor.setAccessible(true);
                BaseMCTS.Tree v_0 = (BaseMCTS.Tree)constructor.newInstance(strategy, board.COLUMN);

                v_0.parent = null;
                Method method1 = TunedMCTS.class.getDeclaredMethod("pruneLoseMove", args);
                method1.setAccessible(true);
                int[] res = (int[])method1.invoke(strategy,  board, v_0);
                if(t==0) {
                    assertEquals(String.format("Test Case %d ",t), 1, res[0]);
                } else if(t==3) {
                    assertNotNull(res);
                } else {
                    BaseMCTS.Tree[] children = v_0.children;
                    int prune_num = 0;
                    for(int i=0;i<children.length;i++) {
                        if(children[i] != null && children[i].is_terminal) {
                            prune_num ++;
                            boolean exist = false;
                            for(int col: answer[t]) if(i==col) exist = true;
                            assertTrue(String.format("Test Case %d (pruned column %d)", t, i), exist);
                        }
                    }
                    int l = answer[t].length;
                    assertEquals(String.format("Test case %d (prune num is %d)",t, l), l, prune_num);
                }
            } catch (Exception e) {
                fail(e.toString());
            }
        }
    }



}
