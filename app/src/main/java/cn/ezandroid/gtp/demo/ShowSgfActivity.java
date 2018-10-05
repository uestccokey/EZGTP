package cn.ezandroid.gtp.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;

import java.io.IOException;
import java.util.ArrayList;

import cn.ezandroid.lib.board.BoardView;
import cn.ezandroid.lib.board.Intersection;
import cn.ezandroid.lib.board.Stone;
import cn.ezandroid.lib.board.StoneColor;
import cn.ezandroid.lib.board.theme.GoTheme;
import cn.ezandroid.lib.board.theme.WoodTheme;
import cn.ezandroid.lib.sgf.SGFException;

/**
 * ShowSgfActivity
 *
 * @author like
 * @date 2018-10-05
 */
public class ShowSgfActivity extends AppCompatActivity {

    private BoardView mBoardView;

    private ArrayList<SGFReader.Move> mMoveList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sgf);

        mBoardView = findViewById(R.id.board);
        mBoardView.setGoTheme(new WoodTheme(new GoTheme.DrawableCache(this, (int) Runtime.getRuntime().maxMemory() / 32)));

        mBoardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                mBoardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            mMoveList.addAll(SGFReader.readFromSGF(getResources().openRawResource(R.raw.test)));
                            runOnUiThread(() -> {
                                if (!mMoveList.isEmpty()) {
                                    for (SGFReader.Move move : mMoveList) {
                                        Stone stone = new Stone();
                                        stone.color = move.mIsBlack ? StoneColor.BLACK : StoneColor.WHITE;
                                        stone.intersection = new Intersection(move.mPosition.x, move.mPosition.y);

                                        mBoardView.addStone(stone);
                                        mBoardView.setHighlightIntersection(null);
                                        mBoardView.setHighlightStone(stone);
                                    }
                                }
                            });
                        } catch (IOException | SGFException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }
}
