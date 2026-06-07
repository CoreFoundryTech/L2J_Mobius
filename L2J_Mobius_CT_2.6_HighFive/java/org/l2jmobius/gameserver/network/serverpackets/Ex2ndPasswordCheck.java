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
import org.l2jmobius.gameserver.network.ClientProtocolProfile;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author mrTJO
 */
public class Ex2ndPasswordCheck implements IClientOutgoingPacket
{
	public static final int PASSWORD_NEW = 0x00;
	public static final int PASSWORD_PROMPT = 0x01;
	public static final int PASSWORD_OK = 0x02;

	private final int _windowType;
	private final ClientProtocolProfile _protocolProfile;

	public Ex2ndPasswordCheck(int windowType)
	{
		this(windowType, ClientProtocolProfile.HIGH_FIVE);
	}

	public Ex2ndPasswordCheck(int windowType, ClientProtocolProfile protocolProfile)
	{
		_windowType = windowType;
		_protocolProfile = protocolProfile;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		if (_protocolProfile == ClientProtocolProfile.SALVATION_140)
		{
			packet.writeC(0xFE);
			packet.writeH(0x105);
		}
		else
		{
			OutgoingPackets.EX_2ND_PASSWORD_CHECK.writeId(packet);
		}
		packet.writeD(_windowType);
		packet.writeD(0x00);
		return true;
	}
}
