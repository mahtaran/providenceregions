package me.mahtaran.ProvidenceRegions;

import java.lang.reflect.Constructor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TitleAPI {
    public static void sendTitle(final Player player, final Integer fadeIn, final Integer stay, final Integer fadeOut, String title,
	    String subtitle) {
	try {
	    Object TIMES = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TIMES").get((Object) null);
	    Object chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class })
		    .invoke((Object) null, new Object[] { "{\"text\":\"" + title + "\"}" });
	    if (title != null) {
		title = ChatColor.translateAlternateColorCodes('&', title);
		title = title.replaceAll("%player%", player.getDisplayName());
		// Times packets
		Constructor<?> titleConstructor = getNMSClass("PacketPlayOutTitle")
			.getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
				getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
		Object titlePacket = titleConstructor.newInstance(new Object[] { TIMES, chatTitle, fadeIn, stay, fadeOut });
		sendPacket(player, titlePacket);

		Object TITLE = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get((Object) null);
		chatTitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class })
			.invoke((Object) null, new Object[] { "{\"text\":\"" + title + "\"}" });
		titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(
			new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0], getNMSClass("IChatBaseComponent") });
		titlePacket = titleConstructor.newInstance(new Object[] { TITLE, chatTitle });
		sendPacket(player, titlePacket);
	    }

	    if (subtitle != null) {
		subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
		subtitle = subtitle.replaceAll("%player%", player.getDisplayName());
		// Times packets
		Object chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class })
			.invoke((Object) null, new Object[] { "{\"text\":\"" + title + "\"}" });
		Constructor<?> subtitleConstructor = getNMSClass("PacketPlayOutTitle")
			.getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
				getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
		Object subtitlePacket = subtitleConstructor.newInstance(new Object[] { TIMES, chatSubtitle, fadeIn, stay, fadeOut });
		sendPacket(player, subtitlePacket);

		Object SUBTITLE = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get((Object) null);
		chatSubtitle = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", new Class[] { String.class })
			.invoke((Object) null, new Object[] { "{\"text\":\"" + subtitle + "\"}" });
		subtitleConstructor = getNMSClass("PacketPlayOutTitle")
			.getConstructor(new Class[] { getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
				getNMSClass("IChatBaseComponent"), Integer.TYPE, Integer.TYPE, Integer.TYPE });
		subtitlePacket = subtitleConstructor.newInstance(new Object[] { SUBTITLE, chatSubtitle, fadeIn, stay, fadeOut });
		sendPacket(player, subtitlePacket);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private static final Class<?> getNMSClass(final String name) {
	try {
	    return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    private static final Object getPlayerHandle(final Player p) {
	try {
	    return p.getClass().getMethod("getHandle").invoke(p);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return null;
	}
    }

    private static final void sendPacket(final Player p, final Object packet) throws Exception {
	Object connection = getPlayerHandle(p).getClass().getField("playerConnection").get(getPlayerHandle(p));
	connection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(connection, packet);
    }
}
