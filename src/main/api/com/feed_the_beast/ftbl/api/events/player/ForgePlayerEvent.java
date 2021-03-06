package com.feed_the_beast.ftbl.api.events.player;

import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.events.FTBLibEvent;

/**
 * @author LatvianModder
 */
public abstract class ForgePlayerEvent extends FTBLibEvent
{
	private final IForgePlayer player;

	public ForgePlayerEvent(IForgePlayer p)
	{
		player = p;
	}

	public IForgePlayer getPlayer()
	{
		return player;
	}
}