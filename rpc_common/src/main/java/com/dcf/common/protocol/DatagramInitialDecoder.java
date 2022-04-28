package com.dcf.common.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 负责根据报头中长度分离数据报
 */
public class DatagramInitialDecoder extends LengthFieldBasedFrameDecoder {

    public DatagramInitialDecoder() {
        this(1024, 16, 4, 0, 0);
    }

    public DatagramInitialDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
