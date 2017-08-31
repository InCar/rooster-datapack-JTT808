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
        /*args = new Object[] {"013300312707", 1, 2, new int[]{1, 2}, new String[]{"天气", "新闻"}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_INFO_DEMAND_MENU, args);*/

        // 0x8304 - 信息服务
        /*args = new Object[] {"013300312707", 1, "吸烟有害健康"};
        commandBuffer = commandFactory.createCommand(CommandType.INFO_SERVICE, args);*/

        // 0x8400 - 电话回拨
        /*args = new Object[] {"013300312707", 1, "16688889999"};
        commandBuffer = commandFactory.createCommand(CommandType.PHONE_DIAL, args);*/

        // 0x8401 - 设置电话本
        /*args = new Object[] {"013300312707", 1, 2, new int[]{2,3}, new String[]{"110", "119"}, new String[]{"报警", "消防"}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_PHONE_LIST, args);*/

        // 0x8500 - 车辆控制
        /*args = new Object[] {"013300312707", 1};
        commandBuffer = commandFactory.createCommand(CommandType.VEHICLE_CONTROL, args);*/

        // 0x8600 - 设置圆形区域
        /*args = new Object[] {"013300312707", 1, 1, new byte[]{0x00, 0x01, 0x02}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_AREA_CIRCULAR, args);*/

        // 0x8601 - 删除圆形区域
        /*args = new Object[] {"013300312707", 2, new int[]{1, 2}};
        commandBuffer = commandFactory.createCommand(CommandType.DELETE_AREA_CIRCULAR, args);*/

        // 0x8602 - 设置矩形区域
        /*args = new Object[] {"013300312707", 1, 1, new byte[]{0x00, 0x01, 0x02}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_AREA_RECTANGLE, args);*/

        // 0x8603 - 删除矩形区域
        /*args = new Object[] {"013300312707", 2, new int[]{1, 2}};
        commandBuffer = commandFactory.createCommand(CommandType.DELETE_AREA_RECTANGLE, args);*/

        // 0x8604 - 设置多边形区域
        /*args = new Object[] {"013300312707", 1, 1, new Date(), new Date(), 90, 100, 2, new double[]{100.0, 110.0}, new double[]{30.0, 33.0}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_AREA_POLYGON, args);*/

        // 0x8605 - 删除多边形区域
        /*args = new Object[] {"013300312707", 2, new int[]{1, 2}};
        commandBuffer = commandFactory.createCommand(CommandType.DELETE_AREA_POLYGON, args);*/

        // 0x8606 - 设置路线
        /*args = new Object[] {"013300312707", 1, 0, new Date(), new Date(), 1, new byte[]{0x00, 0x01, 0x02}};
        commandBuffer = commandFactory.createCommand(CommandType.SET_LINE, args);*/

        // 0x8607 - 删除路线
        /*args = new Object[] {"013300312707", 2, new int[]{1, 2}};
        commandBuffer = commandFactory.createCommand(CommandType.DELETE_LINE, args);*/

        // 0x8702 - 上报驾驶员身份信息请求
        /*args = new Object[] {"013300312707"};
        commandBuffer = commandFactory.createCommand(CommandType.SEND_DRIVER_ID, args);*/

        // 0x8801 - 摄像头立即拍摄命令
        /*args = new Object[] {"013300312707", 1, 1, 0, 0, 4, 8, 128, 64, 64, 128};
        commandBuffer = commandFactory.createCommand(CommandType.TAKE_PHOTO, args);*/

        // 0x0802 - 存储多媒体数据检索
        /*args = new Object[] {"013300312707", 0, 0, 0, new Date(), new Date()};
        commandBuffer = commandFactory.createCommand(CommandType.QUERY_MEDIA, args);*/

        // 0x8803 - 存储多媒体数据上传
        /*args = new Object[] {"013300312707", 0, 0, 0, new Date(), new Date(), 1};
        commandBuffer = commandFactory.createCommand(CommandType.UPDATE_MEDIA, args);*/

        // 0x8804 - 录音开始命令
        /*args = new Object[] {"013300312707", 1, 10, 0, 1};
        commandBuffer = commandFactory.createCommand(CommandType.SOUND_RECORDING, args);*/

        // 0x8805 - 单条存储多媒体数据检索上传命令
        /*args = new Object[] {"013300312707", 1, 1};
        commandBuffer = commandFactory.createCommand(CommandType.UPDATE_SINGLE_MEDIA, args);*/

        // 0x8A00 - 平台 RSA 公钥
        args = new Object[] {"013300312707", 1, new byte[128]};
        commandBuffer = commandFactory.createCommand(CommandType.RSA, args);

        // 打印数据包，并释放buffer
        System.out.println(ByteBufUtil.hexDump(commandBuffer).toUpperCase());
        ReferenceCountUtil.release(commandBuffer);
    }
}
