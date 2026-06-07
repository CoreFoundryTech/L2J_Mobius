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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.impl.ClanTable;
import org.l2jmobius.gameserver.data.xml.impl.ExperienceData;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.ClientProtocolProfile;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class CharSelectionInfo implements IClientOutgoingPacket
{
	private static Logger LOGGER = Logger.getLogger(CharSelectionInfo.class.getName());
	private final String _loginName;
	private final int _sessionId;
	private int _activeId;
	private final CharSelectInfoPackage[] _characterPackages;
	private final ClientProtocolProfile _protocolProfile;
	
	/**
	 * Constructor for CharSelectionInfo.
	 * @param loginName
	 * @param sessionId
	 */
	public CharSelectionInfo(String loginName, int sessionId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = -1;
		_protocolProfile = ClientProtocolProfile.HIGH_FIVE;
	}

	public CharSelectionInfo(String loginName, int sessionId, int activeId)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = activeId;
		_protocolProfile = ClientProtocolProfile.HIGH_FIVE;
	}

	public CharSelectionInfo(String loginName, int sessionId, ClientProtocolProfile protocolProfile)
	{
		_sessionId = sessionId;
		_loginName = loginName;
		_characterPackages = loadCharacterSelectInfo(_loginName);
		_activeId = -1;
		_protocolProfile = protocolProfile;
	}
	
	public CharSelectInfoPackage[] getCharInfo()
	{
		return _characterPackages;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		return _protocolProfile == ClientProtocolProfile.SALVATION_140 ? writeSalvation140(packet) : writeHighFive(packet);
	}

	private boolean writeHighFive(PacketWriter packet)
	{
		OutgoingPackets.CHARACTER_SELECTION_INFO.writeId(packet);
		
		final int size = _characterPackages.length;
		packet.writeD(size); // Created character count
		
		packet.writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT); // Can prevent players from creating new characters (if 0); (if 1, the client will ask if chars may be created (0x13) Response: (0x0D) )
		packet.writeC(0x00);
		
		long lastAccess = 0;
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				if (lastAccess < _characterPackages[i].getLastAccess())
				{
					lastAccess = _characterPackages[i].getLastAccess();
					_activeId = i;
				}
			}
		}
		
		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = _characterPackages[i];
			
			packet.writeS(charInfoPackage.getName()); // Character name
			packet.writeD(charInfoPackage.getObjectId()); // Character ID
			packet.writeS(_loginName); // Account name
			packet.writeD(_sessionId); // Account ID
			packet.writeD(charInfoPackage.getClanId()); // Clan ID
			packet.writeD(0x00); // Builder level
			
			packet.writeD(charInfoPackage.getSex()); // Sex
			packet.writeD(charInfoPackage.getRace()); // Race
			packet.writeD(charInfoPackage.getBaseClassId());
			
			packet.writeD(0x01); // GameServerName
			
			packet.writeD(charInfoPackage.getX());
			packet.writeD(charInfoPackage.getY());
			packet.writeD(charInfoPackage.getZ());
			packet.writeF(charInfoPackage.getCurrentHp());
			packet.writeF(charInfoPackage.getCurrentMp());
			
			packet.writeD((int) charInfoPackage.getSp());
			packet.writeQ(charInfoPackage.getExp());
			packet.writeF((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel()))); // High
																																																																									// Five
			packet.writeD(charInfoPackage.getLevel());
			
			packet.writeD(charInfoPackage.getKarma());
			packet.writeD(charInfoPackage.getPkKills());
			packet.writeD(charInfoPackage.getPvPKills());
			
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			
			for (int slot : getPaperdollOrder())
			{
				packet.writeD(charInfoPackage.getPaperdollItemId(slot));
			}
			
			packet.writeD(charInfoPackage.getHairStyle());
			packet.writeD(charInfoPackage.getHairColor());
			packet.writeD(charInfoPackage.getFace());
			
			packet.writeF(charInfoPackage.getMaxHp()); // Maximum HP
			packet.writeF(charInfoPackage.getMaxMp()); // Maximum MP
			
			packet.writeD(charInfoPackage.getDeleteTimer() > 0 ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0);
			packet.writeD(charInfoPackage.getClassId());
			packet.writeD(i == _activeId ? 0x01 : 0x00);
			
			packet.writeC(Math.min(charInfoPackage.getEnchantEffect(), 127));
			packet.writeD(charInfoPackage.getAugmentationId());
			
			// packet.writeD(charInfoPackage.getTransformId()); // Used to display Transformations
			packet.writeD(0x00); // Currently on retail when you are on character select you don't see your transformation.
			
			packet.writeD(0x00); // Pet NpcId
			packet.writeD(0x00); // Pet level
			packet.writeD(0x00); // Pet Food
			packet.writeD(0x00); // Pet Food Level
			packet.writeF(0x00); // Current pet HP
			packet.writeF(0x00); // Current pet MP
			
			packet.writeD(charInfoPackage.getVitalityPoints()); // Vitality
		}
		return true;
	}

	private boolean writeSalvation140(PacketWriter packet)
	{
		OutgoingPackets.CHARACTER_SELECTION_INFO.writeId(packet);

		final int size = _characterPackages.length;
		packet.writeD(size); // Created character count
		packet.writeD(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT);
		packet.writeC(size >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT ? 0x01 : 0x00);
		packet.writeC(0x00);
		packet.writeD(0x02); // Normal lobby.
		packet.writeC(0x00); // Premium account prompt.
		packet.writeC(0x00); // Protocol 140 marker.

		long lastAccess = 0;
		if (_activeId == -1)
		{
			for (int i = 0; i < size; i++)
			{
				if (lastAccess < _characterPackages[i].getLastAccess())
				{
					lastAccess = _characterPackages[i].getLastAccess();
					_activeId = i;
				}
			}
		}

		for (int i = 0; i < size; i++)
		{
			final CharSelectInfoPackage charInfoPackage = _characterPackages[i];

			packet.writeS(charInfoPackage.getName());
			packet.writeD(charInfoPackage.getObjectId());
			packet.writeS(_loginName);
			packet.writeD(_sessionId);
			packet.writeD(charInfoPackage.getClanId());
			packet.writeD(0x00); // Builder level.

			packet.writeD(charInfoPackage.getSex());
			packet.writeD(charInfoPackage.getRace());
			packet.writeD(charInfoPackage.getBaseClassId());

			packet.writeD(0x01); // GameServerName

			packet.writeD(charInfoPackage.getX());
			packet.writeD(charInfoPackage.getY());
			packet.writeD(charInfoPackage.getZ());
			packet.writeF(charInfoPackage.getCurrentHp());
			packet.writeF(charInfoPackage.getCurrentMp());

			packet.writeQ(charInfoPackage.getSp());
			packet.writeQ(charInfoPackage.getExp());
			packet.writeF((float) (charInfoPackage.getExp() - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())) / (ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(charInfoPackage.getLevel())));
			packet.writeD(charInfoPackage.getLevel());

			packet.writeD(charInfoPackage.getKarma());
			packet.writeD(charInfoPackage.getPkKills());
			packet.writeD(charInfoPackage.getPvPKills());

			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00); // Ertheia placeholder.
			packet.writeD(0x00); // Ertheia placeholder.

			for (int slot : getPaperdollOrder())
			{
				packet.writeD(charInfoPackage.getPaperdollItemId(slot));
			}

			// Protocol 140 expects the post-H5 paperdoll extension used by Salvation/Classic clients.
			// High Five has no brooch, jewels, or agathion paperdoll slots, so keep layout alignment
			// by writing zero placeholders before the visual item-id block. This mirrors the
			// L2Scripts 140 Inventory.PAPERDOLL_ORDER tail: brooch, jewel1-6, agathion main, agathion1-4.
			for (int slot = 0; slot < 12; slot++)
			{
				packet.writeD(0x00);
			}

			packet.writeD(0x00); // Weapon visual item id.
			packet.writeD(0x00); // Shield visual item id.
			packet.writeD(0x00); // Gloves visual item id.
			packet.writeD(0x00); // Chest visual item id.
			packet.writeD(0x00); // Legs visual item id.
			packet.writeD(0x00); // Feet visual item id.
			packet.writeD(0x00);
			packet.writeD(0x00); // Hair visual item id.
			packet.writeD(0x00); // Hair2 visual item id.

			packet.writeH(0x00); // Chest enchant effect.
			packet.writeH(0x00); // Legs enchant effect.
			packet.writeH(0x00); // Head enchant effect.
			packet.writeH(0x00); // Gloves enchant effect.
			packet.writeH(0x00); // Feet enchant effect.

			packet.writeD(charInfoPackage.getPaperdollItemId(Inventory.PAPERDOLL_HAIR) > 0 ? charInfoPackage.getSex() : charInfoPackage.getHairStyle());
			packet.writeD(charInfoPackage.getHairColor());
			packet.writeD(charInfoPackage.getFace());

			packet.writeF(charInfoPackage.getMaxHp());
			packet.writeF(charInfoPackage.getMaxMp());

			packet.writeD(charInfoPackage.getAccessLevel() > -100 ? (charInfoPackage.getDeleteTimer() > 0 ? (int) ((charInfoPackage.getDeleteTimer() - System.currentTimeMillis()) / 1000) : 0) : -1);
			packet.writeD(charInfoPackage.getClassId());
			packet.writeD(i == _activeId ? 0x01 : 0x00);

			packet.writeC(Math.min(charInfoPackage.getEnchantEffect(), 127));
			packet.writeD(charInfoPackage.getAugmentationId());
			packet.writeD(0x00);

			packet.writeD(0x00); // Transform id.
			packet.writeD(0x00); // Pet NpcId.
			packet.writeD(0x00); // Pet level.
			packet.writeD(0x00); // Pet Food.
			packet.writeD(0x00); // Pet Food Level.
			packet.writeF(0x00); // Current pet HP.
			packet.writeF(0x00); // Current pet MP.

			packet.writeD(charInfoPackage.getVitalityPoints() * 7);
			packet.writeD(charInfoPackage.getVitalityPoints() > 0 ? 100 : 100);
			packet.writeD(0x00); // Use Vitality Potions Left.

			packet.writeD(0x01); // Character available.
			packet.writeC(0x00); // Chaos Festival Winner.
			packet.writeC(0x00); // Hero glow.
			packet.writeC(0x01); // Show hair accessory.
		}
		return true;
	}
	
	private static CharSelectInfoPackage[] loadCharacterSelectInfo(String loginName)
	{
		CharSelectInfoPackage charInfopackage;
		final List<CharSelectInfoPackage> characterList = new LinkedList<>();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM characters WHERE account_name=? ORDER BY createDate"))
		{
			statement.setString(1, loginName);
			try (ResultSet charList = statement.executeQuery())
			{
				while (charList.next()) // fills the package
				{
					charInfopackage = restoreChar(charList);
					if (charInfopackage != null)
					{
						characterList.add(charInfopackage);
						
						final PlayerInstance player = World.getInstance().getPlayer(charInfopackage.getObjectId());
						if (player != null)
						{
							IdFactory.getInstance().releaseId(player.getObjectId());
							Disconnection.of(player).storeMe().deleteMe();
						}
					}
				}
			}
			return characterList.toArray(new CharSelectInfoPackage[characterList.size()]);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not restore char info: " + e.getMessage(), e);
		}
		return new CharSelectInfoPackage[0];
	}
	
	private static void loadCharacterSubclassInfo(CharSelectInfoPackage charInfopackage, int ObjectId, int activeClassId)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE charId=? AND class_id=? ORDER BY charId"))
		{
			statement.setInt(1, ObjectId);
			statement.setInt(2, activeClassId);
			try (ResultSet charList = statement.executeQuery())
			{
				if (charList.next())
				{
					charInfopackage.setExp(charList.getLong("exp"));
					charInfopackage.setSp(charList.getInt("sp"));
					charInfopackage.setLevel(charList.getInt("level"));
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not restore char subclass info: " + e.getMessage(), e);
		}
	}
	
	private static CharSelectInfoPackage restoreChar(ResultSet chardata) throws Exception
	{
		final int objectId = chardata.getInt("charId");
		final String name = chardata.getString("char_name");
		
		// See if the char must be deleted
		final long deletetime = chardata.getLong("deletetime");
		if (deletetime > 0)
		{
			if (System.currentTimeMillis() > deletetime)
			{
				final Clan clan = ClanTable.getInstance().getClan(chardata.getInt("clanid"));
				if (clan != null)
				{
					clan.removeClanMember(objectId, 0);
				}
				
				GameClient.deleteCharByObjId(objectId);
				return null;
			}
		}
		
		final CharSelectInfoPackage charInfopackage = new CharSelectInfoPackage(objectId, name);
		charInfopackage.setAccessLevel(chardata.getInt("accesslevel"));
		charInfopackage.setLevel(chardata.getInt("level"));
		charInfopackage.setMaxHp(chardata.getInt("maxhp"));
		charInfopackage.setCurrentHp(chardata.getDouble("curhp"));
		charInfopackage.setMaxMp(chardata.getInt("maxmp"));
		charInfopackage.setCurrentMp(chardata.getDouble("curmp"));
		charInfopackage.setKarma(chardata.getInt("karma"));
		charInfopackage.setPkKills(chardata.getInt("pkkills"));
		charInfopackage.setPvPKills(chardata.getInt("pvpkills"));
		charInfopackage.setFace(chardata.getInt("face"));
		charInfopackage.setHairStyle(chardata.getInt("hairstyle"));
		charInfopackage.setHairColor(chardata.getInt("haircolor"));
		charInfopackage.setSex(chardata.getInt("sex"));
		
		charInfopackage.setExp(chardata.getLong("exp"));
		charInfopackage.setSp(chardata.getLong("sp"));
		charInfopackage.setVitalityPoints(chardata.getInt("vitality_points"));
		charInfopackage.setClanId(chardata.getInt("clanid"));
		
		charInfopackage.setRace(chardata.getInt("race"));
		
		final int baseClassId = chardata.getInt("base_class");
		final int activeClassId = chardata.getInt("classid");
		
		charInfopackage.setX(chardata.getInt("x"));
		charInfopackage.setY(chardata.getInt("y"));
		charInfopackage.setZ(chardata.getInt("z"));
		
		final int faction = chardata.getInt("faction");
		if (faction == 1)
		{
			charInfopackage.setGood();
		}
		if (faction == 2)
		{
			charInfopackage.setEvil();
		}
		
		if (Config.MULTILANG_ENABLE)
		{
			String lang = chardata.getString("language");
			if (!Config.MULTILANG_ALLOWED.contains(lang))
			{
				lang = Config.MULTILANG_DEFAULT;
			}
			charInfopackage.setHtmlPrefix("data/lang/" + lang + "/");
		}
		
		// if is in subclass, load subclass exp, sp, lvl info
		if (baseClassId != activeClassId)
		{
			loadCharacterSubclassInfo(charInfopackage, objectId, activeClassId);
		}
		
		charInfopackage.setClassId(activeClassId);
		
		// Get the augmentation id for equipped weapon
		int weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		if (weaponObjId < 1)
		{
			weaponObjId = charInfopackage.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND);
		}
		
		if (weaponObjId > 0)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT augAttributes FROM item_attributes WHERE itemId=?"))
			{
				statement.setInt(1, weaponObjId);
				try (ResultSet result = statement.executeQuery())
				{
					if (result.next())
					{
						final int augment = result.getInt("augAttributes");
						charInfopackage.setAugmentationId(augment == -1 ? 0 : augment);
					}
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.WARNING, "Could not restore augmentation info: " + e.getMessage(), e);
			}
		}
		
		// Check if the base class is set to zero and also doesn't match with the current active class, otherwise send the base class ID. This prevents chars created before base class was introduced from being displayed incorrectly.
		if ((baseClassId == 0) && (activeClassId > 0))
		{
			charInfopackage.setBaseClassId(activeClassId);
		}
		else
		{
			charInfopackage.setBaseClassId(baseClassId);
		}
		
		charInfopackage.setDeleteTimer(deletetime);
		charInfopackage.setLastAccess(chardata.getLong("lastAccess"));
		return charInfopackage;
	}
}
