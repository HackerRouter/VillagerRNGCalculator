package villager;

import java.util.List;

public class TradeResult {
    public enum Type { ENCHANT_BOOK, ENCHANTED_ITEM, DYED_ARMOR, TIPPED_ARROW, FIXED }

    public final Type type;
    public final List<String[]> enchantments; // [name, level]
    public final int price;
    public final String item;
    public final String potion;
    public final String label;

    private TradeResult(Type type, List<String[]> enchantments, int price, String item, String potion, String label) {
        this.type = type; this.enchantments = enchantments; this.price = price;
        this.item = item; this.potion = potion; this.label = label;
    }

    public static TradeResult fixed(String label) {
        return new TradeResult(Type.FIXED, null, 0, null, null, label);
    }
    public static TradeResult enchantBook(List<String[]> enchs, int price) {
        return new TradeResult(Type.ENCHANT_BOOK, enchs, price, null, null, null);
    }
    public static TradeResult enchantedItem(String item, List<String[]> enchs, int price) {
        return new TradeResult(Type.ENCHANTED_ITEM, enchs, price, item, null, null);
    }
    public static TradeResult dyedArmor(String item) {
        return new TradeResult(Type.DYED_ARMOR, null, 0, item, null, null);
    }
    public static TradeResult tippedArrow(String potion) {
        return new TradeResult(Type.TIPPED_ARROW, null, 0, null, potion, null);
    }

    @Override
    public String toString() {
        switch (type) {
            case ENCHANT_BOOK: {
                StringBuilder sb = new StringBuilder("EnchantBook(");
                for (String[] e : enchantments) sb.append(e[0]).append(" ").append(e[1]).append(", ");
                if (!enchantments.isEmpty()) sb.setLength(sb.length() - 2);
                return sb.append(") ").append(price).append("em").toString();
            }
            case ENCHANTED_ITEM: {
                StringBuilder sb = new StringBuilder("EnchantedItem(").append(item).append(")[");
                for (String[] e : enchantments) sb.append(e[0]).append(" ").append(e[1]).append(", ");
                if (!enchantments.isEmpty()) sb.setLength(sb.length() - 2);
                return sb.append("] ").append(price).append("em").toString();
            }
            case DYED_ARMOR: return "DyedArmor(" + item + ")";
            case TIPPED_ARROW: return "TippedArrow(" + potion + ")";
            default: return "Fixed(" + label + ")";
        }
    }
}
