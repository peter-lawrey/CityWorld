package me.daddychurchill.CityWorld.Support;

import java.util.Random;

import me.daddychurchill.CityWorld.WorldGenerator;

import org.bukkit.Material;
import org.bukkit.World;

public class SupportChunk {
	
	public World world;
	public int chunkX;
	public int chunkZ;
	public int worldX;
	public int worldZ;
	public int width;
	public int height;
	public Random random;
	
	private byte[] ores;
	
	public static final int chunksBlockWidth = 16;
	public static final int sectionsPerChunk = 16;
	
	public static final byte airId = (byte) Material.AIR.getId();
	public static final byte stoneId = (byte) Material.STONE.getId();
	public static final byte ironId = (byte) Material.IRON_ORE.getId();
	public static final byte goldId = (byte) Material.GOLD_ORE.getId();
	public static final byte lapisId = (byte) Material.LAPIS_ORE.getId();
	public static final byte redstoneId = (byte) Material.REDSTONE_ORE.getId();
	public static final byte diamondId = (byte) Material.DIAMOND_ORE.getId();
	public static final byte coalId = (byte) Material.COAL_ORE.getId();
	public static final byte dirtId = (byte) Material.DIRT.getId();
	public static final byte grassId = (byte) Material.GRASS.getId();
	
	public SupportChunk(WorldGenerator generator, Random aRandom) {
		super();
		
		world = generator.getWorld();
		random = aRandom;
		
		width = chunksBlockWidth;
		height = generator.height;
	}
	
	public int getBlockX(int x) {
		return getOriginX() + x;
	}

	public int getBlockZ(int z) {
		return getOriginZ() + z;
	}

	public int getOriginX() {
		return chunkX * width;
	}

	public int getOriginZ() {
		return chunkZ * width;
	}

	public byte getOre(int y) {
		// a VERY VERY rough approximation of http://www.minecraftwiki.net/wiki/Ore
		
		// haven't been here before
		if (ores == null)
			ores = new byte[sectionsPerChunk];
		
		// what is the random ore for this section?
		int section = y >> 4;
		if (ores[section] == 0) {
			if (inRange(y, 14, 200)) //      diamond, red, lapis, gold, iron, coal
				ores[section] = pickRandomMineral(13);
			else if (inRange(y, 16, 192)) // red, lapis, gold, iron, coal
				ores[section] = pickRandomMineral(11);
			else if (inRange(y, 30, 137)) // lapis, gold, iron, coal
				ores[section] = pickRandomMineral(9);  
			else if (inRange(y, 32, 129)) // gold, iron, coal
				ores[section] = pickRandomMineral(8); 
			else if (inRange(y, 66, 100)) // iron, coal
				ores[section] = pickRandomMineral(6);  
			else //                          coal
				ores[section] = pickRandomMineral(1); 
		}
		
		// return it
		return ores[section];
	}
	
	private byte pickRandomMineral(int max) {
		switch (random.nextInt(max)) {
		default:                     // 67--99   
		case 1:
		case 2: return coalId;
		case 3:
		case 4:
		case 5:                      // 33--128
		case 6: return ironId;
		case 7: return goldId;       // 31--136
		case 8: return lapisId;      // 17--191
		case 9:
		case 10:                     // 15--199
		case 11: return redstoneId;
		case 12: return diamondId;   //  0--255
		}
	}
	
	private boolean inRange(int blockY, int lower, int upper) {
		return blockY <= lower || blockY >= upper;
	}

}
