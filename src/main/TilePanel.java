package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class TilePanel extends JPanel {

    final int originalTileSize = 32;
    final int scale = 2;
    final int columns = 6;

    final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 6;
    final int maxScreenRow = 15;
    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    private static TileSelectionListener tileSelectionListener;
    private Tile[] textures;
    private GamePanel gp;

    public TilePanel(GamePanel gp){
        this.gp = gp;
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.white);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.textures = textureHandler.getTextures();
        ArrayList<Tile> tmpTxtList = new ArrayList<Tile>();
        for (Tile tile : textures){
            if(tile == null) continue;
            if (!tile.combined) tmpTxtList.add(tile);
        }
        textures = new Tile[tmpTxtList.size()];
        textures = tmpTxtList.toArray(textures);

        // Create panel for the grid of textures
        JPanel texturesPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D)g;

                for (int i = 0; i < textures.length; i++) {
                    int x = (i % columns) * tileSize; // x-Position berechnen
                    int y = (i / columns) * tileSize; // y-Position berechnen
                    g.drawImage(textures[i].texture, x, y, tileSize, tileSize, null);
                }
            }
        };

        texturesPanel.setPreferredSize(new Dimension(screenWidth , ((textures.length / columns) + 4) * tileSize)); // Adjust height as needed

        texturesPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / tileSize;
                int y = e.getY() / tileSize;
                int index = y * columns + x;
                if (index >= 0 && index < textures.length) {
                    fireTileSelected(textures[index].id);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(texturesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Create panel for buttons
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS)); // Vertical arrangement
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("Editor"));
        buttonsPanel.setBackground(Color.GRAY); // Light gray background
        buttonsPanel.setAlignmentX(LEFT_ALIGNMENT); // Align components to the left

        JTextField textField = new JTextField(10);
        textField.setMaximumSize(new Dimension(screenWidth, 40)); // Limit width

        textField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                gp.world = textField.getText();
                gp.readChunks();
            }
        });

        buttonsPanel.add(textField);

        JButton button1 = new JButton("Change World");
        button1.setMaximumSize(new Dimension(screenWidth, 40));

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.world = textField.getText();
                gp.readChunks();

            }
        });
        buttonsPanel.add(button1);

        JToggleButton toggleButton = new JToggleButton("Combine");
        toggleButton.setMaximumSize(new Dimension(screenWidth, 40));

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Combine pressed");
                gp.combineActive = toggleButton.isSelected();
            }
        });

        buttonsPanel.add(toggleButton);

        JToggleButton floodButton = new JToggleButton("Flood Fill");
        floodButton.setMaximumSize(new Dimension(screenWidth, 40));

        floodButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.ffActive = floodButton.isSelected();
            }
        });

        buttonsPanel.add(floodButton);

        JButton button = new JButton("Export");
        button.setMaximumSize(new Dimension(screenWidth, 40));

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gp.export();

            }
        });
        buttonsPanel.add(button);


        this.add(scrollPane, BorderLayout.NORTH);
        this.add(buttonsPanel, BorderLayout.SOUTH);
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

    //public void paintComponent(Graphics g) {
    //    super.paintComponent(g);
    //    Graphics2D g2 = (Graphics2D)g;

    //    for (int i = 0; i < textures.length; i++) {
    //        int x = (i % columns) * tileSize; // x-Position berechnen
    //        int y = (i / columns) * tileSize; // y-Position berechnen
    //        g.drawImage(textures[i].texture, x, y, tileSize, tileSize, null);
    //    }
    //}
}

// interface for TileSelectionListener, calls GamePanel to update Tile ID
interface TileSelectionListener {
    void tileSelected(int tileId);
}
