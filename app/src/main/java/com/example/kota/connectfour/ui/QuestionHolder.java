package com.example.kota.connectfour.ui;

/**
 * Created by kota on 2015/03/17.
 */
public class QuestionHolder {
    private static final int f = Board.FIRST_PLAYER, s = Board.SECOND_PLAYER, n = Board.NONE;

    public static final Question[] DEMO = {
        new Question(
                new int[][]{
                        {n,n,f,f,n,n,n},
                        {n,n,s,s,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},},
                f,
                3,
                new int[]{5,4,1}
        ),
        new Question(
                new int[][]{
                        {n,n,f,f,n,n,n},
                        {n,n,s,s,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},},
                f,
                3,
                new int[]{4,5,1}
        ),
    };

    public static final Question[] EASY = {
        new Question(
                new int[][]{
                        {n,n,f,f,n,n,n},
                        {n,n,s,s,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},
                        {n,n,n,n,n,n,n},},
                f,
                3,
                new int[]{4,5,1}
        )
    };

    public static final Question[] HARD = {
        new Question(
                new int[][]{
                        {s,f,n,f,s,s,f},
                        {n,s,n,f,n,f,s},
                        {n,f,n,s,n,f,n},
                        {n,s,n,f,n,s,n},
                        {n,f,n,s,n,f,n},
                        {n,s,n,f,n,s,n},},
                f,
                9,
                new int[]{0,0,0,0,0,6,6,4,4}
        )
    };

    /**
     * deep copy question object
     * @param q : source question object
     * @return  : deep copied question object
     */
    public static Question getQuestion(Question q) {
        int max_row = q.table.length;
        int mat_col = q.table[0].length;
        int[][] table = new int[max_row][mat_col];
        for(int i=0;i<max_row;i++) {
            for(int j=0;j<mat_col;j++) {
                table[i][j] = q.table[i][j];
            }
        }
        return new Question(
                table,
                q.first_player,
                q.limit_turn,
                q.answer_moves
        );
    }
}
