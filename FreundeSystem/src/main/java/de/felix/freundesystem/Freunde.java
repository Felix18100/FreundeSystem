package de.felix.freundesystem;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.plugin.Plugin;

public final class Freunde extends JavaPlugin implements Listener {
    private HashMap<UUID, List<String>> freundeData = new HashMap<>();
    private HashMap<UUID, List<String>> freundeAnfrage = new HashMap<>();
    public static Freunde instance;

    public static Freunde getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, (Plugin) this);
        ladeFreundeData();
    }

    @Override
    public void onDisable() {
        speicherFreundeData();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
        sender.sendMessage("Dieser Befehl kann nur der Spieler ausführen!");
        return false;
        }
        Player player = (Player)sender;
        if(label.equalsIgnoreCase("freunde")){
            if(args.length == 0){
                showUsages(player);
                return true;
            }
            switch (args[0]){
                case "add":
                    if(args.length != 2){
                        showUsages(player);
                        return true;
                    }
                    addFreunde(player, args[1]);
                    return true;
                case "remove":
                    if(args.length != 2){
                        showUsages(player);
                        return true;
                    }
                    removeFreunde(player, args[1]);
                    return true;
                case "accept":
                    if(args.length != 2){
                        showUsages(player);
                        return true;
                    }
                    acceptFreunde(player, args[1]);
                    return true;
                case "deny":
                    if(args.length != 2){
                        showUsages(player);
                        return true;
                    }
                    denyFreunde(player, args[1]);
                    return true;
                case "list":
                  listFreunde(player);
                   return true;
            }
            showUsages(player);
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        UUID playeruuid = player.getUniqueId();
        sendFreunde(player);
        sendOnlineFreunde(player);
        if(!this.freundeData.containsKey(playeruuid)){
            this.freundeData.put(playeruuid, new ArrayList<>());
        }
    }

    private void sendOnlineFreunde(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<String> playerfreundes = this.freundeData.getOrDefault(playerUUID, new ArrayList<>());
        player.sendMessage("§6Aktuelle Freunde");
        for(String freundeName : playerfreundes){
            Player freunde = Bukkit.getPlayerExact(freundeName);
            if(freunde != null && freunde.isOnline()){
                player.sendMessage("§6-" + freundeName);
            }
        }
    }

    private void sendFreunde(Player player) {
        List<String> reguests = this.freundeAnfrage.get(player.getUniqueId());
        if(reguests != null && !reguests.isEmpty()){
            player.sendMessage("§eOffene Freundeanfragen:");
            for(String reguest : reguests){
                player.sendMessage("§e-" + reguest);
            }
        }
    }

    private void listFreunde(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<String> spielerFreunde = this.freundeData.getOrDefault(playerUUID, new ArrayList<>());
        if (spielerFreunde.isEmpty()) {
            player.sendMessage("§cDu hast aktuell keine Freunde!");
        } else {
            player.sendMessage("§6Deine Freunde:");
            for (String freundeName : spielerFreunde)
                player.sendMessage("- " + freundeName);
        }

    }

    private void denyFreunde(Player player, String senderName) {
        Player sender = Bukkit.getPlayer(senderName);
        if (sender != null) {
            List<String> senderAnfrage = this.freundeData.getOrDefault(sender.getUniqueId(), new ArrayList<>());
            if (senderAnfrage.contains(player.getName())) {
                senderAnfrage.remove(player.getName());
                player.sendMessage("§cDu hast die Freundschaftsanfrage von " + senderName + " abgelehnt.");
                sender.sendMessage("§c" + player.getName() + " hat deine Freundschaftsanfrage abgelehnt.");
            } else {
                player.sendMessage("§cDu hast keine ausstehende Freundschaftsanfrage von " + senderName + ".");
            }
        } else {
            player.sendMessage("§cDer Spieler " + senderName + " ist nicht online.");
        }
    }

    private void acceptFreunde(Player player, String senderName) {
        Player sender = Bukkit.getPlayer(senderName);
        if (sender != null) {
            UUID playerUUID = player.getUniqueId();
            UUID senderUUID = sender.getUniqueId();
            List<String> spielerFreunde = this.freundeData.getOrDefault(playerUUID, new ArrayList<>());
            List<String> senderAnfrage = this.freundeAnfrage.getOrDefault(senderUUID, new ArrayList<>());
            if (senderAnfrage.contains(player.getName())) {
                if (!spielerFreunde.contains(senderName)) {
                    spielerFreunde.add(senderName);
                    senderAnfrage.remove(player.getName());
                    this.freundeData.put(playerUUID, spielerFreunde);
                    this.freundeAnfrage.put(senderUUID, senderAnfrage);
                    player.sendMessage("§aDu hast die Freundschaftsanfrage von " + senderName + " angenommen.");
                    sender.sendMessage("§a"+ player.getName() + " hat deine Freundschaftsanfrage angenommen.");
                } else {
                    player.sendMessage("§aDu bist bereits mit " + senderName + " befreundet.");
                }
            } else {
                player.sendMessage("§cDu hast keine ausstehende Freundschaftsanfrage von " + senderName + ".");
            }
        } else {
            player.sendMessage( "§cDer Spieler " + senderName + " ist nicht online.");
        }
    }

    private void removeFreunde(Player player, String freundeName) {
        Player freundeSpieler = Bukkit.getPlayer(freundeName);
        if (freundeSpieler != null) {
            UUID friendUUID = freundeSpieler.getUniqueId();
            List<String> spielerFreunde = this.freundeData.get(player.getUniqueId());
            if (spielerFreunde.contains(freundeName)) {
                spielerFreunde.remove(freundeName);
                player.sendMessage("§aDu hast " + freundeName + " erfolgreich aus deiner Freundesliste entfernt.");
            } else {
                player.sendMessage("§cDu bist nicht mit" + freundeName + " befreundet.");
            }
        } else {
            player.sendMessage("§cDer Spieler " + freundeName + " ist nicht online.");
        }
    }

    private void addFreunde(Player player, String freundeName) {
        Player freundeSpieler = Bukkit.getPlayer(freundeName);
        if (freundeSpieler != null) {
            UUID friendUUID = freundeSpieler.getUniqueId();
            List<String> spielerFreunde = this.freundeData.get(player.getUniqueId());
            List<String> freundeAnfrage = this.freundeData.getOrDefault(friendUUID, new ArrayList<>());
            if (!spielerFreunde.contains(freundeName)) {
                if (!freundeAnfrage.contains(player.getName())) {
                    freundeAnfrage.add(player.getName());
                    this.freundeData.put(friendUUID, freundeAnfrage);
                    player.sendMessage("§a" + "Du hast " + freundeName + " eine Freundschaftsanfrage gesendet.");
                    freundeSpieler.sendMessage("§a" + player.getName() + " hat dir eine Freundschaftsanfrage gesendet. Verwende '/friend accept " + player.getName() + "' um anzunehmen.");
                } else {
                    player.sendMessage("§cDu hast bereits eine ausstehende Freundschaftsanfrage an " + freundeName + ".");
                }
            } else {
                player.sendMessage("§cDu bist bereits mit " + freundeName + " befreundet.");
            }
        } else {
            player.sendMessage( "§cDer Spieler " + freundeName + " ist nicht online.");
        }
    }

    private void showUsages(Player player) {
        player.sendMessage("§6§lVerwendung:");
        player.sendMessage("§6/freunde add <Spieler>");
        player.sendMessage("§6/freunde remove <Spieler>");
        player.sendMessage("§6/freunde accept <Spieler>");
        player.sendMessage("§6/freunde deny <Spieler>");
        player.sendMessage("§6/freunde list");
    }

    private void ladeFreundeData() {
        FileConfiguration config = getConfig();
        if (config.contains("freundeData")) {
            ConfigurationSection friendSection = config.getConfigurationSection("freundeData");
            for (String playerUUID : friendSection.getKeys(false)) {
                List<String> friends = friendSection.getStringList(playerUUID);
                this.freundeData.put(UUID.fromString(playerUUID), friends);
            }
        }
    }
    private void speicherFreundeData() {
        FileConfiguration config = getConfig();
        ConfigurationSection friendSection = config.createSection("friendData");
        for (UUID playerUUID : this.freundeData.keySet())
            friendSection.set(playerUUID.toString(), this.freundeData.get(playerUUID));
        saveConfig();
    }
}
