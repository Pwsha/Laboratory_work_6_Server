package org.example.client.gui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.client.gui.LanguageManager;
import org.example.client.gui.StudyGroupTableModel;
import org.example.common.init.StudyGroup;

import javafx.scene.control.TableCell;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TableManager {
    private final TableView<StudyGroupTableModel> tableView;
    private ObservableList<StudyGroupTableModel> tableData;
    private FilteredList<StudyGroupTableModel> filteredData;
    private SortedList<StudyGroupTableModel> sortedData;
    private final LanguageManager lang;
    private int currentUserId;
    private String currentFilterColumn = "name";
    private String currentFilterText = "";

    private final Map<String, Comparator<StudyGroupTableModel>> sortComparators = new HashMap<>();
    private final Map<String, Function<StudyGroupTableModel, String>> filterExtractors = new HashMap<>();
    private final Map<String, String> columnTexts = new HashMap<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TableManager(LanguageManager lang) {
        this.lang = lang;
        this.tableView = createTableView();
        initMaps();
        updateColumnTexts();
    }

    private void initMaps() {
        sortComparators.put("id", Comparator.comparing(StudyGroupTableModel::getId));
        sortComparators.put("name", Comparator.comparing(StudyGroupTableModel::getName));
        sortComparators.put("x", Comparator.comparingDouble(StudyGroupTableModel::getX));
        sortComparators.put("y", Comparator.comparingLong(StudyGroupTableModel::getY));
        sortComparators.put("creationDate", Comparator.comparing(StudyGroupTableModel::getCreationDate));
        sortComparators.put("studentsCount", Comparator.comparingLong(StudyGroupTableModel::getStudentsCount));
        sortComparators.put("expelledStudents", Comparator.comparingInt(StudyGroupTableModel::getExpelledStudents));
        sortComparators.put("formOfEducation", Comparator.comparing(StudyGroupTableModel::getFormOfEducation));
        sortComparators.put("semesterEnum", Comparator.comparing(StudyGroupTableModel::getSemesterEnum));

        filterExtractors.put("id", item -> String.valueOf(item.getId()));
        filterExtractors.put("name", StudyGroupTableModel::getName);
        filterExtractors.put("x", item -> String.valueOf(item.getX()));
        filterExtractors.put("y", item -> String.valueOf(item.getY()));
        filterExtractors.put("creationDate", item -> item.getCreationDate() != null ? item.getCreationDate().format(dateFormatter) : "");
        filterExtractors.put("studentsCount", item -> String.valueOf(item.getStudentsCount()));
        filterExtractors.put("expelledStudents", item -> String.valueOf(item.getExpelledStudents()));
        filterExtractors.put("formOfEducation", StudyGroupTableModel::getFormOfEducation);
        filterExtractors.put("semesterEnum", StudyGroupTableModel::getSemesterEnum);

        columnTexts.put("id", lang.getString("table.id"));
        columnTexts.put("name", lang.getString("table.name"));
        columnTexts.put("x", "X");
        columnTexts.put("y", "Y");
        columnTexts.put("creationDate", lang.getString("table.creationDate"));
        columnTexts.put("studentsCount", lang.getString("table.studentsCount"));
        columnTexts.put("expelledStudents", lang.getString("table.expelledStudents"));
        columnTexts.put("formOfEducation", lang.getString("table.formOfEducation"));
        columnTexts.put("semesterEnum", lang.getString("table.semester"));
    }

    private TableView<StudyGroupTableModel> createTableView() {
        TableView<StudyGroupTableModel> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StudyGroupTableModel, Long> idCol = new TableColumn<>(lang.getString("table.id"));
        idCol.setId("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setSortable(true);

        TableColumn<StudyGroupTableModel, String> nameCol = new TableColumn<>(lang.getString("table.name"));
        nameCol.setId("name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        nameCol.setSortable(true);

        TableColumn<StudyGroupTableModel, Float> xCol = new TableColumn<>("X");
        xCol.setId("x");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        xCol.setPrefWidth(60);
        xCol.setSortable(true);

        TableColumn<StudyGroupTableModel, Long> yCol = new TableColumn<>("Y");
        yCol.setId("y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        yCol.setPrefWidth(60);
        yCol.setSortable(true);

        TableColumn<StudyGroupTableModel, LocalDateTime> dateCol = new TableColumn<>(lang.getString("table.creationDate"));
        dateCol.setId("creationDate");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        dateCol.setCellFactory(col -> new TableCell<StudyGroupTableModel, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        dateCol.setPrefWidth(150);
        dateCol.setSortable(true);

        TableColumn<StudyGroupTableModel, Long> studentsCol = new TableColumn<>(lang.getString("table.studentsCount"));
        studentsCol.setId("studentsCount");
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentsCount"));
        studentsCol.setPrefWidth(100);
        studentsCol.setSortable(true);

        TableColumn<StudyGroupTableModel, Integer> expelledCol = new TableColumn<>(lang.getString("table.expelledStudents"));
        expelledCol.setId("expelledStudents");
        expelledCol.setCellValueFactory(new PropertyValueFactory<>("expelledStudents"));
        expelledCol.setPrefWidth(80);
        expelledCol.setSortable(true);

        TableColumn<StudyGroupTableModel, String> formCol = new TableColumn<>(lang.getString("table.formOfEducation"));
        formCol.setId("formOfEducation");
        formCol.setCellValueFactory(new PropertyValueFactory<>("formOfEducation"));
        formCol.setPrefWidth(150);
        formCol.setSortable(true);

        TableColumn<StudyGroupTableModel, String> semCol = new TableColumn<>(lang.getString("table.semester"));
        semCol.setId("semesterEnum");
        semCol.setCellValueFactory(new PropertyValueFactory<>("semesterEnum"));
        semCol.setPrefWidth(80);
        semCol.setSortable(true);

        tv.getColumns().addAll(idCol, nameCol, xCol, yCol, dateCol, studentsCol, expelledCol, formCol, semCol);

        tv.setSortPolicy(t -> {
            return true;
        });

        return tv;
    }

    public void updateData(List<StudyGroup> groups, int currentUserId) {
        this.currentUserId = currentUserId;

        List<StudyGroupTableModel> models = groups.stream()
                .filter(g -> g != null && g.getId() != null)
                .map(g -> StudyGroupTableModel.from(g, currentUserId))
                .collect(Collectors.toList());

        tableData = FXCollections.observableArrayList(models);
        filteredData = new FilteredList<>(tableData, p -> true);

        applyFilter(currentFilterColumn, currentFilterText);

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    public void applyFilter(String column, String text) {
        if (filteredData == null) return;

        currentFilterColumn = column;
        currentFilterText = text;

        if (text == null || text.isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = text.toLowerCase();
            Function<StudyGroupTableModel, String> extractor = filterExtractors.get(column);
            if (extractor == null) {
                filteredData.setPredicate(p -> true);
            } else {
                filteredData.setPredicate(item -> {
                    String value = extractor.apply(item);
                    return value != null && value.toLowerCase().contains(lower);
                });
            }
        }
    }

    public void applySort(String column) {
        if (tableView == null) return;

        for (TableColumn<StudyGroupTableModel, ?> col : tableView.getColumns()) {
            if (column.equals(col.getId())) {
                tableView.getSortOrder().clear();
                tableView.getSortOrder().add(col);
                break;
            }
        }
    }

    public void updateColumnTexts() {
        for (TableColumn<StudyGroupTableModel, ?> col : tableView.getColumns()) {
            String id = col.getId();
            if (id != null && columnTexts.containsKey(id)) {
                col.setText(columnTexts.get(id));
            }
        }
    }

    public TableView<StudyGroupTableModel> getTableView() {
        return tableView;
    }

    public StudyGroupTableModel getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    public int getCurrentUserId() {
        return currentUserId;
    }
}