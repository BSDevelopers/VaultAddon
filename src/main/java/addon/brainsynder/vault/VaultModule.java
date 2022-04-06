package addon.brainsynder.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import simplepets.brainsynder.addon.presets.EconomyModule;
import simplepets.brainsynder.api.Namespace;
import simplepets.brainsynder.api.plugin.SimplePets;

import java.util.UUID;

@Namespace(namespace = "Vault")
public class VaultModule extends EconomyModule {
    private Economy econ = null;

    @Override
    public boolean shouldEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if ((plugin != null) && plugin.isEnabled()) return true;
        SimplePets.getDebugLogger().debug(SimplePets.ADDON, "You seem to be missing the Vault plugin...");
        SimplePets.getDebugLogger().debug(SimplePets.ADDON, "Download it here: https://www.spigotmc.org/resources/34315/");
        return false;
    }

    @Override
    public void init() {
        super.init();

        RegisteredServiceProvider economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) this.econ = (Economy) economyProvider.getProvider();
    }

    @Override
    public int getDefaultPrice() {
        return 2000;
    }

    @Override
    public double getBalance(UUID uuid) {
        return econ.getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    @Override
    public void withdraw(UUID uuid, double amount) {
        econ.withdrawPlayer(Bukkit.getOfflinePlayer(uuid), amount);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        econ = null;
    }
}
