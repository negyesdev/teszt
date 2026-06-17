# GeNe Shop

Dinamikus árazású GUI shop plugin Minecraft szerverekre (**1.21.8**, Paper/Spigot).

**Készítette:** negyes Gerii06

---

## ✨ Mit tud?

- **Szépen rendezett GUI shop** – kategóriákba szedett itemekkel (blokkok, ércek, farm, kaja, mob dropok, természet, egyéb/redstone).
- **Vétel és eladás** néhány kattintással.
- **Dinamikus árazás** – ez a plugin lényege:
  - Amikor a szerveren valaki **elad** egy itemből, annak az **eladási ára kicsit beesik**.
  - Az ár ezután **magától, folyamatosan visszamászik** az alapértékre. A teljes visszaállás a legaljáról kb. **2,5 óra** (állítható), tehát ~2-3 órán belül minden ár normálisra áll vissza.
  - A **vételi ár fix**, ezért nem lehet "olcsón veszek – drágán eladok" trükkel visszaélni a rendszerrel.
- **Védett ablak** – a shop GUI-ból **semmit nem lehet kivenni vagy belerakni**, csak a vétel/eladás működik.
- A dinamikus árazás **lemezre mentődik**, így szerver-újraindítás után is pontos marad (időbélyeg alapján számol).

---

## 🎮 Használat

| Parancs | Leírás |
|---|---|
| `/shop` | Megnyitja a fő menüt (aliasok: `/bolt`, `/shopgui`). |
| `/shop <kategória>` | Egyből egy kategóriát nyit meg. |
| `/geneshop reload` | Újratölti a configot. |
| `/geneshop prices` | Listázza a jelenleg beesett árakat. |

### Kattintások a shopban

| Kattintás | Művelet |
|---|---|
| **Bal klikk** | Vásárlás 1 db |
| **Shift + bal** | Vásárlás 1 stack |
| **Jobb klikk** | Eladás 1 db |
| **Shift + jobb** | Az összes eladása az adott itemből |

---

## 🔐 Jogosultságok

| Jog | Alap | Leírás |
|---|---|---|
| `geneshop.use` | mindenki | A shop használata. |
| `geneshop.admin` | OP | Admin parancsok (`reload`, `prices`). |

---

## ⚙️ Telepítés

1. Tedd a `GeNeShop-1.0.0.jar` fájlt a szerver `plugins/` mappájába.
2. Telepíts egy **Vault**-ot és egy gazdasági plugint (pl. EssentialsX).
3. Indítsd újra a szervert.
4. A beállítások a `plugins/GeNeShop/config.yml` fájlban szerkeszthetők.

### Dinamikus árazás beállításai (`config.yml`)

```yaml
dynamic-pricing:
  enabled: true          # be/ki kapcsolás
  drop-per-sale: 0.0008  # 1 db eladása ennyivel csökkenti az ár-szorzót
  min-multiplier: 0.40   # az ár max ennyire eshet (0.40 = -60%)
  recover-minutes: 150   # ennyi perc alatt áll vissza a legaljáról (2,5 óra)
  save-interval-seconds: 120
```

---

## 🛠️ Fordítás forrásból

```bash
mvn clean package
```

A kész jar a `target/GeNeShop-1.0.0.jar` lesz. (Java 21 szükséges.)

---

*GeNe Shop – készítette: negyes Gerii06*
