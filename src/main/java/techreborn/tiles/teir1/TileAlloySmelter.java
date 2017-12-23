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

package techreborn.tiles.teir1;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import reborncore.api.IToolDrop;
import reborncore.api.recipe.IRecipeCrafterProvider;
import reborncore.api.recipe.RecipeHandler;
import reborncore.api.tile.IInventoryProvider;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.registration.RebornRegistry;
import reborncore.common.registration.impl.ConfigRegistry;
import reborncore.common.util.Inventory;
import reborncore.common.util.ItemUtils;
import techreborn.api.Reference;
import techreborn.api.recipe.machines.AlloySmelterRecipe;
import techreborn.client.container.IContainerProvider;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.container.builder.ContainerBuilder;
import techreborn.init.ModBlocks;
import techreborn.lib.ModInfo;

@RebornRegistry(modID = ModInfo.MOD_ID)
public class TileAlloySmelter extends TilePowerAcceptor
	implements IToolDrop, IInventoryProvider, IRecipeCrafterProvider, IContainerProvider {

	@ConfigRegistry(config = "machines", category = "alloy_smelter", key = "AlloySmelterMaxInput", comment = "Alloy Smelter Max Input (Value in EU)")
	public static int maxInput = 32;
	@ConfigRegistry(config = "machines", category = "alloy_smelter", key = "AlloySmelterMaxEnergy", comment = "Alloy Smelter Max Energy (Value in EU)")
	public static int maxEnergy = 1000;
	//	@ConfigRegistry(config = "machines", category = "alloy_smelter", key = "AlloySmelterWrenchDropRate", comment = "Alloy Smelter Wrench Drop Rate")
	public static float wrenchDropRate = 1.0F;

	public int tickTime;
	public Inventory inventory = new Inventory(8, "TileAlloySmelter", 64, this);
	public RecipeCrafter crafter;

	public TileAlloySmelter() {
		super();
		// Input slots
		final int[] inputs = new int[2];
		inputs[0] = 0;
		inputs[1] = 1;
		final int[] outputs = new int[1];
		outputs[0] = 2;
		this.crafter = new RecipeCrafter(Reference.alloySmelterRecipe, this, 2, 1, this.inventory, inputs, outputs);
	}

	@Override
	public void update() {
		super.update();
		this.charge(3);
	}

	@Override
	public ItemStack getToolDrop(final EntityPlayer entityPlayer) {
		return new ItemStack(ModBlocks.ALLOY_SMELTER, 1);
	}

	public boolean isComplete() {
		return false;
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.inventory.readFromNBT(tagCompound);
		this.crafter.readFromNBT(tagCompound);
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		this.crafter.writeToNBT(tagCompound);
		return tagCompound;
	}

	public int getProgressScaled(final int scale) {
		if (this.crafter.currentTickTime != 0 && this.crafter.currentNeededTicks != 0) {
			return this.crafter.currentTickTime * scale / this.crafter.currentNeededTicks;
		}
		return 0;
	}

	@Override
	public double getBaseMaxPower() {
		return maxEnergy;
	}

	@Override
	public boolean canAcceptEnergy(final EnumFacing direction) {
		return true;
	}

	@Override
	public boolean canProvideEnergy(final EnumFacing direction) {
		return false;
	}

	@Override
	public double getBaseMaxOutput() {
		return 0;
	}

	@Override
	public double getBaseMaxInput() {
		return maxInput;
	}

	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	@Override
	public RecipeCrafter getRecipeCrafter() {
		return this.crafter;
	}

	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("alloysmelter").player(player.inventory).inventory().hotbar()
			.addInventory().tile(this)
			.filterSlot(0, 34, 47,
				stack -> RecipeHandler.recipeList.stream()
					.anyMatch(recipe -> recipe instanceof AlloySmelterRecipe
						&& ItemUtils.isInputEqual(recipe.getInputs().get(0), stack, true, true, true)))
			.filterSlot(1, 126, 47,
				stack -> RecipeHandler.recipeList.stream()
					.anyMatch(recipe -> recipe instanceof AlloySmelterRecipe
						&& ItemUtils.isInputEqual(recipe.getInputs().get(1), stack, true, true, true)))
			.outputSlot(2, 80, 47).energySlot(3, 8, 72).syncEnergyValue().syncCrafterValue().addInventory()
			.create(this);
	}
}
