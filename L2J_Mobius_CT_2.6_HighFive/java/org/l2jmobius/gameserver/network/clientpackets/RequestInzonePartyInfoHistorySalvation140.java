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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ExLoadInzonePartyHistorySalvation140;

/** Salvation 140 inzone party history request. */
public class RequestInzonePartyInfoHistorySalvation140 implements IClientIncomingPacket
{
	private int _skipped;

	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_skipped = packet.getReadableBytes();
		packet.readB(_skipped);
		return true;
	}

	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		System.out.println("SALVATION140 RequestInzonePartyInfoHistory account=" + client.getAccountName() + ", player=" + player.getName() + ", skipped=" + _skipped);
		client.sendPacket(new ExLoadInzonePartyHistorySalvation140());
	}
}
