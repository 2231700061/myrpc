package com.myrpc.rpc.netty;

import com.myrpc.rpc.Channel;
import com.myrpc.rpc.FutureListener;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.net.SocketAddress;

public class NettyChannel implements Channel {

	private static final AttributeKey<NettyChannel> NETTY_CHANNEL_KEY = AttributeKey.valueOf("netty.channel");

	public static NettyChannel attachChannel(io.netty.channel.Channel channel) {
		Attribute<NettyChannel> attr = channel.attr(NETTY_CHANNEL_KEY);
		NettyChannel nChannel = attr.get();
		if (nChannel == null) {
			NettyChannel newNChannel = new NettyChannel(channel);
			nChannel = attr.setIfAbsent(newNChannel);
			if (nChannel == null) {
				nChannel = newNChannel;
			}
		}
		return nChannel;
	}

	private final io.netty.channel.Channel channel;

	private NettyChannel(io.netty.channel.Channel channel) {
		this.channel = channel;
	}

	public io.netty.channel.Channel channel() {
		return channel;
	}

	@Override
	public String id() {
		return channel.id().asShortText(); // 注意这里的id并不是全局唯一, 单节点中是唯一的
	}

	@Override
	public boolean isActive() {
		return channel.isActive();
	}

	@Override
	public boolean inIoThread() {
		return channel.eventLoop().inEventLoop();
	}

	@Override
	public SocketAddress localAddress() {
		return channel.localAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		return channel.remoteAddress();
	}

	@Override
	public boolean isWritable() {
		return channel.isWritable();
	}

	@Override
	public boolean isAutoRead() {
		return channel.config().isAutoRead();
	}

	@Override
	public void setAutoRead(boolean autoRead) {
		channel.config().setAutoRead(autoRead);
	}

	@Override
	public Channel close() {
		channel.close();
		return this;
	}

	@Override
	public Channel close(final FutureListener<Channel> listener) {
		final Channel Channel = this;
		channel.close().addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					listener.operationSuccess(Channel);
				} else {
					listener.operationFailure(Channel, future.cause());
				}
			}
		});
		return Channel;
	}

	@Override
	public Channel write(Object msg) {
		channel.writeAndFlush(msg, channel.voidPromise());
		return this;
	}

	@Override
	public Channel write(Object msg, final FutureListener<Channel> listener) {
		final Channel Channel = this;
		channel.writeAndFlush(msg).addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					listener.operationSuccess(Channel);
				} else {
					listener.operationFailure(Channel, future.cause());
				}
			}
		});
		return Channel;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || (obj instanceof NettyChannel && channel.equals(((NettyChannel) obj).channel));
	}

	@Override
	public int hashCode() {
		return channel.hashCode();
	}

	@Override
	public String toString() {
		return channel.toString();
	}
}
