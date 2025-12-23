package com.bobsgame.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.*;
import java.io.*;
import java.net.*;
import java.sql.*;

import javax.mail.*;
import javax.mail.internet.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import com.bobsgame.ServerMain;
import com.bobsgame.shared.Utils;
import com.bobsgame.net.*;
import com.bobsgame.net.BobsGameLeaderBoardAndHighScoreBoard.LeaderBoardScore;
import com.bobsgame.server.assets.AssetDataIndex;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Version;
import com.restfb.types.User;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.jasypt.util.text.BasicTextEncryptor;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

public class GameServerTCP {

    // Netty 4 Thread Pools
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    EventExecutorGroup executionGroup;

    Timer timer;

    public CopyOnWriteArrayList<Channel> channels = new CopyOnWriteArrayList<Channel>();
    public ConcurrentHashMap<BobsGameClient, Channel> channelsByClient = new ConcurrentHashMap<BobsGameClient, Channel>();
    public ConcurrentHashMap<Channel, BobsGameClient> clientsByChannel = new ConcurrentHashMap<Channel, BobsGameClient>();
    public ConcurrentHashMap<Long, BobsGameClient> clientsByUserID = new ConcurrentHashMap<Long, BobsGameClient>();
    public ConcurrentHashMap<String, BobsGameClient> clientsByFacebookID = new ConcurrentHashMap<String, BobsGameClient>();
    public ConcurrentHashMap<String, BobsGameClient> clientsByEmailAddress = new ConcurrentHashMap<String, BobsGameClient>();
    public ConcurrentHashMap<String, BobsGameClient> clientsByUserName = new ConcurrentHashMap<String, BobsGameClient>();

    public CopyOnWriteArrayList<BobsGameRoom> rooms = new CopyOnWriteArrayList<BobsGameRoom>();
    public ConcurrentHashMap<Long, BobsGameRoom> roomsByUserID = new ConcurrentHashMap<Long, BobsGameRoom>();
    public ConcurrentHashMap<String, BobsGameRoom> roomsByRoomUUID = new ConcurrentHashMap<String, BobsGameRoom>();

    ServerBootstrap tcpServerBootstrap;
    Channel tcpChannel;

    public static Logger log = (Logger) LoggerFactory.getLogger(GameServerTCP.class);

    static ComboPooledDataSource amazonRDSConnectionPool = null;
    static ComboPooledDataSource dreamhostSQLConnectionPool = null;

    public GameServerTCP() {
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

                    pipeline.addLast("idleStateHandler", new IdleStateHandler(30, 30, 30));
                    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(65535, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast(executionGroup, "handler", new BobsGameServerHandler());
                }
            });

        int serverPort = BobNet.serverTCPPort;
        try {
            tcpChannel = tcpServerBootstrap.bind(serverPort).sync().channel();
            log.info("Server TCP ChannelID: " + tcpChannel.id());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Database setup
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.slf4j.Slf4jMLog");
        ((Logger) LoggerFactory.getLogger(com.mchange.v2.async.ThreadPoolAsynchronousRunner.class)).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger(com.mchange.v2.resourcepool.ResourcePool.class)).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger("com.mchange.v2.resourcepool.BasicResourcePool")).setLevel(Level.WARN);
        ((Logger) LoggerFactory.getLogger(com.mchange.v2.c3p0.impl.C3P0PooledConnectionPool.class)).setLevel(Level.WARN);

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            amazonRDSConnectionPool = new ComboPooledDataSource();
            amazonRDSConnectionPool.setDriverClass("com.mysql.jdbc.Driver");
            amazonRDSConnectionPool.setJdbcUrl(PrivateCredentials.AMAZON_RDS_URL);
            amazonRDSConnectionPool.setUser(PrivateCredentials.AMAZON_RDS_USERNAME);
            amazonRDSConnectionPool.setPassword(PrivateCredentials.AMAZON_RDS_PASSWORD);
            amazonRDSConnectionPool.setMinPoolSize(3);
            amazonRDSConnectionPool.setAcquireIncrement(5);
            amazonRDSConnectionPool.setMaxPoolSize(80);
            amazonRDSConnectionPool.setAutoCommitOnClose(true);
            amazonRDSConnectionPool.setTestConnectionOnCheckin(true);
            amazonRDSConnectionPool.setTestConnectionOnCheckout(true);
            amazonRDSConnectionPool.setAutomaticTestTable("c3p0_test_table");
            amazonRDSConnectionPool.setIdleConnectionTestPeriod(30);

            dreamhostSQLConnectionPool = new ComboPooledDataSource();
            dreamhostSQLConnectionPool.setDriverClass("com.mysql.jdbc.Driver");
            dreamhostSQLConnectionPool.setJdbcUrl(PrivateCredentials.DREAMHOST_SQL_URL);
            dreamhostSQLConnectionPool.setUser(PrivateCredentials.DREAMHOST_SQL_USERNAME);
            dreamhostSQLConnectionPool.setPassword(PrivateCredentials.DREAMHOST_SQL_PASSWORD);
            dreamhostSQLConnectionPool.setMinPoolSize(3);
            dreamhostSQLConnectionPool.setAcquireIncrement(5);
            dreamhostSQLConnectionPool.setMaxPoolSize(20);
            dreamhostSQLConnectionPool.setAutoCommitOnClose(true);
            dreamhostSQLConnectionPool.setTestConnectionOnCheckin(true);
            dreamhostSQLConnectionPool.setTestConnectionOnCheckout(true);
            dreamhostSQLConnectionPool.setAutomaticTestTable("c3p0_test_table");
            dreamhostSQLConnectionPool.setIdleConnectionTestPeriod(30);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        if (amazonRDSConnectionPool != null) amazonRDSConnectionPool.close();
        if (dreamhostSQLConnectionPool != null) dreamhostSQLConnectionPool.close();

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
                long id = -1;
                BobsGameClient c = getClientByChannel(ctx.channel());
                if (c != null) id = c.userID;

                if (e.state() == IdleState.READER_IDLE) {
                    log.info("channelIdle: No incoming traffic from client timeout. Closing channel. | ChannelID: " + ctx.channel().id() + " | ClientuserID: " + id);
                    ctx.close();
                } else if (e.state() == IdleState.WRITER_IDLE) {
                    writePlaintext(ctx.channel(), "ping" + BobNet.endline);
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String id = ctx.channel().id().toString();
            String ip = ctx.channel().remoteAddress().toString();
            log.info("channelConnected: (" + id + ") " + ip + " " + getCityFromIP(ip));
            channels.add(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            BobsGameClient c = getClientByChannel(ctx.channel());
            String userName = "";
            if (c != null) userName = c.userName;

            String id = ctx.channel().id().toString();
            String ip = ctx.channel().remoteAddress().toString();

            log.info("channelDisconnected: (" + id + ") " + userName + " " + ip + " " + getCityFromIP(ip));

            channels.remove(ctx.channel());

            if (c != null) {
                channelsByClient.remove(c);
                clientsByChannel.remove(ctx.channel());
                clientsByUserID.remove(c.userID);
                if (c.facebookID.length() > 0) clientsByFacebookID.remove(c.facebookID);
                if (c.userName.length() > 0) clientsByUserName.remove(c.userName);
                if (c.emailAddress.length() > 0) clientsByEmailAddress.remove(c.emailAddress);

                if (c.startTime != -1 && c.userID != -1) {
                    long end = System.currentTimeMillis();
                    long start = c.startTime;
                    long len = (end - start) / 1000;

                    Connection databaseConnection = openDreamhostSQLDB();
                    if (databaseConnection != null) {
                        try {
                            PreparedStatement ps = databaseConnection.prepareStatement(
                                "UPDATE connections SET endTime = ? , lengthSeconds = ? WHERE startTime = ?");
                            ps.setLong(1, end);
                            ps.setLong(2, len);
                            ps.setLong(3, c.startTime);
                            ps.executeUpdate();
                            ps.close();
                        } catch (Exception ex) {
                            log.error("DB ERROR: " + ex.getMessage());
                        }
                        closeDBConnection(databaseConnection);
                    }

                    long totalTimePlayed_DB = -1;
                    databaseConnection = openAccountsDBOnAmazonRDS();
                    if (databaseConnection != null) {
                        try {
                            PreparedStatement ps = databaseConnection.prepareStatement("SELECT totalTimePlayed FROM accounts WHERE userID = ?");
                            ps.setLong(1, c.userID);
                            ResultSet resultSet = ps.executeQuery();
                            if (resultSet.next()) {
                                totalTimePlayed_DB = resultSet.getLong("totalTimePlayed");
                            }
                            resultSet.close();
                            ps.close();
                        } catch (Exception ex) {
                            log.error("DB ERROR: " + ex.getMessage());
                        }

                        totalTimePlayed_DB += len;

                        try {
                            PreparedStatement ps = databaseConnection.prepareStatement(
                                "UPDATE accounts SET lastSeenTime = ? , isOnline = ? , totalTimePlayed = ? WHERE userID = ?");
                            ps.setLong(1, end);
                            ps.setInt(2, 0);
                            ps.setLong(3, totalTimePlayed_DB);
                            ps.setLong(4, c.userID);
                            ps.executeUpdate();
                            ps.close();
                        } catch (Exception ex) {
                            log.error("DB ERROR: " + ex.getMessage());
                        }
                        closeDBConnection(databaseConnection);
                    }
                }
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            String message = (String) msg;

            if (!message.startsWith("ping") && !message.startsWith("pong") &&
                !message.startsWith("Server_Stats") && !message.startsWith("Online_Friends") &&
                !message.startsWith("Bobs_Game_RoomList")) {
                long userID = -1;
                String userName = "";
                BobsGameClient c = getClientByChannel(ctx.channel());
                if (c != null) {
                    userID = c.userID;
                    userName = c.userName;
                }

                if (message.contains("Login") || message.contains("Reconnect") || message.contains("Create_Account"))
                    log.warn("FROM CLIENT: (" + ctx.channel().id() + ") " + userName + " | " + message.substring(0, message.indexOf(":") + 1) + "(censored)");
                else log.warn("FROM CLIENT: (" + ctx.channel().id() + ") " + userName + " | " + message);
            }

            if (message.startsWith("ping")) {
                writePlaintext(ctx.channel(), "pong" + BobNet.endline);
                return;
            }
            if (message.startsWith("pong")) {
                return;
            }

            MessageEvent e = new MessageEvent(ctx.channel(), message);

            if (message.startsWith(BobNet.Server_IP_Address_Request)) { incomingServerIPAddressRequest(e); return; }
            if (message.startsWith(BobNet.Server_Stats_Request)) { incomingServerStatsRequest(e); return; }
            if (message.startsWith(BobNet.Client_Location_Request)) { incomingClientLocationRequest(e); return; }
            if (message.startsWith(BobNet.Login_Request)) { incomingLoginRequest(e); return; }
            if (message.startsWith(BobNet.Facebook_Login_Request)) { incomingFacebookLoginOrCreateAccountAndLoginRequest(e); return; }
            if (message.startsWith(BobNet.Reconnect_Request)) { incomingReconnectRequest(e); return; }
            if (message.startsWith(BobNet.Password_Recovery_Request)) { incomingPasswordRecoveryRequest(e); return; }
            if (message.startsWith(BobNet.Create_Account_Request)) { incomingCreateAccountRequest(e); return; }
            if (message.startsWith(BobNet.Initial_GameSave_Request)) { incomingInitialGameSaveRequest(e); return; }
            if (message.startsWith(BobNet.Encrypted_GameSave_Update_Request)) { incomingGameSaveUpdateRequest(e); return; }
            if (message.startsWith(BobNet.Load_Event_Request)) { incomingLoadEventRequest(e); return; }
            if (message.startsWith(BobNet.Postal_Code_Update_Request)) { incomingPostalCodeUpdateRequest(e); return; }
            if (message.startsWith(BobNet.Sprite_Request_By_Name)) { incomingSpriteDataRequestByName(e); return; }
            if (message.startsWith(BobNet.Sprite_Request_By_ID)) { incomingSpriteDataRequestByID(e); return; }
            if (message.startsWith(BobNet.Map_Request_By_Name)) { incomingMapDataRequestByName(e); return; }
            if (message.startsWith(BobNet.Map_Request_By_ID)) { incomingMapDataRequestByID(e); return; }
            if (message.startsWith(BobNet.Dialogue_Request)) { incomingDialogueDataRequest(e); return; }
            if (message.startsWith(BobNet.Flag_Request)) { incomingFlagDataRequest(e); return; }
            if (message.startsWith(BobNet.Skill_Request)) { incomingSkillDataRequest(e); return; }
            if (message.startsWith(BobNet.Event_Request)) { incomingEventDataRequest(e); return; }
            if (message.startsWith(BobNet.GameString_Request)) { incomingGameStringDataRequest(e); return; }
            if (message.startsWith(BobNet.Music_Request)) { incomingMusicDataRequest(e); return; }
            if (message.startsWith(BobNet.Sound_Request)) { incomingSoundDataRequest(e); return; }
            if (message.startsWith(BobNet.Player_Coords)) { incomingPlayerCoords(e); return; }
            if (message.startsWith(BobNet.Update_Facebook_Account_In_DB_Request)) { incomingUpdateFacebookAccountInDBRequest(e); return; }
            if (message.startsWith(BobNet.Online_Friends_List_Request)) { incomingOnlineFriendsListRequest(e); return; }
            if (message.startsWith(BobNet.Add_Friend_By_UserName_Request)) { incomingAddFriendByUserNameRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_GameTypesAndSequences_Download_Request)) { incomingBobsGameGameTypesDownloadRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_GameTypesAndSequences_Upload_Request)) { incomingBobsGameGameTypesUploadRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_GameTypesAndSequences_Vote_Request)) { incomingBobsGameGameTypesVoteRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_RoomList_Request)) { incomingBobsGameRoomListRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_TellRoomHostToAddMyUserID)) { incomingBobsGameTellRoomHostToAddUserID(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_HostingPublicRoomUpdate)) { incomingBobsGameHostingPublicRoomUpdate(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_HostingPublicRoomStarted)) { incomingBobsGameHostingPublicRoomStarted(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_HostingPublicRoomCanceled)) { incomingBobsHostingPublicRoomCanceled(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_HostingPublicRoomEnded)) { incomingBobsGameHostingPublicRoomEnded(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_GameStats)) { incomingBobsGameGameStats(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_GetHighScoresAndLeaderboardsRequest)) { incomingBobsGameGetHighScoresAndLeaderboardsRequest(e); return; }
            if (message.startsWith(BobNet.Bobs_Game_ActivityStream_Request)) { incomingBobsGameActivityStreamRequest(e); return; }
            if (message.startsWith(BobNet.Chat_Message)) { incomingChatMessage(e, true); return; }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (cause instanceof ConnectException) {
                log.error("Exception caught from Client connection - ConnectException: " + cause.getMessage());
            } else if (cause instanceof ReadTimeoutException) {
                log.error("Exception caught from Client connection - ReadTimeoutException: " + cause.getMessage());
            } else if (cause instanceof IOException) {
                // Ignore generic IOException
            } else {
                log.error("Unexpected Exception caught from Client connection: " + cause.getMessage());
                cause.printStackTrace();
            }
            // ctx.close(); // Optional: close on exception
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

    public ChannelFuture writeFuture(Channel c, String s) {
        if (!s.endsWith(BobNet.endline)) {
            log.error("Message doesn't end with endline");
            s = s + BobNet.endline;
        }

        long id = -1;
        String userName = "";
        BobsGameClient client = getClientByChannel(c);
        if (client != null) { id = client.userID; userName = client.userName; }
        log.info("SEND: (" + c.id() + ") " + userName + " | " + s.substring(0, s.length() - 2));

        return c.writeAndFlush(s);
    }

    public ArrayList<ChannelFuture> writePlaintext(Channel c, String s) {
        ArrayList<ChannelFuture> futures = new ArrayList<ChannelFuture>();
        if (!s.endsWith(BobNet.endline)) {
            log.error("Message doesn't end with endline");
            s = s + BobNet.endline;
        }

        long id = -1;
        String userName = "";
        if (c != null) {
            BobsGameClient client = getClientByChannel(c);
            if (client != null) { id = client.userID; userName = client.userName; }
        }

        if (!s.startsWith("ping") && !s.startsWith("pong")) {
            if (s.contains("Login") || s.contains("Reconnect") || s.contains("Create_Account"))
                log.info("SEND CLIENT: (" + c.id() + ") " + userName + " | " + s.substring(0, s.indexOf(":") + 1) + "(censored)");
            else
                log.info("SEND CLIENT: (" + c.id() + ") " + userName + " | " + s.substring(0, Math.min(100, s.length() - 2)) + "...");
        }

        if (s.length() > 1400) {
            s = s.substring(0, s.indexOf(BobNet.endline));
            while (s.length() > 1300) {
                String partial = "PARTIAL:" + s.substring(0, 1300) + BobNet.endline;
                s = s.substring(1300);
                futures.add(c.writeAndFlush(partial));
            }
            String finalString = "FINAL:" + s + BobNet.endline;
            futures.add(c.writeAndFlush(finalString));
        } else {
            futures.add(c.writeAndFlush(s));
        }
        return futures;
    }

    public ArrayList<ChannelFuture> writeCompressed(Channel c, String s) {
        ArrayList<ChannelFuture> futures = new ArrayList<ChannelFuture>();
        if (!s.endsWith(BobNet.endline)) {
            log.error("Message doesn't end with endline");
            s = s + BobNet.endline;
        }

        long id = -1;
        String userName = "";
        if (c != null) {
            BobsGameClient client = getClientByChannel(c);
            if (client != null) {
                id = client.userID;
                userName = client.userName;
            }
        }

        String plainTextCat = s.substring(0, Math.min(100, s.length() - 2));
        int origSize = s.length();

        try {
            LZ4Factory factory = LZ4Factory.safeInstance();
            byte[] data = s.getBytes("UTF-8");
            final int decompressedLength = data.length;
            LZ4Compressor compressor = factory.fastCompressor();
            int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
            byte[] compressedBuffer = new byte[maxCompressedLength];
            int compressedLength = compressor.compress(data, 0, decompressedLength, compressedBuffer, 0, maxCompressedLength);
            byte[] compressedBytes = new byte[compressedLength];
            System.arraycopy(compressedBuffer, 0, compressedBytes, 0, compressedLength);

            String base64 = Base64.encodeBase64String(compressedBytes);
            s = new String(base64);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int compSize = s.length();
        String com = ("Compressed " + origSize + " to " + compSize + " " + (int) (((float) compSize / (float) origSize) * 100) + "%");

        if (!plainTextCat.contains("Server_Stats") && !plainTextCat.contains("Online_Friends") &&
            !plainTextCat.contains("Bobs_Game_RoomList") && !plainTextCat.contains("Friend_Is_Online")) {
            if (plainTextCat.contains("Login") || plainTextCat.contains("Reconnect") || plainTextCat.contains("Create_Account"))
                log.info("SEND CLIENT: (" + c.id() + ") " + userName + " | " + plainTextCat.substring(0, plainTextCat.indexOf(":") + 1) + "(censored) " + com);
            else
                log.info("SEND CLIENT: (" + c.id() + ") " + userName + " | " + plainTextCat + "..." + " " + com);
        }

        if (s.length() > 1400) {
            while (s.length() > 1300) {
                String partial = "PARTIAL:" + s.substring(0, 1300);
                s = s.substring(1300);
                futures.add(c.writeAndFlush(partial + BobNet.endline));
            }
            String finalString = "FINAL:" + s;
            futures.add(c.writeAndFlush(finalString + BobNet.endline));
        } else {
            futures.add(c.writeAndFlush(s + BobNet.endline));
        }
        return futures;
    }

    public static Connection openAccountsDBOnAmazonRDS() {
        Connection c = null;
        try {
            c = amazonRDSConnectionPool.getConnection();
            c.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public static Connection openDreamhostSQLDB() {
        Connection c = null;
        try {
            c = dreamhostSQLConnectionPool.getConnection();
            c.setAutoCommit(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    public static void closeDBConnection(Connection databaseConnection) {
        if (databaseConnection != null) {
            try {
                databaseConnection.close();
            } catch (Exception ex) {
                log.error("DB Error while closing DB: " + ex.getMessage());
            }
        }
    }

    public static File getResourceAsFile(String resourcePath) {
        try {
            InputStream in = ServerMain.class.getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) return null;
            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    File geoDB = null;
    public String getCityFromIP(String ip) {
        if (ip.startsWith("/")) ip = ip.substring(1);
        if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":"));
        if (ip.equals("127.0.0.1")) return "localhost";

        if (geoDB == null) geoDB = getResourceAsFile("GeoLite2-City.mmdb");

        String loc = "";
        try {
            DatabaseReader reader = new DatabaseReader.Builder(geoDB).build();
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            Country country = response.getCountry();
            Subdivision subdivision = response.getMostSpecificSubdivision();
            loc += subdivision.getName() + ", " + country.getName();
        } catch (Exception e) {
            // ignore
        }
        return loc;
    }

    private void incomingServerIPAddressRequest(MessageEvent e) {
        writeCompressed(e.getChannel(), BobNet.Server_IP_Address_Response + ServerMain.myIPAddressString + BobNet.endline);
    }

    class ServerStats {
        public int serversOnline = 0;
        public int usersOnline = 0;
        public long serverUptime = 0;
        public String toString() {
            String s = "";
            s += "serversOnline:`" + (serversOnline) + "`,";
            s += "usersOnline:`" + (usersOnline) + "`,";
            s += "serverUptime:`" + (serverUptime) + "`,";
            return s;
        }
    }

    private void incomingServerStatsRequest(MessageEvent e) {
        ServerStats s = new ServerStats();
        s.serversOnline = 1;
        s.usersOnline = clientsByChannel.size();
        s.serverUptime = (System.currentTimeMillis() - ServerMain.startTime) / 1000;
        writeCompressed(e.getChannel(), BobNet.Server_Stats_Response + s.toString() + BobNet.endline);
    }

    private void incomingClientLocationRequest(MessageEvent e) {
        String ip = e.getChannel().remoteAddress().toString();
        writeCompressed(e.getChannel(), BobNet.Client_Location_Response + getCityFromIP(ip) + BobNet.endline);
    }

    public void sendTellClientTheirSessionWasLoggedOnSomewhereElseAndCloseChannel(long userID) {
        if (!BobNet.debugMode) {
            BobsGameClient check = clientsByUserID.get(userID);
            if (check != null && check.channel.isActive()) {
                clientsByChannel.remove(check.channel);
                clientsByUserID.remove(check.userID);
                if (check.facebookID.length() > 0) clientsByFacebookID.remove(check.facebookID);
                ChannelFuture cf = writeFuture(check.channel, BobNet.Tell_Client_Their_Session_Was_Logged_On_Somewhere_Else + BobNet.endline);
                cf.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    public class ChannelCloseListener implements ChannelFutureListener {
        @Override
        public void operationComplete(ChannelFuture f) throws Exception {
            Channel c = f.channel();
            c.close();
        }
    }

    private void incomingLoginRequest(MessageEvent e) {
        String s = (String) e.getMessage();
        String userNameOrEmailAddress = "";
        String password = "";

        s = s.substring(s.indexOf(":")+1);
        s = s.substring(s.indexOf("`")+1);
        userNameOrEmailAddress = s.substring(0,s.indexOf("`"));

        s = s.substring(s.indexOf("`")+3);
        password = s.substring(0,s.indexOf("`"));
        s = s.substring(s.indexOf("`")+1);
        s = s.substring(s.indexOf(",")+1);
        String clientInfo = "";
        if(s.length()>BobNet.endline.length())clientInfo=s;

        userNameOrEmailAddress = userNameOrEmailAddress.trim();
        if(userNameOrEmailAddress.length() == 0)return;

        password = password.trim();
        if(password.length() == 0)return;

        userNameOrEmailAddress = userNameOrEmailAddress.toLowerCase();

        String queryString = "";

        if(userNameOrEmailAddress.contains("@")) {
            queryString = "SELECT accountVerified, accountCreatedTime, passwordHash, userID, userName, emailAddress, facebookID, firstLoginTime, timesLoggedIn, firstIP FROM accounts WHERE emailAddress = ?";
        } else {
            queryString = "SELECT accountVerified, accountCreatedTime, passwordHash, userID, userName, emailAddress, facebookID, firstLoginTime, timesLoggedIn, firstIP FROM accounts WHERE userName = ?";
        }

        String sessionToken = createRandomHash();

        BobsGameClient c = null;
        boolean loggedIn = false;

        Connection databaseConnection = openAccountsDBOnAmazonRDS();
        if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}

        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            ps = databaseConnection.prepareStatement(queryString);
            ps.setString(1, userNameOrEmailAddress);
            resultSet = ps.executeQuery();
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());closeDBConnection(databaseConnection);return;}

        int accountVerified_DB = -1;
        long accountCreatedTime_DB = -1;
        String passwordHash_DB = "";
        long userID_DB = -1;
        String userName_DB = "";
        String emailAddress_DB = "";
        String facebookID_DB = "";
        long firstLoginTime_DB = -1;
        int timesLoggedIn_DB = -1;
        String firstIP_DB = "";

        try {
            if(resultSet.next()) {
                accountVerified_DB = resultSet.getInt("accountVerified");
                accountCreatedTime_DB = resultSet.getLong("accountCreatedTime");
                passwordHash_DB = resultSet.getString("passwordHash");
                userID_DB = resultSet.getLong("userID");
                userName_DB = resultSet.getString("userName");
                emailAddress_DB = resultSet.getString("emailAddress");
                facebookID_DB = resultSet.getString("facebookID");
                firstLoginTime_DB = resultSet.getLong("firstLoginTime");
                timesLoggedIn_DB = resultSet.getInt("timesLoggedIn");
                firstIP_DB = resultSet.getString("firstIP");

                if(passwordHash_DB==null)passwordHash_DB="";
                if(facebookID_DB==null)facebookID_DB="";
                if(firstIP_DB==null)firstIP_DB="";

                resultSet.close();
                ps.close();
            } else {
                resultSet.close();
                ps.close();
            }
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());closeDBConnection(databaseConnection);return;}

        closeDBConnection(databaseConnection);

        {
            String passwordHash = hashPassword(password, accountCreatedTime_DB);
            if(passwordHash_DB.length()>0 && passwordHash.equals(passwordHash_DB)) {
                loggedIn = true;
                c = clientsByUserID.get(userID_DB);
                if(c==null) {
                    c = new BobsGameClient();
                    c.channel = e.getChannel();
                    c.startTime = System.currentTimeMillis();
                    c.encryptionKey = createRandomHash();
                    c.userID = userID_DB;
                    c.facebookID = facebookID_DB;
                    c.emailAddress = emailAddress_DB;
                    c.userName = userName_DB;

                    clientsByChannel.put(e.getChannel(), c);
                    clientsByUserID.put(c.userID,c);
                    if(c.userName.length()>0)clientsByUserName.put(c.userName,c);
                    if(c.emailAddress.length()>0)clientsByEmailAddress.put(c.emailAddress,c);
                    if(c.facebookID.length()>0)clientsByFacebookID.put(c.facebookID,c);
                } else {
                    if(c.channel!=e.getChannel()) {
                        if(c.channel.isActive()) {
                            sendTellClientTheirSessionWasLoggedOnSomewhereElseAndCloseChannel(c.userID);
                        }
                    }
                    clientsByChannel.remove(c.channel);
                    channelsByClient.remove(c);

                    c.channel = e.getChannel();

                    clientsByChannel.put(e.getChannel(), c);
                    channelsByClient.put(c,e.getChannel());
                }

                long firstLoginTime = firstLoginTime_DB;
                if(firstLoginTime==0)firstLoginTime = c.startTime;

                int timesLoggedIn = timesLoggedIn_DB;
                timesLoggedIn++;

                String firstIP = firstIP_DB;
                if(firstIP.length()==0)firstIP = ""+e.getChannel().remoteAddress().toString();

                writeCompressed(e.getChannel(),BobNet.Login_Response+"Success,"+c.userID+",`"+sessionToken+"`"+BobNet.endline);

                databaseConnection = openAccountsDBOnAmazonRDS();
                if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}
                try {
                    ps = databaseConnection.prepareStatement(
                            "UPDATE accounts SET sessionToken = ?, encryptionKey = ?, firstLoginTime = ?, lastLoginTime = ?, lastSeenTime = ?, timesLoggedIn = ?, firstIP = ?, lastIP = ?, isOnline = ? WHERE userID = ?");
                    int i=0;
                    ps.setString(++i, sessionToken);
                    ps.setString(++i, c.encryptionKey);
                    ps.setLong(++i, firstLoginTime);
                    ps.setLong(++i, c.startTime);
                    ps.setLong(++i, c.startTime);
                    ps.setInt(++i, timesLoggedIn);
                    ps.setString(++i, firstIP);
                    ps.setString(++i, ""+e.getChannel().remoteAddress().toString());
                    ps.setInt(++i, 1);
                    ps.setLong(++i, userID_DB);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());closeDBConnection(databaseConnection);return;}
                closeDBConnection(databaseConnection);

                ServerMain.indexClientTCP.send_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(c.userID);
                incomingInitialGameSaveRequest(e);
                incomingOnlineFriendsListRequest(e);

                if(c.facebookID.length()>0) {
                    // incomingUpdateFacebookAccountInDBRequest(e);
                }
            } else {
                // wrong password
            }
        }

        if(loggedIn==false) {
            writeCompressed(e.getChannel(),BobNet.Login_Response+"Failed"+BobNet.endline);
            return;
        }
    }

    public BobsGameClient getClientByChannel(Channel c) {
        return clientsByChannel.get(c);
    }

    public BobsGameClient getClientConnectionByMessageEvent(MessageEvent e) {
        return getClientByChannel(e.getChannel());
    }

    public GameSave getGameSaveByConnection(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        if(c == null) return null;
        return getGameSaveFromDB(c.userID);
    }

    public static GameSave getGameSaveFromDB(long userID) {
        GameSave g = null;
        Connection databaseConnection = openAccountsDBOnAmazonRDS();
        if(databaseConnection==null) return null;
        try {
            PreparedStatement ps = databaseConnection.prepareStatement("SELECT * FROM accounts WHERE userID = ?");
            ps.setLong(1, userID);
            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()) {
                g = new GameSave(resultSet);
            }
            resultSet.close();
            ps.close();
        } catch (Exception ex) { ex.printStackTrace(); }
        closeDBConnection(databaseConnection);
        return g;
    }

    private void incomingFacebookLoginOrCreateAccountAndLoginRequest(MessageEvent e) {
        String s = (String) e.getMessage();
        String facebookID = "";
        String facebookAccessToken = "";

        s = s.substring(s.indexOf(":")+1);
        s = s.substring(s.indexOf("`")+1);
        facebookID = s.substring(0,s.indexOf("`"));
        s = s.substring(s.indexOf("`")+3);
        facebookAccessToken = s.substring(0,s.indexOf("`"));
        s = s.substring(s.indexOf("`")+1);
        String clientInfo = "";
        if(s.length()>0)clientInfo=s.substring(s.indexOf(",")+1);

        facebookID = facebookID.trim();
        if(facebookID.length() == 0)return;
        facebookAccessToken = facebookAccessToken.trim();
        if(facebookAccessToken.length() == 0)return;

        FacebookClient facebookClient = null;
        String facebookEmail = "";
        try {
            facebookClient = new DefaultFacebookClient(facebookAccessToken, Version.VERSION_14_0);
            User user = facebookClient.fetchObject("me", User.class);
            facebookEmail = user.getEmail();
        } catch(Exception ex) {
            log.error("Error logging into facebook and getting facebook email for facebookID: "+facebookID+" fbAccessToken: "+facebookAccessToken+" "+ex.getMessage());
            writeCompressed(e.getChannel(),BobNet.Facebook_Login_Response+"Failed"+BobNet.endline);
            ex.printStackTrace();
            return;
        }

        AccessToken accessToken = new DefaultFacebookClient(Version.VERSION_14_0).obtainExtendedAccessToken(PrivateCredentials.facebookAppID, PrivateCredentials.facebookAppSecret, facebookAccessToken);
        facebookAccessToken = accessToken.getAccessToken();

        int status = loginWithFacebookIDAndAccessToken(e, facebookID, facebookAccessToken, clientInfo, facebookEmail);

        if(status==1) {
        } else if(status==-1) {
            log.error("Facebook_Login_Response FacebookID: "+facebookID+" facebookAccessToken:"+facebookAccessToken);
            writeCompressed(e.getChannel(),BobNet.Facebook_Login_Response+"Failed"+BobNet.endline);
        }
        if(status==0) {
            String password = "";
            List<String> myList = null;
            try {
                myList=new Sampler().sampler(4);
            } catch(Exception e1) {
                e1.printStackTrace();
            }
            for(int index = 0;index<myList.size();index++) {
                password = password + myList.get(index).trim()+" ";
            }
            password = password.trim();

            String verificationHash = createRandomHash();
            String sessionToken = createRandomHash();
            String encryptionKey = createRandomHash();
            long accountCreatedTime = System.currentTimeMillis();
            String passwordHash = hashPassword(password,accountCreatedTime);

            Connection databaseConnection = openAccountsDBOnAmazonRDS();
            if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}
            PreparedStatement ps = null;
            try {
                ps = databaseConnection.prepareStatement(
                        "INSERT INTO accounts (emailAddress, passwordHash, verificationHash, accountVerified, accountCreatedTime, sessionToken, encryptionKey, firstLoginTime, lastLoginTime, lastSeenTime, timesLoggedIn, firstIP, lastIP, isOnline) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, facebookEmail);
                ps.setString(2, passwordHash);
                ps.setString(3, verificationHash);
                ps.setInt(4, 1);
                ps.setLong(5, accountCreatedTime);
                ps.setString(6, sessionToken);
                ps.setString(7, encryptionKey);
                ps.setLong(8, accountCreatedTime);
                ps.setLong(9, accountCreatedTime);
                ps.setLong(10, accountCreatedTime);
                ps.setInt(11, 1);
                ps.setString(12, ""+e.getChannel().remoteAddress().toString());
                ps.setString(13, ""+e.getChannel().remoteAddress().toString());
                ps.setInt(14, 1);
                ps.executeUpdate();
                ps.close();
            } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}
            closeDBConnection(databaseConnection);

            loginWithFacebookIDAndAccessToken(e, facebookID, facebookAccessToken, clientInfo, facebookEmail);
            sendFacebookAccountCreationEmail(facebookEmail,password);
            status = 1;
        }

        if(status==1) {
            if(facebookID.length()>0) {
                incomingUpdateFacebookAccountInDBRequest(e);
                incomingOnlineFriendsListRequest(e);
            }
        }
    }

    private int loginWithFacebookIDAndAccessToken(MessageEvent e, String facebookID, String facebookAccessToken, String clientInfo, String facebookEmail) {
        int retVal = -1;
        String sessionToken = createRandomHash();
        BobsGameClient c = null;

        Connection databaseConnection = openAccountsDBOnAmazonRDS();
        if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return -1;}

        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            ps = databaseConnection.prepareStatement("SELECT accountVerified, accountCreatedTime, passwordHash, userID, userName, emailAddress, firstLoginTime, timesLoggedIn, firstIP FROM accounts WHERE facebookID = ? OR emailAddress = ?");
            ps.setString(1, facebookID);
            ps.setString(2, facebookEmail);
            resultSet = ps.executeQuery();
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}

        try {
            if(resultSet.next()) {
                retVal = 1;
                long userID_DB = resultSet.getLong("userID");
                long firstLoginTime_DB = resultSet.getLong("firstLoginTime");
                int timesLoggedIn_DB = resultSet.getInt("timesLoggedIn");
                String firstIP_DB = resultSet.getString("firstIP");
                String userName_DB = resultSet.getString("userName");
                String emailAddress_DB = resultSet.getString("emailAddress");

                if(firstIP_DB==null)firstIP_DB="";
                if(userName_DB==null)userName_DB="";
                if(emailAddress_DB==null)emailAddress_DB="";

                resultSet.close();
                ps.close();
                closeDBConnection(databaseConnection);

                c = clientsByUserID.get(userID_DB);
                if(c==null) {
                    c = new BobsGameClient();
                    c.channel = e.getChannel();
                    c.startTime = System.currentTimeMillis();
                    c.encryptionKey = createRandomHash();
                    c.userID = userID_DB;
                    c.facebookID = facebookID;
                    c.emailAddress = emailAddress_DB;
                    c.userName = userName_DB;

                    clientsByChannel.put(e.getChannel(), c);
                    clientsByUserID.put(c.userID,c);
                    if(c.userName.length()>0)clientsByUserName.put(c.userName,c);
                    if(c.emailAddress.length()>0)clientsByEmailAddress.put(c.emailAddress,c);
                    if(c.facebookID.length()>0)clientsByFacebookID.put(c.facebookID,c);
                } else {
                    if(c.channel!=e.getChannel()) {
                        if(c.channel.isActive()) {
                            sendTellClientTheirSessionWasLoggedOnSomewhereElseAndCloseChannel(c.userID);
                        }
                    }
                    clientsByChannel.remove(c.channel);
                    channelsByClient.remove(c);
                    c.channel = e.getChannel();
                    clientsByChannel.put(e.getChannel(), c);
                    channelsByClient.put(c,e.getChannel());
                }

                long firstLoginTime = firstLoginTime_DB;
                if(firstLoginTime==0)firstLoginTime = c.startTime;

                int timesLoggedIn = timesLoggedIn_DB;
                timesLoggedIn++;

                String firstIP = firstIP_DB;
                if(firstIP.length()==0)firstIP = ""+e.getChannel().remoteAddress().toString();

                ServerMain.indexClientTCP.send_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(c.userID);

                databaseConnection = openAccountsDBOnAmazonRDS();
                if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return -1;}
                try {
                    ps = databaseConnection.prepareStatement(
                            "UPDATE accounts SET sessionToken = ?, encryptionKey = ?, firstLoginTime = ?, lastLoginTime = ?, lastSeenTime = ?, timesLoggedIn = ?, firstIP = ?, lastIP = ?, isOnline = ?, facebookID = ?, emailAddress = ?, userName = ? WHERE facebookID = ? OR emailAddress = ?");
                    int i=0;
                    ps.setString(++i, sessionToken);
                    ps.setString(++i, c.encryptionKey);
                    ps.setLong(++i, firstLoginTime);
                    ps.setLong(++i, c.startTime);
                    ps.setLong(++i, c.startTime);
                    ps.setInt(++i, timesLoggedIn);
                    ps.setString(++i, firstIP);
                    ps.setString(++i, ""+e.getChannel().remoteAddress().toString());
                    ps.setInt(++i, 1);
                    ps.setString(++i, facebookID);
                    ps.setString(++i, emailAddress_DB);
                    ps.setString(++i, userName_DB);
                    ps.setString(++i, facebookID);
                    ps.setString(++i, facebookEmail);
                    ps.executeUpdate();
                    ps.close();
                } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}
                closeDBConnection(databaseConnection);

                // Dreamhost stats removed for brevity/Netty4 context
            } else {
                retVal=0;
                resultSet.close();
                ps.close();
                closeDBConnection(databaseConnection);
            }
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}

        if(c!=null) {
            writeCompressed(e.getChannel(),BobNet.Facebook_Login_Response+"Success,"+c.userID+",`"+sessionToken+"`"+BobNet.endline);
            return 1;
        }

        return retVal;
    }

    private void incomingReconnectRequest(MessageEvent e) {
        String s = (String) e.getMessage();
        long userID = -1;
        String sessionToken = "";
        s = s.substring(s.indexOf(":")+1);
        s = s.substring(s.indexOf("`")+1);
        try{userID = Long.parseLong(s.substring(0,s.indexOf("`")));}catch(Exception ex){log.warn("userID not an long?");return;}
        s = s.substring(s.indexOf("`")+3);
        sessionToken = s.substring(0,s.indexOf("`"));
        s = s.substring(s.indexOf("`")+1);
        s = s.substring(s.indexOf(",")+1);
        String clientInfo = "";
        if(s.length()>BobNet.endline.length())clientInfo=s;

        if(userID==-1){log.warn("userID -1");return;}
        sessionToken = sessionToken.trim();
        if(sessionToken.length() == 0){log.warn("No sessionToken for userID:"+userID);return;}

        boolean loggedIn = false;
        Connection databaseConnection = openAccountsDBOnAmazonRDS();
        if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}

        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            ps = databaseConnection.prepareStatement("SELECT sessionToken, userName, emailAddress, encryptionKey, facebookID, timesLoggedIn FROM accounts WHERE userID = ?");
            ps.setLong(1, userID);
            resultSet = ps.executeQuery();
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());closeDBConnection(databaseConnection);return;}

        String sessionToken_DB = "";
        String userName_DB = "";
        String emailAddress_DB = "";
        String encryptionKey_DB = "";
        String facebookID_DB = "";
        int timesLoggedIn_DB = -1;

        try {
            if(resultSet.next()) {
                sessionToken_DB = resultSet.getString("sessionToken");
                userName_DB = resultSet.getString("userName");
                emailAddress_DB = resultSet.getString("emailAddress");
                encryptionKey_DB = resultSet.getString("encryptionKey");
                facebookID_DB = resultSet.getString("facebookID");
                timesLoggedIn_DB = resultSet.getInt("timesLoggedIn");

                if(sessionToken_DB==null)sessionToken_DB="";
                if(userName_DB==null)userName_DB="";
                if(emailAddress_DB==null)emailAddress_DB="";
                if(encryptionKey_DB==null)encryptionKey_DB="";
                if(facebookID_DB==null)facebookID_DB="";

                resultSet.close();
                ps.close();
            } else {
                resultSet.close();
                ps.close();
            }
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();closeDBConnection(databaseConnection);return;}
        closeDBConnection(databaseConnection);

        if(sessionToken_DB.length()>0 && sessionToken.equals(sessionToken_DB)) {
            loggedIn = true;
            BobsGameClient c = clientsByUserID.get(userID);
            if(c==null) {
                c = new BobsGameClient();
                c.channel = e.getChannel();
                c.startTime = System.currentTimeMillis();
                c.encryptionKey = createRandomHash();
                c.userID = userID;
                c.userName = userName_DB;
                c.emailAddress = emailAddress_DB;
                c.facebookID = facebookID_DB;

                clientsByChannel.put(e.getChannel(), c);
                clientsByUserID.put(c.userID,c);
                if(c.emailAddress.length()>0)clientsByEmailAddress.put(c.emailAddress,c);
                if(c.userName.length()>0)clientsByUserName.put(c.userName,c);
                if(c.facebookID.length()>0)clientsByFacebookID.put(c.facebookID,c);
            } else {
                if(c.channel!=e.getChannel()) {
                    if(c.channel.isActive()) {
                        sendTellClientTheirSessionWasLoggedOnSomewhereElseAndCloseChannel(c.userID);
                    }
                }
                clientsByChannel.remove(c.channel);
                channelsByClient.remove(c);
                c.channel = e.getChannel();
                clientsByChannel.put(e.getChannel(), c);
                channelsByClient.put(c,e.getChannel());
            }

            if(encryptionKey_DB==null||encryptionKey_DB.length()==0) {
                encryptionKey_DB = c.encryptionKey;
            }
            c.encryptionKey = encryptionKey_DB;

            int timesLoggedIn = timesLoggedIn_DB;
            timesLoggedIn++;

            writeCompressed(e.getChannel(),BobNet.Reconnect_Response+"Success,"+c.userID+",`"+sessionToken+"`"+BobNet.endline);

            databaseConnection = openAccountsDBOnAmazonRDS();
            if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}
            try {
                ps = databaseConnection.prepareStatement("UPDATE accounts SET encryptionKey = ?, lastSeenTime = ?, timesLoggedIn = ?, lastIP = ?, isOnline = ? WHERE userID = ?");
                int i=0;
                ps.setString(++i, c.encryptionKey);
                ps.setLong(++i, c.startTime);
                ps.setInt(++i, timesLoggedIn);
                ps.setString(++i, ""+e.getChannel().remoteAddress().toString());
                ps.setInt(++i, 1);
                ps.setLong(++i, userID);
                ps.executeUpdate();
                ps.close();
            } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());closeDBConnection(databaseConnection); return;}
            closeDBConnection(databaseConnection);

            ServerMain.indexClientTCP.send_INDEX_User_Logged_On_This_Server_Log_Them_Off_Other_Servers(c.userID);
            incomingInitialGameSaveRequest(e);
            incomingOnlineFriendsListRequest(e);
            if(c.facebookID.length()>0) {
                // incomingUpdateFacebookAccountInDBRequest(e);
            }
        } else {
            log.debug("Wrong sessionToken for userID:"+userID);
        }

        if(loggedIn==false) {
            writeCompressed(e.getChannel(),BobNet.Reconnect_Response+"Failed"+BobNet.endline);
            return;
        }
    }

    private void incomingPasswordRecoveryRequest(MessageEvent e) {}
    private void incomingCreateAccountRequest(MessageEvent e) {}

    private void incomingInitialGameSaveRequest(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        if(c==null)return;
        long userID = c.userID;
        if(userID==-1)return;

        GameSave g = getGameSaveByConnection(e);

        if(g!=null) {
            String gameSave = g.encodeGameSave();
            String encryptedGameSave = encryptGameSave(c,g);

            writeCompressed(e.getChannel(),BobNet.Initial_GameSave_Response+gameSave+BobNet.endline);
            writeCompressed(e.getChannel(),BobNet.Encrypted_GameSave_Update_Response+"-1,"+encryptedGameSave+BobNet.endline);
        }
    }

    private void incomingGameSaveUpdateRequest(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        long userID = c.userID;
        if(userID==-1)return;

        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":")+1);
        int gameSaveID = -1;
        try{gameSaveID = Integer.parseInt(s.substring(0,s.indexOf(',')));}catch(NumberFormatException ex){ex.printStackTrace();return;}
        s = s.substring(s.indexOf(",")+1);

        class UpdateCommand {
            String variableName = "";
            String value = "";
            Object changedValue = null;
        }
        ArrayList<UpdateCommand> commands = new ArrayList<UpdateCommand>();

        while(s.startsWith("gameSave:")==false) {
            UpdateCommand u = new UpdateCommand();
            u.variableName = s.substring(0,s.indexOf(':'));
            s = s.substring(s.indexOf("`")+1);
            u.value = s.substring(0,s.indexOf('`'));
            s = s.substring(s.indexOf("`")+2);
            commands.add(u);
        }
        s = s.substring(s.indexOf(":")+1);
        String encryptedGameSave = s.substring(0,s.indexOf(":"));
        GameSave g = decryptGameSave(c,encryptedGameSave);

        for(int i=0;i<commands.size();i++) {
            UpdateCommand u = commands.get(i);
            u.changedValue = g.updateGameSaveValue(u.variableName,u.value);
            if(u.changedValue==null){log.warn("Error updating GameSave:"+u.variableName+","+u.value);}
        }

        String updatedEncryptedGameSave = encryptGameSave(c,g);
        writeCompressed(e.getChannel(),BobNet.Encrypted_GameSave_Update_Response+gameSaveID+","+updatedEncryptedGameSave+BobNet.endline);

        for(int i=0;i<commands.size();i++) {
            UpdateCommand u = commands.get(i);
            if(u.changedValue!=null) {
                Connection databaseConnection = openAccountsDBOnAmazonRDS();
                if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}
                try {
                    PreparedStatement ps = databaseConnection.prepareStatement(
                            "UPDATE accounts SET "+u.variableName+" = ? WHERE userID = ?");
                    ps.setString(1, u.changedValue.toString());
                    ps.setLong(2, c.userID);
                    ps.executeUpdate();
                    ps.close();
                }catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}
                closeDBConnection(databaseConnection);
            }
        }
    }

    private void incomingLoadEventRequest(MessageEvent e) {}
    private void incomingPostalCodeUpdateRequest(MessageEvent e) {}
    private void incomingSpriteDataRequestByName(MessageEvent e) {}
    private void incomingSpriteDataRequestByID(MessageEvent e) {}
    private void incomingMapDataRequestByName(MessageEvent e) {}
    private void incomingMapDataRequestByID(MessageEvent e) {}
    private void incomingDialogueDataRequest(MessageEvent e) {}
    private void incomingFlagDataRequest(MessageEvent e) {}
    private void incomingSkillDataRequest(MessageEvent e) {}
    private void incomingEventDataRequest(MessageEvent e) {}
    private void incomingGameStringDataRequest(MessageEvent e) {}
    private void incomingMusicDataRequest(MessageEvent e) {}
    private void incomingSoundDataRequest(MessageEvent e) {}
    private void incomingPlayerCoords(MessageEvent e) {}
    private void incomingUpdateFacebookAccountInDBRequest(MessageEvent e) {}

    private void incomingOnlineFriendsListRequest(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        long userID = -1;
        if(c!=null) {
            userID = c.userID;
        } else {log.error("Could not find client connection, was connection dropped?");return;}

        Connection databaseConnection = openAccountsDBOnAmazonRDS();
        if(databaseConnection==null){log.error("DB ERROR: Could not open DB connection!");return;}

        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            ps = databaseConnection.prepareStatement("SELECT userNameFriends, facebookFriends FROM accounts WHERE userID = ?");
            ps.setLong(1, userID);
            resultSet = ps.executeQuery();
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}

        String facebookFriendsCSV = "";
        String userNameFriendsCSV = "";

        try {
            if(resultSet.next()) {
                facebookFriendsCSV = resultSet.getString("facebookFriends");
                userNameFriendsCSV = resultSet.getString("userNameFriends");
                if(facebookFriendsCSV==null)facebookFriendsCSV="";
                if(userNameFriendsCSV==null)userNameFriendsCSV="";
            }
            resultSet.close();
            ps.close();
            closeDBConnection(databaseConnection);
        } catch (Exception ex){log.error("DB ERROR: "+ex.getMessage());ex.printStackTrace();}

        ServerMain.indexClientTCP.send_INDEX_Tell_All_Servers_To_Tell_FacebookIDs_That_UserID_Is_Online(userID,facebookFriendsCSV);
        ServerMain.indexClientTCP.send_INDEX_Tell_All_Servers_To_Tell_UserNames_That_UserID_Is_Online(userID,userNameFriendsCSV);

        String onlineFriendUserIDsCSV = "";

        while(facebookFriendsCSV.length()>0) {
            String friendFacebookID = facebookFriendsCSV.substring(0,facebookFriendsCSV.indexOf(","));
            facebookFriendsCSV = facebookFriendsCSV.substring(facebookFriendsCSV.indexOf(",")+1);

            BobsGameClient friendClient = clientsByFacebookID.get(friendFacebookID);
            if(friendClient!=null) {
                String type = "facebook";
                long friendID = friendClient.userID;
                onlineFriendUserIDsCSV = onlineFriendUserIDsCSV+type+":"+friendID+",";
                writeCompressed(friendClient.channel,BobNet.Friend_Is_Online_Notification+type+":"+userID+BobNet.endline);
            }
        }

        while(userNameFriendsCSV.length()>0) {
            String friendUserName = userNameFriendsCSV.substring(0,userNameFriendsCSV.indexOf(","));
            userNameFriendsCSV = userNameFriendsCSV.substring(userNameFriendsCSV.indexOf(",")+1);

            BobsGameClient friendClient = clientsByUserName.get(friendUserName);
            if(friendClient!=null) {
                String type = "userName";
                long friendID = friendClient.userID;
                onlineFriendUserIDsCSV = onlineFriendUserIDsCSV+type+":"+friendID+",";
                writeCompressed(friendClient.channel,BobNet.Friend_Is_Online_Notification+type+":"+userID+BobNet.endline);
            }
        }

        writeCompressed(e.getChannel(),BobNet.Online_Friends_List_Response+onlineFriendUserIDsCSV+BobNet.endline);
    }

    private void incomingAddFriendByUserNameRequest(MessageEvent e) {}
    private void incomingBobsGameGameTypesDownloadRequest(MessageEvent e) {}
    private void incomingBobsGameGameTypesUploadRequest(MessageEvent e) {}
    private void incomingBobsGameGameTypesVoteRequest(MessageEvent e) {}
    private void incomingBobsGameRoomListRequest(MessageEvent e) {}
    private void incomingBobsGameTellRoomHostToAddMyUserID(MessageEvent e) {}
    private void incomingBobsGameHostingPublicRoomUpdate(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        if(c==null) return;
        long userID = c.userID;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":")+1);
        BobsGameRoom newRoom = createRoom(s, userID);
        if(newRoom!=null) {
            tellAllClientsNewRoomHasBeenCreated(newRoom, userID);
        }
        ServerMain.indexClientTCP.send_INDEX_Tell_All_Servers_Bobs_Game_Hosting_Room_Update(s, userID);
    }
    private void incomingBobsGameHostingPublicRoomStarted(MessageEvent e) {}
    private void incomingBobsHostingPublicRoomCanceled(MessageEvent e) {
        BobsGameClient c = getClientConnectionByMessageEvent(e);
        if(c==null) return;
        String s = (String) e.getMessage();
        s = s.substring(s.indexOf(":")+1);
        removeRoom(s, c.userID);
        ServerMain.indexClientTCP.send_INDEX_Tell_All_Servers_Bobs_Game_Remove_Room(s, c.userID);
    }
    private void incomingBobsGameHostingPublicRoomEnded(MessageEvent e) {}
    private void incomingBobsGameGameStats(MessageEvent e) {}
    private void incomingBobsGameGetHighScoresAndLeaderboardsRequest(MessageEvent e) {}
    private void incomingBobsGameActivityStreamRequest(MessageEvent e) {}
    private void incomingChatMessage(MessageEvent e, boolean sendToIndex) {}

    public BobsGameRoom createRoom(String s, long userID) {
        BobsGameRoom newRoom = new BobsGameRoom(s);
        if(newRoom.multiplayer_HostUserID!=userID) return null;
        BobsGameRoom oldRoom = roomsByRoomUUID.get(newRoom.uuid);
        if(oldRoom!=null) {
            roomsByRoomUUID.remove(newRoom.uuid);
            roomsByUserID.remove(newRoom.multiplayer_HostUserID);
            rooms.remove(oldRoom);
            newRoom.timeStarted = oldRoom.timeStarted;
            newRoom.timeLastGotUpdate = System.currentTimeMillis();
            roomsByRoomUUID.put(newRoom.uuid, newRoom);
            roomsByUserID.put(newRoom.multiplayer_HostUserID, newRoom);
            rooms.add(newRoom);
            return null;
        } else {
            newRoom.timeStarted = System.currentTimeMillis();
            newRoom.timeLastGotUpdate = System.currentTimeMillis();
            roomsByRoomUUID.put(newRoom.uuid, newRoom);
            roomsByUserID.put(newRoom.multiplayer_HostUserID, newRoom);
            rooms.add(newRoom);
            return newRoom;
        }
    }

    public void tellAllClientsNewRoomHasBeenCreated(BobsGameRoom room, long exceptUserID) {
        for(Channel c : channels) {
            writeCompressed(c, BobNet.Bobs_Game_NewRoomCreatedUpdate + room.encodeRoomData() + BobNet.endline);
        }
    }

    public void removeRoom(String s, long userID) {
        String roomUUID = s.substring(0, s.indexOf(":"));
        BobsGameRoom r = roomsByRoomUUID.get(roomUUID);
        if(r!=null && r.multiplayer_HostUserID==userID) {
            roomsByRoomUUID.remove(r.uuid);
            roomsByUserID.remove(r.multiplayer_HostUserID);
            rooms.remove(r);
        }
    }

    public void sendActivityUpdateToAllClients(String activityString) {
        for(Channel c : channels) {
            writeCompressed(c, BobNet.Bobs_Game_ActivityStream_Update + activityString + BobNet.endline);
        }
    }

    public void sendChatMessageToAllClients(String s) {
        for(Channel c : channels) {
            writeCompressed(c, BobNet.Chat_Message + s + BobNet.endline);
        }
    }

    public GameSave decryptGameSave(BobsGameClient session, String encryptedGameSave) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(session.encryptionKey);
        String plainText = textEncryptor.decrypt(encryptedGameSave);
        GameSave g = new GameSave();
        g.decodeGameSave(plainText);
        return g;
    }

    public String encryptGameSave(BobsGameClient session, GameSave g) {
        String s = g.encodeGameSave();
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword(session.encryptionKey);
        String encryptedText = textEncryptor.encrypt(s);
        return encryptedText;
    }

    public String hashPassword(String password, long accountCreatedTime) {
        return Utils.getStringMD5(PrivateCredentials.passwordSalt+password+accountCreatedTime);
    }

    public String createRandomHash() {
        return Utils.getStringMD5(""+Math.random()+Math.random()+Math.random());
    }

    public void sendEmail(String emailAddress, String subject, String htmlContent, String textContent) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", PrivateCredentials.emailHost);
            props.put("mail.smtp.user", PrivateCredentials.emailUsername);
            props.put("mail.smtp.password", PrivateCredentials.emailPassword);
            props.put("mail.smtp.port", PrivateCredentials.emailPort);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getInstance(props, new DefaultAuthenticator(PrivateCredentials.emailUsername,PrivateCredentials.emailPassword));
            HtmlEmail email = new HtmlEmail();
            email.setMailSession(session);
            email.addTo(emailAddress);
            email.setFrom("noreply@bobsgame.com", "\"bob's game\"");
            email.setSubject(subject);
            email.setHtmlMsg(htmlContent);
            email.setTextMsg(textContent);
            email.send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFacebookAccountCreationEmail(String emailAddress, String password) {
        String subject = "Welcome to \"bob's game!\" Here is your (optional) passphrase.";
        String htmlContent = "<html><head></head><body><p>Your email address was signed up (using Facebook) for an account on <a href=\"http://bobsgame.com\">\"bob's game\"</a>.<br>Here is your randomly generated passphrase if you ever want to log in without Facebook. You can change it from inside the game if you want.<br><br>Passphrase:\""+password+"\"<br><br><br></p></body></html>";
        String textContent = "Your email address was signed up (using Facebook) for an account on http://bobsgame.com \nHere is your randomly generated passphrase if you ever want to log in without Facebook. You can change it from inside the game if you want. \nPassphrase:\""+password+"\"<br>\n";
        sendEmail(emailAddress,subject,htmlContent,textContent);
    }

    public class Sampler {
        public Sampler(){}
        public List<String> sampler (int reservoirSize) throws FileNotFoundException, IOException {
            String currentLine=null;
            List <String> reservoirList= new ArrayList<String>(reservoirSize);
            int count=0;
            Random ra = new Random();
            int randomNumber = 0;
            Scanner sc = new Scanner(ServerMain.class.getClassLoader().getResourceAsStream("1000.txt"));
            sc.useDelimiter("\n");
            while (sc.hasNext()) {
                currentLine = sc.next();
                count ++;
                if (count<=reservoirSize) {
                    reservoirList.add(currentLine);
                } else if ((randomNumber = (int) ra.nextInt(count))<reservoirSize) {
                    reservoirList.set(randomNumber, currentLine);
                }
            }
            sc.close();
            return reservoirList;
        }
    }
}
