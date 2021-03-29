package fr.olympa.pvpfac.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.olympa.pvpfac.PvPFaction;
import net.minecraft.server.v1_16_R3.HeightMap.Type;

public class WorldManager {
	
	private static final int loadChunks = 15;
	
	//private Map<WorldType, World> worlds = new HashMap<WorldType, World>();
	
	private PvPFaction plugin;
	private YamlConfiguration config = new YamlConfiguration();
	private File configFile;
	
	public WorldManager(PvPFaction plugin) throws FileNotFoundException, IOException, InvalidConfigurationException {
		this.plugin = plugin;
		
		configFile = new File(plugin.getDataFolder() + "worlds.yml");
		if (!configFile.mkdirs())
			config.load(configFile);
		else {
			for (WorldType type : WorldType.values()) {
				config.set(type.toString() + ".reset_interval", type.getDefaultResetDays());
				config.set(type.toString() + ".next_reset", 0);	
			}

			config.save(configFile);
		}
		
		
		for (WorldType type : WorldType.values()) {
			File worldFile = new File(plugin.getDataFolder().getParentFile().getParentFile().getPath() + type.getWorldName());
			if (System.currentTimeMillis() > config.getLong(type.toString() + ".next_reset") && config.getInt(type.toString() + ".reset_interval") > 0) {
				Stream.of(worldFile.listFiles()).forEach(f -> f.delete());
				worldFile.delete();
				config.set(type.toString() + ".next_reset", System.currentTimeMillis() + config.getLong(type.toString() + ".reset_interval") * 24 * 3600 * 1000 - 2 * 3600 * 1000);
				config.save(configFile);
				
				plugin.getLogger().info("§2World " + type + " will reset, this may take a while, generating " + loadChunks*loadChunks + " chunks...");
			}else
				plugin.getLogger().info("§aWorld " + type + " is loading...");
			
			type.setWorld(new WorldCreator(type.getWorldName()).environment(type.getType()).createWorld());
			type.getWorld().setPVP(type.canPvp());
			type.getWorld().setDifficulty(Difficulty.NORMAL);
			type.getWorld().setGameRule(GameRule.DISABLE_RAIDS, false);
			type.getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			type.getWorld().setGameRule(GameRule.DO_TRADER_SPAWNING, false);
			
			//preload chunks in min radius
			for (int x = -loadChunks ; x <= loadChunks ; x++)
				for (int z = -loadChunks ; z <= loadChunks ; z++)
					type.getWorld().getChunkAt(x, z);
		}
		
		
	}
	
	public static enum WorldType {
		OVERWORLD("world", -1, Environment.NORMAL, false, true),
		NETHER("nether", 7, Environment.NETHER, true, false),
		END("end", 7, Environment.THE_END, true, false),
		MINE("mine", 7, Environment.NORMAL, true, false),
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
		
		public void tpPlayer(Player p) {
			if (useRandomTp)
				tpPlayerRandom(p, 0, 10);
			else
				tpPlayerSpawn(p);
		}
		
		public void tpPlayerSpawn(Player p) {			
			world.getChunkAtAsync(world.getSpawnLocation(), new Consumer<Chunk>(){
				@Override
				public void accept(Chunk ch) {
					p.teleport(world.getSpawnLocation());	
				}
			});
		}
		
		public void tpPlayerRandom(Player p, int minRadius, int radius) {
			ThreadLocalRandom r = ThreadLocalRandom.current();
			 
			world.getChunkAtAsync(
					r.nextBoolean() ? r.nextInt(minRadius, radius + 1) : -r.nextInt(minRadius, radius + 1), 
					r.nextBoolean() ? r.nextInt(minRadius, radius + 1) : -r.nextInt(minRadius, radius + 1), 
					new Consumer<Chunk>(){
				@Override
				public void accept(Chunk ch) {
					int x = ch.getX() * 16 + r.nextInt(16);
					int z = ch.getZ() * 16 + r.nextInt(16);
					int y = ((CraftChunk)ch).getHandle().heightMap.get(Type.MOTION_BLOCKING).a(x & 0xF, z & 0xF);
					
					if (y > 100 || y < 30)
						tpPlayerRandom(p, minRadius, radius);
					else {
						p.teleport(new Location(world, x + 0.5, y + 1, z + 0.5));
						p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, true, false, false));
					}
				}
			});
		}
	}
}
