package me.daddychurchill.CityWorld;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import me.daddychurchill.CityWorld.Clipboard.PasteProvider;
import me.daddychurchill.CityWorld.Plugins.BalloonProvider;
import me.daddychurchill.CityWorld.Plugins.BonesProvider;
import me.daddychurchill.CityWorld.Plugins.CoverProvider;
import me.daddychurchill.CityWorld.Plugins.TreeProvider;
import me.daddychurchill.CityWorld.Plugins.StructureProvider;
import me.daddychurchill.CityWorld.Plugins.LootProvider;
import me.daddychurchill.CityWorld.Plugins.OdonymProvider;
import me.daddychurchill.CityWorld.Plugins.OreProvider;
import me.daddychurchill.CityWorld.Plugins.ShapeProvider;
import me.daddychurchill.CityWorld.Plugins.SpawnProvider;
import me.daddychurchill.CityWorld.Plugins.SurfaceProvider;
import me.daddychurchill.CityWorld.Support.InitialBlocks;
import me.daddychurchill.CityWorld.Support.Odds;
import me.daddychurchill.CityWorld.Support.PlatMap;
import me.daddychurchill.CityWorld.Support.RealBlocks;
import me.daddychurchill.CityWorld.Support.WorldBlocks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class CityWorldGenerator extends ChunkGenerator {

	private CityWorld plugin;
	private World world;
	private Long worldSeed;
	private Odds connectionKeyGen;
	
	public String worldName;
	public WorldStyle worldStyle;
	public Environment worldEnvironment;
	
	public ShapeProvider shapeProvider;
	public PasteProvider pasteProvider;
	public LootProvider lootProvider;
	public SpawnProvider spawnProvider;
	public OreProvider oreProvider;
	public BonesProvider bonesProvider;
	public SurfaceProvider surfaceProvider;
	public CoverProvider coverProvider;
	public OdonymProvider odonymProvider;
	public BalloonProvider balloonProvider;
	public StructureProvider structureProvider;
	public TreeProvider treeProvider;
	
	public WorldBlocks decayBlocks;
	
	public CityWorldSettings settings;

	public int streetLevel;
	
	public int deepseaLevel;
	public int seaLevel;
	public int structureLevel;
	public int treeLevel;
	public int evergreenLevel;
	public int deciduousRange;
	public int evergreenRange;
	public int height;
	public int snowLevel;
	public int landRange;
	public int seaRange;
	
	public long connectedKeyForPavedRoads;
	public long connectedKeyForParks;
	
	public double minecraftVer;
	
	public enum WorldStyle {
		FLOATING,		// very low terrain with floating houses and cities
		FLOODED,		// traditional terrain and cities but with raised sea level
		SNOWDUNES,		// traditional terrain and cities but covered with snow dunes
		SANDDUNES,		// traditional terrain and cities but covered with sand dunes
		ASTRAL,			// alien landscape 
		MAZE,			// mazes with smaller cities
		//MOON,			// lunar landscape with lunar bases
		//UNDERWATER,	// traditional terrain with raised sea level with under water cities
		//WESTERN,		// desert landscape with sparse western styled towns and ranches
		//UNDERGROUND,	// elevated terrain with underground cities
		//MINING,		// elevated terrain with very shallow mines and very small towns
		DESTROYED,		// normal landscape with destroyed cities
		NORMAL};   		// traditional terrain and cities
	
	public static WorldStyle validateStyle(WorldStyle style) {
//		switch (style) {
//		case FLOATING:
////		case MAZE:
////		case ASTRAL:
////		case FLOODED:
////		case SANDDUNES:
////		case SNOWDUNES:
//			CityWorld.log.info("[Generator] " + style + " worlds unavailable due to performance issues, switching to NORMAL");
//			return WorldStyle.NORMAL;
//		default:
//		}
		return style;
	}
		
	public CityWorldGenerator(CityWorld plugin, String worldName, String worldStyle) {
		this.plugin = plugin;
		this.worldName = worldName;
		this.worldStyle = WorldStyle.NORMAL;
		
		try {
			String versionTxt = plugin.getServer().getVersion();
			int mcAt = versionTxt.indexOf("MC: ");
			if (mcAt != -1) {
				versionTxt = versionTxt.substring(mcAt + 4, mcAt + 7);
				this.minecraftVer = Double.parseDouble(versionTxt);
			} else
				this.minecraftVer = 0;
		} catch (NumberFormatException e) {
			this.minecraftVer = 0;
		}
		
		// parse the style string
		if (worldStyle != null) {
			try {
				this.worldStyle = validateStyle(WorldStyle.valueOf(worldStyle.trim().toUpperCase()));
			} catch (IllegalArgumentException e) {
				reportMessage("[Generator] Unknown world style " + worldStyle + ", switching to NORMAL");
				this.worldStyle = WorldStyle.NORMAL;
			}
		}
	}

	public CityWorld getPlugin() {
		return plugin;
	}
	
	public String getPluginName() {
		return plugin.getPluginName();
	}
	
	public World getWorld() {
		return world;
	}

	public Long getWorldSeed() {
		return worldSeed;
	}
	
	private int deltaSeed = 0;
	public Long getRelatedSeed() {
		deltaSeed++;
		return worldSeed + deltaSeed;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList((BlockPopulator) new CityWorldBlockPopulator(this));
	}
	
	public void initializeWorldInfo(World aWorld) {
		
		// initialize the shaping logic
		if (world == null) {
			world = aWorld;
			settings = new CityWorldSettings(this);
			worldSeed = world.getSeed();
			connectionKeyGen = new Odds(getRelatedSeed());

			shapeProvider = ShapeProvider.loadProvider(this, new Odds(getRelatedSeed()));
			lootProvider = LootProvider.loadProvider(this);
			spawnProvider = SpawnProvider.loadProvider(this);
			oreProvider = OreProvider.loadProvider(this);
			bonesProvider = BonesProvider.loadProvider(this);
			coverProvider = CoverProvider.loadProvider(this, new Odds(getRelatedSeed()));
			odonymProvider = OdonymProvider.loadProvider(this, new Odds(getRelatedSeed()));
			surfaceProvider = SurfaceProvider.loadProvider(this, new Odds(getRelatedSeed()));
			balloonProvider = BalloonProvider.loadProvider(this);
			structureProvider = StructureProvider.loadProvider(this);
			treeProvider = TreeProvider.loadProvider(this, new Odds(getRelatedSeed()));
			pasteProvider = PasteProvider.loadProvider(this);
			decayBlocks = new WorldBlocks(this, new Odds(getRelatedSeed()));
			
			// get ranges and contexts
			height = shapeProvider.getWorldHeight();
			seaLevel = shapeProvider.getSeaLevel();
			landRange = shapeProvider.getLandRange();
			seaRange = shapeProvider.getSeaRange();
			structureLevel = shapeProvider.getStructureLevel();
			streetLevel = shapeProvider.getStreetLevel();
			
			// did we load any schematics?
			pasteProvider.reportStatus(this);

			// now the other vertical points
			deepseaLevel = seaLevel - seaRange / 3;
			snowLevel = seaLevel + (landRange / 4 * 3);
			evergreenLevel = seaLevel + (landRange / 4 * 2);
			treeLevel = seaLevel + (landRange / 4);
			deciduousRange = evergreenLevel - treeLevel;
			evergreenRange = snowLevel - evergreenLevel;
			
//				// seabed = 35 deepsea = 50 sea = 64 sidewalk = 65 tree = 110 evergreen = 156 snow = 202 top = 249
//				CityWorld.reportMessage("seabed = " + (seaLevel - seaRange) + 
//								        " deepsea = " + deepseaLevel + 
//								        " sea = " + seaLevel + 
//								        " sidewalk = " + sidewalkLevel + 
//								        " tree = " + treeLevel + 
//								        " evergreen = " + evergreenLevel + 
//								        " snow = " + snowLevel + 
//								        " top = " + (seaLevel + landRange));
			
			// get the connectionKeys
			connectedKeyForPavedRoads = connectionKeyGen.getRandomLong();
			connectedKeyForParks = connectionKeyGen.getRandomLong();
			
//			reportMessage("Plugins...");
//			PluginManager pm = Bukkit.getServer().getPluginManager();
//			Plugin[] plugins = pm.getPlugins();
//			for (Plugin plugin: plugins) {
//				reportMessage("Plugin = " + plugin.getName());
//			}
		}
	}
	
	@Override
	public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
		try {

			initializeWorldInfo(world);

			// place to work
			InitialBlocks initialBlocks = new InitialBlocks(this, this.createChunkData(world), x, z);
		
			// figure out what everything looks like
			PlatMap platmap = getPlatMap(x, z);
			if (platmap != null) {
				//CityWorld.reportMessage("generate X,Z = " + chunkX + "," + chunkZ);
				platmap.generateChunk(initialBlocks, biome);
			}
			
			return initialBlocks.chunkData;
			
		} catch (Exception e) {
			reportException("ChunkPopulator FAILED", e);
			return null;
		} 
	}
	
	public long getConnectionKey() {
		return connectionKeyGen.getRandomLong();
	}
	
	public int getFarBlockY(int blockX, int blockZ) {
		return shapeProvider.findBlockY(this, blockX, blockZ);
	}
	
//	@Override
//	public Location getFixedSpawnLocation(World world, Random random) {
//		
////		// guess a location
////		int spawnX = random.nextInt(spawnRadius * 2) - spawnRadius;
////		int spawnZ = random.nextInt(spawnRadius * 2) - spawnRadius;
////		
////		// find a general height
////		int spawnY = getFarBlockY(spawnX, spawnZ);
////		int maxY = world.getMaxHeight();
////		
////		// find the first empty block
////		while (spawnY < maxY) {
////			if (world.getBlockAt(spawnX, spawnY, spawnZ).isEmpty() && 
////				world.getBlockAt(spawnX, spawnY + 1, spawnZ).isEmpty())
////				return new Location(world, spawnX, spawnY, spawnZ);
////			
////			// little higher up then
////			spawnY++;
////		}
////		
////		// still nothing?
////		return new Location(world, spawnX, spawnY, spawnZ);
//		CityWorld.log.info("******* getFixedSpawnLocation = 0, 250, 0");
//		return new Location(world, 0, world.getHighestBlockYAt(0, 0), 0);
//	}

	// manager for handling the city plat maps collection
	private Hashtable<Long, PlatMap> platmaps;
	public PlatMap getPlatMap(int chunkX, int chunkZ) {

		// get the plat map collection
		if (platmaps == null)
			platmaps = new Hashtable<Long, PlatMap>();

		// find the origin for the plat
		int platX = calcOrigin(chunkX);
		int platZ = calcOrigin(chunkZ);

		// calculate the plat's key
		Long platkey = Long.valueOf(((long) platX * (long) Integer.MAX_VALUE + (long) platZ));

		// get the right plat
		PlatMap platmap = platmaps.get(platkey);
		
		// doesn't exist? then make it!
		if (platmap == null) {
			
			// what is the context for this one?
			platmap = new PlatMap(this, shapeProvider, platX, platZ);
			
			// remember it for quicker look up
			platmaps.put(platkey, platmap);
		}

		// finally return the plat
		return platmap;
	}

	// Supporting code used by getPlatMap
	private int calcOrigin(int i) {
		if (i >= 0) {
			return i / PlatMap.Width * PlatMap.Width;
		} else {
			return -((Math.abs(i + 1) / PlatMap.Width * PlatMap.Width) + PlatMap.Width);
		}
	}
	
	public void reportMessage(String message) {
		plugin.reportMessage(message);
	}

	public void reportMessage(String message1, String message2) {
		plugin.reportMessage(message1, message2);
	}
	
	public void reportFormatted(String format, Object ... objects) {
		plugin.reportMessage(String.format(format, objects));
	}

	public void reportException(String message, Exception e) {
		plugin.reportException(message, e);
	}

	private class CityWorldBlockPopulator extends BlockPopulator {

		private CityWorldGenerator chunkGen;

		public CityWorldBlockPopulator(CityWorldGenerator chunkGen) {
			this.chunkGen = chunkGen;
		}

		@Override
		public void populate(World aWorld, Random random, Chunk chunk) {
			try {

				chunkGen.initializeWorldInfo(aWorld);
				
				// where are we?
				int chunkX = chunk.getX();
				int chunkZ = chunk.getZ();
				
				// place to work
				RealBlocks realChunk = new RealBlocks(chunkGen, chunk);

				// figure out what everything looks like
				PlatMap platmap = chunkGen.getPlatMap(chunkX, chunkZ);
				if (platmap != null) {
					platmap.generateBlocks(realChunk);
					
					// finalize things
					chunkGen.lootProvider.saveLoots();
					
					// Originally by Sablednah
					// Moved and modified a bit by DaddyChurchill
					CityWorldEvent event = new CityWorldEvent(chunk, platmap, platmap.getMapLot(chunkX, chunkZ));
					Bukkit.getServer().getPluginManager().callEvent(event);
				}
			} catch (Exception e) {
				reportException("BlockPopulator FAILED", e);
			} 
		}
	}
}
