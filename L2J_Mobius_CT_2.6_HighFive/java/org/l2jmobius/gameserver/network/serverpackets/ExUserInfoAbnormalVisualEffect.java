/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ExUserInfoAbnormalVisualEffect implements IClientOutgoingPacket
{
	private final PlayerInstance _player;

	public ExUserInfoAbnormalVisualEffect(PlayerInstance player)
	{
		_player = player;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_ABNORMAL_VISUAL_EFFECT.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeD(_player.getTransformationDisplayId());
		packet.writeD(0); // Initial bootstrap: no variable abnormal effect list yet.
		return true;
	}
}
