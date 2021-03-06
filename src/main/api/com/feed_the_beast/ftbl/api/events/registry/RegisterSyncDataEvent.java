package com.feed_the_beast.ftbl.api.events.registry;

import com.feed_the_beast.ftbl.api.ISyncData;
import com.feed_the_beast.ftbl.api.events.FTBLibEvent;

import java.util.function.BiConsumer;

/**
 * @author LatvianModder
 */
public class RegisterSyncDataEvent extends FTBLibEvent
{
	private final BiConsumer<String, ISyncData> callback;

	public RegisterSyncDataEvent(BiConsumer<String, ISyncData> c)
	{
		callback = c;
	}

	public void register(String mod, ISyncData data)
	{
		callback.accept(mod, data);
	}
}