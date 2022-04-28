package com.dcf.common.protocol;

public class TestMessageCodec {
    public static void main(String[] args) throws Exception {
        /*EmbeddedChannel channel = new EmbeddedChannel(
                // 解决黏包半包问题
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),new MessageCodec());

        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "hahah");

        channel.writeOutbound(message);

        // 测试解码
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,message,buf);
        // channel.writeInbound(buf);

        // 半包
        ByteBuf slice1 = buf.slice(0, 100);
        ByteBuf slice2 = buf.slice(100, buf.readableBytes()-100);
        slice1.retain();
        channel.writeInbound(slice1);
        channel.writeInbound(slice2);*/
    }
}
