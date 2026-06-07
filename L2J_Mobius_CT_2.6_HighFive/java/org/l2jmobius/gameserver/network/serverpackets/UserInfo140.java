/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.impl.ExperienceData;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.TerritoryWarManager;
import org.l2jmobius.gameserver.model.Elementals;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class UserInfo140 implements IClientOutgoingPacket
{
	private static final byte[] MASKS =
	{
		(byte) 0xFF,
		(byte) 0xFF,
		(byte) 0xFE
	};

	private final PlayerInstance _player;
	private final String _name;
	private final String _title;
	private final int _relation;
	private final int _initSize;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;

	public UserInfo140(PlayerInstance player)
	{
		_player = player;
		_name = player.getAppearance().getVisibleName();
		_title = player.isGM() && player.isInvisible() ? "[Invisible]" : player.getTitle();
		_relation = calculateRelation(player);
		_initSize = 5 + 4 + (16 + (_name.length() * 2)) + 18 + 14 + 38 + 4 + 15 + 6 + 56 + 14 + 18 + 18 + 18 + 18 + 5 + (32 + (_title.length() * 2)) + 22 + 15 + 11 + 4 + 10 + 9 + 9;
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(player.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(player.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(player.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.USER_INFO.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeD(_initSize);
		packet.writeH(23);
		packet.writeB(MASKS);

		packet.writeD(_relation);

		packet.writeH(16 + (_name.length() * 2));
		packet.writeString(_name);
		packet.writeC(_player.isGM() ? 1 : 0);
		packet.writeC(_player.getRace().ordinal());
		packet.writeC(_player.getAppearance().getSex() ? 1 : 0);
		packet.writeD(_player.getBaseClass());
		packet.writeD(_player.getClassId().getId());
		packet.writeC(_player.getLevel());

		packet.writeH(18);
		packet.writeH(_player.getSTR());
		packet.writeH(_player.getDEX());
		packet.writeH(_player.getCON());
		packet.writeH(_player.getINT());
		packet.writeH(_player.getWIT());
		packet.writeH(_player.getMEN());
		packet.writeH(0);
		packet.writeH(0);

		packet.writeH(14);
		packet.writeD(_player.getMaxHp());
		packet.writeD(_player.getMaxMp());
		packet.writeD(_player.getMaxCp());

		packet.writeH(38);
		packet.writeD((int) Math.round(_player.getCurrentHp()));
		packet.writeD((int) Math.round(_player.getCurrentMp()));
		packet.writeD((int) _player.getCurrentCp());
		packet.writeQ((long) _player.getSp());
		packet.writeQ(_player.getExp());
		packet.writeF((float) (_player.getExp() - ExperienceData.getInstance().getExpForLevel(_player.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_player.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_player.getLevel())));

		packet.writeH(4);
		packet.writeC(_player.isMounted() ? 0 : _player.getEnchantEffect());
		packet.writeC(0);

		packet.writeH(15);
		packet.writeD(_player.getAppearance().getHairStyle());
		packet.writeD(_player.getAppearance().getHairColor());
		packet.writeD(_player.getAppearance().getFace());
		packet.writeC(1);

		packet.writeH(6);
		packet.writeC(_player.getMountType().ordinal());
		packet.writeC(_player.getPrivateStoreType().getId());
		packet.writeC(_player.hasDwarvenCraft() ? 1 : 0);
		packet.writeC(0);

		packet.writeH(56);
		packet.writeH(_player.getActiveWeaponItem() != null ? 40 : 20);
		packet.writeD((int) _player.getPAtk(null));
		packet.writeD((int) _player.getPAtkSpd());
		packet.writeD((int) _player.getPDef(null));
		packet.writeD(_player.getEvasionRate(null));
		packet.writeD(_player.getAccuracy());
		packet.writeD(_player.getCriticalHit(null, null));
		packet.writeD((int) _player.getMAtk(null, null));
		packet.writeD(_player.getMAtkSpd());
		packet.writeD((int) _player.getPAtkSpd());
		packet.writeD(0);
		packet.writeD((int) _player.getMDef(null, null));
		packet.writeD(0);
		packet.writeD(0);

		packet.writeH(14);
		packet.writeH(_player.getDefenseElementValue(Elementals.FIRE));
		packet.writeH(_player.getDefenseElementValue(Elementals.WATER));
		packet.writeH(_player.getDefenseElementValue(Elementals.WIND));
		packet.writeH(_player.getDefenseElementValue(Elementals.EARTH));
		packet.writeH(_player.getDefenseElementValue(Elementals.HOLY));
		packet.writeH(_player.getDefenseElementValue(Elementals.DARK));

		packet.writeH(18);
		packet.writeD(_player.getX());
		packet.writeD(_player.getY());
		packet.writeD(_player.getZ());
		packet.writeD(_player.getVehicle() != null ? _player.getVehicle().getObjectId() : 0);

		packet.writeH(18);
		packet.writeH(_runSpd);
		packet.writeH(_walkSpd);
		packet.writeH(_swimRunSpd);
		packet.writeH(_swimWalkSpd);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(_flyRunSpd);
		packet.writeH(_flyWalkSpd);

		packet.writeH(18);
		packet.writeF(_moveMultiplier);
		packet.writeF(_player.getAttackSpeedMultiplier());

		packet.writeH(18);
		packet.writeF(_player.getCollisionRadius());
		packet.writeF(_player.getCollisionHeight());

		packet.writeH(5);
		final byte attackAttribute = _player.getAttackElement();
		packet.writeC(attackAttribute);
		packet.writeH(_player.getAttackElementValue(attackAttribute));

		packet.writeH(32 + (_title.length() * 2));
		packet.writeString(_title);
		packet.writeH(_player.getPledgeType());
		packet.writeD(_player.getClanId());
		packet.writeD(_player.getClanCrestLargeId());
		packet.writeD(_player.getClanCrestId());
		packet.writeD(_player.getClanPrivileges().getBitmask());
		packet.writeC(_player.isClanLeader() ? 1 : 0);
		packet.writeD(_player.getAllyId());
		packet.writeD(_player.getAllyCrestId());
		packet.writeC(_player.isInPartyMatchRoom() ? 1 : 0);

		packet.writeH(22);
		packet.writeC(_player.getPvpFlag());
		packet.writeD(_player.getKarma());
		packet.writeC(_player.isNoble() ? 1 : 0);
		packet.writeC(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
		packet.writeC(_player.getPledgeClass());
		packet.writeD(_player.getPkKills());
		packet.writeD(_player.getPvpKills());
		packet.writeH(_player.getRecomLeft());
		packet.writeH(_player.getRecomHave());

		packet.writeH(15);
		packet.writeD(_player.getVitalityPoints());
		packet.writeC(0);
		packet.writeD(_player.getFame());
		packet.writeD(0);

		packet.writeH(11);
		packet.writeC(_player.getInventory().getTalismanSlots());
		packet.writeC(0);
		packet.writeC(_player.getTeam().getId());
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);

		packet.writeH(4);
		packet.writeC(_player.isInsideZone(ZoneId.WATER) ? 1 : _player.isFlying() ? 2 : 0);
		packet.writeC(_player.isRunning() ? 1 : 0);

		packet.writeH(10);
		packet.writeD(_player.getAppearance().getNameColor());
		packet.writeD(_player.getAppearance().getTitleColor());

		packet.writeH(9);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(_player.getInventoryLimit());
		packet.writeC(0);

		packet.writeH(9);
		packet.writeD(_player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquippedId()) : 0);
		packet.writeH(0);
		packet.writeC(0);
		return true;
	}

	private static int calculateRelation(PlayerInstance player)
	{
		int relation = player.isClanLeader() ? 0x40 : 0;
		final int territoryId = TerritoryWarManager.getInstance().getRegisteredTerritoryId(player);
		if (player.getSiegeState() == 1)
		{
			relation |= territoryId == 0 ? 0x180 : 0x1000;
		}
		else if (player.getSiegeState() == 2)
		{
			relation |= 0x80;
		}
		return relation;
	}
}
