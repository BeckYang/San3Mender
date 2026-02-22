package com.beck.san3.mender;

import java.util.List;
import java.util.Optional;

public class ComboItem {
    public int value;
    public String text;
    public boolean selectable;
    
    public ComboItem(int value, String text, boolean selectable) {
        super();
        this.value = value;
        this.text = text;
        this.selectable = selectable;
    }

    public static Optional<ComboItem> from(final int value, final List<ComboItem> list) {
        return list.stream().filter(ci -> ci.value == value).findFirst();
    }
}
