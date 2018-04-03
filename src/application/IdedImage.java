package application;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class IdedImage extends Image {

    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private final long id = NEXT_ID.getAndIncrement();

    IdedImage(String url) {
        super(url);
    }

    IdedImage(InputStream is) {
        super(is);
    }

    public long getId() {
        return id;
    }
}
