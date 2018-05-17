package sample;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import util.FileUtils;
import util.PropertiesUtils;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.ImageView;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML
    private AnchorPane pane_settings;

    @FXML
    private Button btn_settings;

    @FXML
    private Button btn_close, btn_close_settings, load_button, stop_button, music_load_button, button_play;

    @FXML
    private JFXListView music_list;

    @FXML
    private ObservableList playListFiles = FXCollections.observableArrayList();

    @FXML
    private MediaView mediaView;

    @FXML
    private JFXToggleButton dark_mode_button;

    @FXML
    private JFXProgressBar progress;

    @FXML
    private Pane right_banner_top, left_banner_top;

    @FXML
    private AnchorPane center_banner;

    @FXML
    private JFXSlider volumeSlider;

    @FXML
    private JFXButton next_button, prev_button;

    private ObjectProperty<Path> selectedMedia = new SimpleObjectProperty<>();
    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;
    private String path;
    private Media media;
    private MediaPlayer player;
    private MediaPlayer nextPlayer;
    //    private final
    private String dir = "/Users/paul/Music/iTunes/iTunes Media/Music/Blur/Blur";
    private ObservableList music_real_path_list;
    private ChangeListener<Duration> progressChangeListener;
    private int selected;
    private ObservableList music_name_list;
    private MapChangeListener<String, Object> metadataChangeListener;
    private List<MediaPlayer> players;
    private List<Path> listOfFiles;

    public void initialize(URL location, ResourceBundle resources) {


        music_list.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(Change c) {
                System.exit(0);
            }
        });

        music_list.setOnMouseClicked((event) -> {
            if (event.getClickCount() == 2) {


                selected = music_list.getSelectionModel().getSelectedIndex();


                for (int i = selected; i < music_real_path_list.size(); i++) {
                    player = players.get(selected);
                    if (selected == music_real_path_list.size() - 1) {
                        nextPlayer = null;
                    } else {

                        nextPlayer = players.get((selected + 1) % players.size());
                        player.setOnEndOfMedia(new Runnable() {
                            @Override
                            public void run() {
                                player.stop();

                                mediaView.setMediaPlayer(nextPlayer);
                                nextPlayer.play();
                                setCurrentlyPlaying(mediaView.getMediaPlayer());

                            }
                        });
                    }


                }


                if (mediaView.getMediaPlayer() != null) {
                    mediaView.getMediaPlayer().stop();
                }
                mediaView = new MediaView(players.get(selected));


//
                mediaView.setMediaPlayer(players.get(selected));
                mediaView.getMediaPlayer().play();
                setCurrentlyPlaying(mediaView.getMediaPlayer());


                volumeSlider.setValue(mediaView.getMediaPlayer().getVolume() * 50);
                volumeSlider.valueProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        mediaView.getMediaPlayer().setVolume(volumeSlider.getValue() / 100);
                    }
                });


                System.out.println(mediaView.getMediaPlayer().getStatus());

            }
        });


    }

    @FXML
    private void handleButtonAction(MouseEvent event) {
        if (event.getSource() == btn_close) {
            System.exit(0);


        } else {
            if (event.getSource() == btn_settings) {
                pane_settings.setVisible(true);
            }
            if (event.getSource() == btn_close_settings) {
                pane_settings.setVisible(false);

            }
            if (event.getSource() == button_play) {
                mediaView.getMediaPlayer().play();
            }
            if (event.getSource() == stop_button) {
//                mediaPlayer.pause();
                final MediaPlayer curPlayer = mediaView.getMediaPlayer();
                curPlayer.pause();
            }

            if (event.getSource() == next_button) {
                selected = selected + 1;
                if (mediaView.getMediaPlayer() != null) {
                    mediaView.getMediaPlayer().stop();
                }

                if (selected >= players.size()) {
                    selected = 0;
                }


//
                mediaView.setMediaPlayer(players.get(selected));
                mediaView.getMediaPlayer().play();
            }

            if (event.getSource() == prev_button) {
                selected = selected - 1;
                if (mediaView.getMediaPlayer() != null) {
                    mediaView.getMediaPlayer().stop();
                }
                if (selected <= -1) {
                    selected = players.size() - 1;
                }
                mediaView.setMediaPlayer(players.get(selected));
                mediaView.getMediaPlayer().play();
            }

            if (event.getSource() == music_load_button) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Files",
                                PropertiesUtils.readFormats()));
                listOfFiles = new ArrayList<Path>();
                listOfFiles = FileUtils.convertListFiletoListPath(chooser.showOpenMultipleDialog(((Button) event.getSource()).getScene().getWindow()));
                if (listOfFiles != null) {
                    listOfFiles.stream().forEach(System.out::println);
                    listOfFiles.stream().forEach(playListFiles::add);
                    playListFiles.stream().forEach(System.out::println);

                    music_name_list = FXCollections.observableArrayList();
                    music_real_path_list = FXCollections.observableArrayList();
                    players = new ArrayList<>();

//                    File[] sourceFile = new File[playListFiles.size()];
                    for (int i = 0; i < playListFiles.size(); i++) {
                        music_real_path_list.add("target/classes/media/" + playListFiles.get(i).toString().substring(playListFiles.get(i).toString().lastIndexOf("/") + 1, playListFiles.get(i).toString().length()));
                        FileInputStream inputStream = null;
//

                        try {
                            inputStream = new FileInputStream(playListFiles.get(i).toString());
                            FileOutputStream outputStream = new FileOutputStream(HomeController.class.getResource("../media/").toString().split("file:")[1] + playListFiles.get(i).toString().substring(playListFiles.get(i).toString().lastIndexOf("/") + 1, playListFiles.get(i).toString().length()));
                            FileChannel fcin = inputStream.getChannel();
                            FileChannel fcout = outputStream.getChannel();
                            long size = fcin.size();
                            fcin.transferTo(0, size, fcout);

                            fcout.close();
                            fcin.close();

                            outputStream.close();
                            inputStream.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        music_name_list.add(playListFiles.get(i).toString().substring(playListFiles.get(i).toString().lastIndexOf("/") + 1, playListFiles.get(i).toString().length()));
                        music_list.setItems(music_name_list);


                    }

                }


                for (int i = 0; i < music_real_path_list.size(); i++) {

                    players.add(createPlayer(Paths.get(music_real_path_list.get(i).toString()).toUri().toString()));
                }


                for (int i = selected; i < music_real_path_list.size(); i++) {
                    player = players.get(selected);
                    if (selected == music_real_path_list.size() - 1) {
                        nextPlayer = null;
                    } else {

                        nextPlayer = players.get((selected + 1) % players.size());
                        player.setOnEndOfMedia(new Runnable() {
                            @Override
                            public void run() {
                                player.stop();

                                mediaView.setMediaPlayer(nextPlayer);
                                nextPlayer.play();
                                setCurrentlyPlaying(mediaView.getMediaPlayer());

                            }
                        });
                    }


                }


//                    System.out.println(music_list);


            }

            if (event.getSource() == dark_mode_button) {
                if (dark_mode_button.isSelected()) {
                    right_banner_top.setStyle("-fx-background-color: #" + "000000");
                    center_banner.setStyle("-fx-background-color: #" + "000000");
                    music_list.setStyle("-fx-background-color: #" + "000000");
                    left_banner_top.setStyle("-fx-background-color: #" + "000000");
                } else {
                    right_banner_top.setStyle("-fx-background-color: #" + "5AB0E2");
                    center_banner.setStyle("-fx-background-color: #" + "5AB0E2");
                    music_list.setStyle("-fx-background-color: #" + "FFFFFF");
                    left_banner_top.setStyle("-fx-background-color: #" + "5AB0E2");


                }
            }


        }
    }

    private MediaPlayer createPlayer(String mediaSource) {
        final Media media = new Media(mediaSource);
        final MediaPlayer player = new MediaPlayer(media);
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println("Media error occurred: " + player.getError());
            }
        });
        return player;
    }

    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
        newPlayer.seek(Duration.ZERO);

        progress.setProgress(0);
        progressChangeListener = new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
            }
        };
        newPlayer.currentTimeProperty().addListener(progressChangeListener);

        String source = newPlayer.getMedia().getSource();


    }


}
