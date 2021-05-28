package addon.brainsynder.vault;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lib.brainsynder.item.ItemBuilder;
import lib.brainsynder.utils.Colorize;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import simplepets.brainsynder.addon.AddonConfig;
import simplepets.brainsynder.addon.AddonPermissions;
import simplepets.brainsynder.addon.PermissionData;
import simplepets.brainsynder.addon.PetAddon;
import simplepets.brainsynder.api.Namespace;
import simplepets.brainsynder.api.event.inventory.PetInventoryAddPetItemEvent;
import simplepets.brainsynder.api.event.inventory.PetSelectTypeEvent;
import simplepets.brainsynder.api.pet.PetType;
import simplepets.brainsynder.api.user.PetUser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Namespace(namespace = "Vault")
public class VaultAddon extends PetAddon {
    private Economy econ = null;
    private Map<PetType, Double> priceMap;

    private String prefix, bypassPerm, freePrice, bypassPrice, insufficientFunds, successfulPayment, paid;
    private List<String> lore;
    private boolean hidePrice, payPerUse;

    public VaultAddon() {
        priceMap = Maps.newHashMap();
    }

    @Override
    public void loadDefaults(AddonConfig config) {
        config.addDefault("bypass_permission", "pet.vault.bypass",
                "This is the bypass permission, who ever has this permission will not have to pay");
        config.addDefault("prefix", "&eSimplePets &6>>");
        config.addDefault("PurchaseSuccessful",
                "{prefix} &7You have Successfully Purchased the {type} Pet.",
                "This message will be sent if the purchase it successful\n" +
                        "Placeholders:\n" +
                        "- {prefix} (uses the customized prefix)\n" +
                        "- {type} (will get what type of pet it is)\n" +
                        "- {price} (what price the pet is)");
        config.addDefault("InsufficientFunds",
                "{prefix} &cYou do not have enough money to buy this pet, you need to have {price}",
                "This message will be sent if the player does not have enough money to buy the pet\n" +
                        "Placeholders:\n" +
                        "- {prefix} (uses the customized prefix)\n" +
                        "- {type} (will get what type of pet it is)\n" +
                        "- {price} (what price the pet is)");
        config.addDefault("Price-Free", "Free",
                "If a pet is free this will be in the place of the price in the lore\n" +
                        "Default: 'Free'");
        config.addDefault("One-Time-Pay-Lore-Lines", Lists.newArrayList("&6Price: &e{price}", "&6Purchased: &e{purchased}"),
                "These lines get added to the pet items when the GUI is opened\n" +
                        "These Lore Lines will only be used if 'Pay-Per-Use' is set to false\n" +
                        "Placeholders:\n" +
                        "- {price} (price of the pet)\n" +
                        "- {purchased} (true/false if the player purchased the pet)");
        config.addDefault("Bypass.Price", "BYPASSED",
                "If the player has the bypass permission, will be in the place of the price in the lore\n" +
                        "Default: 'BYPASSED'");
        config.addDefault("Bypass.Hide-Price-If-Bypassed", true,
                "Disabling this will make the items show the price, but if the player has bypass permissions he wont have to pay\n" +
                        "Default: true");
        config.addDefault("Pay-Per-Use.Enabled", false,
                "Should players have to pay each time they spawn a pet?\nDefault: false");
        config.addDefault("Pay-Per-Use.Paid",
                "{prefix} &7You have Successfully Paid for the {type} Pet.",
                "This message will be sent if the purchase it successful\n" +
                        "Placeholders:\n" +
                        "- {prefix} (uses the customized prefix)\n" +
                        "- {type} (will get what type of pet it is)\n" +
                        "- {price} (what price the pet is)");
        config.addDefault("Pay-Per-Use.Lore-Lines", Collections.singletonList("&6Price: &e{price}"),
                "These lines get added to the pet items when the GUI is opened\n" +
                        "Placeholders:\n" +
                        "- {price} (price of the pet)\n" +
                        "- {purchased} (true/false if the player purchased the pet)");

        for (PetType type : PetType.values()) {
            config.addDefault("type." + type.getName(), 2000, "The price of the " + type.getName() + " pet");
        }

        for (PetType type : PetType.values()) priceMap.put(type, config.getDouble("type." + type.getName(), 2000));


        hidePrice = config.getBoolean("Bypass.Hide-Price-If-Bypassed", true);
        payPerUse = config.getBoolean("Pay-Per-Use.Enabled", false);

        prefix = config.getString("prefix", "&eSimplePets &6>>");
        bypassPerm = config.getString("bypass_permission", "pet.vault.bypass");
        bypassPrice = String.valueOf(config.get("Bypass.Price", "BYPASSED"));
        freePrice = config.getString("Price-Free", "Free");
        insufficientFunds = config.getString("InsufficientFunds", "{prefix} &cYou do not have enough money to buy this pet, you need to have {price}");
        paid = config.getString("Pay-Per-Use.Paid", "{prefix} &7You have Successfully Paid for the {type} Pet.");
        successfulPayment = config.getString("PurchaseSuccessful", "{prefix} &7You have Successfully Purchased the {type} Pet.");

        lore = config.getStringList((config.getBoolean("Pay-Per-Use.Enabled") ? "Pay-Per-Use.Lore-Lines" : "One-Time-Pay-Lore-Lines"));
    }

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
        AddonPermissions.register(this, new PermissionData(bypassPerm).setDescription("Players that have this permission can bypass paying for pets"));

        RegisteredServiceProvider economyProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) this.econ = (Economy) economyProvider.getProvider();
    }

    @Override
    public void cleanup() {
        if (priceMap != null) priceMap.clear();

        bypassPerm = null;
        priceMap = null;
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

    @EventHandler
    public void onInventoryOpen(PetInventoryAddPetItemEvent event) {
        if (!isEnabled()) return;
        PetUser user = event.getUser();
        List<PetType> petArray = user.getOwnedPets();

        PetType type = event.getType();
        ItemBuilder maker = ItemBuilder.fromItem(event.getItem());
        String price = String.valueOf(priceMap.getOrDefault(type, 2000.0));
        if (price.equals("-1")) price = freePrice;

        if (hidePrice && ((Player) event.getUser().getPlayer()).hasPermission(bypassPerm)) price = bypassPrice;
        boolean contains = petArray.contains(type);
        for (String line : lore)
            maker.addLore(line.replace("{price}", price).replace("{purchased}", String.valueOf(contains)));
        System.out.println("Setting item...");

        event.setItem(maker.build());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSelect(PetSelectTypeEvent event) {
        if (!isEnabled()) return;
        if (((Player) event.getUser().getPlayer()).hasPermission(bypassPerm)) return;

        double price = priceMap.getOrDefault(event.getPetType(), 2000.0);
        if (price == -1) return; // The pet is free, return

        PetUser user = event.getUser();
        // If player already owns the pet ignore
        if (user.getOwnedPets().contains(event.getPetType())) return;

        double bal = econ.getBalance(user.getPlayer());

        // Checks the players balance (if they have a balance that is lower then the price)
        if (bal < price) {
            event.setCancelled(true);
            ((Player) user.getPlayer()).sendMessage(Colorize.translateBungeeHex(insufficientFunds
                    .replace("{price}", String.valueOf(price))
                    .replace("{type}", event.getPetType().getName())
                    .replace("{prefix}", prefix)
            ));
            return;
        }

        // Checks if PayPerUse is enabled, if it is dont add the pet to the players purchased list
        if (payPerUse) {
            econ.withdrawPlayer(user.getPlayer(), price);
            ((Player) user.getPlayer()).sendMessage(Colorize.translateBungeeHex(paid
                    .replace("{price}", String.valueOf(price))
                    .replace("{type}", event.getPetType().getName())
                    .replace("{prefix}", prefix)
            ));
            return;
        }

        // withdraw money, and add pet to the players purchased list
        user.addOwnedPet(event.getPetType());
        econ.withdrawPlayer(user.getPlayer(), price);
        ((Player) user.getPlayer()).sendMessage(Colorize.translateBungeeHex(successfulPayment
                .replace("{price}", String.valueOf(price))
                .replace("{type}", event.getPetType().getName())
                .replace("{prefix}", prefix)
        ));
    }
}
