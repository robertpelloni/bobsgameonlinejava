package com.bobsgame.serverindex;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.ConnectException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;

import com.bobsgame.net.BobNet;
import com.bobsgame.net.BobsGameServer;
import com.bobsgame.net.PrivateCredentials;

public class IndexServerTCP {

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    EventExecutorGroup executionGroup;

    Timer timer;

    public Vector<BobsGameServer> serverList = new Vector<BobsGameServer>();
    public ConcurrentHashMap<Channel, BobsGameServer> serversByChannel = new ConcurrentHashMap<Channel, BobsGameServer>();
    public ConcurrentHashMap<Integer, BobsGameServer> serversByServerID = new ConcurrentHashMap<Integer, BobsGameServer>();

    ServerBootstrap tcpServerBootstrap;
    Channel tcpChannel;

    public static Logger log = (Logger) LoggerFactory.getLogger(IndexServerTCP.class);

    public IndexServerTCP() {
        timer = new HashedWheelTimer();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        executionGroup = new DefaultEventExecutorGroup(16);

        tcpServerBootstrap = new ServerBootstrap();
        tcpServerBootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 100)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_SNDBUF, 524288)
            .option(ChannelOption.SO_RCVBUF, 524288)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childOption(ChannelOption.SO_SNDBUF, 524288)
            .childOption(ChannelOption.SO_RCVBUF, 524288)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    pipeline.addLast("idleStateHandler", new IdleStateHandler(120, 30, 0));
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65536, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast(executionGroup, "handler", new BobsGameServerHandler());
                }
            });

        int serverPort = BobNet.INDEXServerTCPPort;
        try {
            tcpChannel = tcpServerBootstrap.bind(serverPort).sync().channel();
            log.info("INDEX Server TCP ChannelID: " + tcpChannel.id());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        if (executionGroup != null) executionGroup.shutdownGracefully();
        if (timer != null) timer.stop();
    }

    public class BobsGameServerHandler extends ChannelInboundHandlerAdapter {
        Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                int id = -1;
                BobsGameServer s = getServerByChannel(ctx.channel());
                if (s != null) id = s.serverID;

                if (e.state() == IdleState.READER_IDLE) {
                    log.warn("channelIdle: No incoming traffic from server timeout. Closing channel. ServerID: " + id);
                    ctx.close();
                } else if (e.state() == IdleState.WRITER_IDLE) {
                    ctx.channel().writeAndFlush("ping" + BobNet.endline);
                    if (BobNet.debugMode) log.debug("channelIdle: ping ServerID: " + id);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channelConnected: from Server. ChannelID: " + ctx.channel().id());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("channelDisconnected: from Server. ChannelID: " + ctx.channel().id());

            BobsGameServer s = getServerByChannel(ctx.channel());

            if (s != null) {
                serverList.remove(s);
                serversByChannel.remove(ctx.channel());
                serversByServerID.remove(s.serverID);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String message = (String) msg;

            int serverID = -1;
            BobsGameServer s = getServerByChannel(ctx.channel());
            if (s != null) serverID = s.serverID;

            if (message.startsWith("pong")) {
                return;
            } else if (message.startsWith("ping")) {
            }

            if (BobNet.debugMode) {
                log.warn("FROM SERVER: cID:" + ctx.channel().id() + " sID:" + serverID + " | " + message);
            }

            MessageEvent e = new MessageEvent(ctx.channel(), message);

            if (message.startsWith(BobNet.INDEX_Register_Server_With_INDEX_Request)) { incoming_INDEX_Register_Server_Request(e); return; }

            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online)) { incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(e); return; }
            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online)) { incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(e); return; }
            if (message.startsWith(BobNet.INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online)) { incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online(e); return; }
            if (message.startsWith(BobNet.INDEX_UserID_Logged_On_This_Server_Log_Them_Off_Other_Servers)) { incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(e); return; }

            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update)) { incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(e); return; }
            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_Bobs_Game_Remove_Room)) { incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(e); return; }
            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients)) { incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients(e); return; }
            if (message.startsWith(BobNet.INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients)) { incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients(e); return; }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof ConnectException) {
                log.error("Exception caught from Server connection - ConnectException: " + cause.getMessage());
            } else if (cause instanceof ReadTimeoutException) {
                log.error("Exception caught from Server connection - ReadTimeoutException: " + cause.getMessage());
            } else {
                log.error("Unexpected Exception caught from Server connection: " + cause.getMessage());
                cause.printStackTrace();
            }

            ctx.close();
        }
    }

    public static class MessageEvent {
        Channel channel;
        Object message;
        public MessageEvent(Channel channel, Object message) {
            this.channel = channel;
            this.message = message;
        }
        public Channel getChannel() { return channel; }
        public Object getMessage() { return message; }
    }

    public BobsGameServer getServerByChannel(Channel c) {
        return serversByChannel.get(c);
    }

    public BobsGameServer getServerByServerID(int i) {
        return serversByServerID.get(i);
    }

    public void incoming_INDEX_Register_Server_Request(MessageEvent e) {
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        String passCode = s.substring(0, s.indexOf(","));
        s = s.substring(s.indexOf(",") + 1);
        int serverID = -1;
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(","))); } catch (NumberFormatException ex) { ex.printStackTrace(); return; }
        s = s.substring(s.indexOf(",") + 1);
        String ipAddressString = s.substring(0, s.indexOf(":"));

        if (!passCode.equals(PrivateCredentials.passwordSalt)) {
            e.getChannel().writeAndFlush(BobNet.Server_Register_Server_With_INDEX_Response + "Incorrect passcode, cannot register with index.:-1:" + BobNet.endline);
            e.getChannel().close();
        }

        BobsGameServer server = null;
        if (serverID != -1) {
            server = serversByServerID.get(serverID);
            if (server != null) {
                serverList.remove(server);
                serversByChannel.remove(server.channel);
                serversByServerID.remove(serverID);
                serverID = server.serverID;
            }
        }

        if (server == null) {
            server = new BobsGameServer(e.getChannel(), ipAddressString);
        }

        serverID = server.serverID;

        serverList.add(server);
        serversByChannel.put(e.getChannel(), server);
        serversByServerID.put(server.serverID, server);

        e.getChannel().writeAndFlush(BobNet.Server_Register_Server_With_INDEX_Response + "Successfully registered with index.:" + serverID + ":" + BobNet.endline);
    }

    public void incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(MessageEvent e) {
        BobsGameServer thisServer = getServerByChannel(e.getChannel());

        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        int userID = -1;
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); return; }
        s = s.substring(s.indexOf("`") + 1);
        String facebookIDsCSV = s.substring(0, s.indexOf('`'));

        if (userID == -1) return;
        if (facebookIDsCSV.length() == 0) return;

        for (int i = 0; i < serverList.size(); i++) {
            Channel c = serverList.get(i).channel;
            if (c.isActive()) {
                c.writeAndFlush(BobNet.Server_Tell_All_FacebookIDs_That_UserID_Is_Online + thisServer.serverID + "," + userID + ",`" + facebookIDsCSV + "`" + BobNet.endline);
            } else {
                log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online. Did server drop connection?");
            }
        }
    }

    public void incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(MessageEvent e) {
        BobsGameServer thisServer = getServerByChannel(e.getChannel());

        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        int userID = -1;
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); return; }
        s = s.substring(s.indexOf("`") + 1);
        String userNamesCSV = s.substring(0, s.indexOf('`'));

        if (userID == -1) return;
        if (userNamesCSV.length() == 0) return;

        for (int i = 0; i < serverList.size(); i++) {
            Channel c = serverList.get(i).channel;
            if (c.isActive()) {
                c.writeAndFlush(BobNet.Server_Tell_All_UserNames_That_UserID_Is_Online + thisServer.serverID + "," + userID + ",`" + userNamesCSV + "`" + BobNet.endline);
            } else {
                log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online. Did server drop connection?");
            }
        }
    }

    public void incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online(MessageEvent e) {
        int serverID = -1;
        int userID = -1;

        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf("`") + 1);
        String onlineUserIDCSV = s.substring(0, s.indexOf('`'));

        if (serverID == -1) return;
        if (userID == -1) return;
        if (onlineUserIDCSV.length() == 0) return;

        BobsGameServer originalUserServer = getServerByServerID(serverID);
        if (originalUserServer != null) {
            originalUserServer.channel.writeAndFlush(BobNet.Server_Tell_UserID_That_UserIDs_Are_Online + userID + ",`" + onlineUserIDCSV + "`" + BobNet.endline);
        } else {
            log.warn("serverID could not be found during incoming_INDEX_Tell_ServerID_To_Tell_UserID_That_UserIDs_Are_Online. Did server drop connection?");
        }
    }

    public void incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(MessageEvent e) {
        int serverID = -1;
        int userID = -1;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(":"))); } catch (NumberFormatException ex) { ex.printStackTrace(); }

        if (serverID == -1) return;
        if (userID == -1) return;

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).serverID != serverID) {
                Channel c = serverList.get(i).channel;
                if (c.isActive()) {
                    c.writeAndFlush(BobNet.Server_UserID_Logged_On_Other_Server_So_Log_Them_Off + userID + BobNet.endline);
                } else {
                    log.warn("channel is not connected in incoming_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers. Did server drop connection?");
                }
            }
        }
    }

    public void incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(MessageEvent e) {
        int serverID = -1;
        int userID = -1;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(","))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        String roomString = s.substring(0, s.indexOf(":"));

        if (serverID == -1) return;
        if (userID == -1) return;

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).serverID != serverID) {
                Channel c = serverList.get(i).channel;
                if (c.isActive()) {
                    c.writeAndFlush(BobNet.Server_Bobs_Game_Hosting_Room_Update + serverID + "," + userID + "," + roomString + ":" + BobNet.endline);
                } else {
                    log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update. Did server drop connection?");
                }
            }
        }
    }

    public void incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(MessageEvent e) {
        int serverID = -1;
        int userID = -1;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        try { userID = Integer.parseInt(s.substring(0, s.indexOf(","))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);
        String roomString = s.substring(0, s.indexOf(":"));

        if (serverID == -1) return;
        if (userID == -1) return;

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).serverID != serverID) {
                Channel c = serverList.get(i).channel;
                if (c.isActive()) {
                    c.writeAndFlush(BobNet.Server_Bobs_Game_Remove_Room + serverID + "," + userID + "," + roomString + ":" + BobNet.endline);
                } else {
                    log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room. Did server drop connection?");
                }
            }
        }
    }

    public void incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients(MessageEvent e) {
        int serverID = -1;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);

        String activityString = s.substring(0, s.indexOf(":END:"));

        if (serverID == -1) return;

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).serverID != serverID) {
                Channel c = serverList.get(i).channel;
                if (c.isActive()) {
                    c.writeAndFlush(BobNet.Server_Send_Activity_Update_To_All_Clients + activityString + ":END:" + BobNet.endline);
                } else {
                    log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Send_Activity_Update_To_All_Clients. Did server drop connection?");
                }
            }
        }
    }

    public void incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients(MessageEvent e) {
        int serverID = -1;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":") + 1);
        try { serverID = Integer.parseInt(s.substring(0, s.indexOf(','))); } catch (NumberFormatException ex) { ex.printStackTrace(); }
        s = s.substring(s.indexOf(",") + 1);

        String activityString = s.substring(0, s.indexOf(":END:"));

        if (serverID == -1) return;

        for (int i = 0; i < serverList.size(); i++) {
            if (serverList.get(i).serverID != serverID) {
                Channel c = serverList.get(i).channel;
                if (c.isActive()) {
                    c.writeAndFlush(BobNet.Server_Send_Chat_Message_To_All_Clients + activityString + ":END:" + BobNet.endline);
                } else {
                    log.warn("channel is not connected in incoming_INDEX_Tell_All_Servers_To_Send_Chat_Message_To_All_Clients. Did server drop connection?");
                }
            }
        }
    }

    public void send_Tell_All_Servers_To_Tell_All_Clients_Servers_Are_Shutting_Down() {
        for (int i = 0; i < serverList.size(); i++) {
            Channel c = serverList.get(i).channel;
            if (c.isActive()) {
                c.writeAndFlush(BobNet.Server_Tell_All_Users_Servers_Are_Shutting_Down + BobNet.endline);
            } else {
                log.warn("channel is not connected in send_Tell_All_Servers_To_Tell_All_Clients_Servers_Are_Shutting_Down. Did server drop connection?");
            }
        }
    }

    public void send_Tell_All_Servers_To_Tell_All_Clients_Servers_Have_Shut_Down() {
        for (int i = 0; i < serverList.size(); i++) {
            Channel c = serverList.get(i).channel;
            if (c.isActive()) {
                c.writeAndFlush(BobNet.Server_Tell_All_Users_Servers_Have_Shut_Down + BobNet.endline);
            } else {
                log.warn("channel is not connected in send_Tell_All_Servers_To_Tell_All_Clients_Servers_Have_Shut_Down. Did server drop connection?");
            }
        }
    }
}
