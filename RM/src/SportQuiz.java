import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class SportQuiz extends Application {

    Client client;
    Stage primaryStage;
    Scene kvizScena;

    private Timeline timeline;
    private Label lblTimer = new Label("Time: 20"); // Prikaz tajmera
    private int timeSeconds = 5; // Preostalo vreme u sekundama
    Label lblObavijestIgracima;

    Label lblPrviIgrac = new Label("");
    Label lblBodoviPrvi = new Label("0");
    Label lblBodoviDrugi = new Label("0");
    Label lblBodoviTreci = new Label("0");
    Label lblBodoviCetvrti = new Label("0");
    Label lblDrugiIgrac = new Label("");
    Label lblTreciIgrac = new Label("");
    Label lblCetvrtiIgrac = new Label("");
    Label lblPitanje = new Label("Pitanje");
    Button btnPrviOdgovor = new Button("");
    Button btnDrugiOdgovor = new Button("");
    Button btnTreciOdgovor = new Button("");
    Button btnTaster = new Button("ODGOVOR");

    Map<String, Label> bodoviMap = new HashMap<>();
    Map<String, Label> igracLabelMap = new HashMap<>();
    @Override
    public void start(Stage primaryStage) throws Exception {

        client = new Client(this);
        client.start();

        VBox pocetna = new VBox();
        pocetna.setAlignment(Pos.CENTER);
        pocetna.setSpacing(5);
        Scene scena = new Scene(pocetna, 250, 250);
        scena.getStylesheets().add("styles.css");
        primaryStage.setScene(scena);
        primaryStage.setTitle("Sportski kviz");
        primaryStage.show();

        Label lblKorisnickoIme = new Label("Unesite svoje korisnicko ime:");
        lblKorisnickoIme.getStyleClass().add("label-warning");
        TextArea taKorisnickoIme = new TextArea();
        lblObavijestIgracima = new Label("");
        lblObavijestIgracima.getStyleClass().add("label-warning");
        taKorisnickoIme.setPrefWidth(50);
        taKorisnickoIme.setPrefHeight(40);
        Button btnPotvrdaKorisnickogImena = new Button("Potvrdi");
        pocetna.getChildren().addAll(lblKorisnickoIme, taKorisnickoIme, lblObavijestIgracima, btnPotvrdaKorisnickogImena);

        btnPotvrdaKorisnickogImena.setOnAction(actionEvent -> {
            String ime = taKorisnickoIme.getText();
            if (ime.equals("")) {
                lblObavijestIgracima.setText("Korisnicko ime je prazno");
                return;
            }
            client.setUsername(taKorisnickoIme.getText());
            client.sendUsername(taKorisnickoIme.getText());
        });
    }

    public void setLabelObavijest(String poruka) {
        lblObavijestIgracima.setText(poruka);
    }

    public void lostGame(){

        VBox gameLose=new VBox();
        gameLose.setAlignment(Pos.CENTER);
        gameLose.getStyleClass().add("vbox-elimination");
        Scene lostScene=new Scene(gameLose,400,400);
        lostScene.getStylesheets().add("styles.css");
        Label lblispisPoraza=new Label("Ispali ste iz igre!");
        lblispisPoraza.getStyleClass().add("label-lose");
        gameLose.getChildren().addAll(lblispisPoraza);

        Platform.runLater(() -> {
            Stage primaryStage=(Stage)lblPitanje.getScene().getWindow();
            primaryStage.setScene(lostScene);
        });
    }

    public void winGame(){
        VBox winLayout = new VBox();
        winLayout.setAlignment(Pos.CENTER);
        winLayout.getStyleClass().add("vbox-elimination");
        Scene winScene = new Scene(winLayout, 400, 400);
        winScene.getStylesheets().add("styles.css");
        Label lblIspisPobjede = new Label("Cestitamo, osvojili ste kviz!");
        lblIspisPobjede.getStyleClass().add("label-win");
        winLayout.getChildren().addAll(lblIspisPobjede);

        Platform.runLater(() -> {
            Stage primaryStage = (Stage) lblPitanje.getScene().getWindow();
            primaryStage.setScene(winScene);
        });
    }
    public void startGame() {

        VBox gameLayout = new VBox();
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.getStyleClass().add("vbox-game");
        Scene gameScene = new Scene(gameLayout, 450, 350);
        gameScene.getStylesheets().add("styles.css");

        // Postavljanje prvih dva igraca u prvi red
        HBox prviRed = new HBox();
        prviRed.setAlignment(Pos.CENTER);
        prviRed.setSpacing(10);
        prviRed.getChildren().addAll(lblPrviIgrac, lblBodoviPrvi, lblDrugiIgrac, lblBodoviDrugi);

        // Postavljanje druga dva igraca u drugi red
        HBox drugiRed = new HBox();
        drugiRed.setAlignment(Pos.CENTER);
        drugiRed.setSpacing(10);
        drugiRed.getChildren().addAll(lblTreciIgrac, lblBodoviTreci, lblCetvrtiIgrac, lblBodoviCetvrti);

        Platform.runLater(() -> {
            Stage primaryStage = (Stage) lblObavijestIgracima.getScene().getWindow();
            primaryStage.setScene(gameScene);
        });

        btnPrviOdgovor.setDisable(true);
        btnTreciOdgovor.setDisable(true);
        btnDrugiOdgovor.setDisable(true);

        gameLayout.getChildren().addAll(prviRed, drugiRed, lblPitanje, btnPrviOdgovor, btnDrugiOdgovor, btnTreciOdgovor, btnTaster, lblTimer);


        btnPrviOdgovor.setOnAction(actionEvent -> client.posaljiOdgovor(btnPrviOdgovor.getText()));
        btnDrugiOdgovor.setOnAction(actionEvent -> client.posaljiOdgovor(btnDrugiOdgovor.getText()));
        btnTreciOdgovor.setOnAction(actionEvent -> client.posaljiOdgovor(btnTreciOdgovor.getText()));

        initializeTimer(); // Inicijalizacija tajmera
        timeline.playFromStart(); // Pokretanje tajmera

        btnTaster.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                client.sendMessage("TASTER /a");
            }
        });
    }

    private void initializeTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeSeconds--;
            lblTimer.setText("Time: " + timeSeconds);
            if (timeSeconds <= 0) {
                timeline.stop();
                onTimeUp();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }

    private void resetTimer() {
        timeSeconds = 20; // Postavite željeno početno vreme
        lblTimer.setText("Time: " + timeSeconds);
        timeline.playFromStart(); // Ponovo pokrenite tajmer
    }
    private void onTimeUp() {
        // Radnje koje se izvode kada istekne vreme
        client.sendMessage("TIME_UP /");
        onemoguciOdgovore();
        onemoguciTaster();
    }

    public void azurirajBodove(String username, int bodovi) {
        Label lblBodovi = bodoviMap.get(username);
        if (lblBodovi != null) {
            Platform.runLater(() -> lblBodovi.setText(String.valueOf(bodovi)));
        }
    }

    public void postaviImena(String imena) {
        System.out.println(imena);
        String imena2 = imena.trim();
        String[] niz = imena2.split(" ");
        System.out.println(niz.length);
        lblPrviIgrac.setText(niz[0]);
        lblDrugiIgrac.setText(niz[1]);
        lblTreciIgrac.setText(niz[2]);
        lblCetvrtiIgrac.setText(niz[3]);

        lblPrviIgrac.getStyleClass().add("label-players");
        lblDrugiIgrac.getStyleClass().add("label-players");
        lblTreciIgrac.getStyleClass().add("label-players");
        lblCetvrtiIgrac.getStyleClass().add("label-players");

        lblBodoviPrvi.getStyleClass().add("label-points");
        lblBodoviDrugi.getStyleClass().add("label-points");
        lblBodoviTreci.getStyleClass().add("label-points");
        lblBodoviCetvrti.getStyleClass().add("label-points");

        lblPitanje.getStyleClass().add("label-questions");
        lblTimer.getStyleClass().add("label-points");

        bodoviMap.put(niz[0], lblBodoviPrvi);
        bodoviMap.put(niz[1], lblBodoviDrugi);
        bodoviMap.put(niz[2], lblBodoviTreci);
        bodoviMap.put(niz[3], lblBodoviCetvrti);

        igracLabelMap.put(niz[0] , lblPrviIgrac);
        igracLabelMap.put(niz[1],lblDrugiIgrac);
        igracLabelMap.put(niz[2],lblTreciIgrac);
        igracLabelMap.put(niz[3],lblCetvrtiIgrac);
    }

    public void omoguciTaster(){
        btnTaster.setDisable(false);
    }
    public void onemoguciTaster(){
        btnTaster.setDisable(true);
    }

    public void postaviPitanje(String pitanje) {
        String[] niz = pitanje.split(",");
        lblPitanje.setText(niz[0] + "?");
        String[] odgovori = niz[1].split(" ");
        btnPrviOdgovor.setText(odgovori[0]);
        btnDrugiOdgovor.setText(odgovori[1]);
        btnTreciOdgovor.setText(odgovori[2]);
        resetTimer();
    }

    public void omoguciOdgovore(){
        btnPrviOdgovor.setDisable(false);
        btnDrugiOdgovor.setDisable(false);
        btnTreciOdgovor.setDisable(false);
    }
    public void onemoguciOdgovore(){
        btnPrviOdgovor.setDisable(true);
        btnDrugiOdgovor.setDisable(true);
        btnTreciOdgovor.setDisable(true);
    }

    public void eliminacija(String username) {
        // Postavi labelu za igrača na "ELIMINISAN" i bodove na 0
        Label lblIgrac = igracLabelMap.get(username);
        Label lblBodovi = bodoviMap.get(username);
        if (lblIgrac != null && lblBodovi != null) {
            Platform.runLater(()->{
                lblIgrac.setText("ELIMINISAN");
                lblIgrac.getStyleClass().add("label-elimination");
                lblBodovi.setText("0");
                lblBodovi.getStyleClass().add("label-elimination");
            });
        }
    }
}
