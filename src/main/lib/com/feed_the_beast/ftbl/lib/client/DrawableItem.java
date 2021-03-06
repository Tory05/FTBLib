package com.feed_the_beast.ftbl.lib.client;

import com.feed_the_beast.ftbl.api.gui.IDrawableObject;
import com.feed_the_beast.ftbl.lib.Color4I;
import com.feed_the_beast.ftbl.lib.gui.GuiHelper;
import com.feed_the_beast.ftbl.lib.item.ItemStackSerializer;
import com.feed_the_beast.ftbl.lib.util.InvUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.item.ItemStack;

/**
 * @author LatvianModder
 */
public class DrawableItem implements IDrawableObject
{
	public ItemStack stack;

	public DrawableItem(ItemStack is)
	{
		stack = is;
	}

	@Override
	public void draw(int x, int y, int w, int h, Color4I col)
	{
		if (!GuiHelper.drawItem(stack, x, y, w / 16D, h / 16D, true, col))
		{
			stack = InvUtils.ERROR_ITEM;
		}
	}

	@Override
	public JsonElement getJson()
	{
		return new JsonPrimitive("item:" + ItemStackSerializer.toString(stack));
	}
}