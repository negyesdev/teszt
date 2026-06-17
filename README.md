# GeNe Shop

**ShopGUI+ stílusú**, dinamikus árazású GUI shop plugin Minecraft szerverekre (**1.21.8**, Paper/Spigot).

**Készítette:** negyes Gerii06

---

## ✨ Mit tud?

- **ShopGUI+ kinézet** – ugyanaz a megjelenés, amit megszoktatok: egyedi **player-head ikonok**, **hex (#RRGGBB) színek**, szépen rendezett **kategóriás főmenü** és **többoldalas** kategóriák, balance-kijelző, fill-itemek, vissza/lapozás gombok.
- **15 kategória, ~1200 item** – a mellékelt ShopGUI+ csomag teljes tartalma, **1.21.8-ra frissítve**:
  Building Blocks, Colored Blocks, Food, Mob Drops, Miscellaneous, Minerals,
  Potions & Arrows, Spawners, Monster Eggs, Redstone, Enchanting,
  Dyes & Candles, Farming, Combat & Tools, Decoration.
- Támogatott item-típusok: sima itemek, **spawnerek** (mob beállítással), **dobható/iható bájitalok** (effekttel), **varázskönyvek** (enchantmenttel), és a **balance** speciális elem.
- **Dinamikus árazás** – ez a plugin egyedi logikája (változatlan):
  - Amikor a szerveren valaki **elad** egy itemből, annak az **eladási ára kicsit beesik**.
  - Az ár **magától, folyamatosan visszamászik** az alapra. A teljes visszaállás a legaljáról kb. **2,5 óra** (állítható), tehát ~2-3 órán belül minden ár normálisra áll vissza.
  - A **vételi ár fix** → nem lehet "olcsón veszek – drágán eladok" trükkel visszaélni.
  - A GUI-ban a `%sell%` ár **mindig az aktuális (dinamikus) árat** mutatja, és külön jelzés szól, ha egy ár épp beesett.
- **Védett ablak** – a shop GUI-ból **semmit nem lehet kivenni vagy belerakni**, csak a vétel/eladás fut le.
- Az árállapot **lemezre mentődik** (időbélyeg alapján), így szerver-újraindítás után is pontos.

---

## 🎮 Használat

| Parancs | Leírás |
|---|---|
| `/shop` | Megnyitja a főmenüt (aliasok: `/bolt`, `/shopgui`). |
| `/shop <kategória>` | Egyből egy kategóriát nyit meg (pl. `/shop minerals`). |
| `/geneshop reload` | Újratölti a configot, shopokat, üzeneteket. |
| `/geneshop prices` | Listázza a jelenleg beesett eladási árakat. |

### Kattintások a shopban (a `config.yml` `clickActions` szerint)

| Kattintás | Művelet |
|---|---|
| **Bal klikk** | Vásárlás 1 (az item alap mennyisége) |
| **Shift + bal** | Vásárlás 1 teljes stack |
| **Jobb klikk** | Eladás 1 |
| **Shift + jobb / Középső gomb** | Az összes eladása az adott itemből |

---

## 🔐 Jogosultságok

| Jog | Alap | Leírás |
|---|---|---|
| `geneshop.use` | mindenki | A shop használata. |
| `geneshop.admin` | OP | Admin parancsok (`reload`, `prices`). |

---

## ⚙️ Telepítés

1. Tedd a `GeNeShop-1.0.0.jar` fájlt a szerver `plugins/` mappájába.
2. Telepíts **Vault**-ot és egy gazdasági plugint (pl. EssentialsX).
3. Indítsd újra a szervert. Az alap csomag (config + shops + lang) automatikusan kicsomagolódik a `plugins/GeNeShop/` mappába, ahol szabadon szerkeszthető.

### Konfigurációs fájlok (`plugins/GeNeShop/`)

- `config.yml` – főmenü, ikonok, lore-formátum, gombok, kattintás-műveletek **és a dinamikus árazás** beállításai.
- `shops/*.yml` – a 15 kategória itemei (slot, oldal, ár).
- `lang.yml` – minden üzenet.

### Dinamikus árazás (`config.yml` → `dynamic-pricing`)

```yaml
dynamic-pricing:
  enabled: true
  drop-per-sale: 0.0008  # 1 db eladása ennyivel csökkenti az ár-szorzót
  min-multiplier: 0.40   # az ár max ennyire eshet (0.40 = -60%)
  recover-minutes: 150   # ennyi perc alatt áll vissza a legaljáról (2,5 óra)
  save-interval-seconds: 120
  show-discount-note: true
```

---

## 📌 Eltérés az eredeti ShopGUI+-tól

A megjelenés és a kategóriák/itemek **lényegében azonosak**. Két dolog tér el szándékosan:

1. **Eladási ár**: a GeNe Shop egyedi **dinamikus árazását** használja (a kérésnek megfelelően).
2. **Mennyiség-választó ablak**: a vétel/eladás közvetlenül, kattintással történik (bal = 1, shift+bal = 1 stack, jobb = 1, shift+jobb / középső = mind), nincs külön mennyiség-választó GUI.

---

## 🛠️ Fordítás forrásból

```bash
mvn clean package
```

A kész jar a `target/GeNeShop-1.0.0.jar` lesz. (Java 21 szükséges.)

---

*GeNe Shop – készítette: negyes Gerii06*
