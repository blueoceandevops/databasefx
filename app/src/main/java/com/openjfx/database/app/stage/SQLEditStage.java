package com.openjfx.database.app.stage;

import com.openjfx.database.app.AbstractStage;
import com.openjfx.database.app.annotation.Layout;
import io.vertx.core.json.JsonObject;

/**
 * sql edit stage view
 *
 * @author yangkui
 * @since 1.0
 */
@Layout(layout = "sql_edit_view.fxml", title = "app.stage.editor")
public class SQLEditStage extends AbstractStage<JsonObject> {
    public SQLEditStage(JsonObject data) {
        super(data);
    }
}
