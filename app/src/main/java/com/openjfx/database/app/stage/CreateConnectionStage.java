package com.openjfx.database.app.stage;

import com.openjfx.database.app.AbstractStage;
import com.openjfx.database.app.annotation.Layout;

/**
 * create connection stage view
 *
 * @author yangkui
 * @since 1.0
 */
@Layout(layout = "create_connection_view.fxml", title = "app.stage.create.con", resizable = false)
public class CreateConnectionStage extends AbstractStage<String> {
    public CreateConnectionStage() {
    }

    public CreateConnectionStage(String uuid) {
        super(uuid);
    }
}
