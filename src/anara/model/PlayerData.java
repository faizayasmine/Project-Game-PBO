package anara.model;

import java.io.*;
import java.util.*;

public class PlayerData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String name;
    private String password;
    private int gold;
    private List<ShopItem> inventory;
    private List<ShopItem> equippedItems;
    private int[] mapProgress;
    private int diamond;

    public PlayerData(String userId, String name) {
        this.userId = userId;
        this.name = name;
        this.gold = 300;
        this.diamond = 0;
        this.inventory = new ArrayList<>();
        this.equippedItems = new ArrayList<>();
        // Hanya Map 1 yang terbuka dari awal; map lain terbuka setelah menang map sebelumnya.
        this.mapProgress = new int[]{1, 0, 0, 0};
    }

    // --- Getters & Setters ---
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public void addDiamond(int amount) {
        this.diamond += amount;
    }

    public List<ShopItem> getInventory() {
        return inventory;
    }

    public List<ShopItem> getEquippedItems() {
        return equippedItems;
    }

    public int[] getMapProgress() {
        return mapProgress;
    }

    /** Buka map berikutnya (dipanggil setelah menang battle). Map di luar indeks 0..3 diabaikan. */
    public void unlockMap(int mapIndex) {
        if (mapIndex >= 0 && mapIndex < mapProgress.length) {
            mapProgress[mapIndex] = 1;
        }
    }

    public boolean isMapUnlocked(int mapIndex) {
        if (mapIndex < 0 || mapIndex >= mapProgress.length) return false;
        return mapProgress[mapIndex] == 1;
    }

    public void addItem(ShopItem item) {
        inventory.add(item);
    }

    public void removeItem(ShopItem item) {
        inventory.remove(item);
        equippedItems.remove(item);
    }

    /**
     * Equip item. Dibatasi maksimal 1 item aktif per tipe (WEAPON/SKILL) —
     * mengequip item baru otomatis melepas item lain dari tipe yang sama,
     * supaya bonus stat tidak menumpuk tanpa batas.
     */
    public void equipItem(ShopItem item) {
        if (!inventory.contains(item) || equippedItems.contains(item)) {
            return;
        }
        equippedItems.removeIf(i -> {
            if (i.getType() == item.getType()) {
                i.setEquipped(false);
                return true;
            }
            return false;
        });
        equippedItems.add(item);
        item.setEquipped(true);
    }

    public void unequipItem(ShopItem item) {
        equippedItems.remove(item);
        item.setEquipped(false);
    }

    public int getTotalAttackBonus() {
        return equippedItems.stream()
                .filter(i -> i.getType() == ShopItem.ItemType.WEAPON)
                .mapToInt(ShopItem::getStatBonus).sum();
    }

    public int getTotalSkillBonus() {
        return equippedItems.stream()
                .filter(i -> i.getType() == ShopItem.ItemType.SKILL)
                .mapToInt(ShopItem::getStatBonus).sum();
    }
}
