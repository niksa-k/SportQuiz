import javafx.application.Platform;

import javax.lang.model.type.ArrayType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;


public class Server {

    // Mapa za čuvanje bodova svakog igrača
    Map<String, Integer> bodovanje = new HashMap<>();

    private List<String> questions = new ArrayList<>();

    // Indeks trenutnog pitanja u listi
    private int currentQuestionIndex = 0;

    // Lista sa svim klijentima koji su se konektovali na server
    private List<ClientHandler> clients = new ArrayList<>();

    // Lista grupa od po četiri klijenta za igranje kviza
    private ArrayList<ArrayList<Socket>> fourClients = new ArrayList<>();

    // Tajmer za vremensko ograničenje za odgovaranje na pitanja
    private Timer timer;

    // Brojač pitanja za praćenje koliko je pitanja postavljeno
    int pitanjaBrojac = 0;

    // Lista grupa sa klijent handlerima za praćenje trenutne grupe klijenata
    private ArrayList<ArrayList<ClientHandler>> grupeHandlera=new ArrayList<>();

    // Iterator za pracenje broja grupa
    int iterator = 0;

    // Konstruktor klase, dodaje pitanja u listu pitanja za kviz
    public Server() {
        questions.add("Ko je postigao najviše golova u istoriji Svetskih prvenstava,Klose Pele Ronaldo,Klose");
        questions.add("Koji igrač je osvojio najviše NBA titula,Russell Jordan James,Russell");
        questions.add("Ko je osvojio prvo svjetsko prvenstvo u fudbalu,Engleska Brazil Urugvaj,Urugvaj");
        questions.add("Koji broj nosi Nikola Jokic,10 15 20,15");
        questions.add("Koji klub je poznat kao Rossoneri,Inter Milan Juventus,Milan");
        questions.add("Koji tim je osvojio NBA titulu 2008,Lakers Celtics Heat,Celtics");
        questions.add("Ko je osvojio Evropsko prvenstvo 2004,Portugal Grčka Španija,Grčka");
        questions.add("Ko je najskuplji fudbaler u istoriji transfera,Neymar Mbappe Ronaldo,Neymar");
        questions.add("Koji tim je osvojio najviše NBA titula,Lakers Celtics Bulls,Celtics");
        questions.add("Koji klub ima nadimak Gunners,Chelsea Tottenham Arsenal,Arsenal");
        questions.add("Koliko Ligi sampiona je osvojio Real Madrid,14 15 16,15");
        questions.add("Koji igrač je postigao najviše poena u svojoj rookie sezoni,James Chamberlain Jordan,Chamberlain");
        questions.add("Ko je osvojio Svjetsko prvenstvo 2018,Hrvatska Francuska Brazil,Francuska");
        questions.add("Koji igrač je najviše puta bio NBA najbolji strelac,Jordan Bryant Chamberlain,Jordan");
        questions.add("Koji brazilski igrač je poznat kao El Fenomeno,Ronaldinho Ronaldo Neymar,Ronaldo");
        questions.add("Ko je postigao najviše poena u jednoj NBA utakmici,Jordan Bryant Chamberlain,Chamberlain");
        questions.add("Koji tim je bio prvak NBA 2020,Heat Lakers Bucks,Lakers");
        questions.add("Koji igrač je postigao najviše golova u istoriji Lige šampiona,Messi Ronaldo Raúl,Ronaldo");
        questions.add("Koji igrač je poznat kao The King,James Jordan Bryant,James");
        questions.add("Koji tim je Michael Jordan vodio do šest NBA titula,Lakers Celtics Bulls,Bulls");
        questions.add("Koji igrač drži rekord za najviše asistencija u jednoj utakmici, Stockton Skiles Rondo,Skiles");
        questions.add("Koji igrač je poznat kao The Black Mamba,O'Neal Bryant Garnett,Bryant");
        questions.add("Koji igrač je postigao najviše trojki u jednoj sezoni,Curry Allen Miller,Curry");
        questions.add("Koji igrač je najskuplji transfer u istoriji Premier lige,Pogba Grealish Maguire,Grealish");
        questions.add("Koje godine je Engleska osvojila Svjetsko prvenstvo,1962 1966 1970,1966");
        questions.add("Koji igrač je postigao najviše golova u Premier ligi,Shearer Rooney Henry,Shearer");
        questions.add("Koji igrač je postigao najviše trojki u jednoj NBA utakmici,Curry Thompson Allen,Thompson");
        questions.add("Koji igrač je najviše puta osvojio NBA titulu kao trener,Riley Rivers Jackson,Jackson");
        questions.add("Ko je osvojio Evropsko prvenstvo u fudbalu 2016,Portugal Francuska Španija,Portugal");
        questions.add("Koji igrač je poznat kao The Mailman,Barkley Malone O'Neal,Malone");
        questions.add("Koji klub je osvojio najviše titula u Eredivisie,Ajax PSV Feyenoord,Ronaldo");
        questions.add("Koji igrač drži rekord za najviše asistencija u jednoj sezoni,Stockton Johnson Robertson,Stockton");
        questions.add("Koji klub je osvojio najviše titula u Ligue 1,Marseille PSG Saint-Étienne,Saint-Étienne");
    }

    // Metoda za pokretanje servera i osluškivanje novih konekcija
    public void execute() {
        try (ServerSocket ss = new ServerSocket(1000)) {
            System.out.println("Osluskuje...");
            while (true) {
                Socket s = ss.accept();
                System.out.println("Klijent primljen!");
                ClientHandler client = new ClientHandler(s, this);
                client.start();
                clients.add(client);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Metoda za dodavanje novog klijenta u listu čekanja za igru
    public synchronized void addToList(Socket socket) throws IOException {
        if (fourClients.isEmpty()) {
            ArrayList<Socket> newGroup = new ArrayList<>();
            newGroup.add(socket);
            fourClients.add(newGroup);
        } else {
            fourClients.get(fourClients.size() - 1).add(socket);
        }

        ArrayList<Socket> waitingGroup = fourClients.get(fourClients.size() - 1);

        if (waitingGroup.size() < 4) {
            for (Socket s : waitingGroup) {
                for (ClientHandler ch : clients) {
                    if (s.equals(ch.getSocket())) {
                        ch.sendMessage("CEKANJE /Cekajte da se prikljuce ostali igraci!");
                        break;
                    }
                }
            }
        }

        if (waitingGroup.size() == 4) {
            sendStart(waitingGroup);
            ArrayList<Socket> newGroup = new ArrayList<>();
            fourClients.add(newGroup);
        }
    }

    // Metoda za slanje poruke za početak kviza grupi klijenata
    private void sendStart(ArrayList<Socket> group) {
        String message = "START /pocinje";
        ArrayList<ClientHandler> handleri = new ArrayList<>();
        for (Socket client : group) {
            for (ClientHandler handler : clients) {
                if (client.equals(handler.getSocket())) {
                    handler.sendMessage(message);
                    handleri.add(handler);
                    break;
                }
            }
        }
        grupeHandlera.add(handleri);
        quizGame(group, handleri,iterator);
        iterator+=1;
    }

    // Metoda za pokretanje igre kviza za određenu grupu klijenata
    private void quizGame(ArrayList<Socket> group, ArrayList<ClientHandler> handleri,int i) {
        // Slanje poruke sa imenima svih igrača u grupi
        String imena = "IMENA /";
        for (ClientHandler handler : handleri) {
            String username = handler.getUsername();
            imena += username;
            imena += " ";
            bodovanje.put(username, 0);
        }
        for (ClientHandler handler : handleri) {
            handler.sendMessage(imena);
        }
        sendNextQuestion();
    }

    // Metoda za proveru odgovora igrača na pitanje
    public synchronized void proveriOdgovor(ClientHandler handler, String odgovor) {
        // Pronalaženje tačnog odgovora na trenutno pitanje
        String provera = questions.get(currentQuestionIndex - 1);
        String[] niz = provera.split(",");
        String tacanOdgovor = niz[2];
        System.out.println(tacanOdgovor);

        ArrayList<ClientHandler> handleri=grupeHandlera.get(0);

        String bodoviPoruka;
        int noviBodovi;
        if (odgovor.equals(tacanOdgovor)) {
            // Dodavanje bodova za tačan odgovor
            noviBodovi = handler.addBodovi(3);
            bodoviPoruka = "BODOVI /" + handler.getUsername() + " " + noviBodovi;
            // Zaustavljanje tajmera kada je odgovor tačan
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            sendNextQuestion();
        } else {
            // Smanjenje bodova za netačan odgovor
            noviBodovi = handler.addBodovi(-3);
            bodoviPoruka = "BODOVI /" + handler.getUsername() + " " + noviBodovi;
            for(ClientHandler pomocna:handleri){
                if(handler.equals(pomocna)){
                    pomocna.sendMessage("ONEMOGUCI_ODGOVORE");
                    pomocna.sendMessage("ONEMOGUCI");
                }else{
                    pomocna.sendMessage("OMOGUCI_ODGOVORE");
                }
            }
        }
        // Slanje poruke o novom stanju bodova svim klijentima
        for (ClientHandler ch : clients) {
            ch.sendMessage(bodoviPoruka);
        }
    }

    // Metoda za slanje sledećeg pitanja grupi klijenata
    public synchronized void sendNextQuestion() {
        // Onemogućavanje odgovaranja dok se ne postavi novo pitanje
        onemoguciSvima();
        ArrayList<ClientHandler> handlers=grupeHandlera.get(0);

        if (currentQuestionIndex < questions.size()) {
            // Eliminacija igrača sa najmanjim brojem bodova svakih 5 pitanja
            if (pitanjaBrojac > 0 && pitanjaBrojac % 5 == 0 && handlers.size() > 1) {
                eliminateLowestScorer(handlers);
            }
            if (handlers.size() <= 1) {
                return; // Kviz je završen
            }
            // Slanje novog pitanja
            String question = questions.get(currentQuestionIndex);
            for (ClientHandler handler : handlers) {
                handler.sendMessage("OMOGUCI_ODGOVORE /o");
                handler.sendMessage("PITANJE /" + question);
            }
            currentQuestionIndex++;
            pitanjaBrojac++;

            // Postavljanje tajmera za odgovaranje
            for(ClientHandler handler:handlers) {
                if (timer != null) {
                    timer.cancel();
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> sendNextQuestion());
                    }
                }, 20000); // 20 sekundi
            }
        } else {
            // Slanje poruke o završetku kviza ako su sva pitanja postavljena
            for (ClientHandler handler : handlers) {
                handler.sendMessage("KRAJ /Kviz je završen!");
            }
        }
    }

    // Metoda za onemogućavanje odgovaranja svim klijentima
    public void onemoguciSvima(){
        ArrayList<ClientHandler> sviHandleri=grupeHandlera.get(0);
        for(ClientHandler handleri:sviHandleri){
            handleri.sendMessage("ONEMOGUCI /o");
        }
    }

    // Metoda za omogućavanje odgovaranja jednom klijentu
    public void omoguciOdgovor(ClientHandler handler){

        ArrayList<ClientHandler> sviHandleri = grupeHandlera.get(0);

        for(ClientHandler handleri:sviHandleri){
            if(handleri.equals(handler)){
                handler.sendMessage("OMOGUCI /o");
            }else{
                handleri.sendMessage("ONEMOGUCI /o");
                handleri.sendMessage("ONEMOGUCI_ODGOVORE /o");
            }
        }
    }

    // Metoda za eliminaciju igrača sa najmanjim brojem bodova
    private void eliminateLowestScorer(List<ClientHandler> handlers) {
        // Pronađi minimalan broj bodova
        int minScore = Integer.MAX_VALUE;
        for (ClientHandler handler : handlers) {
            int score = handler.getBodovi();
            if (score < minScore) {
                minScore = score;
            }
        }

        // Pronađi sve klijente sa minimalnim brojem bodova
        List<ClientHandler> lowestScorers = new ArrayList<>();
        for (ClientHandler handler : handlers) {
            if (handler.getBodovi() == minScore) {
                lowestScorers.add(handler);
            }
        }

        // Ukloni sve klijente sa minimalnim brojem bodova
        for (ClientHandler lowestScorer : lowestScorers) {
            handlers.remove(lowestScorer);
            clients.remove(lowestScorer);

            lowestScorer.sendMessage("ELIMINISAN /" + lowestScorer.getUsername());
            lowestScorer.closeConnection(); // Zatvori konekciju i prekini nit
        }

        // Obavesti preostale klijente o eliminisanim klijentima
        for (ClientHandler handler : handlers) {
            for (ClientHandler lowestScorer : lowestScorers) {
                handler.sendMessage("ELIMINISAN_IGRAC /" + lowestScorer.getUsername());
            }
        }

        // Ako je preostao samo jedan klijent, proglasi ga pobednikom
        if (handlers.size() == 1) {
            handlers.get(0).sendMessage("POBEDA /Čestitamo, pobedili ste u kvizu!");
            handlers.get(0).closeConnection();
        }
    }
    // Glavna metoda koja pokreće server
    public static void main(String[] args) {
        new Server().execute();
    }
}
