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


    public void initialize() {
        setSize(300, 300);
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        checkBox1.addItemListener(e -> {
            boolean checked = e.getStateChange() == ItemEvent.SELECTED;
            refreshList(checked);
        });

        comboBox.setEditable(true);
        refreshList(false);

        comboBox.addActionListener(event -> {
            //
            // Get the source of the component, which is our combo
            // box.
            //
            JComboBox comboBox = (JComboBox) event.getSource();

            Object selected = comboBox.getSelectedItem();
            if (selected == null) { // Ignore checkbox event
                return;
            }
            boolean isAvailable = IntentionHandler.Companion.checkSelectedIntentionByName(selected.toString());
            textField.setText(String.valueOf(isAvailable));

        });

        applySequenceButton.addActionListener(event -> {
            SequentialApplier applier = new SequentialApplier();

            applier.start(); // Build intentions tree
            applier.dumpHashMap("noname", "out");
            new IntentionListToDot().process(applier.getEvents(), "noname", "out");
        });

        applyToFileButton.addActionListener(event -> {
            FileApplier applier = new FileApplier();
            applier.start();
        });
        setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void refreshList(boolean onlyAvailable) {
        comboBox.removeAllItems();
        for (String actionName : IntentionHandler.Companion.getIntentionsList(onlyAvailable)) {
            comboBox.addItem(actionName);
        }
    }

}