package cn.ezandroid.gtp.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import cn.ezandroid.lib.board.BoardView;
import cn.ezandroid.lib.board.Intersection;
import cn.ezandroid.lib.board.Stone;
import cn.ezandroid.lib.board.StoneColor;
import cn.ezandroid.lib.board.sound.SoundManager;
import cn.ezandroid.lib.board.theme.GoTheme;
import cn.ezandroid.lib.board.theme.WoodTheme;
import cn.ezandroid.lib.ezgtp.GtpGame;
import cn.ezandroid.lib.ezgtp.GtpHuman;
import cn.ezandroid.lib.ezgtp.GtpListener;
import cn.ezandroid.lib.ezgtp.GtpLogListener;
import cn.ezandroid.lib.ezgtp.GtpLogQueue;
import cn.ezandroid.lib.ezgtp.GtpUtil;

public class MainActivity extends AppCompatActivity implements GtpListener {

    private static final int UPDATE_LOG_DELAY = 500;

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;

    private boolean mIsConnected;
    private boolean mIsThinking;

    private LinearLayout mToolbar;

    private GtpHuman mBlackHuman;
    private LeelaZeroProgram mWhiteLeela;
    private GtpGame mGtpGame;

    private Handler mUpdateLogHandler = new Handler(Looper.getMainLooper());
    private long mLastUpdateLogTime;
    private Runnable mUpdateLogRunnable = new Runnable() {
        @Override
        public void run() {
            StringBuilder sb = new StringBuilder();
            synchronized (mWhiteLeela.getLogQueue()) {
                GtpLogQueue<Pair<String, Integer>> logQueue = mWhiteLeela.getLogQueue();
                for (Pair<String, Integer> log : logQueue) {
                    if (log.second == GtpLogListener.TYPE_REQUEST) {
                        sb.append("\n").append("$").append(log.first);
                    } else {
                        sb.append("\n").append(log.first);
                    }
                }
            }
            Log.e("Main", sb.toString());
            mLastUpdateLogTime = System.currentTimeMillis();
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardView = findViewById(R.id.board);
        mBoardView.setOnTouchListener((v, event) -> {
            Intersection nearest = mBoardView.getNearestIntersection(event.getX(), event.getY());
            if (nearest != null && mIsConnected && !mIsThinking) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Intersection highlight = mBoardView.getHighlightIntersection();
                        if (nearest.equals(highlight)) {
                            mBlackHuman.playMove(new Point(highlight.x, highlight.y), true);

                            Stone stone = new Stone();
                            stone.color = mIsCurrentBlack ? StoneColor.BLACK : StoneColor.WHITE;
                            stone.intersection = highlight;

                            mBoardView.addStone(stone);
                            mBoardView.setHighlightIntersection(null);
                            mBoardView.setHighlightStone(stone);
                            mIsCurrentBlack = !mIsCurrentBlack;

                            SoundManager.getInstance().playSound(MainActivity.this,
                                    mBoardView.getGoTheme().mSoundEffect.mMove);

                            mIsThinking = true;
                            return false;
                        } else {
                            mBoardView.setHighlightIntersection(nearest);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mBoardView.getHighlightIntersection() != null) {
                            mBoardView.setHighlightIntersection(nearest);
                        }
                        break;
                }
            }
            return true;
        });
        mBoardView.setGoTheme(new WoodTheme(new GoTheme.DrawableCache(this, (int) Runtime.getRuntime().maxMemory() / 32)));

        mToolbar = findViewById(R.id.tool_bar);

        findViewById(R.id.two_gtp).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TwoGtpActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.show_sgf).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ShowSgfActivity.class);
            startActivity(intent);
        });

        mBlackHuman = new GtpHuman();
        mWhiteLeela = new LeelaZeroProgram(MainActivity.this);
        mWhiteLeela.setGtpLogListener((gtpClient, logQueue) -> updateLog());
        mGtpGame = new GtpGame(mBlackHuman, mWhiteLeela);
        mGtpGame.setGtpListener(MainActivity.this);
        mGtpGame.start();

        updateLayoutOrientation(getResources().getConfiguration());
    }

    private void updateLog() {
        mUpdateLogHandler.removeCallbacks(mUpdateLogRunnable);
        if (System.currentTimeMillis() - mLastUpdateLogTime < UPDATE_LOG_DELAY) {
            mUpdateLogHandler.postDelayed(mUpdateLogRunnable, System.currentTimeMillis() - mLastUpdateLogTime);
        } else {
            mUpdateLogHandler.post(mUpdateLogRunnable);
        }
    }

    public void onStart(boolean isSuccess, boolean isBlack) {
        if (!isBlack) {
            mIsConnected = isSuccess;

            if (isSuccess) {
                mWhiteLeela.setBoardSize(19);
                mWhiteLeela.setKomi(7.5f);
                mWhiteLeela.timeSettings(2);
            }
        } else {
            if (isSuccess) {
                mBlackHuman.setBoardSize(19);
                mBlackHuman.setKomi(7.5f);
                mBlackHuman.timeSettings(2);
            }
        }
    }

    @Override
    public void onGenMove(Point move, boolean isBlack) {
        if (!isBlack) {
            if (move.x == GtpUtil.PASS_POS || move.x == GtpUtil.RESIGN_POS) {
                return;
            }
            runOnUiThread(() -> {
                Stone stone = new Stone();
                stone.color = mIsCurrentBlack ? StoneColor.BLACK : StoneColor.WHITE;
                stone.intersection = new Intersection(move.x, move.y);

                mBoardView.addStone(stone);
                mBoardView.setHighlightIntersection(null);
                mBoardView.setHighlightStone(stone);
                mIsCurrentBlack = !mIsCurrentBlack;

                SoundManager.getInstance().playSound(MainActivity.this,
                        mBoardView.getGoTheme().mSoundEffect.mMove);

                mIsThinking = false;
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayoutOrientation(newConfig);
    }

    private void updateLayoutOrientation(Configuration configuration) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.addRule(RelativeLayout.BELOW, 0);
            params.addRule(RelativeLayout.RIGHT_OF, R.id.board);
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            params.addRule(RelativeLayout.RIGHT_OF, 0);
            params.addRule(RelativeLayout.BELOW, R.id.board);
        }
        mToolbar.setLayoutParams(params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGtpGame.stop();
    }
}
