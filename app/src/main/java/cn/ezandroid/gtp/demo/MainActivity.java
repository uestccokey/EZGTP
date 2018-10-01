package cn.ezandroid.gtp.demo;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import cn.ezandroid.lib.board.BoardView;
import cn.ezandroid.lib.board.Intersection;
import cn.ezandroid.lib.board.Stone;
import cn.ezandroid.lib.board.StoneColor;
import cn.ezandroid.lib.board.sound.SoundManager;
import cn.ezandroid.lib.board.theme.GoTheme;
import cn.ezandroid.lib.board.theme.WoodTheme;
import cn.ezandroid.lib.ezgtp.GtpEngine;
import cn.ezandroid.lib.ezgtp.GtpUtil;

public class MainActivity extends AppCompatActivity {

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;
    private LeelaZeroEngine mLeelaZeroEngine;
    private boolean mIsConnected;
    private boolean mIsThinking;

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
                            if (GtpEngine.success(mLeelaZeroEngine.playMove(new Point(highlight.x, highlight.y), mIsCurrentBlack))) {
                                Stone stone = new Stone();
                                stone.color = mIsCurrentBlack ? StoneColor.BLACK : StoneColor.WHITE;
                                stone.intersection = highlight;

                                mBoardView.addStone(stone);
                                mBoardView.setHighlightIntersection(null);
                                mBoardView.setHighlightStone(stone);
                                mIsCurrentBlack = !mIsCurrentBlack;

                                SoundManager.getInstance().playSound(MainActivity.this,
                                        mBoardView.getGoTheme().mSoundEffect.mMove);

                                mLeelaZeroEngine.showBoard();

                                mIsThinking = true;

                                new Thread() {
                                    @Override
                                    public void run() {
                                        Point point = mLeelaZeroEngine.genMove(mIsCurrentBlack);

                                        runOnUiThread(
                                                () -> {
                                                    if (point.x != GtpUtil.PASS_POS
                                                            && point.x != GtpUtil.RESIGN_POS) {
                                                        Stone stone1 = new Stone();
                                                        stone1.color = mIsCurrentBlack ? StoneColor.BLACK : StoneColor.WHITE;
                                                        stone1.intersection = new Intersection(point.x, point.y);

                                                        mBoardView.addStone(stone1);
                                                        mBoardView.setHighlightIntersection(null);
                                                        mBoardView.setHighlightStone(stone1);
                                                        mIsCurrentBlack = !mIsCurrentBlack;

                                                        SoundManager.getInstance().playSound(MainActivity.this,
                                                                mBoardView.getGoTheme().mSoundEffect.mMove);
                                                    }
                                                    mLeelaZeroEngine.showBoard();

                                                    mIsThinking = false;
                                                }
                                        );
                                    }
                                }.start();
                            }
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

        mLeelaZeroEngine = new LeelaZeroEngine(this);
        new Thread() {
            @Override
            public void run() {
                mIsConnected = mLeelaZeroEngine.connect();
                if (mIsConnected) {
                    mLeelaZeroEngine.setBoardSize(19);
                    mLeelaZeroEngine.setKomi(7.5f);
                    mLeelaZeroEngine.timeSettings(5);
                }
            }
        }.start();
    }
}
