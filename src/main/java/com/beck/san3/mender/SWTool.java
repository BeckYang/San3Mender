package com.beck.san3.mender;

import java.util.List;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class SWTool {
    public static Label newLabel(final Composite parent, final String text) {
        final Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    public static Spinner newSpinner(final Composite parent, final int max, final int min) {
        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setMinimum(min);
        spinner.setMaximum(max);
        return spinner;
    }
    public static Spinner newSpinner(final Composite parent, int max) {
        return newSpinner(parent, max, 0);
    }
    
    @SuppressWarnings("unchecked")
    public static int getComboValue(final CCombo combo) {
        final String text = combo.getText();
        final List<ComboItem> list = (List<ComboItem>)combo.getData(ComboItem.class.getName());
        final Optional<ComboItem> optional = list.stream().filter(ci -> text.equals(ci.text)).findFirst();
        if (optional.isPresent()) {
            return optional.get().value;
        } else {
            return -1;
        }
    }
    
    public static void selectCombo(final CCombo combo, final int value) {
        selectCombo(combo, value, false);
    }
    @SuppressWarnings("unchecked")
    public static void selectCombo(final CCombo combo, final int value, final boolean enableIfNoItem) {
        final ComboItem comboItem = value < 0 ? null : ComboItem.from(value, (List<ComboItem>)combo.getData(ComboItem.class.getName())).orElse(null);
        if (comboItem == null) {
            combo.setEnabled(enableIfNoItem);
            combo.setText("");
        } else {
            combo.setEnabled(comboItem.selectable);
            combo.setText(comboItem.text);
        }
    }
    
    public static CCombo newCombo(final Composite parent, final int style, final List<ComboItem> comboItems) {
        final CCombo combo = new CCombo(parent, style);
        combo.setEditable(false);
        _updateCombo(combo, comboItems);
        return combo;
    }
    public static void updateCombo(final CCombo combo, final List<ComboItem> comboItems) {
        final int oldSelection = getComboValue(combo);
        _updateCombo(combo, comboItems);
        if (oldSelection != -1) {
            selectCombo(combo, oldSelection);
        }
    }
    private static void _updateCombo(final CCombo combo, final List<ComboItem> comboItems) {
        combo.setData(ComboItem.class.getName(), comboItems);
        combo.removeAll();
        for (final ComboItem comboItem : comboItems) {
            if (comboItem.selectable) {
                combo.add(comboItem.text);
            }
        }
    }
    public static void addMaxEventHandler(final Label label, final Spinner spinner, final Refreshable tab) {
        spinner.setData(tab);//Refreshable.class.getName()
        label.setData(spinner);
        label.addListener(SWT.MouseDoubleClick, SWTool::maxAndApply);
    }

    private static void maxAndApply(Event event) {
        final Object targetWidget = event.widget.getData();
        if (targetWidget instanceof Spinner) {
            final Spinner spinner = (Spinner)targetWidget;
            final Refreshable tab = (Refreshable)spinner.getData();
            tab.maxAndApply(spinner);
        }
    }
    
}
