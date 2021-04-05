package fr.olympa.pvpfac.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.region.tracking.ActionResult;
import fr.olympa.api.region.tracking.TrackedRegion;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.pvpfac.PvPFaction;
import net.minecraft.server.v1_16_R3.HeightMap.Type;

public class WorldsManager {
	
	private static final int loadChunks = 15;
	public static final WorldType CLAIM_WORLD = WorldType.OVERWORLD;
	
	private final Map<World, WorldType> worlds = new HashMap<World, WorldsManager.WorldType>();
	
	private PvPFaction plugin;
	
	private YamlConfiguration config = new YamlConfiguration();
	private File configFile;
	
	private WorldCommand worldCmd;
	
	public WorldsManager(PvPFaction plugin) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.plugin = plugin;
		
		worldCmd = new WorldCommand(plugin);
		worldCmd.register();
		
		configFile = new File(plugin.getDataFolder() + "/worlds.yml");
		plugin.getDataFolder().mkdirs();
		if (!configFile.createNewFile())
			config.load(configFile);
		else {
			for (WorldType type : WorldType.values()) {
				config.set(type + ".reset_interval", type.getDefaultResetDays());
				config.set(type + ".next_reset", 0);	
				config.set(type + ".portal", null);	
			}

			config.save(configFile);
		}
		
		
		for (WorldType type : WorldType.values()) {
			File worldFile = new File(plugin.getServer().getWorldContainer().getAbsolutePath() + "/" + type.getWorldName());
			
			//reset worlds if needed
			if (System.currentTimeMillis() > config.getLong(type.toString() + ".next_reset") && config.getInt(type.toString() + ".reset_interval") > 0) {
				
				if (worldFile.exists())
					Stream.of(worldFile.listFiles()).forEach(f -> f.delete());
				
				worldFile.delete();
				config.set(type.toString() + ".next_reset", System.currentTimeMillis() + config.getLong(type.toString() + ".reset_interval") * 24 * 3600 * 1000 - 2 * 3600 * 1000);
				config.save(configFile);
				
				plugin.getLogger().info("§2World " + type + " will reset, this may take a while, generating " + loadChunks*loadChunks + " chunks...");
			}else
				plugin.getLogger().info("§aWorld " + type + " is loading...");
			
			//set world parameters
			type.setWorld(new WorldCreator(type.getWorldName()).environment(type.getType()).createWorld());
			type.getWorld().setPVP(type.canPvp());
			type.getWorld().setDifficulty(Difficulty.NORMAL);
			type.getWorld().setGameRule(GameRule.DISABLE_RAIDS, false);
			type.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			type.getWorld().setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			
			worlds.put(type.getWorld(), type);
			
			//preload chunks in min radius
			for (int x = -loadChunks ; x <= loadChunks ; x++)
				for (int z = -loadChunks ; z <= loadChunks ; z++)
					type.getWorld().getChunkAt(x, z);
			
			//manage portals
			if (config.get(type + ".portal", null) != null)
				OlympaCore.getInstance().getRegionManager().registerRegion((Cuboid) config.get(type + ".portal"), "portal_world_" + type.toString().toLowerCase(), EventPriority.HIGHEST,
					new Flag() {
						@Override
						public ActionResult enters(Player p, Set<TrackedRegion> to) {
							super.enters(p, to);
							type.teleport(p);
							Prefix.FACTION.sendMessage(p, "§aTu vas être téléporté vers le monde " + type.getWorldName() + "...");
							return ActionResult.TELEPORT_ELSEWHERE;
						}
					});
		}	
	}
	
	public boolean isClaimWorld(World w) {
		return worlds.get(w) == CLAIM_WORLD;
	}
	
	public WorldType getWorldType(World w) {
		return worlds.get(w);
	}

	public void setPortal(WorldType world, Cuboid region) {
		config.set(world + ".portal", region);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			plugin.getLogger().warning("§cFailed to save new portal loc for world " + world);
			e.printStackTrace();
		}
	}
	
	public static enum WorldType {
		OVERWORLD("world", -1, Environment.NORMAL, false, true),
		NETHER("nether", 7, Environment.NETHER, true, false),
		END("end", 7, Environment.THE_END, true, false),
		MINING("mining", 7, Environment.NORMAL, true, false),
		;
		
		
		private WorldType(String worldName, int defaultResetDays, Environment type, boolean useRandomTp, boolean canPvp) {
			this.worldName = worldName;
			this.defaultResetDays = defaultResetDays;
			this.type = type;
			this.useRandomTp = useRandomTp;
			this.canPvp = canPvp;
		}

		private String worldName;
		private int defaultResetDays;
		private Environment type;
		
		private boolean useRandomTp;
		private boolean canPvp;
		
		private World world;
		
		private void setWorld(World world) {
			this.world = world;
		}
		
		public String getWorldName() {
			return worldName;
		}
		
		public int getDefaultResetDays() {
			return defaultResetDays;
		}
		
		public Environment getType() {
			return type;
		}
		
		public boolean canPvp() {
			return canPvp;
		}
		
		public World getWorld() {
			return world;
		}
		
		public void teleport(Player p) {
			if (useRandomTp)
				teleportRandom(p, 0, 10);
			else
				teleportSpawn(p);
		}
		
		public void teleportSpawn(Player p) {			
			world.getChunkAtAsync(world.getSpawnLocation(), new Consumer<Chunk>(){
				@Override
				public void accept(Chunk ch) {
					p.teleport(world.getSpawnLocation());	
				}
			});
		}
		
		public void teleportRandom(Player p, int minRadiusChunk, int radiusChunk) {
			ThreadLocalRandom r = ThreadLocalRandom.current();
			
			world.getChunkAtAsync(
					r.nextBoolean() ? r.nextInt(minRadiusChunk, radiusChunk + 1) : -r.nextInt(minRadiusChunk, radiusChunk + 1), 
					r.nextBoolean() ? r.nextInt(minRadiusChunk, radiusChunk + 1) : -r.nextInt(minRadiusChunk, radiusChunk + 1), 
					new Consumer<Chunk>(){
				@Override
				public void accept(Chunk ch) {
					int x = ch.getX() * 16 + r.nextInt(16);
					int z = ch.getZ() * 16 + r.nextInt(16);
					int y = ((CraftChunk)ch).getHandle().heightMap.get(Type.MOTION_BLOCKING).a(x & 0xF, z & 0xF);
					
					if (y > 100 || y < 30)
						teleportRandom(p, minRadiusChunk, radiusChunk);
					else {
						p.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
						p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, true, false, false));
					}
				}
			});
		}
		
		
		public static WorldType fromString(String s) {
			for (WorldType w : WorldType.values())
				if (w.getWorldName().equals(s))
					return w;
			return null;
		}
	}
}
