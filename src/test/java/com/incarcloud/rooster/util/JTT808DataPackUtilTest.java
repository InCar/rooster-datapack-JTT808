package com.incarcloud.rooster.util;

import com.incarcloud.rooster.datapack.DataPackAlarm;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * JTT808DataPackUtilTest
 *
 * @author Aaric, created on 2017-08-24T10:40.
 * @since 2.0
 */
public class JTT808DataPackUtilTest {

    @Test
    public void testDetailAlarmProps() {
        long alarmProps = 0xAAAAAAAA;
        List<DataPackAlarm.Alarm> alarmList = JTT808DataPackUtil.detailAlarmProps(alarmProps);
        Assert.assertEquals(14L, alarmList.size());
    }
}
