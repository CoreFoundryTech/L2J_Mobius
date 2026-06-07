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

public class ExBasicActionListSalvation140 implements IClientOutgoingPacket
{
	public static final int[] DEFAULT_ACTION_LIST;
	public static final ExBasicActionListSalvation140 STATIC_PACKET;
	static
	{
		final int count1 = 91; // 0..90
		final int count2 = 156; // 1000..1155
		final int count3 = 17; // 5000..5016
		DEFAULT_ACTION_LIST = new int[count1 + count2 + count3];
		for (int i = 0; i < count1; i++)
		{
			DEFAULT_ACTION_LIST[i] = i;
		}
		for (int i = 0; i < count2; i++)
		{
			DEFAULT_ACTION_LIST[count1 + i] = 1000 + i;
		}
		for (int i = 0; i < count3; i++)
		{
			DEFAULT_ACTION_LIST[count1 + count2 + i] = 5000 + i;
		}
		STATIC_PACKET = new ExBasicActionListSalvation140(DEFAULT_ACTION_LIST);
	}

	private final int[] _actionIds;

	public ExBasicActionListSalvation140(int[] actionIds)
	{
		_actionIds = actionIds;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		packet.writeC(0xFE);
		packet.writeH(0x60);
		packet.writeD(_actionIds.length);
		for (int actionId : _actionIds)
		{
			packet.writeD(actionId);
		}
		return true;
	}
}
