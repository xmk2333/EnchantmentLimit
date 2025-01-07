# EnchantmentLimit Plugin 🎉

Welcome to the EnchantmentLimit plugin! This plugin allows you to set limits on enchantment levels for items in Minecraft. It also provides commands to bypass these limits for specific items. 

## Features ✨

- **Enchantment Level Limits**: Set maximum levels for different enchantments.
- **Bypass Command**: Allow players to bypass enchantment limits for specific items.
- **Localization**: Customizable messages for different events.
- **Event Handling**: Monitors various events to enforce enchantment limits.

## Commands 📜

- `/enchantmentlimit reload` - Reload the plugin configuration.
- `/enchantmentlimit bypass` - Toggle bypass mode for the item in your main hand.

## Permissions 🔑

- `enchantmentlimit.reload` - Permission to reload the plugin configuration.
- `enchantmentlimit.bypass` - Permission to bypass enchantment limits.

## Installation 📦

1. Download the plugin jar file.
2. Place the jar file in your server's `plugins` directory.
3. Start or restart your server.

## Configuration ⚙️

The plugin generates a default configuration file on first run. You can edit this file to set custom enchantment limits and messages.

```yaml
# Example configuration
enchantments:
  minecraft:sharpness: 5
  minecraft:protection: 4
  # Following omission
messages:
  limitExceeded: "&cYou have exceeded the enchantment limit!"
  configReloaded: "&aEnchantmentLimit configuration reloaded."
  noPermission: "&cYou do not have permission to execute this command."
  bypassEnabled: "&aBypass enabled for this item."
  bypassDisabled: "&cBypass disabled for this item."
settings:
  enableMessages: true
```

## Events 📅

The plugin listens to various events to enforce enchantment limits:

- EnchantItemEvent
- PrepareAnvilEvent
- InventoryClickEvent
- InventoryDragEvent
- InventoryMoveItemEvent
- InventoryOpenEvent
- InventoryCloseEvent
- PlayerInteractEvent
- EntityPickupItemEvent
- InventoryPickupItemEvent

## Join Our Community 💬

If you have any questions or need support, feel free to join our community!

[![Join our QQGroup](https://img.shields.io/badge/QQGroup-528651839-blue)](https://jq.qq.com/?_wv=1027&k=528651839)

## License 📄

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Enjoy the plugin and happy enchanting! 🪄