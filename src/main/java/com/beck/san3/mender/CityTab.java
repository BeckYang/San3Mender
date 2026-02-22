package com.beck.san3.mender;

import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import static com.beck.san3.mender.SWTool.newSpinner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CityTab extends Composite implements Refreshable {
    private Table tblCity;
    private TableViewer tvCity;
    Text name;
    Spinner population;
    Spinner gold;
    Spinner rice;
    Spinner cultivation;
    Spinner farming;
    Spinner irrigation;//灌溉
    Spinner water;//治水
    Spinner business;//商業 *2
    Spinner taxRate;//稅率
    Spinner faithful;//民忠
    Spinner bow;//弩 *2
    Spinner powerfulBow;//強弩 *2
    Spinner horse;//軍馬 *2
    Spinner boat1;//戰艦
    Spinner boat2;//重艦
    Spinner boat3;//輕艦
    Spinner riceSell;//賣米
    Spinner riceBuy;//買米
    Spinner bowBuy;//弩
    Spinner powerfulBowBuy;//強弩
    Spinner horseBuy;//軍馬
    CCombo admin;
    Button cbCommandEnd;
    Button cbTradeable;
    CCombo country;
    Text counsellor;
    Button rbMajor;
    Button rbHomeless;
    Button btnMoveUp;
    Button btnMoveDown;
    private TableViewer tvPerson;
    
    private SaveState saveState;
    private City city;

    private class CityUIProvider extends BaseUIProvider {

        private CityUIProvider(final TableViewer tableViewer) {
            super(tableViewer);
        }

        @Override
        public String getColumnText(Object data, int index) {
            final City city = (City) data;
            return city.getColumnText(index);
        }

        @Override
        public void initColumnsAndEvent() {
            initColumnsAndEvent(City.getColumnHeader(),
                    null, City.getColumnType(),true);
        }
    }

    public CityTab(TabFolder parent, int style) {
        super(parent, style);
        final GridLayout layout = new GridLayout(2, false);
        setLayout(layout);
        {
            tblCity = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
            tblCity.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 8));
            tblCity.setHeaderVisible(true);
            tblCity.setLinesVisible(true);
            tvCity = new TableViewer(tblCity);
            final CityUIProvider uiProvider = new CityUIProvider(tvCity);
            uiProvider.initColumnsAndEvent();
            tvCity.setContentProvider(uiProvider);
            tvCity.setLabelProvider(uiProvider);
            tvCity.addSelectionChangedListener(this::selectRow);
        }
        newBasicGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        newWeaponGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        newBizGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        newStatusGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        
        {
            Group group = new Group(this, SWT.NONE);
            group.setLayout(new GridLayout(5, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
            rbMajor = new Button(group, SWT.RADIO);
            rbMajor.setText("太守、現役");
            rbHomeless = new Button(group, SWT.RADIO);
            rbHomeless.setText("在野武將");
            final Button rbStandby = new Button(group, SWT.RADIO);
            rbStandby.setText("可搜尋武將");
            rbStandby.addListener(SWT.Selection, this::switchPersonView);
            rbHomeless.addListener(SWT.Selection, this::switchPersonView);
            rbMajor.addListener(SWT.Selection, this::switchPersonView);
            rbMajor.setSelection(true);
            btnMoveUp = new Button(group, SWT.PUSH);
            btnMoveUp.setText("^");
            btnMoveUp.addListener(SWT.Selection, this::moveUp);
            btnMoveDown = new Button(group, SWT.PUSH);
            btnMoveDown.setText("V");
            btnMoveDown.addListener(SWT.Selection, this::moveDown);
        }
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData.heightHint = 100;
        Table tblPerson = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
        tblPerson.setLayoutData(gridData);
        tblPerson.setHeaderVisible(true);
        tblPerson.setLinesVisible(true);
        tvPerson = new TableViewer(tblPerson);
        final PersonUIProvider uiProvider = new PersonUIProvider(tvPerson, false);
        uiProvider.initColumnsAndEvent();
        tvPerson.setContentProvider(uiProvider);
        tvPerson.setLabelProvider(uiProvider);
        
        final Button btnSave = new Button(this, SWT.PUSH);
        btnSave.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnSave.setText("套用變更");
        btnSave.addListener(SWT.Selection, this::applyChange);
    }

    private Group newBasicGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("基本屬性");
        group.setLayout(new GridLayout(5, false));
        {
            new Label(group, SWT.NONE).setText("城市");
            name = new Text(group, SWT.BORDER);
            name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            name.setEditable(false);
            new Label(group, SWT.NONE).setText("人口");
            population = newSpinner(group, 32766);
            population.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        }
        {
            new Label(group, SWT.NONE).setText("君主");
            country = SWTool.newCombo(group, SWT.NONE, Collections.emptyList());
            country.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("軍師");
            counsellor = new Text(group, SWT.BORDER);
            counsellor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
            counsellor.setEditable(false);
        }
        {
            new Label(group, SWT.NONE).setText("黃金");
            gold = newSpinner(group, 32766);
            gold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("糧");
            rice = newSpinner(group, 2000000000);
            rice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        }
        {
            final Label lbcultivation = SWTool.newLabel(group, "開發(!)");
            final Label lbfarming = SWTool.newLabel(group, "耕作(!)");
            final Label lbirrigation = SWTool.newLabel(group, "灌溉(!)");
            final Label lbwater = SWTool.newLabel(group, "治水(!)");
            SWTool.newLabel(group, "商業");

            cultivation = newSpinner(group, 100);
            cultivation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            farming = newSpinner(group, 100);
            farming.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            irrigation = newSpinner(group, 100);
            irrigation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            water = newSpinner(group, 100);
            water.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            business = newSpinner(group, 9999);
            business.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            SWTool.addMaxEventHandler(lbcultivation, cultivation, this);
            SWTool.addMaxEventHandler(lbfarming, farming, this);
            SWTool.addMaxEventHandler(lbirrigation, irrigation, this);
            SWTool.addMaxEventHandler(lbwater, water, this);
        }
        return group;
    }
    

    private Group newStatusGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("狀態");
        group.setLayout(new GridLayout(6, false));
        {
            SWTool.newLabel(group, "稅率");
            taxRate = newSpinner(group, 100);
            taxRate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            final Label label = SWTool.newLabel(group, "民忠(!)");
            faithful = newSpinner(group, 100);
            faithful.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
            SWTool.addMaxEventHandler(label, faithful, this);
            new Label(group, SWT.NONE).setText("委任");
            admin = SWTool.newCombo(group, SWT.NONE, City.ADMIN_ITEMS);
            admin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }{
            cbCommandEnd = new Button(group, SWT.CHECK);
            cbCommandEnd.setText("命令結束");
            cbCommandEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            cbTradeable = new Button(group, SWT.CHECK);
            cbTradeable.setText("可交易");
            cbTradeable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        }
        return group;
    }

    private Group newWeaponGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("持有武器");
        group.setLayout(new GridLayout(6, false));
        {
            new Label(group, SWT.NONE).setText("弩");
            new Label(group, SWT.NONE).setText("強弩");
            new Label(group, SWT.NONE).setText("軍馬");
            new Label(group, SWT.NONE).setText("戰艦");
            new Label(group, SWT.NONE).setText("重艦");
            new Label(group, SWT.NONE).setText("輕艦");

            bow = newSpinner(group, 9999);
            bow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            powerfulBow = newSpinner(group, 9999);
            powerfulBow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            horse = newSpinner(group, 9999);
            horse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            boat1 = newSpinner(group, 100);
            boat1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            boat2 = newSpinner(group, 100);
            boat2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            boat3 = newSpinner(group, 100);
            boat3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        return group;
    }

    private Group newBizGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("買賣");
        group.setLayout(new GridLayout(5, false));
        {
            new Label(group, SWT.NONE).setText("賣米");
            new Label(group, SWT.NONE).setText("買米");
            new Label(group, SWT.NONE).setText("弩");
            new Label(group, SWT.NONE).setText("強弩");
            new Label(group, SWT.NONE).setText("軍馬");

            riceSell = newSpinner(group, 200, 1);
            riceSell.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            riceBuy = newSpinner(group, 200, 1);
            riceBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            bowBuy = newSpinner(group, 200, 1);
            bowBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            powerfulBowBuy = newSpinner(group, 200, 1);
            powerfulBowBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            horseBuy = newSpinner(group, 200, 1);
            horseBuy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        return group;
    }

    public void refresh() {
        tvCity.refresh();
        switchPersonView(null);
    }

    public void load(final SaveState saveState) {
        int cityNo = city == null ? -1 : city.getNo();
        this.saveState = saveState;
        if (saveState == null) {
            city = null;
            tvCity.setInput(new City[0]);
            tvPerson.setInput(new Person[0]);
            return;
        }
        final Object[] citys = saveState.cityList.toArray();
        tvCity.setInput(citys);
        final List<ComboItem> countryItems = Person.getCountryItems(saveState.personList, false);
        SWTool.updateCombo(country, countryItems);
        final Optional<City> cityOpt = saveState.getCity(cityNo);
        if (cityOpt.isPresent()) {
            City selectCity = cityOpt.get();
            tvCity.setSelection(new StructuredSelection(selectCity));
            fetchCity(selectCity);
        } else {
            city = null;
        }
    }

    private void selectRow(final SelectionChangedEvent event) {
        Object element = event.getStructuredSelection().getFirstElement();
        if (element != null) {
            fetchCity((City)element);
        }
    }

    private void fetchCity(final City city) {
        name.setText(city.name);
        population.setSelection(city.population);
        gold.setSelection(city.gold);
        rice.setSelection(city.rice);
        cultivation.setSelection(city.cultivation);
        farming.setSelection(city.farming);
        irrigation.setSelection(city.irrigation);
        water.setSelection(city.water);
        business.setSelection(city.business);
        taxRate.setSelection(city.taxRate);
        faithful.setSelection(city.faithful);
        bow.setSelection(city.bow);
        powerfulBow.setSelection(city.powerfulBow);
        horse.setSelection(city.horse);
        boat1.setSelection(city.boat1);
        boat2.setSelection(city.boat2);
        boat3.setSelection(city.boat3);
        riceSell.setSelection(city.riceSell);
        riceBuy.setSelection(city.riceBuy);
        bowBuy.setSelection(city.bowBuy);
        powerfulBowBuy.setSelection(city.powerfulBowBuy);
        horseBuy.setSelection(city.horseBuy);
        SWTool.selectCombo(admin, city.admin);
        cbCommandEnd.setSelection(city.isCommandEnd());
        cbTradeable.setSelection(city.isTradeable());
        
        Person mayor = saveState.getPerson(city.mayor).orElse(null);
        SWTool.selectCombo(country, mayor == null ? -1 : mayor.country);
        
        counsellor.setText(saveState.getPerson(city.counsellor).map(Person::getName).orElse(""));
        this.city = city;
        switchPersonView(null);
    }
    
    private void switchPersonView(Event event) {
        if (city == null) {
            return;
        }
        final PersonIdentity personIdentity;
        boolean orderChangeable = false;
        if (rbMajor.getSelection()) {
            personIdentity = PersonIdentity.hired;
            orderChangeable = true;
        } else if (rbHomeless.getSelection()) {
            personIdentity = PersonIdentity.homeless;
        } else {
            personIdentity = PersonIdentity.standby;
        }
        btnMoveUp.setEnabled(orderChangeable);
        btnMoveDown.setEnabled(orderChangeable);
        final List<Person> persons = saveState.getPersonChain(personIdentity.getFirstPerson(city));
        tvPerson.setInput(persons);
    }
    
    @SuppressWarnings("unchecked")
    private List<Person> getTvPerson() {
        return (List<Person>) tvPerson.getInput();
    }
    
    private void moveUp(Event event) {
        final List<Person> list = getTvPerson();
        Person selectPerson = (Person) tvPerson.getStructuredSelection().getFirstElement();
        int pos = list.indexOf(selectPerson);
        if (pos > 0) {
            list.set(pos, list.get(pos-1));
            list.set(pos-1, selectPerson);
            tvPerson.refresh();
        }
    }
    private void moveDown(Event event) {
        final List<Person> list = getTvPerson();
        Person selectPerson = (Person) tvPerson.getStructuredSelection().getFirstElement();
        int pos = list.indexOf(selectPerson);
        if ((pos + 1) < list.size()) {
            list.set(pos, list.get(pos+1));
            list.set(pos+1, selectPerson);
            tvPerson.refresh();//input is not change...
        }
    }
    
    @Override
    public void maxAndApply(final Spinner spinner) {
        if (city == null) return;
        spinner.setSelection(spinner.getMaximum());
        applyChange(null);
    }

    private void applyChange(Event event) {
        if (city != null) {
            city.gold = (short) gold.getSelection();
            city.rice = rice.getSelection();
            city.cultivation = cultivation.getSelection();
            city.farming = farming.getSelection();
            city.irrigation = irrigation.getSelection();
            city.water = water.getSelection();
            city.business = (short) business.getSelection();
            city.taxRate = taxRate.getSelection();
            city.faithful = faithful.getSelection();
            city.bow = (short) bow.getSelection();
            city.powerfulBow = (short) powerfulBow.getSelection();
            city.horse = (short) horse.getSelection();
            city.boat1 = boat1.getSelection();
            city.boat2 = boat2.getSelection();
            city.boat3 = boat3.getSelection();
            city.riceSell = riceSell.getSelection();
            city.riceBuy = riceBuy.getSelection();
            city.bowBuy = bowBuy.getSelection();
            city.powerfulBowBuy = powerfulBowBuy.getSelection();
            city.horseBuy = horseBuy.getSelection();
            city.admin = SWTool.getComboValue(admin);
            city.setStatus(cbCommandEnd.getSelection(), cbTradeable.getSelection());
            if (rbMajor.getSelection()) {
                saveState.updatePersonOrder(city, getTvPerson(), PersonIdentity.hired);
            }
            
            city.setDirty();
            saveState.fireDirtyEvent();
            tvCity.refresh(city);
        }
    }
}
