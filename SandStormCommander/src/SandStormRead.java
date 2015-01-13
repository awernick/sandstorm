/**
 * Created by awernick on 1/8/15.
 */

import gnu.io.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;


public class SandStormRead<T> implements SerialPortEventListener
{

    public ArrayList<PropertyChangeListener> listeners;
    public CommPortIdentifier currentPortID;

    public static final int BAUD_RATE = 115200;
    public static final int TIME_OUT = 2000;
    public String inputLine;
    public BufferedReader input;
    public OutputStream output;
    PrintWriter sensorOutput;

    public static boolean waitingForHeartbeat = false;
    public long lastHeartBeat = 0;
    public int status = 0;

    SerialPort serialPort;

    private static final String PORT_NAMES[] = { "/dev/tty.usbmodem1411" };

    public SandStormRead()
    {
        listeners = new ArrayList<PropertyChangeListener>();
        currentPortID = null;
    }

    public void initialize()
    {
        if(currentPortID == null)
        {
            System.out.println("Could not open serial port");
            return;
        }

        try {
            serialPort = (SerialPort) currentPortID.open(this.getClass().getName(), TIME_OUT);

            serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            inputLine = "";

            output = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            try
            {
                sensorOutput = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true)));
            } catch (IOException e) {
                //exception handling left as an exercise for the reader
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Closing..");
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public void addListener(PropertyChangeListener observer)
    {
        listeners.add(observer);
    }

    public void notifyListeners(Object object, String propertyName, String oldValue, String newValue)
    {
        for(PropertyChangeListener listener : listeners)
        {
            listener.propertyChange(new PropertyChangeListener.PropertyChangeEvent(object, propertyName, oldValue, newValue));
        }
    }


    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if(serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                String temp = input.readLine();
                notifyListeners(this, "serial input", temp, inputLine);
                inputLine = temp;
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
        }
        else if(serialPortEvent.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
        {
            System.out.println("Closing");
            sensorOutput.close();
        }
    }

    public void recordHeartBeat(String inputLine)
    {   System.out.println(inputLine);
        System.out.println("Waiting for tock...");

        if(inputLine.equals("tock"))
        {
            lastHeartBeat = System.currentTimeMillis();
            waitingForHeartbeat = false;
            System.out.println("Recieved Tock");
        }
    }

    public void pollSandStorm()
    {
        try {
            String serialMessage = "tick\r\n";
            output.write(serialMessage.getBytes());
            waitingForHeartbeat = true;
            System.out.println("Sending Tick");

        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public void writeSensorInfo(String sensorInfo)
    {
        sensorOutput.println(sensorInfo);
    }


    public ArrayList<String> getCommPorts()
    {
        ArrayList<String> temp = new ArrayList<String>();
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while(portEnum.hasMoreElements()) {
            CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();
            temp.add(currentPortID.getName());
        }

        return temp;
    }

    public void setCurrentPort(String commPort)
    {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while(portEnum.hasMoreElements())
        {
            CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();

            if (currentPortID.getName().equals(commPort)) {
                this.currentPortID = currentPortID;
                initialize();
                return;
            }
        }
    }
}
