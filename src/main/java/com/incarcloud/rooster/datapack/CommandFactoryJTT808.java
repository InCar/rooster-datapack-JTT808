package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.gather.cmd.CommandType;
import io.netty.buffer.ByteBuf;

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
        // 消息ID
        int msgId = 0x00;
        switch (msgId) {
            case 0x8001:
                /**
                 * 平台通用应答<br>
                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
                 */
                System.out.println("## 0x8001 - 平台通用应答");
                break;
            case 0x8003:
                /* 补传分包请求 */
                System.out.println("## 0x8003 - 补传分包请求");
                break;
            case 0x8100:
                /**
                 * 终端注册应答<br>
                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
                 */
                System.out.println("## 0x8100 - 终端注册应答");
                break;
            case 0x8103:
                /* 设置终端参数 */
                System.out.println("## 0x8103 - 设置终端参数");
                break;
            case 0x8104:
                /* 查询终端参数 */
                System.out.println("## 0x8104 - 查询终端参数");
                break;
            case 0x8105:
                /* 终端控制 */
                System.out.println("## 0x8105 - 终端控制");
                break;
            case 0x8106:
                /* 查询指定终端参数 */
                System.out.println("## 0x8106 - 查询指定终端参数");
                break;
            case 0x8107:
                /* 查询终端属性 */
                System.out.println("## 0x8107 - 查询终端属性");
                break;
            case 0x8108:
                /* 下发终端升级包 */
                System.out.println("## 0x8108 - 下发终端升级包");
                break;
            case 0x8201:
                /* 位置信息查询 */
                System.out.println("## 0x8201 - 位置信息查询");
                break;
            case 0x8202:
                /* 临时位置跟踪控制 */
                System.out.println("## 0x8202 - 临时位置跟踪控制");
                break;
            case 0x8203:
                /* 人工确认报警消息 */
                System.out.println("## 0x8203 - 人工确认报警消息");
                break;
            case 0x8300:
                /* 文本信息下发 */
                System.out.println("## 0x8300 - 文本信息下发");
                break;
            case 0x8301:
                /* 事件设置 */
                System.out.println("## 0x8301 - 事件设置");
                break;
            case 0x8302:
                /* 提问下发 */
                System.out.println("## 0x8302 - 提问下发");
                break;
            case 0x8303:
                /* 信息点播菜单设置 */
                System.out.println("## 0x8303 - 信息点播菜单设置");
                break;
            case 0x8304:
                /* 信息服务 */
                System.out.println("## 0x8304 - 信息服务");
                break;
            case 0x8400:
                /* 电话回拨 */
                System.out.println("## 0x8400 - 电话回拨");
                break;
            case 0x8401:
                /* 设置电话本 */
                System.out.println("## 0x8401 - 设置电话本");
                break;
            case 0x8500:
                /* 车辆控制 */
                System.out.println("## 0x8500 - 车辆控制");
                break;
            case 0x8600:
                /* 设置圆形区域 */
                System.out.println("## 0x8600 - 设置圆形区域");
                break;
            case 0x8601:
                /* 删除圆形区域 */
                System.out.println("## 0x8601 - 删除圆形区域");
                break;
            case 0x8602:
                /* 设置矩形区域 */
                System.out.println("## 0x8602 - 设置矩形区域");
                break;
            case 0x8603:
                /* 删除矩形区域 */
                System.out.println("## 0x8603 - 删除矩形区域");
                break;
            case 0x8604:
                /* 设置多边形区域 */
                System.out.println("## 0x8604 - 设置多边形区域");
                break;
            case 0x8605:
                /* 删除多边形区域 */
                System.out.println("## 0x8605 - 删除多边形区域");
                break;
            case 0x8606:
                /* 设置路线 */
                System.out.println("## 0x8606 - 设置路线");
                break;
            case 0x8607:
                /* 删除路线 */
                System.out.println("## 0x8607 - 删除路线");
                break;
            case 0x8700:
                /**
                 * 行驶记录仪数据采集命令<br>
                 * 关联GB/T 19056，暂时不予实现
                 */
                System.out.println("## 0x8700 - 行驶记录仪数据采集命令");
                break;
            case 0x8701:
                /**
                 * 行驶记录仪参数下传命令<br>
                 * 关联GB/T 19056，暂时不予实现
                 */
                System.out.println("## 0x8701 - 行驶记录仪参数下传命令");
                break;
            case 0x8702:
                /* 上报驾驶员身份信息请求 */
                System.out.println("## 0x8702 - 上报驾驶员身份信息请求");
                break;
            case 0x8800:
                /**
                 * 多媒体数据上传应答<br>
                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
                 */
                System.out.println("## 0x8800 - 多媒体数据上传应答");
                break;
            case 0x8801:
                /* 摄像头立即拍摄命令 */
                System.out.println("## 0x8801 - 摄像头立即拍摄命令");
                break;
            case 0x8802:
                /* 存储多媒体数据检索 */
                System.out.println("## 0x0802 - 存储多媒体数据检索");
                break;
            case 0x8803:
                /* 存储多媒体数据上传 */
                System.out.println("## 0x8803 - 存储多媒体数据上传");
                break;
            case 0x8804:
                /* 录音开始命令 */
                System.out.println("## 0x8804 - 录音开始命令");
                break;
            case 0x8805:
                /* 单条存储多媒体数据检索上传命令 */
                System.out.println("## 0x8805 - 单条存储多媒体数据检索上传命令");
                break;
            case 0x8900:
                /* 数据下行透传 */
                System.out.println("## 0x8900 - 数据下行透传");
                break;
            case 0x8A00:
                /* 平台 RSA 公钥 */
                System.out.println("## 0x8A00 - 平台 RSA 公钥");
                break;
        }
        return null;
    }
}
