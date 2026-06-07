/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.l2jmobius.commons.network.codecs;

import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.commons.network.IConnectionState;
import org.l2jmobius.commons.network.IIncomingPacket;
import org.l2jmobius.commons.network.IIncomingPackets;
import org.l2jmobius.commons.network.PacketReader;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author Nos
 * @param <T>
 */
public class PacketDecoder<T>extends ByteToMessageDecoder
{
	private static final Logger LOGGER = Logger.getLogger(PacketDecoder.class.getName());
	
	private final IIncomingPackets<T>[] _incomingPackets;
	private final T _client;
	
	public PacketDecoder(IIncomingPackets<T>[] incomingPackets, T client)
	{
		_incomingPackets = incomingPackets;
		_client = client;
	}
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
	{
		if ((in == null) || !in.isReadable())
		{
			return;
		}
		
		try
		{
			final short packetId = in.readUnsignedByte();
			if (packetId >= _incomingPackets.length)
			{
				logSalvation140Packet("unknown-range", packetId, null, in);
				LOGGER.finer("Unknown packet: " + Integer.toHexString(packetId));
				return;
			}

			final IIncomingPackets<T> incomingPacket = _incomingPackets[packetId];
			if (incomingPacket == null)
			{
				logSalvation140Packet("unknown-null", packetId, null, in);
				LOGGER.finer("Unknown packet: " + Integer.toHexString(packetId));
				return;
			}

			final IConnectionState connectionState = ctx.channel().attr(IConnectionState.ATTRIBUTE_KEY).get();
			if ((connectionState == null) || !incomingPacket.getConnectionStates().contains(connectionState))
			{
				logSalvation140Packet("invalid-state " + incomingPacket, packetId, connectionState, in);
				// LOGGER.warning(incomingPacket + ": Connection at invalid state: " + connectionState + " Required States: " + incomingPacket.getConnectionStates());
				return;
			}
			
			final IIncomingPacket<T> packet = incomingPacket.newIncomingPacket();
			if ((packet != null) && packet.read(_client, new PacketReader(in)))
			{
				out.add(packet);
			}
		}
		finally
		{
			// We always consider that we read whole packet.
			in.readerIndex(in.writerIndex());
		}
	}

	private void logSalvation140Packet(String reason, short packetId, IConnectionState connectionState, ByteBuf in)
	{
		if (!isSalvation140Client())
		{
			return;
		}

		final String message = "SALVATION140 decoder " + reason + ", packet=0x" + String.format("%02X", packetId & 0xFF) + ", state=" + connectionState + ", readable=" + in.readableBytes() + ", bytes=" + toHex(in, Math.min(in.readableBytes(), 32)) + ", client=" + _client;
		LOGGER.warning(message);
		System.out.println(message);
	}

	private boolean isSalvation140Client()
	{
		try
		{
			return Boolean.TRUE.equals(_client.getClass().getMethod("isSalvation140Client").invoke(_client));
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private static String toHex(ByteBuf in, int length)
	{
		final StringBuilder sb = new StringBuilder(length * 3);
		final int readerIndex = in.readerIndex();
		for (int i = 0; i < length; i++)
		{
			if (i > 0)
			{
				sb.append(' ');
			}
			sb.append(String.format("%02X", in.getUnsignedByte(readerIndex + i)));
		}
		return sb.toString();
	}
}
