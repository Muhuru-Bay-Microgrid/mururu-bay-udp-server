package org.mbmg.udp.client;
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

        InetSocketAddress address = new InetSocketAddress("localhost", port);

        String sendingContent = "4:#STD:123456,511;L:308;TM:1404120015;D:1;T:01;C:44;A00:0.632;A01:00000;A02:00000;A03:00000;A04:00000;A05:00000;A06:00000;A07:00000;A08:00000;A09:00000;A10:00000;A11:00000;A12:00000;A13:22.31;A14:22.68;P01:00000000;P02:00000000;P03:00000000;P04:00000000;P05:00000000;P06:00000000;K01:13300000000000000;O01:0000;28#\r\n";
        ch.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(sendingContent, CharsetUtil.UTF_8), address)).sync();

        // ClientHandler will close the DatagramChannel when a response is received.
        //If the channel is not closed within 5 seconds, print an error message and quit.
        //if (!ch.closeFuture().await(10000)) {
        //  System.err.println("QOTM request timed out.");
        //}
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

    new Client(port).start();

  }
}