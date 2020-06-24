package com.openjfx.database.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;

/**
 * 系统相关操作工具类
 *
 * @author yangkui
 * @since 1.0
 */
public class OSUtils {
    /**
     * System OS type
     *
     * @author yangkui
     * @since 1.0
     */
    public enum OsType {
        /**
         * linux
         */
        LINUX("Linux"),
        /**
         * window
         */
        WINDOW("Window"),
        /**
         * mac
         */
        MAC("Mac");

        private final String osName;

        OsType(String osName) {
            this.osName = osName;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(OSUtils.class);

    /**
     * 获取操作系统名称
     *
     * @return 返回操作系统名称
     */
    public static String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * 获取用户主目录
     *
     * @return 返回主目录路径
     */
    public static String getUserHome() {
        return System.getProperty("user.home");
    }

    /**
     * call local progress open fix file
     *
     * @param path file path
     * @throws Exception {@inheritDoc}
     */
    public static void openFile(String path) throws Exception {
        var file = new File(path);
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            LOG.error("Open file fail", e);
            throw new RuntimeException(e);
        }
    }

}
