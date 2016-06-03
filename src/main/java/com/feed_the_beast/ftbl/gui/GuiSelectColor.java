package com.feed_the_beast.ftbl.gui;

import com.feed_the_beast.ftbl.api.MouseButton;
import com.feed_the_beast.ftbl.api.client.FTBLibClient;
import com.feed_the_beast.ftbl.api.client.gui.GuiLM;
import com.feed_the_beast.ftbl.api.client.gui.GuiLang;
import com.feed_the_beast.ftbl.api.client.gui.widgets.ButtonLM;
import com.feed_the_beast.ftbl.api.client.gui.widgets.SliderLM;
import com.feed_the_beast.ftbl.api.client.gui.widgets.WidgetLM;
import com.feed_the_beast.ftbl.util.EnumDyeColorHelper;
import com.feed_the_beast.ftbl.util.TextureCoords;
import latmod.lib.LMColor;
import latmod.lib.LMColorUtils;
import latmod.lib.MathHelperLM;
import latmod.lib.ObjectCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiSelectColor extends GuiLM
{
    public static final ResourceLocation tex = new ResourceLocation("ftbl", "textures/gui/colselector.png");
    public static final ResourceLocation tex_wheel = new ResourceLocation("ftbl", "textures/gui/colselector_wheel.png");

    public static final TextureCoords col_tex = new TextureCoords(tex, 145, 10, 29, 16);
    public static final TextureCoords cursor_tex = new TextureCoords(tex, 145, 36, 8, 8);

    public static final int SLIDER_W = 6, SLIDER_H = 10, SLIDER_BAR_W = 64;
    public static final TextureCoords slider_tex = new TextureCoords(tex, 145, 26, SLIDER_W, SLIDER_H);
    public static final TextureCoords slider_col_tex = new TextureCoords(tex, 145, 0, SLIDER_BAR_W, SLIDER_H);

    public static class ColorSelector extends WidgetLM
    {
        public final GuiSelectColor gui;
        public boolean grabbed = false;
        public double cursorPosX = 0D;
        public double cursorPosY = 0D;

        public ColorSelector(GuiSelectColor g, int x, int y, int w, int h)
        {
            super(g, x, y, w, h);
            gui = g;
            cursorPosX = cursorPosY = -1D;
        }

        @Override
        public void renderWidget()
        {
            int ax = getAX();
            int ay = getAY();

            if(grabbed && !Mouse.isButtonDown(0))
            {
                grabbed = false;
            }

            if(grabbed)
            {
                cursorPosX = (gui.mouse().x - ax) / (double) width;
                cursorPosY = (gui.mouse().y - ay) / (double) height;

                double s = MathHelperLM.dist(cursorPosX, cursorPosY, 0D, 0.5D, 0.5D, 0D) * 2D;

                if(s > 1D)
                {
                    cursorPosX = (cursorPosX - 0.5D) / s + 0.5D;
                    cursorPosY = (cursorPosY - 0.5D) / s + 0.5D;
                    s = 1D;
                }

                cursorPosX = MathHelperLM.clamp(cursorPosX, 0D, 1D);
                cursorPosY = MathHelperLM.clamp(cursorPosY, 0D, 1D);

                double h = Math.atan2(cursorPosY - 0.5D, cursorPosX - 0.5D) / MathHelperLM.TWO_PI;

                gui.setColor(new LMColor.HSB((float) h, (float) s, gui.sliderBrightness.value));
            }

            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
            FTBLibClient.setTexture(tex_wheel);
            GuiLM.drawTexturedRectD(ax, ay, gui.zLevel, width, height, 0D, 0D, 1D, 1D);

            if(cursorPosX >= 0D && cursorPosY >= 0D)
            {
                GlStateManager.color(1F - gui.sliderRed.value, 1F - gui.sliderGreen.value, 1F - gui.sliderBlue.value, 1F);
                GuiLM.render(cursor_tex, ax + cursorPosX * width - 2, ay + cursorPosY * height - 2, gui.getZLevel(), 4, 4);
                GlStateManager.color(1F, 1F, 1F, 1F);
            }
        }

        @Override
        public void mousePressed(MouseButton b)
        {
            if(b.isLeft() && mouseOver())
            {
                grabbed = true;
            }
        }
    }

    public final ObjectCallback.Handler callback;
    public final LMColor.HSB initCol;
    public final Object colorID;
    public final boolean isInstant;
    public final LMColor currentColor;
    public final ButtonLM colorInit, colorCurrent;
    public final SliderLM sliderRed, sliderGreen, sliderBlue;
    public final SliderLM sliderHue, sliderSaturation, sliderBrightness;
    public final ColorSelector colorSelector;

    public GuiSelectColor(ObjectCallback.Handler cb, LMColor col, Object id, boolean instant)
    {
        super(null, tex);
        callback = cb;
        initCol = new LMColor.HSB();
        initCol.set(col);
        currentColor = new LMColor.RGB();
        colorID = id;
        isInstant = instant;

        mainPanel.width = 143;
        mainPanel.height = 93;

        colorInit = new ButtonLM(this, 76, 71, col_tex.widthI(), col_tex.heightI())
        {
            @Override
            public void onClicked(MouseButton button)
            {
                closeGui(false);
            }

            @Override
            public void addMouseOverText(List<String> s)
            {
                s.add(GuiLang.button_cancel.translate());
                s.add(initCol.toString());
            }
        };

        colorCurrent = new ButtonLM(this, 109, 71, col_tex.widthI(), col_tex.heightI())
        {
            @Override
            public void onClicked(MouseButton button)
            {
                closeGui(true);
            }

            @Override
            public void addMouseOverText(List<String> s)
            {
                s.add(GuiLang.button_accept.translate());
                s.add(currentColor.toString());
            }
        };

        sliderRed = new SliderLM(this, 6, 6, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.RGB((int) (value * 255F), currentColor.green(), currentColor.blue()));
            }
        };
        sliderRed.displayMax = 255;
        sliderRed.title = EnumDyeColorHelper.get(EnumDyeColor.RED).toString();
        sliderRed.scrollStep = 1F / 255F;

        sliderGreen = new SliderLM(this, 6, 19, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.RGB(currentColor.red(), (int) (value * 255F), currentColor.blue()));
            }
        };

        sliderGreen.displayMax = 255;
        sliderGreen.title = EnumDyeColorHelper.get(EnumDyeColor.GREEN).toString();
        sliderGreen.scrollStep = 1F / 255F;

        sliderBlue = new SliderLM(this, 6, 32, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.RGB(currentColor.red(), currentColor.green(), (int) (value * 255F)));
            }
        };

        sliderBlue.displayMax = 255;
        sliderBlue.title = EnumDyeColorHelper.get(EnumDyeColor.BLUE).toString();
        sliderBlue.scrollStep = 1F / 255F;

        sliderHue = new SliderLM(this, 6, 51, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.HSB(value, currentColor.saturation(), currentColor.brightness()));
            }
        };

        sliderHue.displayMax = 255;
        sliderHue.title = "Hue";
        sliderHue.scrollStep = 1F / 255F;

        sliderSaturation = new SliderLM(this, 6, 64, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.HSB(currentColor.hue(), value, currentColor.brightness()));
            }
        };
        sliderSaturation.displayMax = 255;
        sliderSaturation.title = "Saturation";
        sliderSaturation.scrollStep = 1F / 255F;

        sliderBrightness = new SliderLM(this, 6, 77, SLIDER_BAR_W, SLIDER_H, SLIDER_W)
        {
            @Override
            public void onMoved()
            {
                setColor(new LMColor.HSB(currentColor.hue(), currentColor.saturation(), value));
            }
        };
        sliderBrightness.displayMax = 255;
        sliderBrightness.title = "Brightness";
        sliderBrightness.scrollStep = 1F / 255F;

        colorSelector = new ColorSelector(this, 75, 5, 64, 64);

        setColor(initCol);
    }

    @Override
    public void addWidgets()
    {
        mainPanel.add(colorInit);
        mainPanel.add(colorCurrent);

        mainPanel.add(sliderRed);
        mainPanel.add(sliderGreen);
        mainPanel.add(sliderBlue);

        mainPanel.add(sliderHue);
        mainPanel.add(sliderSaturation);
        mainPanel.add(sliderBrightness);

        mainPanel.add(colorSelector);
    }

    public void setColor(LMColor col)
    {
        if((0xFF000000 | currentColor.color()) == (0xFF000000 | col.color()))
        {
            return;
        }
        currentColor.set(col);

        sliderRed.value = currentColor.red() / 255F;
        sliderGreen.value = currentColor.green() / 255F;
        sliderBlue.value = currentColor.blue() / 255F;

        sliderHue.value = currentColor.hue();
        sliderSaturation.value = currentColor.saturation();
        sliderBrightness.value = currentColor.brightness();

        colorSelector.cursorPosX = (Math.cos(sliderHue.value * MathHelperLM.TWO_PI) * 0.5D) * sliderSaturation.value + 0.5D;
        colorSelector.cursorPosY = (Math.sin(sliderHue.value * MathHelperLM.TWO_PI) * 0.5D) * sliderSaturation.value + 0.5D;

        if(isInstant)
        {
            callback.onCallback(new ObjectCallback(colorID, true, false, currentColor));
        }
    }

    @Override
    public void drawBackground()
    {
        sliderRed.update();
        sliderGreen.update();
        sliderBlue.update();

        sliderHue.update();
        sliderSaturation.update();
        sliderBrightness.update();

        super.drawBackground();

        FTBLibClient.setGLColor(initCol.color(), 255);
        colorInit.render(col_tex);
        FTBLibClient.setGLColor(currentColor.color(), 255);
        colorCurrent.render(col_tex);
        GlStateManager.color(1F, 1F, 1F, 1F);

        FTBLibClient.setTexture(tex);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        LMColor col1 = new LMColor.RGB();
        LMColor col2 = new LMColor.RGB();

        col1.setRGBA(0, currentColor.green(), currentColor.blue(), 255);
        col2.setRGBA(255, currentColor.green(), currentColor.blue(), 255);
        renderSlider(sliderRed, col1.color(), col2.color());

        col1.setRGBA(currentColor.red(), 0, currentColor.blue(), 255);
        col2.setRGBA(currentColor.red(), 255, currentColor.blue(), 255);
        renderSlider(sliderGreen, col1.color(), col2.color());

        col1.setRGBA(currentColor.red(), currentColor.green(), 0, 255);
        col2.setRGBA(currentColor.red(), currentColor.green(), 255, 255);
        renderSlider(sliderBlue, col1.color(), col2.color());

        col1 = new LMColor.HSB();
        col2 = new LMColor.HSB();

        col1.setHSB(currentColor.hue(), currentColor.saturation(), currentColor.brightness());
        col2.setHSB(currentColor.hue(), currentColor.saturation(), currentColor.brightness());
        renderSlider(sliderHue, col1.color(), col2.color());

        col1.setHSB(currentColor.hue(), 0F, currentColor.brightness());
        col2.setHSB(currentColor.hue(), 1F, currentColor.brightness());
        renderSlider(sliderSaturation, col1.color(), col2.color());

        col1.setHSB(currentColor.hue(), currentColor.saturation(), 0F);
        col2.setHSB(currentColor.hue(), currentColor.saturation(), 1F);
        renderSlider(sliderBrightness, col1.color(), col2.color());

        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.shadeModel(GL11.GL_FLAT);

        colorSelector.renderWidget();

        sliderRed.renderSlider(slider_tex);
        sliderGreen.renderSlider(slider_tex);
        sliderBlue.renderSlider(slider_tex);

        sliderHue.renderSlider(slider_tex);
        sliderSaturation.renderSlider(slider_tex);
        sliderBrightness.renderSlider(slider_tex);
    }

    public void renderSlider(WidgetLM widget, int colLeft, int colRight)
    {
        int x = widget.getAX();
        int y = widget.getAY();
        double w = widget.width;
        double h = widget.height;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        int red = LMColorUtils.getRed(colLeft);
        int green = LMColorUtils.getGreen(colLeft);
        int blue = LMColorUtils.getBlue(colLeft);
        buffer.pos(x, y, zLevel).tex(slider_col_tex.minU, slider_col_tex.minV).color(red, green, blue, 255).endVertex();
        buffer.pos(x, y + h, zLevel).tex(slider_col_tex.minU, slider_col_tex.maxV).color(red, green, blue, 255).endVertex();
        red = LMColorUtils.getRed(colRight);
        green = LMColorUtils.getGreen(colRight);
        blue = LMColorUtils.getBlue(colRight);
        buffer.pos(x + w, y + h, zLevel).tex(slider_col_tex.maxU, slider_col_tex.maxV).color(red, green, blue, 255).endVertex();
        buffer.pos(x + w, y, zLevel).tex(slider_col_tex.maxU, slider_col_tex.minV).color(red, green, blue, 255).endVertex();
        tessellator.draw();
    }

    public void closeGui(boolean set)
    {
        FTBLibClient.playClickSound();
        callback.onCallback(new ObjectCallback(colorID, set, true, set ? currentColor : initCol));
    }
}