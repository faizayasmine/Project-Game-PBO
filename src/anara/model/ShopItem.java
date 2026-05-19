package anara.model;

import java.io.Serializable;

public class ShopItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ItemType { WEAPON, SKILL}

    private String id;
    private String name;
    private String description;
    private int price;
    private int statBonus;
    private ItemType type;
    private boolean equipped;

    public ShopItem(String id, String name, String description, int price, int statBonus, ItemType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.statBonus = statBonus;
        this.type = type;
        this.equipped = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPrice() { return price; }
    public int getStatBonus() { return statBonus; }
    public ItemType getType() { return type; }
    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean equipped) { this.equipped = equipped; }

    @Override
    public String toString() { return name; }
}