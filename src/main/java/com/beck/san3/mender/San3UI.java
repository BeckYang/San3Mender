package com.beck.san3.mender;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.win32.OS;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class San3UI {
    private final Shell shell = new Shell();
    private TabFolder tabFolder;
    private CCombo saveSlot;
    private PersonTab personTab;
    private CityTab cityTab;
    private CountryTab countryTab;
    private Button btnSave;
    
    //non-UI
    private SaveState saveState;

    public San3UI() {
        //super(parent, style);
        shell.setMinimumSize(new Point(800, 800));
        shell.setSize(800, 560);
        shell.setText("三國志3 for dos 存檔修改器");
    }

    public static void main(String[] args) {
        //final Shell shell = new Shell();
        new San3UI().open();
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public void open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            try {
                if (!display.readAndDispatch())
                    display.sleep();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        final GridLayout gridLayout = new GridLayout(3, false);
        shell.setLayout(gridLayout);
        {
            //new Label(shell, SWT.NONE).setText("存檔位置");
            //slotIndex = SWTool.newSpinner(shell, 10, 1);
            //slotIndex.addListener(SWT.Selection, this::selectSlot);
            
        }{
            final Button btnLoad = new Button(shell, SWT.NONE);
            btnLoad.setBounds(0, 0, 75, 25);
            final String title = "選擇存檔";
            btnLoad.setText(title);
            btnLoad.addListener(SWT.Selection, this::load);
        }{
            saveSlot = SWTool.newCombo(shell, SWT.NONE, Collections.emptyList());
            saveSlot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            saveSlot.addListener(SWT.Selection, this::selectSlot);
        }{
            btnSave = new Button(shell, SWT.NONE);
            btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            btnSave.setText("儲存");
            btnSave.setData("dirtyColor", shell.getDisplay().getSystemColor(OS.COLOR_HIGHLIGHT));
            btnSave.setData("oldColor", btnSave.getBackground());
            btnSave.addListener(SWT.Selection, this::save);
        }{
            tabFolder = new TabFolder(shell, SWT.NONE);
            {
                GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, gridLayout.numColumns, 1);
                gd_tabFolder.heightHint = 314;
                gd_tabFolder.widthHint = 586;
                tabFolder.setLayoutData(gd_tabFolder);
            }{
                personTab = new PersonTab(tabFolder, SWT.NONE);
                TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
                tabItem.setText("人物屬性");
                tabItem.setControl(personTab);
            }{
                cityTab = new CityTab(tabFolder, SWT.NONE);
                TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
                tabItem.setText("都市屬性");
                tabItem.setControl(cityTab);
            }{
                countryTab = new CountryTab(tabFolder, SWT.NONE);
                TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
                tabItem.setText("外交/流浪");
                tabItem.setControl(countryTab);
            }{
                final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
                tabItem.setText("設定");
                tabItem.setControl(new SettingTab(tabFolder, SWT.NONE));
                /*{
                    final Composite composite = new Composite(tabFolder, SWT.NONE);
                    tabItem.setControl(composite);
                    composite.setLayout(new GridLayout(1, false));
                }*/
            }
            tabFolder.addListener(SWT.Selection, this::tabSelectionChange);
        }

    }
    
    private void tabSelectionChange(final Event event) {
        refreshTabItems(tabFolder.getSelection());
    }
    private void refreshTabItems(TabItem[] tabItems) {
        for (TabItem tabItem : tabItems) {
            final Control control = tabItem.getControl();
            if (control instanceof Refreshable) {
                ((Refreshable)control).refresh();
            }
        }
    }
    
    private void clearDirty() {
        final Color color = (Color)btnSave.getData("oldColor");
        btnSave.setBackground(color);
    }
    
    private void setDirty() {
        final Color color = (Color)btnSave.getData("dirtyColor");
        btnSave.setBackground(color);
    }

    private void handleDirtyEvent() {
        shell.getDisplay().asyncExec(this::setDirty);
    }

    private void save(final Event event) {
        if (saveState != null) {
            boolean modified = saveState.isModified();
            if (modified && !MessageDialog.openConfirm(shell, "警告!", "發現在此檔案讀取之後，有其他程式修改過它，是否確定繼續儲存?\n(可能發生資料不一致情況)")) {
                return;
            }
            try {
                saveState.save();
                refreshTabItems(tabFolder.getItems());
                if (modified) {
                    SWTool.updateCombo(saveSlot, saveState.readIndex());
                }
                clearDirty();
            } catch (Exception e) {
                handleException(e);
            }
        }
    }

    private void load(final Event event) {
        FileDialog dialog = new FileDialog(shell);
        if (saveState != null) {
            dialog.setFileName(saveState.getFileName());
        }
        String path = dialog.open();
        File file = path == null ? null : new File(path);
        if (file != null && file.exists()) {
            try {
                loadFile(file);
                final Button btnLoad = (Button) event.widget;
                btnLoad.setToolTipText(btnLoad.getText() + file.getAbsolutePath());
            } catch (Exception e) {
                handleException(e);
            }
        }
    }
    
    private void handleException(final Exception e) {
        e.printStackTrace();
        MessageDialog.openError(Display.getDefault().getActiveShell(),"Error", e.getMessage());
    }
    
    private void selectSlot(final Event event) {
        try {
            int slotIndex = SWTool.getComboValue(saveSlot);
            if (saveState.isModified()) {
                SWTool.updateCombo(saveSlot, saveState.readIndex());
            }
            saveState.readSlot(slotIndex);
        } catch (Exception e) {
            handleException(e);
        }
        clearDirty();
        personTab.load(saveState);
        cityTab.load(saveState);
        countryTab.load(saveState);
    }

    private void loadFile(final File file) throws Exception {
        final SaveState ss = SaveState.loadFile(file);
        final List<ComboItem> saveSlotList = ss.readIndex();
        SWTool.updateCombo(saveSlot, saveSlotList);
        if (saveSlotList.isEmpty()) {
            saveState = null;
            return;
        } else {
            int slot = saveSlotList.get(0).value;
            SWTool.selectCombo(saveSlot, slot);
            saveState = ss;
        }
        saveState.addDirtyListener(this::handleDirtyEvent);
        selectSlot(null);
    }
}
