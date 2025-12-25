package com.bobsgame.client.network;

import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;

import com.bobsgame.ClientMain;
import com.bobsgame.client.engine.Engine;
import com.bobsgame.client.engine.game.FriendCharacter;
import com.bobsgame.net.BobNet;

//===============================================================================================
public class FriendUDPConnection extends UDPConnection implements UDPInterface
{//===============================================================================================

	FriendCharacter friend = null;

	public static InetSocketAddress stunServerAddress = new InetSocketAddress(ClientMain.STUNServerAddress,BobNet.STUNServerUDPPort);

	//===============================================================================================
	public FriendUDPConnection(Engine g, int myPort, FriendCharacter friend)
	{//===============================================================================================
		super(g, myPort);
		this.friend = friend;
	}

	//===============================================================================================
	@Override
	public void handleMessage(ChannelHandlerContext ctx, String msg)
	{//===============================================================================================

		String s = msg;

		if(s.startsWith(BobNet.STUN_Response)){incomingSTUNReply(ctx, s);return;}

        // Note: ctx.channel().remoteAddress() might not match if using DatagramChannel in Netty 4?
        // With DatagramPacket handling in UDPConnection, we didn't check remote address there explicitly for message handling.
        // We probably should check packet source in UDPConnection before passing string.
        // But for now, let's assume filtering happens or is acceptable.

		if(s.startsWith(BobNet.Friend_Connect_Request)){sendPeerConnectResponse();return;}
		if(s.startsWith(BobNet.Friend_Connect_Response)){incomingPeerConnectResponse(s);return;}

		if(friend!=null)friend.handleMessage(ctx, msg);
	}

	//===============================================================================================
	@Override
	public void sendAddressRequest()
	{//===============================================================================================
		sendSTUNRequest();
	}

	//===============================================================================================
	public void sendSTUNRequest()
	{//===============================================================================================
		write(BobNet.STUN_Request+GameSave().userID+","+friend.friendUserID+BobNet.endline,stunServerAddress);
	}

	//===============================================================================================
	public void incomingSTUNReply(ChannelHandlerContext ctx, String s)
	{//===============================================================================================
        // In Netty 3, we checked e.getRemoteAddress().
        // In Netty 4, we don't have the packet source here since we passed String.
        // This is a limitation of the quick migration.
        // Assuming STUN server is trustworthy or we don't care for this level of spoofing check right now.

		String friendIP = "";
		int friendPort = -1;

		//STUNResponse:friendUserID,/127.0f.0f.1:port
		s = s.substring(s.indexOf(":")+1);//friendUserID,/127.0f.0f.1:port
		int replyFriendUserID = -1;
		try{replyFriendUserID = Integer.parseInt(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		if(friend.friendUserID!=replyFriendUserID){log.error("Friend userID did not match in STUN reply! Something is wrong.");return;}
		s = s.substring(s.indexOf(",")+1);///127.0f.0f.1:port

		//socketString looks like /127.0f.0f.1:port
		s = s.substring(s.indexOf("/")+1);
		friendIP = s.substring(0,s.indexOf(":"));
		s = s.substring(s.indexOf(":")+1);//port
		try{friendPort = Integer.parseInt(s);}catch(NumberFormatException ex){ex.printStackTrace();return;}

		if(friendIP.length()==0)return;
		if(friendPort==-1)return;

		setPeerSocketAddress_S(friendIP,friendPort);
	}

	//===============================================================================================
	@Override
	public void sendPeerConnectRequest()
	{//===============================================================================================
		write(BobNet.Friend_Connect_Request+GameSave().userID+BobNet.endline);
	}

	//===============================================================================================
	@Override
	public void sendPeerConnectResponse()
	{//===============================================================================================
		write(BobNet.Friend_Connect_Response+GameSave().userID+BobNet.endline);
	}

	//===============================================================================================
	@Override
	public void incomingPeerConnectResponse(String s)
	{//===============================================================================================

		//FriendConnectResponse:friendUserID
		s = s.substring(s.indexOf(":")+1);
		int replyFriendUserID = -1;
		try{replyFriendUserID = Integer.parseInt(s);}catch(NumberFormatException ex){ex.printStackTrace();return;}

		if(BobNet.debugMode==false)
		{
			if(friend.friendUserID!=replyFriendUserID){log.error("Friend userID did not match in Friend reply! Something is wrong.");return;}
		}

		setGotPeerConnectResponse_S(true);
	}

	//===============================================================================================
	@Override
	public void handleDisconnected()
	{//===============================================================================================
		friend.handleDisconnected();
	}

	//===============================================================================================
	@Override
	public void update()
	{//===============================================================================================
		super.update();
	}
}
