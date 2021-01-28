import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandProcessor implements CommandExecutor, TabCompleter {
    public CommandProcessor(ResetAttributes instance){
        this.instance = instance;
    }

    private final ResetAttributes instance;

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) return false;

        if (!sender.hasPermission("resetattributes")){
            sender.sendMessage(ChatColor.RED + "You don't have permissions for this command");
            return false;
        }

        if (args[0].equalsIgnoreCase("status")){
            instance.getStatus(sender);
        }
        else if (args[0].equalsIgnoreCase("enable")){
            instance.enableReset(sender);
        }
        else if (args[0].equalsIgnoreCase("disable")){
            instance.disableReset(sender);
        }
        else if (args[0].equalsIgnoreCase("reset")){
            instance.doReset(sender);
        }
        else if (args[0].equalsIgnoreCase("reload")){
            instance.doReload();
            sender.sendMessage("Reload complete");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        if (args.length == 1) return Arrays.asList("status", "enable", "disable", "reset", "reload");

        return new ArrayList<>();
    }
}
