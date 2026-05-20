package anara.model;

import java.io.*;
import java.util.*;

public class PlayerData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private int gold;
    private List<ShopItem> inventory;
    private List<ShopItem> equippedItems;
    private int[] mapProgress;
    private int diamond;

    public PlayerData(String name) {
        this.name = name;
        this.gold = 300;
        this.diamond = 0;
        this.inventory = new ArrayList<>();
        this.equippedItems = new ArrayList<>();
        this.mapProgress = new int[]{1, 1, 1, 1}; // all unlocked for prototype
    }

    // --- Getters & Setters ---
    public String getName() { return name; }
    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public int getDiamond() { return diamond; }
public void setDiamond(int diamond) { this.diamond = diamond; }
public void addDiamond(int amount) { this.diamond += amount; }
    public List<ShopItem> getInventory() { return inventory; }
    public List<ShopItem> getEquippedItems() { return equippedItems; }
    public int[] getMapProgress() { return mapProgress; }

    public void addItem(ShopItem item) {
        inventory.add(item);
    }

    public void removeItem(ShopItem item) {
        inventory.remove(item);
        equippedItems.remove(item);
    }

    public void equipItem(ShopItem item) {
        if (inventory.contains(item) && !equippedItems.contains(item)) {
            equippedItems.add(item);
            item.setEquipped(true);
        }
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