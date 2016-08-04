package com.creativemd.littletiles.common.packet;

import java.util.ArrayList;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.utils.TileList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleBlockPacket extends CreativeCorePacket{
	
	public BlockPos blockPos;
	public Vec3d pos;
	public Vec3d look;
	public int action;
	public NBTTagCompound nbt;
	
	public LittleBlockPacket()
	{
		
	}
	
	public LittleBlockPacket(BlockPos blockPos, EntityPlayer player, int action)
	{
		this(blockPos, player, action, new NBTTagCompound());
	}
	
	public LittleBlockPacket(BlockPos blockPos, EntityPlayer player, int action, NBTTagCompound nbt)
	{
		this.blockPos = blockPos;
		this.action = action;
		this.pos = player.getPositionVector();
		double d0 = player.capabilities.isCreativeMode ? 5.0F : 4.5F;
		Vec3d look = player.getLook(1.0F);
		this.look = pos.addVector(look.xCoord * d0, look.yCoord * d0, look.zCoord * d0);
		this.nbt = nbt;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, blockPos);
		writeVec3(pos, buf);
		writeVec3(look, buf);
		buf.writeInt(action);
		writeNBT(buf, nbt);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		blockPos = readPos(buf);
		pos = readVec3(buf);
		look = readVec3(buf);		
		action = buf.readInt();
		nbt = readNBT(buf);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeClient(EntityPlayer player) {
		
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		TileEntity tileEntity = player.worldObj.getTileEntity(blockPos);
		if(tileEntity instanceof TileEntityLittleTiles)
		{
			TileEntityLittleTiles littleEntity = (TileEntityLittleTiles) tileEntity;
			RayTraceResult moving = littleEntity.getMoving(pos, look, true);
			LittleTile tile = littleEntity.loadedTile;
			if(tile != null)
			{
				switch(action)
				{
				case 0: //Activated
					if(tile.onBlockActivated(player.worldObj, blockPos, player.worldObj.getBlockState(blockPos), player, EnumHand.MAIN_HAND, player.getHeldItem(EnumHand.MAIN_HAND), moving.sideHit, (float)moving.hitVec.xCoord, (float)moving.hitVec.yCoord, (float)moving.hitVec.zCoord))
						BlockTile.cancelNext = true;
					break;
				case 1: //Destory tile
					tile.destroy();
					//littleEntity.removeTile(tile);
					if(!player.capabilities.isCreativeMode)
						WorldUtils.dropItem(player.worldObj, tile.getDrops(), blockPos);
					TileList<LittleTile> tiles = littleEntity.getTiles();
					for (int i = 0; i < tiles.size(); i++) {
						tiles.get(i).onNeighborChangeInside();
					}
					littleEntity.updateBlock();
					break;
				case 2:
					try{
						int side = nbt.getInteger("side");
						EnumFacing direction = EnumFacing.getFront(side);
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						if(te.updateLoadedTileServer(pos, look) && te.loadedTile.canSawResizeTile(direction, player))
						{
							LittleTileBox box = null;
							if(player.isSneaking())
								box = te.loadedTile.boundingBoxes.get(0).shrink(direction);
							else
								box = te.loadedTile.boundingBoxes.get(0).expand(direction);
							
							if(box.isBoxInsideBlock() && box.isValidBox() && te.isSpaceForLittleTile(box.getBox(), te.loadedTile))
							{
								float ammount = te.loadedTile.boundingBoxes.get(0).getSize().getPercentVolume()-box.getSize().getPercentVolume();
								boolean success = false;
								if(player.isSneaking())
								{
									if(ItemTileContainer.addBlock(player, ((LittleTileBlock)te.loadedTile).block, ((LittleTileBlock)te.loadedTile).meta, ammount))
										success = true;
								}else{
									if(ItemTileContainer.drainBlock(player, ((LittleTileBlock)te.loadedTile).block, ((LittleTileBlock)te.loadedTile).meta, -ammount))
										success = true;
								}
								
								if(player.capabilities.isCreativeMode || success)
								{
									te.loadedTile.boundingBoxes.set(0, box);
									te.loadedTile.updateCorner();
									te.updateBlock();
								}
							}
						}
					
					}catch(Exception e){
						System.out.println("Failed to use saw!");
						e.printStackTrace();
					}
					break;
				case 3:
					try{
						TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
						if(te.updateLoadedTileServer(pos, look) && (te.loadedTile.getClass() == LittleTileBlock.class || te.loadedTile instanceof LittleTileBlockColored))
						{
							int color = nbt.getInteger("color");
							LittleTile currentTile = te.loadedTile;
							
							int index = te.getTiles().indexOf(currentTile);
							if(player.isSneaking())
							{
								color = ColorUtils.WHITE;
								if(currentTile instanceof LittleTileBlockColored)
									color = ((LittleTileBlockColored) currentTile).color;
								ItemColorTube.setColor(player.getHeldItemMainhand(), color);
							}else{
								
								LittleTile newTile = LittleTileBlockColored.setColor((LittleTileBlock) currentTile, color);
								
								if(newTile != null)
									te.getTiles().set(index, newTile);
								te.updateBlock();
							}
						}
					}catch(Exception e){
						System.out.println("Failed to use color tube!");
						e.printStackTrace();
					}
					break;
				case 4:
					TileEntityLittleTiles te = (TileEntityLittleTiles) tileEntity;
					ArrayList<LittleTile> newTiles = new ArrayList<>();
					if(te.updateLoadedTileServer(pos, look) && (te.loadedTile.getClass() == LittleTileBlock.class || te.loadedTile instanceof LittleTileBlockColored)  && te.loadedTile.structure == null)
					{
						LittleTile oldTile = te.loadedTile;
						for (int j = 0; j < oldTile.boundingBoxes.size(); j++) {
							LittleTileBox box = oldTile.boundingBoxes.get(j);
							for (int littleX = box.minX; littleX < box.maxX; littleX++) {
								for (int littleY = box.minY; littleY < box.maxY; littleY++) {
									for (int littleZ = box.minZ; littleZ < box.maxZ; littleZ++) {
										tile = oldTile.copy();
										tile.boundingBoxes.clear();
										tile.boundingBoxes.add(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1));
										tile.updateCorner();
										tile.te = te;
										newTiles.add(tile);
									}
								}
							}
						}
						
						if(LittleTiles.maxNewTiles >= newTiles.size() - 1)
						{
							te.removeTile(oldTile);
							te.addTiles(newTiles);
							te.updateBlock();
						}else{
							player.addChatComponentMessage(new TextComponentTranslation("Too much new tiles! Limit=" + LittleTiles.maxNewTiles));
						}
					}
					break;
				}
			}
		}
	}
	
}
