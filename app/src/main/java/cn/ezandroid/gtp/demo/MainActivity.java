package cn.ezandroid.gtp.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.List;

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
import cn.ezandroid.lib.ezgtp.GtpUtil;
import cn.ezandroid.lib.sgf.SGFException;

public class MainActivity extends AppCompatActivity implements GtpListener {

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;

    private boolean mIsConnected;
    private boolean mIsThinking;

    private GtpHuman mBlackHuman;
    private LeelaZeroEngine mWhiteLeela;
    private GtpClient mGtpClient;

    private List<SGFReader.Move> mMoveList;

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

        findViewById(R.id.two_gtp).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TwoGtpActivity.class);
            startActivity(intent);
        });

        new Thread() {
            @Override
            public void run() {
                try {
                    mMoveList = SGFReader.readFromSGF(getResources().openRawResource(R.raw.test));
                    runOnUiThread(() -> {
                        if (mMoveList != null && !mMoveList.isEmpty()) {
                            for (SGFReader.Move move : mMoveList) {
                                Stone stone = new Stone();
                                stone.color = move.mIsBlack ? StoneColor.BLACK : StoneColor.WHITE;
                                stone.intersection = new Intersection(move.mPosition.x, move.mPosition.y);

                                mBoardView.addStone(stone);
                                mBoardView.setHighlightIntersection(null);
                                mBoardView.setHighlightStone(stone);
                            }

                            mIsCurrentBlack = !mMoveList.get(mMoveList.size() - 1).mIsBlack;
                        }

                        mBlackHuman = new GtpHuman();
                        mWhiteLeela = new LeelaZeroEngine(MainActivity.this);
                        mGtpClient = new GtpClient(mBlackHuman, mWhiteLeela);
                        mGtpClient.setGtpListener(MainActivity.this);
                        mGtpClient.start();
                    });
                } catch (IOException | SGFException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onStart(boolean isSuccess, boolean isBlack) {
        if (!isBlack) {
            mIsConnected = isSuccess;

            if (isSuccess) {
                mWhiteLeela.setBoardSize(19);
                mWhiteLeela.setKomi(7.5f);
                mWhiteLeela.timeSettings(5);

                if (mMoveList != null && !mMoveList.isEmpty()) {
                    for (SGFReader.Move move : mMoveList) {
                        mWhiteLeela.playMove(new Point(move.mPosition.x, move.mPosition.y), move.mIsBlack);
                    }
                    mWhiteLeela.showBoard();
                }
            }
        } else {
            if (isSuccess) {
                mBlackHuman.setBoardSize(19);
                mBlackHuman.setKomi(7.5f);
                mBlackHuman.timeSettings(5);

                if (mMoveList != null && !mMoveList.isEmpty()) {
                    for (SGFReader.Move move : mMoveList) {
                        mBlackHuman.playMove(new Point(move.mPosition.x, move.mPosition.y), move.mIsBlack);
                    }
                    mBlackHuman.showBoard();
                }
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
    protected void onDestroy() {
        super.onDestroy();
        mGtpClient.stop();
    }
}
