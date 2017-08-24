package com.incarcloud.rooster.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * JTT808DataPackUtilTest
 *
 * @author Aaric, created on 2017-08-24T10:40.
 * @since 2.0
 */
public class JTT808DataPackUtilTest {

    @Test
    public void testReadBCD() {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 0x15});
        Assert.assertEquals("020000000015", JTT808DataPackUtil.readBCD(buffer, 6));
        ReferenceCountUtil.release(buffer);
    }
}
