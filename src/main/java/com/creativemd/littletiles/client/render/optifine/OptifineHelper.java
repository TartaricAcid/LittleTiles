package com.creativemd.littletiles.client.render.optifine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import shadersmod.client.SVertexBuilder;

public class OptifineHelper {
	
	private static Method isShadersMethod = getIsShaderMethod();
	
	private static Method getIsShaderMethod()
	{
		try {
			return Class.forName("Config").getMethod("isShaders");
		} catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isShaders()
	{
		try {
			return (boolean) isShadersMethod.invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return false;
	}

}
