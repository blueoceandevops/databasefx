package com.openjfx.database.app.stage;

import com.openjfx.database.app.AbstractStage;
import com.openjfx.database.app.annotation.Layout;
import com.openjfx.database.app.model.TableTransferModel;

/***
 *
 *Database table move / copy stage
 *
 * @author yangkui
 * @since 1.1
 */
@Layout(layout = "table_transfer_view.fxml", alwaysOnTop = true)
public class TableTransferStage extends AbstractStage<TableTransferModel> {
    public TableTransferStage(TableTransferModel intent) {
        super(intent);
    }
}
