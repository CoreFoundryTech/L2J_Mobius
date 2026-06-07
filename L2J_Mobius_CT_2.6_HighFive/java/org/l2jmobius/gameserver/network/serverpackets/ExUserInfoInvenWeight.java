/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ExUserInfoInvenWeight implements IClientOutgoingPacket
{
	private final PlayerInstance _player;

	public ExUserInfoInvenWeight(PlayerInstance player)
	{
		_player = player;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_INVEN_WEIGHT.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeD(_player.getCurrentLoad());
		packet.writeD(_player.getMaxLoad());
		return true;
	}
}
