package com.feed_the_beast.ftbl.cmd.team;

import com.feed_the_beast.ftbl.api.EnumTeamStatus;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.IForgeTeam;
import com.feed_the_beast.ftbl.api_impl.ForgeTeam;
import com.feed_the_beast.ftbl.lib.cmd.CmdBase;
import com.feed_the_beast.ftbl.lib.internal.FTBLibLang;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * @author LatvianModder
 */
public class CmdTransferOwnership extends CmdBase
{
	public CmdTransferOwnership()
	{
		super("transfer_ownership", Level.ALL);
	}

	@Override
	public boolean isUsernameIndex(String[] args, int i)
	{
		return i == 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		EntityPlayerMP ep = getCommandSenderAsPlayer(sender);
		IForgePlayer p = getForgePlayer(ep);
		IForgeTeam team = p.getTeam();

		if (team == null)
		{
			throw FTBLibLang.TEAM_NO_TEAM.commandError();
		}
		else if (!team.hasStatus(p, EnumTeamStatus.OWNER))
		{
			throw FTBLibLang.TEAM_NOT_OWNER.commandError();
		}

		checkArgs(args, 1, "<player>");

		IForgePlayer p1 = getForgePlayer(args[0]);

		if (!team.equals(p1.getTeam()))
		{
			throw FTBLibLang.TEAM_NOT_MEMBER.commandError(p1.getName());
		}

		team.changeOwner(p1);
		team.printMessage(new ForgeTeam.Message(FTBLibLang.TEAM_TRANSFERRED_OWNERSHIP.textComponent(p1.getName())));
	}
}
