package com.bobsgame.serverindex;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;



import java.util.*;


import ch.qos.logback.classic.Logger;

import com.bobsgame.net.*;

import java.net.ConnectException;
import java.net.InetSocketAddress;


import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;


import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import org.slf4j.LoggerFactory;




//===============================================================================================
public class IndexServerTCP
{//===============================================================================================


	//TODO: are we sure we want 16 threads??? optimise this.
	static public ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));



	Timer timer;


	//DONE: add any other server channels to this vector
	public Vector<BobsGameServer> serverList = new Vector<BobsGameServer>();
	public ConcurrentHashMap<Channel,BobsGameServer> serversByChannel = new ConcurrentHashMap<Channel,BobsGameServer>();
	public ConcurrentHashMap<Integer,BobsGameServer> serversByServerID = new ConcurrentHashMap<Integer,BobsGameServer>();


	ServerBootstrap tcpServerBootstrap;
	NioServerSocketChannelFactory tcpChannelFactory;
	Channel tcpChannel;


	public static Logger log = (Logger) LoggerFactory.getLogger(IndexServerTCP.class);


	//===============================================================================================
	public IndexServerTCP()
	{//===============================================================================================

		timer = new HashedWheelTimer();




		// Configure the channel factory.
		tcpChannelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
		tcpServerBootstrap = new ServerBootstrap(tcpChannelFactory);


		// Configure the pipeline factory.
		tcpServerBootstrap.setPipelineFactory(new TimeOutChannelPipelineFactory(timer));

		tcpServerBootstrap.setOption("child.tcpNoDelay", true);
		tcpServerBootstrap.setOption("child.keepAlive", true);

		tcpServerBootstrap.setOption("tcpNoDelay", true);
		tcpServerBootstrap.setOption("keepAlive", true);

		//tcpServerBootstrap.setOption("reuseAddress", "true");

		tcpServerBootstrap.setOption("sendBufferSize", 524288);
		tcpServerBootstrap.setOption("receiveBufferSize", 524288);
		tcpServerBootstrap.setOption("receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory());

		//tcpServerBootstrap.setOption("broadcast", "true");


		int serverPort = BobNet.INDEXServerTCPPort;
		//if(new File("/localServer").exists())serverPort++;

		// Bind and start to accept incoming connections.
		tcpChannel = tcpServerBootstrap.bind(new InetSocketAddress(serverPort));

		log.info("INDEX Server TCP ChannelID: "+tcpChannel.getId().toString());


	}


	//===============================================================================================
	public void cleanup()
	{//===============================================================================================
		tcpServerBootstrap.releaseExternalResources();
		timer.stop();
	}


	//===============================================================================================
	public class TimeOutChannelPipelineFactory implements ChannelPipelineFactory
	{//===============================================================================================


		private final ChannelHandler idleStateHandler;

		public TimeOutChannelPipelineFactory(Timer timer)
		{
			this.idleStateHandler = new IdleStateHandler(timer, 120, 30, 0); // timer must be shared.
		}

		public ChannelPipeline getPipeline() throws Exception
		{
			//Create a default pipeline implementation.
			ChannelPipeline pipeline = pipeline(idleStateHandler);


			//Add the text line codec combination first,
			pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));//8192
			pipeline.addLast("decoder", new StringDecoder());
			pipeline.addLast("encoder", new StringEncoder());

			//this should help not stall threads when doing db access
			pipeline.addLast("execution-handler", executionHandler);

			//and then business logic.
			pipeline.addLast("handler", new BobsGameServerHandler());

			return pipeline;
		}

	}






	//===============================================================================================
	public class BobsGameServerHandler extends IdleStateAwareChannelHandler
	{//===============================================================================================

		Logger log = (Logger)LoggerFactory.getLogger(this.getClass());

		//===============================================================================================
		public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
		{//===============================================================================================

			int id = -1;
			BobsGameServer s = getServerByChannel(e.getChannel());
			if(s!=null)id=s.serverID;

			if(e.getState() == IdleState.READER_IDLE)
			{
				log.warn("channelIdle: No incoming traffic from server timeout. Closing channel. ServerID: "+id);

				e.getChannel().close();

			}
			else if(e.getState() == IdleState.WRITER_IDLE)
			{

				e.getChannel().write("ping"+BobNet.endline);

				if(BobNet.debugMode)log.debug("channelIdle: ping ServerID: "+id);
			}
		}



		//===============================================================================================
		@Override
		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
		{//===============================================================================================
			if (e instanceof ChannelStateEvent)
			{
				log.debug("handleUpstream: "+e.toString());
			}
			super.handleUpstream(ctx, e);
		}


		//===============================================================================================
		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{//===============================================================================================
			log.info("channelConnected: from Server. ChannelID: "+e.getChannel().getId());

		}




		//===============================================================================================
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================


			log.warn("channelDisconnected: from Server. ChannelID: "+e.getChannel().getId());



			BobsGameServer s = getServerByChannel(e.getChannel());


			if(s!=null)
			{
				serverList.remove(s);
				serversByChannel.remove(e.getChannel());
				serversByServerID.remove(s.serverID);
			}


		}


		//===============================================================================================
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		{//===============================================================================================


			String message = (String) e.getMessage();



			int serverID = -1;
			BobsGameServer s = getServerByChannel(e.getChannel());
			if(s!=null)serverID=s.serverID;






			if(message.startsWith("pong"))
			{
				//log.debug("pong from ServerID: "+id);
				return;

			}
			else
			if(message.startsWith("ping"))
			{

			}

			if(BobNet.debugMode)
			{
				log.warn("FROM SERVER: cID:"+e.getChannel().getId()+" sID:"+serverID+" | "+message);
			}

			if(message.startsWith(BobNet.INDEX_Register_Server_With_INDEX_Request)){incoming_INDEX_Register_Server_Request(e);return;}


			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online)){incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(e);return;}
			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online)){incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(e);return;}
			if(message.startsWith(BobNet.INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online)){incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online(e);return;}
			if(message.startsWith(BobNet.INDEX_UserID_Logged_On_This_Server_Log_Them_Off_Other_Servers)){incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(e);return;}

			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update)){incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(e);return;}
			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_Bobs_Game_Remove_Room)){incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(e);return;}
			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients)){incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients(e);return;}
			if(message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients)){incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients(e);return;}


		}

		//===============================================================================================
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
		{//===============================================================================================
			Throwable cause = e.getCause();
			if(cause instanceof ConnectException)
			{
				log.error("Exception caught from Server connection - ConnectException: "+e.getCause().getMessage());
			}
			else
			if(cause instanceof ReadTimeoutException)
			{
				log.error("Exception caught from Server connection - ReadTimeoutException: "+e.getCause().getMessage());
			}
			else
			{
				log.error("Unexpected Exception caught from Server connection: "+e.getCause().getMessage());
				cause.printStackTrace();
			}

			ctx.getChannel().close();
			e.getChannel().close();
		}



	}




	//===============================================================================================
	public BobsGameServer getServerByChannel(Channel c)
	{//===============================================================================================
		return serversByChannel.get(c);
	}

	//===============================================================================================
	public BobsGameServer getServerByServerID(int i)
	{//===============================================================================================
		return serversByServerID.get(i);
	}


	//===============================================================================================
	public void incoming_INDEX_Register_Server_Request(MessageEvent e)
	{//===============================================================================================

		//INDEX_Register_Server_Request:passCode,serverID,ipAddress
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//passCode,serverID,ipAddress
		String passCode = s.substring(0, s.indexOf(","));
		s = s.substring(s.indexOf(",")+1);
		int serverID = -1;
		try{serverID = Integer.parseInt(s.substring(0, s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf(",")+1);
		String ipAddressString = s.substring(0, s.indexOf(":"));

		if(passCode.equals(PrivateCredentials.passwordSalt)==false)
		{
			e.getChannel().write(BobNet.Server_Register_Server_With_INDEX_Response+"Incorrect passcode, cannot register with index.:-1:"+BobNet.endline);
			e.getChannel().close();
		}
		

		BobsGameServer server = null;
		if(serverID!=-1)
		{
			//look through our hashtables to find that serverID, set the channel to the new one.
			server = serversByServerID.get(serverID);

			if(server!=null)
			{
				serverList.remove(server);
				serversByChannel.remove(server.channel);
				serversByServerID.remove(serverID);

				serverID = server.serverID;
			}
		}



		if(server==null)
		{
			//make a new serverID, add to hashtables.
			server = new BobsGameServer(e.getChannel(), ipAddressString);
		}

		serverID = server.serverID;

		serverList.add(server);
		serversByChannel.put(e.getChannel(),server);
		serversByServerID.put(server.serverID,server);


		e.getChannel().write(BobNet.Server_Register_Server_With_INDEX_Response+"Successfully registered with index.:"+serverID+":"+BobNet.endline);

	}




	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(MessageEvent e)
	{//===============================================================================================


		BobsGameServer thisServer = getServerByChannel(e.getChannel());

		//ServerNotifyFacebookFriendsUserIsOnline:userID,`facebookFriendsCSV,`
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//userID,`facebookFriendsCSV,`
		int userID = -1;
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf("`")+1);//facebookFriendsCSV,`
		String facebookIDsCSV = s.substring(0,s.indexOf('`'));

		if(userID==-1)return;
		if(facebookIDsCSV.length()==0)return;



		for(int i=0;i<serverList.size();i++)
		{
			Channel c = serverList.get(i).channel;

			if(c.isConnected())
			{
				c.write(BobNet.Server_Tell_All_FacebookIDs_That_UserID_Is_Online+thisServer.serverID+","+userID+",`"+facebookIDsCSV+"`"+BobNet.endline);
			}
			else
			{
				log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online. Did server drop connection?");
			}
		}



		//TODO: now each of those servers will look up each facebook friend ID in their hashtable, connect to each client and tell them our userID is online.
		//each server should make a LIST of the clients userID it has in its facebook ID hashtable, and send that list of userIDs back to the index server, which sends it back to the originating server, which sends it to the originating client.

	}

	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(MessageEvent e)
	{//===============================================================================================


		BobsGameServer thisServer = getServerByChannel(e.getChannel());

		//ServerNotifyUserNameFriendsUserIsOnline:userID,`facebookFriendsCSV,`
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//userID,`facebookFriendsCSV,`
		int userID = -1;
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
		s = s.substring(s.indexOf("`")+1);//facebookFriendsCSV,`
		String userNamesCSV = s.substring(0,s.indexOf('`'));

		if(userID==-1)return;
		if(userNamesCSV.length()==0)return;



		for(int i=0;i<serverList.size();i++)
		{
			Channel c = serverList.get(i).channel;

			if(c.isConnected())
			{
				c.write(BobNet.Server_Tell_All_UserNames_That_UserID_Is_Online+thisServer.serverID+","+userID+",`"+userNamesCSV+"`"+BobNet.endline);
			}
			else
			{
				log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online. Did server drop connection?");
			}
		}


	}
	//===============================================================================================
	public void incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online(MessageEvent e)
	{//===============================================================================================

		//after a user logs in, we sent a list of all their facebook friends to notify to every server.

		//each server will go through its online users and notify any of them that are in that list.

		//it then compiles a list of the users on that server, and sends it back to us, so we can send it back to the original user, so it knows which friends are online so it can try to connect to them.

		int serverID = -1;
		int userID = -1;

		//INDEXTellServerNotifyUserFriendsAreOnline:serverID,userID,`onlineUserIDCSV,`
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID,userID,`onlineUserIDCSV,`
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);//userID,`onlineUserIDCSV,`
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf("`")+1);//onlineUserIDCSV,`
		String onlineUserIDCSV = s.substring(0,s.indexOf('`'));

		if(serverID==-1)return;
		if(userID==-1)return;
		if(onlineUserIDCSV.length()==0)return;



		BobsGameServer originalUserServer = getServerByServerID(serverID);
		if(originalUserServer!=null)
		{
			originalUserServer.channel.write(BobNet.Server_Tell_UserID_That_UserIDs_Are_Online+userID+",`"+onlineUserIDCSV+"`"+BobNet.endline);
		}
		else
		{
			log.warn("serverID could not be found during incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online. Did server drop connection?");
		}
	}



	//===============================================================================================
	public void incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(MessageEvent e)
	{//===============================================================================================

		//INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers:serverID,userID
		int serverID = -1;
		int userID = -1;
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID,userID
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);//userID
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(":")));}catch(NumberFormatException ex){ex.printStackTrace();}

		if(serverID==-1)return;
		if(userID==-1)return;



		for(int i=0;i<serverList.size();i++)
		{

			if(serverList.get(i).serverID!=serverID)
			{

				Channel c = serverList.get(i).channel;

				if(c.isConnected())
				{
					c.write(BobNet.Server_UserID_Logged_On_Other_Server_So_Log_Them_Off+userID+BobNet.endline);
				}
				else
				{
					log.warn("channel is not connected in incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers. Did server drop connection?");
				}
			}
		}


	}




	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(MessageEvent e)
	{//===============================================================================================

		//INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update,serverID,userID,roomString:
		int serverID = -1;
		int userID = -1;
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID,userID
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);//userID
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);
		String roomString = s.substring(0,s.indexOf(":"));

		if(serverID==-1)return;
		if(userID==-1)return;



		for(int i=0;i<serverList.size();i++)
		{

			if(serverList.get(i).serverID!=serverID)
			{

				Channel c = serverList.get(i).channel;

				if(c.isConnected())
				{
					c.write(BobNet.Server_Bobs_Game_Hosting_Room_Update+serverID+","+userID+","+roomString+":"+BobNet.endline);
				}
				else
				{
					log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update. Did server drop connection?");
				}
			}
		}

	}



	
	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(MessageEvent e)
	{//===============================================================================================
		
		//INDEX_Tell_All_Servers_Bobs_Game_Remove_Room,serverID,userID,roomString:
		int serverID = -1;
		int userID = -1;
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID,userID
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);//userID
		try{userID = Integer.parseInt(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);
		String roomString = s.substring(0,s.indexOf(":"));
		
		if(serverID==-1)return;
		if(userID==-1)return;
		
		
		
		for(int i=0;i<serverList.size();i++)
		{
			
			if(serverList.get(i).serverID!=serverID)
			{
				
				Channel c = serverList.get(i).channel;
				
				if(c.isConnected())
				{
					c.write(BobNet.Server_Bobs_Game_Remove_Room+serverID+","+userID+","+roomString+":"+BobNet.endline);
				}
				else
				{
					log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room. Did server drop connection?");
				}
			}
		}
		
		
	}

	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients(MessageEvent e)
	{//===============================================================================================
		
		//INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients,serverID,activityString:END:
		int serverID = -1;
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);
		
		String activityString = s.substring(0,s.indexOf(":END:"));
		
		if(serverID==-1)return;
		
		
		for(int i=0;i<serverList.size();i++)
		{
			
			if(serverList.get(i).serverID!=serverID)
			{
				
				Channel c = serverList.get(i).channel;
				
				if(c.isConnected())
				{
					c.write(BobNet.Server_Send_Activity_Update_To_All_Clients+activityString+":END:"+BobNet.endline);
				}
				else
				{
					log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients. Did server drop connection?");
				}
			}
		}
	}
	//===============================================================================================
	public void incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients(MessageEvent e)
	{//===============================================================================================

		//INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients,serverID,activityString:END:
		int serverID = -1;
		String s = (String) e.getMessage();
		s = s.substring(s.indexOf(":")+1);//serverID
		try{serverID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();}
		s = s.substring(s.indexOf(",")+1);
		
		String activityString = s.substring(0,s.indexOf(":END:"));

		if(serverID==-1)return;


		for(int i=0;i<serverList.size();i++)
		{

			if(serverList.get(i).serverID!=serverID)
			{

				Channel c = serverList.get(i).channel;

				if(c.isConnected())
				{
					c.write(BobNet.Server_Send_Chat_Message_To_All_Clients+activityString+":END:"+BobNet.endline);
				}
				else
				{
					log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients. Did server drop connection?");
				}
			}
		}
	}


	//===============================================================================================
	public void send_Tell_All_Servers_To_Tell_All_Clients_Servers_Are_Shutting_Down()
	{//===============================================================================================
		for(int i=0;i<serverList.size();i++)
		{
			Channel c = serverList.get(i).channel;

			if(c.isConnected())
			{
				c.write(BobNet.Server_Tell_All_Users_Servers_Are_Shutting_Down+BobNet.endline);
			}
			else
			{
				log.warn("channel is not connected in send_Tell_All_Servers_To_Tell_All_Clients_Servers_Are_Shutting_Down. Did server drop connection?");
			}
		}
	}



	//===============================================================================================
	public void send_Tell_All_Servers_To_Tell_All_Clients_Servers_Have_Shut_Down()
	{//===============================================================================================
		for(int i=0;i<serverList.size();i++)
		{
			Channel c = serverList.get(i).channel;

			if(c.isConnected())
			{
				c.write(BobNet.Server_Tell_All_Users_Servers_Have_Shut_Down+BobNet.endline);
			}
			else
			{
				log.warn("channel is not connected in send_Tell_All_Servers_To_Tell_All_Clients_Servers_Have_Shut_Down. Did server drop connection?");
			}
		}
	}







}
