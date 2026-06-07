/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ExUserInfoCubic implements IClientOutgoingPacket
{
	private final PlayerInstance _player;

	public ExUserInfoCubic(PlayerInstance player)
	{
		_player = player;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_CUBIC.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeH(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			packet.writeH(cubicId);
		}
		packet.writeD(_player.getAgathionId());
		return true;
	}
}
