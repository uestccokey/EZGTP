package cn.ezandroid.gtp.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.ezandroid.lib.gtp.GtpUtil;
import cn.ezandroid.lib.sgf.Point;
import cn.ezandroid.lib.sgf.SGFException;
import cn.ezandroid.lib.sgf.SGFGame;
import cn.ezandroid.lib.sgf.SGFLeaf;
import cn.ezandroid.lib.sgf.SGFLoader;
import cn.ezandroid.lib.sgf.SGFTree;
import cn.ezandroid.lib.sgf.tokens.AddBlackToken;
import cn.ezandroid.lib.sgf.tokens.AddEmptyToken;
import cn.ezandroid.lib.sgf.tokens.AddWhiteToken;
import cn.ezandroid.lib.sgf.tokens.BlackNameToken;
import cn.ezandroid.lib.sgf.tokens.InfoToken;
import cn.ezandroid.lib.sgf.tokens.KomiToken;
import cn.ezandroid.lib.sgf.tokens.MoveToken;
import cn.ezandroid.lib.sgf.tokens.PlacementListToken;
import cn.ezandroid.lib.sgf.tokens.SGFToken;
import cn.ezandroid.lib.sgf.tokens.SizeToken;
import cn.ezandroid.lib.sgf.tokens.TextToken;
import cn.ezandroid.lib.sgf.tokens.WhiteNameToken;

/**
 * SGF读取器
 *
 * @author like
 * @date 2018-02-10
 */
public class SGFReader {

    public static class Move implements Serializable {
        private static final long serialVersionUID = 42L;

        Point mPosition;
        boolean mIsBlack;
    }

    public static List<Move> readFromSGF(InputStream stream) throws IOException, SGFException {
        SGFLoader loader = new SGFLoader();
        SGFGame game = loader.load(stream);

        parseSGFGameInfo(game);

        List<Move> moveSequence = new ArrayList<>();
        extractMoveList(game.getTree(), moveSequence);
        return moveSequence;
    }

    private static void parseSGFGameInfo(SGFGame game) throws SGFException {
        Iterator<InfoToken> iterator = game.getInfoTokens();
        while (iterator.hasNext()) {
            InfoToken token = iterator.next();
            if (token instanceof SizeToken) {
                SizeToken sizeToken = (SizeToken) token;
                // TODO
            } else if (token instanceof KomiToken) {
                KomiToken komiToken = (KomiToken) token;
                // TODO
            } else if (token instanceof WhiteNameToken) {
                WhiteNameToken nameToken = (WhiteNameToken) token;
                // TODO
            } else if (token instanceof BlackNameToken) {
                BlackNameToken nameToken = (BlackNameToken) token;
                // TODO
            }
        }
    }

    private static void extractMoveList(SGFTree tree, List<Move> moveList) {
        Iterator<SGFTree> trees = tree.getTrees();
        Iterator<SGFLeaf> leaves = tree.getLeaves();
        Iterator<SGFToken> tokens;
        while (leaves.hasNext()) {
            SGFToken token;
            tokens = leaves.next().getTokens();

            // While a move token hasn't been found, and there are more tokens to
            // examine ... try and find a move token in this tree's leaves to add
            // to the collection of moves (moveList).
            while (tokens.hasNext()) {
                token = tokens.next();
                processToken(token, moveList);
            }
        }

        // If there are variations, use the first variation, which is
        // the entire game, without extraneous variations.
        if (trees.hasNext()) {
            extractMoveList(trees.next(), moveList);
        }
    }

    private static void processToken(SGFToken token, List<Move> moveList) {
        if (token instanceof MoveToken) {
            moveList.add(createMoveFromToken(token));
        } else if (token instanceof AddEmptyToken) {
            removeMoves((PlacementListToken) token, moveList);
        } else if (token instanceof AddBlackToken) {
            moveList.add(createMoveFromToken(token));
        } else if (token instanceof AddWhiteToken) {
            moveList.add(createMoveFromToken(token));
        } else if (token instanceof TextToken) {
            TextToken textToken = (TextToken) token;
            // TODO
        }
    }

    private static void removeMoves(PlacementListToken token, List<Move> moveList) {
        Iterator<Point> points = token.getPoints();

        List<Move> deleteMoves = new ArrayList<>();
        while (points.hasNext()) {
            Point point = points.next();
            for (Move move : moveList) {
                Point location = move.mPosition;
                if (location.x == point.x && location.y == point.y) {
                    deleteMoves.add(move);
                }
            }
        }
        moveList.removeAll(deleteMoves);
    }

    private static Move createMoveFromToken(SGFToken token) {
        MoveToken mvToken = (MoveToken) token;
        if (mvToken.isPass()) {
            Move move = new Move();
            move.mIsBlack = mvToken.isBlack();
            move.mPosition = new Point((byte) GtpUtil.PASS_POS, (byte) GtpUtil.PASS_POS);
            return move;
        }
        Move move = new Move();
        move.mIsBlack = mvToken.isBlack();
        move.mPosition = new Point((byte) (mvToken.getX() - 1), (byte) (mvToken.getY() - 1));
        return move;
    }
}
