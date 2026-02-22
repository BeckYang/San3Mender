package com.beck.san3.mender;

import static com.beck.san3.mender.SWTool.newSpinner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class CountryTab extends Composite {
    static final String EMPTY_NAME = "　　　";
    
    private Table tblCountry;
    private TableViewer tvCountry;
    
    CCombo city;
    Spinner gold;
    Spinner rice;
    
    Group diplomacyGroup;
    Button alliance;
    Spinner hostility;
    
  //NON-UI
    private SaveState saveState;
    private LinkedHashMap<Integer, Country> aliveCountry;
    private Map<Country, String> lordName;
    private Country selectedCountry;
    private int selectedIndex;
    
    public CountryTab(TabFolder parent, int style) {
        super(parent, style);
        final GridLayout layout = new GridLayout(2, false);
        setLayout(layout);
        {
            tblCountry = new Table(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
            tblCountry.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
            tblCountry.setHeaderVisible(true);
            tblCountry.setLinesVisible(true);
            tvCountry = new TableViewer(tblCountry);
            final CountryUIProvider uiProvider = new CountryUIProvider(tvCountry, this::columnText);
            uiProvider.initColumnsAndEvent();
            tvCountry.setContentProvider(uiProvider);
            tvCountry.setLabelProvider(uiProvider);
            //tvCountry.addPostSelectionChangedListener(this::selectTable);
            tblCountry.addListener(SWT.MouseUp, this::clickTable);
        }
        newBasicGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        newDiplomacyGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        {
            final Button btnSave = new Button(this, SWT.PUSH);
            btnSave.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
            btnSave.setText("套用變更");
            btnSave.addListener(SWT.Selection, this::applyChange);
        }
    }
    
    private Group newDiplomacyGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("");//opposite lord name
        group.setLayout(new GridLayout(2, false));
        {
            new Label(group, SWT.NONE).setText("敵對");
            hostility = newSpinner(group, 100);
            gold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            alliance = new Button(group, SWT.CHECK);
            alliance.setText("同盟");
        }
        diplomacyGroup = group;
        return group;
    }
    
    private Group newBasicGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("流浪中");
        group.setLayout(new GridLayout(2, false));
        {
            new Label(group, SWT.NONE).setText("城市");
            city = SWTool.newCombo(group, SWT.NONE, Collections.emptyList());
            city.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("黃金");
            gold = newSpinner(group, 32766);
            gold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("糧");
            rice = newSpinner(group, 32766);
            rice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        return group;
    }
    
    public void load(final SaveState saveState) {
        this.saveState = saveState;
        final List<ComboItem> cityItems = City.getItems(saveState.cityList);
        SWTool.updateCombo(city, cityItems);
        final Map<Integer, Person> lordMap = Person.countryLord(saveState.personList)
                .collect(Collectors.toMap(Person::getNo, Function.identity()));
        aliveCountry = new LinkedHashMap<>();
        lordName = new HashMap<>();
        int index = 0;
        for (Country c : saveState.countryList) {
            final Person lord = lordMap.get(c.lord);
            if (lord != null) {
                lordName.put(c, lord.getName());
                aliveCountry.put(index, c);
            }
            index++;
        }
        TableColumn[] columns = ((CountryUIProvider) tvCountry.getLabelProvider()).getColumns();
        for (int i = 1; i < columns.length; i++) {
            final Country country = aliveCountry.get(i-1);
            TableColumn c = columns[i];
            if (country == null) {
                c.setWidth(0);
                c.setResizable(false);
            } else {
                c.setText(lordName.get(country));
                c.setResizable(true);
                c.pack();
            }
        }
        tvCountry.setInput(aliveCountry.values().toArray());
    }
    
    private void applyChange(Event event) {
        if (selectedCountry == null) {
            return;
        }
        if (selectedCountry.homelessCity >= 0) {
            selectedCountry.homelessCity = SWTool.getComboValue(city);
            selectedCountry.homelessGold = (short) gold.getSelection();
            selectedCountry.homelessRice = (short) rice.getSelection();
        }
        selectedCountry.hostility[selectedIndex] = hostility.getSelection();
        selectedCountry.setAlliance(selectedIndex, alliance.getSelection());
        selectedCountry.setDirty();
        saveState.fireDirtyEvent();
        tvCountry.refresh();
    }
    
    private void fetchCountry(Country c) {
        SWTool.selectCombo(city, c.homelessCity);
        gold.setSelection(c.homelessGold);
        rice.setSelection(c.homelessRice);
        boolean enabled = c.homelessCity >= 0;
        gold.setEnabled(enabled);
        rice.setEnabled(enabled);
    }
    
    private String columnText(Country country, int index) {
        if (index == 0) {
            String name = lordName.getOrDefault(country, EMPTY_NAME);
            if (country.homelessCity >= 0) {
                return name + "(流浪中)";
            } else {
                return name;
            }
        }
        final int idx = index - 1;
        String h = Integer.toString(country.hostility[idx]);
        boolean x = country.isAlliance(idx);
        return x ? (h + "*") : h;
    }
    
    private void clickTable(Event e) {
        Point p = new Point(e.x, e.y);
        ViewerCell cell = tvCountry.getCell(p);
        if (cell != null) {
            Country c = (Country)cell.getElement();
            fetchCountry(c);
            int idx = cell.getColumnIndex()-1;
            if (idx >= 0 && idx < c.hostility.length) {
                hostility.setSelection(c.hostility[idx]);
                alliance.setSelection(c.isAlliance(idx));
                selectedIndex = idx;
                selectedCountry = c;
                diplomacyGroup.setText(lordName.getOrDefault(aliveCountry.get(idx), EMPTY_NAME) + "->" + lordName.get(c));
            }
        } else {
            System.err.println("xx");
        }
    }
    /*
    private void selectTable(SelectionChangedEvent event) {
        Country country = (Country)tvCountry.getStructuredSelection().getFirstElement();
        if (country != null) {
            fetchCountry(country);
        }
    }*/

    private static class CountryUIProvider extends BaseUIProvider {
        final BiFunction<Country, Integer, String> func;
        CountryUIProvider(final TableViewer tableViewer, final BiFunction<Country, Integer, String> func) {
            super(tableViewer);
            this.func = func;
        }

        @Override
        public String getColumnText(Object data, int index) {
            return func.apply((Country)data, index);
        }

        @Override
        public void initColumnsAndEvent() {
            final String[] names = IntStream.range(0, 22).mapToObj(i -> EMPTY_NAME).toArray(String[]::new);
            initColumnsAndEvent(names, null, null, false);
        }
        
        private TableColumn[] getColumns() {
            return columns;
        }
    }
}
