[English](README.md) | [简体中文](README_cn.md)

# EnchantmentLimit 插件 🎉

欢迎使用 EnchantmentLimit 插件！这个插件允许你为 Minecraft 中的物品设置附魔等级限制。它还提供了一些命令，可以绕过这些限制。

## 功能 ✨

- **附魔等级限制**：设置不同附魔的最大等级。
- **绕过命令**：允许玩家为特定物品绕过附魔限制。
- **本地化**：自定义不同事件的消息。
- **事件处理**：监控各种事件以强制执行附魔限制。

## 命令 📜

- `/enchantmentlimit reload` - 重新加载插件配置。
- `/enchantmentlimit bypass` - 切换主手物品的绕过模式。

## 权限 🔑

- `enchantmentlimit.reload` - 重新加载插件配置的权限。
- `enchantmentlimit.bypass` - 绕过附魔限制的权限。

## 安装 📦

1. 下载插件 jar 文件。
2. 将 jar 文件放入服务器的 `plugins` 目录。
3. 启动或重启服务器。

## 配置 ⚙️

插件首次运行时会生成一个默认配置文件。你可以编辑此文件以设置自定义的附魔限制和消息。

```yaml
# 示例配置
enchantments:
  minecraft:sharpness: 5
  minecraft:protection: 4
messages:
  limitExceeded: "&c你已经超过了附魔限制！"
  configReloaded: "&aEnchantmentLimit 配置已重新加载。"
  noPermission: "&c你没有执行此命令的权限。"
  bypassEnabled: "&a此物品的绕过已启用。"
  bypassDisabled: "&c此物品的绕过已禁用。"
settings:
  enableMessages: true
```

## 事件 📅

插件监听各种事件以强制执行附魔限制：

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

## 加入我们的社区 💬

如果你有任何问题或需要支持，请随时加入我们的社区！

[![加入我们的QQ群](https://img.shields.io/badge/QQGroup-528651839-blue)](https://jq.qq.com/?_wv=1027&k=528651839)

## 许可证 📄

本项目采用 MIT 许可证。详情请参阅 [LICENSE](LICENSE) 文件。
