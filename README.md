# AutoMe — Fabric Client Mod
<<<<<<< HEAD
仅限1.21.11fabric
=======

>>>>>>> 2e05b47 (ci: add release workflow)
> 自动为普通聊天消息添加前缀（如 `/me`），让每条消息都以动作形式发出。

[

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-green)

](https://minecraft.net)
[

![Fabric](https://img.shields.io/badge/Fabric_Loader-0.18.4-blue)

](https://fabricmc.net)
[

![License](https://img.shields.io/badge/License-MIT-yellow)

](LICENSE)

---

## 功能

- 自动为普通聊天添加自定义前缀
- 纯命令控制，无 GUI，稳定兼容各种 mod 环境
- 白名单服务器自动启用（`mc.mangomc.top`）
- 实时保存配置，重启后保留设置

---

## 不转换规则

以下情况消息**原样发送**，不添加前缀：

| 条件 | 示例 |
|------|------|
| 以 `/` 开头 | `/spawn` |
| 以 `.` 开头 | `.b` |
| 以 `#` 开头 | `#script` |
| 以 `$` 开头 | `$cmd` |
| 以 `&` 开头 | `&alias` |
| 以 `*` 开头 | `*mark` |
| 以 `@` 开头 | `@player` |
| 纯数字 | `123` |
| 等于 all（不区分大小写） | `all` / `ALL` |

---

## 命令

| 命令 | 说明 |
|------|------|
| `/autome` | 显示帮助 |
| `/autome on` | 启用前缀 |
| `/autome off` | 禁用前缀 |
| `/autome set /me` | 设置前缀为 `/me` |
| `/autome set "/say"` | 含斜杠时加引号 |
| `/autome status` | 查看当前状态 |

---

## 使用示例

启用后，前缀设为 `/me`：

| 你输入 | 实际发送 |
|--------|---------|
| `hello` | `/me hello` |
| `在吗` | `/me 在吗` |
| `/spawn` | `/spawn`（原样） |
| `123` | `123`（原样） |
| `all` | `all`（原样） |

---

## 安装

1. 安装 [Fabric Loader](https://fabricmc.net/use/) ≥ 0.15.0
2. 下载 [Fabric API](https://modrinth.com/mod/fabric-api)
3. 将 `autome-x.x.x.jar` 放入 `.minecraft/mods/`
4. 启动游戏

---

## 配置文件

自动生成于 `.minecraft/config/autome.json`：

```json
{
  "enabled": false,
  "prefix": "/me"
}
<<<<<<< HEAD
```

---

## 构建

### GitHub Actions（推荐）

1. Fork 本仓库
2. Actions → Build AutoMe → Run workflow
3. 下载 Artifacts 中的 jar

### Termux 本地构建

```bash
pkg install git openjdk-21 -y
git clone https://github.com/BzAxu/AutoMe_Reborn.git
cd AutoMe_Reborn
chmod +x gradlew
./gradlew build
```

---

## 开源协议

MIT License © BzAxu
=======
>>>>>>> 2e05b47 (ci: add release workflow)
