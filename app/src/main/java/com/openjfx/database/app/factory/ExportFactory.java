package com.openjfx.database.app.factory;

import com.openjfx.database.app.DatabaseFX;
import com.openjfx.database.app.component.paginations.ExportWizardFormatPage;
import com.openjfx.database.app.component.paginations.ExportWizardSelectColumnPage;
import com.openjfx.database.app.model.ExportWizardModel;
import com.openjfx.database.base.AbstractDataBasePool;
import com.openjfx.database.common.VertexUtils;
import com.openjfx.database.common.utils.StringUtils;
import com.openjfx.database.model.TableColumnMeta;
import io.vertx.core.CompositeFuture;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.openjfx.database.app.DatabaseFX.DATABASE_SOURCE;

/**
 * Factory class is used for data export
 *
 * @author yangkui
 * @since 1.0
 */
public class ExportFactory {
    /**
     * Export configuration information
     */
    private final ExportWizardModel model;
    /**
     * Export progress
     */
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    /**
     * Export progress description
     */
    private final StringProperty text = new SimpleStringProperty("");

    private ExportFactory(ExportWizardModel model) {
        this.model = model;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExportFactory.class);

    /**
     * start export task
     */
    public void start() {
        setText("Pre export condition check in progress.....");
        var a = model.getSelectColumnPattern() == ExportWizardSelectColumnPage.SelectColumnPattern.NORMAL
                && model.getSelectTableColumn().isEmpty();
        var b = model.getSelectColumnPattern() == ExportWizardSelectColumnPage.SelectColumnPattern.SENIOR
                && StringUtils.isEmpty(model.getCustomExportSql());
        if (a) {
            setText("常规模式至少选择一列!");
            return;
        }
        if (b) {
            setText("高级模式下SQL语句不能为空!");
            return;
        }
        var sql = buildSql();
        setProgress(0.1);
        var pool = DATABASE_SOURCE.getDataBaseSource(model.getUuid());
        var future = pool.execute(sql);
        setText("Start reading data.......");
        future.onSuccess(rows -> {
            var map = new LinkedHashMap<String, List<String>>();
            int size = rows.columnsNames().size();
            int rowSize = rows.size();
            int count = 0;
            for (Row row : rows) {
                for (int i = 0; i < size; i++) {
                    var val = StringUtils.getObjectStrElseGet(row.getValue(i), "", "yyyy-MM-dd HH:mm:ss");
                    var columnName = row.getColumnName(i);
                    final List<String> list;
                    if (map.containsKey(columnName)) {
                        list = map.get(columnName);

                    } else {
                        list = new ArrayList<>();
                        map.put(columnName, list);
                    }
                    list.add(val);
                    setProgress((1f - progress.getValue()) * (10f * count + i) / (rowSize * size));
                }
                count++;
            }
            setText("Read data successfully.......");
            formatFilePath();
            setText("Start writing data.......");
            //execute export
            switch (model.getExportDataType()) {
                case JSON -> exportAsJson(map);
                case EXCEL -> exportAsExcel(map);
                case EXCEL_PRIOR -> exportAsSeniorExcel(map);
                case HTML -> exportAsHtml(map);
                case XML -> exportAsXml(map);
                case CSV -> exportAsCsv(map);
                case SQL_SCRIPT -> exportAsSql(map);
                default -> exportAsTxt(map);
            }
        });
        //execute sql fail
        future.onFailure(t -> {
            logger.error("export data failed", t);
            setText("Failed to read data:" + t.getMessage());
        });
    }

    /**
     * Export table as json
     *
     * @param map table data
     */
    private void exportAsJson(Map<String, List<String>> map) {
        var json = new JsonObject();
        var array = new JsonArray();
        json.put("RECORDS", array);
        var list = getValueList(map.values());
        if (map.size() > 0) {
            var rowSize = list.size() / map.size();
            var keys = map.keySet().toArray(new String[0]);
            for (int j = 0; j < rowSize; j++) {
                var k = 0;
                var record = new JsonObject();
                while (k < map.size()) {
                    record.put(keys[k], list.get(j + k * rowSize));
                    k++;
                }
                array.add(record);
            }
        }
        writerFile(json.toBuffer().getBytes());
    }

    /**
     * Export table as csv
     *
     * @param map table data
     */
    private void exportAsCsv(LinkedHashMap<String, List<String>> map) {
        String fileName = model.getPath();
        File csvFile;
        BufferedWriter csvWtriter = null;
        Throwable throwable = null;
        try {
            csvFile = new File(fileName);
            if (!csvFile.exists()) {
                if (!csvFile.createNewFile()) {
                    throw new Exception("fail to create csv file");
                }
            }
            csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    csvFile), StandardCharsets.UTF_8), 1024);
            List<String> headers = new ArrayList<>(map.keySet());
            // write header
            writeRow(headers, csvWtriter, ",");
            // write items
            final List<List<String>> dataCollect = new ArrayList<>(map.values());
            int rowCount = dataCollect.stream().mapToInt(List::size).min().orElse(0);
            List<String> rowData;
            for (int i = 0; i < rowCount; i++) {
                int finalIndex = i;
                rowData = dataCollect.stream().map(t -> t.get(finalIndex)).collect(Collectors.toList());
                writeRow(rowData, csvWtriter, ",");
            }
            csvWtriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throwable = e;
        } finally {
            try {
                assert csvWtriter != null;
                csvWtriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writerResult(throwable);
    }

    /**
     * write one row data to file
     *
     * @param row       row data
     * @param csvWriter BufferedWriter object
     * @param rule      design the rule to append data
     * @throws IOException io exception
     */
    private void writeRow(List<String> row, BufferedWriter csvWriter, String rule) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String item : row) {
            sb.append("\"").append(item).append("\"").append(rule);
        }
        sb.deleteCharAt(sb.length() - 1);
        csvWriter.write(sb.toString());
        csvWriter.newLine();
    }

    /**
     * Export table as excel(version after 2007)
     *
     * @param map table data
     */
    private void exportAsSeniorExcel(Map<String, List<String>> map) {
        var workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet(model.getTable());
        updateSheet(map, sheet);
        //writer file
        Throwable throwable = null;
        try {
            var file = new File(model.getPath());
            workbook.write(new FileOutputStream(file));
        } catch (IOException e) {
            throwable = e;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writerResult(throwable);
    }

    /**
     * Export table as excel(version before 2007)
     *
     * @param map table data
     */
    private void exportAsExcel(Map<String, List<String>> map) {
        var workbook = new HSSFWorkbook();
        var sheet = workbook.createSheet(model.getTable());
        updateSheet(map, sheet);
        //writer file
        Throwable throwable = null;
        try {
            var file = new File(model.getPath());
            workbook.write(file);
        } catch (IOException e) {
            throwable = e;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writerResult(throwable);
    }

    /**
     * Encapsulation is used to unify versions before and after 07
     *
     * @param map   table data
     * @param sheet 07 after and before sheet
     */
    private void updateSheet(Map<String, List<String>> map, Sheet sheet) {
        var titleRow = sheet.createRow(0);
        var i = 0;
        //create title row
        for (String s : map.keySet()) {
            var cell = titleRow.createCell(i, CellType.STRING);
            cell.setCellValue(s);
            i++;
        }
        var list = getValueList(map.values());
        if (map.size() > 0) {
            var rowSize = list.size() / map.size();
            for (int j = 0; j < rowSize; j++) {
                var row = sheet.createRow(j + 1);
                var k = 0;
                while (k < map.size()) {
                    var cell = row.createCell(k, CellType.STRING);
                    cell.setCellValue(list.get(j + k * rowSize));
                    k++;
                }
            }
        }
    }

    /**
     * Export table data as html
     *
     * @param map table data
     */
    private void exportAsHtml(Map<String, List<String>> map) {
        var templatePath = "assets/html/export_html_template.html";
        VertexUtils.getFileSystem().readFile(templatePath, ar -> {
            if (ar.failed()) {
                var str = "html template file not found";
                setText(str);
                logger.error(str, ar.cause());
                return;
            }
            var title = "{{TABLE_TITLE}}";
            var content = "{{TABLE_CONTENT}}";
            var text = ar.result().toString().replace(title, model.getTable());
            var table = new StringBuilder();
            table.append("<tr>");
            for (String s : map.keySet()) {
                table.append("<th>").append(s).append("</th>");
            }
            table.append("</tr>");
            var list = getValueList(map.values());
            if (map.size() > 0) {
                var rowSize = list.size() / map.size();
                for (int j = 0; j < rowSize; j++) {
                    table.append("<tr>");
                    var k = 0;
                    while (k < map.size()) {
                        var td = list.get(j + k * rowSize);
                        table.append("<td>").append(td).append("</td>");
                        k++;
                    }
                    table.append("</tr>");
                }
            }
            text = text.replace(content, table.toString());
            writerFile(text.getBytes());
        });
    }

    /**
     * Export table data as XML
     *
     * @param map table data
     */
    private void exportAsXml(Map<String, List<String>> map) {
        var dom = DocumentHelper.createDocument();
        var root = dom.addElement("RECORDS");
        var list = getValueList(map.values());
        var rowSize = list.size() / map.size();
        var keys = map.keySet().toArray(new String[0]);
        for (int i = 0; i < rowSize; i++) {
            var el = root.addElement("RECORD");
            var k = 0;
            while (k < map.size()) {
                var key = keys[k];
                var ell = el.addElement(key);
                ell.setText(list.get(i + k * rowSize));
                k++;
            }
        }
        //Write XML file asynchronously
        var format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        var file = new File(model.getPath());
        CompletableFuture.runAsync(() -> {
            XMLWriter writer = null;
            Throwable throwable = null;
            try {
                writer = new XMLWriter(new FileOutputStream(file), format);
                writer.setEscapeText(false);
                writer.write(dom);
            } catch (Exception e) {
                e.printStackTrace();
                throwable = e;
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.writerResult(throwable);
        });
    }

    /**
     * Export table data as TXT
     *
     * @param map table data
     */
    private void exportAsTxt(Map<String, List<String>> map) {
        String fileName = model.getPath();
        File txtFile;
        BufferedWriter csvWtriter = null;
        Throwable throwable = null;
        try {
            txtFile = new File(fileName);
            if (!txtFile.exists()) {
                boolean writable = txtFile.setWritable(true);
                if (!txtFile.createNewFile() && !writable) {
                    var str = "Fail to create txt file";
                    logger.info(str);
                    throw new Exception(str);
                }
            }
            csvWtriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    txtFile), StandardCharsets.UTF_8), 1024);
            List<String> headers = new ArrayList<>(map.keySet());
            // write header
            writeRow(headers, csvWtriter, "\t\t");
            // write items
            final List<List<String>> dataCollect = new ArrayList<>(map.values());
            int rowCount = dataCollect.stream().mapToInt(List::size).min().orElse(0);
            List<String> rowData;
            for (int i = 0; i < rowCount; i++) {
                int finalIndex = i;
                rowData = dataCollect.stream().map(t -> t.get(finalIndex)).collect(Collectors.toList());
                writeRow(rowData, csvWtriter, "\t\t");
            }
            csvWtriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throwable = e;
        } finally {
            try {
                assert csvWtriter != null;
                csvWtriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writerResult(throwable);
    }

    /**
     * export data as sql
     */
    private void exportAsSql(Map<String, List<String>> map) {
        var generate = DATABASE_SOURCE.getGenerator();
        var columns = map.keySet().toArray(new String[0]);
        var list = getValueList(map.values());
        var sql = generate.insert(columns, model.getScheme(), model.getTable(), list);
        writerFile(sql.getBytes());
    }

    private String buildSql() {
        final String sql;
        if (model.getSelectColumnPattern() == ExportWizardSelectColumnPage.SelectColumnPattern.SENIOR) {
            sql = model.getCustomExportSql();
        } else {
            var generator = DATABASE_SOURCE.getGenerator();
            sql = generator.select(model.getSelectTableColumn(), model.getScheme(), model.getTable());
        }
        return sql;
    }

    private void writerFile(byte[] bytes) {
        var path = model.getPath();
        var future = VertexUtils.writerFile(path, bytes);
        future.onComplete(ar -> writerResult(ar.cause()));

    }

    private void writerResult(Throwable throwable) {
        final String str;
        if (throwable == null) {
            str = "File success save!";
            setProgress(1);
        } else {
            str = "Failed to generate file";
            logger.error(str, throwable);
        }
        setText(str);
    }

    /**
     * <p>Because the file selector of Linux platform does not automatically
     * add the file extension to the path after selecting the file save path,
     * this function is added to deal with the problem of no suffix in the
     * exported file of Linux platform
     * </p>
     */
    private void formatFilePath() {
        var array = model.getPath().split(File.separator);
        if (array.length == 0) {
            return;
        }
        var lastKey = array[array.length - 1];
        var suffix = model.getExportDataType().getSuffix();
        if (!lastKey.matches("(.)*\\." + suffix)) {
            var path = model.getPath() + "." + suffix;
            model.setPath(path);
        }
    }

    private List<String> getValueList(Collection<List<String>> values) {
        var list = new ArrayList<String>();
        for (List<String> value : values) {
            list.addAll(value);
        }
        return list;
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public ExportWizardModel getModel() {
        return model;
    }

    public static ExportFactory factory(ExportWizardModel model) {
        return new ExportFactory(model);
    }
}
