/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ExUserInfoEquipSlot implements IClientOutgoingPacket
{
	private static final int SLOT_COUNT = 37;
	private static final byte[] FULL_MASK =
	{
		(byte) 0xFF,
		(byte) 0xFF,
		(byte) 0xFF,
		(byte) 0xFF,
		(byte) 0xF8
	};
	private static final int[] SALVATION_SLOTS =
	{
		Inventory.PAPERDOLL_UNDER,
		Inventory.PAPERDOLL_REAR,
		Inventory.PAPERDOLL_LEAR,
		Inventory.PAPERDOLL_NECK,
		Inventory.PAPERDOLL_RFINGER,
		Inventory.PAPERDOLL_LFINGER,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_LHAND,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_FEET,
		Inventory.PAPERDOLL_CLOAK,
		Inventory.PAPERDOLL_RHAND,
		Inventory.PAPERDOLL_HAIR,
		Inventory.PAPERDOLL_HAIR2,
		Inventory.PAPERDOLL_RBRACELET,
		Inventory.PAPERDOLL_LBRACELET,
		Inventory.PAPERDOLL_DECO1,
		Inventory.PAPERDOLL_DECO2,
		Inventory.PAPERDOLL_DECO3,
		Inventory.PAPERDOLL_DECO4,
		Inventory.PAPERDOLL_DECO5,
		Inventory.PAPERDOLL_DECO6,
		Inventory.PAPERDOLL_BELT,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1,
		-1
	};

	private final PlayerInstance _player;

	public ExUserInfoEquipSlot(PlayerInstance player)
	{
		_player = player;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_INFO_EQUIP_SLOT.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeH(SLOT_COUNT);
		packet.writeB(FULL_MASK);
		for (int slot : SALVATION_SLOTS)
		{
			writeSlot(packet, slot);
		}
		return true;
	}

	private void writeSlot(PacketWriter packet, int slot)
	{
		packet.writeH(22);
		if (slot < 0)
		{
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			packet.writeD(0);
			return;
		}

		final ItemInstance item = _player.getInventory().getPaperdollItem(slot);
		packet.writeD(item != null ? item.getObjectId() : 0);
		packet.writeD(item != null ? item.getId() : 0);
		packet.writeD(_player.getInventory().getPaperdollAugmentationId(slot));
		packet.writeD(0);
		packet.writeD(_player.getInventory().getPaperdollItemDisplayId(slot));
	}
}
