package com.feed_the_beast.ftbl.lib.config;

import com.feed_the_beast.ftbl.lib.Color4I;
import com.feed_the_beast.ftbl.lib.NameMap;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * @author LatvianModder
 */
public enum EnumTristate implements IStringSerializable
{
	TRUE("true", Event.Result.ALLOW, PropertyBool.COLOR_TRUE, 1),
	FALSE("false", Event.Result.DENY, PropertyBool.COLOR_FALSE, 0),
	DEFAULT("default", Event.Result.DEFAULT, PropertyEnumAbstract.COLOR, 2);

	public static final NameMap<EnumTristate> NAME_MAP = NameMap.create(DEFAULT, values());

	private final String name;
	private final Event.Result result;
	private final Color4I color;
	private final int opposite;

	EnumTristate(String s, Event.Result r, Color4I c, int o)
	{
		name = s;
		result = r;
		color = c;
		opposite = o;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public Event.Result getResult()
	{
		return result;
	}

	public Color4I getColor()
	{
		return color;
	}

	public boolean isTrue()
	{
		return this == TRUE;
	}

	public boolean isFalse()
	{
		return this == FALSE;
	}

	public boolean isDefault()
	{
		return this == DEFAULT;
	}

	public boolean get(boolean def)
	{
		return isDefault() ? def : isTrue();
	}

	public EnumTristate getOpposite()
	{
		return NAME_MAP.get(opposite);
	}
}