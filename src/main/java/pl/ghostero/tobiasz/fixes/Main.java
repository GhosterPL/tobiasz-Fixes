package pl.ghostero.tobiasz.fixes;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.ghostero.tobiasz.fixes.command.WingsCommand;
import pl.ghostero.tobiasz.fixes.listener.FixesListener;
import pl.ghostero.tobiasz.fixes.listener.WingsListener;
import pl.ghostero.tobiasz.fixes.objects.CosmeticStorage;
import pl.ghostero.tobiasz.fixes.wings.WingsManager;
import pl.ghostero.tobiasz.sectors.command.WorkBenchCommand;

@Getter
public class Main extends JavaPlugin {


    private CosmeticStorage cosmeticStorage;
    private WingsManager wingsManager;

    @Override
    public void onEnable(){
        getServer().getPluginManager().registerEvents(new FixesListener(), this);
        getServer().getPluginManager().registerEvents(new WingsListener(this), this);
        cosmeticStorage = new CosmeticStorage();
        wingsManager = new WingsManager(this);
        getCommand("wings").setExecutor(new WingsCommand(this));
    }
}
