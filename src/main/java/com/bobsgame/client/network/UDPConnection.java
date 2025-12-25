package com.bobsgame.client.network;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.bobsgame.client.console.Console;
import com.bobsgame.client.engine.Engine;
import com.bobsgame.client.engine.EnginePart;
import com.bobsgame.client.engine.game.FriendCharacter;
import com.bobsgame.net.BobNet;
import com.bobsgame.shared.BobColor;




//===============================================================================================
public class UDPConnection extends EnginePart
{//===============================================================================================

	public static Logger log = (Logger) LoggerFactory.getLogger(UDPConnection.class);





	String peerIP_S = "";//synchronized
	int peerUDPPort_S = -1;//synchronized
	int myUDPPort = -1;


	ConnectionlessBootstrap connectionlessBootstrap;
	DatagramChannelFactory channelFactory;
	private Channel channel;


	private int port;


	long lastReceivedDataTime = System.currentTimeMillis();
	long lastSentPingTime = System.currentTimeMillis();

	//===============================================================================================
	public UDPConnection(Engine g, int myPort)
	{//===============================================================================================

		super(g);

		this.myUDPPort = myPort;

		channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
		connectionlessBootstrap = new ConnectionlessBootstrap(channelFactory);

		final ChannelPipelineFactory perDatagramFactory = new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{

				//Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();


				//Add the text line codec combination first,
				pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));//8192
				pipeline.addLast("decoder", new StringDecoder());
				pipeline.addLast("encoder", new StringEncoder());


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


		//connectionlessBootstrap.setOption("sendBufferSize", 65536);
		//connectionlessBootstrap.setOption("receiveBufferSize", 65536);
		connectionlessBootstrap.setOption("receiveBufferSizePredictorFactory", new AdaptiveReceiveBufferSizePredictorFactory(16000,16000,65536));

		//udpConnectionlessBootstrap.setOption("broadcast", "true");
		connectionlessBootstrap.setOption("broadcast", "false");

		connectionlessBootstrap.setOption("reuseAddress", "true");


		this.port = myUDPPort;

		//connectionlessBootstrap.setOption("localAddress", new InetSocketAddress(port));
		//connectionlessBootstrap.setOption("tcpNoDelay", true);

		channel = connectionlessBootstrap.bind(new InetSocketAddress(port));

		log.info("UDP Channel: "+channel.getId().toString());
	}




	//===============================================================================================
	public class UDPHandler extends SimpleChannelUpstreamHandler
	{//===============================================================================================

		//===============================================================================================
		public UDPHandler()
		{//===============================================================================================
			super();
		}

		//===============================================================================================
		@Override
		public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
		{//===============================================================================================
			log.debug("UDP channelConnected: "+e.getChannel().getId()+" IP: "+e.getChannel().getRemoteAddress().toString());
		}
		//===============================================================================================
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelDisconnected: "+e.getChannel().getId()+" IP: "+e.getChannel().getRemoteAddress().toString());
		}
		//===============================================================================================
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelClosed: "+e.getChannel().getId());
		}
		//===============================================================================================
		@Override
		public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
		{//===============================================================================================
			log.debug("UDP channelUnbound: "+e.getChannel().getId());
		}

		//===============================================================================================
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
		{//===============================================================================================

			try{Thread.currentThread().setName("UDPConnection_UDPHandler");}catch(SecurityException ex){ex.printStackTrace();}

			String s = (String) e.getMessage();


			if(BobNet.debugMode)
			{

				if(s.startsWith("Friend_Location_Update")==false)
				log.warn("FROM CLIENT: cID:"+channel.getId()+" | "+s);
			}

			lastReceivedDataTime = System.currentTimeMillis();

			if(s.startsWith("ping"))
			{
				InetSocketAddress peerAddress = getPeerSocketAddress_S();
				if(peerAddress!=null)
				{
					write("pong"+BobNet.endline);
				}
				else
				{
					log.warn("peerAddress was null, but got ping.");
				}
				return;
			}

			if(s.startsWith("pong")){}

			handleMessage(ctx,e);
		}

	}

	//===============================================================================================
	public void cleanup()
	{//===============================================================================================

		if(channel!=null)channel.close().awaitUninterruptibly();
		connectionlessBootstrap.releaseExternalResources();
	}

	//===============================================================================================
	public ChannelFuture write(String s)
	{//===============================================================================================

		if(s.endsWith(BobNet.endline)==false)
		{
			log.error("Packet doesn't end with endline");
			s = s +BobNet.endline;
		}

		if(BobNet.debugMode)
		{
			if(s.startsWith("Friend_Location_Update")==false&&s.startsWith("Friend_Connect_Request")==false)
			log.debug("SEND CLIENT: cID:"+channel.getId()+" | "+s.substring(0,s.length()-2));
		}

		InetSocketAddress peerAddress = getPeerSocketAddress_S();
		if(peerAddress!=null)
		{
			return write(s,getPeerSocketAddress_S());
		}
		else log.warn("peerAddress was null.");

		return null;

	}
	//===============================================================================================
	public synchronized ChannelFuture write(String s,InetSocketAddress address)
	{//===============================================================================================
		ChannelFuture c = channel.write(s,getPeerSocketAddress_S());
//		try
//		{
//			c.sync();
//		}
//		catch(InterruptedException e)
//		{
//			e.printStackTrace();
//		}
		return c;
	}





	private InetSocketAddress _peerAddress = null;

	//===============================================================================================
	protected synchronized InetSocketAddress getPeerSocketAddress_S()
	{//===============================================================================================
		return _peerAddress;
	}
	//===============================================================================================
	public synchronized void setPeerSocketAddress_S(String ipAddress, int port)
	{//===============================================================================================

		if(ipAddress==null)_peerAddress=null;
		else
		_peerAddress = new InetSocketAddress(ipAddress,port);
	}



	boolean established_NonThreaded = false;
	//===============================================================================================
	public boolean established()
	{//===============================================================================================
		return established_NonThreaded;
	}
	//===============================================================================================
	public void setEstablished(boolean b)
	{//===============================================================================================
		established_NonThreaded = b;
	}





	boolean gotPeerConnectResponse_S = false;
	//===============================================================================================
	synchronized boolean gotConnectResponse_S()
	{//===============================================================================================
		return gotPeerConnectResponse_S;
	}
	//===============================================================================================
	protected synchronized void setGotPeerConnectResponse_S(boolean b)
	{//===============================================================================================
		gotPeerConnectResponse_S = b;
	}




	long lastConnectAttemptTime = System.currentTimeMillis();
	//===============================================================================================
	public void update()
	{//===============================================================================================

		//see if we have a udp connection to them established
		if(established()==false)
		{
			long currentTime = System.currentTimeMillis();

			//if we don't, keep pinging the stun server with our request. they should be doing the same thing.
			if(currentTime-lastConnectAttemptTime>500)
			{
				lastConnectAttemptTime=currentTime;

				if(getPeerSocketAddress_S()==null)
				{
					sendAddressRequest();
				}
				else
				{

					if(gotConnectResponse_S()==false)
					{
						sendPeerConnectRequest();
					}
					else
					{
						setEstablished(true);
					}
				}
			}
		}

		if(established()==true)
		{
			long currentTime = System.currentTimeMillis();

			//-----------------------------
			//connection is established!
			//-----------------------------

			//send keepalive
			//keep last got friend keepalive ping/pong
			if(currentTime-lastReceivedDataTime>10000)//10 seconds
			{
				//send ping
				if(currentTime-lastSentPingTime>10000)
				{
					lastSentPingTime = currentTime;
					write("ping"+BobNet.endline);
				}
			}

			if(currentTime-lastReceivedDataTime>60000)//60 seconds
			{
				//assume peer has gone offline
				//close if no keepalive
				established_NonThreaded = false;
				setPeerSocketAddress_S(null,-1);

				handleDisconnected();
			}
		}
	}



	//===============================================================================================
	public void handleMessage(ChannelHandlerContext ctx, MessageEvent e)
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void incomingPeerConnectResponse(MessageEvent e)
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void sendAddressRequest()
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void sendPeerConnectRequest()
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void sendPeerConnectResponse()
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void handleDisconnected()
	{//===============================================================================================

		//override this

		log.error("Disconnected from "+getPeerSocketAddress_S());
	}

}
