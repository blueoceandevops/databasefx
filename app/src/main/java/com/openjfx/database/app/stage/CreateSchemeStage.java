package com.openjfx.database.app.stage;

import com.openjfx.database.app.AbstractStage;
import com.openjfx.database.app.annotation.Layout;

/**
 * New database stage
 *
 * @author yangkui
 * @since 1.0
 */
@Layout(layout = "create_scheme_view.fxml", resizable = false,title = "app.stage.create.database")
public class CreateSchemeStage extends AbstractStage<String> {
    public CreateSchemeStage(String data) {
        super(data);
    }
}
