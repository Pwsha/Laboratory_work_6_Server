package org.example.client.gui;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private Locale currentLocale;
    private final Map<String, Locale> availableLocales;
    private Consumer<Locale> localeChangeListener;

    private LanguageManager() {
        availableLocales = new ConcurrentHashMap<>();
        availableLocales.put("ru", new Locale("ru"));
        availableLocales.put("nl", new Locale("nl"));
        availableLocales.put("sv", new Locale("sv"));
        availableLocales.put("en_AU", Locale.forLanguageTag("en-AU"));
        currentLocale = availableLocales.get("ru");
        loadBundle();
    }

    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    public void addLocaleChangeListener(Consumer<Locale> listener) {
        this.localeChangeListener = listener;
    }

    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        loadBundle();
        if (localeChangeListener != null) {
            localeChangeListener.accept(locale);
        }
    }

    public void setLocale(String language) {
        Locale locale = availableLocales.get(language);
        if (locale != null) {
            setLocale(locale);
        }
    }

    private void loadBundle() {
        try {
            String bundleName = "i18n/messages_" + currentLocale.toString();
            URL url = getClass().getClassLoader().getResource(bundleName + ".properties");
            if (url == null) {
                url = getClass().getClassLoader().getResource("i18n/messages.properties");
            }
            if (url != null) {
                URLConnection conn = url.openConnection();
                conn.setUseCaches(false);
                try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                    bundle = new PropertyResourceBundle(reader);
                    System.out.println("DEBUG: Loaded bundle for locale: " + currentLocale);
                }
            } else {
                bundle = ResourceBundle.getBundle("i18n/messages", currentLocale);
            }
        } catch (Exception e) {
            System.err.println("Error loading bundle: " + e.getMessage());
            bundle = ResourceBundle.getBundle("i18n/messages", currentLocale);
        }
    }

    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            System.err.println("Missing key: " + key);
            return key;
        }
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }
}