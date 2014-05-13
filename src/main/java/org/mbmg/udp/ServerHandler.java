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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerHandler extends
        SimpleChannelInboundHandler<DatagramPacket>
{
    
    private static final Random random = new Random();
    private int counter;
    private int consumerCounter;
    private BlockingQueue<UdpRequest> queue = new LinkedBlockingQueue<UdpRequest>();
    
    public ServerHandler()
    {
        for (int i = 0; i < 512; i++)
        {
            new Consumer().start();
        }
    }
    
    // Quotes from Mohandas K. Gandhi:
    private static final String[] quotes = {
            "Where there is love there is life.",
            "First they ignore you, then they laugh at you, then they fight you, then you win.",
            "Be the change you want to see in the world.",
            "The weak can never forgive. Forgiveness is the attribute of the strong.", };
    
    private static String nextQuote()
    {
        int quoteId;
        synchronized (random)
        {
            quoteId = random.nextInt(quotes.length);
        }
        return quotes[quoteId];
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
    {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception
    {
        cause.printStackTrace();
    }
    
    private class Consumer extends Thread
    {
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    UdpRequest request = queue.take();
                    System.err.println(Thread.currentThread().getName() + " "
                            + request.getPacket() + " " + ++consumerCounter);
                    sleep(80); // Processing time
                    
                    if ("QOTM?".equals(request.getPacket().content()
                            .toString(CharsetUtil.UTF_8)))
                    {
                        request.getContext().write(
                                new DatagramPacket(Unpooled.copiedBuffer(
                                        "QOTM: " + nextQuote()
                                                + consumerCounter,
                                        CharsetUtil.UTF_8), request.getPacket()
                                        .sender()));
                    }
                } catch (InterruptedException e)
                {
                    e.printStackTrace(); // To change body of catch statement
                                         // use File | Settings | File
                                         // Templates.
                }
            }
        }
    }
    
    private class UdpRequest
    {
        private DatagramPacket packet;
        private ChannelHandlerContext context;
        
        private UdpRequest(DatagramPacket packet, ChannelHandlerContext context)
        {
            this.packet = packet;
            this.context = context;
        }
        
        private DatagramPacket getPacket()
        {
            return packet;
        }
        
        private ChannelHandlerContext getContext()
        {
            return context;
        }
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet)
            throws Exception
    {
        queue.put(new UdpRequest(packet.copy(), ctx));
        // Thread.sleep(50);
        System.out.println(Thread.currentThread().getName() + " " + packet
                + " " + ++counter);
    }
}