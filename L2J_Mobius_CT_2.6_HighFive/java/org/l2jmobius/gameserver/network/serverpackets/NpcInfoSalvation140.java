/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.impl.NpcNameLocalisationData;
import org.l2jmobius.gameserver.enums.Team;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.GuardInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class NpcInfoSalvation140 implements IClientOutgoingPacket
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
	
	private final Npc _npc;
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
	private String[] _localisation;
	
	public NpcInfoSalvation140(Npc npc)
	{
		_npc = npc;
		addComponentType(NpcInfoType.ATTACKABLE, NpcInfoType.UNKNOWN1, NpcInfoType.ID, NpcInfoType.POSITION, NpcInfoType.ALIVE, NpcInfoType.RUNNING);
		
		if (npc.getHeading() > 0)
		{
			addComponentType(NpcInfoType.HEADING);
		}
		if ((npc.getPAtkSpd() > 0) || (npc.getMAtkSpd() > 0))
		{
			addComponentType(NpcInfoType.ATK_CAST_SPEED);
		}
		if (npc.getRunSpeed() > 0)
		{
			addComponentType(NpcInfoType.SPEED_MULTIPLIER);
		}
		if ((npc.getLeftHandItem() > 0) || (npc.getRightHandItem() > 0))
		{
			addComponentType(NpcInfoType.EQUIPPED);
		}
		if (npc.getTeam() != Team.NONE)
		{
			addComponentType(NpcInfoType.TEAM);
		}
		if (npc.getDisplayEffect() > 0)
		{
			addComponentType(NpcInfoType.DISPLAY_EFFECT);
		}
		if (npc.isInsideZone(ZoneId.WATER) || npc.isFlying())
		{
			addComponentType(NpcInfoType.SWIM_OR_FLY);
		}
		if (npc.isFlying())
		{
			addComponentType(NpcInfoType.FLYING);
		}
		if (npc.getMaxHp() > 0)
		{
			addComponentType(NpcInfoType.MAX_HP);
		}
		if (npc.getMaxMp() > 0)
		{
			addComponentType(NpcInfoType.MAX_MP);
		}
		if (npc.getCurrentHp() <= npc.getMaxHp())
		{
			addComponentType(NpcInfoType.CURRENT_HP);
		}
		if (npc.getCurrentMp() <= npc.getMaxMp())
		{
			addComponentType(NpcInfoType.CURRENT_MP);
		}
		if (npc.getTemplate().isUsingServerSideName())
		{
			addComponentType(NpcInfoType.NAME);
		}
		if (npc.getTemplate().isUsingServerSideTitle() || (Config.SHOW_NPC_LVL && npc.isMonster()) || npc.isChampion() || npc.isTrap())
		{
			addComponentType(NpcInfoType.TITLE);
		}
		if (npc.getEnchantEffect() > 0)
		{
			addComponentType(NpcInfoType.ENCHANT);
		}
		if (npc.isShowSummonAnimation())
		{
			addComponentType(NpcInfoType.SUMMONED);
		}
		addComponentType(NpcInfoType.COLOR_EFFECT);
		
		if (npc.isInCombat())
		{
			_statusMask |= 0x01;
		}
		if (npc.isDead())
		{
			_statusMask |= 0x02;
		}
		if (npc.isTargetable())
		{
			_statusMask |= 0x04;
		}
		if (npc.isShowName())
		{
			_statusMask |= 0x08;
		}
		if (_statusMask != 0)
		{
			addComponentType(NpcInfoType.VISUAL_STATE);
		}
	}
	
	public void setLang(String lang)
	{
		_localisation = NpcNameLocalisationData.getInstance().getLocalisation(lang, _npc.getId());
		if (_localisation != null)
		{
			if (containsMask(NpcInfoType.NAME))
			{
				_blockSize += (_localisation[0].length() - _npc.getName().length()) * 2;
			}
			if (containsMask(NpcInfoType.TITLE))
			{
				_localisation = null;
				final int oldLength = getTitle().length();
				_localisation = NpcNameLocalisationData.getInstance().getLocalisation(lang, _npc.getId());
				_initSize += (getTitle().length() - oldLength) * 2;
			}
		}
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
				_initSize += type._blockLength + (getTitle().length() * 2);
				break;
			}
			case NAME:
			{
				_blockSize += type._blockLength + (_npc.getName().length() * 2);
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
		OutgoingPackets.NPC_INFO.writeId(packet);
		packet.writeD(_npc.getObjectId());
		packet.writeC(_npc.isShowSummonAnimation() ? 0x02 : 0x00);
		packet.writeH(37);
		packet.writeB(_masks);
		
		packet.writeC(_initSize);
		if (containsMask(NpcInfoType.ATTACKABLE))
		{
			packet.writeC(_npc.isAttackable() && !(_npc instanceof GuardInstance) ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.UNKNOWN1))
		{
			packet.writeD(0x00);
		}
		if (containsMask(NpcInfoType.TITLE))
		{
			packet.writeS(getTitle());
		}
		
		packet.writeH(_blockSize);
		if (containsMask(NpcInfoType.ID))
		{
			packet.writeD(_npc.getTemplate().getDisplayId() + 1000000);
		}
		if (containsMask(NpcInfoType.POSITION))
		{
			packet.writeD(_npc.getX());
			packet.writeD(_npc.getY());
			packet.writeD(_npc.getZ());
		}
		if (containsMask(NpcInfoType.HEADING))
		{
			packet.writeD(_npc.getHeading());
		}
		if (containsMask(NpcInfoType.ATK_CAST_SPEED))
		{
			packet.writeD((int) _npc.getPAtkSpd());
			packet.writeD(_npc.getMAtkSpd());
		}
		if (containsMask(NpcInfoType.SPEED_MULTIPLIER))
		{
			packet.writeE((float) _npc.getMovementSpeedMultiplier());
			packet.writeE((float) _npc.getAttackSpeedMultiplier());
		}
		if (containsMask(NpcInfoType.EQUIPPED))
		{
			packet.writeD(_npc.getRightHandItem());
			packet.writeD(0x00);
			packet.writeD(_npc.getLeftHandItem());
		}
		if (containsMask(NpcInfoType.ALIVE))
		{
			packet.writeC(_npc.isDead() ? 0x00 : 0x01);
		}
		if (containsMask(NpcInfoType.RUNNING))
		{
			packet.writeC(_npc.isRunning() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.SWIM_OR_FLY))
		{
			packet.writeC(_npc.isInsideZone(ZoneId.WATER) ? 0x01 : _npc.isFlying() ? 0x02 : 0x00);
		}
		if (containsMask(NpcInfoType.TEAM))
		{
			packet.writeC(_npc.getTeam().getId());
		}
		if (containsMask(NpcInfoType.ENCHANT))
		{
			packet.writeD(_npc.getEnchantEffect());
		}
		if (containsMask(NpcInfoType.FLYING))
		{
			packet.writeD(_npc.isFlying() ? 0x01 : 0x00);
		}
		if (containsMask(NpcInfoType.COLOR_EFFECT))
		{
			packet.writeD(_npc.getColorEffect());
		}
		if (containsMask(NpcInfoType.DISPLAY_EFFECT))
		{
			packet.writeD(_npc.getDisplayEffect());
		}
		if (containsMask(NpcInfoType.CURRENT_HP))
		{
			packet.writeD((int) _npc.getCurrentHp());
		}
		if (containsMask(NpcInfoType.CURRENT_MP))
		{
			packet.writeD((int) _npc.getCurrentMp());
		}
		if (containsMask(NpcInfoType.MAX_HP))
		{
			packet.writeD(_npc.getMaxHp());
		}
		if (containsMask(NpcInfoType.MAX_MP))
		{
			packet.writeD(_npc.getMaxMp());
		}
		if (containsMask(NpcInfoType.SUMMONED))
		{
			packet.writeC(0x00);
		}
		if (containsMask(NpcInfoType.NAME))
		{
			packet.writeS((_localisation != null) ? _localisation[0] : _npc.getName());
		}
		if (containsMask(NpcInfoType.VISUAL_STATE))
		{
			packet.writeC(_statusMask);
		}
		return true;
	}
	
	private String getTitle()
	{
		if (_npc.isInvisible())
		{
			return "Invisible";
		}
		if (Config.CHAMPION_ENABLE && _npc.isChampion())
		{
			return Config.CHAMP_TITLE;
		}
		if ((_localisation != null) && !_localisation[1].isEmpty())
		{
			return _localisation[1];
		}
		if (Config.SHOW_NPC_LVL && _npc.isMonster())
		{
			return "Lv " + _npc.getLevel() + (_npc.isAggressive() ? "*" : "") + " " + _npc.getTitle();
		}
		return _npc.getTitle();
	}
}
