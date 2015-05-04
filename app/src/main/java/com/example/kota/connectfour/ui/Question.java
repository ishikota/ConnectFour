package com.example.kota.connectfour.ui;

/**
 * Created by kota on 2015/03/17.
 * Store question information and current question state (like current_turn_num).
 */
public class Question {
    int[][] table;
    int[] position;
    final int limit_turn;
    int current_turn_num;

    // it's possible to calculate first player by counting table state.
    // but we do not calculate it to be able to create illegal state question.
    // (like ,"lots of squares are filled with red move but yellow should win in 5 turn!!")
    final int first_player;

    // array of columns of two answer player
    final int[] answer_moves;

    public Question(int[][] table, int first_player, int limit_turn, int[] answer) {
        this.table = table;
        this.position = calcPosition(table);
        this.first_player = first_player;
        this.limit_turn = limit_turn;
        this.answer_moves = answer;
    }

    public int getCurrentTurn() {return current_turn_num;}

    public int getNextPlayer() {
        int temp = first_player == Board.FIRST_PLAYER ? 0:1;
        if((temp+current_turn_num)%2==0) return Board.FIRST_PLAYER;
        else return Board.SECOND_PLAYER;
    }

    public void forwardState() {current_turn_num++;}
    public void rewindState() {current_turn_num--;}
    public int getAnswerMove() { return answer_moves[current_turn_num];}

    private static int[] calcPosition(int[][] table) {
        int[] position = new int[table[0].length];
        for (int i = table.length - 1; i >= 0; i--){
            for (int j = 0; j < table[0].length; j++) {
                if (position[j] == 0 && table[i][j] != Board.NONE) position[j] = i + 1;
            }
        }
        return position;
    }
}