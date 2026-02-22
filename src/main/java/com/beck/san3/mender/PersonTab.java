package com.beck.san3.mender;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import static com.beck.san3.mender.SWTool.newSpinner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PersonTab extends Composite implements Refreshable {
    private static final int FILTER_COUNTRY = 1;
    private static final int FILTER_CITY = 2;
    
    private CCombo filterType;
    private CCombo filterValue;
    private Table tblPerson;
    private TableViewer tvPerson;
    /**武力*/
    private Spinner strength;
    /**智力*/
    private Spinner intelligence;
    /**政治*/
    private Spinner governing;
    /**魅力*/
    private Spinner charm;
    /**陸指*/
    private Spinner abilityLand;
    /**水指*/
    private Spinner abilityWater;
    /**訓練*/
    private Spinner training;
    /**士氣*/
    private Spinner morale;
    /**忠誠*/
    private Spinner faithful;
    /**生年*/
    private Spinner birthYear;
    private Text name;
    /**官*/
    private CCombo role;
    /**血緣*/
    private Spinner consanguinity;
    /**相性*/
    private Spinner match;
    /**士兵*/
    private Spinner soldier;
    /**義理*/
    private Spinner rational;
    private Spinner ambition;//野心、運氣
    private Spinner lucky;
    private Spinner calmness;//冷靜、勇猛
    private Spinner brave;
    private Spinner lifespan;//壽命
    /**君主*/
    private CCombo country;
    private Spinner seniority;
    private CCombo countryActual;
    private Spinner seniorityActual;
    private CCombo work;
    private Spinner workMonthTotal;
    private Spinner workMonthDone;
    private Spinner workEffect;
    private CCombo workCity;
    
    private Button cbCommandEnd;
    private Button cbDead;
    private Button cbAmbush;
    private Button cbSick;

    private CCombo location;//所在
    
    //NON-UI
    private SaveState saveState;
    private Person person;
    private int newLocation;
    private PersonIdentity locationType;
    private Listener workCityHandler = this::selectWorkCity;

    public PersonTab(TabFolder parent, int style) {
        super(parent, style);
        final GridLayout layout = new GridLayout(2, false);
        this.setLayout(layout);
        {
            tblPerson = new Table(this, SWT.BORDER | SWT.FULL_SELECTION);
            tblPerson.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 6));
            tblPerson.setHeaderVisible(true);
            tblPerson.setLinesVisible(true);
            tvPerson = new TableViewer(tblPerson);
            final PersonUIProvider uiProvider = new PersonUIProvider(tvPerson, true);
            uiProvider.initColumnsAndEvent();
            tvPerson.setContentProvider(uiProvider);
            tvPerson.setLabelProvider(uiProvider);
            tvPerson.addSelectionChangedListener(this::selectRow);
        }
        newFilterPanel(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        newBasicGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        newStatusGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        newAbilityGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        newHiddenGroup(this).setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        
        final Button btnSave = new Button(this, SWT.PUSH);
        btnSave.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1));
        btnSave.setText("套用變更");
        btnSave.addListener(SWT.Selection, this::applyChange);
    }

    private Composite newFilterPanel(final Composite parent) {
        final Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new RowLayout(SWT.HORIZONTAL));//new GridLayout(4, false));
        final List<ComboItem> comboItems = Arrays.asList(new ComboItem(0, "全部", true),
                new ComboItem(FILTER_COUNTRY, "君主", true),
                new ComboItem(FILTER_CITY, "城市", true));
        new Label(panel, SWT.NONE).setText("人物簡易篩選");
        filterType  = SWTool.newCombo(panel, SWT.NONE, comboItems);
        filterType.addListener(SWT.Selection, this::selectFilter);
        filterValue = SWTool.newCombo(panel, SWT.NONE, comboItems);
        filterValue.addListener(SWT.Selection, this::selectFilterValue);
        //final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        //gridData.minimumWidth = 115;
        //filterValue.setLayoutData(gridData);
        filterValue.setLayoutData(new RowData(100, SWT.DEFAULT));
        final Button button = new Button(panel, SWT.PUSH);
        button.setText("尚未登場");
        button.addListener(SWT.Selection, this::filterOther);
        return panel;
    }
    
    private void filterOther(Event event) {
        SWTool.selectCombo(filterType, FILTER_COUNTRY);
        SWTool.selectCombo(filterValue, -1);
        selectFilterValue(event);
    }
    
    private void selectFilter(Event event) {
        final int type = SWTool.getComboValue(filterType);
        List<ComboItem> items = null;
        switch (type) {
        case FILTER_COUNTRY:
            items = Person.getCountryItems(saveState.personList, true);
            break;
        case FILTER_CITY:
            items = City.getItems(saveState.cityList);
            break;
        default:
            filterValue.setEnabled(false);
            updatePersonView(type, getPersonNo());
            return;
        }
        filterValue.setEnabled(true);
        filterValue.setText("");
        SWTool.updateCombo(filterValue, items);
    }
    private void selectFilterValue(Event event) {
        updatePersonView(SWTool.getComboValue(filterType), getPersonNo());
    }
    
    private void updatePersonView(final int filterType, final int personNo) {
        Object[] persons = null;
        final int fvalue = SWTool.getComboValue(filterValue);
        switch (filterType) {
        case FILTER_COUNTRY: persons = saveState.getPersonByCountry(fvalue); break;
        case FILTER_CITY: persons = saveState.getPersonByCity(fvalue); break;
        default:
            persons = saveState.personList.toArray();
            break;
        }
        ;
        tvPerson.setInput(persons == null ? new Object[0] : persons);
        final Optional<Person> personOpt = saveState.getPerson(personNo);
        if (personOpt.isPresent()) {
            Person p = personOpt.get();
            tvPerson.setSelection(new StructuredSelection(p));
            fetchPerson(p);
        } else {
            person = null;
        }
    }
    
    public void refresh() {
        tvPerson.setComparator(null); //reset sorting to default!
        tblPerson.setSortColumn(null);
        tvPerson.refresh();
    }

    private int getPersonNo() {
        return person == null ? -1 : person.getNo();
    }
    public void load(final SaveState saveState) {
        int personNo = getPersonNo();
        this.saveState = saveState;
        if (saveState == null) {
            person = null;
            tvPerson.setInput(new Person[0]);
            return;
        }
        final List<ComboItem> countryItems = Person.getCountryItems(saveState.personList, true);
        final List<ComboItem> cityItems = City.getItems(saveState.cityList);
        SWTool.updateCombo(country, countryItems);
        SWTool.updateCombo(countryActual, countryItems);
        SWTool.updateCombo(location, cityItems);
        SWTool.updateCombo(workCity, cityItems);
        final int type = SWTool.getComboValue(filterType);
        if (type == FILTER_COUNTRY) {
            SWTool.updateCombo(filterValue, countryItems);
            filterValue.setText("");
        } else if (type == FILTER_CITY) {
            SWTool.updateCombo(filterValue, cityItems);
        }
        updatePersonView(type, personNo);
    }

    private void selectRow(final SelectionChangedEvent event) {
        Object element = event.getStructuredSelection().getFirstElement();
        if (element != null) {
            fetchPerson((Person)element);
        }
    }

    private Group newBasicGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("基本屬性");
        group.setLayout(new GridLayout(6, false));
        {
            new Label(group, SWT.NONE).setText("姓名");
            name = new Text(group, SWT.BORDER);
            name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            name.setEditable(false);

            final Label birthYearLb = SWTool.newLabel(group, "生年");
            birthYearLb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            birthYear = SWTool.newSpinner(group, 255);
            birthYear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            
            final Label locationLb = new Label(group, SWT.NONE);
            locationLb.setText("所在都市");
            //locationLb.setToolTipText("參考用，修改請透過都市屬性");
            location = SWTool.newCombo(group, SWT.NONE, Arrays.asList());//new Text(group, SWT.BORDER);
            location.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            location.addListener(SWT.Selection, this::selectLocation);
        }
        {
            new Label(group, SWT.NONE).setText("士兵");
            soldier = newSpinner(group, 20000);
            soldier.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            final Label lbTraining = SWTool.newLabel(group, "訓練(!)");
            training = newSpinner(group, 100);
            training.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            SWTool.addMaxEventHandler(lbTraining, training, this);
            final Label lbMorale = SWTool.newLabel(group, "士氣(!)");
            morale = newSpinner(group, 120);
            morale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            SWTool.addMaxEventHandler(lbMorale, morale, this);
        }
        return group;
    }

    private Group newStatusGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("狀態");
        final java.util.List<ComboItem> emptyItems = Arrays.asList();
        group.setLayout(new GridLayout(6, false));
        {
            final Label lb = SWTool.newLabel(group, "忠誠(!)");
            faithful = newSpinner(group, 100, 0);
            faithful.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            SWTool.addMaxEventHandler(lb, faithful, this);
            new Label(group, SWT.NONE).setText("君主");
            country = SWTool.newCombo(group, SWT.NONE, emptyItems);
            country.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            country.setToolTipText("(參考用，所在都市才重要)");
            new Label(group, SWT.NONE).setText("仕官");
            seniority = newSpinner(group, 100, 1);
            seniority.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        {
            new Label(group, SWT.NONE).setText("官別");
            role = SWTool.newCombo(group, SWT.NONE, PersonIdentity.ROLE_ITEMS);
            role.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("裏君主");
            countryActual = SWTool.newCombo(group, SWT.NONE, emptyItems);
            countryActual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            new Label(group, SWT.NONE).setText("裏仕官");
            seniorityActual = newSpinner(group, 100, 1);
            seniorityActual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        {
            final Label workLb = SWTool.newLabel(group, "工作(!)");
            workLb.setToolTipText("在此雙擊滑鼠可進行設定間諜工作");
            workLb.addListener(SWT.MouseDoubleClick, this::spySetting);
            work = SWTool.newCombo(group, SWT.NONE, Person.WORK_ITEMS);
            work.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            workEffect = SWTool.newSpinner(group, 255);
            workEffect.setToolTipText("工作效果");
            workCity = SWTool.newCombo(group, SWT.NONE, emptyItems);
            workCity.setToolTipText("目標都市");
            workCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            workCity.setEnabled(false);
            workMonthDone = SWTool.newSpinner(group, 6);
            workMonthDone.setToolTipText("完成月數(參考用，修改無效)");
            workMonthTotal = SWTool.newSpinner(group, 6);
            workMonthTotal.setToolTipText("剩餘月數(參考用，修改無效)");
        }
        {
            cbCommandEnd = new Button(group, SWT.CHECK);
            cbCommandEnd.setText("已行動");
            cbSick = new Button(group, SWT.CHECK);
            cbSick.setText("傷病");
            cbAmbush = new Button(group, SWT.CHECK);
            cbAmbush.setText("埋伏");
            cbAmbush.addListener(SWT.Selection, this::ambushHandler);
            cbDead = new Button(group, SWT.CHECK);
            cbDead.setText("近期死亡");
            Button cbTreasure = new Button(group, SWT.PUSH);
            cbTreasure.setText("寶物設定");
            cbTreasure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
            cbTreasure.addListener(SWT.Selection, this::treasureHandler);
        }
        return group;
    }

    private Group newAbilityGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("能力");
        group.setLayout(new GridLayout(6, false));
        {
            new Label(group, SWT.NONE).setText("陸指");
            new Label(group, SWT.NONE).setText("水指");
            new Label(group, SWT.NONE).setText("武力");
            new Label(group, SWT.NONE).setText("智力");
            new Label(group, SWT.NONE).setText("政治");
            new Label(group, SWT.NONE).setText("魅力");

            abilityLand = newSpinner(group, 100, 1);
            abilityLand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            abilityWater = newSpinner(group, 100, 1);
            abilityWater.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            strength = newSpinner(group, 100, 1);
            strength.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            intelligence = newSpinner(group, 100, 1);
            intelligence.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            governing =newSpinner(group, 100, 1);
            governing.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            charm = newSpinner(group, 100, 1);
            charm.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        return group;
    }

    private Group newHiddenGroup(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setText("隱藏屬性");
        group.setLayout(new GridLayout(4, false));
        {
            new Label(group, SWT.NONE).setText("血緣");
            new Label(group, SWT.NONE).setText("相性");
            new Label(group, SWT.NONE).setText("義理");
            new Label(group, SWT.NONE).setText("壽命");
            
            consanguinity = SWTool.newSpinner(group, 127, -1);
            consanguinity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            match = SWTool.newSpinner(group, 128, 0);
            match.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            rational = newSpinner(group, 127);
            rational.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            lifespan = newSpinner(group, 7, 0);
            lifespan.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        {
            new Label(group, SWT.NONE).setText("野心");
            new Label(group, SWT.NONE).setText("運氣");
            new Label(group, SWT.NONE).setText("冷靜");
            new Label(group, SWT.NONE).setText("勇猛");
            
            ambition = SWTool.newSpinner(group, 15);
            ambition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            lucky = SWTool.newSpinner(group, 15);
            lucky.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            calmness = newSpinner(group, 15);
            calmness.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            brave = newSpinner(group, 15);
            brave.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        }
        return group;
    }

    public void fetchPerson(final Person person) {
        name.setText(person.name);
        lifespan.setSelection(person.lifespan);
        ambition.setSelection(person.ambition);
        lucky.setSelection(person.lucky);
        calmness.setSelection(person.calmness);
        brave.setSelection(person.brave);
        strength.setSelection(person.strength);
        intelligence.setSelection(person.intelligence);
        governing.setSelection(person.governing);
        charm.setSelection(person.charm);
        abilityLand.setSelection(person.abilityLand);
        abilityWater.setSelection(person.abilityWater);
        training.setSelection(person.training);
        morale.setSelection(person.morale);
        faithful.setSelection(person.faithful);
        birthYear.setSelection(person.birthYear);
        consanguinity.setSelection(person.consanguinity);
        match.setSelection(person.match);
        soldier.setSelection(person.soldier);
        rational.setSelection(person.rational);
        SWTool.selectCombo(country, person.country);
        seniority.setSelection(person.seniority);
        SWTool.selectCombo(countryActual, person.countryActual);
        seniorityActual.setSelection(person.seniorityActual);
        SWTool.selectCombo(location, person.location, true);
        SWTool.selectCombo(role, person.role);
        cbCommandEnd.setSelection(person.isCommandEnd());
        cbDead.setSelection(person.isDead());
        cbAmbush.setSelection(person.isAmbush());
        cbSick.setSelection(person.isSick());
        
        SWTool.selectCombo(work, person.work);
        SWTool.selectCombo(workCity, person.workCity);
        workEffect.setSelection(person.workEffect);
        workMonthDone.setSelection(person.getWorkMonthDone());
        workMonthTotal.setSelection(person.getWorkMonthTotal());
        locationType = null;
        this.person = person;
    }

    private void applyChange(Event event) {
        if (person == null) {
            return;
        }
        int newBirthYear = birthYear.getSelection();
        if (person.birthYear == 0 && newBirthYear != 0) {
            int answer = MessageDialog.open(MessageDialog.QUESTION, getShell(), "問題", "此人物已死亡，選擇武將身分", SWT.SHEET, "取消變更", "在野武將", "可搜尋武將");//,"已錄用武將");
            if (answer == 0) {
                newBirthYear = person.birthYear;
                birthYear.setSelection(person.birthYear);
                locationType = null;
            } else {
                locationType = PersonIdentity.values()[answer];
            }
        }
        person.setDirty();
        saveState.fireDirtyEvent();
        person.strength = strength.getSelection();
        person.intelligence = intelligence.getSelection();
        person.governing = governing.getSelection();
        person.charm = charm.getSelection();
        person.abilityLand = abilityLand.getSelection();
        person.abilityWater = abilityWater.getSelection();
        person.training = training.getSelection();
        person.morale = morale.getSelection();
        person.faithful = faithful.getSelection();
        person.birthYear = newBirthYear;
        person.consanguinity = consanguinity.getSelection();
        person.match = match.getSelection();
        person.soldier = (short) soldier.getSelection();
        person.rational = rational.getSelection();
        person.country = SWTool.getComboValue(country);
        person.seniority = seniority.getSelection();
        person.countryActual = SWTool.getComboValue(countryActual);
        person.seniorityActual = seniorityActual.getSelection();
        if (role.isEnabled()) {
            person.role = SWTool.getComboValue(role);
        }
        person.setStatus(cbCommandEnd.getSelection(), cbDead.getSelection());
        person.setAmbush(cbAmbush.getSelection());
        person.setSick(cbSick.getSelection());
        person.workEffect = workEffect.getSelection();
        if (locationType != null) {
            saveState.updateLocation(person, newLocation, locationType);
            SWTool.selectCombo(role, person.role);
        }
        tvPerson.refresh(person);
    }
    
    private void selectLocation(Event event) {
        newLocation = SWTool.getComboValue(location);
        if (PersonIdentity.notHired(person)) {
            int answer = MessageDialog.open(MessageDialog.QUESTION, getShell(), "問題", "此人物目前沒有君主，選擇武將身分", SWT.SHEET, "取消變更", "未登場", "在野武將", "可搜尋武將");//,"已錄用武將");
            if (answer == 0) {
                SWTool.selectCombo(location, person.location);
                locationType = null;
            } else {
                locationType = PersonIdentity.values()[answer-1];
            }
        } else {
            locationType = PersonIdentity.hired;
        }
    }
    
    private void treasureHandler(Event event) {
        if (person == null) {
            return;
        }
        final TreasureDialog dialog = new TreasureDialog(getShell(), person.treasure);
        if (dialog.open() == TreasureDialog.OK) {
            person.treasure = dialog.treasure;
            person.setDirty();
            saveState.fireDirtyEvent();
        }
    }
    
    private void ambushHandler(Event event) {
        if (person == null) {
            return;
        }
        if (cbAmbush.getSelection()) {
            countryActual.setEnabled(true);
            countryActual.setListVisible(true);
        } else {
            SWTool.selectCombo(countryActual, -1);
        }
    }
    
    public void maxAndApply(final Spinner spinner) {
        if (person == null) {
            return;
        }
        spinner.setSelection(spinner.getMaximum());
        applyChange(null);
    }
    
    private void spySetting(Event event) {
        if (person == null) {
            return;
        }
        workCity.setEnabled(true);
        workCity.setText("");
        workCity.addListener(SWT.Selection, workCityHandler);
        MessageDialog.openInformation(getShell(), "提示", "選定要從事間諜工作的城市後，會自動修改相關欄位(時間固定1個月)");
        workCity.setListVisible(true);
    }
    
    private void selectWorkCity(Event event) {
        int city = SWTool.getComboValue(workCity);
        workCity.removeListener(SWT.Selection, workCityHandler);
        workCity.setEnabled(false);
        if (person == null) {
            return;
        }
        cbCommandEnd.setSelection(true);
        workEffect.setSelection(240);
        person.doSpy(city);
        SWTool.selectCombo(work, person.work);
    }

}
