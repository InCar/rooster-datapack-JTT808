package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.gather.cmd.CommandType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;
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
        // 初始化
        Object[] args;
        CommandFactory commandFactory = new CommandFactoryJTT808();

        // 0x8003 - 补传分包请求
        args = new Object[] {"013300312707", 2, 2, new int[]{1, 2}};
        ByteBuf commandBuffer = commandFactory.createCommand(CommandType.REISSUE_PACK, args);

        // 打印数据包，并释放buffer
        System.out.println(ByteBufUtil.hexDump(commandBuffer).toUpperCase());
        ReferenceCountUtil.release(commandBuffer);
    }
}
