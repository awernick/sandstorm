/**
 * Created by awernick on 1/8/15.
 */

import gnu.io.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;


public class SandStormRead implements SerialPortEventListener
{
    public static final int BAUD_RATE = 115200;
    public static final int TIME_OUT = 2000;
    public BufferedReader input;
    public OutputStream output;

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

        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if(serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE)
        {
            try {
                String inputLine = input.readLine();
                System.out.println(inputLine);
            } catch (Exception e)
            {
                System.err.println(e.toString());
            }
        }
    }
}
