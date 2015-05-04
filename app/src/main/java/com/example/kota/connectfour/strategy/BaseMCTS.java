package com.example.kota.connectfour.strategy;

import android.util.Log;
import android.util.Pair;

import com.example.kota.connectfour.ui.Board;

/**
 * Created by kota on 2015/02/17.
 * This is the basic MCTS algorithm with UCT value.
 */
public class BaseMCTS extends BaseStrategy{
    private static final String TAG = BaseMCTS.class.getSimpleName();
    // type of computational budget
    public static final int SIMULATION_NUM_LIMIT = 1;
    public static final int TIME_LIMIT = 2; // unit is second

    protected int mode = 2; // default computational budget is time
    protected int limit = 3;

    protected BaseStrategy mySimulationStrategy, oppoSimulationStrategy;

    private boolean overflow_flg = false;

    /**
     * This class represents a node of search tree.
     */
    protected class Tree {
        Tree parent;
        boolean is_infeasible; // this state is infeasible.(out of board size)
        double value;
        int update_num;
        final Tree[] children;
        final int child_num;
        int unvisited_child_num;
        boolean is_terminal;

        Tree(int child_num) {
            this.child_num = child_num;
            children = new Tree[child_num];
            unvisited_child_num = child_num;
        }
    }

    public BaseMCTS(int me, int oppo) {
        super(me, oppo);
        //set up
        mySimulationStrategy = new RandomStrategy(ME, OPPO);
        oppoSimulationStrategy = new RandomStrategy(OPPO, ME);
    }

    /**
     * set computational budget of MCTS search
     * @param mode : pass TIME_LIMIT or SIMULATION_NUM_LIMIT (these constants are defined above)
     * @param budget : the number of computational budget. if you use TIME_LIMIT then used unit is second.
     */
    public void setParameter(int mode, int budget) {
        this.mode = mode;
        this.limit = budget;
    }

    @Override
    public int[] makeANextMove(Board board) {
        return new int[] {UCTSearch(board)};
    }

    /**
     * grow search tree and choose best root child with UCT algorithm.
     * @param origin_board : current board
     * @return best move(column)
     */
    protected int UCTSearch(Board origin_board) {
        overflow_flg = false;
        long st = System.currentTimeMillis(); // start time
        // create root node v_0 with state s_0
        Tree v_0 = new Tree(origin_board.COLUMN);
        v_0.parent = null;
        // iterate MCTS search within computational budget
        int play_counter = 0;
        while (!checkIfReachBudget(st, play_counter) && !overflow_flg) {
            Board cp_board = origin_board.copyBoard();
            Pair<Tree, Integer> tmp = treePolicy(v_0, cp_board);// leaf node and next player
            double delta = defaultPolicy(tmp.first, cp_board, tmp.second);
            backPropagation(tmp.first, delta, tmp.second);
            play_counter += 1;
        }
        if(overflow_flg) Log.i("Overflow", "Overflow occurred");
        Log.i(TAG, String.format("PLAYOUT NUM : %d",play_counter));
        // choose best child of root node
        return bestChild(v_0, 0);
    }

    /**
     * check if search process reached to defined computational budget.
     * @param start_time  : pass search-started time if mode == TIME_LIMIT
     * @param play_counter : pass current times of simulation if mode == SIMULATION_NUM_LIMIT
     * @return result : return true if it reaches computational budget
     */
    protected boolean checkIfReachBudget(long start_time, int play_counter) {
        int budget;
        if (mode == TIME_LIMIT) {
            long ct = System.currentTimeMillis();
            budget = (int) ((ct - start_time) / 1000.0);
        } else {
            budget = play_counter;
        }

        // update progress bar
        if(mThinkingProgressListener!=null) {
            int progress = (int)(100.0*budget/limit);
            mThinkingProgressListener.publishProgress(progress);
        }
        return budget>=limit;
    }

    /**
     * descends search tree until it finds unvisited node.
     *
     * @param v : root node(v_0)
     * @param board : game state which corresponds to v
     * @return leaf : node where start point of simulation and its next player
     */
    protected Pair<Tree, Integer> treePolicy(Tree v, Board board) {
        int next_player = ME;
        while(!v.is_terminal) {
            if (v.unvisited_child_num != 0){
                return expand(v, board, next_player);
            } else {
                double C = 1/Math.sqrt(2); // constant for UCT value calculation
                int col = bestChild(v, C); // index(column) of best child
                v = v.children[col];
                final int row = board.getPosition()[col]++;
                board.getTable()[row][col] = next_player;
                next_player = next_player == ME ? OPPO : ME;
            }
        }
        return new Pair<Tree, Integer>(v, next_player);
    }

    /**
     * add unvisited node(state) to search tree
     * @param v : parent node of unvisited node.
     * @param board : game state which corresponds to v
     * @param next_player : the player who is about to make a next move in this state.
     * @return Tree object which corresponds to unvisited state and next player of that state.
     */
    private Pair<Tree, Integer> expand(Tree v, Board board, int next_player) {
        int col = -1;
        int len = v.children.length;
        for(int i=0; i<len; i++) {
            if(v.children[i] == null) {
                v.unvisited_child_num -= 1;
                if(board.getPosition()[i]==board.ROW) {
                    v.children[i] = new Tree(len);
                    v.children[i].is_infeasible = true; // this is invalid move.
                    if(v.unvisited_child_num == 0) {
                        return treePolicy(v, board);
                    }
                } else {
                    col = i;
                    break;
                }
            }
        }
        // add a new child node to node v
        Tree v_child = new Tree(len);
        v_child.parent = v;
        final int row = board.getPosition()[col]++;
        board.getTable()[row][col] = next_player;
        if(board.checkIfWin(next_player, row, col, false)) {
            v_child.is_terminal = true;
            v_child.value = next_player == ME ? 100 : -100;
        } else if(board.checkIfDraw()) {
            v_child.is_terminal = true;
            v_child.value = 0;
        }
        v.children[col] = v_child;
        next_player = next_player == ME ? OPPO : ME;
        return new Pair<Tree, Integer>(v_child, next_player);
    }

    /**
     * @param v : choose a child of this node.
     * @param C : Constant UCT value calculation
     * @return the index of v's child which gets highest UCT value.
     */
    protected int bestChild(Tree v, double C) {
        double best_val = Double.NEGATIVE_INFINITY;
        int best_index = -1;
        int len = v.children.length;
        for(int i=0;i<len;i++) {
            Tree child = v.children[i];
            if (child.is_infeasible) continue;
            double uct_value = calcUCTvalue(child, C);
            if(uct_value > best_val) {
                best_val = uct_value;
                best_index = i;
            } else if(uct_value == best_val) {
                if(mRandom.nextBoolean()) best_index = i;
            }

            if(C==0) Log.i(TAG, String.format("column %d: update = %d, uct_value : %f", (i+1), child.update_num, uct_value));
        }

        if (best_index==-1) {
            throw new IllegalStateException("best child selected nothing.");
        }
        return best_index;
    }

    /**
     * calculate UCT value of passed node
     * @param v : node of search tree to calculate UCT value
     * @param C : constant value for exploration term
     * @return UCT value
     */
    private double calcUCTvalue(Tree v, double C) {
        double exploitation_term = 1.0*v.value/v.update_num;
        double exploration_term = C*Math.sqrt(2*Math.log(v.parent.update_num)/v.update_num);
        //if(exploitation_term + exploration_term < 0)
//            Log.i("overflow", String.format("val:%d,update_num:%d,p_update_num:%d,exploit:%f,explore:%f",
//                    v.value,v.update_num,v.parent.update_num,exploitation_term, exploration_term));
        if (exploitation_term + exploration_term > 1<<16) overflow_flg = true;
        return exploitation_term + exploration_term;
    }

    /**
     * start simulation and get result value.
     * @param v_l : leaf node which is a start point of simulation
     * @param board : board which corresponds to v_l
     * @param next_player : : the player who is about to make a next move in v_l.
     * @return simulation result(WIN -> 1, DRAW -> 0.5, LOSE -> 0.01)
     */
    protected double defaultPolicy(Tree v_l, Board board, int next_player) {
        // this game state has already solved.
        if(v_l.is_terminal) return v_l.value;

        while (true) {
            // choose next move
            int col = chooseNextMove(next_player, board);
            // update board and check if game finished
            final int row = board.getPosition()[col]++;
            board.getTable()[row][col] = next_player;
            if(board.checkIfWin(next_player, row, col, false)) {
                return next_player==ME ? 1 : 0.01;
            } else if(board.checkIfDraw()) {
                return 0.5;
            }
            next_player = next_player==ME ? OPPO : ME;
        }
    }

    /**
     * Back propagate simulation result from leaf node to root node.
     * We use Negamax(good result for ME is bad result for OPPO), so
     * If we win in the simulation, then we give negative score to v_l
     * because v_l would be bad move for OPPO.
     * @param v : leaf node(v_l) which started simulation. start back propagation here.
     * @param delta : the result of simulation.
     * @param next_player : the next player of v_l
     */
    protected void backPropagation(Tree v, double delta, int next_player) {
        // if v_l is minimizer node, inverse sign of delta(reward) for negamax.
        delta = next_player == ME ? -delta : delta;
        // ascends tree to the root node
        while(true) {
            v.update_num += 1;
            v.value += delta;
            v = v.parent;
            delta = -delta; // doing negamax here
            if (v == null) break; // if it reaches root node
        }
    }

    /**
     * Return next move in the passed situation.
     * Now using strategy is RandomStrategy.
     * @param player : the player to make a next move.
     * @param board  : board to make a next move
     * @return selected move(column) by strategy.
     */
    private int chooseNextMove(int player, Board board) {
        if(player==ME) {
            return mySimulationStrategy.think(board);
        } else {
            return oppoSimulationStrategy.think(board);
        }
    }
}
