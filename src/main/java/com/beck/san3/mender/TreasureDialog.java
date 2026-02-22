package com.beck.san3.mender;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

public class TreasureDialog extends Dialog {
    private final String[] labels = {"孫子兵法","孟德新書","遁甲天書三卷","太平要術",
            "青釭劍","倚天劍","七星劍","青龍偃月刀", 
            "赤兔馬", "的盧","爪黃飛電",
            "青囊書","玉璽"};
    private final int bookCount = 6;

    int treasure;
    private Button[] btns;

    protected TreasureDialog(final Shell parentShell, final int treasure) {
        super(parentShell);
        this.treasure = treasure;
    }
    
    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
    }
    
    @Override
    protected Control createDialogArea(final Composite parent) {
        getShell().setText("寶物設定");
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, true);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        int seq = 0;
        btns = new Button[labels.length + bookCount];
        for (int i = 0; i < labels.length; i++) {
            final Button btn = new Button(composite, SWT.CHECK);
            btn.setText(labels[i]);
            btn.setSelection(isOn(treasure, seq));
            btns[i] = btn;
            seq++;
        }
        btns[0].setEnabled(false);
        for (int i = 0; i < bookCount; i++) {
            final Button btn = new Button(composite, SWT.CHECK);
            btn.setText(labels[0]);
            btn.setSelection(isOn(treasure, seq));
            btn.addListener(SWT.Selection, this::bookSpHandler);
            btns[seq] = btn;
            seq++;
        }
        
        return composite;
    }
    
    private void bookSpHandler(Event event) {
        final Button btn = (Button) event.widget;
        if (btn.getSelection()) {
            btns[0].setSelection(true);
        } else {
            boolean anySelected = false;
            for (int i = labels.length; i < btns.length; i++) {
                if (btns[i].getSelection()) {
                    anySelected = true;
                }
            }
            if (!anySelected) {
                btns[0].setSelection(false);
            }
        }
    }
    
    public static boolean isOn(final int data, final int seq) {
        int mask = 1 << seq;
        return (data & mask) > 0;
    }
    
    @Override
    protected void okPressed() {
        int result = 0;
        for (int i = 0; i < btns.length; i++) {
            if (btns[i].getSelection()) {
                int mask = 1 << i;
                result = result | mask;
            }
        }
        treasure = result;
        super.okPressed();
    }

}
