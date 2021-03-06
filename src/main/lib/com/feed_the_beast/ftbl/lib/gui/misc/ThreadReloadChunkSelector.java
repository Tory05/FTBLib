package com.feed_the_beast.ftbl.lib.gui.misc;

import com.feed_the_beast.ftbl.lib.client.ClientUtils;
import com.feed_the_beast.ftbl.lib.client.PixelBuffer;
import com.feed_the_beast.ftbl.lib.util.ColorUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ThreadReloadChunkSelector extends Thread
{
	private static ByteBuffer pixelBuffer = null;
	private static final PixelBuffer PIXELS = new PixelBuffer(GuiConfigs.CHUNK_SELECTOR_TILES_TEX * 16, GuiConfigs.CHUNK_SELECTOR_TILES_TEX * 16);
	private static final Map<IBlockState, Integer> COLOR_CACHE = new HashMap<>();
	private static final BlockPos.MutableBlockPos CURRENT_BLOCK_POS = new BlockPos.MutableBlockPos(0, 0, 0);
	public final World world;
	private final int startX, startZ;
	private boolean cancelled = false;
	private static ThreadReloadChunkSelector instance;
	private static int textureID = -1;

	public static int getTextureID()
	{
		if (textureID == -1)
		{
			textureID = TextureUtil.glGenTextures();
		}

		return textureID;
	}

	public static void updateTexture()
	{
		if (pixelBuffer != null)
		{
			//boolean hasBlur = false;
			//int filter = hasBlur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
			GlStateManager.bindTexture(getTextureID());
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, GuiConfigs.CHUNK_SELECTOR_TILES_TEX * 16, GuiConfigs.CHUNK_SELECTOR_TILES_TEX * 16, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
			pixelBuffer = null;
		}
	}

	public static void reloadArea(World w, int sx, int sz)
	{
		if (instance != null)
		{
			instance.cancelled = true;
			instance = null;
		}

		instance = new ThreadReloadChunkSelector(w, sx, sz);
		instance.cancelled = false;
		instance.start();
	}

	public static boolean isReloading()
	{
		return instance != null && !instance.cancelled;
	}

	private ThreadReloadChunkSelector(World w, int sx, int sz)
	{
		super("ChunkSelectorAreaReloader");
		setDaemon(true);
		world = w;
		startX = sx;
		startZ = sz;
	}

	private static int getBlockColor0(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		Block b = state.getBlock();

		if (b == Blocks.SANDSTONE)
		{
			return MapColor.SAND.colorValue;
		}
		else if (b == Blocks.FIRE)
		{
			return MapColor.RED.colorValue;
		}
		else if (b == Blocks.YELLOW_FLOWER)
		{
			return MapColor.YELLOW.colorValue;
		}
		else if (b == Blocks.LAVA)
		{
			return MapColor.ADOBE.colorValue;
		}
		else if (b == Blocks.END_STONE)
		{
			return MapColor.SAND.colorValue;
		}
		else if (b == Blocks.OBSIDIAN)
		{
			return 0x150047;
		}
		else if (b == Blocks.GRAVEL)
		{
			return 0x8D979B;
		}
		else if (b == Blocks.GRASS)
		{
			return 0x74BC7C;
		}
		else if (b == Blocks.TORCH)
		{
			return 0xFFA530;
		}
		//else if(b.getMaterial(state) == Material.water)
		//	return ColorUtils.multiply(MapColor.waterColor.colorValue, b.colorMultiplier(world, pos), 200);
		else if (b == Blocks.RED_FLOWER)
		{
			switch (state.getValue(Blocks.RED_FLOWER.getTypeProperty()))
			{
				case DANDELION:
					return MapColor.YELLOW.colorValue;
				case POPPY:
					return MapColor.RED.colorValue;
				case BLUE_ORCHID:
					return MapColor.LIGHT_BLUE.colorValue;
				case ALLIUM:
					return MapColor.MAGENTA.colorValue;
				case HOUSTONIA:
					return MapColor.SILVER.colorValue;
				case RED_TULIP:
					return MapColor.RED.colorValue;
				case ORANGE_TULIP:
					return MapColor.ADOBE.colorValue;
				case WHITE_TULIP:
					return MapColor.SNOW.colorValue;
				case PINK_TULIP:
					return MapColor.PINK.colorValue;
				case OXEYE_DAISY:
					return MapColor.SILVER.colorValue;
			}
		}
		else if (b == Blocks.PLANKS)
		{
			switch (state.getValue(BlockPlanks.VARIANT))
			{
				case OAK:
					return 0xC69849;
				case SPRUCE:
					return 0x7C5E2E;
				case BIRCH:
					return 0xF2E093;
				case JUNGLE:
					return 0xC67653;
				case ACACIA:
					return 0xE07F3E;
				case DARK_OAK:
					return 0x512D14;
			}
		}

		//if(b == Blocks.leaves || b == Blocks.vine || b == Blocks.waterlily)
		//	return ColorUtils.addBrightness(b.colorMultiplier(world, pos), -40);
		//else if(b == Blocks.grass && state.getValue(BlockGrass.SNOWY))
		//	return ColorUtils.addBrightness(b.colorMultiplier(world, pos), -15);

		return state.getMapColor(world, pos).colorValue;
	}

	@Override
	public void run()
	{
		Arrays.fill(PIXELS.getPixels(), 0);
		pixelBuffer = ColorUtils.toByteBuffer(PIXELS.getPixels(), false);

		Chunk chunk;
		int cx, cz, x, z, wx, wz, by, color, topY;
		boolean depth = GuiConfigs.ENABLE_CHUNK_SELECTOR_DEPTH.getBoolean();

		int startY = ClientUtils.MC.player.getPosition().getY();

		try
		{
			for (cz = 0; cz < GuiConfigs.CHUNK_SELECTOR_TILES_GUI; cz++)
			{
				for (cx = 0; cx < GuiConfigs.CHUNK_SELECTOR_TILES_GUI; cx++)
				{
					chunk = world.getChunkProvider().getLoadedChunk(startX + cx, startZ + cz);

					if (chunk != null)
					{
						x = (startX + cx) << 4;
						z = (startZ + cz) << 4;
						topY = Math.max(255, chunk.getTopFilledSegment() + 15);

						for (wz = 0; wz < 16; wz++)
						{
							for (wx = 0; wx < 16; wx++)
							{
								for (by = topY; by > 0; --by)
								{
									if (cancelled)
									{
										return;
									}

									IBlockState state = chunk.getBlockState(wx, by, wz);

									CURRENT_BLOCK_POS.setPos(x + wx, by, z + wz);

									if (state.getBlock() != Blocks.TALLGRASS && !world.isAirBlock(CURRENT_BLOCK_POS))
									{
										color = COLOR_CACHE.computeIfAbsent(state, state1 -> 0xFF000000 | getBlockColor0(state, world, CURRENT_BLOCK_POS));

										if (depth)
										{
											color = ColorUtils.addBrightness(color, MathHelper.clamp(by - startY, -30, 30) * 5);
										}

										PIXELS.setRGB(cx * 16 + wx, cz * 16 + wz, color);
										break;
									}
								}
							}
						}
					}

					pixelBuffer = ColorUtils.toByteBuffer(PIXELS.getPixels(), false);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		pixelBuffer = ColorUtils.toByteBuffer(PIXELS.getPixels(), false);
		instance = null;
	}
}