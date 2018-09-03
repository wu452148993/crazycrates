package me.badbones69.crazycrates.multisupport;

import org.black_ixx.bossshop.BossShop;
import org.bukkit.inventory.ItemStack;

import cc.bukkitPlugin.bossshop.commons.Log;
import cc.bukkitPlugin.bossshop.commons.plugin.ABukkitPlugin;
import cc.bukkitPlugin.bossshop.nbt.NBT;
import cc.bukkitPlugin.bossshop.nbt.NBTEditManager;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTSerializer;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.bukkitPlugin.commons.nmsutil.nbt.exception.NBTDeserializeException;
import cc.commons.util.ByteUtil;
import me.badbones69.crazycrates.api.CrazyCrates;

public class BossShopSupport {

	private static boolean install = false;
		
	public static void loaded ()
	{
		install = true;
		System.out.println(CrazyCrates.getFileManager().getPrefix() + "BossShop is loaded.");
	}
	
	public static boolean checkloaded()
	{
		if(install == false)
		{
			System.out.println(CrazyCrates.getFileManager().getPrefix() + "warning:BossShop can't loaded.");
			return false;
		}
		return true;
	}
	
	
	public static BossShop getBossShop()
	{
		return (BossShop)ABukkitPlugin.getInstance();
	}
	
	public static Object ConfigtoNBT(String i)
	{
		if(!checkloaded())
		{
			return null;
		}    
        if(i.startsWith("Nbt:")){
        	return nbtToNBT(i.replaceAll("Nbt:", ""));
        }else{
        	return rawnbtToNBT(i.replaceAll("Rawnbt:", ""));
        }
/*
        if(i.startsWith("Nbt:")){
        	tPropValue = stringFix(i.replaceAll("Nbt:", ""));
            NBT tCNBT=getBossShop().getManager(NBTEditManager.class).getItemNBT(tPropValue,true);
            if(tCNBT==null){
                Log.warn(getBossShop().C("MsgMissingNBTNode")+"["+tPropValue+"]");
                return null;
            }
            NBT=tCNBT.getNBTCopy();
        }else{
        	tPropValue = stringFix(i.replaceAll("Rawnbt:", ""));
            try{
            	NBT=NBTSerializer.deserializeNBTFromByte(ByteUtil.base64ToByte(tPropValue));
            }catch(NBTDeserializeException exp){
                Log.severe("反序列化物品NBT时发生了错误",exp);
                return null;
            }
        }
        return NBT;*/
	}
	
	public static Object nbtToNBT(String text) 
	{
		text = stringFix(text);
        NBT tCNBT=getBossShop().getManager(NBTEditManager.class).getItemNBT(text,true);
        if(tCNBT==null){
            Log.warn(getBossShop().C("MsgMissingNBTNode")+"["+text+"]");
            return null;
        }
        return tCNBT.getNBTCopy();
	}
	
	public static Object rawnbtToNBT(String text) 
	{
		text = stringFix(text);
        try{
        	return NBTSerializer.deserializeNBTFromByte(ByteUtil.base64ToByte(text));
        }catch(NBTDeserializeException exp){
            Log.severe("反序列化物品NBT时发生了错误",exp);
            return null;
        }
	}
	
	public static ItemStack setItemNBT (Object NBT,ItemStack item) {
		if(!checkloaded())
		{
			return null;
		}
		//System.out.print("!!!!!!!!!!!!!!!!!!!");
        Object tNBTExist=NBTUtil.getItemNBT(item);
        if(tNBTExist!=null){
            tNBTExist=NBTUtil.mixNBT(tNBTExist,NBT,false);
        }else{
            tNBTExist=NBT;
        }
        ItemStack tResult=NBTUtil.setItemNBT(item,tNBTExist);
        if(tResult!=null) item=tResult;
        
        return item;
	}
	
    private static String stringFix(String s){
        if(s.contains(" ")){
            s=s.replaceAll(" ","");
        }
        return s;
    }
}
