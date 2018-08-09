package misat11.za.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import misat11.za.Main;
import misat11.za.game.Game;
import misat11.za.game.GameCreator;
import misat11.za.game.GamePlayer;
import misat11.za.utils.I18n;

public class ZaCommand implements CommandExecutor, TabCompleter {

	public HashMap<String, GameCreator> gc = new HashMap<String, GameCreator>();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 0) {
				sendHelp(player);
			} else if (args.length > 0) {
				if (args[0].equalsIgnoreCase("help")) {
					sendHelp(player);
				} else if (args[0].equalsIgnoreCase("coins")) {
					GamePlayer gPlayer = Main.getPlayerGameProfile(player);
					sender.sendMessage(I18n._("have_coins").replace("%coins%", Integer.toString(gPlayer.coins)));
				} else if (args[0].equalsIgnoreCase("antiteleport")) {
					GamePlayer gPlayer = Main.getPlayerGameProfile(player);
					sender.sendMessage(I18n._("have_antiteleports").replace("%antiteleport%",
							Integer.toString(gPlayer.teleportAura)));
				} else if (args[0].equalsIgnoreCase("join")) {
					if (args.length > 1) {
						String arenaN = args[1];
						if (Main.isGameExists(arenaN)) {
							Main.getGame(arenaN).joinToGame(player);
						} else {
							player.sendMessage(I18n._("no_arena_found"));
						}
					} else {
						player.sendMessage(I18n._("usage_za_join"));
					}
				} else if (args[0].equalsIgnoreCase("leave")) {
					if (Main.isPlayerInGame(player)) {
						Main.getPlayerGameProfile(player).changeGame(null);
					} else {
						player.sendMessage(I18n._("you_arent_in_game"));
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					player.sendMessage(I18n._("list_header"));
					Main.sendGameListInfo(player);
				} else if (args[0].equalsIgnoreCase("admin")) {
					if (player.hasPermission("misat11.za.admin")) {
						if (args.length >= 3) {
							String arN = args[1];
							if (args[2].equalsIgnoreCase("add")) {
								if (Main.isGameExists(arN)) {
									player.sendMessage(I18n._("allready_exists"));
								} else if (gc.containsKey(arN)) {
									player.sendMessage(I18n._("allready_working_on_it"));
								} else {
									GameCreator creator = new GameCreator(Game.createGame(arN));
									gc.put(arN, creator);
									player.sendMessage(I18n._("arena_added"));
								}
							} else if (args[2].equalsIgnoreCase("remove")) {
								if (Main.isGameExists(arN)) {
									if (!gc.containsKey(arN)) {
										player.sendMessage(I18n._("arena_must_be_in_edit_mode"));
									} else {
										gc.remove(arN);
										new File(Main.getInstance().getDataFolder(), "arenas/" + arN + ".yml").delete();
										Main.removeGame(Main.getGame(arN));
										player.sendMessage(I18n._("arena_removed"));
									}
								} else if (gc.containsKey(arN)) {
									gc.remove(arN);
									player.sendMessage(I18n._("arena_removed"));
								} else {
									player.sendMessage(I18n._("no_arena_found"));
								}
							} else if (args[2].equalsIgnoreCase("edit")) {
								if (Main.isGameExists(arN)) {
									Game game = Main.getGame(arN);
									game.stop();
									gc.put(arN, new GameCreator(game));
									player.sendMessage(I18n._("arena_switched_to_edit"));
								} else {
									player.sendMessage(I18n._("no_arena_found"));
								}
							} else {
								if (gc.containsKey(arN)) {
									List<String> nargs = new ArrayList<String>();
									int lid = 0;
									for (String arg : args) {
										if (lid >= 3) {
											nargs.add(arg);
										}
										lid++;
									}
									gc.get(arN).cmd(player, args[2], nargs.toArray(new String[nargs.size()]));
									if (args[2].equalsIgnoreCase("save")) {
										gc.remove(arN);
									}
								} else {
									player.sendMessage(I18n._("arena_not_in_edit"));
								}
							}
						} else {
							player.sendMessage(I18n._("usage_za_admin"));
						}
					} else {
						player.sendMessage(I18n._("no_permissions"));
					}
				} else if (args[0].equalsIgnoreCase("skip")) {
					if (player.hasPermission("misat11.za.admin")) {
						if (Main.isPlayerInGame(player)) {
							if (args.length > 1) {
								Main.getPlayerGameProfile(player).getGame().skip(Integer.parseInt(args[1]));
							} else {
								Main.getPlayerGameProfile(player).getGame().skip();
							}
						} else {
							player.sendMessage(I18n._("you_arent_in_game"));
						}
					} else {
						player.sendMessage(I18n._("no_permissions"));
					}
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (player.hasPermission("misat11.za.admin")) {
						Main.getInstance().getPluginLoader().disablePlugin(Main.getInstance());
						Main.getInstance().getPluginLoader().enablePlugin(Main.getInstance());
						player.sendMessage("Plugin reloaded!");
					} else {
						player.sendMessage(I18n._("no_permissions"));
					}
				} else {
					player.sendMessage(I18n._("unknown_command"));
				}
			}
		} else {
			sender.sendMessage("Za commands cannot be executed from console!");
		}
		return true;
	}

	public void sendHelp(Player player) {
		player.sendMessage(I18n._("help_title", false));
		player.sendMessage(I18n._("help_za_join", false));
		player.sendMessage(I18n._("help_za_leave", false));
		player.sendMessage(I18n._("help_za_list", false));
		player.sendMessage(I18n._("help_za_coins", false));
		player.sendMessage(I18n._("help_za_antiteleport", false));
		if (player.hasPermission("misat11.za.admin")) {
			player.sendMessage(I18n._("help_za_admin_add", false));
			player.sendMessage(I18n._("help_za_admin_spawn", false));
			player.sendMessage(I18n._("help_za_admin_pos1", false));
			player.sendMessage(I18n._("help_za_admin_pos2", false));
			player.sendMessage(I18n._("help_za_admin_pausecountdown", false));
			player.sendMessage(I18n._("help_za_admin_phase_add", false));
			player.sendMessage(I18n._("help_za_admin_phase_remove", false));
			player.sendMessage(I18n._("help_za_admin_phase_insert", false));
			player.sendMessage(I18n._("help_za_admin_phase_set", false));
			player.sendMessage(I18n._("help_za_admin_monster_add", false));
			player.sendMessage(I18n._("help_za_admin_monster_remove", false));
			player.sendMessage(I18n._("help_za_admin_small_add", false));
			player.sendMessage(I18n._("help_za_admin_small_remove", false));
			player.sendMessage(I18n._("help_za_admin_small_pos1", false));
			player.sendMessage(I18n._("help_za_admin_small_pos2", false));
			player.sendMessage(I18n._("help_za_admin_small_monsteradd", false));
			player.sendMessage(I18n._("help_za_admin_small_monsterremove", false));
			player.sendMessage(I18n._("help_za_admin_store_add", false));
			player.sendMessage(I18n._("help_za_admin_store_remove", false));
			player.sendMessage(I18n._("help_za_admin_bossgame_set", false));
			player.sendMessage(I18n._("help_za_admin_bossgame_reset", false));
			player.sendMessage(I18n._("help_za_admin_save", false));
			player.sendMessage(I18n._("help_za_admin_remove", false));
			player.sendMessage(I18n._("help_za_admin_edit", false));
			player.sendMessage(I18n._("help_za_reload", false));
			player.sendMessage(I18n._("help_za_skip", false));
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> completionList = new ArrayList<>();
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length == 1) {
				List<String> cmds = Arrays.asList("join", "leave", "list", "coins", "antiteleport");
				if (player.hasPermission("misat11.za.admin")) {
					cmds = Arrays.asList("join", "leave", "list", "coins", "antiteleport", "admin", "skip", "reload");
				}
				StringUtil.copyPartialMatches(args[0], cmds, completionList);
			}
			if (args.length > 1) {
				if (args[0].equalsIgnoreCase("join")) {
					List<String> arenas = Main.getGameNames();
					StringUtil.copyPartialMatches(args[1], arenas, completionList);
				} else if (args[0].equalsIgnoreCase("admin") && player.hasPermission("misat11.za.admin")) {
					if (args.length == 2) {
						List<String> arenas = Main.getGameNames();
						for (String arena : gc.keySet()) {
							arenas.add(arena);
						}
						StringUtil.copyPartialMatches(args[1], arenas, completionList);
					} else if (args.length == 3) {
						List<String> cmds = Arrays.asList("add", "spawn", "pos1", "pos2", "pausecountdown", "phase",
								"monster", "small", "store", "bossgame", "save", "remove", "edit");
						StringUtil.copyPartialMatches(args[2], cmds, completionList);
					} else if (args[2].equalsIgnoreCase("pausecountdown") && args.length == 4) {
						StringUtil.copyPartialMatches(args[3], Arrays.asList("60"), completionList);
					} else if (args[2].equalsIgnoreCase("phase")) {
						if (args.length == 4) {
							List<String> cmds = Arrays.asList("add", "remove", "insert", "set");
							StringUtil.copyPartialMatches(args[3], cmds, completionList);
						}
						if (args.length > 4) {
							if (args[3].equalsIgnoreCase("add") && args.length == 5) {
								StringUtil.copyPartialMatches(args[4], Arrays.asList("60"), completionList);
							} else if (args[3].equalsIgnoreCase("remove") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("insert") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("insert") && args.length == 6) {
								StringUtil.copyPartialMatches(args[5], Arrays.asList("60"), completionList);
							} else if (args[3].equalsIgnoreCase("set") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("set") && args.length == 6) {
								StringUtil.copyPartialMatches(args[5], Arrays.asList("60"), completionList);
							}
						}
					} else if (args[2].equalsIgnoreCase("monster")) {
						if (args.length == 4) {
							List<String> cmds = Arrays.asList("add", "remove");
							StringUtil.copyPartialMatches(args[3], cmds, completionList);
						}
						if (args.length > 4) {
							if (args[3].equalsIgnoreCase("add") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("add") && args.length == 6) {
								List<EntityType> enumValues = Arrays.asList(EntityType.values());
								List<String> mobs = new ArrayList<String>();
								for (EntityType en : enumValues) {
									mobs.add(en.toString());
								}
								StringUtil.copyPartialMatches(args[5], mobs, completionList);
							} else if (args[3].equalsIgnoreCase("add") && args.length == 7) {
								StringUtil.copyPartialMatches(args[6], Arrays.asList("5", "10", "15", "20"),
										completionList);
							} else if (args[3].equalsIgnoreCase("remove") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("remove") && args.length == 6) {
								List<EntityType> enumValues = Arrays.asList(EntityType.values());
								List<String> mobs = new ArrayList<String>();
								for (EntityType en : enumValues) {
									mobs.add(en.toString());
								}
								StringUtil.copyPartialMatches(args[5], mobs, completionList);
							}
						}
					} else if (args[2].equalsIgnoreCase("small")) {
						if (args.length == 4) {
							List<String> cmds = Arrays.asList("add", "remove", "pos1", "pos2", "monsteradd",
									"monsterremove");
							StringUtil.copyPartialMatches(args[3], cmds, completionList);
						}
						if (args.length > 4) {
							if (args[3].equalsIgnoreCase("add") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("pos1") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("pos2") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("remove") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("monsteradd") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("monsterremove") && args.length == 5) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[4], creator.getSmallArenas(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("monsteradd") && args.length == 6) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[5], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("monsteradd") && args.length == 7) {
								List<EntityType> enumValues = Arrays.asList(EntityType.values());
								List<String> mobs = new ArrayList<String>();
								for (EntityType en : enumValues) {
									mobs.add(en.toString());
								}
								StringUtil.copyPartialMatches(args[6], mobs, completionList);
							} else if (args[3].equalsIgnoreCase("monsteradd") && args.length == 8) {
								StringUtil.copyPartialMatches(args[7], Arrays.asList("5", "10", "15", "20"),
										completionList);
							} else if (args[3].equalsIgnoreCase("monsterremove") && args.length == 6) {
								GameCreator creator = gc.get(args[1]);
								if (creator != null) {
									StringUtil.copyPartialMatches(args[5], creator.getPhaseIndexes(), completionList);
								}
							} else if (args[3].equalsIgnoreCase("monsterremove") && args.length == 7) {
								List<EntityType> enumValues = Arrays.asList(EntityType.values());
								List<String> mobs = new ArrayList<String>();
								for (EntityType en : enumValues) {
									mobs.add(en.toString());
								}
								StringUtil.copyPartialMatches(args[6], mobs, completionList);
							}
						}
					} else if (args[2].equalsIgnoreCase("store")) {
						if (args.length == 4) {
							List<String> cmds = Arrays.asList("add", "remove");
							StringUtil.copyPartialMatches(args[3], cmds, completionList);
						}
					} else if (args[2].equalsIgnoreCase("bossgame")) {
						if (args.length == 4) {
							List<String> cmds = Arrays.asList("set", "reset");
							StringUtil.copyPartialMatches(args[3], cmds, completionList);
						}
					}
				}
			}
		}
		return completionList;
	}

}
