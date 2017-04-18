package me.mahtaran.ProvidenceRegions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import lombok.Getter;

public class ProvidenceRegions extends JavaPlugin implements CommandExecutor, Listener, TabCompleter {
    public class EnterTitle extends Title {
	public EnterTitle(final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
	    super(title, subtitle, fadein, stay, fadeout);
	}
    }

    public class LeaveTitle extends Title {
	public LeaveTitle(final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
	    super(title, subtitle, fadein, stay, fadeout);
	}
    }

    public class Title {
	public String title, subtitle;
	int fadein, stay, fadeout;

	public Title(final String title, final String subtitle, final int fadein, final int stay, final int fadeout) {
	    this.title = title;
	    this.subtitle = subtitle;
	    this.fadein = fadein;
	    this.stay = stay;
	    this.fadeout = fadeout;
	}
    }

    public class TitleSet {
	public Title entertitle, leavetitle;

	public TitleSet(final EnterTitle entertitle, final LeaveTitle leavetitle) {
	    this.entertitle = entertitle;
	    this.leavetitle = leavetitle;
	}
    }

    @Getter
    private static final Map<String, TitleSet> titles = new HashMap<String, TitleSet>();
    @Getter
    private static ProvidenceRegions instance;

    private static final Map<Player, List<ProtectedRegion>> playerRegions = new HashMap<Player, List<ProtectedRegion>>();

    private WorldGuardPlugin wgPlugin;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
	if (args.length > 0) switch (args[0]) {
	    case "create":
		if (args.length == 2)
		    if (titles.put(args[1], new TitleSet(new EnterTitle("", "", 0, 0, 0), new LeaveTitle("", "", 0, 0, 0))) == null)
			sender.sendMessage("Succesfully added the title for " + args[1]);
		    else sender.sendMessage("Succesfully overridden the title for " + args[1]);
		else sender.sendMessage(ChatColor.DARK_AQUA + "/rpg create <region name>: create a title");
		break;
	    case "add":
		if (args.length > 9 && titles.containsKey(args[2])) switch (args[1]) {
		    case "enter":
			String title = "", subtitle = "";
			boolean subflag = false, flag = false;
			for (String arg : args)
			    if (arg.equalsIgnoreCase("-subtitle")) {
				subflag = true;
				flag = false;
			    } else if (arg.equalsIgnoreCase("-title")) {
				subflag = false;
				flag = true;
			    } else if (flag) title += arg + " ";
			    else if (subflag) subtitle += arg + " ";
			title = title.substring(0, title.length() - 1);
			subtitle = subtitle.substring(0, subtitle.length() - 1);
			int fadein = 0, stay = 0, fadeout = 0;
			try {
			    fadein = Integer.valueOf(args[3]);
			    stay = Integer.valueOf(args[4]);
			    fadeout = Integer.valueOf(args[5]);
			} catch (Exception ex) {
			    sender.sendMessage(ChatColor.DARK_RED
				    + "Error! Invalid numbers for fadein, stay and/or fadeout! Make sure you use numbers withou decimals!");
			    break;
			}
			titles.get(args[2]).entertitle.title = title;
			titles.get(args[2]).entertitle.subtitle = subtitle;
			titles.get(args[2]).entertitle.fadein = fadein;
			titles.get(args[2]).entertitle.stay = stay;
			titles.get(args[2]).entertitle.fadeout = fadeout;
			sender.sendMessage("Succesfully set the enter title for the region " + args[2]);
			break;
		    case "leave":
			title = subtitle = "";
			subflag = flag = false;
			for (String arg : args)
			    if (arg.equalsIgnoreCase("-subtitle")) {
				subflag = true;
				flag = false;
			    } else if (arg.equalsIgnoreCase("-title")) {
				subflag = false;
				flag = true;
			    } else if (flag) title += arg + " ";
			    else if (subflag) subtitle += arg + " ";
			title = title.substring(0, title.length() - 1);
			subtitle = subtitle.substring(0, title.length() - 1);
			fadein = stay = fadeout = 0;
			try {
			    fadein = Integer.valueOf(args[3]);
			    stay = Integer.valueOf(args[4]);
			    fadeout = Integer.valueOf(args[5]);
			} catch (Exception ex) {
			    sender.sendMessage(ChatColor.DARK_RED
				    + "Error! Invalid numbers for fadein, stay and/or fadeout! Make sure you use numbers withou decimals!");
			    break;
			}
			titles.get(args[2]).leavetitle.title = title;
			titles.get(args[2]).leavetitle.subtitle = subtitle;
			titles.get(args[2]).leavetitle.fadein = fadein;
			titles.get(args[2]).leavetitle.stay = stay;
			titles.get(args[2]).leavetitle.fadeout = fadeout;
			sender.sendMessage("Succesfully set the leave title for the region " + args[2]);
			break;
		    default:
			sender.sendMessage(ChatColor.DARK_AQUA
				+ "/prg add <enter|leave> <name of region> <fadein> <stay> <fadeout> -title <title> -subtitle <subtitle>");

		}
		else sender.sendMessage(ChatColor.DARK_AQUA
			+ "/prg add <enter|leave> <name of region> <fadein> <stay> <fadeout> -title <title> -subtitle <subtitle>: add a title. Make sure you have created the title with /rpg create.");
		break;
	    case "delete":
		if (args.length == 2) if (titles.remove(args[1]) != null) sender.sendMessage("Succesfully deleted the title for " + args[1]);
		else sender.sendMessage("No known title for " + args[1]);
		else sender.sendMessage(ChatColor.DARK_AQUA + "/prg delete <name of region>");
		break;
	    case "reload":
		reloadConfig();
		sender.sendMessage("Succesfully reloaded the config!");
		break;
	    default:
		sendHelp(sender);
		break;
	}
	else sendHelp(sender);
	updateConfig();
	return true;
    }

    @Override
    public void onDisable() {
	updateConfig();
    }

    @Override
    public void onEnable() {
	getServer().getPluginManager().registerEvents(this, this);
	getCommand("prg").setExecutor(this);
	getConfig().addDefault("Messages.test.Enter.Title", "Welcome");
	getConfig().addDefault("Messages.test.Enter.Subtitle", "&6%player%");
	getConfig().addDefault("Messages.test.Enter.Fadein", 3);
	getConfig().addDefault("Messages.test.Enter.Stay", 10);
	getConfig().addDefault("Messages.test.Enter.Fadeout", 3);
	getConfig().addDefault("Messages.test.Leave.Title", "Goodbye");
	getConfig().addDefault("Messages.test.Leave.Subtitle", "&6%player%");
	getConfig().addDefault("Messages.test.Leave.Fadein", 3);
	getConfig().addDefault("Messages.test.Leave.Stay", 10);
	getConfig().addDefault("Messages.test.Leave.Fadeout", 3);
	getConfig().options().copyDefaults(true);
	saveConfig();
	ConfigurationSection messages = getConfig().getConfigurationSection("Messages");
	for (String string : messages.getKeys(false)) {
	    ConfigurationSection message = messages.getConfigurationSection(string);
	    titles.put(string,
		    new TitleSet(
			    new EnterTitle(message.getString("Enter.Title"), message.getString("Enter.Subtitle"),
				    message.getInt("Enter.Fadein"), message.getInt("Enter.Stay"), message.getInt("Enter.Fadeout")),
			    new LeaveTitle(message.getString("Leave.Title"), message.getString("Leave.Subtitle"),
				    message.getInt("Leave.Fadein"), message.getInt("Leave.Stay"), message.getInt("Leave.Fadeout"))));
	}

    }

    @Override
    public void onLoad() {
	instance = this;
	try {
	    wgPlugin = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
	} catch (Exception e) {
	    getLogger().info(ChatColor.DARK_RED + "WorldGuard was not found!!! Disabling ProvidenceRegions...");
	    getPluginLoader().disablePlugin(this);
	}
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent e) {
	updateRegions(e.getPlayer(), e.getPlayer().getLocation());
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent e) {
	updateRegions(e.getPlayer(), e.getTo());
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
	updateRegions(e.getPlayer(), e.getRespawnLocation());
    }

    @EventHandler
    public void onPlayerTeleport(final PlayerTeleportEvent e) {
	updateRegions(e.getPlayer(), e.getTo());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
	String[] subs = new String[] { "create", "add", "delete" };
	List<String> tabs = new ArrayList<String>();
	for (String sub : subs)
	    if (args.length > 0 ? sub.startsWith(args[0]) : true) tabs.add(sub);
	return tabs;
    }

    private final void sendHelp(final CommandSender sender) {
	sender.sendMessage(ChatColor.DARK_AQUA + "/prg create <region name>");
	sender.sendMessage(ChatColor.DARK_AQUA
		+ "/prg add <enter|leave> <name of region> <fadein> <stay> <fadeout> -title <title> -subtitle <subtitle>");
	sender.sendMessage(ChatColor.DARK_AQUA + "/prg delete <name of region>");
	sender.sendMessage(ChatColor.DARK_AQUA + "/prg reload");
    }

    private final void updateConfig() {
	for (Entry<String, TitleSet> entry : titles.entrySet()) {
	    getConfig().set("Messages." + entry.getKey() + ".Enter.Title", entry.getValue().entertitle.title);
	    getConfig().set("Messages." + entry.getKey() + ".Enter.Subtitle", entry.getValue().entertitle.subtitle);
	    getConfig().set("Messages." + entry.getKey() + ".Enter.Fadein", entry.getValue().entertitle.fadein);
	    getConfig().set("Messages." + entry.getKey() + ".Enter.Stay", entry.getValue().entertitle.stay);
	    getConfig().set("Messages." + entry.getKey() + ".Enter.Fadeout", entry.getValue().entertitle.fadeout);
	    getConfig().set("Messages." + entry.getKey() + ".Leave.Title", entry.getValue().leavetitle.title);
	    getConfig().set("Messages." + entry.getKey() + ".Leave.Subtitle", entry.getValue().leavetitle.subtitle);
	    getConfig().set("Messages." + entry.getKey() + ".Leave.Fadein", entry.getValue().leavetitle.fadein);
	    getConfig().set("Messages." + entry.getKey() + ".Leave.Stay", entry.getValue().leavetitle.stay);
	    getConfig().set("Messages." + entry.getKey() + ".Leave.Fadeout", entry.getValue().leavetitle.fadeout);
	}
	saveConfig();
    }

    private void updateRegions(final Player player, final Location to) {
	List<ProtectedRegion> regions;
	if (playerRegions.get(player) == null) regions = new ArrayList<ProtectedRegion>();
	else regions = new ArrayList<ProtectedRegion>(playerRegions.get(player));
	RegionManager rm = wgPlugin.getRegionManager(to.getWorld());
	if (rm == null) return;
	List<ProtectedRegion> appRegions = new ArrayList<ProtectedRegion>(rm.getApplicableRegions(to).getRegions());
	ProtectedRegion globalRegion = rm.getRegion("__global__");
	if (globalRegion != null) appRegions.add(globalRegion);
	for (final ProtectedRegion region : appRegions)
	    if (!regions.contains(region)) {
		// Entered
		regions.add(region);
		playerRegions.put(player, regions);
		TitleSet set = titles.get(region.getId());
		if (set != null) TitleAPI.sendTitle(player, set.entertitle.fadein, set.entertitle.stay, set.entertitle.fadeout,
			set.entertitle.title, set.entertitle.subtitle);
		return;
	    }
	Iterator<ProtectedRegion> itr = regions.iterator();
	while (itr.hasNext()) {
	    final ProtectedRegion region = itr.next();
	    if (!appRegions.contains(region)) {
		if (rm.getRegion(region.getId()) != region) {
		    itr.remove();
		    continue;
		}
		// Left
		itr.remove();
		playerRegions.put(player, regions);
		TitleSet set = titles.get(region.getId());
		if (set != null) TitleAPI.sendTitle(player, set.leavetitle.fadein, set.leavetitle.stay, set.leavetitle.fadeout,
			set.leavetitle.title, set.leavetitle.subtitle);
		return;
	    }
	}
	playerRegions.put(player, regions);
    }
}
