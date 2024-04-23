package main;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class GamePanel extends JPanel implements Runnable{

    final int originalTileSize = 32;
    final int scale = 2;
    final String path = "./src/textures/";

    final int tileSize = originalTileSize * scale;
    final int maxScreenCol = 25;
    final int maxScreenRow = 15;
    final int screenWidth = tileSize * maxScreenCol;
    final int screenHeight = tileSize * maxScreenRow;

    private BufferedImage[] textures;

    final private int sizeX = 16 * 2;
    final private int sizeY = 16 * 2;

    int[][] map = new int[sizeY][sizeX]; // dimensions should be divisible by 16!
    private int camOffsetX;
    private int camOffsetY;
    private int startX;
    private int startY;
    private boolean isDragging;

    int FPS = 60;

    Thread gameThread;
    private int mouseBtn;

    int cursorX = 100;
    int cursorY = 100;
    int lastCursorX = 100;
    int lastCursorY = 100;
    int selectedTileId;

    public boolean ffActive = false;
    public boolean combineActive = false;

    public GamePanel(){
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        this.requestFocusInWindow();
        textures = textureHandler.getTextures();
        for (int[] row : map){
            Arrays.fill(row, -1);
        }

        readChunks();

        TileSelectionListener listener = new TileSelectionListener() {
            @Override
            public void tileSelected(int tileId) {
                selectedTileId = tileId;
                System.out.println(tileId);
                repaint();
            }
        };

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println(e.getButton());
                mouseBtn = e.getButton();
                if (e.getButton() == MouseEvent.BUTTON2) {
                    isDragging = true;
                    startX = e.getX();
                    startY = e.getY();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                mouseBtn = 0;
                if (e.getButton() == MouseEvent.BUTTON2) {
                    isDragging = false;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    int dx = e.getX() - startX;
                    int dy = e.getY() - startY;

                    camOffsetX += dx;
                    camOffsetY += dy;

                    startX = e.getX();
                    startY = e.getY();
                }
            }
        });

        TilePanel.setTileSelectionListener(listener);
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run(){

        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null){

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if(delta >= 1){
                // update: update game state
                update();

                // draw: draw game to screen
                repaint();
                delta--;
                drawCount++;
            }

            if(timer >= 1000000000){
                System.out.printf("FPS: %d\n", drawCount);
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update(){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        Point pos = this.getLocationOnScreen();

        int mouseX = mouse.x - pos.x - camOffsetX;
        int mouseY = mouse.y - pos.y - camOffsetY;

        cursorX = (int) (Math.floor((double) mouseX / 64));
        cursorY = (int) (Math.floor((double) mouseY / 64));

        //System.out.printf("x: %d y: %d\n", cursorX, cursorY);
        if (cursorX >= 0 && cursorX <= map.length-1 && cursorY >= 0 && cursorY <= map[0].length-1){
            if(mouseBtn == 1){
                if(this.combineActive){
                    if(cursorX != lastCursorX || cursorY != lastCursorY){
                        map[cursorX][cursorY] = combine(map[cursorX][cursorY]);
                        lastCursorX = cursorX;
                        lastCursorY = cursorY;
                    }
                } else if(ffActive){
                    floodFill(cursorX, cursorY);
                } else map[cursorX][cursorY] = selectedTileId;
            }
            if(mouseBtn == 3) map[cursorX][cursorY] = -1;
        }
    }

    private int combine(int sourceID){
        BufferedImage sourceImage = textures[sourceID];
        BufferedImage image2 = textures[selectedTileId];
        BufferedImage combinedImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();
        g2d.drawImage(sourceImage, 0, 0, null);
        g2d.drawImage(image2, 0, 0, null);
        g2d.dispose();

        System.out.println("saving combined image");
        try {
            File file = new File(path + textures.length + "-N-combined.png");
            ImageIO.write(combinedImage, "png", file);
            System.out.println("saved combined image");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        textures = textureHandler.getTextures();

        return textures.length-1;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++){
                g2.setColor(Color.GRAY);
                if(map[x][y] == -1){
                    g.drawRect((int) ((x * 64 + camOffsetX)), (int) ((y * 64 + camOffsetY)), (int) (tileSize), (int) (tileSize));
                    continue;
                }
                try {
                    g.drawImage(textures[map[x][y]], (int) ((x * 64 + camOffsetX)), (int) ((y * 64 + camOffsetY)), (int) (tileSize), (int) (tileSize), null);
                } catch (Exception e){
                    e.printStackTrace();
                }


            }
        }

        g.drawImage(textures[selectedTileId], (int) ((cursorX * 64 + camOffsetX)), (int) ((cursorY * 64 + camOffsetY)), (int) (tileSize), (int) (tileSize), null);

        g2.setColor(Color.WHITE);
        g2.drawString("Selected Tile ID: " + selectedTileId, 20, 20);
        g2.drawString("Cursor Position: x " + cursorX + " y " + cursorY, 20, 40);

        g2.dispose();
    }

    public void floodFill(int startX, int startY) {
        int rows = map.length;
        int cols = map[0].length;
        int targetID = map[startX][startY];

        // Check if starting point is within bounds and is not the replacement color
        if (startX < 0 || startY < 0 || startX >= rows || startY >= cols || map[startX][startY] == selectedTileId || map[startX][startY] != targetID) {
            return;
        }

        // Perform DFS
        dfs(startX, startY, targetID);
        repaint();
    }

    private void dfs(int x, int y, int targetID) {
        // Replace color at current position
        map[x][y] = selectedTileId;

        // Define the four directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Traverse neighbors
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            // Check if neighbor is within bounds and has the target color
            if (newX >= 0 && newX < map.length && newY >= 0 && newY < map[0].length && map[newX][newY] == targetID) {
                dfs(newX, newY, targetID);
            }
        }
    }

    public void export(){
        System.out.println("exporting data");
        int chunkSizeX = map.length / 16;
        int chunkSizeY = map[0].length / 16;
        System.out.println(chunkSizeX + "-" + chunkSizeY);
        for (int x = 0; x < chunkSizeX; x++){
            for (int y = 0; y < chunkSizeY; y++){
                writeChunkToFile(x, y);
            }
        }
    }

    private void writeChunkToFile(int x, int y){
        int[][] chunk = new int[16][16];
        for (int i = 0; i < 16; i++){
            for (int c = 0; c < 16; c++){
                chunk[c][i] = map[i + x * 16][c + y * 16];
            }
        }

        String filePath = "./src/mapdata/chunk-" + x + "-" + y + ".dat";
        //filePath = "./src/mapdata/default.dat"; // overwrite to export to default chunk file
        try {
            FileWriter writer = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            for (int[] row : chunk) {
                StringBuilder line = new StringBuilder();
                for (int id : row) {
                    line.append(id).append(",");
                }
                bufferedWriter.write(line.toString());
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readChunks(){

        for (int x = 0; x < sizeX / 16; x++){
            for (int y = 0; y < sizeY / 16; y++){
                try {
                    File file = new File("./src/mapdata/chunk-" + x + "-" + y + ".dat");
                    //System.out.println("Reading file: " + file.getName());
                    if (!file.exists()){
                        continue;
                    }
                    Scanner reader = new Scanner(file);
                    int counter = 0;
                    while (reader.hasNextLine()) {
                        String data = reader.nextLine();
                        String[] string = data.replaceAll(" ", "").split(",");
                        for (int i = 0; i < string.length; i++){
                            int id = Integer.parseInt(string[i]);
                            //System.out.printf("For loop values counter: %d, i: %d\n", counter + (y * 16), i + (x * 16));
                            map[i + (x * 16)][counter + (y * 16)] = id;
                        }
                        counter++;
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}
