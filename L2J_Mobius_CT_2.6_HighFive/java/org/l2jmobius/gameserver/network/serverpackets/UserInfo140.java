/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
	private static final Logger LOGGER = Logger.getLogger(UserInfo140.class.getName());
	// TEMP diagnostics for Salvation 140 client crash in CachingUserInfoData::decodeStream.
	private static final String DEBUG_PREFIX = "SALVATION140 UserInfo140 DEBUG";
	private static final Set<Integer> DEBUGGED_OBJECT_IDS = ConcurrentHashMap.newKeySet();

	private static final int BLOCK_COUNT = 24;
	private static final int RELATION_LENGTH = 4;
	private static final int BASIC_INFO_LENGTH = 16;
	private static final int BASE_STATS_LENGTH = 18;
	private static final int MAX_HPCPMP_LENGTH = 14;
	private static final int CURRENT_HPMPCP_EXP_SP_LENGTH = 38;
	private static final int ENCHANTLEVEL_LENGTH = 4;
	private static final int APPAREANCE_LENGTH = 15;
	private static final int STATUS_LENGTH = 6;
	private static final int STATS_LENGTH = 56;
	private static final int ELEMENTALS_LENGTH = 14;
	private static final int POSITION_LENGTH = 18;
	private static final int SPEED_LENGTH = 18;
	private static final int MULTIPLIER_LENGTH = 18;
	private static final int COL_RADIUS_HEIGHT_LENGTH = 18;
	private static final int ATK_ELEMENTAL_LENGTH = 5;
	private static final int CLAN_LENGTH = 32;
	private static final int SOCIAL_LENGTH = 22;
	private static final int VITA_FAME_LENGTH = 15;
	private static final int SLOTS_LENGTH = 11;
	private static final int MOVEMENTS_LENGTH = 4;
	private static final int COLOR_LENGTH = 10;
	private static final int INVENTORY_LIMIT_LENGTH = 9;
	private static final int TRUE_HERO_LENGTH = 9;

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
		_initSize = 5 + RELATION_LENGTH + (BASIC_INFO_LENGTH + (_name.length() * 2)) + BASE_STATS_LENGTH + MAX_HPCPMP_LENGTH + CURRENT_HPMPCP_EXP_SP_LENGTH + ENCHANTLEVEL_LENGTH + APPAREANCE_LENGTH + STATUS_LENGTH + STATS_LENGTH + ELEMENTALS_LENGTH + POSITION_LENGTH + SPEED_LENGTH + MULTIPLIER_LENGTH + COL_RADIUS_HEIGHT_LENGTH + ATK_ELEMENTAL_LENGTH + (CLAN_LENGTH + (_title.length() * 2)) + SOCIAL_LENGTH + VITA_FAME_LENGTH + SLOTS_LENGTH + MOVEMENTS_LENGTH + COLOR_LENGTH + INVENTORY_LIMIT_LENGTH + TRUE_HERO_LENGTH;
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
		final int packetStart = packet.getPosition();
		final boolean debugThisPacket = DEBUGGED_OBJECT_IDS.add(_player.getObjectId());

		OutgoingPackets.USER_INFO.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeD(_initSize);
		packet.writeH(BLOCK_COUNT);
		packet.writeB(MASKS);
		if (debugThisPacket)
		{
			logDebug("header objectId=" + _player.getObjectId() + ", initSize=" + _initSize + ", blockCount=" + BLOCK_COUNT + ", masks=" + toHex(MASKS));
		}

		int blockStart = packet.getPosition();
		packet.writeD(_relation);
		logBlock("relation", RELATION_LENGTH, blockStart, blockStart, packet.getPosition(), false, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(BASIC_INFO_LENGTH + (_name.length() * 2));
		int payloadStart = packet.getPosition();
		packet.writeString(_name);
		packet.writeC(_player.isGM() ? 1 : 0);
		packet.writeC(_player.getRace().ordinal());
		packet.writeC(_player.getAppearance().getSex() ? 1 : 0);
		packet.writeD(_player.getBaseClass());
		packet.writeD(_player.getClassId().getId());
		packet.writeC(_player.getLevel());
		logBlock("basic_info", BASIC_INFO_LENGTH + (_name.length() * 2), blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(BASE_STATS_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeH(_player.getSTR());
		packet.writeH(_player.getDEX());
		packet.writeH(_player.getCON());
		packet.writeH(_player.getINT());
		packet.writeH(_player.getWIT());
		packet.writeH(_player.getMEN());
		packet.writeH(0);
		packet.writeH(0);
		logBlock("base_stats", BASE_STATS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(MAX_HPCPMP_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(_player.getMaxHp());
		packet.writeD(_player.getMaxMp());
		packet.writeD(_player.getMaxCp());
		logBlock("max_hpcpmp", MAX_HPCPMP_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(CURRENT_HPMPCP_EXP_SP_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD((int) Math.round(_player.getCurrentHp()));
		packet.writeD((int) Math.round(_player.getCurrentMp()));
		packet.writeD((int) _player.getCurrentCp());
		packet.writeQ((long) _player.getSp());
		packet.writeQ(_player.getExp());
		packet.writeF((float) (_player.getExp() - ExperienceData.getInstance().getExpForLevel(_player.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_player.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_player.getLevel())));
		logBlock("current_hpmpcp_exp_sp", CURRENT_HPMPCP_EXP_SP_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(ENCHANTLEVEL_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeC(_player.isMounted() ? 0 : _player.getEnchantEffect());
		packet.writeC(0);
		logBlock("enchant_level", ENCHANTLEVEL_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(APPAREANCE_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(_player.getAppearance().getHairStyle());
		packet.writeD(_player.getAppearance().getHairColor());
		packet.writeD(_player.getAppearance().getFace());
		packet.writeC(1);
		logBlock("appearance", APPAREANCE_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(STATUS_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeC(_player.getMountType().ordinal());
		packet.writeC(_player.getPrivateStoreType().getId());
		packet.writeC(_player.hasDwarvenCraft() ? 1 : 0);
		packet.writeC(0);
		logBlock("status", STATUS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(STATS_LENGTH);
		payloadStart = packet.getPosition();
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
		logBlock("stats", STATS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(ELEMENTALS_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeH(_player.getDefenseElementValue(Elementals.FIRE));
		packet.writeH(_player.getDefenseElementValue(Elementals.WATER));
		packet.writeH(_player.getDefenseElementValue(Elementals.WIND));
		packet.writeH(_player.getDefenseElementValue(Elementals.EARTH));
		packet.writeH(_player.getDefenseElementValue(Elementals.HOLY));
		packet.writeH(_player.getDefenseElementValue(Elementals.DARK));
		logBlock("elementals", ELEMENTALS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(POSITION_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(_player.getX());
		packet.writeD(_player.getY());
		packet.writeD(_player.getZ());
		packet.writeD(_player.getVehicle() != null ? _player.getVehicle().getObjectId() : 0);
		logBlock("position", POSITION_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(SPEED_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeH(_runSpd);
		packet.writeH(_walkSpd);
		packet.writeH(_swimRunSpd);
		packet.writeH(_swimWalkSpd);
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(_flyRunSpd);
		packet.writeH(_flyWalkSpd);
		logBlock("speed", SPEED_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(MULTIPLIER_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeF(_moveMultiplier);
		packet.writeF(_player.getAttackSpeedMultiplier());
		logBlock("multiplier", MULTIPLIER_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(COL_RADIUS_HEIGHT_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeF(_player.getCollisionRadius());
		packet.writeF(_player.getCollisionHeight());
		logBlock("collision_radius_height", COL_RADIUS_HEIGHT_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(ATK_ELEMENTAL_LENGTH);
		payloadStart = packet.getPosition();
		final byte attackAttribute = _player.getAttackElement();
		packet.writeC(attackAttribute);
		packet.writeH(_player.getAttackElementValue(attackAttribute));
		logBlock("attack_elemental", ATK_ELEMENTAL_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(CLAN_LENGTH + (_title.length() * 2));
		payloadStart = packet.getPosition();
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
		logBlock("clan", CLAN_LENGTH + (_title.length() * 2), blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(SOCIAL_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeC(_player.getPvpFlag());
		packet.writeD(_player.getKarma());
		packet.writeC(_player.isNoble() ? 1 : 0);
		packet.writeC(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
		packet.writeC(_player.getPledgeClass());
		packet.writeD(_player.getPkKills());
		packet.writeD(_player.getPvpKills());
		packet.writeH(_player.getRecomLeft());
		packet.writeH(_player.getRecomHave());
		logBlock("social", SOCIAL_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(VITA_FAME_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(_player.getVitalityPoints());
		packet.writeC(0);
		packet.writeD(_player.getFame());
		packet.writeD(0);
		logBlock("vitality_fame", VITA_FAME_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		// Salvation 140 writes a total block size of 11 here: H(length) + 9 C payload bytes.
		blockStart = packet.getPosition();
		packet.writeH(SLOTS_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeC(_player.getInventory().getTalismanSlots());
		packet.writeC(0);
		packet.writeC(_player.getTeam().getId());
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		packet.writeC(0);
		logBlock("slots", SLOTS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(MOVEMENTS_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeC(_player.isInsideZone(ZoneId.WATER) ? 1 : _player.isFlying() ? 2 : 0);
		packet.writeC(_player.isRunning() ? 1 : 0);
		logBlock("movements", MOVEMENTS_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(COLOR_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(_player.getAppearance().getNameColor());
		packet.writeD(_player.getAppearance().getTitleColor());
		logBlock("color", COLOR_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(INVENTORY_LIMIT_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeH(0);
		packet.writeH(0);
		packet.writeH(_player.getInventoryLimit());
		packet.writeC(_player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquippedId()) : 0);
		logBlock("inventory_limit", INVENTORY_LIMIT_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		blockStart = packet.getPosition();
		packet.writeH(TRUE_HERO_LENGTH);
		payloadStart = packet.getPosition();
		packet.writeD(0);
		packet.writeH(0);
		packet.writeC(0);
		logBlock("true_hero", TRUE_HERO_LENGTH, blockStart, payloadStart, packet.getPosition(), true, debugThisPacket);

		if (debugThisPacket)
		{
			final int packetEnd = packet.getPosition();
			logDebug("packetBytes=" + (packetEnd - packetStart) + ", hex=" + toHex(packet.getBytes(packetStart, packetEnd - packetStart)));
		}
		return true;
	}

	private static void logBlock(String name, int declaredLength, int blockStart, int payloadStart, int blockEnd, boolean lengthIncludesField, boolean debugThisPacket)
	{
		final int expectedPayloadBytes = lengthIncludesField ? declaredLength - 2 : declaredLength;
		final int actualPayloadBytes = blockEnd - payloadStart;
		final String message = "block=" + name + ", declaredLen=" + declaredLength + ", expectedPayloadBytes=" + expectedPayloadBytes + ", actualPayloadBytes=" + actualPayloadBytes + ", startOffset=" + blockStart + ", endOffset=" + blockEnd;
		if (debugThisPacket)
		{
			logDebug(message);
		}
		if (actualPayloadBytes != expectedPayloadBytes)
		{
			logWarning("WARNING " + message);
		}
	}

	private static void logDebug(String message)
	{
		final String log = DEBUG_PREFIX + " " + message;
		LOGGER.info(log);
		System.out.println(log);
	}

	private static void logWarning(String message)
	{
		final String log = DEBUG_PREFIX + " " + message;
		LOGGER.warning(log);
		System.out.println(log);
	}

	private static String toHex(byte[] data)
	{
		final StringBuilder sb = new StringBuilder(data.length * 3);
		for (int i = 0; i < data.length; i++)
		{
			if (i > 0)
			{
				sb.append(' ');
			}
			sb.append(String.format("%02X", data[i] & 0xFF));
		}
		return sb.toString();
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
