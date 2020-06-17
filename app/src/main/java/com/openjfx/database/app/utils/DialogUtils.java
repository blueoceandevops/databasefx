package com.openjfx.database.app.utils;

import com.openjfx.database.app.enums.NotificationType;
import com.sun.javafx.scene.control.InputField;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.ExceptionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

import static com.openjfx.database.app.DatabaseFX.I18N;

/**
 * application dialog utils
 *
 * @author yangkui
 * @since 1.0
 */
public class DialogUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DialogUtils.class);

    /**
     * show error dialog
     *
     * @param title     header title of dialog
     * @param throwable error info
     */
    public static void showErrorDialog(Throwable throwable, String title) {
        LOG.error(throwable.getMessage(), throwable);
        Platform.runLater(() -> {
            var exceptionDialog = new ExceptionDialog(throwable);
            var pane = exceptionDialog.getDialogPane();
            pane.getStylesheets().add("css/base.css");
            exceptionDialog.setHeaderText(title);
            exceptionDialog.setTitle("Exception detail");
            exceptionDialog.show();
        });
    }

    /**
     * show notification
     *
     * @param text notification content
     * @param pos  {@link Pos} notification show position
     * @param type {@link NotificationType} notification type
     */
    public static void showNotification(String text, Pos pos, NotificationType type) {
        showNotification(text, pos, type, null);
    }

    /**
     * show notification
     *
     * @param text   notification content
     * @param pos    {@link Pos} notification show position
     * @param window target window
     * @param type   {@link NotificationType} notification type
     */
    public static void showNotification(String text, Pos pos, NotificationType type, Window window) {
        var notifications = Notifications.create();
        notifications.position(pos);
        notifications.text(text);
        if (window != null) {
            notifications.owner(window);
        }
        Platform.runLater(() -> {
            switch (type) {
                case ERROR -> notifications.showError();
                case WARNING -> notifications.showWarning();
                case INFORMATION -> notifications.showInformation();
                case CONFIRMATION -> notifications.showConfirm();
                default -> notifications.show();
            }
        });
    }

    /**
     * show confirm message
     *
     * @param message message content
     * @return <p>return the confirmation result.If you click OK,
     * it will return true. Otherwise, it will return false<p/>
     */
    public static boolean showAlertConfirm(String message) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("css/base.css");
        var optional = alert.showAndWait();
        return optional.isPresent() && optional.get() == ButtonType.OK;
    }

    /**
     * Display the input dialog box and return the input results
     *
     * @param title dialog title
     * @return inout title
     */
    public static String showInputDialog(String title) {
        var dialog = new TextInputDialog();
        dialog.setTitle(I18N.getString("app.dialog.input"));
        dialog.setHeaderText(title);
        dialog.getDialogPane().getStylesheets().add("css/base.css");
        var optional = dialog.showAndWait();
        return optional.orElse("");
    }

    /**
     * show dialog message
     *
     * @param message message content
     */
    public static void showAlertInfo(String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("消息");
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add("css/base.css");
        alert.showAndWait();
    }
}
