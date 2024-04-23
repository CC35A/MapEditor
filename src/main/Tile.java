package main;

import java.awt.image.BufferedImage;

public class Tile {
    BufferedImage texture;
    int id;
    boolean walkable;
    boolean combined;
    int[] parents;

    public Tile(BufferedImage texture, int id, boolean walkable, boolean combined){
        this.texture = texture;
        this.id = id;
        this.walkable = walkable;
        this.combined = combined;
    }

    public Tile(BufferedImage texture, int id, boolean walkable, boolean combined, int[] parents){
        this.parents = parents;
        new Tile(texture, id, walkable, combined);
    }
}
