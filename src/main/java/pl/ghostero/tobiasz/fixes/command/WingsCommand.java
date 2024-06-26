package pl.ghostero.tobiasz.fixes.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.ghostero.tobiasz.fixes.Main;
import pl.ghostero.tobiasz.fixes.objects.CosmeticUser;
import pl.ghostero.tobiasz.fixes.tempstorage.Wing;

public class WingsCommand implements CommandExecutor {

    private Main main;

    public WingsCommand(Main main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if(args.length == 0){
            sender.sendMessage("Komendy dotyczące testów skrzydełek:");
            sender.sendMessage("/wings add - Naklada skrzydełka");
            sender.sendMessage("/wings remove - Usuwa skrzydełka");
            sender.sendMessage("/wings value <num> - Ustawia testowe przesunięcie");
            return false;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("add")){
                CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(sender.getName());
                cosmeticUser.setWing(new Wing(1005, 1005));
                main.getWingsManager().prepareWingsForPlayer((Player) sender);
            } else if (args[0].equalsIgnoreCase("debug")) {
                //main.getWingsManager().createWings((Player) sender);

            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("add")) {
                final int idLeft = Integer.parseInt(args[1]);
                final int idRight = Integer.parseInt(args[2]);
                main.getWingsManager().destroyPlayerWings((Player) sender);
                CosmeticUser cosmeticUser = main.getCosmeticStorage().getCosmeticUsers().get(sender.getName());
                cosmeticUser.setWing(new Wing(idLeft, idRight));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        main.getWingsManager().prepareWingsForPlayer((Player) sender);
                    }
                }.runTaskLater(main, 1L);

            }
        }
        return false;
    }
}