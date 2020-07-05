package com.openjfx.database.app;

import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * base controller
 *
 * @param <T> Transfer data type
 * @author yangkui
 * @since 1.0
 */
public abstract class AbstractController<T> implements Initializable {
    /**
     * extension data
     */
    protected T intent;
    /**
     * stage reference
     */
    protected Stage stage;
    /**
     * URL
     */
    protected URL location;
    /**
     * logger
     */
    protected final Logger logger;
    /**
     * ResourceBundle
     */
    protected ResourceBundle resourceBundle;

    /**
     * empty construction
     */
    public AbstractController() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * Called when initializing the fxml view
     * {@inheritDoc}
     *
     * @param location  location
     * @param resources resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        this.location = location;
    }

    /**
     * Init controller
     */
    public void init() {
    }

    /**
     * Current stage close call method
     */
    public void close() {
    }

    public T getIntent() {
        return intent;
    }

    public void setIntent(T intent) {
        this.intent = intent;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
