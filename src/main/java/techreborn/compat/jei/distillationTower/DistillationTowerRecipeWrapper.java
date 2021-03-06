/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.compat.jei.distillationTower;

import javax.annotation.Nonnull;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import reborncore.common.powerSystem.PowerSystem;
import techreborn.api.recipe.machines.DistillationTowerRecipe;
import techreborn.client.gui.TRBuilder;
import techreborn.compat.jei.BaseRecipeWrapper;

/**
 * @author drcrazy
 *
 */
public class DistillationTowerRecipeWrapper extends BaseRecipeWrapper<DistillationTowerRecipe> {
	private final IDrawableAnimated progress;
	
	/**
	 * @param jeiHelpers JEI Helper Class
	 * @param baseRecipe Distillation Tower recipe
	 */

	public DistillationTowerRecipeWrapper(@Nonnull IJeiHelpers jeiHelpers, @Nonnull	DistillationTowerRecipe baseRecipe) {
		super(baseRecipe);
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
		IDrawableStatic progressStatic = guiHelper.createDrawable(TRBuilder.GUI_SHEET, 100, 151, 16, 10);
		int ticksPerCycle = baseRecipe.tickTime() / 4; // speed up the animation
		this.progress = guiHelper.createAnimatedDrawable(progressStatic, ticksPerCycle, IDrawableAnimated.StartDirection.LEFT, false);
	}
	
	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		super.drawInfo(minecraft, recipeWidth, recipeHeight, mouseX, mouseY);
		progress.draw(minecraft, 25, 26);

		int y1 = 1;
		int y2 = 54;
		minecraft.fontRenderer.drawString(baseRecipe.tickTime / 20 + " seconds", (recipeWidth / 2 - minecraft.fontRenderer.getStringWidth(baseRecipe.tickTime / 20 + " seconds") / 2) - 40, y1, 0x444444);
		minecraft.fontRenderer.drawString(PowerSystem.getLocaliszedPowerFormatted(baseRecipe.euPerTick * baseRecipe.tickTime), (recipeWidth / 2 - minecraft.fontRenderer.getStringWidth(PowerSystem.getLocaliszedPowerFormatted(baseRecipe.euPerTick * baseRecipe.tickTime)) / 2) - 40, y2, 0x444444);

	}
	

}
