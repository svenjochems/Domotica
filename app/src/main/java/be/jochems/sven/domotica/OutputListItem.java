package be.jochems.sven.domotica;

/**
 * Created by sven on 10/09/16.
 */

public class OutputListItem {
    private int imgResource;
    private String text;

    public OutputListItem(int img, String text){
        this.imgResource = img;
        this.text = text;
    }

    public int getImgResource() {
        return imgResource;
    }

    public void setImgResource(int imgResource) {
        this.imgResource = imgResource;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
