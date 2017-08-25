package com.incarcloud.rooster.util;

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
     * 浮点型精度补偿，保存6位小数点
     *
     * @param value 数值
     * @return
     */
    private static BigDecimal divideScale6(BigDecimal value) {
        if(null == value) {
            throw new IllegalArgumentException("value is null");
        }
        return value.divide(BigDecimal.ONE, 7, BigDecimal.ROUND_HALF_UP);
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
                number = divideScale6(new BigDecimal(bigInt / 10e+6));
                break;
            case 1:
                // 1：西经
                number = divideScale6(new BigDecimal(bigInt / 10e+6 * -1));
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
                number = divideScale6(new BigDecimal(bigInt / 10e+6));
                break;
            case 1:
                // 1：南纬
                number = divideScale6(new BigDecimal(bigInt / 10e+6 * -1));
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

    protected JTT808DataPackUtil() {
        super();
    }
}
