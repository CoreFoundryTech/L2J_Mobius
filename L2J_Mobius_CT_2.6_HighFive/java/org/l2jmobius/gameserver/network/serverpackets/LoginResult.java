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

import org.l2jmobius.commons.network.PacketWriter;

public class LoginResult implements IClientOutgoingPacket
{
	public static final LoginResult SUCCESS = new LoginResult(0xFFFFFFFF, 0);
	
	private final int _reason1;
	private final int _reason2;
	
	private LoginResult(int reason1, int reason2)
	{
		_reason1 = reason1;
		_reason2 = reason2;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		// Salvation/Classic 140 uses opcode 0x0A for LoginResult success.
		// Mobius H5 already names 0x0A as LOGIN_FAIL, so keep this local to avoid changing H5 callers.
		packet.writeC(0x0A);
		packet.writeD(_reason1);
		packet.writeD(_reason2);
		return true;
	}
}
