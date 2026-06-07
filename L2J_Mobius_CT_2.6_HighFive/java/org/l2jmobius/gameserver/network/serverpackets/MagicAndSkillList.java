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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Salvation 140 post-enter-world magic and skill bootstrap packet.
 */
public class MagicAndSkillList implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _unknown1;
	private final int _unknown2;

	public MagicAndSkillList(Creature creature, int unknown1, int unknown2)
	{
		_objectId = creature.getObjectId();
		_unknown1 = unknown1;
		_unknown2 = unknown2;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MAGIC_AND_SKILL_LIST.writeId(packet);
		packet.writeD(_objectId);
		packet.writeD(_unknown1);
		packet.writeD(_unknown2);
		return true;
	}
}
