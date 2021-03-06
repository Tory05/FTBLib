package com.feed_the_beast.ftbl.api_impl;

import com.feed_the_beast.ftbl.FTBLibMod;
import com.feed_the_beast.ftbl.FTBLibModCommon;
import com.feed_the_beast.ftbl.api.EnumReloadType;
import com.feed_the_beast.ftbl.api.FTBLibAPI;
import com.feed_the_beast.ftbl.api.IForgePlayer;
import com.feed_the_beast.ftbl.api.IRankConfig;
import com.feed_the_beast.ftbl.api.ISharedClientData;
import com.feed_the_beast.ftbl.api.ISharedServerData;
import com.feed_the_beast.ftbl.api.IUniverse;
import com.feed_the_beast.ftbl.api.config.IConfigContainer;
import com.feed_the_beast.ftbl.api.config.IConfigValue;
import com.feed_the_beast.ftbl.api.config.IConfigValueProvider;
import com.feed_the_beast.ftbl.api.events.LoadWorldDataEvent;
import com.feed_the_beast.ftbl.api.events.ReloadEvent;
import com.feed_the_beast.ftbl.api.gui.IContainerProvider;
import com.feed_the_beast.ftbl.client.FTBLibClientConfig;
import com.feed_the_beast.ftbl.lib.BroadcastSender;
import com.feed_the_beast.ftbl.lib.Notification;
import com.feed_the_beast.ftbl.lib.guide.GuidePage;
import com.feed_the_beast.ftbl.lib.internal.FTBLibFinals;
import com.feed_the_beast.ftbl.lib.internal.FTBLibLang;
import com.feed_the_beast.ftbl.lib.net.MessageBase;
import com.feed_the_beast.ftbl.lib.util.CommonUtils;
import com.feed_the_beast.ftbl.lib.util.ServerUtils;
import com.feed_the_beast.ftbl.lib.util.StringUtils;
import com.feed_the_beast.ftbl.net.MessageDisplayGuide;
import com.feed_the_beast.ftbl.net.MessageEditConfig;
import com.feed_the_beast.ftbl.net.MessageOpenGui;
import com.feed_the_beast.ftbl.net.MessageReload;
import com.google.common.base.Preconditions;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class FTBLibAPI_Impl extends FTBLibAPI
{
	public static final boolean LOG_NET = System.getProperty("ftbl.logNetwork", "0").equals("1");

	@Override
	public Collection<ITickable> ticking()
	{
		return TickHandler.INSTANCE.TICKABLES;
	}

	@Override
	public ISharedServerData getServerData()
	{
		return SharedServerData.INSTANCE;
	}

	@Override
	public ISharedClientData getClientData()
	{
		return SharedClientData.INSTANCE;
	}

	@Override
	public IUniverse getUniverse()
	{
		Preconditions.checkNotNull(Universe.INSTANCE);
		return Universe.INSTANCE;
	}

	@Override
	public void addServerCallback(int timer, Runnable runnable)
	{
		TickHandler.INSTANCE.addServerCallback(timer, runnable);
	}

	@Override
	public void loadWorldData(MinecraftServer server)
	{
		new LoadWorldDataEvent(server).post();
	}

	@Override
	public void reload(Side side, ICommandSender sender, EnumReloadType type, ResourceLocation id)
	{
		long ms = System.currentTimeMillis();
		boolean serverSide = side.isServer();

		if (serverSide)
		{
			Preconditions.checkNotNull(Universe.INSTANCE, "Can't reload yet!");
			FTBLibMod.PROXY.reloadConfig(LoaderState.ModState.AVAILABLE);
		}

		HashSet<ResourceLocation> failed = new HashSet<>();
		new ReloadEvent(side, sender, type, id, failed).post();

		if (serverSide && ServerUtils.hasOnlinePlayers())
		{
			for (EntityPlayerMP ep : ServerUtils.getServer().getPlayerList().getPlayers())
			{
				NBTTagCompound syncData = new NBTTagCompound();
				IForgePlayer p = Universe.INSTANCE.getPlayer(ep);
				FTBLibModCommon.SYNCED_DATA.forEach((key, value) -> syncData.setTag(key, value.writeSyncData(ep, p)));
				new MessageReload(type, syncData, id).sendTo(ep);
			}
		}

		String millis = (System.currentTimeMillis() - ms) + "ms";

		if (type != EnumReloadType.CREATED)
		{
			if (!serverSide)
			{
				FTBLibLang.RELOAD_CLIENT.printChat(BroadcastSender.INSTANCE, millis);
			}

			if (serverSide && type == EnumReloadType.RELOAD_COMMAND)
			{
				Notification notification = Notification.of(FTBLibFinals.get("reload_client_config"));
				notification.addLine(FTBLibLang.RELOAD_SERVER.textComponent(millis));
				String cmd = FTBLibClientConfig.MIRROR_COMMANDS.getBoolean() ? "/reload_client" : "/ftbc reload_client";
				notification.addLine(FTBLibLang.RELOAD_CLIENT_CONFIG.textComponent(StringUtils.color(new TextComponentString(cmd), TextFormatting.GOLD)));

				notification.setTimer(140);
				notification.send(null);
			}
		}

		FTBLibFinals.LOGGER.info("Reloaded " + side + " in " + millis);
	}

	@Override
	public void openGui(ResourceLocation guiId, EntityPlayerMP player, BlockPos pos, @Nullable NBTTagCompound data)
	{
		IContainerProvider containerProvider = FTBLibModCommon.GUI_CONTAINER_PROVIDERS.get(guiId);

		if (containerProvider == null)
		{
			return;
		}

		Container c = containerProvider.getContainer(player, pos, data);

		player.getNextWindowId();
		player.closeContainer();

		if (c != null)
		{
			player.openContainer = c;
		}

		player.openContainer.windowId = player.currentWindowId;
		player.openContainer.addListener(player);
		new MessageOpenGui(guiId, pos, data, player.currentWindowId).sendTo(player);
	}

	@Override
	public void editServerConfig(EntityPlayerMP player, @Nullable NBTTagCompound nbt, IConfigContainer configContainer)
	{
		new MessageEditConfig(player.getGameProfile().getId(), nbt, configContainer).sendTo(player);
	}

	@Override
	public void displayGuide(EntityPlayer player, GuidePage page)
	{
		if (player.world.isRemote)
		{
			FTBLibMod.PROXY.displayGuide(page);
		}
		else
		{
			new MessageDisplayGuide(page).sendTo(player);
		}
	}

	@Override
	public IConfigValue getConfigValueFromID(String id)
	{
		IConfigValueProvider provider = FTBLibModCommon.CONFIG_VALUE_PROVIDERS.get(id);
		Preconditions.checkNotNull(provider, "Unknown Config ID: " + id);
		return provider.createConfigValue();
	}

	@Override
	public Map<String, IRankConfig> getRankConfigRegistry()
	{
		return FTBLibModCommon.RANK_CONFIGS_MIRROR;
	}

	@Override
	public void handleMessage(MessageBase<?> message, MessageContext context, Side side)
	{
		if (side.isServer())
		{
			context.getServerHandler().player.mcServer.addScheduledTask(() ->
			{
				message.onMessage(CommonUtils.cast(message), context.getServerHandler().player);

				if (LOG_NET)
				{
					CommonUtils.DEV_LOGGER.info("TX MessageBase: " + message.getClass().getName());
				}
			});
		}
		else
		{
			FTBLibMod.PROXY.handleClientMessage(message);
		}
	}
}