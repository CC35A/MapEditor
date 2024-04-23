package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class textureHandler {
    static String path = "./src/textures";

    static public BufferedImage[] getTextures(){
        File[] dir = new File(path).listFiles();
        ArrayList<BufferedImage> imgList = new ArrayList<BufferedImage>();
        BufferedImage[] imgArr = new BufferedImage[dir.length];
        try {
            for (File f : Objects.requireNonNull(dir)){
                BufferedImage img = ImageIO.read(f);
                Image scaleImg = img.getScaledInstance(32, 32, Image.SCALE_DEFAULT);
                img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                img.getGraphics().drawImage(scaleImg, 0, 0, null);
                imgList.add(img);
                String renamePath = f.getPath().replace(f.getName(), imgList.size()-1 + "-" + f.getName());
                if(!f.getName().matches("^\\d.*")) f.renameTo(new File(renamePath));
                int index;
                String[] keys = f.getName().split("-");
                index = Integer.parseInt(keys[0]);
                imgArr[index] = img;
                //System.out.println(renamePath);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        BufferedImage[] returnArr = new BufferedImage[imgList.size()];
        returnArr = imgList.toArray(returnArr);
        //return returnArr;
        return imgArr;
    }
}
