/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.Team;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class SummonInfoSalvation140 implements IClientOutgoingPacket
{
	private enum NpcInfoType
	{
		ID(0x00, 4),
		ATTACKABLE(0x01, 1),
		UNKNOWN1(0x02, 4),
		NAME(0x03, 2),
		POSITION(0x04, 12),
		HEADING(0x05, 4),
		UNKNOWN2(0x06, 4),
		ATK_CAST_SPEED(0x07, 8),
		SPEED_MULTIPLIER(0x08, 8),
		EQUIPPED(0x09, 12),
		ALIVE(0x0A, 1),
		RUNNING(0x0B, 1),
		SWIM_OR_FLY(0x0E, 1),
		TEAM(0x0F, 1),
		ENCHANT(0x10, 4),
		FLYING(0x11, 4),
		CLONE(0x12, 4),
		COLOR_EFFECT(0x13, 4),
		DISPLAY_EFFECT(0x16, 4),
		TRANSFORMATION(0x17, 4),
		CURRENT_HP(0x18, 4),
		CURRENT_MP(0x19, 4),
		MAX_HP(0x1A, 4),
		MAX_MP(0x1B, 4),
		SUMMONED(0x1C, 1),
		UNKNOWN12(0x1D, 8),
		TITLE(0x1E, 2),
		NAME_NPCSTRINGID(0x1F, 4),
		TITLE_NPCSTRINGID(0x20, 4),
		PVP_FLAG(0x21, 1),
		REPUTATION(0x22, 4),
		CLAN(0x23, 20),
		ABNORMALS(0x24, 0),
		VISUAL_STATE(0x25, 1);

		private final int _mask;
		private final int _blockLength;

		NpcInfoType(int mask, int blockLength)
		{
			_mask = mask;
			_blockLength = blockLength;
		}
	}

	private static final byte[] DEFAULT_FLAG_ARRAY =
	{
		(byte) 0x80,
		0x40,
		0x20,
		0x10,
		0x08,
		0x04,
		0x02,
		0x01
	};

	private final Summon _summon;
	private final Creature _attacker;
	private final int _val;
	private final byte[] _masks =
	{
		(byte) 0x00,
		(byte) 0x0C,
		(byte) 0x0C,
		(byte) 0x00,
		(byte) 0x00
	};
	private int _initSize;
	private int _blockSize;
	private int _statusMask;
	private final String _title;

	public SummonInfoSalvation140(Summon summon, Creature attacker, int val)
	{
		_summon = summon;
		_attacker = attacker;
		_val = val;
		_title = (summon.getOwner() != null) && summon.getOwner().isOnline() ? summon.getOwner().getName() : "";

		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.TITLE, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING, NpcInfoType.PVP_FLAG);
		if (summon.getHeading() > 0)
		{
			addComponentType(NpcInfoType.HEADING);
		}
		if ((summon.getPAtkSpd() > 0) || (summon.getMAtkSpd() > 0))
		{
			addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}
		if (summon.getRunSpeed() > 0)
		{
			addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}
		if ((summon.getWeapon() > 0) || (summon.getArmor() > 0))
		{
			addComponentType(NpcInfoType.EQUIPPED);
		}
		if (summon.getTeam() != Team.NONE)
		{
			addComponentType(NpcInfoType.TEAM);
		}
		if (summon.isInsideZone(ZoneId.WATER) || summon.isFlying())
		{
			addComponentType(NpcInfoType.SWIM_OR_FLY);
		}
		if (summon.isFlying())
		{
			addComponentType(NpcInfoType.FLYING);
		}
		if (summon.getMaxHp() > 0)
		{
			addComponentType(NpcInfoType.MAX_HP);
		}
		if (summon.getMaxMp() > 0)
		{
			addComponentType(NpcInfoType.MAX_MP);
		}
		if (summon.getCurrentHp() <= summon.getMaxHp())
		{
			addComponentType(NpcInfoType.CURRENT_HP);
		}
		if (summon.getCurrentMp() <= summon.getMaxMp())
		{
			addComponentType(NpcInfoType.CURRENT_MP);
		}
		if (summon.getTemplate().getWeaponEnchant() > 0)
		{
			addComponentType(NpcInfoType.ENCHANT);
		}
		if (summon.getFormId() > 0)
		{
			addComponentType(NpcInfoType.TRANSFORMATION);
		}
		if (summon.isShowSummonAnimation() || (val == 2))
		{
			addComponentType(NpcInfoType.SUMMONED);
		}
		addComponentType(NpcInfoType.COLOR_EFFECT);

		if (summon.isInCombat())
		{
			_statusMask |= 0x01;
		}
		if (summon.isDead())
		{
			_statusMask |= 0x02;
		}
		if (summon.isTargetable())
		{
			_statusMask |= 0x04;
		}
		_statusMask |= 0x08;
		addComponentType(NpcInfoType.VISUAL_STATE);
	}

	private void addComponentType(NpcInfoType... types)
	{
		for (NpcInfoType type : types)
		{
			if (!containsMask(type))
			{
				_masks[type._mask >> 3] |= DEFAULT_FLAG_ARRAY[type._mask & 7];
				calcBlockSize(type);
			}
		}
	}

	private boolean containsMask(NpcInfoType type)
	{
		return (_masks[type._mask >> 3] & DEFAULT_FLAG_ARRAY[type._mask & 7]) != 0;
	}

	private void calcBlockSize(NpcInfoType type)
	{
		switch (type)
		{
			case ATTACKABLE:
			case UNKNOWN1:
			{
				_initSize += type._blockLength;
				break;
			}
			case TITLE:
			{
				_initSize += type._blockLength + (_title.length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += type._blockLength + (_summon.getName().length() * 2);
				break;
			}
			default:
			{
				_blockSize += type._blockLength;
				break;
			}
		}
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SUMMON_INFO.writeId(packet);
		packet.writeD(_summon.getObjectId());
		packet.writeC(_val);
		packet.writeH(37);
		packet.writeB(_masks);

		packet.writeC(_initSize);
		if (containsMask(NpcInfoType.ATTACKABLE))
		{
			packet.writeC(_summon.isAutoAttackable(_attacker) ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.UNKNOWN1))
		{
			packet.writeD(0x00);
		}
		if (containsMask(NpcInfoType.TITLE))
		{
			packet.writeS(_title);
		}

		packet.writeH(_blockSize);
		if (containsMask(NpcInfoType.ID))
		{
			packet.writeD(_summon.getTemplate().getDisplayId() + 1000000);
		}
		if (containsMask(NpcInfoType.POSITION))
		{
			packet.writeD(_summon.getX());
			packet.writeD(_summon.getY());
			packet.writeD(_summon.getZ());
		}
		if (containsMask(NpcInfoType.HEADING))
		{
			packet.writeD(_summon.getHeading());
		}
		if (containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			packet.writeD((int) _summon.getPAtkSpd());
			packet.writeD(_summon.getMAtkSpd());
		}
		if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			packet.writeE((float) _summon.getMovementSpeedMultiplier());
			packet.writeE((float) _summon.getAttackSpeedMultiplier());
		}
		if (containsMask(NpcInfoType.EQUIPPED))
		{
			packet.writeD(_summon.getWeapon());
			packet.writeD(_summon.getArmor());
			packet.writeD(0x00);
		}
		if (containsMask(NpcInfoType.ALIVE))
		{
			packet.writeC(_summon.isDead() ? 0x00 : 0x01);
		}
		if (containsMask(NpcInfoType.RUNNING))
		{
			packet.writeC(_summon.isRunning() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.SWIM_OR_FLY))
		{
			packet.writeC(_summon.isInsideZone(ZoneId.WATER) ? 0x01 : _summon.isFlying() ? 0x02 : 0x00);
		}
		if (containsMask(NpcInfoType.TEAM))
		{
			packet.writeC(_summon.getTeam().getId());
		}
		if (containsMask(NpcInfoType.ENCHANT))
		{
			packet.writeD(_summon.getTemplate().getWeaponEnchant());
		}
		if (containsMask(NpcInfoType.FLYING))
		{
			packet.writeD(_summon.isFlying() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.COLOR_EFFECT))
		{
			packet.writeD(0x00);
		}
		if (containsMask(NpcInfoType.TRANSFORMATION))
		{
			packet.writeD(_summon.getFormId());
		}
		if (containsMask(NpcInfoType.CURRENT_HP))
		{
			packet.writeD((int) _summon.getCurrentHp());
		}
		if (containsMask(NpcInfoType.CURRENT_MP))
		{
			packet.writeD((int) _summon.getCurrentMp());
		}
		if (containsMask(NpcInfoType.MAX_HP))
		{
			packet.writeD(_summon.getMaxHp());
		}
		if (containsMask(NpcInfoType.MAX_MP))
		{
			packet.writeD(_summon.getMaxMp());
		}
		if (containsMask(NpcInfoType.SUMMONED))
		{
			packet.writeC(_summon.isShowSummonAnimation() || (_val == 2) ? 0x02 : 0x00);
		}
		if (containsMask(NpcInfoType.NAME))
		{
			packet.writeS(_summon.getName());
		}
		if (containsMask(NpcInfoType.PVP_FLAG))
		{
			packet.writeC(_summon.getPvpFlag());
		}
		if (containsMask(NpcInfoType.VISUAL_STATE))
		{
			packet.writeC(_statusMask);
		}
		return true;
	}
}
