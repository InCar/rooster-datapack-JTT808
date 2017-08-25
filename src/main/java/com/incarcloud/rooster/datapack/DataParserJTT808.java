package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.util.JTT808DataPackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

import java.util.*;

/**
 * JTT808 Parser.
 *
 * @author Aaric, created on 2017-08-22T10:22.
 * @since 2.0
 */
public class DataParserJTT808 implements IDataParser {

    /**
     * 协议分组和名称
     */
    public static final String PROTOCOL_GROUP = "china";
    public static final String PROTOCOL_NAME = "jtt808";
    public static final String PROTOCOL_VERSION = "2013.1";
    public static final String PROTOCOL_PREFIX = PROTOCOL_GROUP + "-" + PROTOCOL_NAME + "-";

    static {
        /**
         * 声明数据包版本与解析器类关系
         */
        DataParserManager.register(PROTOCOL_PREFIX + PROTOCOL_VERSION, DataParserJTT808.class);
    }

    /**
     * 数据包准许最大容量2M
     */
    private static final int DISCARDS_MAX_LENGTH = 1024 * 1024 * 2;

    @Override
    public List<DataPack> extract(ByteBuf buffer) {
        /**
         * ## JTT808数据包格式 ###
         * # 1.标识位(0x7E)
         * # 2.消息头
         * # 3.消息体
         * # 4.检验码
         * # 5.标识位(0x7E)
         */
        DataPack dataPack;
        List<DataPack> dataPackList = new ArrayList<>();

        // 长度大于2M的数据包直接抛弃(恶意数据)
        if(DISCARDS_MAX_LENGTH < buffer.readableBytes()) {
            //System.out.println("clear");
            buffer.clear();
        }

        // 遍历
        byte check;
        int start, offset;
        List<Byte> byteList;
        while (buffer.isReadable()) {
            // 初始化
            start = buffer.readerIndex();
            offset = start + 1;

            // 寻找以0x7E开始和0x7E结束的数据段
            if(0x7E == (buffer.getByte(start) & 0xFF)) {
                // 寻找0x7E结束点
                for (; offset < buffer.writerIndex(); offset++) {
                    if(0x7E == (buffer.getByte(offset) & 0xFF)) {
                        // 寻找0x7E结束点成功，结束for循环
                        break;
                    }
                }

                // 寻找0x7E结束点失败，结束while循环
                if(buffer.writableBytes() == offset) {
                    break;
                }

                // 转义还原字节码
                // 还原规则：0x7D0x01->0x7D, 0x7D0x02->0x7E
                byteList = new ArrayList<>();
                for(int i = start + 1; i < offset - 1; i++) {
                    if(0x7D == (buffer.getByte(i) & 0xFF) && 0x01 == (buffer.getByte(i+1) & 0xFF)) {
                        // 0x7D0x01->0x7D
                        byteList.add((byte) 0x7D);
                        i++;
                    } else if(0x7D == (buffer.getByte(i) & 0xFF) && 0x02 == (buffer.getByte(i+1) & 0xFF)) {
                        // 0x7D0x02->0x7E
                        byteList.add((byte) 0x7E);
                        i++;
                    } else {
                        byteList.add(buffer.getByte(i));
                    }
                }

                // 计算校验码
                check = 0x00;
                for (Byte byteObject: byteList) {
                    // 校验码指从消息头开始，同后一字节异或，直到校验码前一个字节
                    check ^= byteObject.byteValue();
                }

                // 验证校验码
                if(buffer.getByte(offset -1) == check) {
                    //System.out.println("oxr check success");
                    //System.out.println(String.format("%d-%d", start, offset - start + 1));
                    // 打包
                    dataPack = new DataPack(PROTOCOL_GROUP, PROTOCOL_NAME, PROTOCOL_VERSION);
                    dataPack.setBuf(buffer.slice(start, offset - start + 1));
                    dataPackList.add(dataPack);
                }

                // 跳跃(offset - start + 1)个字节
                buffer.skipBytes(offset - start + 1);

            } else {
                // 不符合条件，向前跳跃1
                buffer.skipBytes(1);
            }
        }

        return dataPackList;
    }

    /**
     * 验证数据包
     *
     * @param bytes 原始数据
     * @return 返回转义还原的字节数组
     */
    private byte[] validate(byte[] bytes) {
        if(null != bytes && 2 < bytes.length) {
            // 标识位(0x7e)
            if(0x7E == (bytes[0] & 0xFF) && 0x7E == (bytes[bytes.length-1] & 0xFF)) {
                // 转义还原字节码
                // 还原规则：0x7D0x01->0x7D, 0x7D0x02->0x7E
                List<Byte> byteList = new ArrayList<>();
                byteList.add(bytes[0]); // 标识位(0x7E)
                for(int i = 1; i < bytes.length - 1; i++) {
                    if(0x7D == (bytes[i] & 0xFF) && 0x01 == (bytes[i+1] & 0xFF)) {
                        // 0x7D0x01->0x7D
                        byteList.add((byte) 0x7D);
                        i++;
                    } else if(0x7D == (bytes[i] & 0xFF) && 0x02 == (bytes[i+1] & 0xFF)) {
                        // 0x7D0x02->0x7E
                        byteList.add((byte) 0x7E);
                        i++;
                    } else {
                        byteList.add(bytes[i]);
                    }
                }
                byteList.add(bytes[bytes.length-1]); // 标识位(0x7E)

                // List<Byte> --> byte[]
                byte[] shiftBytes = new byte[byteList.size()];
                for (int i = 0; i < shiftBytes.length; i++) {
                    shiftBytes[i] = byteList.get(i).byteValue();
                }

                // 计算校验码
                byte check = 0x00;
                for(int i = 1; i <= shiftBytes.length - 3; i++) {
                    check ^= shiftBytes[i];
                }

                // 验证校验码
                if(shiftBytes[shiftBytes.length - 2] == check) {
                    return shiftBytes;
                }
            }
        }
        return null;
    }

    @Override
    public ByteBuf createResponse(DataPack requestPack, ERespReason reason) {
        // 发送消息时：消息封装——>计算并填充校验码——>转义
        // 0x7e-0x7d02, 0x7d-0x7d01
        ByteBuf buffer = null;
        if(null != requestPack && null != reason) {
            // 原始数据
            byte[] dataPackBytes = validate(Base64.getDecoder().decode(requestPack.getDataB64()));
            if(null != dataPackBytes) {
                // 初始化List容器，装载【消息头+消息体】
                List<Byte> byteList = new ArrayList<>();

                // 设置回复命令字
                byteList.add((byte) 0x80);
                byteList.add((byte) 0x01);

                // 设置消息长度：平台通用应答回复5个字节：【应答流水号】+【应答 ID】+【结果】
                // 双字节，最后9个bit表示消息长度，所以&0xFE00在&0x05
                int msgLen = 5 & 0x01FF;
                int msgProps = (((dataPackBytes[3] & 0xFF) << 8) & (dataPackBytes[4] & 0xFF) & 0xFE00) | msgLen;
                byteList.add((byte) ((msgProps >> 8) & 0xFF));
                byteList.add((byte) (msgProps & 0xFF));

                // 设置终端手机号(6个字节BCD码)，5~10
                for (int i = 5; i <= 10; i++) {
                    byteList.add(dataPackBytes[i]);
                }

                // 设置消息流水号，同终端消息的流水号
                byteList.add(dataPackBytes[11]);
                byteList.add(dataPackBytes[12]);

                // 设置对应的终端消息的流水号
                byteList.add(dataPackBytes[11]);
                byteList.add(dataPackBytes[12]);

                // 设置对应的终端消息的ID
                byteList.add(dataPackBytes[1]);
                byteList.add(dataPackBytes[2]);

                // 设置结果
                // 结果说明：0：成功/确认；1：失败；2：消息有误；3：不支持；4：报警处理确认；
                byte statusCode;
                switch (reason) {
                    case OK:
                        // 成功接收
                        statusCode = 0x00;
                        break;
                    default:
                        // 其他
                        statusCode = 0x01;
                }
                byteList.add(statusCode);

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
        return buffer;
    }

    @Override
    public void destroyResponse(ByteBuf responseBuf) {
        if(null != responseBuf) {
            ReferenceCountUtil.release(responseBuf);
        }
    }

    @Override
    public List<DataPackTarget> extractBody(DataPack dataPack) {
        ByteBuf buffer = null;
        List<DataPackTarget> dataPackTargetList = null;
        byte[] dataPackBytes = validate(Base64.getDecoder().decode(dataPack.getDataB64()));
        if(null != dataPackBytes) {
            try {
                // 初始化ByteBuf
                buffer = Unpooled.wrappedBuffer(dataPackBytes);

                /* 标识位 */
                // 跳过标识位(0x7e)
                buffer.readBytes(1);

                /* 消息头 */
                // 1.消息ID
                int msgId = JTT808DataPackUtil.readWord(buffer);
                System.out.println("msgId: " + msgId);

                // 2.消息体属性
                int msgProps = JTT808DataPackUtil.readWord(buffer);
                // 2.1 消息体长度
                int msgLength = msgProps & 0x01FF;
                System.out.println("msgLength: " + msgLength);
                // 2.2 数据加密方式
                int msgEncryptMode = (msgProps >> 10) & 0x0007;
                System.out.println("msgEncryptMode: " + msgEncryptMode);
                switch (msgEncryptMode) {
                    case 0:
                        // 消息体不加密
                        System.out.println("--消息体不加密");
                        break;
                    case 1:
                        // 第 10 位为 1，表示消息体经过 RSA 算法加密
                        System.out.println("--RSA 算法加密");
                }
                // 2.3 分包
                int msgSubPack = (msgProps >> 13) & 0x0001;
                System.out.println("msgSubPack: " + msgSubPack);
                switch (msgSubPack) {
                    case 0:
                        // 第 13 位为 0，则消息头中无消息包封装项字段
                        System.out.println("--无消息包封装项字段");
                        break;
                    case 1:
                        // 第 13 位为 1 时表示消息体为长消息，进行分包发送处理
                        System.out.println("--分包发送处理");
                        break;
                }

                // 3.终端手机号(设备号)
                String deviceId = JTT808DataPackUtil.readBCD(buffer);
                System.out.println("deviceId: " + deviceId);

                // 4.消息流水号
                int msgSeq = JTT808DataPackUtil.readWord(buffer);
                System.out.println("msgSeq: " + msgSeq);

                /* 消息体 */
                switch (msgId) {
                    case 0x001:
                        /* 终端通用应答 */
                        System.out.println("## 0x001 - 终端通用应答");
                        break;
                    case 0x8001:
                        /* 平台通用应答 */
                        System.out.println("## 0x8001 - 平台通用应答");
                        break;
                    case 0x0002:
                        /* 终端心跳 */
                        System.out.println("## 0x0002 - 终端心跳");
                        // 已解析，只有消息头
                        break;
                    case 0x8003:
                        /* 补传分包请求 */
                        System.out.println("## 0x8003 - 补传分包请求");
                        break;
                    case 0x0100:
                        /* 终端注册 */
                        System.out.println("## 0x0100 - 终端注册");
                        int provinceId = JTT808DataPackUtil.readWord(buffer);
                        System.out.println("provinceId: " + provinceId);
                        int cityId = JTT808DataPackUtil.readWord(buffer);
                        System.out.println("cityId: " + cityId);
                        String manufacturerId = JTT808DataPackUtil.readByteArray(buffer, 5);
                        System.out.println("manufacturerId: " + manufacturerId);
                        String terminalModel = JTT808DataPackUtil.readByteArray(buffer, 20);
                        System.out.println("terminalModel: " + terminalModel);
                        String terminalId = JTT808DataPackUtil.readByteArray(buffer, 7);
                        System.out.println("terminalId: " + terminalId);
                        int colorId = JTT808DataPackUtil.readByte(buffer);
                        System.out.println("colorId: " + colorId);
                        switch (colorId) {
                            case 0:
                                // 未上牌时，取值为 0
                                break;
                            default:
                                // 车辆颜色
                        }
                        // 车辆标识
                        String vin = JTT808DataPackUtil.readString(buffer);
                        if(0 == colorId) {
                            // VIN
                            System.out.println("vin: " + vin);
                        } else  {
                            // License
                            System.out.println("License: " + vin);
                        }
                        break;
                    case 0x8100:
                        /* 终端注册应答 */
                        System.out.println("## 0x8100 - 终端注册应答");
                        break;
                    case 0x0003:
                        /* 终端注销 */
                        System.out.println("## 0x0003 - 终端注销");
                        break;
                    case 0x0102:
                        /* 终端鉴权 */
                        System.out.println("## 0x0102 - 终端鉴权");
                        break;
                    case 0x8103:
                        /* 设置终端参数 */
                        System.out.println("## 0x8103 - 设置终端参数");
                        break;
                    case 0x8104:
                        /* 查询终端参数 */
                        System.out.println("## 0x8104 - 查询终端参数");
                        break;
                    case 0x0104:
                        /* 查询终端参数应答 */
                        System.out.println("## 0x0104 - 查询终端参数应答");
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
                    case 0x0107:
                        /* 查询终端属性应答 */
                        System.out.println("## 0x0107 - 查询终端属性应答");
                        break;
                    case 0x8108:
                        /* 下发终端升级包 */
                        System.out.println("## 0x8108 - 下发终端升级包");
                        break;
                    case 0x0108:
                        /* 终端升级结果通知 */
                        System.out.println("## 0x0108 - 终端升级结果通知");
                        break;
                    case 0x0200:
                        /* 位置信息汇报 */
                        System.out.println("## 0x0200 - 位置信息汇报");
                        // 1.位置基本信息
                        long alarmProps = JTT808DataPackUtil.readDWord(buffer);
                        System.out.println("alarmProps: " + alarmProps);
                        long statusProps = JTT808DataPackUtil.readDWord(buffer);
                        System.out.println("statusProps: " + statusProps);
                        double longitude = JTT808DataPackUtil.readLongitude(buffer, statusProps);
                        System.out.println("longitude: " + longitude);
                        double latitude = JTT808DataPackUtil.readLatitude(buffer, statusProps);
                        System.out.println("latitude: " + latitude);
                        int altitude = JTT808DataPackUtil.readWord(buffer);
                        System.out.println("altitude: " + altitude);
                        float speed = JTT808DataPackUtil.readSpeed(buffer);
                        System.out.println("speed: " + speed);
                        float direction = JTT808DataPackUtil.readWord(buffer);
                        System.out.println("direction: " + direction);
                        Date positionTime = JTT808DataPackUtil.readDate(buffer);
                        System.out.println("positionTime: " + positionTime);
                        // 2.位置附加信息项列表（可没有，根据消息头中的长度字段确定）
                        break;
                    case 0x8201:
                        /* 位置信息查询 */
                        System.out.println("## 0x8201 - 位置信息查询");
                        break;
                    case 0x0201:
                        /* 位置信息查询应答 */
                        System.out.println("## 0x0201 - 位置信息查询应答");
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
                    case 0x0301:
                        /* 事件报告 */
                        System.out.println("## 0x0301 - 事件报告");
                        break;
                    case 0x8302:
                        /* 提问下发 */
                        System.out.println("## 0x8302 - 提问下发");
                        break;
                    case 0x0302:
                        /* 提问应答 */
                        System.out.println("## 0x0302 - 提问应答");
                        break;
                    case 0x8303:
                        /* 信息点播菜单设置 */
                        System.out.println("## 0x8303 - 信息点播菜单设置");
                        break;
                    case 0x0303:
                        /* 信息点播/取消 */
                        System.out.println("## 0x0303 - 信息点播/取消");
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
                    case 0x0500:
                        /* 车辆控制应答 */
                        System.out.println("## 0x0500 - 车辆控制应答");
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
                        /* 行驶记录仪数据采集命令 */
                        System.out.println("## 0x8700 - 行驶记录仪数据采集命令");
                        break;
                    case 0x0700:
                        /* 行驶记录仪数据上传 */
                        System.out.println("## 0x0700 - 行驶记录仪数据上传");
                        break;
                    case 0x8701:
                        /* 行驶记录仪参数下传命令 */
                        System.out.println("## 0x8701 - 行驶记录仪参数下传命令");
                        break;
                    case 0x0701:
                        /* 电子运单上报 */
                        System.out.println("## 0x0701 - 电子运单上报");
                        break;
                    case 0x0702:
                        /* 驾驶员身份信息采集上报 */
                        System.out.println("## 0x0702 - 驾驶员身份信息采集上报");
                        break;
                    case 0x8702:
                        /* 上报驾驶员身份信息请求 */
                        System.out.println("## 0x8702 - 上报驾驶员身份信息请求");
                        break;
                    case 0x0704:
                        /* 定位数据批量上传 */
                        System.out.println("## 0x0704 - 定位数据批量上传");
                        break;
                    case 0x0705:
                        /* CAN 总线数据上传 */
                        System.out.println("## 0x0705 - CAN 总线数据上传");
                        break;
                    case 0x0800:
                        /* 多媒体事件信息上传 */
                        System.out.println("## 0x0800 - 多媒体事件信息上传");
                        break;
                    case 0x0801:
                        /* 多媒体数据上传 */
                        System.out.println("## 0x0801 - 多媒体数据上传");
                        break;
                    case 0x8800:
                        /* 多媒体数据上传应答 */
                        System.out.println("## 0x8800 - 多媒体数据上传应答");
                        break;
                    case 0x8801:
                        /* 摄像头立即拍摄命令 */
                        System.out.println("## 0x8801 - 摄像头立即拍摄命令");
                        break;
                    case 0x0805:
                        /* 摄像头立即拍摄命令应答 */
                        System.out.println("## 0x0805 - 摄像头立即拍摄命令应答");
                        break;
                    case 0x8802:
                        /* 存储多媒体数据检索 */
                        System.out.println("## 0x0802 - 存储多媒体数据检索");
                        break;
                    case 0x0802:
                        /* 存储多媒体数据检索应答 */
                        System.out.println("## 0x0802 - 存储多媒体数据检索应答");
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
                    case 0x0900:
                        /* 数据上行透传 */
                        System.out.println("## 0x0900 - 数据上行透传");
                        break;
                    case 0x0901:
                        /* 数据压缩上报 */
                        System.out.println("## 0x0901 - 数据压缩上报");
                        break;
                    case 0x8A00:
                        /* 平台 RSA 公钥 */
                        System.out.println("## 0x8A00 - 平台 RSA 公钥");
                        break;
                    case 0x0A00:
                        /* 终端 RSA 公钥 */
                        System.out.println("## 0x0A00 - 终端 RSA 公钥");
                        break;
                    default:
                        /**
                         * 0x8F00~0x8FFF 平台下行消息保留
                         * 0x0F00~0x0FFF 终端上行消息保留
                         */
                        System.out.println("## msgId(" + msgId + ") can't be parsed.");
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // 释放ByteBuf
                ReferenceCountUtil.release(buffer);
            }
        }
        return dataPackTargetList;
    }

    @Override
    public Map<String, Object> getMetaData(ByteBuf buffer) {
        return null;
    }
}
