package com.ikota.connectfour.strategy;

import com.ikota.connectfour.ui.Board;

/**
 * Created by kota on 2014/12/20.
 * This Strategy choose next move at random.
 * Random choice is implemented in BaseStrategy class, so what this class
 * does is just return all possible columns.
 */
public class RandomStrategy extends BaseStrategy{

    public RandomStrategy(int me, int oppo) {
        super(me, oppo);
    }

    @Override
    public int[] makeANextMove(Board board) {
        int c = 0;
        for(int i=0;i<board.COLUMN;i++) {
            if(board.getPosition()[i]!=board.ROW) c++;
        }
        //if(c==board.COLUMN) throw new IllegalStateException("makeANextMove is called in full stacked state.");
        int[] col = new int[c];
        c = 0;
        for(int i=0;i<board.COLUMN;i++) {
            if(board.getPosition()[i]!=board.ROW) col[c++]=i;
        }
        return col;
    }
}
