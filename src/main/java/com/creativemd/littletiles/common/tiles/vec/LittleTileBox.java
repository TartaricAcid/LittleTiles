package com.creativemd.littletiles.common.tiles.vec;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.place.PlacePreviewTile;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import scala.collection.generic.BitOperations.Int;

public class LittleTileBox {
	
	public int minX;
	public int minY;
	public int minZ;
	public int maxX;
	public int maxY;
	public int maxZ;
	
	public LittleTileBox(LittleTileVec center, LittleTileSize size)
	{
		LittleTileVec offset = size.calculateCenter();
		minX = (int) (center.x-offset.x);
		minY = (int) (center.y-offset.y);
		minZ = (int) (center.z-offset.z);
		maxX = (int) (minX+size.sizeX);
		maxY = (int) (minY+size.sizeY);
		maxZ = (int) (minZ+size.sizeZ);
	}
	
	public LittleTileBox(int[] array)
	{
		if(array.length == 6)
			set(array[0], array[1], array[2], array[3], array[4], array[5]);
		else
			throw new InvalidParameterException("No valid coords given " + array);
	}
	
	public LittleTileBox(String name, NBTTagCompound nbt)
	{
		if(nbt.getTag(name + "minX") instanceof NBTTagByte)
		{
			set((byte) nbt.getByte(name+"minX"), (byte) nbt.getByte(name+"minY"), (byte) nbt.getByte(name+"minZ"), (byte) nbt.getByte(name+"maxX"), (byte) nbt.getByte(name+"maxY"), (byte) nbt.getByte(name+"maxZ"));
			writeToNBT(name, nbt);
		}
		else if(nbt.getTag(name + "minX") instanceof NBTTagInt)
			set(nbt.getInteger(name+"minX"), nbt.getInteger(name+"minY"), nbt.getInteger(name+"minZ"), nbt.getInteger(name+"maxX"), nbt.getInteger(name+"maxY"), nbt.getInteger(name+"maxZ"));
		else if(nbt.getTag(name) instanceof NBTTagIntArray){
			int[] array = nbt.getIntArray(name);
			if(array.length == 6)
				set(array[0], array[1], array[2], array[3], array[4], array[5]);
			else
				throw new InvalidParameterException("No valid coords given " + array);
		}else if(nbt.getTag(name) instanceof NBTTagString){
			String[] coords = nbt.getString(name).split("\\.");
			try{
				set(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]), Integer.parseInt(coords[4]), Integer.parseInt(coords[5]));
			}catch(Exception e){
				set(0, 0, 0, 0, 0, 0);
			}
		}
	}
	
	public LittleTileBox(CubeObject cube)
	{
		this((int)Math.ceil(cube.minX*LittleTile.gridSize), (int)Math.ceil(cube.minY*LittleTile.gridSize), (int)Math.ceil(cube.minZ*LittleTile.gridSize), (int)Math.ceil(cube.maxX*LittleTile.gridSize), (int)Math.ceil(cube.maxY*LittleTile.gridSize), (int)Math.ceil(cube.maxZ*LittleTile.gridSize));
	}
	
	public LittleTileBox(AxisAlignedBB box)
	{
		this((int)(box.minX*LittleTile.gridSize), (int)(box.minY*LittleTile.gridSize), (int)(box.minZ*LittleTile.gridSize), (int)(box.maxX*LittleTile.gridSize), (int)(box.maxY*LittleTile.gridSize), (int)(box.maxZ*LittleTile.gridSize));
	}
	
	public LittleTileBox(LittleTileBox... boxes)
	{
		this(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for (int i = 0; i < boxes.length; i++) {
			this.minX = Math.min(boxes[i].minX, this.minX);
			this.minY = Math.min(boxes[i].minY, this.minY);
			this.minZ = Math.min(boxes[i].minZ, this.minZ);
			this.maxX = Math.max(boxes[i].maxX, this.maxX);
			this.maxY = Math.max(boxes[i].maxY, this.maxY);
			this.maxZ = Math.max(boxes[i].maxZ, this.maxZ);
		}
	}
	
	public LittleTileBox(LittleTileVec min, LittleTileVec max)
	{
		this(min.x, min.y, min.z, max.x, max.y, max.z);
	}
	
	public LittleTileBox(LittleTileVec min)
	{
		this(min.x, min.y, min.z, min.x+1, min.y+1, min.z+1);
	}
	
	public LittleTileBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		set(minX, minY, minZ, maxX, maxY, maxZ);
	}
	
	public AxisAlignedBB getBox(BlockPos offset)
	{
		return new AxisAlignedBB(minX/(double)LittleTile.gridSize + offset.getX(), minY/(double)LittleTile.gridSize + offset.getY(), minZ/(double)LittleTile.gridSize + offset.getZ(),
				maxX/(double)LittleTile.gridSize + offset.getX(), maxY/(double)LittleTile.gridSize + offset.getY(), maxZ/(double)LittleTile.gridSize + offset.getZ());
	}
	
	public AxisAlignedBB getBox()
	{
		return new AxisAlignedBB(minX/(double)LittleTile.gridSize, minY/(double)LittleTile.gridSize, minZ/(double)LittleTile.gridSize, maxX/(double)LittleTile.gridSize, maxY/(double)LittleTile.gridSize, maxZ/(double)LittleTile.gridSize);
	}
	
	public CubeObject getCube()
	{
		return new CubeObject(minX/(float)LittleTile.gridSize, minY/(float)LittleTile.gridSize, minZ/(float)LittleTile.gridSize, maxX/(float)LittleTile.gridSize, maxY/(float)LittleTile.gridSize, maxZ/(float)LittleTile.gridSize);
	}
	
	public int[] getArray()
	{
		return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
	}
	
	public NBTTagIntArray getNBTIntArray()
	{
		return new NBTTagIntArray(getArray());
	}
	
	public void writeToNBT(String name, NBTTagCompound  nbt)
	{
		/*nbt.setInteger(name+"minX", minX);
		nbt.setInteger(name+"minY", minY);
		nbt.setInteger(name+"minZ", minZ);
		nbt.setInteger(name+"maxX", maxX);
		nbt.setInteger(name+"maxY", maxY);
		nbt.setInteger(name+"maxZ", maxZ);*/
		//nbt.setString(name, minX+"."+minY+"."+minZ+"."+maxX+"."+maxY+"."+maxZ);
		nbt.setIntArray(name, new int[]{minX, minY, minZ, maxX, maxY, maxZ});
	}
	
	public Vec3d getSizeD()
	{
		return new Vec3d((maxX - minX)*LittleTile.gridMCLength, (maxY - minY)*LittleTile.gridMCLength, (maxZ - minZ)*LittleTile.gridMCLength);
	}
	
	public LittleTileSize getSize()
	{
		return new LittleTileSize((int)(maxX - minX), (int)(maxY - minY), (int)(maxZ - minZ));
	}
	
	public int getVolume()
	{
		return (int)(maxX - minX) * (int)(maxY - minY) * (int)(maxZ - minZ);
	}
	
	/**@return the volume in percent to a size of a normal block*/
	public double getPercentVolume()
	{
		return (double) getVolume() / (double) (LittleTile.maxTilesPerBlock);
	}
	
	public LittleTileBox copy()
	{
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean isValidBox() {
		return maxX > minX && maxY > minY && maxZ > minZ;
	}
	
	public void set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}
	
	public boolean needsMultipleBlocks() {
		int x = minX/LittleTile.gridSize;
		int y = minY/LittleTile.gridSize;
		int z = minZ/LittleTile.gridSize;
		
		return maxX-x*LittleTile.gridSize<=LittleTile.maxPos && maxY-y*LittleTile.gridSize<=LittleTile.maxPos && maxZ-z*LittleTile.gridSize<=LittleTile.maxPos;
	}
	
	public boolean doesMatchTwoSides(LittleTileBox box, EnumFacing facing)
	{
		switch(facing)
		{
		case EAST:
		case WEST:
			return minY == box.minY && maxY == box.maxY && minZ == box.minZ && maxZ == box.maxZ;
		case UP:
		case DOWN:
			return minX == box.minX && maxX == box.maxX && minZ == box.minZ && maxZ == box.maxZ;
		case SOUTH:
		case NORTH:
			return minX == box.minX && maxX == box.maxX && minY == box.minY && maxY == box.maxY;
		}
		return false;
	}
	
	public LittleTileBox combineBoxes(LittleTileBox box)
	{
		boolean x = this.minX == box.minX && this.maxX == box.maxX;
		boolean y = this.minY == box.minY && this.maxY == box.maxY;
		boolean z = this.minZ == box.minZ && this.maxZ == box.maxZ;
		
		if(x && y && z)
		{
			return this;
		}
		if(x && y)
		{
			if(this.minZ == box.maxZ)
				return new LittleTileBox(minX, minY, box.minZ, maxX, maxY, maxZ);
			else if(this.maxZ == box.minZ)
				return new LittleTileBox(minX, minY, minZ, maxX, maxY, box.maxZ);
		}
		if(x && z)
		{
			if(this.minY == box.maxY)
				return new LittleTileBox(minX, box.minY, minZ, maxX, maxY, maxZ);
			else if(this.maxY == box.minY)
				return new LittleTileBox(minX, minY, minZ, maxX, box.maxY, maxZ);
		}
		if(y && z)
		{
			if(this.minX == box.maxX)
				return new LittleTileBox(box.minX, minY, minZ, maxX, maxY, maxZ);
			else if(this.maxX == box.minX)
				return new LittleTileBox(minX, minY, minZ, box.maxX, maxY, maxZ);
		}
		return null;
	}
	
	public void addOffset(LittleTileVec vec)
	{
		minX += vec.x;
		minY += vec.y;
		minZ += vec.z;
		maxX += vec.x;
		maxY += vec.y;
		maxZ += vec.z;
	}
	
	public void subOffset(LittleTileVec vec)
	{
		minX -= vec.x;
		minY -= vec.y;
		minZ -= vec.z;
		maxX -= vec.x;
		maxY -= vec.y;
		maxZ -= vec.z;
	}
	
	public void assignCube(CubeObject cube)
	{
		this.minX = (int)(cube.minX*LittleTile.gridSize);
		this.minY = (int)(cube.minY*LittleTile.gridSize);
		this.minZ = (int)(cube.minZ*LittleTile.gridSize);
		this.maxX = (int)(cube.maxX*LittleTile.gridSize);
		this.maxY = (int)(cube.maxY*LittleTile.gridSize);
		this.maxZ = (int)(cube.maxZ*LittleTile.gridSize);
	}
	
	public LittleTileVec getMinVec()
	{
		return new LittleTileVec(minX, minY, minZ);
	}
	
	public LittleTileVec getMaxVec()
	{
		return new LittleTileVec(maxX, maxY, maxZ);
	}
	
	/**
	 * @param cutout a list of boxes which have been cut out.
	 * @return all remaining boxes or null if the box remains as it is
	 */
	public List<LittleTileBox> cutOut(List<LittleTileBox> boxes, List<LittleTileBox> cutout)
	{
		ArrayList<LittleTileBox> newBoxes = new ArrayList<>();
		
		for (int littleX = minX; littleX < maxX; littleX++) {
			for (int littleY = minY; littleY < maxY; littleY++) {
				for (int littleZ = minZ; littleZ < maxZ; littleZ++) {
					LittleTileVec min = new LittleTileVec(littleX, littleY, littleZ);
					boolean isInside = false;
					for (int i = 0; i < boxes.size(); i++) {
						if(boxes.get(i).isVecInsideBox(min))
						{
							isInside = true;
							break;
						}
					}
					
					if(isInside)
						cutout.add(new LittleTileBox(min));
					else
						newBoxes.add(new LittleTileBox(min));
				}
			}
		}
		
		combineBoxes(newBoxes);
		
		if(newBoxes.size() == 1 && newBoxes.get(0).equals(this))
			return null;
		
		combineBoxes(cutout);
		
		return newBoxes;
	}
	
	/**
	 * @return all remaining boxes or null if the box remains as it is
	 */
	public List<LittleTileBox> cutOut(LittleTileBox box)
	{
		if(intersectsWith(box))
		{
			ArrayList<LittleTileBox> boxes = new ArrayList<>();
			for (int littleX = minX; littleX < maxX; littleX++) {
				for (int littleY = minY; littleY < maxY; littleY++) {
					for (int littleZ = minZ; littleZ < maxZ; littleZ++) {
						if(!(littleX >= minX && littleX < maxX && littleY >= minY && littleY < maxY && littleZ >= minZ && littleZ < maxZ))
							boxes.add(new LittleTileBox(littleX, littleY, littleZ, littleX+1, littleY+1, littleZ+1));
					}
				}
			}
			
			combineBoxes(boxes);
			
			return boxes;
		}
		
		return null;
	}
	
	public void rotateBoxWithCenter(Rotation direction, Vec3d center)
	{
		CubeObject cube = this.getCube();
		cube = CubeObject.rotateCube(cube, direction, center);
		this.minX = (int) (cube.minX*LittleTile.gridSize);
		this.minY = (int) (cube.minY*LittleTile.gridSize);
		this.minZ = (int) (cube.minZ*LittleTile.gridSize);
		this.maxX = (int) (cube.maxX*LittleTile.gridSize);
		this.maxY = (int) (cube.maxY*LittleTile.gridSize);
		this.maxZ = (int) (cube.maxZ*LittleTile.gridSize);
		//assignCube(cube);
	}
	
	public void flipBox(EnumFacing direction)
	{
		switch(direction)
		{
		case EAST:
		case WEST:
			minX = -minX;
			maxX = -maxX;
			break;
		case UP:
		case DOWN:
			minY = -minY;
			maxY = -maxY;
			break;
		case SOUTH:
		case NORTH:
			minZ = -minZ;
			maxZ = -maxZ;
			break;
		default:
			break;
		}
		
		resort();
	}
	
	public void flipBoxWithCenter(EnumFacing direction, LittleTileVec center)
	{
		if(center == null)
			center = new LittleTileVec(LittleTile.halfGridSize, LittleTile.halfGridSize, LittleTile.halfGridSize);
		subOffset(center);
		flipBox(direction);
		addOffset(center);		
	}
	
	public void rotateBox(EnumFacing direction)
	{
		CubeObject cube = this.getCube();
		/*int x = (int) cube.minX;
		cube.minX -= x;
		cube.maxX -= x;
		int y = (int) cube.minY;
		cube.minY -= y;
		cube.maxY -= y;
		int z = (int) cube.minZ;
		cube.minZ -= z;
		cube.maxZ -= z;*/
		cube = CubeObject.rotateCube(cube, direction);
		/*cube.minX += x;
		cube.maxX += x;
		cube.minY += y;
		cube.maxY += y;
		cube.minZ += z;
		cube.maxZ += z;*/
		assignCube(cube);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if(object instanceof LittleTileBox)
			return minX == ((LittleTileBox) object).minX && minY == ((LittleTileBox) object).minY && minZ == ((LittleTileBox) object).minZ && maxX == ((LittleTileBox) object).maxX && maxY == ((LittleTileBox) object).maxY && maxZ == ((LittleTileBox) object).maxZ;
		return super.equals(object);
	}
	
	@Override
	public String toString()
	{
		return "[" + minX + "," + minY + "," + minZ + " -> " + maxX + "," + maxY + "," + maxZ + "]";
	}
	
	public LittleTileVec getNearstedPointTo(LittleTileVec vec)
	{
		int x = minX;
		if(vec.x >= minX || vec.x <= maxX)
			x = vec.x;
		if(Math.abs(minX-x) > Math.abs(maxX-x))
			x = maxX;
		
		int y = minY;
		if(vec.y >= minY || vec.y <= maxY)
			y = vec.y;
		if(Math.abs(minY-y) > Math.abs(maxY-y))
			y = maxY;
		
		int z = minZ;
		if(vec.z >= minZ || vec.z <= maxZ)
			z = vec.z;
		if(Math.abs(minZ-z) > Math.abs(maxZ-z))
			z = maxZ;
		
		return new LittleTileVec(x, y, z);
	}
	
	public LittleTileVec getNearstedPointTo(LittleTileBox box)
	{
		int x = 0;
		if(minX >= box.minX && minX <= box.maxX)
			x = minX;
		else if(box.minX >= minX && box.minX <= box.maxX)
			x = box.minX;
		else
			if(Math.abs(minX-box.maxX) > Math.abs(maxX - box.minX))
				x = maxX;
			else
				x = minX;
		
		int y = 0;
		if(minY >= box.minY && minY <= box.maxY)
			y = minY;
		else if(box.minY >= minY && box.minY <= box.maxY)
			y = box.minY;
		else
			if(Math.abs(minY-box.maxY) > Math.abs(maxY - box.minY))
				y = maxY;
			else
				y = minY;
		
		int z = 0;
		if(minZ >= box.minZ && minZ <= box.maxZ)
			z = minZ;
		else if(box.minZ >= minZ && box.minZ <= box.maxZ)
			z = box.minZ;
		else
			if(Math.abs(minZ-box.maxZ) > Math.abs(maxZ - box.minZ))
				z = maxZ;
			else
				z = minZ;
		
		return new LittleTileVec(x, y, z);
	}
	
	public double distanceTo(LittleTileBox box)
	{
		return distanceTo(box.getNearstedPointTo(this));
	}
	
	public double distanceTo(LittleTileVec vec)
	{
		return this.getNearstedPointTo(vec).distanceTo(vec);
	}
	
	public boolean isBoxInside(LittleTileBox box) {
		if(this.minX > box.maxX || box.minX > this.minX)
			return false;
		
		if(this.minY > box.maxY || box.minY > this.minY)
			return false;
		
		if(this.minZ > box.maxZ || box.minZ > this.minZ)
			return false;
		
		return true;
	}
	
	public boolean isVecInsideBox(LittleTileVec vec)
	{
		return vec.x >= minX && vec.x < maxX && vec.y >= minY && vec.y < maxY && vec.z >= minZ && vec.z < maxZ;
	}
	
	public boolean intersectsWith(LittleTileBox box)
    {
        return box.maxX > this.minX && box.minX < this.maxX ? (box.maxY > this.minY && box.minY < this.maxY ? box.maxZ > this.minZ && box.minZ < this.maxZ : false) : false;
    }
	
	public EnumFacing faceTo(LittleTileBox box) {
		
		boolean x = !(this.minX >= box.maxX || box.minX >= this.maxX);
		boolean y = !(this.minY >= box.maxY || box.minY >= this.maxY);
		boolean z = !(this.minZ >= box.maxZ || box.minZ >= this.maxZ);
		
		if(x && y && z)
			return EnumFacing.EAST;
		
		if(x && y)
			if(this.minZ > box.maxZ)
				return EnumFacing.NORTH;
			else
				return EnumFacing.SOUTH;
		
		if(x && z)
			if(this.minY > box.maxY)
				return EnumFacing.DOWN;
			else
				return EnumFacing.UP;
		
		if(y && z)
			if(this.minX > box.maxX)
				return EnumFacing.WEST;
			else
				return EnumFacing.EAST;
		
		return EnumFacing.EAST;
	}

	public boolean hasTwoSideIntersection(LittleTileBox box) {
		boolean x = !(this.minX > box.maxX || box.minX > this.minX);
		boolean y = !(this.minY > box.maxY || box.minY > this.minY);
		boolean z = !(this.minZ > box.maxZ || box.minZ > this.minZ);
		if(x && y && z)
			return false;
		return x && y || x && z || y && z;
	}
	
	/**:D**/
	public boolean isParallel(LittleTileBox box) {
		return true;
	}
	
	public boolean isBoxInsideBlock()
	{
		return minX >= LittleTile.minPos && maxX <= LittleTile.maxPos && minY >= LittleTile.minPos && maxY <= LittleTile.maxPos && minZ >= LittleTile.minPos && maxZ <= LittleTile.maxPos;
	}
	
	public LittleTileBox expand(EnumFacing direction)
	{
		return expand(direction, false);
	}
	
	public LittleTileBox expand(EnumFacing direction, boolean toLimit)
	{
		LittleTileBox result = this.copy();
		switch(direction)
		{
		case EAST:
			if(!toLimit || result.maxX == LittleTile.gridSize)
				result.maxX++;
			else
				result.maxX = LittleTile.gridSize;
			break;
		case WEST:
			if(!toLimit || result.maxX <= 0)
				result.minX--;
			else
				result.minX = 0;
			break;
		case UP:
			if(!toLimit || result.maxY == LittleTile.gridSize)
				result.maxY++;
			else
				result.maxY = LittleTile.gridSize;
			break;
		case DOWN:
			if(!toLimit || result.minY <= 0)
				result.minY--;
			else
				result.minY = 0;
			break;
		case SOUTH:
			if(!toLimit || result.maxZ == LittleTile.gridSize)
				result.maxZ++;
			else
				result.maxZ = LittleTile.gridSize;
			break;
		case NORTH:
			if(!toLimit || result.minZ <= 0)
				result.minZ--;
			else
				result.minZ = 0;
			break;
		default:
			break;
		}
		return result;
	}
	
	public LittleTileBox shrink(EnumFacing direction)
	{
		return shrink(direction, false);
	}
	
	public LittleTileBox shrink(EnumFacing direction, boolean toLimit)
	{
		LittleTileBox result = this.copy();
		switch(direction)
		{
		case EAST:
			if(!toLimit)
				result.maxX--;
			else
				result.maxX = result.minX+1;
			break;
		case WEST:
			if(!toLimit)
				result.minX++;
			else
				result.minX = result.maxX-1;
			break;
		case UP:
			if(!toLimit)
				result.maxY--;
			else
				result.maxY = result.minY+1;
			break;
		case DOWN:
			if(!toLimit)
				result.minY++;
			else
				result.minY = result.maxY-1;
			break;
		case SOUTH:
			if(!toLimit)
				result.maxZ--;
			else
				result.maxZ = result.minZ+1;
			break;
		case NORTH:
			if(!toLimit)
				result.minZ++;
			else
				result.minZ = result.maxZ-1;
			break;
		default:
			break;
		}
		return result;
	}
	
	public void applyDirection(EnumFacing direction) {
		switch(direction)
		{
		case EAST:
			minX += LittleTile.gridSize;
			maxX += LittleTile.gridSize;
			break;
		case WEST:
			minX -= LittleTile.gridSize;
			maxX -= LittleTile.gridSize;
			break;
		case UP:
			minY += LittleTile.gridSize;
			maxY += LittleTile.gridSize;
			break;
		case DOWN:
			minY -= LittleTile.gridSize;
			maxY -= LittleTile.gridSize;
			break;
		case SOUTH:
			minZ += LittleTile.gridSize;
			maxZ += LittleTile.gridSize;
			break;
		case NORTH:
			minZ -= LittleTile.gridSize;
			maxZ -= LittleTile.gridSize;
			break;
		default:
			break;
		}
	}

	public void resort() {
		set(Math.min(minX, maxX), Math.min(minY, maxY), Math.min(minZ, maxZ), Math.max(minX, maxX), Math.max(minY, maxY), Math.max(minZ, maxZ));
	}

	public LittleTileBox getSideOfBox(EnumFacing facing) {
		LittleTileBox newBox = this.copy();
		switch(facing)
		{
		case EAST:
			newBox.minX = this.maxX;
			newBox.maxX++;
			break;
		case WEST:
			newBox.maxX = this.minX;
			newBox.minX--;
			break;
		case UP:
			newBox.minY = this.maxY;
			newBox.maxY++;
			break;
		case DOWN:
			newBox.maxY = this.minY;
			newBox.minY--;
			break;
		case SOUTH:
			newBox.minZ = this.maxZ;
			newBox.maxZ++;
			break;
		case NORTH:
			newBox.maxZ = this.minZ;
			newBox.minZ--;
			break;
		}
		return newBox;
	}
	
	public LittleTileBox createOutsideBlockBox(EnumFacing facing)
	{
		LittleTileBox box = this.copy();
		switch(facing)
		{
		case EAST:
			box.minX = 0;
			box.maxX -= LittleTile.gridSize;
			break;
		case WEST:
			box.minX += LittleTile.gridSize;
			box.maxX = LittleTile.gridSize;
			break;
		case UP:
			box.minY = 0;
			box.maxY -= LittleTile.gridSize;
			break;
		case DOWN:
			box.minY += LittleTile.gridSize;
			box.maxY = LittleTile.gridSize;
			break;
		case SOUTH:
			box.minZ = 0;
			box.maxZ -= LittleTile.gridSize;
			break;
		case NORTH:
			box.minZ += LittleTile.gridSize;
			box.maxZ = LittleTile.gridSize;
			break;
		}
		return box;
	}

	public LittleTileBox createInsideBlockBox(EnumFacing facing)
	{
		CubeObject cube = this.getCube();
		return new LittleTileBox(cube.offset(new BlockPos(0, 0, 0).offset(facing)));
	}
	
	public static void combineBoxesBlocks(ArrayList<LittleTileBox> boxes) {
		
		HashMapList<BlockPos, LittleTileBox> chunked = new HashMapList<>();
		for (int i = 0; i < boxes.size(); i++) {
			chunked.add(boxes.get(i).getMinVec().getBlockPos(), boxes.get(i));
		}
		boxes.clear();
		for (Iterator<ArrayList<LittleTileBox>> iterator = chunked.getValues().iterator(); iterator.hasNext();) {
			ArrayList<LittleTileBox> list = iterator.next();
			combineBoxes(list);
			boxes.addAll(list);
		}
	}
	
	public static void combineBoxes(List<LittleTileBox> boxes) {
		//ArrayList<LittleTile> newTiles = new ArrayList<>();
		int size = 0;
		while(size != boxes.size())
		{
			size = boxes.size();
			int i = 0;
			while(i < boxes.size()){
				int j = 0;
				while(j < boxes.size()) {
					if(i != j)
					{
						LittleTileBox box = boxes.get(i).combineBoxes(boxes.get(j));
						if(box != null)
						{
							boxes.set(i, box);
							boxes.remove(j);
							if(i > j)
								i--;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}	
	}
	
	public void makeItFitInsideBlock()
	{
		minX = Math.max(LittleTile.minPos, minX);
		maxX = Math.min(LittleTile.gridSize, maxX);
		minY = Math.max(LittleTile.minPos, minY);
		maxY = Math.min(LittleTile.gridSize, maxY);
		minZ = Math.max(LittleTile.minPos, minZ);
		maxZ = Math.min(LittleTile.gridSize, maxZ);
	}

	public LittleTileVec getCenter() {
		return new LittleTileVec((maxX + minX)/2, (maxY + minY)/2, (maxZ + minZ)/2);
	}
	
	public void split(HashMapList<BlockPos, LittleTileBox> boxes)
	{
		LittleTileSize size = getSize();
		
		int offX = minX/LittleTile.gridSize;
		if(minX < 0)
			offX = (int) Math.floor(minX/(double)LittleTile.gridSize);
		int offY = minY/LittleTile.gridSize;
		if(minY < 0)
			offY = (int) Math.floor(minY/(double)LittleTile.gridSize);
		int offZ = minZ/LittleTile.gridSize;
		if(minZ < 0)
			offZ = (int) Math.floor(minZ/(double)LittleTile.gridSize);
		
		int posX = offX;
		
		int spaceX = minX-offX*LittleTile.gridSize;
		int spaceY = minY-offY*LittleTile.gridSize;
		int spaceZ = minZ-offZ*LittleTile.gridSize;
		
		for (int i = 0; spaceX+size.sizeX > i*LittleTile.gridSize; i++) {
			int posY = offY;
			for (int j = 0; spaceY+size.sizeY > j*LittleTile.gridSize; j++) {
				int posZ = offZ;
				for (int h = 0; spaceZ+size.sizeZ > h*LittleTile.gridSize; h++) {
					
					LittleTileBox box = copy();
					if(i > 0)
						box.minX =	0;
					else
						box.minX = spaceX;
					if(i*LittleTile.gridSize+LittleTile.gridSize > spaceX+size.sizeX)
					{
						box.maxX = (box.maxX-box.maxX/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxX < 0)
							box.maxX = LittleTile.gridSize+box.maxX;
					}
					else
						box.maxX = LittleTile.gridSize;
					
					if(j > 0)
						box.minY =	0;
					else
						box.minY = spaceY;
					if(j*LittleTile.gridSize+LittleTile.gridSize > spaceY+size.sizeY)
					{
						box.maxY = (box.maxY-box.maxY/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxY < 0)
							box.maxY = LittleTile.gridSize+box.maxY;
					}
					else
						box.maxY = LittleTile.gridSize;
					
					if(h > 0)
						box.minZ =	0;
					else
						box.minZ = spaceZ;
					if(h*LittleTile.gridSize+LittleTile.gridSize > spaceZ+size.sizeZ)
					{
						box.maxZ = (box.maxZ-box.maxZ/LittleTile.gridSize*LittleTile.gridSize);
						if(box.maxZ < 0)
							box.maxZ = LittleTile.gridSize+box.maxZ;
					}
					else
						box.maxZ = LittleTile.gridSize;
					
					if(box.isValidBox())
						boxes.add(new BlockPos(posX, posY, posZ), box);
					posZ++;
				}
				posY++;
			}
			posX++;
		}
	}
	
	public static void toBytes(ByteBuf buf, LittleTileBox box)
	{
		buf.writeInt(box.minX);
		buf.writeInt(box.minY);
		buf.writeInt(box.minZ);
		buf.writeInt(box.maxX);
		buf.writeInt(box.maxY);
		buf.writeInt(box.maxZ);
	}
	
	public static LittleTileBox fromBytes(ByteBuf buf)
	{
		return new LittleTileBox(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
	}
	
}
