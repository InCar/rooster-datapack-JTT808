package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.gather.cmd.CommandType;
import org.junit.Test;

/**
 * CommandFactoryJTT808Test
 *
 * @author Aaric, created on 2017-08-29T17:57.
 * @since 2.0
 */
public class CommandFactoryJTT808Test {

    @Test
    public void testCreateCommand() {
        CommandFactory commandFactory = new CommandFactoryJTT808();
        // 0x8003 - 补传分包请求
        commandFactory.createCommand(CommandType.REISSUE_PACK, new Object[]{});
    }
}
