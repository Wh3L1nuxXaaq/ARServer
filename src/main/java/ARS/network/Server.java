package ARS.network;

import ARS.network.packet.BaseListener;
import ARS.network.packet.Packet;
import ARS.network.packet.PacketListener;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.nio.charset.StandardCharsets;

public class Server implements PacketListener {
    private static int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
    private static Server INSTANCE = new Server();
    private ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Server() {}

    public static Server getInstance() {
        return INSTANCE;
    }

    public void start(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, 0, 4, 0, 4));
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                            pipeline.addLast(new BaseListener(channels));
                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("Server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void sendPacket(Packet packet, ChannelHandlerContext ctx) {
        if (ctx != null && ctx.channel().isActive()) {
            byte[] data = packet.toBytes();
            ByteBuf buffer = ctx.alloc().buffer(data.length);
            buffer.writeBytes(data);
            ctx.writeAndFlush(buffer);
        }
    }

    public void broadcastPacket(Packet packet) {
        byte[] data = packet.toBytes();
        ByteBuf buffer = Unpooled.buffer(data.length);
        buffer.writeBytes(data);
        channels.writeAndFlush(buffer);
    }
    @Override
    public void send(Packet packet) {
        broadcastPacket(packet);
    }

    @Override
    public void receive(Packet packet) {
        System.out.println("Received: " + packet.getData());
    }
}
