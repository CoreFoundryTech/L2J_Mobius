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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.network.GameClient;

/**
 * Placeholder for authenticated Salvation/Classic 140 ExPackets that L2Scripts accepts but leaves unimplemented.
 */
public class RequestExSalvation140Ignored implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		final int remaining = packet.getReadableBytes();
		if (remaining > 0)
		{
			packet.readB(remaining);
		}
		System.out.println("SALVATION140 ignored ExPacket account=" + client.getAccountName() + ", state=" + client.getConnectionState() + ", skipped=" + remaining);
		return true;
	}

	@Override
	public void run(GameClient client)
	{
		// Intentionally ignored for Salvation/Classic 140 compatibility.
	}
}
