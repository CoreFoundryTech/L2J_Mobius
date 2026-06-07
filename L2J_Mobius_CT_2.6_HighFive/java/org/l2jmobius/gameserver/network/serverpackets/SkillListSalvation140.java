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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.impl.EnchantSkillGroupsData;
import org.l2jmobius.gameserver.data.xml.impl.SkillData;
import org.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import org.l2jmobius.gameserver.model.EnchantSkillLearn;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.transform.Transform;
import org.l2jmobius.gameserver.model.holders.AdditionalSkillHolder;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class SkillListSalvation140 implements IClientOutgoingPacket
{
	private final List<SkillInfo> _skills = new ArrayList<>();
	private int _lastLearnedSkillId = 0;

	static class SkillInfo
	{
		public int id;
		public int reuseDelayGroup;
		public int level;
		public int subLevel;
		public boolean passive;
		public boolean disabled;
		public boolean enchanted;

		SkillInfo(int pId, int pReuseDelayGroup, int pLevel, int pSubLevel, boolean pPassive, boolean pDisabled, boolean pEnchanted)
		{
			id = pId;
			reuseDelayGroup = pReuseDelayGroup;
			level = pLevel;
			subLevel = pSubLevel;
			passive = pPassive;
			disabled = pDisabled;
			enchanted = pEnchanted;
		}
	}

	public SkillListSalvation140(PlayerInstance player)
	{
		boolean isDisabled = false;
		final Transform transformation = player.getTransformation();
		for (Skill skill : player.getAllSkills())
		{
			if ((skill == null) || ((transformation != null) && !skill.isPassive()) || (player.hasTransformSkill(skill.getId()) && skill.isPassive()))
			{
				continue;
			}
			if (player.getClan() != null)
			{
				isDisabled = skill.isClanSkill() && (player.getClan().getReputationScore() < 0);
			}

			boolean isEnchantable = SkillData.getInstance().isEnchantable(skill.getId());
			if (isEnchantable)
			{
				final EnchantSkillLearn esl = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(skill.getId());
				if ((esl == null) || (skill.getLevel() < esl.getBaseLevel()))
				{
					isEnchantable = false;
				}
			}

			addSkill(skill.getDisplayId(), skill.getReuseHashCode(), skill.getDisplayLevel(), 0, skill.isPassive(), isDisabled, isEnchantable);
		}

		if (transformation != null)
		{
			final Map<Integer, Integer> transformSkills = new TreeMap<>();
			for (SkillHolder holder : transformation.getTemplate(player).getSkills())
			{
				transformSkills.putIfAbsent(holder.getSkillId(), holder.getSkillLevel());
				if (transformSkills.get(holder.getSkillId()) < holder.getSkillLevel())
				{
					transformSkills.put(holder.getSkillId(), holder.getSkillLevel());
				}
			}
			for (AdditionalSkillHolder holder : transformation.getTemplate(player).getAdditionalSkills())
			{
				if (player.getLevel() >= holder.getMinLevel())
				{
					transformSkills.putIfAbsent(holder.getSkillId(), holder.getSkillLevel());
					if (transformSkills.get(holder.getSkillId()) < holder.getSkillLevel())
					{
						transformSkills.put(holder.getSkillId(), holder.getSkillLevel());
					}
				}
			}
			for (SkillLearn skill : SkillTreesData.getInstance().getCollectSkillTree().values())
			{
				if (player.getKnownSkill(skill.getSkillId()) != null)
				{
					player.addTransformSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
				}
			}
			for (Entry<Integer, Integer> transformSkill : transformSkills.entrySet())
			{
				final Skill skill = SkillData.getInstance().getSkill(transformSkill.getKey(), transformSkill.getValue());
				player.addTransformSkill(skill);
				addSkill(transformSkill.getKey(), skill == null ? 0 : skill.getReuseHashCode(), transformSkill.getValue(), 0, false, false, false);
			}
		}
	}

	public void addSkill(int id, int reuseDelayGroup, int level, int subLevel, boolean passive, boolean disabled, boolean enchanted)
	{
		_skills.add(new SkillInfo(id, reuseDelayGroup, level, subLevel, passive, disabled, enchanted));
	}

	public void setLastLearnedSkillId(int lastLearnedSkillId)
	{
		_lastLearnedSkillId = lastLearnedSkillId;
	}

	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SKILL_LIST.writeId(packet);
		packet.writeD(_skills.size());
		for (SkillInfo skill : _skills)
		{
			packet.writeD(skill.passive ? 1 : 0);
			packet.writeH(skill.level);
			packet.writeH(skill.subLevel);
			packet.writeD(skill.id);
			packet.writeD(skill.reuseDelayGroup);
			packet.writeC(skill.disabled ? 1 : 0);
			packet.writeC(skill.enchanted ? 1 : 0);
		}
		packet.writeD(_lastLearnedSkillId);
		return true;
	}
}
