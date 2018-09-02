package me.badbones69.crazycrates.multisupport.reflectionapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Slab.Type;

import com.github.wulf.xmaterial.IMaterial;
import com.github.wulf.xmaterial.XMaterial;

import cc.bukkitPlugin.commons.nmsutil.NMSUtil;
import cc.bukkitPlugin.commons.nmsutil.Version;
import cc.bukkitPlugin.commons.nmsutil.nbt.NBTUtil;
import cc.commons.util.reflect.ClassUtil;
import cc.commons.util.reflect.MethodUtil;
import cc.commons.util.reflect.filter.MethodFilter;
import org.bukkit.block.data.type.Slab;

// TODO: finish codestyle cleanup -sgdc3
public class ReflectionUtil {
	public static Class<?> clazz_World=null;
    public static Class<?> clazz_WorldServer=null;
    public static Class<?> clazz_Block=null;
    public static Class<?> clazz_BlockPosition=null;
    public static Class<?> clazz_TileEntityEnderChest=null;
    public static Class<?> clazz_TileEntityChest=null;
    public static Class<?> clazz_TileEntity=null;
    public static Class<?> clazz_IBlockData=null;
    public static Class<?> clazz_CraftBlock=null;
    public static Class<?> clazz_NBTCompressedStreamTools=null;

    public static final Method method_World_getTileEntity;
    public static final Method method_WorldServer_getTileEntity;
    public static final Method method_World_playBlockAction;
    public static final Method method_TileEntity_getBlock;
    public static final Method method_TileEntity_save;
    public static final Method method_TileEntity_load;
    public static final Method method_IBlockData_getBlock;
    public static final Method method_NBTCompressedStreamTools_get;
    public static final Method method_NBTCompressedStreamTools_set;
    public static final Method method_NBTTagCompound_getShort;
    public static final Method method_NBTTagCompound_getByte;
    public static final Method method_NBTTagCompound_getByteArray;
    public static final Method method_NBTTagCompound_hasKey;
    public static final Method method_CraftBlock_setData;
    

	static{
		clazz_WorldServer=NMSUtil.method_CraftWorld_getHandle.getReturnType();
		clazz_World=clazz_WorldServer.getSuperclass();
		clazz_Block=NMSUtil.getNMSClass("Block");
		clazz_BlockPosition=NMSUtil.getNMSClass("BlockPosition");
		clazz_TileEntityEnderChest=NMSUtil.getNMSClass("TileEntityEnderChest");
		clazz_TileEntityChest=NMSUtil.getNMSClass("TileEntityChest");
		clazz_TileEntity=clazz_TileEntityEnderChest.getSuperclass();
		clazz_IBlockData=NMSUtil.getNMSClass("IBlockData");
		clazz_CraftBlock=NMSUtil.getCBTClass("block.CraftBlock");
		clazz_NBTCompressedStreamTools=NMSUtil.getNMSClass("NBTCompressedStreamTools");
		
		method_World_getTileEntity=MethodUtil.getDeclaredMethod(clazz_World,"getTileEntity",clazz_BlockPosition);
		method_WorldServer_getTileEntity=MethodUtil.getDeclaredMethod(clazz_WorldServer,"getTileEntity",clazz_BlockPosition);
		
		method_TileEntity_getBlock=MethodUtil.getDeclaredMethod(clazz_TileEntity,MethodFilter.rt(clazz_IBlockData).noParam()).oneGet();		
		method_IBlockData_getBlock=MethodUtil.getDeclaredMethod(clazz_IBlockData,MethodFilter.rt(clazz_Block).noParam()).oneGet();
		method_TileEntity_save=MethodUtil.getDeclaredMethod(clazz_TileEntity,"save",NBTUtil.clazz_NBTTagCompound);
		method_TileEntity_load=MethodUtil.getDeclaredMethod(clazz_TileEntity,"load",NBTUtil.clazz_NBTTagCompound);
		
		method_World_playBlockAction=MethodUtil.getDeclaredMethod(clazz_World,"playBlockAction",clazz_BlockPosition,clazz_Block,int.class,int.class);
		
		method_NBTCompressedStreamTools_get=MethodUtil.getDeclaredMethod(clazz_NBTCompressedStreamTools,MethodFilter.rpt(NBTUtil.clazz_NBTTagCompound,InputStream.class)).oneGet();	
		method_NBTCompressedStreamTools_set=MethodUtil.getDeclaredMethod(clazz_NBTCompressedStreamTools,MethodFilter.rpt(void.class,NBTUtil.clazz_NBTTagCompound,OutputStream.class)).oneGet();		
		
		method_NBTTagCompound_getShort=MethodUtil.getDeclaredMethod(NBTUtil.clazz_NBTTagCompound,MethodFilter.rpt(short.class,String.class)).oneGet();
		method_NBTTagCompound_getByteArray=MethodUtil.getDeclaredMethod(NBTUtil.clazz_NBTTagCompound,MethodFilter.rpt(byte[].class,String.class)).oneGet();
		method_NBTTagCompound_hasKey=MethodUtil.getDeclaredMethod(NBTUtil.clazz_NBTTagCompound,"hasKey",String.class);
		method_NBTTagCompound_getByte = MethodUtil.getDeclaredMethod(NBTUtil.clazz_NBTTagCompound,"getByte",String.class);
		
		method_CraftBlock_setData=MethodUtil.getDeclaredMethod(clazz_CraftBlock,"setData",byte.class);
	}
	

    
	
	public static ItemStack addUnbreaking(ItemStack item) {
        // NMS ItemStck
        Object tNMSItem=NMSUtil.getNMSItem(item);
        Object tNBTTag = NBTUtil.getItemNBT_NMS(tNMSItem);
        //NBTUtil.newNBTTagInt(4);
        NBTUtil.invokeNBTTagCompound_set(tNBTTag,"HideFlags",NBTUtil.newNBTTagInt(4));
        //NBTUtil.newNBTTagByte((byte)1);
        NBTUtil.invokeNBTTagCompound_set(tNBTTag,"Unbreakable",NBTUtil.newNBTTagByte((byte)1));
        return NBTUtil.setItemNBT(item,tNBTTag);      
	}
	
	public static ItemStack addGlow(ItemStack item) {
		if(item != null) {
			if(item.hasItemMeta()) {
				if(item.getItemMeta().hasEnchants()) {
					return item;
				}
			}
			item.addUnsafeEnchantment(Enchantment.LUCK, 1);
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			item.setItemMeta(meta);
		}
		return item;
}
	
	
	public static ItemStack getInHand(Player player) {
		return player.getInventory().getItemInMainHand();
	}
	
	public static void openChest(Block b, Location location, Boolean open) {
		
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R1))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");
		}
		else{
			Object world = NMSUtil.getNMSWorld(location.getWorld());
			Object position = ClassUtil.newInstance(clazz_BlockPosition,new Class<?>[]{double.class,double.class,double.class},new Object[]{location.getX(), location.getY(), location.getZ()});
			Object tileChest = null;
			if(b.getType() == Material.ENDER_CHEST) {
				tileChest = clazz_TileEntityEnderChest.cast(MethodUtil.invokeMethod(method_World_getTileEntity,world,position));		
			}else {
				tileChest = clazz_TileEntityChest.cast(MethodUtil.invokeMethod(method_World_getTileEntity,world,position));	
			}
			Object tileChest_getBlock = MethodUtil.invokeMethod(method_TileEntity_getBlock,tileChest);
			tileChest_getBlock = MethodUtil.invokeMethod(method_IBlockData_getBlock,tileChest_getBlock);	
			MethodUtil.invokeMethod(method_World_playBlockAction,world, position,tileChest_getBlock, 1, open ? 1 : 0);	
		}
	}
	
/*
	public static void openChest(Block b, Location location, Boolean open) {
		World world = ((org.bukkit.craftbukkit.v1_13_R2.CraftWorld) location.getWorld()).getHandle();
		BlockPosition position = new BlockPosition(location.getX(), location.getY(), location.getZ());
		if(b.getType() == Material.ENDER_CHEST) {
			TileEntityEnderChest tileChest = (TileEntityEnderChest) world.getTileEntity(position);
			world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
		}else {
			TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
			world.playBlockAction(position, tileChest.getBlock().getBlock(), 1, open ? 1 : 0);
		}
	}*/
	/*
	// http://stackoverflow.com/questions/24101928/setting-block-data-from-schematic-in-bukkit
	@SuppressWarnings("deprecation")
	public static List<Location> pasteSchematic(File f, Location loc) {
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
			return null;
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");		
			return null;
		}
		else{
			loc = loc.subtract(2, 1, 2);
			List<Location> locations = new ArrayList<Location>();
			try {
				FileInputStream fis = new FileInputStream(f);
				Object nbt =  MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_get, fis);		
				//NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
				short width = (short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Width");		
				short height =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Height");
				short length =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Length");
				byte[] blocks = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Blocks");
				byte[] data = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Data");
				fis.close();
				//paste
				for(int x = 0; x < width; ++x) {
					for(int y = 0; y < height; ++y) {
						for(int z = 0; z < length; ++z) {
							int index = y * width * length + z * width + x;
							final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
							int b = blocks[index] & 0xFF;//make the block unsigned, so that blocks with an id over 127, like quartz and emerald, can be pasted
							final Block block = l.getBlock();
							Material m = IMaterial.fromID(b+":"+data[index]);
							/*if(m==null)
							{
								m = IMaterial.fromID(b+"");
								block.setType(m);
								
							}else
							{
								block.setType(m);
							}			*/
							//MethodUtil.invokeMethod(method_CraftBlock_setData,block,data[index]);
			/*				block.setType(m);
							if(block.getBlockData() instanceof Directional){ 
								Directional directional = (Directional)block.getBlockData();
								System.out.print("有效！！！！！！！！！！！！！！"+data[index]+BlockFace.values()[data[index]].toString());
								directional.setFacing(IBlockFace.fromID((int)data[index]));
								block.setBlockData(directional);
							}
							
							//you can check what type the block is here, like if(m.equals(Material.BEACON)) to check if it's a beacon        
							locations.add(l);						
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}
	}*/
	
	/*
	// http://stackoverflow.com/questions/24101928/setting-block-data-from-schematic-in-bukkit
	@SuppressWarnings("deprecation")
	public static List<Location> pasteSchematic(File f, Location loc) {
		loc = loc.subtract(2, 1, 2);
		List<Location> locations = new ArrayList<Location>();
		try {
			FileInputStream fis = new FileInputStream(f);
			NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
			short width = nbt.getShort("Width");
			short height = nbt.getShort("Height");
			short length = nbt.getShort("Length");
			byte[] blocks = nbt.getByteArray("Blocks");
			byte[] data = nbt.getByteArray("Data");
			fis.close();
			//paste
			for(int x = 0; x < width; ++x) {
				for(int y = 0; y < height; ++y) {
					for(int z = 0; z < length; ++z) {
						int index = y * width * length + z * width + x;
						final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
						int b = blocks[index] & 0xFF;//make the block unsigned, so that blocks with an id over 127, like quartz and emerald, can be pasted
						final Block block = l.getBlock();
						Material m = IMaterial.fromID(b+"");
						block.setType(m);
						//block.setData(data[index]);
						//you can check what type the block is here, like if(m.equals(Material.BEACON)) to check if it's a beacon        
						locations.add(l);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return locations;
	}*/
	
	// http://stackoverflow.com/questions/24101928/setting-block-data-from-schematic-in-bukkit
	@SuppressWarnings("deprecation")
	public static List<Location> pasteSchematic(File f, Location loc) {
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
			return null;
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");		
			return null;
		}
		else{
			loc = loc.subtract(2, 1, 2);
			List<Location> locations = new ArrayList<Location>();
			try {
				FileInputStream fis = new FileInputStream(f);
				Object nbt =  MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_get, fis);		
				//NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
				short width = (short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Width");		
				short height =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Height");
				short length =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Length");
				byte[] blocks = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Blocks");
				byte[] data = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Data");
				fis.close();
				//paste
				for(int x = 0; x < width; ++x) {
					for(int y = 0; y < height; ++y) {
						for(int z = 0; z < length; ++z) {
							int index = y * width * length + z * width + x;
							final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
							int b = blocks[index] & 0xFF;//make the block unsigned, so that blocks with an id over 127, like quartz and emerald, can be pasted
							final Block block = l.getBlock();
							Material m = IMaterial.fromID(b+":"+data[index]);
							block.setType(m);
							if(block.getBlockData() instanceof Directional){ 		
								Directional directional = (Directional)block.getBlockData();
								if(b == 33)
								{
									directional.setFacing(BlockFace.UP);
								}else {
									switch(data[index]){
										case 0:
											directional.setFacing(BlockFace.EAST);
											break;
										case 1:
											directional.setFacing(BlockFace.WEST);
											break;
										case 2:
											directional.setFacing(BlockFace.SOUTH);
											break;
										case 3:
											directional.setFacing(BlockFace.NORTH);
											break;
										default:
											break;				
									}
								}
								block.setBlockData(directional);
							}else if(b == 43){
								block.setType(Material.STONE_SLAB);
								Slab slab = (Slab)block.getBlockData();
								slab.setType(Type.DOUBLE);	
								block.setBlockData(slab);
							}	
							//you can check what type the block is here, like if(m.equals(Material.BEACON)) to check if it's a beacon        
							locations.add(l);						
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}
	}
	/*
	public static List<Location> getLocations(File f, Location loc) {
		loc = loc.subtract(2, 1, 2);
		List<Location> locations = new ArrayList<Location>();
		try {
			FileInputStream fis = new FileInputStream(f);
			NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
			short width = nbt.getShort("Width");
			short height = nbt.getShort("Height");
			short length = nbt.getShort("Length");
			fis.close();
			//paste
			for(int x = 0; x < width; ++x) {
				for(int y = 0; y < height; ++y) {
					for(int z = 0; z < length; ++z) {
						final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
						locations.add(l);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return locations;
	}*/
	
	public static List<Location> getLocations(File f, Location loc) {
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
			return null;
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");		
			return null;
		}else {		
			loc = loc.subtract(2, 1, 2);
			List<Location> locations = new ArrayList<Location>();
			try {
				FileInputStream fis = new FileInputStream(f);
				Object nbt =  MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_get, fis);		
				//NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
				short width = (short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Width");		
				short height =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Height");
				short length =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Length");		
				//short width = nbt.getShort("Width");
				//short height = nbt.getShort("Height");
				//short length = nbt.getShort("Length");
				fis.close();
				//paste
				for(int x = 0; x < width; ++x) {
					for(int y = 0; y < height; ++y) {
						for(int z = 0; z < length; ++z) {
							final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
							locations.add(l);
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}
	}
	
	/*
	// http://stackoverflow.com/questions/24101928/setting-block-data-from-schematic-in-bukkit
	@SuppressWarnings("deprecation")
	public static List<Location> pasteSchematic(File f, Location loc) {
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
			return null;
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");		
			return null;
		}
		else{
			loc = loc.subtract(2, 1, 2);
			List<Location> locations = new ArrayList<Location>();
			try {
				FileInputStream fis = new FileInputStream(f);
				Object nbt =  MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_get, fis);		
				//NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
				short width = (short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Width");		
				short height =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Height");
				short length =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Length");
				byte[] blocks = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Blocks");
				byte[] data = (byte[])MethodUtil.invokeMethod(method_NBTTagCompound_getByteArray,nbt,"Data");
				fis.close();
				//paste
				for(int x = 0; x < width; ++x) {
					for(int y = 0; y < height; ++y) {
						for(int z = 0; z < length; ++z) {
							int index = y * width * length + z * width + x;
							final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
							int b = blocks[index] & 0xFF;//make the block unsigned, so that blocks with an id over 127, like quartz and emerald, can be pasted
							final Block block = l.getBlock();
							Material m = IMaterial.fromID(b+":"+data[index]);
							if(m==null)
							{
								m = IMaterial.fromID(b+"");
								block.setType(m);
								MethodUtil.invokeMethod(method_CraftBlock_setData,block,data[index]);
							}else
							{
								block.setType(m);
							}			
							//you can check what type the block is here, like if(m.equals(Material.BEACON)) to check if it's a beacon        
							locations.add(l);						
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}
	}
	
	public static List<Location> getLocations(File f, Location loc) {
		if(Version.getCurrentVersion().isOlder(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too far out of date. " + "Please update or remove this plugin to stop further Errors.");
			return null;
		}else if(Version.getCurrentVersion().isNewer(Version.v1_13_R2))
		{
			Bukkit.getLogger().log(Level.SEVERE, "[Crazy Crates]>> Your server is too new for this plugin. " + "Please update or remove this plugin to stop further Errors.");		
			return null;
		}else {		
			loc = loc.subtract(2, 1, 2);
			List<Location> locations = new ArrayList<Location>();
			try {
				FileInputStream fis = new FileInputStream(f);
				Object nbt =  MethodUtil.invokeStaticMethod(method_NBTCompressedStreamTools_get, fis);		
				//NBTTagCompound nbt = NBTCompressedStreamTools.a(fis);
				short width = (short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Width");		
				short height =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Height");
				short length =(short)MethodUtil.invokeMethod(method_NBTTagCompound_getShort,nbt,"Length");		
				//short width = nbt.getShort("Width");
				//short height = nbt.getShort("Height");
				//short length = nbt.getShort("Length");
				fis.close();
				//paste
				for(int x = 0; x < width; ++x) {
					for(int y = 0; y < height; ++y) {
						for(int z = 0; z < length; ++z) {
							final Location l = new Location(loc.getWorld(), x + loc.getX(), y + loc.getY(), z + loc.getZ());
							locations.add(l);
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			return locations;
		}
	}*/
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSpawnEgg(EntityType type, int amount) {	
		XMaterial mat = XMaterial.requestXMaterial("MONSTER_EGG",(byte)type.getTypeId());
		ItemStack item = mat.parseItem();
		item.setAmount(amount);
		return item;
	}

	public static Directional SetChestFace (BlockData blockdata, int id)
	{
		Directional directional = (Directional)blockdata;
		switch(id){
		case 0:
		case 1:
		case 2:
			directional.setFacing(BlockFace.NORTH);
			break;
		case 3:
			directional.setFacing(BlockFace.SOUTH);
			break;
		case 4:
			directional.setFacing(BlockFace.WEST);
			break;
		case 5:
			directional.setFacing(BlockFace.EAST);
			break;
		default:
			break;			
		}
		return directional;
	}
	
	public static ItemStack setString(ItemStack item, String key, String text) {
		return setNBTBase(item, key, NBTUtil.newNBTTagString(text));
	}
	
	public static ItemStack setBoolean(ItemStack item, String key, Boolean flag) {
		return setNBTBase(item, key, NBTUtil.newNBTTagByte((byte) (flag ? 1 : 0)));
	}
	
	public static ItemStack setInteger(ItemStack item, String key, int num) {
		return setNBTBase(item, key, NBTUtil.newNBTTagInt(num));
	}
	
	public static ItemStack setNBTBase(ItemStack item, String key, Object NBTBase) {
        Object tNMSItem=NMSUtil.getNMSItem(item);
        Object tNBTTag = NBTUtil.getItemNBT_NMS(tNMSItem);
        NBTUtil.invokeNBTTagCompound_set(tNBTTag, key, NBTBase);
        return NBTUtil.setItemNBT(item,tNBTTag);      
	}
	
	public static boolean getBoolean(ItemStack item, String key) {  
        return getByte(item,key)!= 0;
	}
	
	public static byte getByte(ItemStack item, String key) {
        Object tNMSItem=NMSUtil.getNMSItem(item);
        Object tNBTTag = NBTUtil.getItemNBT_NMS(tNMSItem);       
        Object answer = MethodUtil.invokeMethod(method_NBTTagCompound_getByte,tNBTTag,key);
        if(answer != null )
		{
			return (byte)answer;
		}
		return 0;   
	}
	
	public static boolean hasKey(ItemStack item, String key) {
		Object tNMSItem=NMSUtil.getNMSItem(item);
	    Object tNBTTag = NBTUtil.getItemNBT_NMS(tNMSItem);
		Object answer = MethodUtil.invokeMethod(method_NBTTagCompound_hasKey,tNBTTag,key);
		if(answer != null )
		{
			return (boolean)answer;
		}
		return false;
	}
	
}
