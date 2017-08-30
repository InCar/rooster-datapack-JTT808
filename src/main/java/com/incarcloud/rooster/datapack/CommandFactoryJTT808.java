package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.gather.cmd.CommandType;
import com.incarcloud.rooster.util.JTT808DataPackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

/**
 * JTT808命令工厂实现
 *
 * @author Aaric, created on 2017-08-25T10:29.
 * @since 2.0
 */
public class CommandFactoryJTT808 implements CommandFactory {

    static {
        /**
         * 声明数据包版本与解析器类关系
         */
        CommandFacotryManager.registerCommandFacotry(DataParserJTT808.PROTOCOL_PREFIX + DataParserJTT808.PROTOCOL_VERSION, CommandFactoryJTT808.class);
    }

    @Override
    public ByteBuf createCommand(CommandType type, Object... args) {
        // 基本验证，必须有参数，第一个为终端手机号，即设备号
        if(null == args && 0 < args.length) {
            throw new IllegalArgumentException("args is null");
        }

        // 初始化List容器，装载【消息头+消息体】
        List<Byte> byteList = new ArrayList<>();

        // 预留回复命令字位置
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        // 预留消息长度位置
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        // 设置终端手机号(6个字节BCD码)，5~10
        // 第一个参数为终端手机号，即设备号，验证字符串长度为12
        if(args[0] instanceof String && 12 == ((String) args[0]).length()) {
            String deviceId = (String) args[0];
            byteList.addAll(JTT808DataPackUtil.getBCDByteList(deviceId));
        } else {
            // 验证设备号不通过
            throw new IllegalArgumentException("deviceId is error");
        }

        // 设置消息流水号，默认流水号0x00
        byteList.add((byte) 0x00);
        byteList.add((byte) 0x00);
        /*====================begin-判断msgId回复消息-begin====================*/
        // 消息长度
        int msgLength = 0;

        // 判断命令类型
        switch (type) {
//            case 0x8001:
//                /**
//                 * 平台通用应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8001 - 平台通用应答");
//                break;
            case REISSUE_PACK:
                /* 补传分包请求 */
                System.out.println("## 0x8003 - 补传分包请求");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-原始消息流水号(msgSeq:int)
                 *   2-重传包总数(msgTotal:int)
                 *   3-重传包 ID 列表(msgIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x80);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 原始消息流水号
                int msgSeq = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(msgSeq));
                // 2.1 重传包总数
                int msgTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(msgTotal));
                // 2.1 重传包 ID 列表
                int[] msgIds = (int[]) args[3];
                for (int i = 0; i < msgTotal; i++) {
                    byteList.add(JTT808DataPackUtil.getIntegerByte(msgIds[i]));
                }

                // 3.设置消息长度
                msgLength = 2 + msgTotal;
                break;
//            case 0x8100:
//                /**
//                 * 终端注册应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8100 - 终端注册应答");
//                break;
            case SET_PARAMS:
                /* 设置终端参数 */
                System.out.println("## 0x8103 - 设置终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-参数总数(paramTotal:int)
                 *   2-参数项列表(paramListBytes: byte[]，按照表 11 终端参数项数据格式传入字节数组)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 参数总数
                int paramTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(paramTotal));
                // 2.2 参数项列表
                byte[] paramListBytes = (byte[]) args[2];
                for (int i = 0; i < paramListBytes.length; i++) {
                    byteList.add(paramListBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 1 + paramListBytes.length;
                break;
            case QUERY_ALL_PARAMS:
                /* 查询终端参数 */
                System.out.println("## 0x8104 - 查询终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x04);

                // 2.消息体(空)

                // 3.设置消息长度
                msgLength = 0;
                break;
            case TERMINAL_CONTROL:
                /* 终端控制 */
                System.out.println("## 0x8105 - 终端控制");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-命令字(commandId:int)
                 *   2-命令参数(commandArgs:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x05);

                // 2.消息体
                // 2.1 命令字
                int commandId = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(commandId));
                // 2.2 命令参数
                String commandArgs = (String) args[2];
                if(null != commandArgs && 0 < commandArgs.length()) {
                    byte[] commandArgsBytes = commandArgs.getBytes();
                    for (int i = 0; i < commandArgsBytes.length; i++) {
                        byteList.add(commandArgsBytes[i]);
                    }
                }

                // 3.设置消息长度
                msgLength = 1 + commandArgs.length();
                break;
            case QUERY_CUSTOM_PARAMS:
                /* 查询指定终端参数 */
                System.out.println("## 0x8106 - 查询指定终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x06);

                // 2.消息体
                // 2.1 参数总数
                paramTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(paramTotal));
                // 2.2 参数 ID 列表
                int[] params = (int[]) args[2];
                for (int i = 0; i < params.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(params[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * paramTotal);
                break;
//            case 0x8107:
//                /* 查询终端属性 */
//                System.out.println("## 0x8107 - 查询终端属性");
//                break;
//            case 0x8108:
//                /* 下发终端升级包 */
//                System.out.println("## 0x8108 - 下发终端升级包");
//                break;
//            case 0x8201:
//                /* 位置信息查询 */
//                System.out.println("## 0x8201 - 位置信息查询");
//                break;
//            case 0x8202:
//                /* 临时位置跟踪控制 */
//                System.out.println("## 0x8202 - 临时位置跟踪控制");
//                break;
//            case 0x8203:
//                /* 人工确认报警消息 */
//                System.out.println("## 0x8203 - 人工确认报警消息");
//                break;
//            case 0x8300:
//                /* 文本信息下发 */
//                System.out.println("## 0x8300 - 文本信息下发");
//                break;
//            case 0x8301:
//                /* 事件设置 */
//                System.out.println("## 0x8301 - 事件设置");
//                break;
//            case 0x8302:
//                /* 提问下发 */
//                System.out.println("## 0x8302 - 提问下发");
//                break;
//            case 0x8303:
//                /* 信息点播菜单设置 */
//                System.out.println("## 0x8303 - 信息点播菜单设置");
//                break;
//            case 0x8304:
//                /* 信息服务 */
//                System.out.println("## 0x8304 - 信息服务");
//                break;
//            case 0x8400:
//                /* 电话回拨 */
//                System.out.println("## 0x8400 - 电话回拨");
//                break;
//            case 0x8401:
//                /* 设置电话本 */
//                System.out.println("## 0x8401 - 设置电话本");
//                break;
//            case 0x8500:
//                /* 车辆控制 */
//                System.out.println("## 0x8500 - 车辆控制");
//                break;
//            case 0x8600:
//                /* 设置圆形区域 */
//                System.out.println("## 0x8600 - 设置圆形区域");
//                break;
//            case 0x8601:
//                /* 删除圆形区域 */
//                System.out.println("## 0x8601 - 删除圆形区域");
//                break;
//            case 0x8602:
//                /* 设置矩形区域 */
//                System.out.println("## 0x8602 - 设置矩形区域");
//                break;
//            case 0x8603:
//                /* 删除矩形区域 */
//                System.out.println("## 0x8603 - 删除矩形区域");
//                break;
//            case 0x8604:
//                /* 设置多边形区域 */
//                System.out.println("## 0x8604 - 设置多边形区域");
//                break;
//            case 0x8605:
//                /* 删除多边形区域 */
//                System.out.println("## 0x8605 - 删除多边形区域");
//                break;
//            case 0x8606:
//                /* 设置路线 */
//                System.out.println("## 0x8606 - 设置路线");
//                break;
//            case 0x8607:
//                /* 删除路线 */
//                System.out.println("## 0x8607 - 删除路线");
//                break;
//            case 0x8700:
//                /**
//                 * 行驶记录仪数据采集命令<br>
//                 * 关联GB/T 19056，暂时不予实现
//                 */
//                System.out.println("## 0x8700 - 行驶记录仪数据采集命令");
//                break;
//            case 0x8701:
//                /**
//                 * 行驶记录仪参数下传命令<br>
//                 * 关联GB/T 19056，暂时不予实现
//                 */
//                System.out.println("## 0x8701 - 行驶记录仪参数下传命令");
//                break;
//            case 0x8702:
//                /* 上报驾驶员身份信息请求 */
//                System.out.println("## 0x8702 - 上报驾驶员身份信息请求");
//                break;
//            case 0x8800:
//                /**
//                 * 多媒体数据上传应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8800 - 多媒体数据上传应答");
//                break;
//            case 0x8801:
//                /* 摄像头立即拍摄命令 */
//                System.out.println("## 0x8801 - 摄像头立即拍摄命令");
//                break;
//            case 0x8802:
//                /* 存储多媒体数据检索 */
//                System.out.println("## 0x0802 - 存储多媒体数据检索");
//                break;
//            case 0x8803:
//                /* 存储多媒体数据上传 */
//                System.out.println("## 0x8803 - 存储多媒体数据上传");
//                break;
//            case 0x8804:
//                /* 录音开始命令 */
//                System.out.println("## 0x8804 - 录音开始命令");
//                break;
//            case 0x8805:
//                /* 单条存储多媒体数据检索上传命令 */
//                System.out.println("## 0x8805 - 单条存储多媒体数据检索上传命令");
//                break;
//            case 0x8900:
//                /* 数据下行透传 */
//                System.out.println("## 0x8900 - 数据下行透传");
//                break;
//            case 0x8A00:
//                /* 平台 RSA 公钥 */
//                System.out.println("## 0x8A00 - 平台 RSA 公钥");
//                break;
        }
        /*====================end---判断msgId回复消息---end====================*/
        // 设置长度信息
        // 双字节，0x00-不分包，数据不加密
        int msgProps = 0x00 | msgLength;
        byteList.set(2, (byte) ((msgProps >> 8) & 0xFF));
        byteList.set(3, (byte) (msgProps & 0xFF));

        // 计算并填充校验码
        byte check = 0x00;
        for (Byte byteObject: byteList) {
            check ^= byteObject.byteValue();
        }
        byteList.add(check);

        // 转义
        List<Byte> defaultByteList = new ArrayList<>();
        defaultByteList.add((byte) 0x7E); //标识位(0x7E)
        for(Byte byteObject: byteList) {
            if(0x7D == (byteObject.byteValue() & 0xFF)) {
                // 0x7D->0x7D0x01
                defaultByteList.add((byte) 0x7D);
                defaultByteList.add((byte) 0x01);
            } else if(0x7E == (byteObject.byteValue() & 0xFF)) {
                // 0x7E->0x7D0x02
                defaultByteList.add((byte) 0x7D);
                defaultByteList.add((byte) 0x02);
            } else {
                defaultByteList.add(byteObject);
            }
        }
        defaultByteList.add((byte) 0x7E); //标识位(0x7E)

        // add to buffer
        byte[] responseBytes = new byte[defaultByteList.size()];
        for (int i = 0; i < responseBytes.length; i++) {
            responseBytes[i] = defaultByteList.get(i);
        }

        // return
        return Unpooled.wrappedBuffer(responseBytes);
    }
}
