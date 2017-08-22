package com.incarcloud.rooster.datapack;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.junit.After;
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
    private static final Logger logger = LoggerFactory.getLogger(DataParserJTT808Test.class);

    /**
     * Data
     */
    private ByteBuf buffer;

    @Before
    public void begin() {
        byte[] data = {};
        buffer = Unpooled.wrappedBuffer(data);
    }

    @After
    public void end() {
        ReferenceCountUtil.release(buffer);
    }

    @Test
    public void testExtract() {

    }

    @Test
    public void testCreateResponse() {

    }

    @Test
    public void testDestroyResponse() {

    }

    @Test
    public void testExtractBody() {

    }

    @Test
    public void testGetMetaData() {

    }
}
