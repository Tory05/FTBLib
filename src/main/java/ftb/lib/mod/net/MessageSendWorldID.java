package ftb.lib.mod.net;

import ftb.lib.*;
import ftb.lib.api.EventFTBWorldClient;
import ftb.lib.api.net.*;
import latmod.lib.ByteCount;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public class MessageSendWorldID extends MessageLM_IO
{
	public MessageSendWorldID() { super(ByteCount.INT); }
	
	public MessageSendWorldID(FTBWorld w, EntityPlayerMP ep)
	{
		this();
		MessageReload.writeSyncedConfig(io);
		io.writeBoolean(FTBLib.ftbu != null);
		w.writeReloadData(io);
		if(FTBLib.ftbu != null) FTBLib.ftbu.writeWorldData(io, ep);
	}
	
	public LMNetworkWrapper getWrapper()
	{ return FTBLibNetHandler.NET; }
	
	public IMessage onMessage(MessageContext ctx)
	{
		MessageReload.readSyncedConfig(io);
		boolean hasFTBU = io.readBoolean();
		
		boolean first = FTBWorld.client == null;
		if(first) FTBWorld.client = new FTBWorld();
		FTBWorld.client.readReloadData(io);
		new EventFTBWorldClient(FTBWorld.client).post();
		if(first && hasFTBU && FTBLib.ftbu != null) FTBLib.ftbu.readWorldData(io);
		
		MessageReload.reloadClient(0L, false);
		return null;
	}
}