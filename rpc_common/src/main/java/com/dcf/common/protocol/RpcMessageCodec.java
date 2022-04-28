package com.dcf.common.protocol;

import com.dcf.common.config.Config;
import com.dcf.common.message.Message;
import com.dcf.common.serialization.SerializerAlgorithm;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;

@Slf4j
/**
 * 允许在多个channel中共享此handler
 */
@ChannelHandler.Sharable
public class RpcMessageCodec extends MessageToMessageCodec<ByteBuf, Message> {
    /**
     * header format
     * <pre>
     * +-----------------------------------+
     * |              magicNum             |
     * +--------+--------+--------+--------+
     * |   os   | jdkVer | msgVer |seriType|
     * +--------+--------+--------+--------+
     * |msgType |        sequenceId        |
     * +--------+--------+--------+--------+
     * |        |      padding bytes       |
     * +-----------------------------------+
     * |            body length            |
     * +-----------------------------------+
     * </pre>
     * @param ctx
     * @param msg
     * @param outList
     * @throws Exception
     */
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4B 魔数
        out.writeBytes(new byte[]{1, 2, 0, 7});
        // 2. 1B os  linux 0 window 1
        Properties properties = System.getProperties();
        String osName = properties.getProperty("os.name");
        if (osName.toLowerCase().contains("linux")) {
            out.writeByte(0);
        }else if(osName.toLowerCase().contains("window")){
            out.writeByte(1);
        }else {
            out.writeByte(2);
        }
        // 3. 1B javaVersion
        String javaVer = properties.getProperty("java.version");
        int ver = Integer.parseInt(javaVer.substring(0,javaVer.indexOf('.')));
        out.writeByte(ver);
        // 4. 1B msg版本,
        out.writeByte(1);
        // 5. 1B 序列化方式 jdk 0 , json 1
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 6. 1B msg类型
        out.writeByte(msg.getMessageType());
        // 7. 4B sequenceId
        out.writeInt(msg.getSequenceId());
        // 8. 3B
        out.writeBytes(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff});
        // 6. 获取内容的字节数组
        byte[] bytes = Config.getSerializerAlgorithm().getSerializer().serialize(msg);
        // 7. 4B body长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        if (magicNum != 16908295) {
            return;
        }
        byte os = in.readByte();
        byte javaVer = in.readByte();
        byte version = in.readByte();

        byte serializerAlgorithm = in.readByte(); // 0 或 1
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readBytes(3);
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);

        // 找到反序列化算法
        SerializerAlgorithm algorithm = SerializerAlgorithm.values()[serializerAlgorithm];
        // 确定具体消息类型
        Class<? extends Message> messageClass = Message.getMessageClass(messageType);
        Message message = algorithm.getSerializer().deserialize(messageClass, bytes);

        out.add(message);
    }

    public static void main(String[] args) {
        /*ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byteBuf.writeBytes(new byte[]{1,2,0,7});
        System.out.println(byteBuf.readInt());*/
        Properties properties = System.getProperties();
        System.out.println(properties.getProperty("os.name"));
        System.out.println(properties.get("java.version"));
    }

}
