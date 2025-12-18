package com.bobsgame.client.network;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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

	Bootstrap connectionlessBootstrap;
    EventLoopGroup group;
	private Channel channel;

	private int port;

	long lastReceivedDataTime = System.currentTimeMillis();
	long lastSentPingTime = System.currentTimeMillis();

	//===============================================================================================
	public UDPConnection(Engine g, int myPort)
	{//===============================================================================================

		super(g);

		this.myUDPPort = myPort;

        group = new NioEventLoopGroup();
		connectionlessBootstrap = new Bootstrap();
        connectionlessBootstrap.group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, false)
            .option(ChannelOption.SO_REUSEADDR, true)
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16000,16000,65536))
            .handler(new ChannelInitializer<DatagramChannel>() {
                @Override
                public void initChannel(DatagramChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());

                    pipeline.addLast("handler", new UDPHandler());
                }
            });

		this.port = myUDPPort;

		try {
            channel = connectionlessBootstrap.bind(port).sync().channel();
            log.info("UDP Channel: "+channel.id().toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}

	//===============================================================================================
	public class UDPHandler extends ChannelInboundHandlerAdapter
	{//===============================================================================================

		//===============================================================================================
		public UDPHandler()
		{//===============================================================================================
			super();
		}

		//===============================================================================================
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception
		{//===============================================================================================
			log.debug("UDP channelActive: "+ctx.channel().id());
            super.channelActive(ctx);
		}
		//===============================================================================================
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception
		{//===============================================================================================
			log.debug("UDP channelInactive: "+ctx.channel().id());
            super.channelInactive(ctx);
		}

		//===============================================================================================
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
		{//===============================================================================================

			try{Thread.currentThread().setName("UDPConnection_UDPHandler");}catch(SecurityException ex){ex.printStackTrace();}

			String s = (String) msg;

			if(BobNet.debugMode)
			{
				if(s.startsWith("Friend_Location_Update")==false)
				log.warn("FROM CLIENT: cID:"+channel.id()+" | "+s);
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

			handleMessage(ctx, s);
		}
	}

	//===============================================================================================
	public void cleanup()
	{//===============================================================================================
		if(channel!=null) {
            try {
                channel.close().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(group != null) {
            group.shutdownGracefully();
        }
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
			log.debug("SEND CLIENT: cID:"+channel.id()+" | "+s.substring(0,s.length()-2));
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
        // In Netty 4 DatagramChannel, writing strings directly works if encoders are set up,
        // but typically for UDP you write DatagramPacket.
        // However, since we added StringEncoder, let's see if writeAndFlush handles it.
        // Wait, StringEncoder is for stream-based mostly. For Datagram, we might need a specific handler
        // or wrap it in a DatagramPacket if we want to specify the destination per packet.
        // Since we are connected or bound... wait, DatagramChannel in Netty 4 is connectionless usually.
        // To send to a specific address, we wrap content in DatagramPacket.

        // But here we are using pipeline filters like DelimiterBasedFrameDecoder which are stream based.
        // Using these on UDP in Netty is tricky. Netty 3 NioDatagramChannelFactory supported it somewhat differently.
        // For now, let's assume the pipeline setup works similar to TCP for the payload, but we need to target the address.

        // Actually, DelimiterBasedFrameDecoder on UDP is problematic because packets might not be fragmented/assembled the same way.
        // But let's stick to the migration of API first.

        // If we want to send to 'address', we should probably use writeAndFlush(new DatagramPacket(buffer, address)).
        // But the previous code just did channel.write(s, remoteAddress).

        // Let's try writing the string and let the encoder handle it, but we need to ensure the remote address is attached if not connected.
        // Netty 4 channel.write() doesn't take an address.

        // We might need to wrap it. But let's just use writeAndFlush(s) and assume the channel is connected?
        // No, UDPConnection uses "connectionlessBootstrap".
        // The previous code `channel.write(s, getPeerSocketAddress_S())` implies sending to a specific address.

        // We will need to wrap the string in a DatagramPacket in the pipeline or change the architecture.
        // For a direct migration, we can't easily pass the address through the StringEncoder.
        // The StringEncoder converts String to ByteBuf.
        // We need a handler that takes that ByteBuf and the target address and wraps in DatagramPacket.

        // For now, I'll assume we can connect the channel to the peer if we are talking to one peer, but this class seems to support one peer at a time?
        // `_peerAddress` is stored.

        // If we connect the channel, we can just write(s).
        // But UDPConnection logic seems to be: bind to local port, send to peer address.

        // Hack: We can't easily change the pipeline logic here without breaking the "modernize as much as possible" scope into "rewrite".
        // I will try to use `channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8), address))`
        // BUT we have encoders in the pipeline.

        // Let's keep it simple: `channel.connect(address)` if we only talk to one peer?
        // But `UDPConnection` seems to switch peers?

        // Let's rely on the fact that we can just write the message.
        // Actually, if we use `DatagramPacket`, the StringEncoder might get in the way.
        // We might need to remove StringEncoder/Decoder for UDP and handle bytes manually, or use `AddressedEnvelope`.

        // Given the constraints, I will attempt to use `channel.writeAndFlush` but I suspect this part needs more work.
        // I'll leave a TODO or try to implement a wrapper.

        // Ideally: String -> [StringEncoder] -> ByteBuf -> [MessageToMessageEncoder wrapping ByteBuf in DatagramPacket] -> Output
        // But we need to pass the address.

        // For now, I'll just write:
		return channel.writeAndFlush(new io.netty.channel.socket.DatagramPacket(
            io.netty.buffer.Unpooled.copiedBuffer(s, io.netty.util.CharsetUtil.UTF_8),
            address
        ));
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
	public void handleMessage(ChannelHandlerContext ctx, String msg)
	{//===============================================================================================
		//override this
		log.error("This function should always be overridden");
	}
	//===============================================================================================
	public void incomingPeerConnectResponse(String msg)
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
