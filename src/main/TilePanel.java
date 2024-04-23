package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class TilePanel extends JPanel {

    final int originalTileSize = 32;
    final int scale = 2;
    final int columns = 4;

    final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 5;
    final int maxScreenRow = 15;
    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    private static TileSelectionListener tileSelectionListener;
    private BufferedImage[] textures;
    private GamePanel gp;

    public TilePanel(GamePanel gp){
        this.gp = gp;
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.textures = textureHandler.getTextures();

        this.add(Box.createVerticalGlue());

        JToggleButton toggleButton = new JToggleButton("Combine");
        toggleButton.setPreferredSize(new Dimension(100, 40));

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Combine pressed");
                gp.combineActive = toggleButton.isSelected();
            }
        });

        this.add(toggleButton);

        JToggleButton floodButton = new JToggleButton("Flood Fill");
        floodButton.setPreferredSize(new Dimension(100, 40));

        floodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.ffActive = floodButton.isSelected();
            }
        });

        this.add(floodButton);

        JButton button = new JButton("Export");
        button.setPreferredSize(new Dimension(100, 40));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.export();

            }
        });
        this.add(button);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / tileSize;
                int y = e.getY() / tileSize;
                int index = y * columns + x;
                if (index >= 0 && index < textures.length) {
                    fireTileSelected(index);
                }
            }
        });
    }
    // method to set TileSelectionListener
    public static void setTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListener = listener;
    }

    // method to fire tileSelected event
    private void fireTileSelected(int tileId) {
        if (tileSelectionListener != null) {
            tileSelectionListener.tileSelected(tileId);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        for (int i = 0; i < textures.length; i++) {
            int x = (i % columns) * tileSize; // x-Position berechnen
            int y = (i / columns) * tileSize; // y-Position berechnen
            g.drawImage(textures[i], x, y, tileSize, tileSize, null);
        }
    }
}

// interface for TileSelectionListener, calls GamePanel to update Tile ID
interface TileSelectionListener {
    void tileSelected(int tileId);
}
