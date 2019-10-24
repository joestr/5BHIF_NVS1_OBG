package pkg01medianfilter;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.apache.commons.math3.stat.descriptive.rank.Median;

public class FXMLDocumentController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private MenuItem menuitem_loadpng;

    @FXML
    private MenuItem menuitem_exportmodifiedpng;

    @FXML
    private MenuItem menuitem_applyred;

    @FXML
    private MenuItem menuitem_applygreen;

    @FXML
    private MenuItem menuitem_applyblue;

    @FXML
    private MenuItem menuitem_applymedian;

    @FXML
    private MenuItem menuitem_about;

    @FXML
    private ImageView imageview_current;

    @FXML
    private ImageView imageview_modified;

    @FXML
    private ProgressBar progressbar_currentlydoing;

    @FXML
    private Text text_status;
    
    @FXML
    private TextField median_filter;

    @FXML
    void onAction(ActionEvent event) {
        if(event.getSource().equals(menuitem_loadpng)) {
            
            FileChooser fileChooser = new FileChooser();

            fileChooser.getExtensionFilters().addAll(
                 new FileChooser.ExtensionFilter("PNG - Portable Network Graphic", "*.png")
            );
            
            File file = fileChooser.showOpenDialog(Main.mainStage);
            
            Image image = new Image(file.toURI().toString());
        
            this.imageview_current.setImage(image);
        }
        
        if(event.getSource().equals(menuitem_exportmodifiedpng)) {
            
        }
        
        if(event.getSource().equals(menuitem_applyred)) {
            
            CompletableFuture
                .supplyAsync(() -> {
                    final Text status = text_status;
                    final ProgressBar pB = progressbar_currentlydoing;
                    final int height=(int)imageview_current.getImage().getHeight();
                    final int width=(int)imageview_current.getImage().getWidth();
                    PixelReader pixelReader=imageview_current.getImage().getPixelReader();
                    WritableImage writableImage = new WritableImage(width,height);
                    PixelWriter pixelWriter = writableImage.getPixelWriter();

                    long cTime = System.currentTimeMillis();
                    
                    for (int y = 0; y < height; y++){
                        for (int x = 0; x < width; x++){
                            if(System.currentTimeMillis() - cTime > 1000) {
                                cTime = System.currentTimeMillis();
                                final int xs = x;
                                final int ys = y;
                                Platform.runLater(() -> {
                                    status.setText("Filtering red on image @ X:" + xs + ", Y:" + ys);
                                    pB.setProgress((xs+1*ys+1)/(height*width));
                                });
                            }
                            Color color = pixelReader.getColor(x, y);
                            Color c = new Color(0, color.getGreen(), color.getBlue(), color.getOpacity());
                            pixelWriter.setColor(x, y, c);
                        }
                    }

                    return writableImage;                    
                }).whenComplete((result, error) -> {
                    Platform.runLater(() -> {
                        if(error != null) {
                            text_status.setText("Error");
                            progressbar_currentlydoing.setProgress(0);
                            return;
                        }
                        text_status.setText("Done");
                        progressbar_currentlydoing.setProgress(1);
                        imageview_modified.setImage(result);
                    });
                });
        }
        
        if(event.getSource().equals(menuitem_applygreen)) {
            
            CompletableFuture
                .supplyAsync(() -> {
                    final Text status = text_status;
                    final ProgressBar pB = progressbar_currentlydoing;
                    final int height=(int)imageview_current.getImage().getHeight();
                    final int width=(int)imageview_current.getImage().getWidth();
                    PixelReader pixelReader=imageview_current.getImage().getPixelReader();
                    WritableImage writableImage = new WritableImage(width,height);
                    PixelWriter pixelWriter = writableImage.getPixelWriter();

                    long cTime = System.currentTimeMillis();
                    
                    for (int y = 0; y < height; y++){
                        for (int x = 0; x < width; x++){
                            if(System.currentTimeMillis() - cTime > 1000) {
                                cTime = System.currentTimeMillis();
                                final int xs = x;
                                final int ys = y;
                                Platform.runLater(() -> {
                                    status.setText("Filtering red on image @ X:" + xs + ", Y:" + ys);
                                    pB.setProgress((xs+1*ys+1)/(height*width));
                                });
                            }
                            Color color = pixelReader.getColor(x, y);
                            Color c = new Color(color.getRed(), 0, color.getBlue(), color.getOpacity());
                            pixelWriter.setColor(x, y, c);
                        }
                    }

                    return writableImage;                    
                }).whenComplete((result, error) -> {
                    Platform.runLater(() -> {
                        if(error != null) {
                            text_status.setText("Error");
                            progressbar_currentlydoing.setProgress(0);
                            return;
                        }
                        text_status.setText("Done");
                        progressbar_currentlydoing.setProgress(1);
                        imageview_modified.setImage(result);
                    });
                });
        }
        
        if(event.getSource().equals(menuitem_applyblue)) {
            
            CompletableFuture
                .supplyAsync(() -> {
                    final Text status = text_status;
                    final ProgressBar pB = progressbar_currentlydoing;
                    final int height=(int)imageview_current.getImage().getHeight();
                    final int width=(int)imageview_current.getImage().getWidth();
                    PixelReader pixelReader=imageview_current.getImage().getPixelReader();
                    WritableImage writableImage = new WritableImage(width,height);
                    PixelWriter pixelWriter = writableImage.getPixelWriter();

                    long cTime = System.currentTimeMillis();
                    
                    for (int y = 0; y < height; y++){
                        for (int x = 0; x < width; x++){
                            if(System.currentTimeMillis() - cTime > 1000) {
                                cTime = System.currentTimeMillis();
                                final int xs = x;
                                final int ys = y;
                                Platform.runLater(() -> {
                                    status.setText("Filtering red on image @ X:" + xs + ", Y:" + ys);
                                    pB.setProgress((xs+1*ys+1)/(height*width));
                                });
                            }
                            Color color = pixelReader.getColor(x, y);
                            Color c = new Color(color.getRed(), color.getGreen(), 0, color.getOpacity());
                            pixelWriter.setColor(x, y, c);
                        }
                    }

                    return writableImage;                    
                }).whenComplete((result, error) -> {
                    Platform.runLater(() -> {
                        if(error != null) {
                            text_status.setText("Error");
                            progressbar_currentlydoing.setProgress(0);
                            return;
                        }
                        text_status.setText("Done");
                        progressbar_currentlydoing.setProgress(1);
                        imageview_modified.setImage(result);
                    });
                });
        }
        
        if(event.getSource().equals(menuitem_applymedian)) {
            CompletableFuture
                .supplyAsync(() -> {
                    final Text status = text_status;
                    final ProgressBar pB = progressbar_currentlydoing;
                    final int height=(int)imageview_current.getImage().getHeight();
                    final int width=(int)imageview_current.getImage().getWidth();
                    PixelReader pixelReader=imageview_current.getImage().getPixelReader();
                    WritableImage writableImage = new WritableImage(width,height);
                    PixelWriter pixelWriter = writableImage.getPixelWriter();

                    long cTime = System.currentTimeMillis();
                    
                    int r = Integer.parseInt(median_filter.getText());
                    int runex = 0 + 1 + 2 * r;
                    
                    
                    for (int y = 0; y < height; y++){
                        for (int x = 0; x < width; x++){
                            double bv[] = new double[runex * runex];
                            double rv[] = new double[runex * runex];
                            double gv[] = new double[runex * runex];
                            int addCount = 0;
                            for(int a = x - r; a  < x + 1 + r; a++) {
                                for(int b = y - r; b  < y + 1 * r; b++) {
                                    
                                    if(a < 0 || b < 0) continue;
                                    
                                    Color color;
                                    try {
                                        color = pixelReader.getColor(a, b);
                                    } catch (Exception e) {
                                        continue;
                                    }
                                    
                                    rv[addCount] = color.getRed();
                                    gv[addCount] = color.getGreen();
                                    bv[addCount] = color.getBlue();
                                    
                                    addCount++;
                                }
                            }
                            
                            Median median = new Median();
                            
                            double rm = median.evaluate(rv, 0, addCount);
                            double bm = median.evaluate(bv, 0, addCount);
                            double gm = median.evaluate(gv, 0, addCount);
                            
                            if(System.currentTimeMillis() - cTime > 1000) {
                                cTime = System.currentTimeMillis();
                                final int xs = x;
                                final int ys = y;
                                Platform.runLater(() -> {
                                    status.setText("Filtering red on image @ X:" + xs + ", Y:" + ys);
                                    pB.setProgress((xs+1*ys+1)/(height*width));
                                });
                            }
                            
                            for(int a = x - r; a  < x + 1 +  r; a++) {
                                for(int b = y - r; b  < y + 1 + r; b++) {
                                    
                                    if(a < 0 || b < 0) continue;
                                    
                                    try {
                                        Color color2 = pixelReader.getColor(a, b);
                                        Color c = new Color(rm, gm, bm, color2.getOpacity());
                                        pixelWriter.setColor(a, b, c);
                                    } catch (Exception e) {
                                        
                                    }
                                }
                            }
                            
                        }
                    }

                    return writableImage;                    
                }).whenComplete((result, error) -> {
                    Platform.runLater(() -> {
                        if(error != null) {
                            text_status.setText("Error");
                            progressbar_currentlydoing.setProgress(0);
                            return;
                        }
                        text_status.setText("Done");
                        progressbar_currentlydoing.setProgress(1);
                        imageview_modified.setImage(result);
                    });
                });
        }
        
        if(event.getSource().equals(menuitem_about)) {
            
        }
    }

    @FXML
    void initialize() {
        assert menuitem_loadpng != null : "fx:id=\"menuitem_loadpng\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_exportmodifiedpng != null : "fx:id=\"menuitem_exportmodifiedpng\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_applyred != null : "fx:id=\"menuitem_applyred\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_applygreen != null : "fx:id=\"menuitem_applygreen\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_applyblue != null : "fx:id=\"menuitem_applyblue\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_applymedian != null : "fx:id=\"menuitem_applymedian\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert menuitem_about != null : "fx:id=\"menuitem_abbout\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert imageview_current != null : "fx:id=\"imageview_current\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert imageview_modified != null : "fx:id=\"imageview_modified\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert progressbar_currentlydoing != null : "fx:id=\"progressbar_currentlydoing\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
        assert text_status != null : "fx:id=\"text_status\" was not injected: check your FXML file 'FXMLDocument.fxml'.";

    }
}
