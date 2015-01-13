import gnu.io.CommPortIdentifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by awernick on 1/13/15.
 */
public class SandStormInit extends JFrame implements PropertyChangeListener {
    private SandStormRead ssr;
    private Container contentPane;
    private JTextArea serialOutput;
    private JScrollPane serialOutputScrollPane;
    private JPanel mapPanel;
    private JPanel serialInfoPanel;
    private JComboBox commPortComboBox;
    private ArrayList<String> commPortsList;


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

        serialInfoPanel = new JPanel();
        mapPanel = new JPanel();


        serialOutput = new JTextArea(5, 20);
        serialOutputScrollPane = new JScrollPane(serialOutput);
        serialOutput.setEditable(true);

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
                ssr.setCurrentPort((String)jComboBox.getSelectedItem());
            }
        });

        serialInfoPanel.add(serialOutputScrollPane);
        serialInfoPanel.add(commPortComboBox);
        contentPane.add(serialInfoPanel);

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
        if(changeEvent.getPropertyName().equals("serial input"))
        {
            serialOutput.append((String) changeEvent.getNewValue() + "\n");
        }
    }
}
