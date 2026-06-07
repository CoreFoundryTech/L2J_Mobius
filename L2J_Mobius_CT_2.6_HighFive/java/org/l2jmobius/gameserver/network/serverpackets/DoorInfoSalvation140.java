/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.DoorInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class DoorInfoSalvation140 implements IClientOutgoingPacket
{
	private final DoorInstance _door;
	
	public DoorInfoSalvation140(DoorInstance door)
	{
		_door = door;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.DOOR_INFO.writeId(packet);
		packet.writeD(_door.getObjectId());
		packet.writeD(_door.getId());
		return true;
	}
}
