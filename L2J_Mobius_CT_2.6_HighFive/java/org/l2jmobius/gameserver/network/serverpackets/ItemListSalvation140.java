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
import org.l2jmobius.gameserver.model.ItemInfo;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class ItemListSalvation140 extends AbstractItemPacket
{
	private final int _sendType;
	private final PlayerInstance _player;
	private final List<ItemInstance> _items = new ArrayList<>();

	public ItemListSalvation140(int sendType, PlayerInstance player)
	{
		_sendType = sendType;
		_player = player;
		for (ItemInstance item : player.getInventory().getItems())
		{
			if (!item.isQuestItem())
			{
				_items.add(item);
			}
		}
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ITEM_LIST.writeId(packet);
		if (_sendType == 2)
		{
			packet.writeC(_sendType);
			packet.writeD(_items.size());
			packet.writeD(_items.size());
			for (ItemInstance item : _items)
			{
				writeItemSalvation140(packet, item);
			}
		}
		else
		{
			packet.writeC(0x01);
			packet.writeD(0x00);
			packet.writeD(_items.size());
		}
		writeInventoryBlock(packet, _player.getInventory());
		return true;
	}

	static void writeItemSalvation140(PacketWriter packet, ItemInstance item)
	{
		writeItemSalvation140(packet, new ItemInfo(item));
	}

	static void writeItemSalvation140(PacketWriter packet, ItemInfo item)
	{
		packet.writeC(0x00); // Optional field mask. Keep zero for H5-backed minimal rows.
		packet.writeD(item.getObjectId());
		packet.writeD(item.getItem().getDisplayId());
		packet.writeC(item.getItem().isQuestItem() || (item.getEquipped() == 1) ? 0xFF : item.getLocation());
		packet.writeQ(item.getCount());
		packet.writeC(item.getItem().getType2());
		packet.writeC(item.getCustomType1());
		packet.writeH(item.getEquipped());
		packet.writeQ(item.getItem().getBodyPart());
		packet.writeC(item.getEnchant());
		packet.writeC(0x01);
		packet.writeD(item.getMana());
		packet.writeD(item.getTime());
		packet.writeC(0x01); // Enabled.
		packet.writeC(0x00); // Protocol 140.
		packet.writeC(0x00); // Protocol 140.
	}
}
