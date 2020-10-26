import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class PopUpForm extends JFrame {
    private JComboBox<String> comboBox;
    private JTextField textField;
    private JPanel panel1;
    private JCheckBox checkBox1;
    private JButton applySequenceButton;
    private JButton applyToFileButton;
    private JButton applyToPathButton;


    public void initialize(AnActionEvent e) {
        setSize(300, 300);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CurrentPositionHandler handler = new CurrentPositionHandler(e); // Save current editor state etc

        checkBox1.addItemListener(event -> {
            boolean checked = event.getStateChange() == ItemEvent.SELECTED;
            refreshList(checked, e);
        });

        comboBox.setEditable(true);
        refreshList(false, e);

        applyToPathButton.addActionListener(event -> {
            PathApplier applier = new PathApplier(handler);
            String path = textField.getText();
            applier.start(path);
        });

        applySequenceButton.addActionListener(event -> {
            SequentialApplier applier = new SequentialApplier(handler);

            if (applier.start(0, 20)) { // Build intentions tree
                applier.dumpHashMap("noname", "out");
                new IntentionListToDot().process(applier.getEvents(), "noname", "out");
            }
        });

        applyToFileButton.addActionListener(event -> {
            FileApplier applier = new FileApplier(handler);
            applier.start();
        });
        setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void refreshList(boolean onlyAvailable, AnActionEvent e) {
        comboBox.removeAllItems();
        for (IntentionAction intention : new CurrentPositionHandler(e).getIntentionsList(onlyAvailable)) {
            comboBox.addItem(intention.getFamilyName());
        }
    }

}