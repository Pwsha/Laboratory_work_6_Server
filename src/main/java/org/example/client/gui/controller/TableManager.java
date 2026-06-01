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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableManager {
    private final TableView<StudyGroupTableModel> tableView;
    private ObservableList<StudyGroupTableModel> tableData;
    private FilteredList<StudyGroupTableModel> filteredData;
    private final LanguageManager lang;
    private int currentUserId;
    private final Map<String, String> columnTexts = new HashMap<>();

    public TableManager(LanguageManager lang) {
        this.lang = lang;
        this.tableView = createTableView();
    }

    private TableView<StudyGroupTableModel> createTableView() {
        TableView<StudyGroupTableModel> tv = new TableView<>();
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<StudyGroupTableModel, Long> idCol = new TableColumn<>(lang.getString("table.id"));
        idCol.setId("id");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, String> nameCol = new TableColumn<>(lang.getString("table.name"));
        nameCol.setId("name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, Float> xCol = new TableColumn<>("X");
        xCol.setId("x");
        xCol.setCellValueFactory(new PropertyValueFactory<>("x"));
        xCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, Long> yCol = new TableColumn<>("Y");
        yCol.setId("y");
        yCol.setCellValueFactory(new PropertyValueFactory<>("y"));
        yCol.setPrefWidth(60);

        TableColumn<StudyGroupTableModel, LocalDateTime> dateCol = new TableColumn<>(lang.getString("table.creationDate"));
        dateCol.setId("creationDate");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        dateCol.setCellFactory(col -> new javafx.scene.control.TableCell<StudyGroupTableModel, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
        dateCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, Long> studentsCol = new TableColumn<>(lang.getString("table.studentsCount"));
        studentsCol.setId("studentsCount");
        studentsCol.setCellValueFactory(new PropertyValueFactory<>("studentsCount"));
        studentsCol.setPrefWidth(100);

        TableColumn<StudyGroupTableModel, Integer> expelledCol = new TableColumn<>(lang.getString("table.expelledStudents"));
        expelledCol.setId("expelledStudents");
        expelledCol.setCellValueFactory(new PropertyValueFactory<>("expelledStudents"));
        expelledCol.setPrefWidth(80);

        TableColumn<StudyGroupTableModel, String> formCol = new TableColumn<>(lang.getString("table.formOfEducation"));
        formCol.setId("formOfEducation");
        formCol.setCellValueFactory(new PropertyValueFactory<>("formOfEducation"));
        formCol.setPrefWidth(150);

        TableColumn<StudyGroupTableModel, String> semCol = new TableColumn<>(lang.getString("table.semester"));
        semCol.setId("semesterEnum");
        semCol.setCellValueFactory(new PropertyValueFactory<>("semesterEnum"));
        semCol.setPrefWidth(80);

        tv.getColumns().addAll(idCol, nameCol, xCol, yCol, dateCol, studentsCol, expelledCol, formCol, semCol);
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
        tableView.setItems(filteredData);
    }

    public void applyFilter(String column, String text) {
        if (filteredData == null) return;
        if (text == null || text.isEmpty()) {
            filteredData.setPredicate(p -> true);
        } else {
            String lower = text.toLowerCase();
            filteredData.setPredicate(item -> {
                if (column.equals("ID")) {
                    return String.valueOf(item.getId()).contains(lower);
                } else if (column.equals(lang.getString("table.name"))) {
                    return item.getName().toLowerCase().contains(lower);
                } else if (column.equals(lang.getString("table.studentsCount"))) {
                    return String.valueOf(item.getStudentsCount()).contains(lower);
                }
                return true;
            });
        }
    }

    public void applySort(String column) {
        if (filteredData == null) return;
        Comparator<StudyGroupTableModel> comparator;
        if (column.equals("ID")) {
            comparator = Comparator.comparing(StudyGroupTableModel::getId);
        } else if (column.equals(lang.getString("table.name"))) {
            comparator = Comparator.comparing(StudyGroupTableModel::getName);
        } else if (column.equals(lang.getString("table.studentsCount"))) {
            comparator = Comparator.comparingLong(StudyGroupTableModel::getStudentsCount);
        } else {
            comparator = Comparator.comparing(StudyGroupTableModel::getId);
        }
        tableView.setItems(new SortedList<>(filteredData, comparator));
    }

    private void initColumnTexts() {
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