package org.mbmg.udp.server;
/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.CharsetUtil;
import org.mbmg.udp.util.Parser;
import org.mbmg.udp.util.Record;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerHandler extends
        SimpleChannelInboundHandler<DatagramPacket> {

    private int counter;
    private int consumerCounter;
    private BlockingQueue<UdpRequest> queue = new LinkedBlockingQueue<UdpRequest>();

    private final GraphiteClient graphiteClient;


    public ServerHandler(String graphiteHost, int graphitePort) {
        graphiteClient = new GraphiteClient(graphiteHost,graphitePort);
        new Consumer().start();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
    }

    private class Consumer extends Thread {


        @Override
        public void run() {

            while (true) {
                try {
                    UdpRequest request = queue.take();
                    System.err.println(Thread.currentThread().getName() + " "
                            + request.getPacket() + " " + ++consumerCounter);
                    String recievedContent = request.getPacket().content().toString(CharsetUtil.UTF_8);
                    System.out.println(recievedContent);
                    // To ignore the packets that the datalogger sends to test connection
                    // and which has the following pattern: @67688989
                    if (!recievedContent.startsWith("@")) {
                        Record newRecord = Parser.toRecord(recievedContent);
                        System.out.println(newRecord.toGraphite());

                        // RJP - totally naive implementation - no idea if this will work yet
                        List<String> channelData = newRecord.toGraphite();
                        for (String chanelSample : channelData) {
                            graphiteClient.sendData(chanelSample);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // To change body of catch statement
                    // use File | Settings | File
                    // Templates.
                }
            }
        }
    }

    private class UdpRequest {
        private DatagramPacket packet;
        private ChannelHandlerContext context;

        private UdpRequest(DatagramPacket packet, ChannelHandlerContext context) {
            this.packet = packet;
            this.context = context;
        }

        private DatagramPacket getPacket() {
            return packet;
        }

        private ChannelHandlerContext getContext() {
            return context;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception {
        queue.put(new UdpRequest(packet.copy(), ctx));
        // Thread.sleep(50);
        System.out.println(Thread.currentThread().getName() + " " + packet
                + " " + ++counter);

    }

    private static class GraphiteClient {

        private static final EventLoopGroup group = new NioEventLoopGroup();
        private static final StringEncoder ENCODER = new StringEncoder();
        private static final WriteTimeoutHandler TIMEOUT_HANDLER = new WriteTimeoutHandler(120);
        private static final Bootstrap bootstrap = new Bootstrap();
        private final String graphiteHost;
        private final int graphitePort;

        private Channel connection;

        private GraphiteClient(String graphiteHost, int graphitePort) {
            this.graphiteHost = graphiteHost;
            this.graphitePort = graphitePort;
        }

        public void startUp() {
            try {
                bootstrap.group(group);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(ENCODER);
                        ch.pipeline().addLast(TIMEOUT_HANDLER);
                    }
                });
                ChannelFuture f = bootstrap.connect(graphiteHost, graphitePort).sync();
                this.connection = f.channel();
            } catch (Exception ex) {
                ex.printStackTrace();
                group.shutdownGracefully();
            }
        }

        public void sendData(String data) {
            // Connect lazily to make start work even if graphite isn't up
            if(connection == null) {
                startUp();
            }
            if (connection != null && connection.isOpen()) {
                connection.writeAndFlush(data);
            }
        }

        public void shutdown() {
            if (connection != null) {
                connection.close().awaitUninterruptibly();
            }
            System.out.println("--- GRAPHITE CLIENT - Stopped.");
        }

    }
}