package ui.screens;

import java.awt.*;
import java.awt.color.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import ui.FindTheDUI;
import ui.components.GradientPanel;
// choosing whether youre a landlord or tenant page
public class WelcomePanel extends GradientPanel {
    public WelcomePanel() {
        setLayout(new GridLayout(1, 2));

        JPanel leftPanel = FindTheDUI.createLeftPanel();

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.insets = new Insets(10, 0, 10, 0);
        gbcRight.fill = GridBagConstraints.HORIZONTAL;

        JButton landlordButton = new JButton("I'm a Landlord");
        landlordButton.setPreferredSize(new Dimension(300, 45));
        landlordButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        landlordButton.setBackground(new Color(0x73, 0x73, 0x73));
        landlordButton.setForeground(Color.WHITE);
        landlordButton.setFocusPainted(false);
        landlordButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightPanel.add(landlordButton, gbcRight);

        gbcRight.gridy = 1;
        JButton tenantButton = new JButton("I'm a Tenant");
        tenantButton.setPreferredSize(new Dimension(300, 45));
        tenantButton.setFont(new Font("Georgia", Font.PLAIN, 16));
        tenantButton.setBackground(Color.WHITE);
        tenantButton.setForeground(Color.WHITE);
        tenantButton.setFocusPainted(false);
        tenantButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xD0, 0xD0, 0xD0), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));
        rightPanel.add(tenantButton, gbcRight);

        landlordButton.addActionListener(e -> FindTheDUI.showLogin("Landlord"));
        tenantButton.addActionListener(e -> FindTheDUI.showLogin("Tenant"));

        add(leftPanel);
        add(rightPanel);
    }
}
