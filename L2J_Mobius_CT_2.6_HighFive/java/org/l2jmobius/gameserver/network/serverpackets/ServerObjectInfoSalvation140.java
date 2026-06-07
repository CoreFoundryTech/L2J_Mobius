/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ServerObjectInfoSalvation140 implements IClientOutgoingPacket
{
	private final Npc _npc;
	private final Creature _actor;
	
	public ServerObjectInfoSalvation140(Npc npc, Creature actor)
	{
		_npc = npc;
		_actor = actor;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SERVER_OBJECT_INFO.writeId(packet);
		packet.writeD(_npc.getObjectId());
		packet.writeD(_npc.getTemplate().getDisplayId() + 1000000);
		packet.writeS(_npc.getTemplate().isUsingServerSideName() ? _npc.getTemplate().getName() : "");
		packet.writeD(_npc.isAutoAttackable(_actor) ? 1 : 0);
		packet.writeD(_npc.getX());
		packet.writeD(_npc.getY());
		packet.writeD(_npc.getZ());
		packet.writeD(_npc.getHeading());
		packet.writeF(1.0);
		packet.writeF(1.0);
		packet.writeF(_npc.getCollisionRadius());
		packet.writeF(_npc.getCollisionHeight());
		packet.writeD(_npc.isAutoAttackable(_actor) ? (int) _npc.getCurrentHp() : 0);
		packet.writeD(_npc.isAutoAttackable(_actor) ? _npc.getMaxHp() : 0);
		packet.writeD(0x01);
		packet.writeD(0x00);
		return true;
	}
}
