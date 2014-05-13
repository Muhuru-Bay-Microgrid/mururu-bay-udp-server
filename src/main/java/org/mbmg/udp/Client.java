package org.mbmg.udp;
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
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * A UDP broadcast client that asks for a quote of the moment (QOTM) to
 * {@link QuoteOfTheMomentServer}.
 * <p/>
 * Inspired by <a href="http://goo.gl/BsXVR">the official Java tutorial</a>.
 */
public class Client extends Thread {

  private final int port;

  public Client(int port) {
    this.port = port;
  }

  public void run() {
    try {
      EventLoopGroup group = new NioEventLoopGroup();
      try {
        Bootstrap b = new Bootstrap();
        b.group(group).channel(NioDatagramChannel.class).handler(new ClientHandler());

        Channel ch = b.bind(0).sync().channel();

        // Broadcast the QOTM request to port 8080.
        InetSocketAddress address = new InetSocketAddress("localhost", port);

        for (int i = 0; i < 1000; i++) {
          sleep(100);
          System.out.println(Thread.currentThread().getName() + " " + i);

          ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8), address)).sync();
        }


        // QuoteOfTheMomentClientHandler will close the DatagramChannel when a
        // response is received.  If the channel is not closed within 5 seconds,
        // print an error message and quit.
        if (!ch.closeFuture().await(10000)) {
          System.err.println("QOTM request timed out.");
        }
      } finally {
        group.shutdownGracefully();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    int port;
    if (args.length > 0) {
      port = Integer.parseInt(args[0]);
    } else {
      port = 8080;
    }

    for (int i = 0; i < 75; i++) {

      new Client(port).start();
    }

    System.err.println("ERROR");
  }
}