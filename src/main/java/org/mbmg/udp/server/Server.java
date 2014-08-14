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
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 *
 String* Inspired by <a href="http://goo.gl/BsXVR">the official Java tutorial</a>.
 */
public class Server {
    // TODO - we need a ShutdownHook to cleanly release socket resources
    private final int port;
    private final String graphiteHost;
    private final int graphitePort;

    public Server(int port, String graphiteHost, int graphitePort) {
        this.graphiteHost = graphiteHost;
        this.graphitePort = graphitePort;
        this.port = port;
    }

    public void run() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_RCVBUF, 1048576)
             .option(ChannelOption.SO_SNDBUF, 1048576)
             .handler(new ServerHandler(graphiteHost, graphitePort));
            b.bind(port).sync().channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main() throws Exception {
        int port;
        int graphitePort;
        String graphiteHost;
        /*
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
            System.out.println("DEBUG - Port provided: "+port);
        } else {
            port = 6002;
            System.out.println("DEBUG - No port provided, use default one: "+port);
        }
        */
        // Retrieve port specified in Sytem properties. Used port 6001 as default
        port = Integer.parseInt(System.getProperty("org.mbmg.udp.server.port","6002"));
        graphiteHost = System.getProperty("org.mbmg.graphite.server.host","localhost");
        graphitePort = Integer.parseInt(System.getProperty("org.mbmg.graphite.server.port","2003"));
        new Server(port, graphiteHost, graphitePort).run();
    }
}