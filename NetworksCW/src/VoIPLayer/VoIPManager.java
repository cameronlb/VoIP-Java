package VoIPLayer;

import CMPC3M06.AudioPlayer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.sound.sampled.LineUnavailableException;
import networkscw.NetworksCW.SocketType;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;
import uk.ac.uea.cmp.voip.DatagramSocket4;

/**
 *
 * @author Beren
 */
public class VoIPManager {

    private final boolean interleave = true;
    private final boolean repetitionMethod = true;

    private final int interleaveNumber = 16;
    private ArrayList<CustomPacket> packetsToSend = new ArrayList<CustomPacket>(interleaveNumber);
    private DatagramSocket sendingSocket;
    private DatagramSocket receivingSocket;
    private AudioPlayer player;
    private SocketType type;

    public VoIPManager(SocketType type) {
        this.type = type;
    }

    public void setType(SocketType type) {
        this.type = type;
    }

    public SocketType getType() {
        return this.type;
    }

    private ArrayList<CustomPacket> interleavePackets(ArrayList<CustomPacket> packetsList) {

        int n = (int) Math.sqrt(packetsList.size());

        CustomPacket[][] arrayToInterleave = new CustomPacket[n][n];

        // POPULATING ARRAY
        int a = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                arrayToInterleave[i][j] = packetsList.get(a);
                a++;

            }
        }

        // TRANSPOSING
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                CustomPacket temp = arrayToInterleave[i][j];
                arrayToInterleave[i][j] = arrayToInterleave[j][i];
                arrayToInterleave[j][i] = temp;

            }
        }

        // REVERSING COLUMNS
        for (int col = 0; col < n; col++) {
            for (int row = 0; row < n / 2; row++) {
                CustomPacket temp = arrayToInterleave[row][col];
                arrayToInterleave[row][col] = arrayToInterleave[n - row - 1][col];
                arrayToInterleave[n - row - 1][col] = temp;

            }
        }

        packetsList.clear();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {

                //System.out.println(arrayToInterleave[i][j]);
                packetsList.add(arrayToInterleave[i][j]);
            }
        }

        return packetsList;

    }

    class CustomPacket implements Comparable<CustomPacket> {

        public long packetID;
        public byte[] packetData;

        private CustomPacket(long numberFromBuffer, byte[] arrayToPlay) {
            this.packetData = arrayToPlay;
            this.packetID = numberFromBuffer;
        }

        public long getPacketID() {
            return packetID;
        }

        public void setPacketID(long packetID) {
            this.packetID = packetID;
        }

        public byte[] getPacketData() {
            return packetData;
        }

        public void setPacketData(byte[] packetData) {
            this.packetData = packetData;
        }

        public DatagramPacket getPacket(InetAddress clientIP, int PORT) {
            return new DatagramPacket(this.packetData, this.packetData.length, clientIP, PORT);
        }

        @Override
        public int compareTo(CustomPacket o) {
            return (int) (this.packetID - o.packetID);
        }

        @Override
        public String toString() {
            return "packetID= " + packetID + ", packetData= " + packetData;
        }

    }

    class PacketComparator implements Comparator<CustomPacket> {

        @Override
        public int compare(CustomPacket customPacket1, CustomPacket customPacket2) {
            return (int) (customPacket1.getPacketID() - customPacket2.getPacketID());
        }

    }

    /**
     *
     * @param socketType
     * @param a
     * @throws SocketException
     * @throws javax.sound.sampled.LineUnavailableException
     */
    public void readySocket(SocketType socketType, char a) throws SocketException, LineUnavailableException {
        player = new AudioPlayer();

        switch (socketType) {
            case Type1:
                if (a == 's') {
                    sendingSocket = new DatagramSocket();
                } else {
                    receivingSocket = new DatagramSocket(55555);
                }
                break;
            case Type2:
                if (a == 's') {
                    sendingSocket = new DatagramSocket2();
                } else {
                    receivingSocket = new DatagramSocket2(55555);
                }
                break;
            case Type3:
                if (a == 's') {
                    sendingSocket = new DatagramSocket3();
                } else {
                    receivingSocket = new DatagramSocket3(55555);
                }
                break;
            case Type4:
                if (a == 's') {
                    sendingSocket = new DatagramSocket4();
                } else {
                    receivingSocket = new DatagramSocket4(55555);
                }
                break;

        }

    }

    public static byte[] long2ByteArray(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static long byteArray2Long(byte[] byteArray) {
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        long l = bb.getLong();
        return l;

    }

    public byte[] mergeArrays(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public byte[] giveNumberToBuffer(byte[] givenBuffer, long num) {

        byte[] packetIdentifier = long2ByteArray(num);
        byte[] mergedArray = mergeArrays(packetIdentifier, givenBuffer);

        return mergedArray;

    }

    public long getNumberFromBuffer(byte[] givenBuffer) {

        byte[] longBuffer = new byte[8];

        System.arraycopy(givenBuffer, 0, longBuffer, 0, 8);

        long packetID = byteArray2Long(longBuffer);

        return packetID;

    }

    public byte[] stripPacket(byte[] givenBuffer) {

        byte[] newArray = new byte[givenBuffer.length - 8];

        System.arraycopy(givenBuffer, 8, newArray, 0, newArray.length);

        return newArray;

    }

    ArrayList<CustomPacket> list1 = new ArrayList<CustomPacket>();
    ArrayList<CustomPacket> list2 = new ArrayList<CustomPacket>();

    /**
     * Method to fix voice issues on the receiving side.
     *
     * @param type
     * @param packet
     * @throws java.io.IOException
     */
    int frameNo = 0;
    int addedPacketTotal = 0;
    int currentMax = interleaveNumber;
    int squareRoot = (int) Math.sqrt(interleaveNumber);
    int seqNo = 0;
    int index = 1;

    CustomPacket[] sortArray = new CustomPacket[interleaveNumber];

    public void fixVoice(SocketType type, DatagramPacket packet) throws IOException {

        byte[] arrayToPlay = stripPacket(packet.getData());

        CustomPacket current = new CustomPacket(getNumberFromBuffer(packet.getData()), arrayToPlay);
        // System.out.println(current);
        int packetNumber = (int) current.getPacketID();

        byte[] empty = new byte[512];

        CustomPacket emptyPacket = new CustomPacket(0, empty);

        switch (type) {

            case Type1:
                player.playBlock(current.getPacketData());
                break;

            case Type2:

                if (expected < currentMax) {
                    expected += squareRoot;
                } else if (index < squareRoot) {
                    currentMax--;
                    expected = squareRoot + (seqNo * interleaveNumber) - index;
                    index++;
                }

                if (addedPacketTotal % interleaveNumber == 0 && addedPacketTotal > 0) {

                    seqNo++;
                    index = 1;
                    currentMax = ((seqNo + 1) * interleaveNumber);
                    expected = ((seqNo) * interleaveNumber) + squareRoot;
                    //System.out.println(Arrays.toString(sortArray));

                    for (CustomPacket pck : sortArray) {
                        
                        if(pck != null)
                        {
                            System.out.println("Playing " + pck);
                            player.playBlock(pck.packetData);
                        }
                            
                    }

                    sortArray = new CustomPacket[interleaveNumber];

                }
                //System.out.println("expected: " + expected + " - current: " + current.packetID + " added: " + addedPacketTotal + " seqNo: " + seqNo);
                // END EXPECTED CALCULATION
                int arrayIndex = expected - (seqNo * interleaveNumber) - 1;
                if (current.packetID == expected) {
                    sortArray[arrayIndex] = current;
                    addedPacketTotal++;
                } else {
                    if (sortArray[arrayIndex] == null) {

                        sortArray[arrayIndex] = emptyPacket;
                        addedPacketTotal++;

                    }
                    //System.out.println(current.packetID + " " +(seqNo * interleaveNumber) +  " " + addedPacketTotal);

                    // if (addedPacketTotal % 16 != 0 && addedPacketTotal > 0) {
                    if (current.packetID > ((seqNo + 1) * interleaveNumber) && seqNo > 0) {

                        for (CustomPacket pack : sortArray) {
                            if (pack == null) {
                                pack = emptyPacket;
                                addedPacketTotal++;

                            }
                        }

                        //PLAY
                        //seqNo++;
                        //index = 1;
                        // currentMax = ((seqNo + 1) * interleaveNumber);
                        // expected = ((seqNo) * interleaveNumber) + squareRoot;
                        //System.out.println(Arrays.toString(sortArray));
                    
                        for (CustomPacket pck : sortArray) {
                            
                            if(pck != null)
                            {
                                System.out.println("Playing " + pck);
                                player.playBlock(pck.packetData);
                            }
                              
                          
                        }

                        sortArray = new CustomPacket[interleaveNumber];

                        sortArray[(int) current.packetID - ((seqNo + 1) * interleaveNumber) - 1] = current;

                    } else {

                        sortArray[(int) current.packetID - (seqNo * interleaveNumber) - 1] = current;
                        addedPacketTotal++;
                    }

                    // }
                    /*
                     if (current.packetID > (seqNo * interleaveNumber) && seqNo > 0) {
                     for (CustomPacket pack : sortArray) {
                     if (pack == null) {
                     pack = emptyPacket;
                     }
                     }

                     //PLAY
                        
                     seqNo++;
                     index = 1;
                     currentMax = ((seqNo + 1) * interleaveNumber);
                     expected = ((seqNo) * interleaveNumber) + squareRoot;
                     //System.out.println(Arrays.toString(sortArray));
                     sortArray = new CustomPacket[interleaveNumber];

                     sortArray[(int) current.packetID - (seqNo * interleaveNumber) - 1] = current;
                     expected += squareRoot;

                     }
                     */
                }

                break;
            case Type3:

                if (!repetitionMethod) {
                    previous = emptyPacket;
                    listToSort.add(current);
                    expected++;
                    if (expected % sortInterval == 0 && expected > 0) {

                        //SORTING THE ARRAY
                        Collections.sort(listToSort, new PacketComparator());

                        //FIXING PACKET LOSS
                        for (int a = 0; a < listToSort.size(); a++) {

                            while (currentID <= listToSort.get(a).packetID) {
                                //   System.out.println("Packet :" + listToSort.get(a).packetID + " compare : " + currentID);
                                //MISSING PACKETS FIX
                                if (listToSort.get(a).packetID == currentID) {

                                    //PREVIOUS BECOMES CURRENT
                                    previous.setPacketID(currentID);

                                } else {

                                    //CURRENT BECOMES PREVIVOUS
                                    listToSort.add(a, previous);

                                }
                                //System.out.println(listToSort.get(a) + "\n");

                                currentID++;
                            }

                        }

                        //PLAYING SORTED ARRAY
                        for (CustomPacket listToSort1 : listToSort) {
                            //System.out.println("Playing :" + listToSort1.packetID);
                            player.playBlock(listToSort1.packetData);
                        }

                        //CLEARING THE LIST
                        listToSort.clear();

                    }
                } else {
                    listToSort.add(current);
                    expected++;
                    if (expected % sortInterval == 0 && expected > 0) {

                        //SORTING THE ARRAY
                        Collections.sort(listToSort, new PacketComparator());

                        //FIXING PACKET LOSS
                        for (int a = 0; a < listToSort.size(); a++) {

                            while (currentID <= listToSort.get(a).packetID) {
                                // System.out.println("Packet :" + listToSort.get(a).packetID + " compare : " + currentID);
                                //MISSING PACKETS FIX
                                if (listToSort.get(a).packetID == currentID) {

                                    //PREVIOUS BECOMES CURRENT
                                    previous = listToSort.get(a);
                                    previous.setPacketID(currentID);

                                } else {

                                    //CURRENT BECOMES PREVIVOUS
                                    listToSort.add(a, previous);

                                }
                                //System.out.println(listToSort.get(a) + "\n");

                                currentID++;
                            }

                        }

                        //PLAYING SORTED ARRAY
                        for (CustomPacket listToSort1 : listToSort) {
                            // System.out.println("Playing :" + listToSort1.packetID);
                            player.playBlock(listToSort1.packetData);
                        }

                        //CLEARING THE LIST
                        listToSort.clear();

                    }
                }

                //ADD CURRENT PACKET TO THE LIST
                break;
            case Type4:
                //MISSING PACKETS FIX
                previous = emptyPacket;

                if (current.packetID == expected) {

                    //PREVIOUS BECOMES CURRENT
                    previous.setPacketID(expected);

                } else {
                    //CURRENT BECOMES PREVIVOUS

                    current = previous;

                }

                //PLAY CURRENT
                player.playBlock(current.getPacketData());

                expected++;

                break;

        }

    }

    /**
     *
     * @param PORT
     * @param buffer
     * @param clientIP
     * @param number
     * @throws IOException
     */
    public void TransmitVoice(int PORT, byte[] buffer, InetAddress clientIP, int number) throws IOException {

        if (interleave && type == SocketType.Type2) {

            buffer = giveNumberToBuffer(buffer, number);

            CustomPacket packetToSend = new CustomPacket(getNumberFromBuffer(buffer), buffer);

            // System.out.println(packetToSend);
            packetsToSend.add(packetToSend);

            if ((number % interleaveNumber == 0) && interleave) {

                packetsToSend = interleavePackets(packetsToSend);

                //SENDING LIST
                for (CustomPacket packetsToSend1 : packetsToSend) {

                    DatagramPacket packet = packetsToSend1.getPacket(clientIP, PORT);
                    // System.out.println(packetsToSend1.toString());
                    if (packetToSend.getPacketID() != 0) {
                        sendingSocket.send(packet);
                    }
                }
                //System.out.println();
                packetsToSend.clear();

            }

        } else {

            buffer = giveNumberToBuffer(buffer, number);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientIP, PORT);

            sendingSocket.send(packet);

        }

    }

    /**
     *
     * @param buffer
     * @param bufferSize
     * @throws IOException
     */
    int expected = 0;
    int error = 0;
    int sortInterval = 10;
    int currentID = 0;
    ArrayList<CustomPacket> listToSort = new ArrayList<CustomPacket>();

    CustomPacket previous = new CustomPacket(0, new byte[512]);

    public void ReceiveVoice(byte[] buffer, int bufferSize) throws IOException {

        DatagramPacket packet = new DatagramPacket(buffer, 0, bufferSize);

        receivingSocket.receive(packet);

        fixVoice(type, packet);

        ///////////////////////////////
        /*TEST
        
         if(nm != getNumberFromBuffer(packet.getData()))
         { 
         error++;
         System.out.println("Packet No: " + getNumberFromBuffer(packet.getData()) + " compare: " + nm);
       
         System.err.println("ERROR IN THE PACKET, error no: " + error );
           
         }
         */
        ///////////////////////////////
    }

}
