package com.bobsgame.client.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;

public interface UDPInterface {
	void sendAddressRequest();
	void sendPeerConnectRequest();
	void handleDisconnected();
	void incomingPeerConnectResponse(MessageEvent e);
	void update();
	void handleMessage(ChannelHandlerContext ctx, MessageEvent e);
}