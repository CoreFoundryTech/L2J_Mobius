/*
 * This file is part of the L2J Mobius project.
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.model.actor.Decoy;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class CharInfoSalvation140 implements IClientOutgoingPacket
{
	private static final int[] PAPERDOLL_ORDER =
	{
		Inventory.PAPERDOLL_UNDER,
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
		Inventory.PAPERDOLL_HAIR2
	};
	
	private final PlayerInstance _player;
	private int _objId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _vehicleId;
	private final boolean _gmSeeInvis;
	private final int _mAtkSpd;
	private final int _pAtkSpd;
	private final int _runSpd;
	private final int _walkSpd;
	private final int _swimRunSpd;
	private final int _swimWalkSpd;
	private final int _flyRunSpd;
	private final int _flyWalkSpd;
	private final double _moveMultiplier;
	
	public CharInfoSalvation140(PlayerInstance player, boolean gmSeeInvis)
	{
		_player = player;
		_objId = player.getObjectId();
		if ((player.getVehicle() != null) && (player.getInVehiclePosition() != null))
		{
			_x = player.getInVehiclePosition().getX();
			_y = player.getInVehiclePosition().getY();
			_z = player.getInVehiclePosition().getZ();
			_vehicleId = player.getVehicle().getObjectId();
		}
		else
		{
			_x = player.getX();
			_y = player.getY();
			_z = player.getZ();
		}
		_heading = player.getHeading();
		_gmSeeInvis = gmSeeInvis;
		_mAtkSpd = player.getMAtkSpd();
		_pAtkSpd = (int) player.getPAtkSpd();
		_moveMultiplier = player.getMovementSpeedMultiplier();
		_runSpd = (int) Math.round(player.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) Math.round(player.getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = (int) Math.round(player.getSwimRunSpeed() / _moveMultiplier);
		_swimWalkSpd = (int) Math.round(player.getSwimWalkSpeed() / _moveMultiplier);
		_flyRunSpd = player.isFlying() ? _runSpd : 0;
		_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
	}
	
	public CharInfoSalvation140(Decoy decoy, boolean gmSeeInvis)
	{
		this(decoy.getActingPlayer(), gmSeeInvis);
		_objId = decoy.getObjectId();
		_x = decoy.getX();
		_y = decoy.getY();
		_z = decoy.getZ();
		_heading = decoy.getHeading();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.CHAR_INFO.writeId(packet);
		packet.writeC(0x00);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeD(_vehicleId);
		packet.writeD(_objId);
		packet.writeS(_player.getAppearance().getVisibleName());
		packet.writeH(_player.getRace().ordinal());
		packet.writeC(_player.getAppearance().getSex() ? 0x01 : 0x00);
		packet.writeD(_player.getBaseClass());
		
		for (int slot : PAPERDOLL_ORDER)
		{
			packet.writeD(_player.getInventory().getPaperdollItemDisplayId(slot));
		}
		for (int slot : PAPERDOLL_ORDER)
		{
			packet.writeD(_player.getInventory().getPaperdollAugmentationId(slot));
			packet.writeD(0x00);
		}
		packet.writeC(0x00);
		for (int ignored : PAPERDOLL_ORDER)
		{
			packet.writeD(0x00);
		}
		packet.writeC(_player.getPvpFlag());
		packet.writeD(_player.getKarma());
		packet.writeD(_mAtkSpd);
		packet.writeD(_pAtkSpd);
		packet.writeH(_runSpd);
		packet.writeH(_walkSpd);
		packet.writeH(_swimRunSpd);
		packet.writeH(_swimWalkSpd);
		packet.writeH(_flyRunSpd);
		packet.writeH(_flyWalkSpd);
		packet.writeH(_flyRunSpd);
		packet.writeH(_flyWalkSpd);
		packet.writeF(_moveMultiplier);
		packet.writeF(_player.getAttackSpeedMultiplier());
		packet.writeF(_player.getCollisionRadius());
		packet.writeF(_player.getCollisionHeight());
		packet.writeD(_player.getAppearance().getHairStyle());
		packet.writeD(_player.getAppearance().getHairColor());
		packet.writeD(_player.getAppearance().getFace());
		packet.writeS(_gmSeeInvis ? "Invisible" : _player.getAppearance().getVisibleTitle());
		packet.writeD(_player.getClanId());
		packet.writeD(_player.getClanCrestId());
		packet.writeD(_player.getAllyId());
		packet.writeD(_player.getAllyCrestId());
		packet.writeC(_player.isSitting() ? 0x00 : 0x01);
		packet.writeC(_player.isRunning() ? 0x01 : 0x00);
		packet.writeC(_player.isInCombat() ? 0x01 : 0x00);
		packet.writeC(!_player.isInOlympiadMode() && _player.isAlikeDead() ? 0x01 : 0x00);
		packet.writeC(!_gmSeeInvis && _player.isInvisible() ? 0x01 : 0x00);
		packet.writeC(_player.getMountType().ordinal());
		packet.writeC(_player.getPrivateStoreType().getId());
		packet.writeH(_player.getCubics().size());
		for (int cubicId : _player.getCubics().keySet())
		{
			packet.writeH(cubicId);
		}
		packet.writeC(_player.isInPartyMatchRoom() ? 0x01 : 0x00);
		packet.writeC(_player.isInsideZone(ZoneId.WATER) ? 1 : _player.isFlyingMounted() ? 2 : 0);
		packet.writeH(_player.getRecomHave());
		packet.writeD(_player.getMountNpcId() == 0 ? 0 : _player.getMountNpcId() + 1000000);
		packet.writeD(_player.getClassId().getId());
		packet.writeD(0x00);
		packet.writeC(_player.isMounted() ? 0 : _player.getEnchantEffect());
		packet.writeC(_player.getTeam().getId());
		packet.writeD(_player.getClanCrestLargeId());
		packet.writeC(_player.isNoble() ? 1 : 0);
		packet.writeC(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) ? 1 : 0);
		packet.writeC(_player.isFishing() ? 1 : 0);
		packet.writeD(_player.getFishx());
		packet.writeD(_player.getFishy());
		packet.writeD(_player.getFishz());
		packet.writeD(_player.getAppearance().getNameColor());
		packet.writeD(_heading);
		packet.writeC(_player.getPledgeClass());
		packet.writeH(_player.getPledgeType());
		packet.writeD(_player.getAppearance().getTitleColor());
		packet.writeC(_player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquippedId()) : 0);
		packet.writeD(_player.getClanId() > 0 ? _player.getClan().getReputationScore() : 0);
		packet.writeD(_player.getTransformationDisplayId());
		packet.writeD(_player.getAgathionId());
		packet.writeC(0x00);
		packet.writeD((int) Math.round(_player.getCurrentCp()));
		packet.writeD(_player.getMaxHp());
		packet.writeD((int) Math.round(_player.getCurrentHp()));
		packet.writeD(_player.getMaxMp());
		packet.writeD((int) Math.round(_player.getCurrentMp()));
		packet.writeC(0x00);
		packet.writeD(0x00);
		packet.writeC(0x00);
		packet.writeC(0x01);
		packet.writeC(0x00);
		return true;
	}
}
