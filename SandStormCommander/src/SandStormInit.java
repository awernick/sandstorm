import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by awernick on 1/13/15.
 */

public class SandStormInit extends JFrame implements PropertyChangeListener
{
    private SandStormRead ssr;
    private Container contentPane;
    private JTextArea serialOutput;
    private JScrollPane serialOutputScrollPane;
    private JPanel mapPanel;
    private JPanel sensorInfoPanel;
    private JPanel commandPanel;
    private JPanel serialInfoPanel;
    private JPanel serialInfoStatusPanel;
    private JPanel mainPanel;

    private JLabel lastHeartBeatLabel;
    private JLabel latitudeLabel;
    private JLabel longitudeLabel;
    private JLabel temperatureLabel;
    private JLabel gpsTimeLabel;
    private JLabel altitudeLabel;
    private JLabel humidityLabel;


    private JComboBox commPortComboBox;
    private JComboBox baudRateComboBox;
    private ArrayList<String> commPortsList;

    private boolean buildingJPEG;
    private OutputStream jpegOutputStream;


    public static void main(String [] args)
    {
        SandStormInit ss = new SandStormInit();
    }

    public SandStormInit()
    {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ssr = new SandStormRead();
        ssr.initialize();
        ssr.addListener(this);

        contentPane = getContentPane();

        latitudeLabel = new JLabel("Latitude: ", SwingConstants.LEFT);
        longitudeLabel = new JLabel("Longitude: ", SwingConstants.LEFT);
        temperatureLabel = new JLabel("Temperature: ", SwingConstants.LEFT);
        gpsTimeLabel = new JLabel("GPS Time: ", SwingConstants.LEFT);
        altitudeLabel = new JLabel("Altitude: ", SwingConstants.LEFT);
        humidityLabel = new JLabel("Humidity: ", SwingConstants.LEFT);


        serialOutput = new JTextArea(5, 20);
        DefaultCaret caret = (DefaultCaret)serialOutput.getCaret(); //Enable auto-scroll
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        serialOutputScrollPane = new JScrollPane(serialOutput);
        serialOutput.setEditable(true);

        lastHeartBeatLabel = new JLabel("Last Heartbeat: " +  formatTime(System.currentTimeMillis()), SwingConstants.LEFT);

        commPortComboBox = new JComboBox();
        commPortComboBox.setEditable(false);
        updateCommPorts();
        commPortComboBox.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                updateCommPorts();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {

            }
        });

        commPortComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JComboBox jComboBox = (JComboBox)actionEvent.getSource();
                ssr.setCurrentPort((String) jComboBox.getSelectedItem());
            }
        });

        // Build Lower status panel (Serial Selector, Status Indicator)
        serialInfoStatusPanel = new JPanel();
        FlowLayout statusLayout = new FlowLayout(FlowLayout.RIGHT);
        serialInfoStatusPanel.setLayout(statusLayout);
        serialInfoStatusPanel.add(lastHeartBeatLabel);
        serialInfoStatusPanel.add(commPortComboBox);

        // Serial Output Panel
        serialInfoPanel = new JPanel();
        serialInfoPanel.setLayout(new BorderLayout());
        serialInfoPanel.add(serialInfoStatusPanel, BorderLayout.SOUTH);
        serialInfoPanel.add(serialOutputScrollPane, BorderLayout.CENTER);

        //Sensor Info Panel (GPS, Heartbeat, Temp, etc...)
        JPanel temp = new JPanel();
        GroupLayout groupLayout = new GroupLayout(temp);
        temp.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setVerticalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(latitudeLabel))
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(longitudeLabel))
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(temperatureLabel))
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(gpsTimeLabel))
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(altitudeLabel))
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(humidityLabel)));

        groupLayout.setHorizontalGroup(
                groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(latitudeLabel)
                                .addComponent(longitudeLabel)
                                .addComponent(temperatureLabel)
                                .addComponent(gpsTimeLabel)
                                .addComponent(altitudeLabel)
                                .addComponent(humidityLabel)));

        TitledBorder title;
        title = BorderFactory.createTitledBorder("Info");
        sensorInfoPanel = new JPanel();
        sensorInfoPanel.setLayout(new BorderLayout());
        sensorInfoPanel.add(temp , BorderLayout.CENTER);
        sensorInfoPanel.setBorder(title);

        //Map Panel
        mapPanel = new JPanel();

        //Command Panel
        commandPanel = new JPanel();
        commandPanel.setLayout(new GridLayout(0,2));
        commandPanel.add(sensorInfoPanel);
        commandPanel.add(mapPanel);

        //Main Panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(commandPanel, BorderLayout.CENTER);
        mainPanel.add(serialInfoPanel, BorderLayout.SOUTH);

        //JFrame Content Pane
        contentPane.add(mainPanel);

        this.pack();
        this.setVisible(true);
    }

    public void updateCommPorts()
    {
        commPortComboBox.removeAllItems();

        for(Object commPort : ssr.getCommPorts())
        {
            commPortComboBox.addItem(commPort);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent changeEvent)
    {

        if(changeEvent.getPropertyName().equals("tock event"))
        {
            lastHeartBeatLabel.setText("Last Heartbeat: " + formatTime((Long) changeEvent.getNewValue()));
            lastHeartBeatLabel.setHorizontalAlignment(SwingConstants.LEFT);
        }

        if(changeEvent.getPropertyName().equals("start jpeg"))
        {
            try
            {
                File yourFile = new File("hello.txt");

                if(!yourFile.exists())
                    yourFile.createNewFile();

                jpegOutputStream = new FileOutputStream(yourFile);

                buildingJPEG = true;
            }
            catch (IOException e)
            {
                System.err.println(e.toString());
            }
        }

        if(changeEvent.getPropertyName().equals("end jpeg"))
        {
            try
            {
                if(buildingJPEG)
                {
                    jpegOutputStream.flush();
                    jpegOutputStream.close();
                }

                jpegOutputStream = null;
                buildingJPEG = false;
            }
            catch (IOException e)
            {
                System.err.println(e.toString());
            }
        }

        if(changeEvent.getPropertyName().equals("serial input"))
        {
            serialOutput.append(new String((byte[])changeEvent.getNewValue()) + "\n");

            if(buildingJPEG)
            {
                try {
                    jpegOutputStream.write((byte[])changeEvent.getNewValue());
                }
                catch (IOException e)
                {
                    System.err.print(e.toString());
                    buildingJPEG = false;
                }
            }
        }
    }

    public String formatTime(long time)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
        Date resultdate = new Date(time);
        return sdf.format(resultdate);
    }
}
