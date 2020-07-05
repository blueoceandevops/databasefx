package com.openjfx.database.app.stage;

import com.openjfx.database.app.AbstractStage;
import com.openjfx.database.app.annotation.Layout;
import javafx.stage.StageStyle;


/**
 * App splash stage view
 *
 * @author yangkui
 * @since 1.0
 */
@Layout(layout = "splash_view.fxml", width = 600, height = 400, stageStyle = StageStyle.UNDECORATED, alwaysOnTop = true)
public class SplashStage extends AbstractStage<Void> {
}
