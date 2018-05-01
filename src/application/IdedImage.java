package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

//a javafx image object with some added functionality

public class IdedImage extends Image {

    IdedImage(String url) {
        super(url);
    }

    IdedImage(InputStream is) {
        super(is);
    }

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private final long id = NEXT_ID.getAndIncrement();

    public long id() {
        return id;
    }
    public int colorCount() { return numColors; }
    public int[][] colors() { return colorArray; }


    private int numColors = 1;
    private int[][] colorArray = new int[][]{{0,0,0}};


    public void initColors() {

        Set<Integer> colorHashSet = new HashSet<>();    //a HashSet of all the colors values, duplicates automatically removed due to the nature of HashSets

        Color color;

        PixelReader reader = this.getPixelReader();

        for( int row = 0; row < this.getHeight(); row++ ) {
            for( int column = 0; column < this.getWidth(); column++ ) {
                color = reader.getColor(column, row);

                //color values are received as a value between 0 to 1, so convert them to 0 to 255
                //  then collapse them into a single int
                //  this is SIGNIFICANTLY better performing than a HashSet of int[]
                colorHashSet.add(   ((int) (255 * color.getRed()) << 16) |
                                    ((int) (255 * color.getGreen()) << 8) |
                                    ((int) (255 * color.getBlue())) );
            }
        }

        colorArray = new int[colorHashSet.size()][3];

        int index = 0;
        for( int colorInt : colorHashSet ) {
            colorArray[index][0] = (colorInt >> 16) & 0xFF;   //split the color values out of the integer value and set them as the RGB values for the color in the new array
            colorArray[index][1] = (colorInt >> 8) & 0xFF;
            colorArray[index++][2] = (colorInt) & 0xFF;
        }

        numColors = colorHashSet.size();

        if( numColors == 0 ) {  //if something went wrong use the fallback values
            numColors = 1;
            colorArray = new int[][]{{0,0,0}};
        }
    }
}
