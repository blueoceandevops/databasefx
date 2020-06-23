package com.openjfx.database.app.stage;

import com.openjfx.database.app.BaseStage;
import com.openjfx.database.app.annotation.Layout;
import com.openjfx.database.app.model.EXModel;

/**
 * data export stage
 *
 * @author yangkui
 * @since 1.0
 */
@Layout(layout = "export_wizard_view.fxml", title = "app.stage.data.export")
public class EXStage extends BaseStage<EXModel> {
    public EXStage(EXModel data) {
        super(data);
    }
}

