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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;

/** Salvation 140 hardware info request. Parses the payload using the Salvation layout without storing private hardware identifiers. */
public class RequestHardWareInfoSalvation140 implements IClientIncomingPacket
{
	private int _windowsPlatformId;
	private int _windowsMajorVersion;
	private int _windowsMinorVersion;
	private int _windowsBuildNumber;
	private int _directxVersion;
	private int _directxRevision;
	private int _cpuSpeed;
	private int _cpuCoreCount;
	private int _vgaCount;
	private int _videoMemory;
	private int _vgaVersion;
	private int _skipped;

	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readS(); // MAC address. Do not store or log.
		_windowsPlatformId = packet.readD();
		_windowsMajorVersion = packet.readD();
		_windowsMinorVersion = packet.readD();
		_windowsBuildNumber = packet.readD();
		_directxVersion = packet.readD();
		_directxRevision = packet.readD();
		packet.readB(16);
		packet.readS(); // CPU name. Do not log.
		_cpuSpeed = packet.readD();
		_cpuCoreCount = packet.readC();
		packet.readD();
		_vgaCount = packet.readD();
		packet.readD(); // VGA PCI speed.
		packet.readD(); // Physical memory slot 1.
		packet.readD(); // Physical memory slot 2.
		packet.readD(); // Physical memory slot 3.
		packet.readC();
		_videoMemory = packet.readD();
		packet.readD();
		_vgaVersion = packet.readH();
		packet.readS(); // VGA name. Do not log.
		packet.readS(); // VGA driver version. Do not log.
		_skipped = packet.getReadableBytes();
		packet.readB(_skipped);
		return true;
	}

	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		System.out.println("SALVATION140 RequestHardWareInfo account=" + client.getAccountName() + ", player=" + ((player != null) ? player.getName() : "null") + ", windows=" + _windowsPlatformId + "." + _windowsMajorVersion + "." + _windowsMinorVersion + "." + _windowsBuildNumber + ", directx=" + _directxVersion + "." + _directxRevision + ", cpuSpeed=" + _cpuSpeed + ", cpuCores=" + _cpuCoreCount + ", vgaCount=" + _vgaCount + ", videoMemory=" + _videoMemory + ", vgaVersion=" + _vgaVersion + ", skipped=" + _skipped);
	}
}
