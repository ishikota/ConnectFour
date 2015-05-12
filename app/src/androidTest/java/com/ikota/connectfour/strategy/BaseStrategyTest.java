package com.ikota.connectfour.strategy;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.ikota.connectfour.ui.Board;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by kota on 2015/02/17.
 * Test these Base Strategy function.
 * 1. randomChoice
 */
public class BaseStrategyTest extends InstrumentationTestCase {
    Context mContext;
    BaseStrategy strategy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
        strategy = new RandomStrategy(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
    }

    /**
     * Test case 1 ( for 50 times )
     *  best_moves = [0,1,2,3,4,5,6]
     *  available column = [0,1,2,3,4,5,6]
     *  answer => any of {0,1,2,3,4,5,6}
     *
     * Test case 2
     *  best_moves = []
     *  available column = [0,1,2,3,4,5,6]
     *  answer => throw IllegalArgumentException
     *
     * Test case 3 ( for 20 times )
     *  best_moves = [0,1,2]
     *  available column = [2,3,4,5,6]
     *  answer => 2
     */
    public void testRandomChoice() {
        Board board = new Board(mContext, 4, 7, 6, false);
        Class[] args = {Board.class, int[].class};
        final int[][] BEST_MOVES = { {0,1,2,3,4,5,6}, {}, {0,1,2} };
        final ArrayList<Integer>[] ANSWER = new ArrayList[]{
                new ArrayList(){{add(0);add(1);add(2);add(3);add(4);add(5);add(6);}},
                new ArrayList(),
                new ArrayList(){{add(2);}}
        };

        for(int i=0;i<3;i++) {
            board.refresh();
            // prepare board
            if(i==2) {
                for(int k=0;k<board.ROW;k++) {
                    board.update(Board.FIRST_PLAYER, 0);
                    board.update(Board.FIRST_PLAYER, 1);
                }
            }
            for(int j=0;j<20;j++) {
                try {
                    Method method = BaseStrategy.class.getDeclaredMethod("randomChoice", args);
                    method.setAccessible(true);
                    int col = (Integer) method.invoke(strategy, board, BEST_MOVES[i]);
                    if(!ANSWER[i].contains(col)) fail("randomChoice is wrong in Test case "+(i+1));
                } catch (NoSuchMethodException e) {
                    fail(e.toString());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    if(i!=1) fail(e.toString());
                }
            }
        }
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
