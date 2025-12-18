package com.bobsgame.client.network;

import io.netty.channel.ChannelHandlerContext;

public interface UDPInterface {
	void sendAddressRequest();
	void sendPeerConnectRequest();
	void handleDisconnected();
	void incomingPeerConnectResponse(String msg);
	void update();
	void handleMessage(ChannelHandlerContext ctx, String msg);
}
