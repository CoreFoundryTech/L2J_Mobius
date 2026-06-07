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

public class ExRotationSalvation140 implements IClientOutgoingPacket
{
	private final int _charId;
	private final int _heading;
	
	public ExRotationSalvation140(int charId, int heading)
	{
		_charId = charId;
		_heading = heading;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0xFE);
		packet.writeH(0xC2);
		packet.writeD(_charId);
		packet.writeD(_heading);
		return true;
	}
}
