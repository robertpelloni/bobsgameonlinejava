package com.bobsgame.stunserver;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Vector;
import java.util.concurrent.Executors;

import com.bobsgame.STUNServerMain;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import com.bobsgame.net.BobNet;


//===============================================================================================
public class STUNServerUDP
{//===============================================================================================


	//TODO: are we sure we want 16 threads??? optimise this.
	static public ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));


	ConnectionlessBootstrap connectionlessBootstrap;
	DatagramChannelFactory channelFactory;
	Channel channel;

	public static Logger log = (Logger)LoggerFactory.getLogger(STUNServerUDP.class);


	//===============================================================================================
	public STUNServerUDP()
	{//===============================================================================================


		channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
		connectionlessBootstrap = new ConnectionlessBootstrap(channelFactory);

		final ChannelPipelineFactory perDatagramFactory = new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{

				//Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();



				//Add the text line codec combination first,
				pipeline.addLast("framer", new DelimiterBasedFrameDecoder(768, Delimiters.lineDelimiter()));//8192
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("encoder", new StringEncoder());

				//this is needed because of DB access stuff, otherwise threads will stall and drop packets
				pipeline.addLast("execution-handler", executionHandler);//this helped when buffer got full, 49/100 -> 65/100


				//and then business logic.
				pipeline.addLast("handler", new UDPHandler());

				return pipeline;

				// Add your handlers here
				//return Channels.pipeline();
			}

		};


		connectionlessBootstrap.setPipelineFactory(perDatagramFactory);



		//if i want a new pipeline for each datagram
//		new ChannelPipelineFactory()
//		{
//			public ChannelPipeline getPipeline() throws Exception
//			{
//				return Channels.pipeline(new DistinctChannelPipelineHandler(perDatagramFactory));
//			}
//		});

		int min = 64;
		int init = 1024;
		int max = 524288;//these don't matter so much?

		connectionlessBootstrap.setOption("sendBufferSize", 524288);//if these are small it drops packets when sending 10000+
		connectionlessBootstrap.setOption("receiveBufferSize", 524288);
		connectionlessBootstrap.setOption("receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory(min,init,max));//this doesnt even seem to be needed?

		//udpConnectionlessBootstrap.setOption("broadcast", "true");
		connectionlessBootstrap.setOption("broadcast", "false");

		//connectionlessBootstrap.setOption("reuseAddress", "true");

		int serverPort = BobNet.STUNServerUDPPort;
		//if(new File("/localServer").exists())serverPort++;

		connectionlessBootstrap.setOption("localAddress", new InetSocketAddress(serverPort));
		//connectionlessBootstrap.setOption("tcpNoDelay", true);

		channel = connectionlessBootstrap.bind(new InetSocketAddress(serverPort));

		log.info("udp Channel: "+channel.getId().toString());



	}




	//===============================================================================================
	public void cleanup()
	{//===============================================================================================
		connectionlessBootstrap.releaseExternalResources();
	}

//	//===============================================================================================
//	private static final class DistinctChannelPipelineHandler implements ChannelDownstreamHandler, ChannelUpstreamHandler
//	{//===============================================================================================
//		private ChannelPipelineFactory factory;
//
//		public DistinctChannelPipelineHandler(ChannelPipelineFactory factory)
//		{
//			this.factory = factory;
//		}
//
//		public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
//		{
//			ChannelPipeline pipeline = factory.getPipeline();
//			pipeline.attach(ctx.getChannel(), ctx.getPipeline().getSink());
//			pipeline.sendUpstream(e);
//
//			ctx.sendUpstream(e);
//
//		}
//
//		public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception
//		{
//			ChannelPipeline pipeline = factory.getPipeline();
//			pipeline.attach(ctx.getChannel(), ctx.getPipeline().getSink());
//			pipeline.sendDownstream(e);
//
//			ctx.sendDownstream(e);
//		}
//
//	}

	//===============================================================================================
	public class UDPHandler extends SimpleChannelHandler
	{//===============================================================================================

		//===============================================================================================
		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{//===============================================================================================
			log.debug("UDP channelConnected:"+e.getChannel().getId());
		}
		//===============================================================================================
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelDisconnected:"+e.getChannel().getId());
		}
		//===============================================================================================
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelClosed:"+e.getChannel().getId());
		}
		//===============================================================================================
		@Override
		public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelUnbound:"+e.getChannel().getId());
		}

		//===============================================================================================
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		{//===============================================================================================

			String s = (String) e.getMessage();

			//if(BobNet.debugMode)
			{
				log.debug("UDP messageReceived:"+s);
			}

			STUNServerMain.totalConnections++;

			if(s.startsWith(BobNet.STUN_Request)){incomingSTUNRequest(e);return;}


		}
	}




	//===============================================================================================
	public class STUNRequest
	{//===============================================================================================
		public long userID1 = -1;
		public long userID2 = -1;
		public long lastHeardFromTime = 0;
		public int user1Port = -1;
		public int user2Port = -1;

		public SocketAddress userIP1 = null;
		public SocketAddress userIP2 = null;


		public STUNRequest(long userID1, long userID2, SocketAddress userIP1, int user1Port)
		{
			lastHeardFromTime = System.currentTimeMillis();
			this.userID1 = userID1;
			this.userID2 = userID2;
			this.userIP1 = userIP1;
			this.user1Port = user1Port;

		}

	}



	Vector<STUNRequest> STUNRequestList = new Vector<STUNRequest>();


	//===============================================================================================
	public synchronized int getSTUNRequestListSize()
	{//===============================================================================================
		return STUNRequestList.size();
	}

	//===============================================================================================
	synchronized void addToSTUNRequestList(STUNRequest s)
	{//===============================================================================================
		STUNRequestList.add(s);
	}

	//===============================================================================================
	synchronized void removeFromSTUNRequestList(STUNRequest s)
	{//===============================================================================================
		STUNRequestList.remove(s);
	}

	//===============================================================================================
	synchronized void removeFromSTUNRequestList(int i)
	{//===============================================================================================
		STUNRequestList.remove(i);
	}

	//===============================================================================================
	synchronized STUNRequest getFromSTUNRequestList(int i)
	{//===============================================================================================
		return STUNRequestList.get(i);
	}

	//===============================================================================================
	public void update()
	{//===============================================================================================
		for(int i=0;i<getSTUNRequestListSize();i++)
		{

			STUNRequest r = getFromSTUNRequestList(i);

			if(System.currentTimeMillis()-r.lastHeardFromTime>10000)//10 seconds
			{

				if(r.userIP1!=null&&r.userIP2!=null)STUNServerMain.totalSTUNsCompleted++;
				else STUNServerMain.totalSTUNsTimedOut++;

				removeFromSTUNRequestList(i);
				i--;
			}
		}


	}

	//===============================================================================================
	public STUNRequest getSTUNRequestFor(long userID, long friendID)
	{//===============================================================================================

		for(int i=0;i<getSTUNRequestListSize();i++)
		{
			STUNRequest s = getFromSTUNRequestList(i);

			if(
				(s.userID1==userID && s.userID2==friendID)
				||
				(s.userID1==friendID && s.userID2==userID)
			)return s;
		}

		return null;
	}

	//===============================================================================================
	private void incomingSTUNRequest(MessageEvent e)
	{//===============================================================================================

		String s = (String) e.getMessage();

		//STUNRequest:userID,friendID
		s = s.substring(s.indexOf(":")+1);
		long userID = -1;
		try{userID = Long.parseLong(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();return;}

		s = s.substring(s.indexOf(",")+1);
		long friendID = -1;
		try{friendID = Long.parseLong(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();return;}


		s = s.substring(s.indexOf(",")+1);
		int port = -1;
		try{port = Integer.parseInt(s.substring(0,s.indexOf(",")));}catch(NumberFormatException ex){ex.printStackTrace();return;}

		//look up userID in RequestList

		STUNRequest r = getSTUNRequestFor(userID, friendID);

		if(r==null)
		{
			log.debug("Created request, waiting: userID: "+userID+" friendID:"+friendID);
			addToSTUNRequestList(new STUNRequest(userID,friendID,e.getRemoteAddress(),port));
		}
		else
		{
			r.lastHeardFromTime = System.currentTimeMillis();

			if(r.userID2==userID)//i didn't create the request first, my friend did.
			{
				log.debug("Found pair: userID2: "+r.userID2+" userID1:"+r.userID1);
				//we have a completed pair
				r.userIP2 = e.getRemoteAddress();
				r.user2Port = port;

				channel.write(BobNet.STUN_Response+r.userID2+","+r.userIP2.toString()+","+r.user2Port+","+BobNet.endline,r.userIP1);
				channel.write(BobNet.STUN_Response+r.userID1+","+r.userIP1.toString()+","+r.user1Port+","+BobNet.endline,r.userIP2);

				//removeFromSTUNRequestList(r);
			}
			else //i created the request, but i didn't get the ping for some reason when they completed the request.
			{
				if(r.userIP1!=null&&r.userIP2!=null)
				{
					log.debug("Found late pair: userID1: "+r.userID1+" userID2:"+r.userID2);

					channel.write(BobNet.STUN_Response+r.userID2+","+r.userIP2.toString()+","+r.user2Port+","+BobNet.endline,r.userIP1);
					channel.write(BobNet.STUN_Response+r.userID1+","+r.userIP1.toString()+","+r.user1Port+","+BobNet.endline,r.userIP2);
				}
				else
				{
					//still waiting
				}
			}
		}
	}






}
