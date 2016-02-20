package TransportLayer;

/*
 * TextSender.java
 *
 * Created on 15 January 2003, 15:29
 */
/**
 *
 * @author abj
 */
import AudioLayer.AudioManager;
import CMPC3M06.AudioPlayer;
import CMPC3M06.AudioRecorder;
import static TransportLayer.SoundSender.sending_socket;
import VoIPLayer.VoIPManager;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import networkscw.NetworksCW.SocketType;
import static networkscw.NetworksCW.SocketType.*;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

public class VoiceSenderThread implements Runnable {

    static DatagramSocket sending_socket;
    private final AudioManager audioManager = new AudioManager();
    VoIPManager voIPManager;
    private SocketType socketType = Type0;
    AudioRecorder recorder;
    private final int PORT = 55555;
    InetAddress clientIP = null;

    public VoiceSenderThread(SocketType type) {
        this.voIPManager = new VoIPManager(socketType);
        this.socketType = type;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {

        try {

            //Port to send to
            //IP ADDRESS to send to
            try {
                clientIP = InetAddress.getByName("25.45.213.173");  //CHANGE localhost to IP or NAME of client machine
            } catch (UnknownHostException e) {
                System.out.println("ERROR: TextSender: Could not find client IP");
                e.printStackTrace();
                System.exit(0);
            }

            //Open a socket to send from
            //We dont need to know its port number as we never send anything to it.
            try {

                voIPManager.readySocket(socketType, 's');

            } catch (SocketException e) {
                System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
                System.exit(0);
            }

            boolean running = true;
            recorder = new AudioRecorder();
      
            int i = 0;
            while (running) {
                try {

                    byte[] buffer = recorder.getBlock();

                    voIPManager.TransmitVoice(PORT, buffer, clientIP, i);
                    i++;
                 
            
                } catch (IOException e) {
                    System.out.println("ERROR: TextSender: Some random IO error occured!");
                }
            }
            //Close the socket
    
            sending_socket.close();

        } catch (LineUnavailableException ex) {
            Logger.getLogger(VoiceSenderThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public String toString() {
        return "VoiceSenderThread{" + ", voIPManager=" + voIPManager + ", socketType=" + socketType + ", recorder=" + recorder + ", PORT=" + PORT + ", clientIP=" + clientIP + '}';
    }

}
