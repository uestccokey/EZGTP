package cn.ezandroid.gtp.demo;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import cn.ezandroid.lib.board.BoardView;
import cn.ezandroid.lib.board.Intersection;
import cn.ezandroid.lib.board.Stone;
import cn.ezandroid.lib.board.StoneColor;
import cn.ezandroid.lib.board.sound.SoundManager;
import cn.ezandroid.lib.board.theme.GoTheme;
import cn.ezandroid.lib.board.theme.WoodTheme;
import cn.ezandroid.lib.ezgtp.GtpGame;
import cn.ezandroid.lib.ezgtp.GtpListener;
import cn.ezandroid.lib.ezgtp.GtpUtil;

/**
 * TwoGtpActivity
 *
 * @author like
 * @date 2018-10-01
 */
public class TwoGtpActivity extends AppCompatActivity implements GtpListener {

    private BoardView mBoardView;
    private boolean mIsCurrentBlack = true;

    private LeelaZeroProgram mBlackLeela;
    private LeelaZeroProgram mWhiteLeela;
    private GtpGame mGtpGame;

    private Button mPauseButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_gtp);

        mBoardView = findViewById(R.id.board);
        mBoardView.setGoTheme(new WoodTheme(new GoTheme.DrawableCache(this, (int) Runtime.getRuntime().maxMemory() / 32)));

        mPauseButton = findViewById(R.id.pause);
        mPauseButton.setOnClickListener(v -> {
            if (mGtpGame.isPause()) {
                mGtpGame.resume();
                mPauseButton.setText("Pause");
            } else {
                mGtpGame.pause();
                mPauseButton.setText("Resume");
            }
        });

        mBlackLeela = new LeelaZeroProgram(this);
        mWhiteLeela = new LeelaZeroProgram(this);
        mGtpGame = new GtpGame(mBlackLeela, mWhiteLeela);
        mGtpGame.setGtpListener(this);
        mGtpGame.start();
    }

    @Override
    public void onStart(boolean isSuccess, boolean isBlack) {
        if (!isBlack) {
            if (isSuccess) {
                mWhiteLeela.setBoardSize(19);
                mWhiteLeela.setKomi(7.5f);
                mWhiteLeela.timeSettings(2);
            }
        } else {
            if (isSuccess) {
                mBlackLeela.setBoardSize(19);
                mBlackLeela.setKomi(7.5f);
                mBlackLeela.timeSettings(2);
            }
        }
    }

    @Override
    public void onGenMove(Point move, boolean isBlack) {
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

            SoundManager.getInstance().playSound(TwoGtpActivity.this,
                    mBoardView.getGoTheme().mSoundEffect.mMove);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGtpGame.stop();
    }
}
