/**
 * Created by awernick on 1/8/15.
 */

import gnu.io.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;


public class SandStormRead<T> implements SerialPortEventListener
{
    public static final int BAUD_RATE = 115200;  //Arduino Serial Baud Rate
    public static final int TIME_OUT = 2000;  //Serial Timeout

    public SerialPort serialPort;
    public CommPortIdentifier currentPortID;

    public ArrayList<PropertyChangeListener> listeners; //Listeners waiting for events
    public BufferedReader input; //Arduino Serial input
    public OutputStream output; //Arduino Serial output
    public PrintWriter sensorOutput; //CSV sensor info writer

    public String inputLine;

    public static boolean waitingForHeartbeat = false;
    public long lastHeartBeat = 0;
    public int status = 0;


    public SandStormRead()
    {
        listeners = new ArrayList<PropertyChangeListener>();
        currentPortID = null;
        inputLine = new String();
    }

    public void initialize()
    {
        if (currentPortID == null)
        {
            System.out.println("Could not open serial port");
            return;
        }

        try
        {
            serialPort = (SerialPort) currentPortID.open(this.getClass().getName(), TIME_OUT);

            serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            //Open serial input stream
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

            //Establish serial output stream to Arduino
            output = serialPort.getOutputStream();

            //Enabled to notify when there's new data in the input stream
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);

            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    pollSandStorm();
                }
            };
            timer.schedule(task, 0, 10000);

            //Open CSV file
            try { sensorOutput = new PrintWriter(new BufferedWriter(new FileWriter("myfile.txt", true))); }

            catch (IOException e)
            {
                //exception handling left as an exercise for the reader
            }

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Closing..");
        }
    }


    public synchronized void closeSerial()
    {
        if (serialPort != null)
        {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }



    /************************ SandStorm Commands ************************/

    public void recordHeartBeat(long heartBeat)
    {
        lastHeartBeat = heartBeat;
        waitingForHeartbeat = false;
        System.out.println("Recieved Tock");
    }

    public void pollSandStorm()
    {
        try
        {
            String serialMessage = "tick";
            output.write(serialMessage.getBytes());
            waitingForHeartbeat = true;
            System.out.println("Sending Tick");
        }

        catch (IOException e)
        {
            System.err.println(e.toString());
        }
    }

    public void writeSensorInfo(String sensorInfo) {
        sensorOutput.println(sensorInfo);
    }



    /************************ COM PORTS ************************/

    public ArrayList<String> getCommPorts()
    {
        ArrayList<String> temp = new ArrayList<String>();
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements())
        {
            CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();
            temp.add(currentPortID.getName());
        }

        return temp;
    }

    public void setCurrentPort(String commPort)
    {
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while (portEnum.hasMoreElements())
        {
            CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();

            if (currentPortID.getName().equals(commPort))
            {
                this.currentPortID = currentPortID;
                initialize();
                return;
            }
        }
    }



    /************************ Serial Events ************************/

    public void addListener(PropertyChangeListener observer)
    {
        listeners.add(observer);
    }


    public void notifyListeners(Object object, String propertyName, Object oldValue, Object newValue)
    {
        for (PropertyChangeListener listener : listeners)
        {
            listener.propertyChange(new PropertyChangeListener.PropertyChangeEvent(object, propertyName, oldValue, newValue));
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {
        if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try
            {
                String temp = input.readLine();
                notifyListeners(this, "serial input", inputLine, temp);
                inputLine = temp;

                if(inputLine.equals("tock"))
                {
                    long tempMillis = System.currentTimeMillis();
                    System.out.println("tock detected!");
                    notifyListeners(this, "tock event", new Long(lastHeartBeat), tempMillis);
                    recordHeartBeat(tempMillis);
                }
            }

            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        else if (serialPortEvent.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY)
        {
            System.out.println("Closing");
            sensorOutput.close();
        }
    }
}
