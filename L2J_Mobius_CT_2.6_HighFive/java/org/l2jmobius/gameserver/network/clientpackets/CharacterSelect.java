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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import org.l2jmobius.gameserver.data.xml.impl.SecondaryAuthData;
import org.l2jmobius.gameserver.instancemanager.AntiFeedManager;
import org.l2jmobius.gameserver.instancemanager.PunishmentManager;
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerSelect;
import org.l2jmobius.gameserver.model.events.returns.TerminateReturn;
import org.l2jmobius.gameserver.model.punishment.PunishmentAffect;
import org.l2jmobius.gameserver.model.punishment.PunishmentType;
import org.l2jmobius.gameserver.network.ConnectionState;
import org.l2jmobius.gameserver.network.Disconnection;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.CharSelected;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.SSQInfo;
import org.l2jmobius.gameserver.network.serverpackets.ServerClose;

/**
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelect implements IClientIncomingPacket
{
	protected static final Logger LOGGER_ACCOUNTING = Logger.getLogger("accounting");
	
	// cd
	private int _charSlot;
	
	@SuppressWarnings("unused")
	private int _unk1; // new in C4
	@SuppressWarnings("unused")
	private int _unk2; // new in C4
	@SuppressWarnings("unused")
	private int _unk3; // new in C4
	@SuppressWarnings("unused")
	private int _unk4; // new in C4
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_charSlot = packet.readD();
		if (client.isSalvation140Client())
		{
			final int remaining = packet.getReadableBytes();
			if (remaining > 0)
			{
				packet.readB(remaining);
			}
			LOGGER_ACCOUNTING.info("CharacterSelect read account=" + client.getAccountName() + ", protocol=" + client.getProtocolVersion() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState() + ", slot=" + _charSlot + ", skipped=" + remaining + ", remaining=" + packet.getReadableBytes());
			System.out.println("SALVATION140 CharacterSelect read account=" + client.getAccountName() + ", state=" + client.getConnectionState() + ", slot=" + _charSlot + ", skipped=" + remaining + ", remaining=" + packet.getReadableBytes());
			return true;
		}
		
		_unk1 = packet.readH();
		_unk2 = packet.readD();
		_unk3 = packet.readD();
		_unk4 = packet.readD();
		LOGGER_ACCOUNTING.info("CharacterSelect read account=" + client.getAccountName() + ", protocol=" + client.getProtocolVersion() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState() + ", slot=" + _charSlot + ", remaining=" + packet.getReadableBytes());
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		LOGGER_ACCOUNTING.info("CharacterSelect run account=" + client.getAccountName() + ", protocol=" + client.getProtocolVersion() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState() + ", slot=" + _charSlot);
		if (client.isSalvation140Client())
		{
			System.out.println("SALVATION140 CharacterSelect run account=" + client.getAccountName() + ", state=" + client.getConnectionState() + ", slot=" + _charSlot);
		}
		
		if (!client.getFloodProtectors().getCharacterSelect().tryPerformAction("CharacterSelect"))
		{
			LOGGER_ACCOUNTING.warning("CharacterSelect rejected by flood protector, account=" + client.getAccountName() + ", slot=" + _charSlot + ", state=" + client.getConnectionState());
			return;
		}
		
		if (SecondaryAuthData.getInstance().isEnabled() && !client.getSecondaryAuth().isAuthed())
		{
			LOGGER_ACCOUNTING.info("CharacterSelect secondary auth required, account=" + client.getAccountName() + ", slot=" + _charSlot + ", state=" + client.getConnectionState());
			client.getSecondaryAuth().openDialog();
			return;
		}
		
		// We should always be able to acquire the lock
		// But if we can't lock then nothing should be done (i.e. repeated packet)
		if (client.getPlayerLock().tryLock())
		{
			try
			{
				// should always be null
				// but if not then this is repeated packet and nothing should be done here
				if (client.getPlayer() == null)
				{
					final CharSelectInfoPackage info = client.getCharSelection(_charSlot);
					if (info == null)
					{
						LOGGER_ACCOUNTING.warning("CharacterSelect no character for account=" + client.getAccountName() + ", slot=" + _charSlot + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState());
						return;
					}
					LOGGER_ACCOUNTING.info("CharacterSelect selected account=" + client.getAccountName() + ", slot=" + _charSlot + ", objectId=" + info.getObjectId() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState());
					
					// Banned?
					if (PunishmentManager.getInstance().hasPunishment(info.getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN) || PunishmentManager.getInstance().hasPunishment(client.getConnectionAddress().getHostAddress(), PunishmentAffect.IP, PunishmentType.BAN))
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					
					// Selected character is banned (compatibility with previous versions).
					if (info.getAccessLevel() < 0)
					{
						client.close(ServerClose.STATIC_PACKET);
						return;
					}
					
					if ((Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0) && !AntiFeedManager.getInstance().tryAddClient(AntiFeedManager.GAME_ID, client, Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP))
					{
						final NpcHtmlMessage msg = new NpcHtmlMessage();
						msg.setFile(null, "data/html/mods/IPRestriction.htm");
						msg.replace("%max%", String.valueOf(AntiFeedManager.getInstance().getLimit(client, Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP)));
						client.sendPacket(msg);
						return;
					}
					
					if (Config.FACTION_SYSTEM_ENABLED && Config.FACTION_BALANCE_ONLINE_PLAYERS)
					{
						if (info.isGood() && (World.getInstance().getAllGoodPlayers().size() >= (World.getInstance().getAllEvilPlayers().size() + Config.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)))
						{
							final NpcHtmlMessage msg = new NpcHtmlMessage();
							msg.setFile(null, "data/html/mods/Faction/ExceededOnlineLimit.htm");
							msg.replace("%more%", Config.FACTION_GOOD_TEAM_NAME);
							msg.replace("%less%", Config.FACTION_EVIL_TEAM_NAME);
							client.sendPacket(msg);
							return;
						}
						if (info.isEvil() && (World.getInstance().getAllEvilPlayers().size() >= (World.getInstance().getAllGoodPlayers().size() + Config.FACTION_BALANCE_PLAYER_EXCEED_LIMIT)))
						{
							final NpcHtmlMessage msg = new NpcHtmlMessage();
							msg.setFile(null, "data/html/mods/Faction/ExceededOnlineLimit.htm");
							msg.replace("%more%", Config.FACTION_EVIL_TEAM_NAME);
							msg.replace("%less%", Config.FACTION_GOOD_TEAM_NAME);
							client.sendPacket(msg);
							return;
						}
					}
					
					// load up character from disk
					final PlayerInstance cha = client.load(_charSlot);
					if (cha == null)
					{
						LOGGER_ACCOUNTING.warning("CharacterSelect load failed account=" + client.getAccountName() + ", slot=" + _charSlot + ", objectId=" + info.getObjectId() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState());
						return; // handled in GameClient
					}
					
					CharNameTable.getInstance().addName(cha);
					
					cha.setClient(client);
					client.setPlayer(cha);
					cha.setOnlineStatus(true, true);
					
					final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerSelect(cha, cha.getObjectId(), cha.getName(), client), Containers.Players(), TerminateReturn.class);
					if ((terminate != null) && terminate.terminate())
					{
						Disconnection.of(cha).defaultSequence(false);
						return;
					}
					
					if (!client.isSalvation140Client())
					{
						client.sendPacket(new SSQInfo());
					}
					
					client.setConnectionState(ConnectionState.ENTERING);
					LOGGER_ACCOUNTING.info("CharacterSelect sending CharSelected account=" + client.getAccountName() + ", slot=" + _charSlot + ", objectId=" + cha.getObjectId() + ", profile=" + client.getProtocolProfile() + ", state=" + client.getConnectionState());
					if (client.isSalvation140Client())
					{
						System.out.println("SALVATION140 CharacterSelect sending CharSelected account=" + client.getAccountName() + ", slot=" + _charSlot + ", objectId=" + cha.getObjectId() + ", state=" + client.getConnectionState());
					}
					client.sendPacket(new CharSelected(cha, client.getSessionId().playOkID1, client.getProtocolProfile()));
				}
			}
			finally
			{
				client.getPlayerLock().unlock();
			}
			
			LOGGER_ACCOUNTING.info("Logged in, " + client);
		}
	}
}
