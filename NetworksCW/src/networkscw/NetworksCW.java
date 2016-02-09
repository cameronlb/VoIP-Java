package networkscw;

import Others.TextSenderThread;
import Others.TextReceiverThread;
import TransportLayer.VoiceSenderThread;
import TransportLayer.VoiceReceiverThread;
import AudioLayer.AudioManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.sound.sampled.LineUnavailableException;
import static networkscw.NetworksCW.SocketType.*;

/**
 *
 * @author Beren
 */
public class NetworksCW {

    private static final AudioManager audioManager = new AudioManager();
    private static TextReceiverThread receiver;
    private static TextSenderThread sender;
    private static VoiceReceiverThread voiceReceiver;
    private static VoiceSenderThread voiceSender;

    /**
     * For testing with different DataSockets.
     */
    public enum SocketType {

        Type0,
        Type1,
        Type2,
        Type3;
    }

    /* @param args the command line arguments
     * @throws javax.sound.sampled.LineUnavailableException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws LineUnavailableException, IOException {

        ReadyThreads(Type0);
        //RecordingTest();
        //RunVoiceThreads();
        //RunTextThreads();
        
        RecordingTest();
        

    }

    /**
     * To start the voice threads;
     */
    private static void RunVoiceThreads() {
        voiceReceiver.start();
        voiceSender.start();
    }

    /**
     * To start the text threads;
     */
    private static void RunTextThreads() {

        receiver.start();
        sender.start();
    }

    /**
     *
     *
     * /**
     * Initialising thread objects.
     *
     * @param type
     */
    static void ReadyThreads(SocketType type) {

        voiceReceiver = new VoiceReceiverThread(type);
        voiceSender = new VoiceSenderThread(type);
        receiver = new TextReceiverThread(type);
        sender = new TextSenderThread(type);

    }

    /**
     * Method for testing the audio manager.
     *
     * @throws LineUnavailableException
     * @throws IOException
     */
    static void RecordingTest() throws LineUnavailableException, IOException {
        Vector<byte[]> recordedAudio = audioManager.RecordAudio(5);
        //audioManager.PlayAudio(recordedAudio);
    
        for (byte[] recordedAudio1 : recordedAudio) {
            
            for (int i = 0; i < recordedAudio1.length; i++) {
                     System.out.print(recordedAudio1[i]);
               }
            
           System.out.println("\n");
       
        }
           
           
    }

}