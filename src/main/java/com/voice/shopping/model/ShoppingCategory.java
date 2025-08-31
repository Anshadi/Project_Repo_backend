package com.voice.shopping.model;

public enum ShoppingCategory {
    DAIRY("Dairy", "🥛"),
    MEAT("Meat", "🥩"),
    VEGETABLES("Vegetables", "🥬"),
    FRUITS("Fruits", "🍎"),
    BAKERY("Bakery", "🍞"),
    BEVERAGES("Beverages", "🥤"),
    SNACKS("Snacks", "🍿"),
    GRAINS("Grains", "🌾"),
    FROZEN("Frozen", "❄️"),
    CANNED("Canned", "🥫"),
    PERSONAL_CARE("Personal Care", "🧴"),
    HOUSEHOLD("Household", "🧽"),
    OTHER("Other", "📦");

    private final String displayName;
    private final String icon;

    ShoppingCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public static ShoppingCategory fromString(String category) {
        if (category == null) return OTHER;
        
        for (ShoppingCategory sc : values()) {
            if (sc.displayName.equalsIgnoreCase(category) || sc.name().equalsIgnoreCase(category)) {
                return sc;
            }
        }
        return OTHER;
    }

    public static String getIconForCategory(String category) {
        return fromString(category).getIcon();
    }

    public static boolean isValidCategory(String category) {
        return fromString(category) != OTHER || "other".equalsIgnoreCase(category);
    }
}
