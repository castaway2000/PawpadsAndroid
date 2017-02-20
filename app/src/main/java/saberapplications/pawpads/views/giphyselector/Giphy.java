package saberapplications.pawpads.views.giphyselector;

/**
 * Created by Stanislav Volnjanskij on 2/14/17.
 */

public class Giphy {
    public static class Image{
        String url;
        int width;
        int height;

        public Image(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
    String name;
    Image thumb;
    Image full;

    public Giphy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Image getThumb() {
        return thumb;
    }

    public void setThumb(Image thumb) {
        this.thumb = thumb;
    }

    public Image getFull() {
        return full;
    }

    public void setFull(Image full) {
        this.full = full;
    }
}
