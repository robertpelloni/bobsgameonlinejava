package com.bobsgame.client.network;

import java.net.InetSocketAddress;
import io.netty.channel.ChannelHandlerContext;

public interface UDPInterface
{
	void incomingPeerConnectResponse(String s);

	void handleMessage(ChannelHandlerContext ctx, String s, InetSocketAddress sender);
}
