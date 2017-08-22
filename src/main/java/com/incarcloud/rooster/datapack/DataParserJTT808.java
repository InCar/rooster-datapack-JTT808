package com.incarcloud.rooster.datapack;

import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * JTT808 Parser.
 *
 * @author Aaric, created on 2017-08-22T10:22.
 * @since 2.0
 */
public class DataParserJTT808 implements IDataParser {

    @Override
    public List<DataPack> extract(ByteBuf buffer) {
        return null;
    }

    @Override
    public ByteBuf createResponse(DataPack requestPack, ERespReason reason) {
        return null;
    }

    @Override
    public void destroyResponse(ByteBuf responseBuf) {

    }

    @Override
    public List<DataPackTarget> extractBody(DataPack dataPack) {
        return null;
    }

    @Override
    public Map<String, Object> getMetaData(ByteBuf buffer) {
        return null;
    }
}
