package me.ihaq.winbin;

import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.ihaq.winbin.file.CustomFile;
import me.ihaq.winbin.file.files.ConfigFile;
import org.apache.http.HttpStatus;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static me.ihaq.winbin.util.CommonUtils.*;

public enum WinBin {
    INSTANCE;

    public final String NAME = "WinBin";

    private CustomFile configFile;
    public String pasteBinKey;
    public TrayIcon trayIcon;

    public static void main(String[] args) {
        INSTANCE.start();
    }

    public void start() {

        //checking for support
        if (!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(null, "SystemTray is not supported on your platform, you will not be able to use this program.", NAME, JOptionPane.ERROR_MESSAGE);
            shutdown(0);
        }

        registerKeyListener();
        createConfig();
        createPopupMenu();

        showNotification(NAME + " started.");
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
                        makeNewPaste(new Callback<String>() {

                            @Override
                            public void completed(HttpResponse<String> httpResponse) {

                                if (httpResponse.getStatus() == HttpStatus.SC_OK && httpResponse.getBody().contains("pastebin.com")) {

                                    String link = httpResponse.getBody();

                                    try {
                                        Desktop.getDesktop().browse(new URL(link).toURI());
                                    } catch (IOException | URISyntaxException e1) {
                                        e1.printStackTrace();
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(new StringSelection(link), null);
                                        showNotification("Could not open web page so we copied the URL.");
                                    }

                                } else {
                                    showNotification(httpResponse.getBody());
                                }
                            }

                            @Override
                            public void failed(UnirestException e) {
                                showNotification("Could not connect to Pastebin.");
                            }

                            @Override
                            public void cancelled() {

                            }

                        }, (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
                    } catch (UnsupportedFlavorException | IOException e1) {
                        e1.printStackTrace();
                        showNotification("An error occurred.");
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                setKeyStatus(e.getKeyCode(), 0);
            }

            private void setKeyStatus(int key, int status) {
                switch (key) {
                    case NativeKeyEvent.VC_CONTROL: {
                        keys[0] = status;
                        break;
                    }
                    case NativeKeyEvent.VC_SHIFT: {
                        keys[1] = status;
                        break;
                    }
                    case NativeKeyEvent.VC_V: {
                        keys[2] = status;
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent e) {
            }
        });
    }

    // loading data from the config file
    private void createConfig() {

        File directory = new File(System.getenv("APPDATA") + "/" + NAME);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        configFile = new ConfigFile(new GsonBuilder().setPrettyPrinting().create(), new File(directory, "config.json"));

        try {
            configFile.loadFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createPopupMenu() {

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

        Image image = new ImageIcon(getClass().getResource("/pastebin.png")).getImage();
        trayIcon = new TrayIcon(image, NAME, trayPopupMenu);
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException awtException) {
            awtException.printStackTrace();
            showNotification("An error occurred, please restart the program.");
            shutdown(1);
        }
    }


}