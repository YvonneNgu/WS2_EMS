package com.example.workshop2.cert.command;

import android.graphics.PointF;

import com.example.workshop2.cert.CertificateItemController;
import com.example.workshop2.cert.model.CertificateItem;

public class CertificateCommand implements Command {
    private final CertificateItemController controller;
    private final String key;
    private final CommandType type;
    private final Object oldValue;
    private final Object newValue;

    public enum CommandType {
        MOVE, TEXT, FONT_SIZE, FONT_STYLE, COLOR, BOLD, ITALIC, UNDERLINE, ALIGNMENT, DELETE, ADD
    }

    public CertificateCommand(CertificateItemController controller, String key, CommandType type,
                              Object oldValue, Object newValue) {
        this.controller = controller;
        this.key = key;
        this.type = type;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        switch (type) {
            case MOVE:
                controller.updateItemPosition(key, (PointF) newValue);
                break;
            case TEXT:
                controller.updateItemText(key, (String) newValue);
                break;
            case FONT_SIZE:
                controller.updateItemFontSize(key, (float) newValue);
                break;
            case FONT_STYLE:
                controller.updateItemFont(key, (String) newValue);
                break;
            case COLOR:
                controller.updateItemTextColor(key, (Integer) newValue);
                break;
            case BOLD:
                controller.updateItemBold(key, (Boolean) newValue);
                break;
            case ITALIC:
                controller.updateItemItalic(key, (Boolean) newValue);
                break;
            case UNDERLINE:
                controller.updateItemUnderline(key, (Boolean) newValue);
                break;
            case ALIGNMENT:
                controller.updateItemAlignment(key, (CertificateItem.Alignment) newValue);
                break;
            case DELETE:
                controller.deleteItem(key);
                break;
            case ADD:
                controller.addItem(key);
                break;
        }
    }

    @Override
    public void undo() {
        switch (type) {
            case MOVE:
                controller.updateItemPosition(key, (PointF) oldValue);
                break;
            case TEXT:
                controller.updateItemText(key, (String) oldValue);
                break;
            case FONT_SIZE:
                controller.updateItemFontSize(key, (float) oldValue);
                break;
            case FONT_STYLE:
                controller.updateItemFont(key, (String) oldValue);
                break;
            case COLOR:
                controller.updateItemTextColor(key, (Integer) oldValue);
                break;
            case BOLD:
                controller.updateItemBold(key, (Boolean) oldValue);
                break;
            case ITALIC:
                controller.updateItemItalic(key, (Boolean) oldValue);
                break;
            case UNDERLINE:
                controller.updateItemUnderline(key, (Boolean) oldValue);
                break;
            case ALIGNMENT:
                controller.updateItemAlignment(key, (CertificateItem.Alignment) oldValue);
                break;
            case DELETE:
                controller.addItem(key);
                break;
            case ADD:
                controller.deleteItem(key);
                break;
        }
    }
}