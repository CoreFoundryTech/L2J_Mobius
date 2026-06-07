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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author KenM
 */
public class ExSetCompassZoneCode implements IClientOutgoingPacket
{
	public static final int ALTEREDZONE = 0x08;
	public static final int SIEGEWARZONE1 = 0x0A;
	public static final int SIEGEWARZONE2 = 0x0B;
	public static final int PEACEZONE = 0x0C;
	public static final int SEVENSIGNSZONE = 0x0D;
	public static final int PVPZONE = 0x0E;
	public static final int GENERALZONE = 0x0F;
	
	private final int _zoneType;
	
	public ExSetCompassZoneCode(int val)
	{
		_zoneType = val;
	}

	public ExSetCompassZoneCode(PlayerInstance player)
	{
		if (player.isInsideZone(ZoneId.ALTERED))
		{
			_zoneType = ALTEREDZONE;
		}
		else if (player.isInsideZone(ZoneId.SIEGE))
		{
			_zoneType = SIEGEWARZONE2;
		}
		else if (player.isInsideZone(ZoneId.PVP))
		{
			_zoneType = PVPZONE;
		}
		else if (player.isIn7sDungeon())
		{
			_zoneType = SEVENSIGNSZONE;
		}
		else if (player.isInsideZone(ZoneId.PEACE))
		{
			_zoneType = PEACEZONE;
		}
		else
		{
			_zoneType = GENERALZONE;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SET_COMPASS_ZONE_CODE.writeId(packet);
		packet.writeD(_zoneType);
		return true;
	}
}
