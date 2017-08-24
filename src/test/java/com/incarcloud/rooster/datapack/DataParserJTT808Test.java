package com.incarcloud.rooster.datapack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DataParserJTT808Test
 *
 * @author Aaric, created on 2017-08-22T10:23.
 * @since 2.0
 */
public class DataParserJTT808Test {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(DataParserJTT808Test.class);

    private ByteBuf buffer;
    private IDataParser parser;

    @Before
    public void begin() {
        byte[] data = {
                //0----------心跳包
                0x7E, 0x00, 0x02, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x15, 0x00, 0x03, 0x16, 0x7E,
                //1----------终端注册
                0x7E, 0x01, 0x00, 0x00, 0x2D, 0x01, 0x33, 0x00, 0x31, 0x27, 0x07, 0x00, 0x0A, 0x00, 0x2C, 0x01, 0x2F, 0x37, 0x30, 0x31, 0x31, 0x31, 0x42, 0x53, 0x4A, 0x2D, 0x41, 0x36, 0x2D, 0x42, 0x44, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x33, 0x31, 0x32, 0x37, 0x30, 0x37, 0x01, (byte) 0xD4, (byte) 0xC1, 0x42, 0x38, 0x38, 0x38, 0x38, 0x38, 0x45, 0x7E
        };
        buffer = Unpooled.wrappedBuffer(data);
        parser = new DataParserJTT808();
    }

    @After
    public void end() {
        ReferenceCountUtil.release(buffer);
    }

    @Test
    public void testExtract() {
        Assert.assertNotEquals(0, parser.extract(buffer).size());
    }

    @Test
    public void testCreateResponse() {

    }

    @Test
    public void testDestroyResponse() {

    }

    @Test
    public void testExtractBody() {
        DataPack dataPack = parser.extract(buffer).get(1);
        parser.extractBody(dataPack);
    }

    @Test
    public void testGetMetaData() {

    }
}
