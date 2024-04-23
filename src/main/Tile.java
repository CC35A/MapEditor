package main;

import java.awt.image.BufferedImage;

public class Tile {
    BufferedImage texture;
    boolean walkable;
    boolean transparent;

    public Tile(BufferedImage texture, boolean walkable, boolean transparent){
        this.texture = texture;
        this.walkable = walkable;
        this.transparent = transparent;
    }
}
