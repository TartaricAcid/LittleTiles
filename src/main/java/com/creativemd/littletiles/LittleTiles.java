package com.creativemd.littletiles;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ItemBlockColored;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemLittleSaw;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.sorting.LittleTileSortingList;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.server.LittleTilesServer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles")
public class LittleTiles {
	
	@Instance(LittleTiles.modid)
	public static LittleTiles instance = new LittleTiles();
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "1.3.0";
	
	public static int maxNewTiles = 512;
	
	public static BlockTile blockTile = (BlockTile) new BlockTile(Material.ROCK).setRegistryName("BlockLittleTiles");
	public static Block coloredBlock = new BlockLTColored().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock");
	
	public static Item hammer = new ItemHammer().setUnlocalizedName("LTHammer");
	public static Item recipe = new ItemRecipe().setUnlocalizedName("LTRecipe");
	public static Item multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles");
	public static Item saw = new ItemLittleSaw().setUnlocalizedName("LTSaw");
	public static Item container = new ItemTileContainer().setUnlocalizedName("LTContainer");
	public static Item wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench");
	public static Item chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel");
	public static Item colorTube = new ItemColorTube().setUnlocalizedName("LTColorTube");
	public static Item rubberMallet = new ItemRubberMallet().setUnlocalizedName("LTRubberMallet");
	
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		ForgeModContainer.fullBoundingBoxLadders = true;
		
		GameRegistry.registerItem(hammer, "hammer");
		GameRegistry.registerItem(recipe, "recipe");
		GameRegistry.registerItem(saw, "saw");
		GameRegistry.registerItem(container, "container");
		GameRegistry.registerItem(wrench, "wrench");
		GameRegistry.registerItem(chisel, "chisel");
		GameRegistry.registerItem(colorTube, "colorTube");
		GameRegistry.registerItem(rubberMallet, "rubberMallet");
		
		//GameRegistry.registerBlock(coloredBlock, "LTColoredBlock");
		GameRegistry.registerBlock(coloredBlock, ItemBlockColored.class);
		GameRegistry.registerBlock(blockTile, ItemBlockTiles.class);
		
		GameRegistry.registerItem(multiTiles, "multiTiles");
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		
		proxy.loadSide();
		
		LittleTile.registerLittleTile(LittleTileBlock.class, "BlockTileBlock");
		//LittleTile.registerLittleTile(LittleTileStructureBlock.class, "BlockTileStructure");
		LittleTile.registerLittleTile(LittleTileTileEntity.class, "BlockTileEntity");
		LittleTile.registerLittleTile(LittleTileBlockColored.class, "BlockTileColored");
		
		CreativeCorePacket.registerPacket(LittlePlacePacket.class, "LittlePlace");
		CreativeCorePacket.registerPacket(LittleBlockPacket.class, "LittleBlock");
		CreativeCorePacket.registerPacket(LittleRotatePacket.class, "LittleRotate");
		CreativeCorePacket.registerPacket(LittleFlipPacket.class, "LittleFlip");
		//FMLCommonHandler.instance().bus().register(new LittleEvent());
		MinecraftForge.EVENT_BUS.register(new LittleEvent());
		
		LittleStructure.initStructures();
		
		//Recipes
		GameRegistry.addRecipe(new ItemStack(hammer),  new Object[]
				{
				"XXX", "ALA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(container),  new Object[]
				{
				"XXX", "XHX", "XXX", 'X', Items.IRON_INGOT, 'H', hammer
				});
		
		GameRegistry.addRecipe(new ItemStack(saw),  new Object[]
				{
				"AXA", "AXA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(wrench),  new Object[]
				{
				"AXA", "ALA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(rubberMallet),  new Object[]
				{
				"XXX", "XLX", "ALA", 'X', Blocks.WOOL, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(colorTube),  new Object[]
				{
				"XXX", "XLX", "XXX", 'X', Items.DYE, 'L', Items.IRON_INGOT
				});
		
    }
	
	@EventHandler
    public void LoadComplete(FMLLoadCompleteEvent event)
    {
		LittleTileSortingList.initVanillaBlocks();
    }
}
