package main;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle("Map Editor");
        window.setLayout(new BorderLayout());

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel, BorderLayout.CENTER);
        TilePanel tilePanel = new TilePanel(gamePanel);
        window.add(tilePanel, BorderLayout.WEST);

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.startGameThread();
    }
}