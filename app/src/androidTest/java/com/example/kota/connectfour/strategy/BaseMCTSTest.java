package com.example.kota.connectfour.strategy;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.example.kota.connectfour.ui.Board;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by kota on 2015/02/17.
 * Test Base Monte Carlo Tree Search functions.
 */
public class BaseMCTSTest extends InstrumentationTestCase {
    private Context mContext;
    private BaseMCTS mStrategy;

    private final int FIRST = Board.FIRST_PLAYER;
    private final int SECOND = Board.SECOND_PLAYER;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
        mStrategy = new BaseMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
        mStrategy.setParameter(BaseMCTS.SIMULATION_NUM_LIMIT, 100);
    }

    /**
     * Could not resolve this problem
     * ==> SOLVED THESE 2 PROBLEM !! (2015/04/02)
     *     changed value to assign terminal node from 1/-1 to 100/-100
     *     v_child.value = next_player == ME ? 100 : -100; => v_child.value = next_player == ME ? 1 : -1;
     *
     * MCTS(O) vs MiniMax(X)
     * In this situation, MCTS made a move on column 6 not 2.
     * UCT score was like this,
     * column 1: update = 10990, uct_value : 0.002730
     * column 2: update = 9158, uct_value : -0.000218
     * column 6: update = 11155, uct_value : 0.002958
     * => Overflow would occur.
     *
     * - - O X X - X
     * - - X O X - O
     * - - X X X - O
     * - X X O O - X
     * - O O X O - O
     * O X O O X - O
     * 1 2 3 4 5 6 7
     *
     * also this situation. second player choose first column but should choose 6
     *  O - O O O - -
     *  O - O X X O -
     *  X - X X O X X
     *  X - O O X X X
     *  O O X O X O O
     *  O O X O X X X
     *  1 2 3 4 5 6 7
     *
     *  in this situation, NullPointerException occurred in reading .is_infeasible.
     *  => Once OverFlowFlg gets true, it kept true state. So in next UCTSearch, no play out is done
     *     and in BestChild nullPo occurred. Solved by adding "overflow_flg = false;" in the first of
     *     UCT search method.
     *
     *  - - - - - - -
     *  - - - - - - -
     *  - - - - - - -
     *  - - - - - - -
     *  O - - - - - -
     *  O - - X X - O
     *  1 2 3 4 5 6 7
     *
     */
    public void testOverFlowSituation() {
        int[][] setup_move = {
                {3,3,3,3,2,4,3,3,2,2,4,2,4,2,2,4,6,4,6,6,6,4,6,6,0,1,1,1},
                {3,4,0,4,1,2,0,4,4,2,2,2,1,0,3,5,3,3,5,0,0,3,3,4,4,6,6,5,2,6,0,5,2,6,5},
                {0,3,6,4,0}
        };
        int[] answer = {1,5,-1};
        for(int t=2;t<setup_move.length;t++) {
            // set up board
            Board board = new Board(mContext, 4, 7, 6, false);
            int player = FIRST;
            for (int col : setup_move[t]){
                board.update(player, col);
                player = player == FIRST ? SECOND : FIRST;
            }
            BaseMCTS s;
            if(player == FIRST) {
                s = new BaseMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            } else {
                s = new BaseMCTS(Board.SECOND_PLAYER, Board.FIRST_PLAYER);
            }
            s.setParameter(BaseMCTS.SIMULATION_NUM_LIMIT, 10000);

            for (int i = 0; i < 10; i++) {
                int col = s.think(board);
                if(t!=2)assertEquals(String.format("Test %d : choosed column %d.", t, col), answer[t], col);
            }
        }
    }

    public void testMainFunction() {
//        int strange = 0;
//        for(int i=0;i<10;i++) {
//            Board board = new Board(mContext, 4, 7, 6, false);
//            int col = mStrategy.think(board);
//            if(col==0) strange++;
//        }
//        if(strange>5) fail("MCTS choices 0 column lot of times");
    }



    /**
     * TODO : check if all child of v(parent of leaf node which treePolicy returns) are expanded
     */
    public void testTreePolicy() {
    }

    /**
     * In first implementation, we set initial value of best_val = -1.
     * But in this situation, all of UCT values are less than -1.
     * So bestChild methods return -1 and this causes ArrayIndexOutOfException in treePolicy method.
     * Finally we solved this problem by changing initial value to negative infinite value
     */
    public void testBestChild() {
        // init board
        Board board = new Board(mContext, 4, 7, 6, false);
        int player = Board.FIRST_PLAYER;
        int[] init_moves = {3,0,3,1,2,4,4,5,5,2,2,1,4,1,1,3,2,1,1,3,4,4,4};
        for(int move : init_moves) {
            board.update(player, move);
            player = player == Board.FIRST_PLAYER ? Board.SECOND_PLAYER : Board.FIRST_PLAYER;
        }
        BaseMCTS strategy = new BaseMCTS(Board.SECOND_PLAYER, Board.FIRST_PLAYER);
        strategy.think(board);

    }

    /**
     * deprecate_menu_activity if random simulation is correctly done.
     * 1. simulation has properly finished.
     * 2. check if each player make his move in turn.
     */
    public void testDefaultPolicy() {
        for(int t=0;t<50;t++) {
            Board board = new Board(mContext, 4, 7, 6, false);
//            board.update(Board.FIRST_PLAYER, 4);
//            board.update(Board.SECOND_PLAYER, 4);
//            board.update(Board.FIRST_PLAYER,3);

            try {
                Class treeClazz = Class.forName("com.example.kota.connectfour.strategy.BaseMCTS$Tree");
                Class[] constructorParamTypes = {BaseMCTS.class, int.class}; // first arg is Outer class object
                Constructor constructor = treeClazz.getDeclaredConstructor(constructorParamTypes);
                constructor.setAccessible(true);
                Object tree = constructor.newInstance(mStrategy, 7);

                Class[] args = {treeClazz, Board.class, int.class};
                Method method = BaseMCTS.class.getDeclaredMethod("defaultPolicy", args);
                method.setAccessible(true);
                method.invoke(mStrategy, tree, board, Board.FIRST_PLAYER);
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.toString());
            }

            // check if simulation was properly played.
            int[][] table = board.getTable();
            int counter[] = new int[3]; // First, Second, Blank
            boolean has_game_finish = false;
            for (int i = 0; i < board.ROW; i++) {
                for (int j = 0; j < board.COLUMN; j++) {
                    int index = table[i][j] == Board.FIRST_PLAYER ? 0 :
                            table[i][j] == Board.SECOND_PLAYER ? 1 : 2;
                    counter[index] += 1;
                    if (board.checkIfWin(table[i][j], i, j, false)) has_game_finish = true;
                }
            }
            has_game_finish |= board.checkIfDraw();
            assertTrue("Simulation finishes in the middle of game.", has_game_finish);
            int test = counter[0] - counter[1];
            if (test != 0 && test != 1) {
                fail(String.format("(%d th deprecate_menu_activity) number of moves in the board is invalid"+
                        "(first : %d , second : %d)",(t+1),counter[0], counter[1]));
            }
        }
    }

    /**
     *
     */
    public void testBackPropagation() {

    }

}
