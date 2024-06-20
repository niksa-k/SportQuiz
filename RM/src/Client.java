import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{

    private Socket s;
    private String username;
    private PrintWriter writer;
    private BufferedReader reader;
    int bodovi=0;
    private SportQuiz quiz;
    public Client(SportQuiz quiz){

        try {
            this.s=new Socket("localhost",1000);
            this.quiz=quiz;
            try {
                writer=new PrintWriter(s.getOutputStream(),true);
                reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void run (){


        try {
            String poruka;
            while(true) {
                poruka = reader.readLine();
                if(poruka==null){
                    break;
                }
                String[] akcija=poruka.split(" /");

                if(akcija[0].equals("CEKANJE")) {
                    String finalPoruka = akcija[1];
                    Platform.runLater(() -> quiz.setLabelObavijest(finalPoruka));
                }
                if(akcija[0].equals("START")){
                    quiz.startGame();
                }
                if(akcija[0].equals("IMENA")){
                    String finalPoruka=akcija[1];
                    Platform.runLater(()->quiz.postaviImena(finalPoruka));
                }
                if(akcija[0].equals("PITANJE")){
                    String finalPoruka=akcija[1];
                    Platform.runLater(()->quiz.postaviPitanje(finalPoruka));
                }
                if (akcija[0].equals("BODOVI")) {
                    String[] bodoviData = akcija[1].split(" ");
                    String username = bodoviData[0];
                    int bodovi = Integer.parseInt(bodoviData[1]);
                    Platform.runLater(() -> quiz.azurirajBodove(username, bodovi));
                }if(akcija[0].equals("ELIMINISAN_IGRAC")){
                    Platform.runLater(()->quiz.eliminacija(akcija[1]));
                }if(akcija[0].equals("ELIMINISAN")){
                    quiz.lostGame();

                }if(akcija[0].equals("POBEDA")){
                    quiz.winGame();
                }if(akcija[0].equals("OMOGUCI")){
                    Platform.runLater(()->quiz.omoguciOdgovore());
                }if(akcija[0].equals("ONEMOGUCI")){
                    Platform.runLater(()->quiz.onemoguciOdgovore());
                }if(akcija[0].equals("ONEMOGUCI_ODGOVORE")){
                    Platform.runLater(()->quiz.onemoguciTaster());
                }if(akcija[0].equals("OMOGUCI_ODGOVORE")){
                    Platform.runLater(()->quiz.omoguciTaster());
                }



            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public void sendUsername(String username){

        writer.println(username);

    }

    public void sendMessage(String odgovor){
        writer.println(odgovor);
    }
    public void posaljiOdgovor(String odgovor) {
        writer.println("ODGOVOR /" + odgovor);
    }

    public void setUsername(String ime){
        this.username=ime;
    }

    public Client vrati(){
        return this;
    }
    public String getUsername(){
        return this.username;
    }

}