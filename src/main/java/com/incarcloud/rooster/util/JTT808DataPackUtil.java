package com.incarcloud.rooster.util;

import com.incarcloud.rooster.datapack.*;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * JTT808 DataPack工具类
 *
 * @author Aaric, created on 2017-08-22T10:56.
 * @since 2.0
 */
public class JTT808DataPackUtil extends DataPackUtil {

    /**
     * 读取一个BYTE类型数据<br>
     *     1个字节
     *
     * @param buffer ByteBuf
     * @return
     */
    public static int readByte(ByteBuf buffer) {
        return readUInt1(buffer);
    }

    /**
     * 读取一个WORD类型数据<br>
     *     2个字节
     *
     * @param buffer ByteBuf
     * @return
     */
    public static int readWord(ByteBuf buffer) {
        return readUInt2(buffer);
    }

    /**
     * 读取一个DWORD类型数据<br>
     *     4个字节
     *
     * @param buffer ByteBuf
     * @return
     */
    public static long readDWord(ByteBuf buffer) {
        return readUInt4(buffer);
    }

    /**
     * 读取默认6个字节BCD码字符串数据<br>
     *     使用复合BCD码，一个字节表示2个十进制数字
     *
     * @param buffer ByteBuf
     * @return
     */
    public static String readBCD(ByteBuf buffer) {
        return readBCD(buffer, 6);
    }

    /**
     * 读取指定长度字节数组数据
     *
     * @param buffer ByteBuf
     * @param length 指定长度
     * @return
     */
    public static String readByteArray(ByteBuf buffer, int length) {
        if(null == buffer) {
            throw new IllegalArgumentException("buffer is null");
        }
        if(0 < length) {
            // 去掉0x00无法解析的byte
            byte b;
            List<Byte> byteList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                b = buffer.readByte();
                if(0x00 != b) {
                    byteList.add(b);
                }
            }
            // List<Byte> --> byte[]
            byte[] stringBytes = new byte[byteList.size()];
            for (int i = 0; i < stringBytes.length; i++) {
                stringBytes[i] = byteList.get(i).byteValue();
            }
            return new String(stringBytes);
        }
        return null;
    }

    /**
     * 读取字符串从可读取字节-2字节（校验码+标识码）
     *
     * @param buffer ByteBuf
     * @return gbk string
     * @throws UnsupportedEncodingException
     */
    public static String readString(ByteBuf buffer) throws UnsupportedEncodingException {
        return readString(buffer, buffer.readableBytes() - 2);
    }

    /**
     * 读取指定长度的字节数组转换为字符串(GBK)
     *
     * @param buffer ByteBuf
     * @param length 长度
     * @return gbk string
     * @throws UnsupportedEncodingException
     */
    public static String readString(ByteBuf buffer, int length) throws UnsupportedEncodingException {
        byte[] stringBytes = new byte[length];
        buffer.readBytes(stringBytes);
        return new String(stringBytes, "GBK");
    }

    /**
     * 获得整型数值的字节码信息(1个字节)
     *
     * @param integer　数值
     * @return
     */
    public static byte getIntegerByte(int integer) {
        return (byte) integer;
    }

    /**
     * 获得WORD的字节码列表信息(2个字节)
     *
     * @param integer 数值
     * @return
     */
    public static List<Byte> getWordByteList(int integer) {
        byte[] bytes = getIntegerBytes(integer, 2);
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }
        return byteList;
    }

    /**
     * 获得DWORD的字节码列表信息(4个字节)
     *
     * @param integer 数值
     * @return
     */
    public static List<Byte> getDWordByteList(int integer) {
        byte[] bytes = getIntegerBytes(integer, 4);
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }
        return byteList;
    }

    /**
     * 获得BCD码字符串的字节码列表信息
     *
     * @param number　获得BCD码字符串
     * @return
     */
    public static List<Byte> getBCDByteList(String number) {
        byte[] bytes = getBCDBytes(number);
        List<Byte> byteList = new ArrayList<>();
        for (int i = 0; i < bytes.length; i++) {
            byteList.add(bytes[i]);
        }
        return byteList;
    }

    /**
     * 位置数据精确度
     */
    private static final int POSITION_DATA_SCALE = 6;

    /**
     * 位置数据单位掩码
     */
    private static final double POSITION_UNIT_MASK = 10e+5;

    /**
     * 浮点型精度补偿，保存6位小数点
     *
     * @param value 数值
     * @return
     */
    private static BigDecimal divideScale6(BigDecimal value) {
        if(null == value) {
            throw new IllegalArgumentException("value is null");
        }
        return value.divide(BigDecimal.ONE, POSITION_DATA_SCALE, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 读取经度数据<br>
     *     规则：[long]*10e-6<br> | E->+,W->-,N->+,S->-
     *
     * @param buffer ByteBuf
     * @param statusProps 状态位定义
     * @return
     */
    public static Double readLongitude(ByteBuf buffer, long statusProps) {
        int flag = Long.valueOf((statusProps >> 3) & 0x00000001).intValue();
        long bigInt = readDWord(buffer);
        BigDecimal number = BigDecimal.ZERO;
        switch (flag) {
            case 0:
                // 0：东经
                number = divideScale6(new BigDecimal(bigInt / POSITION_UNIT_MASK));
                break;
            case 1:
                // 1：西经
                number = divideScale6(new BigDecimal(bigInt / POSITION_UNIT_MASK * -1));
                break;
        }
        return number.doubleValue();
    }

    /**
     * 读取纬度数据<br>
     *     规则：[long]*10e-6 | E->+,W->-,N->+,S->-
     *
     * @param buffer ByteBuf
     * @param statusProps 状态位定义
     * @return
     */
    public static Double readLatitude(ByteBuf buffer, long statusProps) {
        int flag = Long.valueOf((statusProps >> 2) & 0x00000001).intValue();
        long bigInt = readDWord(buffer);
        BigDecimal number = BigDecimal.ZERO;
        switch (flag) {
            case 0:
                // 0：北纬
                number = divideScale6(new BigDecimal(bigInt / POSITION_UNIT_MASK));
                break;
            case 1:
                // 1：南纬
                number = divideScale6(new BigDecimal(bigInt / POSITION_UNIT_MASK * -1));
                break;
        }
        return number.doubleValue();
    }

    /**
     * 读取速度数据<br>
     *     单位：km/h
     *
     * @param buffer ByteBuf
     * @return
     */
    public static Float readSpeed(ByteBuf buffer) {
        return readWord(buffer) * 0.1F;
    }

    /**
     * 读取BCD[6]时间数据
     *
     * @param buffer ByteBuf
     * @return
     */
    public static Date readDate(ByteBuf buffer) throws ParseException {
        String dateString = readBCD(buffer, 6);
        if(!StringUtil.isNullOrEmpty(dateString)) {
            DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA);
            return dateFormat.parse(dateString);
        }
        return null;
    }

    /**
     * 读取BCD[4]日期数据
     *
     * @param buffer ByteBuf
     * @return
     */
    public static Date readDateOnly(ByteBuf buffer) throws ParseException {
        String dateString = readBCD(buffer, 4);
        if(!StringUtil.isNullOrEmpty(dateString)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
            return dateFormat.parse(dateString);
        }
        return null;
    }

    /**
     * 读取一个位置数据
     *
     * @param buffer ByteBuf
     * @param dataPackObject 基对象
     * @param statusProps 状态位
     * @return
     * @throws ParseException
     */
    public static DataPackPosition readPosition(ByteBuf buffer, DataPackObject dataPackObject, long statusProps) throws ParseException {
        DataPackPosition dataPackPosition = new DataPackPosition(dataPackObject);
        // 1.纬度
        double latitude = JTT808DataPackUtil.readLatitude(buffer, statusProps);
        dataPackPosition.setLatitude(latitude);
        JTT808DataPackUtil.debug("latitude: " + latitude);
        // 2.经度
        double longitude = JTT808DataPackUtil.readLongitude(buffer, statusProps);
        dataPackPosition.setLongitude(longitude);
        JTT808DataPackUtil.debug("longitude: " + longitude);
        // 3.海拔高度
        int altitude = JTT808DataPackUtil.readWord(buffer);
        dataPackPosition.setAltitude(altitude);
        JTT808DataPackUtil.debug("altitude: " + altitude);
        // 4.速度
        float speed = JTT808DataPackUtil.readSpeed(buffer);
        dataPackPosition.setSpeed(speed);
        JTT808DataPackUtil.debug("speed: " + speed);
        // 5.方向
        float direction = JTT808DataPackUtil.readWord(buffer);
        dataPackPosition.setDirection(direction);
        JTT808DataPackUtil.debug("direction: " + direction);
        // 6.定位方式
        int mode = 0;
        String modeDesc = "无效数据";
        // 6.1 位18 - GPS定位
        if(queryLongBitContent(statusProps, 18)) {
            mode = DataPackPosition.POSITION_MODE_GPS;
            modeDesc = "GPS定位";
        }
        // 6.2 位19 - 北斗卫星定位
        if(queryLongBitContent(statusProps, 19)) {
            mode = DataPackPosition.POSITION_MODE_BEIDOU;
            modeDesc = "北斗卫星定位";
        }
        // 6.3 位20 - GLONASS卫星定位
        if(queryLongBitContent(statusProps, 20)) {
            mode = DataPackPosition.POSITION_MODE_GLONASS;
            modeDesc = "GLONASS卫星定位";
        }
        // 6.3 位21 - Galileo卫星定位
        if(queryLongBitContent(statusProps, 21)) {
            mode = DataPackPosition.POSITION_MODE_GALILEO;
            modeDesc = "Galileo卫星定位";
        }
        JTT808DataPackUtil.debug("mode: " + mode);
        JTT808DataPackUtil.debug("modeDesc: " + modeDesc);
        dataPackPosition.setPositioMode(mode);
        dataPackPosition.setPositioModeDesc(modeDesc);
        // 7.定位时间
        Date positionTime = readDate(buffer);
        dataPackPosition.setPositionTime(positionTime);
        JTT808DataPackUtil.debug("positionTime: " + positionTime);
        return dataPackPosition;
    }

    /**
     * 读取位置数据附加信息，并封装返回DataPackTarget集合<br>
     *     考虑到数据包里面包含报警数据和极值数据，所以返回DataPackTarget集合
     *
     * @param buffer ByteBuf
     * @param dataPackPosition 位置信息
     * @param extraMsgTotal 附加信息长度
     * @return
     */
    public static List<DataPackTarget> readPositionExtra(ByteBuf buffer, DataPackPosition dataPackPosition, int extraMsgTotal) {
        // 声明变量
        int extraMsgId;
        int extraMsgLength;
        String extraMsgContent;
        List<DataPackTarget> dataPackTargetList = new ArrayList<>();
        DataPackPeak.Peak peak;
        List<DataPackPeak.Peak> peakList = new ArrayList<>();
        DataPackPeak dataPackPeak;
        DataPackAlarm.Alarm alarm;
        List<DataPackAlarm.Alarm> alarmList = new ArrayList<>();
        DataPackAlarm dataPackAlarm;

        // 读取数据
        int left = extraMsgTotal;
        while(0 < left) {

            // 1.附加信息 ID
            extraMsgId = JTT808DataPackUtil.readByte(buffer);
            JTT808DataPackUtil.debug("extraMsgId: " + extraMsgId);
            // 2.附加信息长度
            extraMsgLength = JTT808DataPackUtil.readByte(buffer);
            JTT808DataPackUtil.debug("extraMsgLength: " + extraMsgLength);
            // -.计算剩余
            left -= (2 + extraMsgLength);

            // 3.附加信息
            // 3.1 判断
            // 0x05-0x10 - 保留
            // 0xE1-0xFF - 自定义区域
            // 0x01 - 4 - 里程，DWORD，1/10km，对应车上里程表读数
            if(0x01 == extraMsgId && 4 == extraMsgLength) {
                // 单位km
                extraMsgContent = String.valueOf(JTT808DataPackUtil.readDWord(buffer)/10);
                peak = new DataPackPeak.Peak(extraMsgId, extraMsgContent);
                peak.setPeakUnit("km");
                peak.setPeakDesc("里程，对应车上里程表读数");
                //--add
                peakList.add(peak);
                continue;
            }
            // 0x02 - 2 - 油量，WORD，1/10L，对应车上油量表读数
            if(0x02 == extraMsgId && 2 == extraMsgLength) {
                // 单位L
                extraMsgContent = String.valueOf(JTT808DataPackUtil.readWord(buffer)/10);
                peak = new DataPackPeak.Peak(extraMsgId, extraMsgContent);
                peak.setPeakUnit("L");
                peak.setPeakDesc("油量，对应车上油量表读数");
                //--add
                peakList.add(peak);
                continue;
            }
            // 0x03 - 2 - 行驶记录功能获取的速度，WORD，1/10km/h
            if(0x03 == extraMsgId && 2 == extraMsgLength) {
                // 单位km/h
                extraMsgContent = String.valueOf(JTT808DataPackUtil.readWord(buffer)/10);
                peak = new DataPackPeak.Peak(extraMsgId, extraMsgContent);
                peak.setPeakUnit("km/h");
                peak.setPeakDesc("行驶记录功能获取的速度");
                //--add
                peakList.add(peak);
                continue;
            }
            // 0x04 - 2 - 需要人工确认报警事件的 ID，WORD，从 1 开始计数
            if(0x04 == extraMsgId && 2 == extraMsgLength) {
                alarm = new DataPackAlarm.Alarm("需要人工确认报警事件的 ID");
                alarm.setAlarmCode(String.valueOf(extraMsgId));
                alarm.setAlarmValue(String.valueOf(JTT808DataPackUtil.readWord(buffer)));
                //--add
                alarmList.add(alarm);
                continue;
            }
            // 0x11 - 1 或 5 - 超速报警附加信息见 表 28
            if(0x11 == extraMsgId && (1 == extraMsgLength || 5 == extraMsgLength)) {
                alarm = new DataPackAlarm.Alarm("超速报警");
                alarm.setAlarmCode(String.valueOf(extraMsgId));
                switch (extraMsgLength) {
                    case 1:
                        // 0：无特定位置
                        alarm.setAlarmValue(String.valueOf(JTT808DataPackUtil.readByte(buffer)));
                        break;
                    case 2:
                        // 1：圆形区域；2：矩形区域；3：多边形区域；4：路段
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(JTT808DataPackUtil.readByte(buffer));
                        stringBuffer.append("-");
                        stringBuffer.append(JTT808DataPackUtil.readDWord(buffer));
                        alarm.setAlarmValue(stringBuffer.toString());
                        break;
                }
                alarm.setAlarmDesc("【位置类型：0：无特定位置；1：圆形区域；2：矩形区域；3：多边形区域；4：路段】－【区域或路段 ID】");
                //--add
                alarmList.add(alarm);
                continue;
            }
            // 0x12 - 6 - 进出区域/路线报警附加信息见 表 29
            if(0x12 == extraMsgId && 6 == extraMsgLength) {
                alarm = new DataPackAlarm.Alarm("进出区域/路线报警");
                alarm.setAlarmCode(String.valueOf(extraMsgId));
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(JTT808DataPackUtil.readByte(buffer));
                stringBuffer.append("-");
                stringBuffer.append(JTT808DataPackUtil.readDWord(buffer));
                stringBuffer.append("-");
                stringBuffer.append(JTT808DataPackUtil.readByte(buffer));
                alarm.setAlarmValue(stringBuffer.toString());
                alarm.setAlarmDesc("【位置类型：0：无特定位置；1：圆形区域；2：矩形区域；3：多边形区域；4：路段】－【区域或路段 ID】-【方向：0：进；1：出】");
                //--add
                alarmList.add(alarm);
                continue;
            }
            // 0x13 - 7 - 路段行驶时间不足/过长报警附加信息见 表 30
            if(0x13 == extraMsgId && 7 == extraMsgLength) {
                alarm = new DataPackAlarm.Alarm("路段行驶时间不足/过长报警");
                alarm.setAlarmCode(String.valueOf(extraMsgId));
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(JTT808DataPackUtil.readDWord(buffer));
                stringBuffer.append("-");
                stringBuffer.append(JTT808DataPackUtil.readWord(buffer));
                stringBuffer.append("-");
                stringBuffer.append(JTT808DataPackUtil.readByte(buffer));
                alarm.setAlarmValue(stringBuffer.toString());
                alarm.setAlarmDesc("【路段 ID】-【路段行驶时间:=单位为秒（s）】-【结果：0：不足；1：过长】");
                //--add
                alarmList.add(alarm);
                continue;
            }
            // 0x25 - 4 - 扩展车辆信号状态位，定义见 表 31
            if(0x25 == extraMsgId && 4 == extraMsgLength) {
                // TODO 表 31 扩展车辆信号状态位
                JTT808DataPackUtil.debug("车辆信号状态: " + JTT808DataPackUtil.readBytes(buffer, 4));
                continue;
            }
            // 0x2A - 2 - IO状态位，定义见 表 32
            if(0x2A == extraMsgId && 2 == extraMsgLength) {
                // TODO 表 32 IO 状态位
                JTT808DataPackUtil.debug("IO状态: " + JTT808DataPackUtil.readBytes(buffer, 2));
                continue;
            }
            // 0x2B - 4 - 模拟量，bit0-15，AD0；bit16-31，AD1。
            if(0x2B == extraMsgId && 4 == extraMsgLength) {
                // bit16-31，AD1
                peak = new DataPackPeak.Peak();
                peak.setPeakId(extraMsgId);
                peak.setPeakName("AD1");
                peak.setPeakValue(String.valueOf(JTT808DataPackUtil.readWord(buffer)));
                //--add
                peakList.add(peak);
                // bit0-15，AD0
                peak = new DataPackPeak.Peak();
                peak.setPeakId(extraMsgId);
                peak.setPeakName("AD0");
                peak.setPeakValue(String.valueOf(JTT808DataPackUtil.readWord(buffer)));
                //--add
                peakList.add(peak);
                continue;
            }
            // 0x30 - 1 - BYTE，无线通信网络信号强度
            if(0x30 == extraMsgId && 1 == extraMsgLength) {
                peak = new DataPackPeak.Peak();
                peak.setPeakId(extraMsgId);
                peak.setPeakName("无线通信网络信号强度");
                peak.setPeakValue(String.valueOf(JTT808DataPackUtil.readByte(buffer)));
                //--add
                peakList.add(peak);
                continue;
            }
            // 0x31 - 1 - BYTE，GNSS 定位卫星数
            if(0x31 == extraMsgId && 1 == extraMsgLength) {
                peak = new DataPackPeak.Peak();
                peak.setPeakId(extraMsgId);
                peak.setPeakName("GNSS 定位卫星数");
                peak.setPeakValue(String.valueOf(JTT808DataPackUtil.readByte(buffer)));
                //--add
                peakList.add(peak);
                continue;
            }

            // 无法解析，直接释放
            JTT808DataPackUtil.readBytes(buffer, extraMsgLength);
        }

        // 组装报警数据
        if(null != alarmList && 0 < alarmList.size()) {
            dataPackAlarm = new DataPackAlarm(dataPackPosition);
            dataPackAlarm.setAlarmList(alarmList);
            dataPackAlarm.setPosition(dataPackPosition);
            dataPackTargetList.add(new DataPackTarget(dataPackAlarm));
        }

        // 组装极值数据
        if(null != peakList && 0 < peakList.size()) {
            dataPackPeak = new DataPackPeak(dataPackPosition);
            dataPackPeak.setPeakList(peakList);
            dataPackTargetList.add(new DataPackTarget(dataPackPeak));
        }

        return dataPackTargetList;
    }

    /**
     * 查询long类型数据的位位索引数据
     *
     * @param props 标志属性数据
     * @param index 位索引，高位在前，低位在后
     * @return 1-true, 0-false
     */
    private static boolean queryLongBitContent(long props, int index) {
        if(0x01 == ((props >> index) & 0x01)) {
            return true;
        }
        return false;
    }

    /**
     * 报警标志详情<br>
     *     参考“表 23 位置基本信息数据格式”
     *
     * @param alarmProps 报警标志属性
     * @return 报警集合
     */
    public static List<DataPackAlarm.Alarm> detailAlarmProps(long alarmProps) {
        List<DataPackAlarm.Alarm> alarmList = null;
        if(0 != alarmProps) {
            alarmList = new ArrayList<>();
            // 0 -> 1：紧急报警，触动报警开关后触发
            if(queryLongBitContent(alarmProps, 0)) {
                alarmList.add(new DataPackAlarm.Alarm("紧急报警，触动报警开关后触发", 0, "收到应答后清零"));
            }
            // 1 -> 1：超速报警
            if(queryLongBitContent(alarmProps, 1)) {
                alarmList.add(new DataPackAlarm.Alarm("超速报警", 1, "标志维持至报警条件解除"));
            }
            // 2 -> 1：疲劳驾驶
            if(queryLongBitContent(alarmProps, 2)) {
                alarmList.add(new DataPackAlarm.Alarm("疲劳驾驶", 2, "标志维持至报警条件解除"));
            }
            // 3 -> 1：危险预警
            if(queryLongBitContent(alarmProps, 3)) {
                alarmList.add(new DataPackAlarm.Alarm("危险预警", 3, "收到应答后清零"));
            }
            // 4 -> 1：GNSS 模块发生故障
            if(queryLongBitContent(alarmProps, 4)) {
                alarmList.add(new DataPackAlarm.Alarm("GNSS 模块发生故障", 4, "志维持至报警条件解除"));
            }
            // 5 -> 1：GNSS 天线未接或被剪断
            if(queryLongBitContent(alarmProps, 5)) {
                alarmList.add(new DataPackAlarm.Alarm("GNSS 天线未接或被剪断", 5, "标志维持至报警条件解除"));
            }
            // 6 -> 1：GNSS 天线短路
            if(queryLongBitContent(alarmProps, 6)) {
                alarmList.add(new DataPackAlarm.Alarm("GNSS 天线短路", 6, "标志维持至报警条件解除"));
            }
            // 7 -> 1：终端主电源欠压
            if(queryLongBitContent(alarmProps, 7)) {
                alarmList.add(new DataPackAlarm.Alarm("终端主电源欠压", 7, "标志维持至报警条件解除"));
            }
            // 8 -> 1：终端主电源掉电
            if(queryLongBitContent(alarmProps, 8)) {
                alarmList.add(new DataPackAlarm.Alarm("终端主电源掉电", 8, "标志维持至报警条件解除"));
            }
            // 9 -> 1：终端 LCD 或显示器故障
            if(queryLongBitContent(alarmProps, 9)) {
                alarmList.add(new DataPackAlarm.Alarm("终端 LCD 或显示器故障", 9, "标志维持至报警条件解除"));
            }
            // 10 -> 1：TTS 模块故障
            if(queryLongBitContent(alarmProps, 10)) {
                alarmList.add(new DataPackAlarm.Alarm("TTS 模块故障", 10, "标志维持至报警条件解除"));
            }
            // 11 -> 1：摄像头故障
            if(queryLongBitContent(alarmProps, 11)) {
                alarmList.add(new DataPackAlarm.Alarm("摄像头故障", 11, "标志维持至报警条件解除"));
            }
            // 12 -> 1：道路运输证 IC 卡模块故障
            if(queryLongBitContent(alarmProps, 12)) {
                alarmList.add(new DataPackAlarm.Alarm("道路运输证 IC 卡模块故障", 12, "标志维持至报警条件解除"));
            }
            // 13 -> 1：超速预警
            if(queryLongBitContent(alarmProps, 13)) {
                alarmList.add(new DataPackAlarm.Alarm("超速预警", 13, "标志维持至报警条件解除"));
            }
            // 14 -> 1：疲劳驾驶预警
            if(queryLongBitContent(alarmProps, 14)) {
                alarmList.add(new DataPackAlarm.Alarm("疲劳驾驶预警", 14, "标志维持至报警条件解除"));
            }
            // 18 -> 1：当天累计驾驶超时
            if(queryLongBitContent(alarmProps, 18)) {
                alarmList.add(new DataPackAlarm.Alarm("当天累计驾驶超时", 18, "标志维持至报警条件解除"));
            }
            // 19 -> 1：超时停车
            if(queryLongBitContent(alarmProps, 19)) {
                alarmList.add(new DataPackAlarm.Alarm("超时停车", 19, "标志维持至报警条件解除"));
            }
            // 20 -> 1：进出区域
            if(queryLongBitContent(alarmProps, 20)) {
                alarmList.add(new DataPackAlarm.Alarm("进出区域", 20, "收到应答后清零"));
            }
            // 21 -> 1：进出路线
            if(queryLongBitContent(alarmProps, 21)) {
                alarmList.add(new DataPackAlarm.Alarm("进出路线", 21, "收到应答后清零"));
            }
            // 22 -> 1：路段行驶时间不足/过长
            if(queryLongBitContent(alarmProps, 22)) {
                alarmList.add(new DataPackAlarm.Alarm("路段行驶时间不足/过长", 22, "收到应答后清零"));
            }
            // 23 -> 1：路线偏离报警
            if(queryLongBitContent(alarmProps, 23)) {
                alarmList.add(new DataPackAlarm.Alarm("路线偏离报警", 23, "标志维持至报警条件解除"));
            }
            // 24 -> 1：车辆 VSS 故障
            if(queryLongBitContent(alarmProps, 24)) {
                alarmList.add(new DataPackAlarm.Alarm("车辆 VSS 故障", 24, "标志维持至报警条件解除"));
            }
            // 25 -> 1：车辆油量异常
            if(queryLongBitContent(alarmProps, 25)) {
                alarmList.add(new DataPackAlarm.Alarm("车辆油量异常", 25, "标志维持至报警条件解除"));
            }
            // 26 -> 1：车辆被盗(通过车辆防盗器)
            if(queryLongBitContent(alarmProps, 26)) {
                alarmList.add(new DataPackAlarm.Alarm("车辆被盗(通过车辆防盗器)", 26, "标志维持至报警条件解除"));
            }
            // 27 -> 1：车辆非法点火
            if(queryLongBitContent(alarmProps, 27)) {
                alarmList.add(new DataPackAlarm.Alarm("车辆非法点火", 27, "收到应答后清零"));
            }
            // 28 -> 1：车辆非法位移
            if(queryLongBitContent(alarmProps, 28)) {
                alarmList.add(new DataPackAlarm.Alarm("车辆非法位移", 28, "收到应答后清零"));
            }
            // 29 -> 1：碰撞预警
            if(queryLongBitContent(alarmProps, 29)) {
                alarmList.add(new DataPackAlarm.Alarm("碰撞预警", 29, "标志维持至报警条件解除"));
            }
            // 30 -> 1：侧翻预警
            if(queryLongBitContent(alarmProps, 30)) {
                alarmList.add(new DataPackAlarm.Alarm("侧翻预警", 30, "标志维持至报警条件解除"));
            }
            // 31 -> 1：非法开门报警（终端未设置区域时，不判断非法开门）
            if(queryLongBitContent(alarmProps, 31)) {
                alarmList.add(new DataPackAlarm.Alarm("非法开门报警（终端未设置区域时，不判断非法开门）", 31, "收到应答后清零"));
            }
        }
        return alarmList;
    }

    /**
     * 打印调试信息，调试完成后设置false
     *
     * @param string 字符串
     */
    public static void debug(String string) {
        if(true) {
            System.out.println(string);
        }
    }

    protected JTT808DataPackUtil() {
        super();
    }
}
