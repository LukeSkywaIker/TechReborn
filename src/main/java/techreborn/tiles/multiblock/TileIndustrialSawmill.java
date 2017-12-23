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

package techreborn.tiles.multiblock;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import reborncore.api.IToolDrop;
import reborncore.api.recipe.IRecipeCrafterProvider;
import reborncore.api.tile.IInventoryProvider;
import reborncore.common.powerSystem.TilePowerAcceptor;
import reborncore.common.recipes.RecipeCrafter;
import reborncore.common.registration.RebornRegistry;
import reborncore.common.registration.impl.ConfigRegistry;
import reborncore.common.util.FluidUtils;
import reborncore.common.util.Inventory;
import reborncore.common.util.Tank;
import techreborn.api.Reference;
import techreborn.api.recipe.ITileRecipeHandler;
import techreborn.api.recipe.machines.IndustrialSawmillRecipe;
import techreborn.client.container.IContainerProvider;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.container.builder.ContainerBuilder;
import techreborn.init.ModBlocks;
import techreborn.lib.ModInfo;

@RebornRegistry(modID = ModInfo.MOD_ID)
public class TileIndustrialSawmill extends TilePowerAcceptor implements IToolDrop,
		ITileRecipeHandler<IndustrialSawmillRecipe>, IRecipeCrafterProvider, IInventoryProvider, IContainerProvider {

	@ConfigRegistry(config = "machines", category = "industrial_sawmill", key = "IndustrialSawmillMaxInput", comment = "Industrial Sawmill Max Input (Value in EU)")
	public static int maxInput = 128;
	@ConfigRegistry(config = "machines", category = "industrial_sawmill", key = "IndustrialSawmillMaxEnergy", comment = "Industrial Sawmill Max Energy (Value in EU)")
	public static int maxEnergy = 10000;

	public static final int TANK_CAPACITY = 16000;
	public Inventory inventory;
	public Tank tank;
	public RecipeCrafter crafter;
	public MultiblockChecker multiblockChecker;
	int ticksSinceLastChange;

	//int tickTime;
	//IndustrialSawmillRecipe smRecipe;

	public TileIndustrialSawmill() {
		super();
		this.tank = new Tank("TileSawmill", TileIndustrialSawmill.TANK_CAPACITY, this);
		this.inventory = new Inventory(7, "TileSawmill", 64, this);
		this.ticksSinceLastChange = 0;
		final int[] inputs = new int[] { 0, 1 };
		final int[] outputs = new int[] { 2, 3, 4 };
		this.crafter = new RecipeCrafter(Reference.industrialSawmillRecipe, this, 1, 3, this.inventory, inputs,
				outputs);
	}

	public boolean getMutliBlock() {
		if (multiblockChecker == null) {
			return false;
		}
		final boolean down = this.multiblockChecker.checkRectY(1, 1, MultiblockChecker.STANDARD_CASING,
				MultiblockChecker.ZERO_OFFSET);
		final boolean up = this.multiblockChecker.checkRectY(1, 1, MultiblockChecker.STANDARD_CASING,
				new BlockPos(0, 2, 0));
		final boolean blade = this.multiblockChecker.checkRingY(1, 1, MultiblockChecker.REINFORCED_CASING,
				new BlockPos(0, 1, 0));
		final IBlockState centerBlock = this.multiblockChecker.getBlock(0, 1, 0);
		final boolean center = ((centerBlock.getBlock() instanceof BlockLiquid
				|| centerBlock.getBlock() instanceof IFluidBlock) && centerBlock.getMaterial() == Material.WATER);
		return down && center && blade && up;
	}

	public int getProgressScaled(final int scale) {
		if (this.crafter.currentTickTime != 0) {
			return this.crafter.currentTickTime * scale / this.crafter.currentNeededTicks;
		}
		return 0;
	}

	@Override
	public void update() {
		super.update();
		if (this.multiblockChecker == null) {
			final BlockPos pos = this.getPos().offset(this.getFacing().getOpposite(), 2).down();
			this.multiblockChecker = new MultiblockChecker(this.world, pos);
		}

		this.ticksSinceLastChange++;
		// Check cells input slot 2 time per second
		if (this.ticksSinceLastChange >= 10) {
			if (!this.inventory.getStackInSlot(1).isEmpty()) {
				FluidUtils.drainContainers(this.tank, this.inventory, 1, 5);
				FluidUtils.fillContainers(this.tank, this.inventory, 1, 5, this.tank.getFluidType());
			}
			this.ticksSinceLastChange = 0;
		}

		if (!world.isRemote && this.getMutliBlock()) {
			this.charge(6);
		}
	}

	@Override
	public void readFromNBT(final NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.tank.readFromNBT(tagCompound);
		this.crafter.readFromNBT(tagCompound);
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		this.tank.writeToNBT(tagCompound);
		this.crafter.writeToNBT(tagCompound);
		return tagCompound;
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			if (this.tank != null) {
				return (T) this.tank;
			}
		}
		return super.getCapability(capability, facing);
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
	public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
		if (slotIndex == 1) {
			if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	// IToolDrop
	@Override
	public ItemStack getToolDrop(final EntityPlayer entityPlayer) {
		return new ItemStack(ModBlocks.INDUSTRIAL_SAWMILL, 1);
	}

	// ITileRecipeHandler
	@Override
	public boolean canCraft(TileEntity tile, IndustrialSawmillRecipe recipe) {
		if (!getMutliBlock()) {
			return false;
		}
		final FluidStack recipeFluid = recipe.fluidStack;
		final FluidStack tankFluid = this.tank.getFluid();
		if (recipe.fluidStack == null) {
			return true;
		}
		if (tankFluid == null) {
			return false;
		}
		if (tankFluid.isFluidEqual(recipeFluid)) {
			if (tankFluid.amount >= recipeFluid.amount) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onCraft(TileEntity tile, IndustrialSawmillRecipe recipe) {
		final FluidStack recipeFluid = recipe.fluidStack;
		final FluidStack tankFluid = this.tank.getFluid();
		if (recipe.fluidStack == null) {
			return true;
		}
		if (tankFluid == null) {
			return false;
		}
		if (tankFluid.isFluidEqual(recipeFluid)) {
			if (tankFluid.amount >= recipeFluid.amount) {
				if (tankFluid.amount == recipeFluid.amount) {
					this.tank.setFluid(null);
				} else {
					tankFluid.amount -= recipeFluid.amount;
				}
				this.syncWithAll();
				return true;
			}
		}
		return false;
	}

	// IRecipeCrafterProvider
	@Override
	public RecipeCrafter getRecipeCrafter() {
		return this.crafter;
	}

	// IInventoryProvider
	@Override
	public Inventory getInventory() {
		return this.inventory;
	}

	// IContainerProvider
	@Override
	public BuiltContainer createContainer(final EntityPlayer player) {
		return new ContainerBuilder("industrialsawmill").player(player.inventory).inventory().hotbar().addInventory()
				.tile(this).fluidSlot(1, 34, 35).slot(0, 84, 43).outputSlot(2, 126, 25).outputSlot(3, 126, 43)
				.outputSlot(4, 126, 61).outputSlot(5, 34, 55).energySlot(6, 8, 72).syncEnergyValue().syncCrafterValue()
				.addInventory().create(this);
	}
}
