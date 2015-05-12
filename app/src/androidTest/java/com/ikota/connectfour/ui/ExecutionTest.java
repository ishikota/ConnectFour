package com.ikota.connectfour.ui;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.ikota.connectfour.strategy.AlphaBetaCut;
import com.ikota.connectfour.strategy.BaseMCTS;
import com.ikota.connectfour.strategy.BaseStrategy;
import com.ikota.connectfour.strategy.IterativeDeepening;
import com.ikota.connectfour.strategy.MiniMax;
import com.ikota.connectfour.strategy.TunedMCTS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by kota on 2015/02/19.
 * Measure execution time of each strategy.
 * 1. Minimax
 * 2. AlphaBeta
 * 3. IterativeDeepening
 * 4. BaseMCTS
 * Test result is shown on the log console in the end of deprecate_menu_activity.
 */
public class ExecutionTest extends InstrumentationTestCase{
    private Context mContext;
    private final String CHARSET = "UTF-8";
    private final String MINIMAX_FILE_NAME = "minimax.txt";
    private final String ALPHA_BETA_FILE_NAME = "alphabetacut.txt";
    private final String ITERATIVE_FILE_NAME = "iterative_deepening.txt";
    private final String BASEMCTS_FILE_NAME = "basemcts.txt";
    private final String TUNED_MCTS_FILE_NAME = "tuned_mcts.txt";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getInstrumentation().getTargetContext().getApplicationContext();
    }

    public void testExeTime() {
        for (int i=4;i<5;i++) {
            measureExeTime(i);
        }
    }

    public void measureExeTime(int index) {
        PrintWriter writer = null;
        try {
            String divider = "--------------------------------------------";
            String file_name = getFileName(index);
            OutputStream out = mContext.openFileOutput(file_name, Context.MODE_PRIVATE);
            writer = new PrintWriter(new OutputStreamWriter(out, CHARSET));
            // write header
            writer.println(divider);
            writer.println("Start "+file_name.substring(0, file_name.length()-4)+" Execution TIME deprecate_menu_activity");

            Board board = new Board(mContext, 4, 7, 6, false);
            BaseStrategy s = getStrategy(index);
            for (int d = 1; d <= 10; d++) {
                setParameter(index, s, d);
                long st = System.currentTimeMillis();
                s.think(board);
                long et = System.currentTimeMillis();
                double exe_time = (et - st) / 1000.0;
                writer.println("depth = " + d + " : " + String.valueOf(exe_time) + "(s)");
                if (exe_time >= 15) break;
            }
            // write footer
            writer.println("Finish Execution TIME deprecate_menu_activity");
            writer.println(divider);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer!=null) writer.close();
        }
    }

    private String getFileName(int index) {
        switch (index) {
            case 0: return MINIMAX_FILE_NAME;
            case 1: return ALPHA_BETA_FILE_NAME;
            case 2: return ITERATIVE_FILE_NAME;
            case 3: return BASEMCTS_FILE_NAME;
            case 4: return TUNED_MCTS_FILE_NAME;
            default: return null;
        }
    }

    private BaseStrategy getStrategy(int index) {
        switch (index) {
            case 0: return new MiniMax(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            case 1: return new AlphaBetaCut(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            case 2: return new IterativeDeepening(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            case 3: return new BaseMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            case 4: return new TunedMCTS(Board.FIRST_PLAYER, Board.SECOND_PLAYER);
            default: return null;
        }
    }

    private void setParameter(int index, BaseStrategy s, int d) {
        switch (index) {
            case 0: ((MiniMax)s).setParameter(d); break;
            case 1: ((AlphaBetaCut)s).setParameter(d); break;
            case 2: ((IterativeDeepening)s).setParameter(IterativeDeepening.MODE_DEPTH_LIMIT, d); break;
            // simulation time range is 500 ~ 5000
            case 3:  ((BaseMCTS)s).setParameter(BaseMCTS.SIMULATION_NUM_LIMIT,d*500); break;
            case 4:  ((TunedMCTS)s).setParameter(BaseMCTS.SIMULATION_NUM_LIMIT, d*500, 1);
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        logResult(MINIMAX_FILE_NAME);
//        logResult(ALPHA_BETA_FILE_NAME);
//        logResult(ITERATIVE_FILE_NAME);
//        logResult(BASEMCTS_FILE_NAME);
        logResult(TUNED_MCTS_FILE_NAME);
    }

    private void logResult(String file_name) throws IOException{
        InputStream is = mContext.openFileInput(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, CHARSET));
        String l;
        while ((l=reader.readLine())!=null) {
            Log.i("EXE_TIME TEST RESULT", l);
        }
        reader.close();
    }

}
