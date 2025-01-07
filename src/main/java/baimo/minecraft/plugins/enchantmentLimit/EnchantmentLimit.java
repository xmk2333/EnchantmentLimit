package baimo.minecraft.plugins.enchantmentLimit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnchantmentLimit extends JavaPlugin implements Listener {

    private static final NamespacedKey BYPASS_KEY = new NamespacedKey("enchantmentlimit", "bypass");

    private Map<Enchantment, Integer> enchantmentLimits = new HashMap<>();
    private String limitExceededMessage;
    private String configReloadedMessage;
    private String noPermissionMessage;
    private String bypassEnabledMessage;
    private String bypassDisabledMessage;
    private String pluginDetectedMessage;
    private boolean enableMessages;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        addDefaultEnchantmentsToConfig();
        loadEnchantmentLimits();
        loadLocalization();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("enchantmentlimit").setExecutor(this);
        getCommand("enchantmentlimit").setTabCompleter(this);
        checkExistingEnchantments();
        checkForPlugins();

        // 输出欢迎消息
        getLogger().info(ChatColor.GREEN + "欢迎加入白陌的插件社区群: " + ChatColor.YELLOW + "528651839");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void addDefaultEnchantmentsToConfig() {
        boolean configChanged = false;
        for (Enchantment enchantment : Enchantment.values()) {
            String key = enchantment.getKey().getNamespace() + ":" + enchantment.getKey().getKey();
            if (!getConfig().contains("enchantments." + key)) {
                getConfig().set("enchantments." + key, enchantment.getMaxLevel());
                configChanged = true;
            }
        }
        // 添加对 ecoEnchants、ExcellentEnchants 和 CrazyEnchantments 插件注册的附魔的支持
        if (Bukkit.getPluginManager().isPluginEnabled("ecoEnchants") ||
                Bukkit.getPluginManager().isPluginEnabled("ExcellentEnchants") ||
                Bukkit.getPluginManager().isPluginEnabled("CrazyEnchantments")) {
            for (Enchantment enchantment : Enchantment.values()) {
                String key = enchantment.getKey().getNamespace() + ":" + enchantment.getKey().getKey();
                if (!getConfig().contains("enchantments." + key)) {
                    getConfig().set("enchantments." + key, enchantment.getMaxLevel());
                    configChanged = true;
                }
            }
        }
        if (configChanged) {
            saveConfig();
        }
    }

    private void loadEnchantmentLimits() {
        if (getConfig().contains("enchantments")) {
            getConfig().getConfigurationSection("enchantments").getKeys(false).forEach(key -> {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(key));
                if (enchantment != null) {
                    int maxLevel = getConfig().getInt("enchantments." + key, enchantment.getMaxLevel());
                    enchantmentLimits.put(enchantment, maxLevel);
                }
            });
        }
    }

    private void loadLocalization() {
        limitExceededMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.limitExceeded", "&cYou have exceeded the enchantment limit!"));
        configReloadedMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.configReloaded", "&aEnchantmentLimit configuration reloaded."));
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission", "&cYou do not have permission to execute this command."));
        bypassEnabledMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.bypassEnabled", "&aBypass enabled for this item."));
        bypassDisabledMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.bypassDisabled", "&cBypass disabled for this item."));
        pluginDetectedMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.pluginDetected", "&aDetected plugin: &e{plugin}"));
        enableMessages = getConfig().getBoolean("settings.enableMessages", true);
    }

    private void checkExistingEnchantments() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            getServer().getOnlinePlayers().forEach(this::checkInventory);
        });
    }

    private void checkForPlugins() {
        if (Bukkit.getPluginManager().isPluginEnabled("ecoEnchants")) {
            getLogger().info(pluginDetectedMessage.replace("{plugin}", "ecoEnchants"));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ExcellentEnchants")) {
            getLogger().info(pluginDetectedMessage.replace("{plugin}", "ExcellentEnchants"));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("CrazyEnchantments")) {
            getLogger().info(pluginDetectedMessage.replace("{plugin}", "CrazyEnchantments"));
        }
    }

    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        if (event.getItem().getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        event.getEnchantsToAdd().replaceAll((enchantment, level) -> {
            int maxLevel = enchantmentLimits.getOrDefault(enchantment, 5);
            if (level > maxLevel) {
                if (enableMessages) {
                    player.sendMessage(limitExceededMessage);
                }
                return maxLevel;
            }
            return level;
        });
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(event.getItem()));
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        if (result != null) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(result));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
                return; // Skip items with the special NBT tag
            }
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(item));
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack item : event.getNewItems().values()) {
            if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
                return; // Skip items with the special NBT tag
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            event.getNewItems().values().forEach(this::checkItemEnchantments);
        });
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(event.getItem()));
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            checkInventory(event.getPlayer());
            if (isContainer(event.getInventory().getType())) {
                for (ItemStack item : event.getInventory().getContents()) {
                    if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
                        continue; // Skip items with the special NBT tag
                    }
                    checkItemEnchantments(item);
                }
            }
        });
    }

    private boolean isContainer(InventoryType type) {
        switch (type) {
            case CHEST:
            case SHULKER_BOX:
            case BARREL:
            case ENDER_CHEST:
            case HOPPER:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
                return true;
            default:
                return false;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkInventory(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        if (item != null) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(item));
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        if (item != null) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(item));
        }
    }

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (item != null && item.getItemMeta().getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
            return; // Skip items with the special NBT tag
        }
        if (item != null) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> checkItemEnchantments(item));
        }
    }

    private void checkInventory(HumanEntity entity) {
        for (ItemStack item : entity.getInventory().getContents()) {
            if (item != null) {
                checkItemEnchantments(item);
            }
        }
    }

    private void checkItemEnchantments(ItemStack item) {
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
                return; // 跳过带有特定 NBT 数据的物品
            }
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;
                checkStoredEnchantments(enchantmentMeta, item);
                item.setItemMeta(enchantmentMeta);
            } else {
                checkEnchantments(item.getEnchantments(), item);
            }
        }
    }

    private void checkStoredEnchantments(EnchantmentStorageMeta meta, ItemStack item) {
        boolean changed = false;
        for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            int maxLevel = enchantmentLimits.getOrDefault(enchantment, 5);
            if (level > maxLevel) {
                // 更改附魔
                meta.removeStoredEnchant(enchantment);
                meta.addStoredEnchant(enchantment, maxLevel, true);
                changed = true;
            }
        }
        if (changed) {
            // 发送消息
            Player player = getPlayerFromItem(item);
            if (player != null && enableMessages) {
                Bukkit.getScheduler().runTask(this, () -> player.sendMessage(limitExceededMessage));
            }
            item.setItemMeta(meta);
        }
    }

    private void checkEnchantments(Map<Enchantment, Integer> enchantments, ItemStack item) {
        boolean changed = false;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            int maxLevel = enchantmentLimits.getOrDefault(enchantment, 5);
            if (level > maxLevel) {
                // 更改附魔
                item.addUnsafeEnchantment(enchantment, maxLevel);
                changed = true;
            }
        }
        if (changed) {
            // 发送消息
            Player player = getPlayerFromItem(item);
            if (player != null && enableMessages) {
                Bukkit.getScheduler().runTask(this, () -> player.sendMessage(limitExceededMessage));
            }
            item.setItemMeta(item.getItemMeta());
        }
    }

    private Player getPlayerFromItem(ItemStack item) {
        if (item.getItemMeta() != null) {
            for (Player player : getServer().getOnlinePlayers()) {
                if (player.getInventory().contains(item)) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("enchantmentlimit")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (sender.hasPermission("enchantmentlimit.reload")) {
                        reloadConfig();
                        loadEnchantmentLimits();
                        loadLocalization();
                        checkExistingEnchantments();
                        sender.sendMessage(configReloadedMessage);
                        return true;
                    } else {
                        sender.sendMessage(noPermissionMessage);
                        return true;
                    }
                } else if (args[0].equalsIgnoreCase("bypass")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        ItemStack item = player.getInventory().getItemInMainHand();
                        if (item != null && item.getType() != org.bukkit.Material.AIR) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta.getPersistentDataContainer().has(BYPASS_KEY, PersistentDataType.BYTE)) {
                                meta.getPersistentDataContainer().remove(BYPASS_KEY);
                                player.sendMessage(bypassDisabledMessage);
                            } else {
                                meta.getPersistentDataContainer().set(BYPASS_KEY, PersistentDataType.BYTE, (byte) 1);
                                player.sendMessage(bypassEnabledMessage);
                            }
                            item.setItemMeta(meta); // 保存对物品元数据的更改
                        } else {
                            player.sendMessage(ChatColor.RED + "You must hold an item in your main hand to use this command.");
                        }
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("enchantmentlimit")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("reload");
                completions.add("bypass");
                return completions;
            }
        }
        return null;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            TextComponent message = new TextComponent("欢迎加入白陌的插件社区群: ");
            message.setColor(ChatColor.GREEN);
            TextComponent link = new TextComponent("528651839");
            link.setColor(ChatColor.YELLOW);
            link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jq.qq.com/?_wv=1027&k=528651839"));
            message.addExtra(link);
            player.spigot().sendMessage(message);
        }
    }
}
