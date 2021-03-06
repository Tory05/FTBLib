package com.feed_the_beast.ftbl.lib.client;

import com.feed_the_beast.ftbl.api.gui.IDrawableObject;
import com.feed_the_beast.ftbl.lib.Color4I;
import com.feed_the_beast.ftbl.lib.gui.GuiHelper;
import com.feed_the_beast.ftbl.lib.gui.Widget;
import com.feed_the_beast.ftbl.lib.item.ItemStackSerializer;
import com.google.common.base.Objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class ImageProvider implements IDrawableObject
{
	public static final ImageProvider NULL = new ImageProvider("textures/misc/unknown_pack.png", 0D, 0D, 1D, 1D)
	{
		@Override
		public boolean isNull()
		{
			return true;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void draw(int x, int y, int w, int h, Color4I col)
		{
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void draw(Widget widget, Color4I col)
		{
		}
	};

	public static IDrawableObject get(JsonElement json)
	{
		if (json.isJsonNull())
		{
			return NULL;
		}
		else if (json.isJsonObject())
		{
			JsonObject o = json.getAsJsonObject();

			if (o.has("id"))
			{
				switch (o.get("id").getAsString())
				{
					case "colored":
						return new ColoredObject(get(o.get("parent").getAsJsonObject()), Color4I.fromJson(o.get("color")));
					case "animation":
					{
						List<IDrawableObject> icons = new ArrayList<>();

						for (JsonElement e : o.get("icons").getAsJsonArray())
						{
							icons.add(get(e));
						}

						IconAnimation list = new IconAnimation(icons);

						if (o.has("timer"))
						{
							list.timer = o.get("timer").getAsLong();
						}

						return list;
					}
				}
			}
		}
		else if (json.isJsonArray())
		{
			List<IDrawableObject> list = new ArrayList<>();

			for (JsonElement e : json.getAsJsonArray())
			{
				list.add(get(e));
			}

			return list.isEmpty() ? NULL : new CombinedIcon(list);
		}

		return get(json.getAsString());
	}

	public static IDrawableObject get(String id)
	{
		if (id.isEmpty())
		{
			return NULL;
		}
		else if (id.startsWith("item:"))
		{
			ItemStack stack = ItemStackSerializer.parseItem(id.substring(5));

			if (!stack.isEmpty())
			{
				return new DrawableItem(stack);
			}
		}
		else if (id.startsWith("http:") || id.startsWith("https:"))
		{
			return new URLImageProvider(id, 0D, 0D, 1D, 1D);
		}

		if (!id.endsWith(".png"))
		{
			return new AtlasSpriteProvider(new ResourceLocation(id));
		}

		return new ImageProvider(id, 0D, 0D, 1D, 1D);
	}

	public final ResourceLocation texture;
	public final double minU, minV, maxU, maxV;

	ImageProvider(String tex, double u0, double v0, double u1, double v1)
	{
		texture = new ResourceLocation(tex);
		minU = Math.min(u0, u1);
		minV = Math.min(v0, v1);
		maxU = Math.max(u0, u1);
		maxV = Math.max(v0, v1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ITextureObject bindTexture()
	{
		return ClientUtils.bindTexture(texture);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void draw(int x, int y, int w, int h, Color4I col)
	{
		bindTexture();
		GuiHelper.drawTexturedRect(x, y, w, h, col.hasColor() ? col : Color4I.WHITE, minU, minV, maxU, maxV);
	}

	@Override
	public JsonElement getJson()
	{
		return new JsonPrimitive(texture.toString());
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		else if (o instanceof ImageProvider)
		{
			ImageProvider img = (ImageProvider) o;
			return texture.equals(img.texture) && minU == img.minU && minV == img.minV && maxU == img.maxU && maxV == img.maxV;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(texture, minU, minV, maxU, maxV);
	}

	@Override
	public String toString()
	{
		return Double.toString(minU) + ',' + minV + ',' + maxU + ',' + maxV;
	}

	@Override
	public IDrawableObject withUV(double u0, double v0, double u1, double v1)
	{
		return new ImageProvider(texture.toString(), u0, v0, u1, v1);
	}
}