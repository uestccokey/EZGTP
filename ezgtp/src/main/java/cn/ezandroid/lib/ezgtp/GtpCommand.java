package cn.ezandroid.lib.ezgtp;

/**
 * Gtp命令枚举
 * <p>
 * http://www.lysator.liu.se/~gunnar/gtp/
 *
 * @author like
 * @date 2018-06-07
 */
public enum GtpCommand {

    NAME("name"), // 引擎名称
    VERSION("version"), // 引擎版本
    LIST_COMMANDS("list_commands"), // 引擎支持的命令列表

    BOARD_SIZE("boardsize"), // 设置棋盘大小
    KOMI("komi"), // 设置贴目
    TIME_SETTINGS("time_settings"), // 设置时限

    PLAY("play"), // 落子
    GEN_MOVE("genmove"), // 命令AI落子
    UNDO("undo"), // 悔棋

    SHOW_BOARD("showboard"), // 显示棋盘
    CLEAR_BOARD("clear_board"), // 清空棋盘

    FINAL_SCORE("final_score"); // 点目

    private String mCommand;

    GtpCommand(String command) {
        mCommand = command;
    }

    public String cmd(String... params) {
        StringBuilder builder = new StringBuilder();
        builder.append(mCommand);
        builder.append(" ");
        for (String param : params) {
            builder.append(param);
            builder.append(" ");
        }
        return builder.toString().trim();
    }
}
