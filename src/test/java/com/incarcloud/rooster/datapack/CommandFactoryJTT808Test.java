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
    public void testCreateCommand() throws Exception {
        // 初始化
        Object[] args;
        ByteBuf commandBuffer;
        CommandFactory commandFactory = new CommandFactoryJTT808();

        // 0x8003 - 补传分包请求
        /*args = new Object[] {"013300312707", 2, 2, new int[]{1, 2}};
        commandBuffer = commandFactory.createCommand(CommandType.REISSUE_PACK, args);*/

        // 0x8100 - 终端注册应答
        // 示例设置终端心跳发送间隔(0x0001)10s
        /*args = new Object[] {"013300312707", 1, new byte[]{0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x00, 0x0A}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_PARAMS, args);*/

        // 0x8104 - 查询终端参数
        /*args = new Object[] {"013300312707"};
        commandBuffer = commandFactory.createCommand(CommandType.QUERY_ALL_PARAMS, args);*/

        // 0x8105 - 终端控制
        /*args = new Object[] {"013300312707", 3, ""};
        commandBuffer = commandFactory.createCommand(CommandType.TERMINAL_CONTROL, args);*/

        // 0x8106 - 查询指定终端参数
        /*args = new Object[] {"013300312707", 3, new int[]{1, 2, 3}};
        commandBuffer = commandFactory.createCommand(CommandType.QUERY_CUSTOM_PARAMS, args);*/

        // 0x8107 - 查询终端属性
        /*args = new Object[] {"013300312707"};
        commandBuffer = commandFactory.createCommand(CommandType.QUERY_ALL_ATTRS, args);*/

        // 0x8108 - 下发终端升级包
        /*args = new Object[] {"013300312707", 0, new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, "V2.0.0", new byte[]{0x00, 0x00, 0x00}};
        commandBuffer = commandFactory.createCommand(CommandType.UPGRADE, args);*/

        // 0x8201 - 位置信息查询
        /*args = new Object[] {"013300312707"};
        commandBuffer = commandFactory.createCommand(CommandType.QUERY_POSITION, args);*/

        // 0x8202 - 临时位置跟踪控制
        /*args = new Object[] {"013300312707", 10, 30000};
        commandBuffer = commandFactory.createCommand(CommandType.TRACKING_POSITION, args);*/

        // 0x8203 - 人工确认报警消息
        /*args = new Object[] {"013300312707", 1, 1};
        commandBuffer = commandFactory.createCommand(CommandType.CONFIRM_ALARM, args);*/

        // 0x8300 - 文本信息下发
        /*args = new Object[] {"013300312707", 1, "Hello Driver!"};
        commandBuffer = commandFactory.createCommand(CommandType.SEND_TEXT, args);*/

        // 0x8301 - 事件设置
        /*args = new Object[] {"013300312707", 1, 2, new int[]{1, 2}, new String[]{"aa", "bb"}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_EVENT, args);*/

        // 0x8302 - 提问下发
        /*args = new Object[] {"013300312707", 1, "你喝酒吗？", new int[]{1, 2}, new String[]{"是", "否"}};
        commandBuffer = commandFactory.createCommand(CommandType.QUIZ, args);*/

        // 0x8303 - 信息点播菜单设置
        args = new Object[] {"013300312707", 1, 2, new int[]{1, 2}, new String[]{"天气", "新闻"}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_INFO_DEMAND_MENU, args);

        // 打印数据包，并释放buffer
        System.out.println(ByteBufUtil.hexDump(commandBuffer).toUpperCase());
        ReferenceCountUtil.release(commandBuffer);
    }
}
