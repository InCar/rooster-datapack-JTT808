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

    protected JTT808DataPackUtil() {
        super();
    }
}
