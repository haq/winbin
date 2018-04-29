package me.ihaq.winbin;

import com.google.gson.GsonBuilder;
import me.ihaq.winbin.file.CustomFile;
import me.ihaq.winbin.file.files.ConfigFile;
import me.ihaq.winbin.util.WebUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum WinBin {
    INSTANCE;

    private final Image image = new ImageIcon(getClass().getResource("/pastebin.png")).getImage();
    private final File directory = new File(System.getenv("APPDATA") + "/" + getClass().getSimpleName());
    private final CustomFile configFile = new ConfigFile(new GsonBuilder().setPrettyPrinting().create(), new File(directory, "config.json"));

    // used for running async tasks
    public final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public String pasteBinKey = "";

    public void create() {
        registerKeyListener();
        createConfig();
        createPopupMenu();
    }

    private void registerKeyListener() {

        // setting GlobalScreen logger level to warning.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
            shutdown(1);
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {

            // CTRL + SHIFT + V
            private int[] keys = {0, 0, 0};

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                setKeyStatus(e.getKeyCode(), 1);

                // checking if the proper key combo is pressed
                if (keys[0] == 1 && keys[1] == 1 && keys[2] == 1) {
                    try {
                        WebUtils.makeNewPaste(response -> {
                            if (response.contains("pastebin.com")) {
                                try {
                                    Desktop.getDesktop().browse(new URL(response).toURI());
                                } catch (IOException | URISyntaxException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }, (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
                    } catch (UnsupportedFlavorException | IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                setKeyStatus(e.getKeyCode(), 0);
            }

            private void setKeyStatus(int key, int status) {
                if (key == NativeKeyEvent.VC_CONTROL)
                    keys[0] = status;

                if (key == NativeKeyEvent.VC_SHIFT)
                    keys[1] = status;

                if (key == NativeKeyEvent.VC_V)
                    keys[2] = status;
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
            }
        });
    }

    // loading data from the config file
    private void createConfig() {

        if (!directory.exists())
            directory.mkdirs();

        configFile.makeDirectory();

        try {
            configFile.loadFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPopupMenu() {
        //checking for support
        if (!SystemTray.isSupported())
            return;

        // creating PopupMenu object
        PopupMenu trayPopupMenu = new PopupMenu();

        // exit button
        MenuItem close = new MenuItem("Exit");
        close.addActionListener(e -> {
            try {
                configFile.saveFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            shutdown(0);
        });
        trayPopupMenu.add(close);

        TrayIcon trayIcon = new TrayIcon(image, getClass().getSimpleName(), trayPopupMenu);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException awtException) {
            awtException.printStackTrace();
        }
    }

    private void shutdown(int code) {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        System.exit(code);
    }

    public static void main(String[] args) {
        WinBin.INSTANCE.create();
    }
}