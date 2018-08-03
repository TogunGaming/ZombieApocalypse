package misat11.za;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import misat11.za.commands.ZaCommand;
import misat11.za.game.Game;
import misat11.za.game.GamePlayer;
import misat11.za.listener.PlayerListener;
import misat11.za.utils.Configurator;
import net.milkbowl.vault.economy.Economy;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Main extends JavaPlugin {
	private static Main instance;
	private String version;
	private boolean isSpigot, snapshot, isVault;
	private Economy econ = null;
	private HashMap<String, Game> games = new HashMap<String, Game>();
	private HashMap<Player, GamePlayer> playersInGame = new HashMap<Player, GamePlayer>();
	private Configurator configurator;

	public static Main getInstance() {
		return instance;
	}
	
	public static Configurator getConfigurator() {
		return instance.configurator;
	}

	public static String getVersion() {
		return instance.version;
	}

	public static boolean isSnapshot() {
		return instance.snapshot;
	}

	public static boolean isVault() {
		return instance.isVault;
	}

	public static boolean isSpigot() {
		return instance.isSpigot;
	}

	public static Economy getEcon() {
		return instance.econ;
	}

	public static Game getGame(String string) {
		return instance.games.get(string);
	}

	public static boolean isGameExists(String string) {
		return instance.games.containsKey(string);
	}

	public static void addGame(Game game) {
		instance.games.put(game.getName(), game);
	}

	public static void removeGame(Game game) {
		instance.games.remove(game.getName());
	}

	public static boolean isPlayerInGame(Player player) {
		if (instance.playersInGame.containsKey(player))
			if (instance.playersInGame.get(player).isInGame())
				return true;
		return false;
	}

	public static GamePlayer getPlayerGameProfile(Player player) {
		if (instance.playersInGame.containsKey(player))
			return instance.playersInGame.get(player);
		GamePlayer gPlayer = new GamePlayer(player);
		instance.playersInGame.put(player, gPlayer);
		return gPlayer;
	}

	public static void unloadPlayerGameProfile(Player player) {
		if (instance.playersInGame.containsKey(player)) {
			instance.playersInGame.get(player).changeGame(null);
			instance.playersInGame.remove(player);
		}
	}

	public static boolean isPlayerGameProfileRegistered(Player player) {
		return instance.playersInGame.containsKey(player);
	}

	public void onEnable() {
		instance = this;
		version = this.getDescription().getVersion();
		snapshot = version.toLowerCase().contains("pre");

		try {
			Package spigotPackage = Package.getPackage("org.spigotmc");
			isSpigot = (spigotPackage != null);
		} catch (Exception e) {
			isSpigot = false;
		}

		if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
			isVault = false;
		} else {
			setupEconomy();
			isVault = true;
		}

		configurator = new Configurator(this);

		configurator.createFiles();

		getCommand("za").setExecutor(new ZaCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		Bukkit.getLogger().info("********************");
		Bukkit.getLogger().info("* ZombieApocalypse *");
		Bukkit.getLogger().info("*    by Misat11    *");
		Bukkit.getLogger().info("*                  *");
		if (version.length() == 10) {
			Bukkit.getLogger().info("*                  *");
			Bukkit.getLogger().info("*    V" + version + "   *");
		} else {
			Bukkit.getLogger().info("*      V" + version + "      *");
		}
		Bukkit.getLogger().info("*                  *");
		if (snapshot == true) {
			Bukkit.getLogger().info("* SNAPSHOT VERSION *");
		} else {
			Bukkit.getLogger().info("*  STABLE VERSION  *");
		}
		Bukkit.getLogger().info("*                  *");

		if (isVault == true) {
			Bukkit.getLogger().info("*                  *");
			Bukkit.getLogger().info("*   Vault hooked   *");
			Bukkit.getLogger().info("*                  *");
		}

		if (isSpigot == false) {
			Bukkit.getLogger().info("*                  *");
			Bukkit.getLogger().info("*     WARNING:     *");
			Bukkit.getLogger().info("* You aren't using *");
			Bukkit.getLogger().info("*      Spigot      *");
			Bukkit.getLogger().info("*                  *");
			Bukkit.getLogger().info("* Please download! *");
			Bukkit.getLogger().info("*   spigotmc.org   *");
		}

		Bukkit.getLogger().info("*                  *");
		Bukkit.getLogger().info("********************");

		if (configurator.arenaconfig.isSet("arenas")) {
			Set<String> arenas = configurator.arenaconfig.getConfigurationSection("arenas").getKeys(false);
			for (String arena : arenas) {
				games.put(arena,
						Game.loadGame(arena, configurator.arenaconfig.getConfigurationSection("arenas." + arena)));
			}
		}

	}
	
	public void onDisable() {
		for (Game game : games.values()) {
			game.stop();
		}
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}

		econ = rsp.getProvider();
		return econ != null;
	}
}
