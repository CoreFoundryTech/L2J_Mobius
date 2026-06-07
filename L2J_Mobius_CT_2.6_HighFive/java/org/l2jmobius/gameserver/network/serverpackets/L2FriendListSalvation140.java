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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/** Salvation 140 friend list format. */
public class L2FriendListSalvation140 implements IClientOutgoingPacket
{
	private final List<FriendInfo> _info;

	private static class FriendInfo
	{
		int _objId;
		String _name;
		boolean _online;
		int _level;
		int _classId;

		FriendInfo(int objId, String name, boolean online, int level, int classId)
		{
			_objId = objId;
			_name = name;
			_online = online;
			_level = level;
			_classId = classId;
		}
	}

	public L2FriendListSalvation140(PlayerInstance player)
	{
		_info = new ArrayList<>(player.getFriendList().size());
		for (int objId : player.getFriendList())
		{
			final String name = CharNameTable.getInstance().getNameById(objId);
			if (name == null)
			{
				continue;
			}

			final PlayerInstance friend = World.getInstance().getPlayer(objId);
			if ((friend != null) && friend.isOnline())
			{
				_info.add(new FriendInfo(objId, name, true, friend.getLevel(), friend.getClassId().getId()));
			}
			else
			{
				_info.add(new FriendInfo(objId, name, false, 0, 0));
			}
		}
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.L2_FRIEND_LIST.writeId(packet);
		packet.writeD(_info.size());
		for (FriendInfo info : _info)
		{
			packet.writeD(info._objId);
			packet.writeS(info._name);
			packet.writeD(info._online ? 1 : 0);
			packet.writeD(info._online ? info._objId : 0);
			packet.writeD(info._level);
			packet.writeD(info._classId);
			packet.writeH(0);
		}
		return true;
	}
}
