package com.trackify.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

final class TrackifyTheme {
    static final Color CANVAS = new Color(242, 247, 244);
    static final Color SURFACE = Color.WHITE;
    static final Color INK = new Color(28, 47, 44);
    static final Color MUTED = new Color(103, 121, 116);
    static final Color PRIMARY = new Color(22, 107, 96);
    static final Color PRIMARY_DARK = new Color(16, 83, 75);
    static final Color GREEN = new Color(39, 132, 91);
    static final Color RED = new Color(186, 69, 66);
    static final Color BORDER = new Color(218, 229, 223);
    static final Color SELECTION = new Color(218, 239, 231);
    static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 13);

    private TrackifyTheme() {
    }

    static JButton button(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(LABEL_FONT);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        return button;
    }

    static void styleInput(JComponent component) {
        component.setFont(BODY_FONT);
        component.setForeground(INK);
        component.setBackground(SURFACE);
        if (component instanceof JComboBox<?>)
            component.setBorder(new LineBorder(BORDER));
        else
            component.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
    }

    static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", style, size));
        label.setForeground(color);
        return label;
    }

    static JPanel surfacePanel(LayoutManager layout, Insets padding) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        panel.setBorder(new CompoundBorder(new LineBorder(BORDER), new EmptyBorder(padding)));
        return panel;
    }

    static JPanel sectionPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(SURFACE);
        Border titledBorder = new TitledBorder(new LineBorder(BORDER), title,
                TitledBorder.LEADING, TitledBorder.TOP, LABEL_FONT, MUTED);
        panel.setBorder(new CompoundBorder(titledBorder, new EmptyBorder(8, 12, 12, 12)));
        return panel;
    }
}