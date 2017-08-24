package com.incarcloud.rooster.util;

import io.netty.buffer.ByteBuf;

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
        if(0 > length) {
            throw new IllegalArgumentException("length is illegal");
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

    protected JTT808DataPackUtil() {
        super();
    }
}
