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
import cn.ezandroid.lib.ezgtp.GtpClient;
import cn.ezandroid.lib.ezgtp.GtpHuman;
import cn.ezandroid.lib.ezgtp.GtpListener;

public class MainActivity extends AppCompatActivity implements GtpListener {

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;

    private boolean mIsConnected;
    private boolean mIsThinking;

    private GtpHuman mGtpHuman;
    private LeelaZeroEngine mLeelaZeroEngine;
    private GtpClient mGtpClient;

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
                            mGtpHuman.setWaitPlayMove(new Point(highlight.x, highlight.y));

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

        mGtpHuman = new GtpHuman();
        mLeelaZeroEngine = new LeelaZeroEngine(this);
        mGtpClient = new GtpClient(mGtpHuman, mLeelaZeroEngine);
        mGtpClient.setGtpListener(this);
        mGtpClient.start();
    }

    @Override
    public void onConnected(boolean isSuccess, boolean isBlack) {
        if (!isBlack) {
            mIsConnected = isSuccess;
        }
    }

    @Override
    public void onGenMove(Point move, boolean isBlack) {
        if (!isBlack) {
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
}
