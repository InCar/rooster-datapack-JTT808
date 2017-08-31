package com.incarcloud.rooster.datapack;

import com.incarcloud.rooster.gather.cmd.CommandType;
import com.incarcloud.rooster.util.JTT808DataPackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * JTT808命令工厂实现
 *
 * @author Aaric, created on 2017-08-25T10:29.
 * @since 2.0
 */
public class CommandFactoryJTT808 implements CommandFactory {

    static {
        /**
         * 声明数据包版本与解析器类关系
         */
        CommandFacotryManager.registerCommandFacotry(DataParserJTT808.PROTOCOL_PREFIX + DataParserJTT808.PROTOCOL_VERSION, CommandFactoryJTT808.class);
    }

    @Override
    public ByteBuf createCommand(CommandType type, Object... args) throws Exception {
        // 基本验证，必须有参数，第一个为终端手机号，即设备号
        if(null == args && 0 < args.length) {
            throw new IllegalArgumentException("args is null");
        }

        // 初始化List容器，装载【消息头+消息体】
        List<Byte> byteList = new ArrayList<>();

        // 预留回复命令字位置
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        // 预留消息长度位置
        byteList.add((byte) 0xFF);
        byteList.add((byte) 0xFF);

        // 设置终端手机号(6个字节BCD码)，5~10
        // 第一个参数为终端手机号，即设备号，验证字符串长度为12
        if(args[0] instanceof String && 12 == ((String) args[0]).length()) {
            String deviceId = (String) args[0];
            byteList.addAll(JTT808DataPackUtil.getBCDByteList(deviceId));
        } else {
            // 验证设备号不通过
            throw new IllegalArgumentException("deviceId is error");
        }

        // 设置消息流水号，默认流水号0x00
        byteList.add((byte) 0x00);
        byteList.add((byte) 0x00);
        /*====================begin-判断msgId回复消息-begin====================*/
        // 消息长度
        int msgLength = 0;

        // 判断命令类型
        switch (type) {
//            case 0x8001:
//                /**
//                 * 平台通用应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8001 - 平台通用应答");
//                break;
            case REISSUE_PACK:
                /* 补传分包请求 */
                System.out.println("## 0x8003 - 补传分包请求");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-原始消息流水号(msgSeq:int)
                 *   2-重传包总数(msgTotal:int)
                 *   3-重传包 ID 列表(msgIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x80);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 原始消息流水号
                int msgSeq = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(msgSeq));
                // 2.1 重传包总数
                int msgTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(msgTotal));
                // 2.1 重传包 ID 列表
                int[] msgIds = (int[]) args[3];
                for (int i = 0; i < msgTotal; i++) {
                    byteList.add(JTT808DataPackUtil.getIntegerByte(msgIds[i]));
                }

                // 3.设置消息长度
                msgLength = 2 + msgTotal;
                break;
//            case 0x8100:
//                /**
//                 * 终端注册应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8100 - 终端注册应答");
//                break;
            case SET_PARAMS:
                /* 设置终端参数 */
                System.out.println("## 0x8103 - 设置终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-参数总数(paramTotal:int)
                 *   2-参数项列表(paramListBytes: byte[]，按照表 11 终端参数项数据格式传入字节数组)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 参数总数
                int paramTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(paramTotal));
                // 2.2 参数项列表
                byte[] paramListBytes = (byte[]) args[2];
                for (int i = 0; i < paramListBytes.length; i++) {
                    byteList.add(paramListBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 1 + paramListBytes.length;
                break;
            case QUERY_ALL_PARAMS:
                /* 查询终端参数 */
                System.out.println("## 0x8104 - 查询终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x04);

                // 2.消息体(空)

                // 3.设置消息长度
                msgLength = 0;
                break;
            case TERMINAL_CONTROL:
                /* 终端控制 */
                System.out.println("## 0x8105 - 终端控制");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-命令字(commandId:int)
                 *   2-命令参数(commandArgs:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x05);

                // 2.消息体
                // 2.1 命令字
                int commandId = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(commandId));
                // 2.2 命令参数
                String commandArgs = (String) args[2];
                if(null != commandArgs && 0 < commandArgs.length()) {
                    byte[] commandArgsBytes = commandArgs.getBytes();
                    for (int i = 0; i < commandArgsBytes.length; i++) {
                        byteList.add(commandArgsBytes[i]);
                    }
                }

                // 3.设置消息长度
                msgLength = 1 + commandArgs.length();
                break;
            case QUERY_CUSTOM_PARAMS:
                /* 查询指定终端参数 */
                System.out.println("## 0x8106 - 查询指定终端参数");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-参数总数(paramTotal:int)
                 *   2-参数 ID 列表(params:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x06);

                // 2.消息体
                // 2.1 参数总数
                paramTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(paramTotal));
                // 2.2 参数 ID 列表
                int[] params = (int[]) args[2];
                for (int i = 0; i < params.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(params[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * paramTotal);
                break;
            case QUERY_ALL_ATTRS:
                /* 查询终端属性 */
                System.out.println("## 0x8107 - 查询终端属性");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x07);

                // 2.消息体(空)

                // 3.设置消息长度
                msgLength = 0;
                break;
            case UPGRADE:
                /* 下发终端升级包 */
                System.out.println("## 0x8108 - 下发终端升级包");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-升级类型(upgradeType:int)
                 *   2-制造商 ID(manufacturerIdBytes:byte[5])
                 *   3-版本号(upgradeersion:String)
                 *   4-升级数据包(upgradePackBytes)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x81);
                byteList.set(1, (byte) 0x08);

                // 2.消息体(空)
                // 2.1 升级类型
                int upgradeType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(upgradeType));
                // 2.2 制造商 ID
                byte[] manufacturerIdBytes = (byte[]) args[2];
                for (int i = 0; i < 5; i++) {
                    byteList.add(manufacturerIdBytes[i]);
                }
                // 2.3 版本号长度
                String upgradeVersion = (String) args[3];
                int upgradeVersionLength = upgradeVersion.length();
                byteList.add(JTT808DataPackUtil.getIntegerByte(upgradeVersionLength));
                // 2.4 版本号
                byte[] upgradeVersionBytes = upgradeVersion.getBytes();
                for (int i = 0; i < upgradeVersionBytes.length; i++) {
                    byteList.add(upgradeVersionBytes[i]);
                }
                // 2.5 升级数据包长度
                byte[] upgradePackBytes = (byte[]) args[4];
                int upgradePackLength = upgradePackBytes.length;
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(upgradePackLength));
                // 2.6 升级数据包
                for (int i = 0; i < upgradePackBytes.length; i++) {
                    byteList.add(upgradePackBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 10 + upgradeVersionLength + upgradePackLength;
                break;
            case QUERY_POSITION:
                /* 位置信息查询 */
                System.out.println("## 0x8201 - 位置信息查询");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x82);
                byteList.set(1, (byte) 0x01);

                // 2.消息体(空)

                // 3.设置消息长度
                msgLength = 0;
                break;
            case TRACKING_POSITION:
                /* 临时位置跟踪控制 */
                System.out.println("## 0x8202 - 临时位置跟踪控制");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-时间间隔(intervalSeconds:int)
                 *   2-位置跟踪有效期(expireSeconds:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x82);
                byteList.set(1, (byte) 0x02);

                // 2.消息体
                // 2.1 时间间隔
                int intervalSeconds = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(intervalSeconds));
                // 2.2 位置跟踪有效期
                int expireSeconds = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(expireSeconds));

                // 3.设置消息长度
                msgLength = 6;
                break;
            case CONFIRM_ALARM:
                /* 人工确认报警消息 */
                System.out.println("## 0x8203 - 人工确认报警消息");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-报警消息流水号(msgSeq:int)
                 *   2-人工确认报警类型(alarmType:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x82);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 报警消息流水号
                msgSeq = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(msgSeq));
                // 2.2 人工确认报警类型
                int alarmType = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(alarmType));

                // 3.设置消息长度
                msgLength = 6;
                break;
            case SEND_TEXT:
                /* 文本信息下发 */
                System.out.println("## 0x8300 - 文本信息下发");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-标志(textType:int)
                 *   2-文本信息(textString:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x83);
                byteList.set(1, (byte) 0x00);

                // 2.消息体
                // 2.1 标志
                int textType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(textType));
                // 2.2 文本信息
                String textString = (String) args[2];
                byte[] textStringBytes = JTT808DataPackUtil.getStringBytes(textString);
                for (int i = 0; i < textStringBytes.length; i++) {
                    byteList.add(textStringBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 1 + textStringBytes.length;
                break;
            case SET_EVENT:
                /* 事件设置 */
                System.out.println("## 0x8301 - 事件设置");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-类型(eventType:int)
                 *   2-总数(eventTotal:int)
                 *   3-事件 IDs(eventIds:int[])
                 *   4-事件内容s(eventContents:[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x83);
                byteList.set(1, (byte) 0x01);

                // 2.消息体
                // 2.1 类型
                int eventType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(eventType));
                // 2.2 总数
                int eventTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(eventTotal));
                // 2.3 事件项列表
                int[] eventIds = (int[]) args[3];
                String[] eventContents = (String[]) args[4];
                byte[] eventContentBytes;
                int eventMsgLength = 2;
                for (int i = 0; i < eventTotal; i++) {
                    // 2.3.1 事件 ID
                    byteList.add(JTT808DataPackUtil.getIntegerByte(eventIds[i]));
                    // 2.3.2 事件内容长度
                    eventContentBytes = JTT808DataPackUtil.getStringBytes(eventContents[i]);
                    byteList.add(JTT808DataPackUtil.getIntegerByte(eventContentBytes.length));
                    // 2.3.3 事件内容
                    for (int j = 0; j < eventContentBytes.length; j++) {
                        byteList.add(eventContentBytes[j]);
                    }
                    // 计算长度
                    eventMsgLength += 2;
                    eventMsgLength += eventContentBytes.length;
                }

                // 3.设置消息长度
                msgLength = eventMsgLength;
                break;
            case QUIZ:
                /* 提问下发 */
                System.out.println("## 0x8302 - 提问下发");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-标志(answerFlag:int)
                 *   2-问题(quizContent:String)
                 *   3-答案 IDs(answerIds:int[])
                 *   4-答案内容s(answerContents:String[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x83);
                byteList.set(1, (byte) 0x02);

                // 2.消息体
                // 2.1 标志
                int answerFlag = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(answerFlag));
                // 2.2 问题内容长度
                String quizContent = (String) args[2];
                byte[] quizContentBytes = JTT808DataPackUtil.getStringBytes(quizContent);
                byteList.add(JTT808DataPackUtil.getIntegerByte(quizContentBytes.length));
                // 2.3 问题
                for (int i = 0; i < quizContentBytes.length; i++) {
                    byteList.add(quizContentBytes[i]);
                }
                // 2.4 候选答案列表
                int answerListLength = 2 + quizContentBytes.length;
                int[] answerIds = (int[]) args[3];
                String[] answerContents = (String[]) args[4];
                byte[] answerContentBytes;
                for (int i = 0; i < answerIds.length; i++) {
                    // 2.4.1 答案 ID
                    byteList.add(JTT808DataPackUtil.getIntegerByte(answerIds[i]));
                    // 2.4.2 答案内容长度
                    answerContentBytes = JTT808DataPackUtil.getStringBytes(answerContents[i]);
                    byteList.addAll(JTT808DataPackUtil.getWordByteList(answerContentBytes.length));
                    // 2.4.3 答案内容
                    for (int j = 0; j < answerContentBytes.length; j++) {
                        byteList.add(answerContentBytes[j]);
                    }
                    // 计算长度
                    answerListLength += 3;
                    answerListLength += answerContentBytes.length;
                }

                // 3.设置消息长度
                msgLength = answerListLength;
                break;
            case SET_INFO_DEMAND_MENU:
                /* 信息点播菜单设置 */
                System.out.println("## 0x8303 - 信息点播菜单设置");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-设置类型(setType:int)
                 *   2-信息项总数(infoTotal:int)
                 *   3-信息类型s(infoTypes:int[])
                 *   4-信息名称s(infoTitles:String[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x83);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 设置类型
                int setType  = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(setType));
                // 2.2 信息项总数
                int infoTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(infoTotal));
                // 2.3 信息项列表
                int infoListLength = 2;
                int[] infoTypes = (int[]) args[3];
                String[] infoTitles = (String[]) args[4];
                byte[] infoTitleBytes;
                for (int i = 0; i < infoTotal; i++) {
                    // 2.3.1 信息类型
                    byteList.add(JTT808DataPackUtil.getIntegerByte(infoTypes[i]));
                    // 2.3.2 信息名称长度
                    infoTitleBytes = JTT808DataPackUtil.getStringBytes(infoTitles[i]);
                    byteList.addAll(JTT808DataPackUtil.getWordByteList(infoTitleBytes.length));
                    // 2.3.3 信息名称
                    for (int j = 0; j < infoTitleBytes.length; j++) {
                        byteList.add(infoTitleBytes[j]);
                    }
                    // 计算长度
                    infoListLength += 3;
                    infoListLength += infoTitleBytes.length;
                }

                // 3.设置消息长度
                msgLength = infoListLength;
                break;
            case INFO_SERVICE:
                /* 信息服务 */
                System.out.println("## 0x8304 - 信息服务");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-信息类型(infoType:int)
                 *   2-信息内容(infoContent:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x83);
                byteList.set(1, (byte) 0x04);

                // 2.消息体
                // 2.1 信息类型
                int infoType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(infoType));
                // 2.2 信息长度
                String infoContent = (String) args[2];
                byte[] infoContentBytes = JTT808DataPackUtil.getStringBytes(infoContent);
                byteList.addAll(JTT808DataPackUtil.getWordByteList(infoContentBytes.length));
                // 2.3 信息内容
                for (int i = 0; i < infoContentBytes.length; i++) {
                    byteList.add(infoContentBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 3 + infoContentBytes.length;
                break;
            case PHONE_DIAL:
                /* 电话回拨 */
                System.out.println("## 0x8400 - 电话回拨");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-标志(phoneFlag:int)
                 *   2-电话号码(phoneNumber:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x84);
                byteList.set(1, (byte) 0x00);

                // 2.消息体
                // 2.1 标志
                int phoneFlag = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(phoneFlag));
                // 2.2 电话号码
                String phoneNumber = (String) args[2];
                byte[] phoneNumberBytes = phoneNumber.getBytes();
                for (int i = 0; i < phoneNumberBytes.length; i++) {
                    byteList.add(phoneNumberBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 1 + phoneNumberBytes.length;
                break;
            case SET_PHONE_LIST:
                /* 设置电话本 */
                System.out.println("## 0x8401 - 设置电话本");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-设置类型(setType:int)
                 *   2-联系人总数(phoneTotal:int)
                 *   3-标志s(phoneFlags:int[])
                 *   4-电话号码s(phoneNumbers:String[])
                 *   5-联系人s(phoneNames:String[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x84);
                byteList.set(1, (byte) 0x01);

                // 2.消息体
                // 2.1 设置类型
                setType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(setType));
                // 2.2 联系人总数
                int phoneTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(phoneTotal));
                // 2.3 联系人项
                int phoneListLength = 2;
                int[] phoneFlags = (int[]) args[3];
                String[] phoneNumbers = (String[]) args[4];
                //byte[] phoneNumberBytes;
                String[] phoneNames = (String[]) args[5];
                byte[] phoneNameBytes;
                for (int i = 0; i < phoneTotal; i++) {
                    // 2.3.1 标志：1：呼入；2：呼出；3：呼入/呼出
                    byteList.add(JTT808DataPackUtil.getIntegerByte(phoneFlags[i]));
                    // 2.3.2 号码长度
                    phoneNumberBytes = phoneNumbers[i].getBytes();
                    byteList.add(JTT808DataPackUtil.getIntegerByte(phoneNumberBytes.length));
                    // 2.3.3 电话号码
                    for (int j = 0; j < phoneNumberBytes.length; j++) {
                        byteList.add(phoneNumberBytes[j]);
                    }
                    // 2.3.4 联系人长度
                    phoneNameBytes = JTT808DataPackUtil.getStringBytes(phoneNames[i]);
                    byteList.add(JTT808DataPackUtil.getIntegerByte(phoneNameBytes.length));
                    // 2.3.5 联系人
                    for (int j = 0; j < phoneNameBytes.length; j++) {
                        byteList.add(phoneNameBytes[j]);
                    }
                    // 计算长度
                    phoneListLength += 3;
                    phoneListLength += phoneNumberBytes.length;
                    phoneListLength += phoneNameBytes.length;
                }

                // 3.设置消息长度
                msgLength = phoneListLength;
                break;
            case VEHICLE_CONTROL:
                /* 车辆控制 */
                System.out.println("## 0x8500 - 车辆控制");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-控制标志(controlFlag:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x85);
                byteList.set(1, (byte) 0x00);

                // 2.消息体
                // 2.1 控制标志：0：车门解锁；1：车门加锁
                int controlFlag = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(controlFlag));

                // 3.设置消息长度
                msgLength = 1;
                break;
            case SET_AREA_CIRCULAR:
                /* 设置圆形区域 */
                System.out.println("## 0x8600 - 设置圆形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-设置属性(setType:int)
                 *   2-区域总数(areaTotal:int)
                 *   3-区域项(areaItemBytes:byte[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x00);

                // 2.消息体
                // 2.1 设置属性
                setType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(setType));
                // 2.2 区域总数
                int areaTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(areaTotal));
                // 2.3 区域项
                byte[] areaItemBytes = (byte[]) args[3];
                for (int i = 0; i < areaItemBytes.length; i++) {
                    byteList.add(areaItemBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 2 + areaItemBytes.length;
                break;
            case DELETE_AREA_CIRCULAR:
                /* 删除圆形区域 */
                System.out.println("## 0x8601 - 删除圆形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-区域数(areaTotal:int)
                 *   2-区域 IDs(areaIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x01);

                // 2.消息体
                // 2.1 区域数
                areaTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(areaTotal));
                // 2.2 区域 ID
                int[] areaIds = (int[]) args[2];
                for (int i = 0; i < areaIds.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(areaIds[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * areaTotal);
                break;
            case SET_AREA_RECTANGLE:
                /* 设置矩形区域 */
                System.out.println("## 0x8602 - 设置矩形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-设置属性(setType:int)
                 *   2-区域总数(areaTotal:int)
                 *   3-区域项(areaItemBytes:byte[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x02);

                // 2.消息体
                // 2.1 设置属性
                setType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(setType));
                // 2.2 区域总数
                areaTotal = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(areaTotal));
                // 2.3 区域项
                areaItemBytes = (byte[]) args[3];
                for (int i = 0; i < areaItemBytes.length; i++) {
                    byteList.add(areaItemBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 2 + areaItemBytes.length;
                break;
            case DELETE_AREA_RECTANGLE:
                /* 删除矩形区域 */
                System.out.println("## 0x8603 - 删除矩形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-区域数(areaTotal:int)
                 *   2-区域 IDs(areaIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 区域数
                areaTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(areaTotal));
                // 2.2 区域 ID
                areaIds = (int[]) args[2];
                for (int i = 0; i < areaIds.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(areaIds[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * areaTotal);
                break;
            case SET_AREA_POLYGON:
                /* 设置多边形区域 */
                System.out.println("## 0x8604 - 设置多边形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-区域 ID(areaId:int)
                 *   2-区域属性(areaPros:int)
                 *   3-起始时间(beginTime:Date)
                 *   4-结束时间(endTime:Date)
                 *   5-最高速度(maxSpeed:int)
                 *   6-超速持续时间(maxSpeedSeconds:int)
                 *   7-区域总顶点数(pointTotal:int)
                 *   8-顶点纬度s(latitudes:double[])
                 *   9-顶点经度s(longitude:double[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x05);

                // 2.消息体
                // 2.1 区域 ID
                int areaId = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(areaId));
                // 2.2 区域属性
                int areaPros = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(areaPros));
                // 2.3 起始时间
                DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                Date beginTime = (Date) args[3];
                byte[] timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(beginTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.4 结束时间
                Date endTime = (Date) args[4];
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(endTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.5 最高速度
                int subMsgLength = 0;
                int maxSpeed = (int) args[5];
                if(1 == ((areaPros >> 1) & 0x01)) {
                    // 若区域属性 1 位为 0 则没有该字段
                    byteList.addAll(JTT808DataPackUtil.getWordByteList(maxSpeed));
                } else {
                    subMsgLength += 2;
                }
                // 2.6 超速持续时间
                int maxSpeedSeconds = (int) args[6];
                if(1 == ((areaPros >> 1) & 0x01)) {
                    // 若区域属性 1 位为 0 则没有该字段
                    byteList.add(JTT808DataPackUtil.getIntegerByte(maxSpeedSeconds));
                } else {
                    subMsgLength += 1;
                }
                // 2.7 区域总顶点数
                int pointTotal = (int) args[7];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(pointTotal));
                // 2.8 顶点项
                double[] latitudes = (double[]) args[8];
                double[] longitude = (double[]) args[9];
                for (int i = 0; i < pointTotal; i++) {
                    // 2.8.1 顶点纬度
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(new Double(latitudes[i] * 10e6).intValue()));
                    // 2.8.2 顶点经度
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(new Double(longitude[i] * 10e6).intValue()));
                }

                // 3.设置消息长度
                msgLength = 23 + (8 * pointTotal);
                break;
            case DELETE_AREA_POLYGON:
                /* 删除多边形区域 */
                System.out.println("## 0x8605 - 删除多边形区域");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-区域数(areaTotal:int)
                 *   2-区域 IDs(areaIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x05);

                // 2.消息体
                // 2.1 区域数
                areaTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(areaTotal));
                // 2.2 区域 ID
                areaIds = (int[]) args[2];
                for (int i = 0; i < areaIds.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(areaIds[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * areaTotal);
                break;
            case SET_LINE:
                /* 设置路线 */
                System.out.println("## 0x8606 - 设置路线");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-路线 ID(lineId:int)
                 *   2-路线属性(lineProps:int)
                 *   3-起始时间(beginTime:Date)
                 *   4-结束时间(endTime:Date)
                 *   5-路线总拐点数(pointTotal)
                 *   6-拐点项(pointBytes:byte[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x06);

                // 2.消息体
                // 2.1 路线 ID
                int lineId = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(lineId));
                // 2.2 路线属性
                int lineProps = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(lineProps));
                // 2.3 起始时间
                beginTime = (Date) args[3];
                dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(beginTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.4 结束时间
                endTime = (Date) args[4];
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(endTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.5 路线总拐点数
                pointTotal = (int) args[5];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(pointTotal));
                // 2.6 拐点项
                byte[] pointBytes = (byte[]) args[6];
                for (int i = 0; i < pointBytes.length; i++) {
                    byteList.add(pointBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 20 + pointBytes.length;
                break;
            case DELETE_LINE:
                /* 删除路线 */
                System.out.println("## 0x8607 - 删除路线");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-路线数(lineTotal:int)
                 *   2-路线 IDs(lineIds:int[])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x86);
                byteList.set(1, (byte) 0x07);

                // 2.消息体
                // 2.1 路线数
                int lineTotal = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(lineTotal));
                // 2.2 路线 IDs
                int[] lineIds = (int[]) args[2];
                for (int i = 0; i < lineIds.length; i++) {
                    byteList.addAll(JTT808DataPackUtil.getDWordByteList(lineIds[i]));
                }

                // 3.设置消息长度
                msgLength = 1 + (4 * lineTotal);
                break;
//            case 0x8700:
//                /**
//                 * 行驶记录仪数据采集命令<br>
//                 * 关联GB/T 19056，暂时不予实现
//                 */
//                System.out.println("## 0x8700 - 行驶记录仪数据采集命令");
//                break;
//            case 0x8701:
//                /**
//                 * 行驶记录仪参数下传命令<br>
//                 * 关联GB/T 19056，暂时不予实现
//                 */
//                System.out.println("## 0x8701 - 行驶记录仪参数下传命令");
//                break;
            case SEND_DRIVER_ID:
                /* 上报驾驶员身份信息请求 */
                System.out.println("## 0x8702 - 上报驾驶员身份信息请求");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x87);
                byteList.set(1, (byte) 0x02);

                // 2.消息体(空)

                // 3.设置消息长度
                msgLength = 0;
                break;
//            case 0x8800:
//                /**
//                 * 多媒体数据上传应答<br>
//                 * @see DataParserJTT808#createResponse(DataPack, ERespReason)
//                 */
//                System.out.println("## 0x8800 - 多媒体数据上传应答");
//                break;
            case TAKE_PHOTO:
                /* 摄像头立即拍摄命令 */
                System.out.println("## 0x8801 - 摄像头立即拍摄命令");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-通道 ID(photoChannelId:int)
                 *   2-拍摄命令(photoCommand:int)
                 *   3-拍照间隔/录像时间(photoTimeSeconds:int)
                 *   4-保存标志(photoSaveFlag:int)
                 *   5-分辨率(photoRP:int)
                 *   6-图像/视频质量(photoQuality:int)
                 *   7-亮度(photoBrightness:int)
                 *   8-对比度(photoContrast:int)
                 *   9-饱和度(photoSaturation:int)
                 *   10-色度(photoChroma:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x88);
                byteList.set(1, (byte) 0x01);

                // 2.消息体
                // 2.1 通道 ID
                int photoChannelId = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoChannelId));
                // 2.2 拍摄命令：0 表示停止拍摄；0xFFFF 表示录像；其它表示拍照张数
                int photoCommand = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(photoCommand));
                // 2.3 拍照间隔/录像时间
                int photoTimeSeconds = (int) args[3];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(photoTimeSeconds));
                // 2.4 保存标志：1：保存；0：实时上传
                int photoSaveFlag = (int) args[4];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoSaveFlag));
                // 2.5 分辨率
                int photoRP = (int) args[5];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoRP));
                // 2.6 图像/视频质量
                int photoQuality = (int) args[6];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoQuality));
                // 2.7 亮度
                int photoBrightness = (int) args[7];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoBrightness));
                // 2.8 对比度
                int photoContrast = (int) args[8];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoContrast));
                // 2.9 饱和度
                int photoSaturation = (int) args[9];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoSaturation));
                // 2.10 色度
                int photoChroma = (int) args[10];
                byteList.add(JTT808DataPackUtil.getIntegerByte(photoChroma));

                // 3.设置消息长度
                msgLength = 12;
                break;
            case QUERY_MEDIA:
                /* 存储多媒体数据检索 */
                System.out.println("## 0x0802 - 存储多媒体数据检索");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-多媒体类型(mediaType:int)
                 *   2-通道 ID(mediaChannelId:int)
                 *   3-事件项编码(eventCode:int)
                 *   4-起始时间(beginTime:Date)
                 *   5-结束时间(endTime:Date)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x08);
                byteList.set(1, (byte) 0x02);

                // 2.消息体
                // 2.1 多媒体类型
                int mediaType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(mediaType));
                // 2.2 通道 ID
                int mediaChannelId = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(mediaChannelId));
                // 2.3 事件项编码
                int eventCode = (int ) args[3];
                byteList.add(JTT808DataPackUtil.getIntegerByte(eventCode));
                // 2.4 起始时间
                beginTime = (Date) args[4];
                dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(beginTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.5 结束时间
                endTime = (Date) args[5];
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(endTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }

                // 3.设置消息长度
                msgLength = 15;
                break;
            case UPDATE_MEDIA:
                /* 存储多媒体数据上传 */
                System.out.println("## 0x8803 - 存储多媒体数据上传");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-多媒体类型(mediaType:int)
                 *   2-通道 ID(mediaChannelId:int)
                 *   3-事件项编码(eventCode:int)
                 *   4-起始时间(beginTime:Date)
                 *   5-结束时间(endTime:Date)
                 *   6-删除标志(deleteFlag:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x88);
                byteList.set(1, (byte) 0x03);

                // 2.消息体
                // 2.1 多媒体类型
                mediaType = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(mediaType));
                // 2.2 通道 ID
                mediaChannelId = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(mediaChannelId));
                // 2.3 事件项编码
                eventCode = (int ) args[3];
                byteList.add(JTT808DataPackUtil.getIntegerByte(eventCode));
                // 2.4 起始时间
                beginTime = (Date) args[4];
                dateFormat = new SimpleDateFormat("yyMMddHHmmss");
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(beginTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.5 结束时间
                endTime = (Date) args[5];
                timeBytes = JTT808DataPackUtil.getStringBytes(dateFormat.format(endTime));
                for (int i = 0; i < timeBytes.length; i++) {
                    byteList.add(timeBytes[i]);
                }
                // 2.6 删除标志
                int deteteFlag = (int) args[6];
                byteList.add(JTT808DataPackUtil.getIntegerByte(deteteFlag));

                // 3.设置消息长度
                msgLength = 16;
                break;
            case SOUND_RECORDING:
                /* 录音开始命令 */
                System.out.println("## 0x8804 - 录音开始命令");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-录音命令(recordCommand:int)
                 *   2-录音时间(recordSeconds:int)
                 *   3-保存标志(recordSaveFlag:int)
                 *   4-音频采样率(recordRate:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x88);
                byteList.set(1, (byte) 0x04);

                // 2.消息体
                // 2.1 录音命令
                int recordCommand = (int) args[1];
                byteList.add(JTT808DataPackUtil.getIntegerByte(recordCommand));
                // 2.2 录音时间
                int recordSeconds = (int) args[2];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(recordSeconds));
                // 2.3 保存标志
                int recordSaveFlag = (int) args[3];
                byteList.add(JTT808DataPackUtil.getIntegerByte(recordSaveFlag));
                // 2.4 音频采样率
                int recordRate = (int) args[4];
                byteList.add(JTT808DataPackUtil.getIntegerByte(recordRate));

                // 3.设置消息长度
                msgLength = 5;
                break;
            case UPDATE_SINGLE_MEDIA:
                /* 单条存储多媒体数据检索上传命令 */
                System.out.println("## 0x8805 - 单条存储多媒体数据检索上传命令");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-多媒体 ID(mediaId:int)
                 *   2-删除标志(deleteFlag:int)
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x88);
                byteList.set(1, (byte) 0x05);

                // 2.消息体
                // 2.1 多媒体 ID
                int mediaId = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getWordByteList(mediaId));
                // 2.2 删除标志
                int deleteFlag = (int) args[2];
                byteList.add(JTT808DataPackUtil.getIntegerByte(deleteFlag));

                // 3.设置消息长度
                msgLength = 3;
                break;
//            case 0x8900:
//                /**
//                 * 数据下行透传<br>
//                 * 透传消息内容不明确，暂时不予实现
//                 */
//                System.out.println("## 0x8900 - 数据下行透传");
//                break;
            case RSA:
                /* 平台 RSA 公钥 */
                System.out.println("## 0x8A00 - 平台 RSA 公钥");
                /**
                 * 参数说明：
                 *   0-设置终端手机号(deviceId:String)
                 *   1-平台 RSA 公钥{e,n}中的 e(rsaE:int)
                 *   2-RSA 公钥{e,n}中的 n(rsaN:byte[128])
                 */
                // 1.设置消息ID
                byteList.set(0, (byte) 0x8A);
                byteList.set(1, (byte) 0x00);

                // 2.消息体
                // 2.1 平台 RSA 公钥{e,n}中的 e
                int rsaE = (int) args[1];
                byteList.addAll(JTT808DataPackUtil.getDWordByteList(rsaE));
                // 2.2 RSA 公钥{e,n}中的 n
                byte[] rsaN = (byte[]) args[2];
                for (int i = 0; i < rsaN.length; i++) {
                    byteList.add(rsaN[i]);
                }

                // 3.设置消息长度
                msgLength = 4 + rsaN.length;
                break;
        }
        /*====================end---判断msgId回复消息---end====================*/
        // 设置长度信息
        // 双字节，0x00-不分包，数据不加密
        int msgProps = 0x00 | msgLength;
        byteList.set(2, (byte) ((msgProps >> 8) & 0xFF));
        byteList.set(3, (byte) (msgProps & 0xFF));

        // 计算并填充校验码
        byte check = 0x00;
        for (Byte byteObject: byteList) {
            check ^= byteObject.byteValue();
        }
        byteList.add(check);

        // 转义
        List<Byte> defaultByteList = new ArrayList<>();
        defaultByteList.add((byte) 0x7E); //标识位(0x7E)
        for(Byte byteObject: byteList) {
            if(0x7D == (byteObject.byteValue() & 0xFF)) {
                // 0x7D->0x7D0x01
                defaultByteList.add((byte) 0x7D);
                defaultByteList.add((byte) 0x01);
            } else if(0x7E == (byteObject.byteValue() & 0xFF)) {
                // 0x7E->0x7D0x02
                defaultByteList.add((byte) 0x7D);
                defaultByteList.add((byte) 0x02);
            } else {
                defaultByteList.add(byteObject);
            }
        }
        defaultByteList.add((byte) 0x7E); //标识位(0x7E)

        // add to buffer
        byte[] responseBytes = new byte[defaultByteList.size()];
        for (int i = 0; i < responseBytes.length; i++) {
            responseBytes[i] = defaultByteList.get(i);
        }

        // return
        return Unpooled.wrappedBuffer(responseBytes);
    }
}
