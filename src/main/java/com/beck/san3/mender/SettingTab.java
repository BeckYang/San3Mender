package com.beck.san3.mender;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

public class SettingTab extends Composite {
    private Text codeMapping;
    private Label errorMsg;
    
    public SettingTab(TabFolder parent, int style) {
        super(parent, style);
        final GridLayout layout = new GridLayout(1, false);
        setLayout(layout);
        {
            new Label(this, SWT.NONE).setText("人名字碼對應(每一行以`=`隔開，左方為16進位字元4碼，右方為對應的一個文字，例如：D98E=娜 )");
            final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1);
            gridData.heightHint = 280;
            codeMapping = new Text(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
            codeMapping.setLayoutData(gridData);
            codeMapping.addListener(SWT.FocusOut, this::applyCodeMapping);
            errorMsg = new Label(this, SWT.NONE);
            errorMsg.setForeground(JFaceColors.getErrorText(getDisplay()));
            errorMsg.setText("");//TODO test only
        }
    }
    
    private void applyCodeMapping(Event event) {
        try {
            StringTool.updateCustomCharMap(codeMapping.getText());
        } catch (Exception e) {
            errorMsg.setText("ERROR: " + e.getMessage());
        }
        
    }

}
