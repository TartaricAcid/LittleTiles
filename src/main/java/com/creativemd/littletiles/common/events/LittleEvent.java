package com.creativemd.littletiles.common.events;

import java.util.Iterator;
import java.util.List;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.structure.LittleBed;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.PlacementHelper;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleEvent {
	
	@SubscribeEvent
	public void trackEntity(StartTracking event)
	{
		if(event.getTarget() instanceof EntityDoorAnimation && ((EntityDoorAnimation) event.getTarget()).activator != event.getEntityPlayer())
		{
			EntityDoorAnimation animation = (EntityDoorAnimation) event.getTarget();
			PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), true), (EntityPlayerMP) event.getEntityPlayer());
		}
	}
	
	public static boolean cancelNext = false;
	
	public static ItemStack lastSelectedItem = null;
	public static ISpecialBlockSelector blockSelector = null;
	public static ILittleTile iLittleTile = null;
	
	@SideOnly(Side.CLIENT)
	private boolean leftClicked;
	
	@SubscribeEvent
	public void onLeftClick(LeftClickBlock event)
	{
		if(event.getWorld().isRemote)
		{
			if(!leftClicked)
			{
				ItemStack stack = event.getItemStack();
				RayTraceResult ray = new RayTraceResult(event.getHitVec(), event.getFace(), event.getPos());
				if(lastSelectedItem != null && lastSelectedItem != stack)
				{
					if(blockSelector != null)
					{
						blockSelector.onDeselect(event.getWorld(), lastSelectedItem, event.getEntityPlayer());
						blockSelector = null;
					}
					
					if(iLittleTile != null)
					{
						iLittleTile.onDeselect(event.getEntityPlayer(), lastSelectedItem);
						iLittleTile = null;
					}
					
					lastSelectedItem = null;
				}
				
				if(stack.getItem() instanceof ISpecialBlockSelector)
				{
					if(((ISpecialBlockSelector) stack.getItem()).onClickBlock(event.getWorld(), stack, event.getEntityPlayer(), ray, new LittleTileVec(ray.hitVec)))
						event.setCanceled(true);
					blockSelector = (ISpecialBlockSelector) stack.getItem();
					lastSelectedItem = stack;
				}
				
				iLittleTile = PlacementHelper.getLittleInterface(stack);
				
				if(iLittleTile != null)
				{
					iLittleTile.onClickBlock(event.getEntityPlayer(), stack, ray);
					lastSelectedItem = stack;
				}
				
				leftClicked = true;
			}
		}else if(event.getItemStack().getItem() instanceof ISpecialBlockSelector)
			event.setCanceled(true);
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event)
	{
		ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
		if(stack.getItem() instanceof ISpecialBlockSelector)
			event.setNewSpeed(0);
	}
	
	@SubscribeEvent
	public void onInteract(RightClickBlock event)
	{
		if(cancelNext)
		{
			cancelNext = false;
			event.setCanceled(true);
			return ;
		}
		
		ItemStack stack = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
		if(event.getWorld().isRemote && event.getHand() == EnumHand.MAIN_HAND && stack.getItem() == Items.GLOWSTONE_DUST && event.getEntityPlayer().isSneaking())
		{
			BlockTile.TEResult te = BlockTile.loadTeAndTile(event.getEntityPlayer().world, event.getPos(), event.getEntityPlayer());
			if(te.isComplete())
			{
				new LittleActionGlowstone(event.getPos(), event.getEntityPlayer()).execute();
				event.setCanceled(true);
			}
		}
		
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if(iTile != null )
		{
			if(event.getHand() == EnumHand.MAIN_HAND && event.getWorld().isRemote)
				onRightInteractClient(iTile, event.getEntityPlayer(), event.getHand(), event.getWorld(), stack, event.getPos(), event.getFace());
			event.setCanceled(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void onRightInteractClient(ILittleTile iTile, EntityPlayer player, EnumHand hand, World world, ItemStack stack, BlockPos pos, EnumFacing facing)
	{
		if(iTile.onRightClick(player, stack, Minecraft.getMinecraft().objectMouseOver))
		{
			if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack))
	        {
				if(iTile.arePreviewsAbsolute())
				{
					new LittleActionPlaceAbsolute(iTile.getLittlePreview(stack, false), true).execute();
				}else if(new LittleActionPlaceRelative(PreviewRenderer.markedPosition != null ? PreviewRenderer.markedPosition : PlacementHelper.getPosition(world, Minecraft.getMinecraft().objectMouseOver), PreviewRenderer.isCentered(player), PreviewRenderer.isFixed(player), GuiScreen.isCtrlKeyDown()).execute())
					PreviewRenderer.markedPosition = null;
	        }
			iTile.onDeselect(player, stack);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event)
	{
		if(event.getTarget().typeOfHit == Type.BLOCK)
		{
			EntityPlayer player = event.getPlayer();
			World world = event.getPlayer().getEntityWorld();
			BlockPos pos = event.getTarget().getBlockPos();
			IBlockState state = world.getBlockState(pos);
			ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
			if(stack != null && stack.getItem() instanceof ISpecialBlockSelector && ((ISpecialBlockSelector) stack.getItem()).hasCustomBox(world, stack, player, state, event.getTarget(), new LittleTileVec(event.getTarget().hitVec)))
			{
				double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
		        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
		        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();
		        List<LittleTileBox> boxes = ((ISpecialBlockSelector) stack.getItem()).getBox(world, stack, player, event.getTarget(), new LittleTileVec(event.getTarget().hitVec));
		        //box.addOffset(new LittleTileVec(pos));
		        
		        GlStateManager.enableBlend();
	            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	            GlStateManager.glLineWidth(2.0F);
	            GlStateManager.disableTexture2D();
	            GlStateManager.depthMask(false);
	            for (int i = 0; i < boxes.size(); i++) {
	            	RenderGlobal.drawSelectionBoundingBox(boxes.get(i).getBox().expandXyz(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
				}
				
				GlStateManager.depthMask(true);
	            GlStateManager.enableTexture2D();
	            GlStateManager.disableBlend();
	            
				event.setCanceled(true);
			}
			
		}
	}
	
	@SubscribeEvent
	public void isSleepingLocationAllowed(SleepingLocationCheckEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if(bed instanceof LittleBed && ((LittleBed) bed).sleepingPlayer == event.getEntityPlayer())
				event.setResult(Result.ALLOW);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.player);
			if(bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.player, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event)
	{
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if(bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.getEntityPlayer(), null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerSleep(PlayerSleepInBedEvent event)
	{
		if(event.getEntityPlayer().world.getBlockState(event.getPos()).getBlock() instanceof BlockTile)
		{
			TileEntityLittleTiles te = BlockTile.loadTe(event.getEntityPlayer().world, event.getPos());
			if(te != null)
			{
				for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					if(tile.structure instanceof LittleBed && ((LittleBed) tile.structure).hasBeenActivated)
					{
						((LittleBed) tile.structure).trySleep(event.getEntityPlayer(), tile.structure.getHighestCenterPoint());
						event.setResult(SleepResult.OK);
						((LittleBed) tile.structure).hasBeenActivated = false;
						return ;
					}
				}
			}
		}
	}
	
	/*@SubscribeEvent
	public void worldCollision(GetCollisionBoxesEvent event)
	{
		for (int i = 0; i < event.getWorld().loadedEntityList.size(); i++) {
			Entity entity = event.getWorld().loadedEntityList.get(i);
			if(entity instanceof EntityDoorAnimation)
			{
				
			}
		}
		
	}*/
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(ClientTickEvent event)
	{
		if(event.phase == Phase.END)
		{
			Minecraft mc = Minecraft.getMinecraft();
			
			ItemModelCache.tick();
			
			if(leftClicked && !mc.gameSettings.keyBindAttack.isKeyDown())
			{
				leftClicked = false;
			}
			
			if(mc.player != null)
			{
				ItemStack stack = mc.player.getHeldItemMainhand();
				
				if(lastSelectedItem != null && lastSelectedItem != stack)
				{
					if(blockSelector != null)
					{
						blockSelector.onDeselect(mc.world, lastSelectedItem, mc.player);
						blockSelector = null;
					}
					
					if(iLittleTile != null)
					{
						iLittleTile.onDeselect(mc.player, lastSelectedItem);
						iLittleTile = null;
					}
					
					lastSelectedItem = null;
				}
			}
		}		
	}
}
