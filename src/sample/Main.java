//SENDER

package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main extends Application {

    private int port = 7000;

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Peer sender");
        BorderPane root = new BorderPane();
        Button btnSend = new Button("Start sending");
        root.setCenter(btnSend);
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        btnSend.setOnAction(ev -> {
            try {
                send();
            } catch (IOException | LineUnavailableException e) {
                e.printStackTrace();
            } catch (UnsupportedAudioFileException e) {
                System.out.println("Audio fajl iz kog se cita nije podrzan");
                e.printStackTrace();
            }
        });
    }

    private void send() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        InetAddress recieverAddress = InetAddress.getByName("localhost");
        System.out.println("Adresa primaoca ---->" + recieverAddress.toString());
        DatagramSocket socket = new DatagramSocket();
        File audioFile = new File("/home/lumar26/Downloads/dostojevski-bedni_ljudi.wav");
        //preko klase AudioSystem se pristupa svim resursima u sistemu, tipa fajlovi, mikrofon itd... takodje mozemo da vidimo koji je format audia
//        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(audioFile); //trebala bi da se uradi provera da li je tip fajla podrzan
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        // veličina frejma
        int frameSize = audioInputStream.getFormat().getFrameSize();
        System.out.println("Veličina jednog okvira u bajtovima -----> " + frameSize);
        //bafer u koji ucitavamo frejm po frejm
        byte[] sendBuffer = new byte[1024 * frameSize]; // ovde da se proveri da li je okej da bude 1024 bajta
        byte[] confirmationMessage = new byte[10];
        int bytesRead = 0, framesRead = 0;
        DatagramPacket sendPackage;
//        paket koji dobijamo od primaoca da bismo znali kad je završio sa obrađivanjem poslednjeg paketa koji smo mu poslali
        DatagramPacket confirmationPacket = new DatagramPacket(confirmationMessage, confirmationMessage.length);




        while((bytesRead = audioInputStream.read(sendBuffer)) != -1){ // kada se dodje do kraja toka vraca -1
//            System.out.println("Broj pročitanih bajtova -----> " + bytesRead);
            framesRead += bytesRead / frameSize;
            sendPackage = new DatagramPacket(sendBuffer, sendBuffer.length, recieverAddress, port);
            socket.send(sendPackage);
            System.out.println("Poslat je " + framesRead + " frejm");

//            ovaj potvrdni paket nam omogućava da blokiramo sljanje sve dok primalac ne primi paket, i dok ga ne prosledi na mikser
            socket.receive(confirmationPacket);
            System.out.println("Frejm uspesno procitan na strani prijema");
        }
        socket.close();
        System.out.println("Ceo fajl je procitan, i soket zatvoren");

    }


    public static void main(String[] args) {
        launch(args);
    }
}
