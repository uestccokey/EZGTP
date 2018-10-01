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

public class MainActivity extends AppCompatActivity {

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;
    private LeelaZeroEngine mLeelaZeroEngine;
    private boolean mIsConnected;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardView = findViewById(R.id.board);
        mBoardView.setOnTouchListener((v, event) -> {
            Intersection nearest = mBoardView.getNearestIntersection(event.getX(), event.getY());
            if (nearest != null && mIsConnected) {
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
                                mIsCurrentBlack = !mIsCurrentBlack;

                                SoundManager.getInstance().playSound(MainActivity.this, mBoardView.getGoTheme().mSoundEffect.mMove);

                                mLeelaZeroEngine.showBoard();
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
            }
        }.start();
    }
}
