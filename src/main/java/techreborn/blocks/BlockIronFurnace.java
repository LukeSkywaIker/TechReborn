package techreborn.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import reborncore.common.blocks.BlockMachineBase;
import reborncore.common.blocks.IRotationTexture;
import techreborn.Core;
import techreborn.client.GuiHandler;
import techreborn.client.TechRebornCreativeTab;
import techreborn.tiles.TileAlloyFurnace;
import techreborn.tiles.TileIronFurnace;

public class BlockIronFurnace extends BlockMachineBase implements IRotationTexture {

	public BlockIronFurnace() {
		super();
		setUnlocalizedName("techreborn.ironfurnace");
        setCreativeTab(TechRebornCreativeTab.instance);
	}
	
    @Override
    public TileEntity createNewTileEntity(World world, int p_149915_2_) {
        return new TileIronFurnace();
    }
	
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking())
            player.openGui(Core.INSTANCE, GuiHandler.ironFurnace, world, x, y, z);
        return true;
    }

    private final String prefix = "techreborn:blocks/machine/";

    @Override
    public String getFrontOff() {
        return prefix + "alloy_furnace_front_off";
    }

    @Override
    public String getFrontOn() {
        return prefix + "alloy_furnace_front_on";
    }

    @Override
    public String getSide() {
        return prefix + "alloy_furnace_side";
    }

    @Override
    public String getTop() {
        return prefix + "alloy_furnace_top";
    }

    @Override
    public String getBottom() {
        return prefix + "machine_bottom";
    }
}
