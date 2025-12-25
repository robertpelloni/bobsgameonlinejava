package com.bobsgame.server;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.bobsgame.ServerMain;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import com.bobsgame.net.BobNet;
import com.bobsgame.net.BobsGameClient;
import com.bobsgame.net.BobsGameRoom;
import com.bobsgame.net.PrivateCredentials;

//=========================================================================================================================
public class IndexClientTCP
{//=========================================================================================================================

	Bootstrap clientBootstrap;
	ChannelFuture channelFuture;
	Channel channel;
	Timer timer;
    EventLoopGroup workerGroup;

	int serverID = -1;

	public static Logger log = (Logger)LoggerFactory.getLogger(IndexClientTCP.class);

	//=========================================================================================================================
	public IndexClientTCP()
	{//=========================================================================================================================

		//Initialize the timer that schedules subsequent reconnection attempts.
		timer = new HashedWheelTimer();

        workerGroup = new NioEventLoopGroup();

		//Configure the client.
		clientBootstrap = new Bootstrap();
        clientBootstrap.group(workerGroup);
        clientBootstrap.channel(NioSocketChannel.class);

        clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
        clientBootstrap.option(ChannelOption.SO_SNDBUF, 65536);
        clientBootstrap.option(ChannelOption.SO_RCVBUF, 65536);

        clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());

                pipeline.addLast("handler", new IndexClientHandler());
            }
        });

		int serverPort = BobNet.INDEXServerTCPPort;
		String serverAddress = ServerMain.INDEXServerAddress;
		if(new File("/localServer").exists())
		{
			//serverPort++;
			serverAddress = "127.0.0.1";
		}

		// clientBootstrap.setOption("remoteAddress", new InetSocketAddress(serverAddress, serverPort));
        // In Netty 4, remoteAddress is set during connect()

		connectToServer(serverAddress, serverPort);
	}

	//===============================================================================================
	public class IndexClientHandler extends ChannelInboundHandlerAdapter
	{//===============================================================================================

		//===============================================================================================
		public IndexClientHandler()
		{//===============================================================================================
			super();
		}

		//===============================================================================================
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception
		{//===============================================================================================
			log.warn("channelDisconnected to INDEX: ChannelID: "+ctx.channel().id());

			setConnectedToServer_S(false);

			channel = null;

            final int RECONNECT_DELAY = 1;//seconds

            log.warn("channelClosed to INDEX - Sleeping for " + RECONNECT_DELAY + " seconds: ChannelID: "+ctx.channel().id());

            timer.newTimeout(new TimerTask()
            {
                public void run(Timeout timeout) throws Exception
                {
                    log.warn("channelClosed TimerTask - Reconnecting to INDEX");

                    String serverAddress = ServerMain.INDEXServerAddress;
                    if(new File("/localServer").exists()) serverAddress = "127.0.0.1";
                    connectToServer(serverAddress, BobNet.INDEXServerTCPPort);
                }
            }, RECONNECT_DELAY, TimeUnit.SECONDS);

            super.channelInactive(ctx);
		}

		//===============================================================================================
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception
		{//===============================================================================================
			log.info("channelConnected to INDEX: ChannelID: "+ctx.channel().id());

			channel = ctx.channel();

			setConnectedToServer_S(true);

			send_INDEX_Register_Server_Request();

            super.channelActive(ctx);
		}

		//===============================================================================================
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
		{//===============================================================================================
			if(cause instanceof ConnectException)
			{
				log.error("Exception caught connecting to INDEX - ConnectException: "+cause.getMessage());
			}
			else
			if(cause instanceof ReadTimeoutException)
			{
				log.error("Exception caught connecting to INDEX - ReadTimeoutException: "+cause.getMessage());
			}
			else
			{
				log.error("Unexpected Exception caught connecting to INDEX: "+cause.getMessage());
				cause.printStackTrace();
			}

			ctx.close();
		}

		//===============================================================================================
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
		{//===============================================================================================

			String s = (String) msg;

			if(s.startsWith("ping"))
			{
				//log.debug("INDEX: ping");
				write(ctx.channel(),"pong"+BobNet.endline);
				return;
			}
			else if(s.startsWith("pong")){}
			else if(BobNet.debugMode)log.info("FROM INDEX: cID:"+ctx.channel().id()+" | "+s);

			if(s.startsWith(BobNet.Server_Register_Server_With_INDEX_Response)){incoming_Server_Registered_With_INDEX_Response(s);return;}

			if(s.startsWith(BobNet.Server_Tell_All_FacebookIDs_That_UserID_Is_Online)){incoming_Server_Tell_All_FacebookIDs_That_UserID_Is_Online(ctx, s);return;}
			if(s.startsWith(BobNet.Server_Tell_All_UserNames_That_UserID_Is_Online)){incoming_Server_Tell_All_UserNames_That_UserID_Is_Online(ctx, s);return;}
			if(s.startsWith(BobNet.Server_Tell_UserID_That_UserIDs_Are_Online)){incoming_Server_Tell_UserID_That_UserIDs_Are_Online(s);return;}
			if(s.startsWith(BobNet.Server_UserID_Logged_On_Other_Server_So_Log_Them_Off)){incoming_Server_UserID_Logged_On_Other_Server_So_Log_Them_Off(s);return;}
			if(s.startsWith(BobNet.Server_Tell_All_Users_Servers_Are_Shutting_Down)){incoming_Server_Tell_All_Users_Servers_Are_Shutting_Down(s);return;}
			if(s.startsWith(BobNet.Server_Tell_All_Users_Servers_Have_Shut_Down)){incoming_Server_Tell_All_Users_Servers_Have_Shut_Down(s);return;}

			if(s.startsWith(BobNet.Server_Bobs_Game_Hosting_Room_Update)){incoming_Server_Bobs_Game_Hosting_Room_Update(s);return;}
			if(s.startsWith(BobNet.Server_Bobs_Game_Remove_Room)){incoming_Server_Bobs_Game_Remove_Room(s);return;}
			if(s.startsWith(BobNet.Server_Send_Activity_Update_To_All_Clients)){incoming_Server_Send_Activity_Update_To_All_Clients(s);return;}
			if(s.startsWith(BobNet.Server_Send_Chat_Message_To_All_Clients)){incoming_Server_Send_Chat_Message_To_All_Clients(s);return;}

		}
	}

	//===============================================================================================
	public ChannelFuture write(Channel c, String s)
	{//===============================================================================================

		if(s.endsWith(BobNet.endline)==false)
		{
			log.error("Message doesn't end with endline");
			s = s +BobNet.endline;
		}

		if(BobNet.debugMode)
		{
			log.debug("SEND INDEX: cID:"+c.id()+" | "+s.substring(0,s.length()-2));
		}

		ChannelFuture cf = c.writeAndFlush(s);

		return cf;
	}

	//=========================================================================================================================
	//CONNECT TO SERVER
	//=========================================================================================================================

	private boolean _connectedToServer = false;//synchronized

	//=========================================================================================================================
	public void connectToServer(String host, int port)
	{//=========================================================================================================================

		if(getConnectedToServer_S()==true)return;

		//Initiate the first connection attempt
		channelFuture = clientBootstrap.connect(host, port);
	}

	//=========================================================================================================================
	synchronized public void setConnectedToServer_S(boolean b)
	{//=========================================================================================================================
		_connectedToServer = b;
	}
	//=========================================================================================================================
	synchronized public boolean getConnectedToServer_S()
	{//=========================================================================================================================
		return _connectedToServer;
	}

	//===============================================================================================
	public void send_INDEX_Register_Server_Request()
	{//===============================================================================================

		//if we already have a serverID, send it with the request.
		//the index server will go through its hashtable and look for that serverID, setting it to the new connection channel.

		write(channel,BobNet.INDEX_Register_Server_With_INDEX_Request+PrivateCredentials.passwordSalt+","+serverID+","+ServerMain.myIPAddressString+":"+BobNet.endline);

	}

	//===============================================================================================
	public void incoming_Server_Registered_With_INDEX_Response(String s)
	{//===============================================================================================

		//set the serverID

		//Server_Registered_With_INDEX_Response:message:serverID:
		s = s.substring(s.indexOf(":")+1);//message:serverID:
		String message = s.substring(0,s.indexOf(":"));
		log.info(message);
		s = s.substring(s.indexOf(":")+1);
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(":")));}catch(NumberFormatException ex){ex.printStackTrace();return;}

	}

	//===============================================================================================
	public void send_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(long userID)
	{//===============================================================================================

		if(BobNet.debugMode==false)
		{
			write(channel,BobNet.INDEX_UserID_Logged_On_This_Server_Log_Them_Off_Other_Servers+serverID+","+userID+BobNet.endline);
		}

	}

	//===============================================================================================
	public void incoming_Server_UserID_Logged_On_Other_Server_So_Log_Them_Off(String s)
	{//===============================================================================================

		//Server_UserID_Logged_On_Other_Server_So_Log_Them_Off:userID
		int userID = -1;
		s = s.substring(s.indexOf(":")+1);//userID
		userID = -1;
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(":")));}catch(NumberFormatException ex){ex.printStackTrace();return;}

		if(userID==-1)return;

		BobsGameClient c = ServerMain.gameServerTCP.clientsByUserID.get(userID);

		if(c!=null)
		{
			ServerMain.gameServerTCP.sendTellClientTheirSessionWasLoggedOnSomewhereElseAndCloseChannel(userID);
		}

	}

	//===============================================================================================
	public void incoming_Server_Tell_All_Users_Servers_Are_Shutting_Down(String s)
	{//===============================================================================================

		//Server_Tell_All_Users_Servers_Are_Shutting_Down

		// send "server is shutting down" to all clients
		//TODO: stop accepting logins

		Iterator<BobsGameClient> i = ServerMain.gameServerTCP.clientsByUserID.values().iterator();
		while(i.hasNext())
		{
			BobsGameClient check = i.next();
			if(check!=null)
			{
				if(check.channel.isActive())
				{
					ChannelFuture cf = write(check.channel,BobNet.Tell_Client_Servers_Are_Shutting_Down+BobNet.endline);
				}
			}
		}

	}

	//===============================================================================================
	public void incoming_Server_Tell_All_Users_Servers_Have_Shut_Down(String s)
	{//===============================================================================================

		//Server_Tell_All_Users_Servers_Have_Shut_Down

		// send "server has shut down" to all clients
		// shut down instance.

		Iterator<BobsGameClient> i = ServerMain.gameServerTCP.clientsByUserID.values().iterator();
		while(i.hasNext())
		{
			BobsGameClient check = i.next();
			if(check!=null)
			{
				if(check.channel.isActive())
				{

					ChannelFuture cf = write(check.channel,BobNet.Tell_Client_Servers_Have_Shut_Down+BobNet.endline);

					try
					{
						cf.sync();
					}
					catch(InterruptedException ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}

		//TODO: probably have to make sure each channel gets closed, database connections closed, no halfway database writes or anything

		try
		{
			Process p = Runtime.getRuntime().exec(new String[]{"reboot"});
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}

	}

	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(long userID, String userNameFriendsCSV)
	{//===============================================================================================

		//DONE: send this to INDEX SERVER
		write(channel,BobNet.INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online+userID+",`"+userNameFriendsCSV+"`"+BobNet.endline);
	}

	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(long userID, String facebookFriendsCSV)
	{//===============================================================================================

		//DONE: send this to INDEX SERVER
		write(channel,BobNet.INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online+userID+",`"+facebookFriendsCSV+"`"+BobNet.endline);
	}

	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	//when a user comes online, it gets a list of its facebook friends by facebookID.
	//then it sends that list to all the other servers.
	//once we get that list, we check through our hashmap to see if any of those facebook IDs are on this server.
	//we tell all those facebook friends that user came online
	//then we make a new list of online friends by userID and send it back to the original server, which sends it back to the original client.
	public void incoming_Server_Tell_All_FacebookIDs_That_UserID_Is_Online(ChannelHandlerContext ctx, String s)
	{//===============================================================================================

		int originatingServerID = -1;
		int originatingUserID = -1;

		//ServerNotifyFacebookFriendsUserIsOnline:serverID,userID,`facebookFriendsCSV,`
		s = s.substring(s.indexOf(":")+1);//serverID,userID,`facebookFriendsCSV,`
		try{originatingServerID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//userID,`facebookFriendsCSV,`
		try{originatingUserID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf("`")+1);//facebookFriendsCSV,`
		String facebookIDsCSV = s.substring(0,s.indexOf('`'));

		if(originatingUserID==-1)return;
		if(originatingServerID==-1)return;
		if(facebookIDsCSV.length()==0)return;

		String onlineFriendUserIDsCSV = "";
		String temp = ""+facebookIDsCSV;

		while(temp.length()>0)
		{
			String facebookID = temp.substring(0,temp.indexOf(","));
			temp = temp.substring(temp.indexOf(",")+1);

			if(facebookID.length()>0)
			{

				BobsGameClient friendClient = ServerMain.gameServerTCP.clientsByFacebookID.get(facebookID);
				if(friendClient!=null)
				{

					//store list of online friend userIDs to send back to the original server, to send back to the original client.
					String type="facebook";
					long friendUserID = friendClient.userID;
					onlineFriendUserIDsCSV = onlineFriendUserIDsCSV+type+":"+friendUserID+",";

					//notify friend that user is online, they start pinging the stun server.
					write(friendClient.channel,BobNet.Friend_Is_Online_Notification+type+":"+originatingUserID+BobNet.endline);
				}
				else
				{
					//facebookID not found on this server or doesnt have account
				}
			}
		}

		//now send the list of facebook friends USERIDs that were online on THIS server BACK to the originating server, which sends it BACK to the originating client.
		write(ctx.channel(),BobNet.INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online+originatingServerID+","+originatingUserID+",`"+onlineFriendUserIDsCSV+"`"+BobNet.endline);

		//their client should ping the STUN server while our client does the same, hopefully matching each others request and getting each others IPs
		//then they connect to each other simultaneously, punching a hole in firewall

		//for each friend:

		//tell our client through TCP to repeatedly ping us through STUN with udp on a unique port (STUN:userID,friendID)
		//we record our clients outgoing UDP port and IP

		//connect to all other servers, tell them:
		//our userID, target friend userID

		//if that server has friend userID, connect to them and tell them we are online, and to connect to STUN to get our IP
	}

	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	//this is sent back after we send a request to another server which looks up what facebook friends are connected to it, notifies them, and sends back the list to us
	//so we should send that list to our client that originated the request.
	public void incoming_Server_Tell_UserID_That_UserIDs_Are_Online(String s)
	{//===============================================================================================

		//ServerNotifyUserFriendsAreOnline:userID,`onlineFriendUserIDsCSV,`
		s = s.substring(s.indexOf(":")+1);//userID,`onlineFriendUserIDsCSV,`
		int userID = -1;
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf("`")+1);//onlineFriendUserIDsCSV,`
		String onlineFriendUserIDsCSV = s.substring(0,s.indexOf('`'));

		if(userID==-1)return;
		if(onlineFriendUserIDsCSV.length()==0)return;

		//now we have a list of friends userIDs that are online right now.
		//send our list to our client which should start pinging all of the IPs to make connections.
		BobsGameClient c = ServerMain.gameServerTCP.clientsByUserID.get(userID);

		if(c!=null)
		{
			write(c.channel,BobNet.Online_Friends_List_Response+onlineFriendUserIDsCSV+BobNet.endline);
		}
		else
		{
			//userID not on this server

		}
	}

	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	public void incoming_Server_Tell_All_UserNames_That_UserID_Is_Online(ChannelHandlerContext ctx, String s)
	{//===============================================================================================

		int originatingServerID = -1;
		int originatingUserID = -1;

		//Server_Tell_All_UserNames_That_UserID_Is_Online,userID,`userNamesCSV,`
		s = s.substring(s.indexOf(":")+1);//serverID,userID,`userNamesCSV,`
		try{originatingServerID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//userID,`userNamesCSV,`
		try{originatingUserID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf("`")+1);//userNamesCSV,`
		String userNamesCSV = s.substring(0,s.indexOf('`'));

		if(originatingUserID==-1)return;
		if(originatingServerID==-1)return;
		if(userNamesCSV.length()==0)return;

		String onlineFriendUserIDsCSV = "";
		String temp = ""+userNamesCSV;

		while(temp.length()>0)
		{
			String userName = temp.substring(0,temp.indexOf(","));
			temp = temp.substring(temp.indexOf(",")+1);

			if(userName.length()>0)
			{

				BobsGameClient friendClient = ServerMain.gameServerTCP.clientsByUserName.get(userName);
				if(friendClient!=null)
				{

					//store list of online friend userIDs to send back to the original server, to send back to the original client.
					String type="userName";
					long friendUserID = friendClient.userID;
					onlineFriendUserIDsCSV = onlineFriendUserIDsCSV+type+":"+friendUserID+",";

					//notify friend that user is online, they start pinging the stun server.
					write(friendClient.channel,BobNet.Friend_Is_Online_Notification+type+":"+originatingUserID+BobNet.endline);
				}
				else
				{
					//userName not found on this server or doesnt have account
				}
			}
		}

		//now send the list of facebook friends USERIDs that were online on THIS server BACK to the originating server, which sends it BACK to the originating client.
		write(ctx.channel(),BobNet.INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online+originatingServerID+","+originatingUserID+",`"+onlineFriendUserIDsCSV+"`"+BobNet.endline);

	}

	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(String s, long userID)
	{//===============================================================================================

		if(BobNet.debugMode==false)
		{
			write(channel,BobNet.INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update+serverID+","+userID+","+s+":"+BobNet.endline);
		}
	}

	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(String s, long userID)
	{//===============================================================================================
		
		if(BobNet.debugMode==false)
		{
			write(channel,BobNet.INDEX_Tell_All_Servers_Bobs_Game_Remove_Room+serverID+","+userID+","+s+":"+BobNet.endline);
		}
	}

	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	public void incoming_Server_Bobs_Game_Hosting_Room_Update(String s)
	{//===============================================================================================

		int originatingServerID = -1;
		int originatingUserID = -1;

		//Server_Bobs_Game_Hosting_Room_Update:serverID,userID,roomString:
		s = s.substring(s.indexOf(":")+1);//serverID,userID,
		try{originatingServerID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//userID,
		try{originatingUserID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//roomString:
		String roomString = s.substring(0,s.indexOf(':'));

		if(originatingUserID==-1)return;
		if(originatingServerID==-1)return;
		if(roomString.length()==0)return;

		BobsGameRoom newRoom = ServerMain.gameServerTCP.createRoom(roomString,originatingUserID);
		
		if(newRoom!=null)
		{
			ServerMain.gameServerTCP.tellAllClientsNewRoomHasBeenCreated(newRoom, -1);
		}
	}

	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	public void incoming_Server_Bobs_Game_Remove_Room(String s)
	{//===============================================================================================

		int originatingServerID = -1;
		int originatingUserID = -1;

		//Server_Bobs_Game_Remove_Room:serverID,userID,roomUUID:
		s = s.substring(s.indexOf(":")+1);//serverID,userID,

		try{originatingServerID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//userID,
		try{originatingUserID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);//roomUUID:
		String roomUUID = s.substring(0,s.indexOf(':'));

		if(originatingUserID==-1)return;
		if(originatingServerID==-1)return;
		if(roomUUID.length()==0)return;

		ServerMain.gameServerTCP.removeRoom(roomUUID,originatingUserID);
	}

	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients(String activityString)
	{//===============================================================================================
		
		if(BobNet.debugMode==false)
		{
			write(channel,BobNet.INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients+serverID+","+activityString+":END:"+BobNet.endline);
		}
	}
	
	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	public void incoming_Server_Send_Activity_Update_To_All_Clients(String s)
	{//===============================================================================================
		
		//Server_Send_Activity_Update_To_All_Clients:activityString:
		s = s.substring(s.indexOf(":")+1);
		
		String activityString = s.substring(0,s.indexOf(":END:"));
		
		if(activityString.length()==0)return;
		
		ServerMain.gameServerTCP.sendActivityUpdateToAllClients(activityString);
	}
	
	//===============================================================================================
	public void send_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients(String s)
	{//===============================================================================================

		if(BobNet.debugMode==false)
		{
			write(channel,BobNet.INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients+serverID+","+s+":END:"+BobNet.endline);
		}
	}
	
	//===============================================================================================
	//this event should only come from the index server
	//--------------------------------------------------
	public void incoming_Server_Send_Chat_Message_To_All_Clients(String s)
	{//===============================================================================================

		//Server_Send_Chat_Message_To_All_Clients:activityString:
		s = s.substring(s.indexOf(":")+1);

		String chatMessage = s.substring(0,s.indexOf(":END:"));

		if(chatMessage.length()==0)return;

		ServerMain.gameServerTCP.sendChatMessageToAllClients(chatMessage);
	}
	
}
