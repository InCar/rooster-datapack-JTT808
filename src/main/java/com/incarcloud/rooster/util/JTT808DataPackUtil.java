package com.incarcloud.rooster.util;

import com.incarcloud.rooster.datapack.DataPackAlarm;
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
     * 读取指定长度BCD码字符串数据<br>
     *     使用复合BCD码，一个字节表示2个十进制数字
     *
     * @param buffer ByteBuf
     * @param length 指定长度
     * @return
     */
    public static String readBCD(ByteBuf buffer, int length) {
        if(null == buffer) {
            throw new IllegalArgumentException("buffer is null");
        }
        if(0 < length) {
            int number;
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < length; i++) {
                number = readByte(buffer);
                stringBuffer.append(number >> 4 & 0x0F);
                stringBuffer.append(number & 0x0F);
            }
            return stringBuffer.toString();
        }
        return null;
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
