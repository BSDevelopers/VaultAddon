package addon.brainsynder.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import simplepets.brainsynder.addon.presets.EconomyModule;
import simplepets.brainsynder.api.Namespace;

import java.util.UUID;

@Namespace(namespace = "Vault")
public class VaultModule extends EconomyModule {
    private Economy econ = null;

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
