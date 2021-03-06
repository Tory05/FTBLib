package com.feed_the_beast.ftbl.lib.guide;

import com.feed_the_beast.ftbl.api.guide.IGuideTextLine;
import com.feed_the_beast.ftbl.lib.Color4I;
import com.feed_the_beast.ftbl.lib.MutableColor4I;
import com.feed_the_beast.ftbl.lib.gui.GuiBase;
import com.feed_the_beast.ftbl.lib.gui.GuiHelper;
import com.feed_the_beast.ftbl.lib.gui.Panel;
import com.feed_the_beast.ftbl.lib.gui.Widget;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author LatvianModder
 */
public class GuideHrLine extends EmptyGuidePageLine
{
	public final int height;
	public final MutableColor4I color;

	public GuideHrLine(int h, Color4I c)
	{
		height = h;
		color = c.mutable();
	}

	public GuideHrLine(JsonElement e)
	{
		JsonObject o = e.getAsJsonObject();
		height = o.has("height") ? Math.max(1, o.get("height").getAsInt()) : 1;
		color = Color4I.fromJson(o.get("color")).mutable();
	}

	@Override
	public Widget createWidget(GuiBase gui, Panel parent)
	{
		return new WidgetGuideHr(parent);
	}

	@Override
	public IGuideTextLine copy(GuidePage page)
	{
		return new GuideHrLine(height, color);
	}

	@Override
	public JsonElement getJson()
	{
		JsonObject o = new JsonObject();
		o.addProperty("id", "hr");
		o.addProperty("height", height);
		o.add("color", color.toJson());
		return o;
	}

	private class WidgetGuideHr extends Widget
	{
		private WidgetGuideHr(Panel parent)
		{
			super(0, 1, parent.width, GuideHrLine.this.height + 2);
		}

		@Override
		public void renderWidget(GuiBase gui)
		{
			GuiHelper.drawBlankRect(getAX(), getAY() + 1, width, GuideHrLine.this.height, color.hasColor() ? color : gui.getContentColor());
		}
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}
}