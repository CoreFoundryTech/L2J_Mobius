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
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Salvation 140 lighting candle event packet.
 */
public class ExLightingCandleEvent implements IClientOutgoingPacket
{
	public static final ExLightingCandleEvent DISABLED = new ExLightingCandleEvent(0);
	public static final ExLightingCandleEvent ENABLED = new ExLightingCandleEvent(1);

	private final int _value;

	private ExLightingCandleEvent(int value)
	{
		_value = value;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_LIGHTING_CANDLE_EVENT.writeId(packet);
		packet.writeH(_value);
		return true;
	}
}
