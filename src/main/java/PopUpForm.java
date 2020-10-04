import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.HashMap;

public class PopUpForm extends JFrame {
    private final HashMap<String, IntentionAction> intentionsMap = new HashMap<>(); // Used for getting action from string (change to some existing method later?)
    private JComboBox<String> comboBox;
    private JTextField textField;
    private JPanel panel1;
    private JCheckBox checkBox1;

    public PopUpForm() {
        initialize();
    }

    private void initialize() {
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
            boolean isAvailable = PluginRunAction.Companion.checkSelectedIntention(intentionsMap.get(selected.toString()));
            textField.setText(String.valueOf(isAvailable));

        });
        setContentPane(this.panel1);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void refreshList(boolean onlyAvailable) {
        comboBox.removeAllItems();
        for (IntentionAction action : new IntentionManagerImpl().getAvailableIntentionActions()) {
            // Add all or only available actions
            if (!onlyAvailable || PluginRunAction.Companion.checkSelectedIntention(action)) {
                comboBox.addItem(action.getFamilyName());
                intentionsMap.put(action.getFamilyName(), action);

            }
        }
    }

}