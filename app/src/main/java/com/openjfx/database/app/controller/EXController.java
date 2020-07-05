package com.openjfx.database.app.controller;

import com.openjfx.database.app.AbstractController;
import com.openjfx.database.app.component.paginations.EXConvertPage;
import com.openjfx.database.app.component.paginations.EXFormatPage;
import com.openjfx.database.app.component.paginations.EXInfoPage;
import com.openjfx.database.app.component.paginations.EXColumnPage;
import com.openjfx.database.app.enums.NotificationType;
import com.openjfx.database.app.factory.ExportFactory;
import com.openjfx.database.app.model.EXModel;
import com.openjfx.database.app.utils.DialogUtils;
import com.openjfx.database.common.utils.OSUtils;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.stage.FileChooser;
import javafx.util.Callback;

/**
 * Export Wizard controller
 *
 * @author yangkui
 * @since 1.0
 */
public class EXController extends AbstractController<EXModel> {
    @FXML
    private Label wizardTitle;
    @FXML
    private Button startOrCancel;
    @FXML
    private Pagination pagination;

    private static final String[] TITLE = {
            "向导可以让你指定导出数据的细节。你要使用哪一种导出格式?(1/4)",
            "你可以选择具体需要导出哪些列。(2/4)",
            "你可以对数据进行一些个性化设置。(3/4)",
            "我们已收集向导导出数据所需要的全部信息。点击[开始]按钮开始导出。(3/4)"
    };

    private EXInfoPage infoPage;
    private EXFormatPage formatPage;
    private EXConvertPage convertPage;
    private EXColumnPage selectColumnPage;


    @Override
    public void init() {
        infoPage = new EXInfoPage(intent);
        formatPage = new EXFormatPage(intent);
        convertPage = new EXConvertPage(intent);
        selectColumnPage = new EXColumnPage(intent);
        pagination.setPageFactory(pageFactory());
    }

    public Callback<Integer, Node> pageFactory() {
        return (index) -> {
            final Node node;
            if (index == 0) {
                node = formatPage;
            } else if (index == 1) {
                node = selectColumnPage;
            } else if (index == 2) {
                node = convertPage;
            } else {
                node = infoPage;
            }
            wizardTitle.setText(TITLE[index]);
            return node;
        };
    }

    @FXML
    public void next(ActionEvent event) {
        var index = pagination.getCurrentPageIndex();
        if (index < pagination.getPageCount() - 1) {
            pagination.setCurrentPageIndex(++index);
        }
        if (index == pagination.getPageCount() - 1) {
            startOrCancel.setText("开始");
        }

    }

    @FXML
    public void last(ActionEvent event) {
        var index = pagination.getCurrentPageIndex();
        if (index > 0) {
            pagination.setCurrentPageIndex(--index);
        }
        if (index < pagination.getPageCount() - 1) {
            startOrCancel.setText("取消");
        }
    }

    @FXML
    public void completeOrCancel(ActionEvent event) {
        var index = pagination.getCurrentPageIndex();
        if (index == pagination.getPageCount() - 1) {
            infoPage.reset();
            if (intent.getPath() == null) {
                var file = openFileSelector();
                intent.setPath(file.getAbsolutePath());
            }
            var factory = ExportFactory.factory(intent);
            factory.textProperty().addListener((observable, oldValue, newValue) -> {
                infoPage.appendStr(newValue);
            });
            factory.progressProperty().addListener((observable, oldValue, newValue) -> {
                infoPage.updateProgressValue(newValue.doubleValue());
                if (newValue.doubleValue() < 1d) {
                    return;
                }
                try {
                    OSUtils.openFile(intent.getPath());
                    Platform.runLater((stage::close));
                } catch (Exception e) {
                    DialogUtils.showNotification("打开文件失败", Pos.TOP_CENTER, NotificationType.ERROR, stage);
                }
            });
            factory.start();
        } else {
            stage.close();
        }
    }

    private File openFileSelector() {
        var fileChooser = new FileChooser();
        var initPath = OSUtils.getUserHome();
        var suffix = intent.getExportDataType().getSuffix();
        var filter = new FileChooser.ExtensionFilter(String.format("%s File", suffix.toUpperCase()), String.format("*.%s", suffix));
        fileChooser.setTitle("请选择保存路径");
        fileChooser.setInitialDirectory(new File(initPath));
        fileChooser.getExtensionFilters().add(filter);
        return fileChooser.showSaveDialog(getStage());
    }
}
