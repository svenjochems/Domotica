package be.jochems.sven.domotica.view;

/**
 * Created by sven on 10/09/16.
 */

public class OutputListItem {
    private int imgResource;
    private String text;
    private boolean isMood;
    int outputIndex = -1;

    public OutputListItem(int img, String text, boolean isMood){
        this.imgResource = img;
        this.text = text;
        this.isMood = isMood;
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

    public boolean isMood() {
        return isMood;
    }

    public void setMood(boolean mood) {
        isMood = mood;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public void setOutputIndex(int outputIndex) {
        this.outputIndex = outputIndex;
    }
}
