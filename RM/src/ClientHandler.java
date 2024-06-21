import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread{

    Socket s; // Soket za komunikaciju sa klijentomA
    PrintWriter writer; // Objekat za slanje poruka klijentu
    BufferedReader reader; // Objekat za čitanje poruka od klijenta

    int bodovi = 0; // Bodovi klijenta
    String username; // Korisničko ime klijenta
    Server server; // Referenca na server

    // Konstruktor koji inicijalizuje ClientHandler sa soketom i serverom
    public ClientHandler(Socket s, Server server) throws IOException {
        this.s = s;
        this.server = server;
        try {
            writer = new PrintWriter(s.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Glavna metoda koja se izvršava kada se nit pokrene
    @Override
    public void run() {
        try {
            String ime = reader.readLine(); // Čitanje korisničkog imena od klijenta
            this.username = ime;
            server.addToList(s); // Dodavanje klijenta u listu klijenata na serveru

            String odgovor;
            while (true) {
                odgovor = reader.readLine(); // Čitanje odgovora od klijenta
                String[] akcija = odgovor.split(" /");

                if (akcija[0].equals("ODGOVOR")) {
                    server.proveriOdgovor(this, akcija[1]);
                }
                if (akcija[0].equals("TASTER")) {
                    server.omoguciOdgovor(this);
                }
                if (akcija[0].equals("TIME_UP")) {
                    server.sendNextQuestion();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Metoda za zatvaranje konekcije sa klijentom
    public void closeConnection() {
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.interrupt();
    }

    // Metoda za dodavanje bodova klijentu
    public int addBodovi(int bodovi) {
        this.bodovi += bodovi;
        return this.bodovi;
    }

    // Metoda za dobijanje korisničkog imena klijenta
    public String getUsername() {
        return this.username;
    }

    // Metoda za slanje poruke klijentu
    public void sendMessage(String message) {
        writer.println(message);
    }

    // Metoda za dobijanje soketa klijenta
    public Socket getSocket() {
        return this.s;
    }

    // Metoda za dobijanje broja bodova klijenta
    public int getBodovi() {
        return this.bodovi;
    }
}
