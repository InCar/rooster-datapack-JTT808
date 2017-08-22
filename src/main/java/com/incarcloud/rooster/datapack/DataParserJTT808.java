package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.util.JTT808DataPackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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

                // 计算校验码
                check = 0x00;
                for(int i = start + 1; i <= offset - 2; i++) {
                    // 校验码指从消息头开始，同后一字节异或，直到校验码前一个字节
                    check ^= buffer.getByte(i);
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

    @Override
    public ByteBuf createResponse(DataPack requestPack, ERespReason reason) {
        return null;
    }

    @Override
    public void destroyResponse(ByteBuf responseBuf) {

    }

    /**
     * 验证数据包
     *
     * @param bytes 原始数据
     * @return
     */
    private boolean validate(byte[] bytes) {
        if(null != bytes && 2 < bytes.length) {
            // 标识位(0x7e)
            int length = bytes.length;
            if(0x7E == (bytes[0] & 0xFF) && 0x7E == (bytes[length-1] & 0xFF)) {
                // 计算校验码
                byte check = 0x00;
                for(int i = 1; i <= length - 3; i++) {
                    check ^= bytes[i];
                }
                // 验证校验码
                if(bytes[length - 2] == check) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<DataPackTarget> extractBody(DataPack dataPack) {
        ByteBuf buffer = null;
        List<DataPackTarget> dataPackTargetList = null;
        byte[] dataPackBytes = Base64.getDecoder().decode(dataPack.getDataB64());
        if(validate(dataPackBytes)) {
            try {
                // 初始化ByteBuf
                buffer = Unpooled.wrappedBuffer(dataPackBytes);

                // 跳过标识位(0x7e)
                buffer.readBytes(1);

                // 消息ID
                int msgId = JTT808DataPackUtil.readWord(buffer);
                System.out.println(msgId);


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
