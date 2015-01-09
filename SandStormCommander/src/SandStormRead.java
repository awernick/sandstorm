/**
 * Created by awernick on 1/8/15.
 */

import gnu.io.*;

import java.io.*;
import java.util.Enumeration;


public class SandStormRead implements SerialPortEventListener
{
    public static final int RAW_NMAE_MODE = 0;
    public static final int HEARTBEAT_MODE = 1;
    public static final int SENSOR_INFO_MODE = 2;

    public static final int BAUD_RATE = 115200;
    public static final int TIME_OUT = 2000;

    public BufferedReader input;
    public OutputStream output;
    PrintWriter sensorOutput;

    public static boolean waitingForHeartbeat = false;
    public long lastHeartBeat = 0;
    public int status = 0;

    SerialPort serialPort;

    private static final String PORT_NAMES[] =
            {
                    "/dev/tty.usbmodem1411"
            };


    public static void main(String[] args)
    {
        SandStormRead ssr = new SandStormRead();
        ssr.initialize();

        new Thread()
        {
            public void run(){
                try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
            }
        }.start();

        System.out.println("Started");
    }

    public void initialize()
    {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        while(portEnum.hasMoreElements())
        {
            CommPortIdentifier currentPortID = (CommPortIdentifier) portEnum.nextElement();

            for(String portName : PORT_NAMES) {
                if (currentPortID.getName().equals(portName)) {
                    portId = currentPortID;
                    break;
                }
            }
        }

        if(portId == null)
        {
            System.out.println("Could not open serial port");
            return;
        }

        try {
            serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

            serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
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
            System.err.println(e.toString());
            System.out.println("Closing..");
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent)
    {


        if(serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try {
                String inputLine = input.readLine();

                if(inputLine.equals("Status: " + SENSOR_INFO_MODE))
                {
                    status = SENSOR_INFO_MODE;
                }
                else if(inputLine.equals("Status: " + HEARTBEAT_MODE))
                {
                    status = HEARTBEAT_MODE;
                }
                else if(inputLine.equals("Status: " + RAW_NMAE_MODE))
                {
                    status = RAW_NMAE_MODE;
                }

                switch (status)
                {
                    case RAW_NMAE_MODE:
                        System.out.println(inputLine);
                        break;
                    case SENSOR_INFO_MODE:
                        writeSensorInfo(inputLine);
                        break;
                    case HEARTBEAT_MODE:
                        if(waitingForHeartbeat)
                            recordHeartBeat(inputLine);
                        else
                            pollSandStorm();
                        break;
                }
            } catch (Exception e)
            {
                System.err.println(e.toString());
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
}
