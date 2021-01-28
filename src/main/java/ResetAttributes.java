import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;

public class ResetAttributes extends JavaPlugin {

    final private Logger logger = Logger.getLogger("ResetAttributes");
    private List<UUID> completedUsers;
    boolean isAutoResetEnabled;
    private List<Attribute> enabledAttributes;

    @Override
    public void onEnable(){
        final PluginCommand commandExecutor = getCommand("resetattributes");
        if (commandExecutor == null)
            logger.warning("command executor was null");
        else
            commandExecutor.setExecutor(new CommandProcessor(this));
        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        loadStuff();
    }

    @Override
    public void onDisable(){

    }

    private void loadStuff(){
        saveDefaultConfig();
        final FileConfiguration config = getConfig();
        loadUserList();

        int enabledAttribCount = 0;
        enabledAttributes = new ArrayList<>();
        for (Attribute attrib : Arrays.asList(
                Attribute.GENERIC_ARMOR,
                Attribute.GENERIC_ARMOR_TOUGHNESS,
                Attribute.GENERIC_ATTACK_DAMAGE,
                Attribute.GENERIC_ATTACK_SPEED,
                Attribute.GENERIC_KNOCKBACK_RESISTANCE,
                Attribute.GENERIC_LUCK,
                Attribute.GENERIC_MOVEMENT_SPEED
        )){
            String checkName = "attribute-" + attrib.name().toLowerCase().replace("_", "-").substring(8);

            if (config.getBoolean(checkName)){
                enabledAttributes.add(attrib);
                enabledAttribCount++;
            }
        }

        this.isAutoResetEnabled = config.getBoolean("reset-is-enabled");
        if (this.isAutoResetEnabled) resetAllOnline();

        logger.info(String.format("auto reset enabled: %s, attributes enabled: %s", isAutoResetEnabled, enabledAttribCount));
    }


    private void loadUserList(){
        this.completedUsers = new ArrayList<>();

        File file = new File(getDataFolder(), "processedusers.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))){

            String userId;
            while ((userId = br.readLine()) != null){
                if (userId.length() > 10)
                    completedUsers.add(UUID.fromString(userId));
            }
        }
        catch (Exception e){
            logger.warning("Error reading processedusers.txt: " + e.getMessage());
        }
    }

    void getStatus(CommandSender sender){
        if (isAutoResetEnabled){
            int remainingCount = getOfflineRemainingCount();
            sender.sendMessage(String.format("%s player(s) has been updated, %s offline player(s) remaining", completedUsers.size(), remainingCount));
        }
        else {
            sender.sendMessage("Auto reset is currently disabled");
        }
    }

    void enableReset(CommandSender sender){
        int onlineCount = resetAllOnline();
        int remainingCount = getOfflineRemainingCount();

        sender.sendMessage("Auto reset has been enabled");
        sender.sendMessage(String.format("%s online player(s) updated, %s offline player(s) remaining", onlineCount, remainingCount));

        isAutoResetEnabled = true;
    }

    void doReset(CommandSender sender){
        completedUsers.clear();
        writeCompletedUsersListToDisk();
        sender.sendMessage("Removed all users from the processed list");
    }

    void doReload(){
        loadStuff();
    }

    void disableReset(CommandSender sender){
        int remainingCount = getOfflineRemainingCount();

        isAutoResetEnabled = false;
        if (remainingCount > 0)
            sender.sendMessage(String.format("%s offline player(s) were not updated", remainingCount));
        else {
            sender.sendMessage("All players were updated");
        }
    }

    void resetPlayer(Player player, boolean writeToDisk){
        for (Attribute enabledAttribute : this.enabledAttributes){
            AttributeInstance attribInstance = player.getAttribute(enabledAttribute);
            if (attribInstance == null) continue;

            switch (enabledAttribute){
                case GENERIC_ARMOR:
                case GENERIC_ARMOR_TOUGHNESS:
                case GENERIC_KNOCKBACK_RESISTANCE:
                case GENERIC_LUCK:
                    attribInstance.setBaseValue(0.0);
                    break;
                case GENERIC_ATTACK_DAMAGE:
                    attribInstance.setBaseValue(8.0);
                    break;
                case GENERIC_ATTACK_SPEED:
                    attribInstance.setBaseValue(1.5999999046325684);
                    break;
                case GENERIC_MAX_HEALTH:
                    attribInstance.setBaseValue(20.0);
                    break;
                case GENERIC_MOVEMENT_SPEED:
                    attribInstance.setBaseValue(0.10000000149011612);
                    break;
            }
        } // next attribute

        logger.info("Reset attributes for player " + player.getName());
        updateCompletedUserList(player.getUniqueId(), writeToDisk);
    }

    private int getOfflineRemainingCount(){
        int pendingCount = 0;
        for (OfflinePlayer op : Bukkit.getOfflinePlayers()){
            if (!completedUsers.contains(op.getUniqueId())) pendingCount++;
        }

        return pendingCount;
    }

    private void updateCompletedUserList(UUID id, boolean writeToDisk){
        if (!completedUsers.contains(id))
            completedUsers.add(id);

        if (writeToDisk) writeCompletedUsersListToDisk();
    }

    private void writeCompletedUsersListToDisk(){
        File file = new File(getDataFolder(), "processedusers.txt");
        StringBuilder sb = new StringBuilder();
        for (UUID id : completedUsers){
            if (sb.length() > 0) sb.append("\n");
            sb.append(id.toString());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.write(sb.toString());
        } catch (Exception e) {
            logger.warning("Unable to write processedusers.txt: " + e.getMessage());
        }
    }

    private int resetAllOnline(){
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()){
            count++;
            resetPlayer(player, false);
        }

        if (count > 0) writeCompletedUsersListToDisk();

        return count;
    }
}
