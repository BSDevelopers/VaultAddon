package addon.brainsynder.vault;

import com.google.common.collect.Lists;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import simplepets.brainsynder.addon.presets.EconomyAddon;
import simplepets.brainsynder.api.Namespace;

import java.util.List;
import java.util.UUID;

@Namespace(namespace = "Vault")
public class VaultAddon extends EconomyAddon {
    private Economy econ = null;

    @Override
    public boolean shouldEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if ((plugin != null) && plugin.isEnabled()) return true;
        System.out.println("[SimplePets VaultAddon] You seem to be missing the Vault plugin...");
        System.out.println("[SimplePets VaultAddon] Download it here: https://www.spigotmc.org/resources/34315/");
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

    @Override
    public double getVersion() {
        return 0.1;
    }

    @Override
    public String getAuthor() {
        return "brainsynder";
    }

    @Override
    public List<String> getDescription() {
        return Lists.newArrayList(
                "&7This addon links into the Vault Plugin",
                "&7To make it possible to buy pets with in-game money"
        );
    }
}
