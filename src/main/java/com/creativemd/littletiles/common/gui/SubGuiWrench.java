package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.custom.GuiItemListBox;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException;
import com.creativemd.littletiles.common.action.block.NotEnoughIngredientsException.NotEnoughVolumeExcepion;
import com.creativemd.littletiles.common.container.SubContainerWrench;
import com.creativemd.littletiles.common.ingredients.ColorUnit;
import com.creativemd.littletiles.common.ingredients.BlockIngredient;
import com.creativemd.littletiles.common.ingredients.BlockIngredient.BlockIngredients;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SubGuiWrench extends SubGui {

	@Override
	public void createControls() {
		controls.add(new GuiButton("Craft", 55, 3, 40){

			@Override
			public void onClicked(int x, int y, int button) {
				ItemStack stack1 = ((SubContainerWrench)container).basic.getStackInSlot(0);
				ItemStack stack2 = ((SubContainerWrench)container).basic.getStackInSlot(1);
				
				GuiItemListBox listBox = (GuiItemListBox) get("missing");
				GuiLabel label = (GuiLabel) get("label");
				label.caption = "";
				listBox.clear();
				
				if(!stack1.isEmpty())
				{
					if(stack1.getItem() instanceof ItemRecipe)
					{
						List<LittleTilePreview> previews = LittleTilePreview.getPreview(stack1);
						
						EntityPlayer player = getPlayer();
						
						ColorUnit color = new ColorUnit();
						BlockIngredients ingredients = new BlockIngredients();
						for (LittleTilePreview preview : previews) {
							if(preview.canBeConvertedToBlockEntry())
							{
								ingredients.addIngredient(preview.getBlockIngredient());
								color.addColorUnit(ColorUnit.getRequiredColors(preview));
							}
						}
						try {
							if(LittleAction.drainIngredients(player, ingredients, color))
							{
								sendPacketToServer(new NBTTagCompound());
							}
						} catch (NotEnoughIngredientsException e) {
							if(e instanceof NotEnoughVolumeExcepion)
							{
								for (BlockIngredient ingredient : ingredients.getIngredients()) {
									listBox.add(ingredient.value > 1 ? ingredient.value + " blocks" : (int) (ingredient.value*LittleTile.maxTilesPerBlock) + " pixels", ingredient.getItemStack());
								}
							}else
								label.caption = e.getLocalizedMessage();
						}
						
						
					}
				}
				
			}
			
		});
		controls.add(new GuiItemListBox("missing", 5, 25, 160, 50, new ArrayList<ItemStack>(), new ArrayList<String>()));
		controls.add(new GuiLabel("label", "", 100, 5));
	}

}
