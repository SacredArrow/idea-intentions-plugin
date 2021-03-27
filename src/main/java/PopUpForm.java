import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import graph.Graph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;

public class PopUpForm extends JFrame {
    private JComboBox<String> comboBox;
    private JTextField textField;
    private JPanel panel1;
    private JCheckBox checkBox1;
    private JButton applySequenceButton;
    private JButton applyToFileButton;
    private JButton applyToPathButton;
    private JButton gatherStatisticsButton;
    private JButton extractExamplesButton;
    private JButton assertIntentionsButton;
    private JButton uniteStatisticsButton;
    private JButton labelStudioExportButton;
    private JButton testButtonButton;
    private JButton calculateMetricsButton;


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

            ProgressManager.getInstance().run(new Task.Backgroundable(handler.getProject(), "Title"){
                public void run(@NotNull ProgressIndicator progressIndicator) {

                    // start your process

                    // Set the progress bar percentage and text
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setText("Start");


                    // 50% done
                    PathApplier applier = new PathApplier(handler);
                    String path = textField.getText();
                    applier.start(path);


                    // Finished
                    progressIndicator.setFraction(1.0);
                    progressIndicator.setText("finished");

                }});

        });

        applySequenceButton.addActionListener(event -> { // Better not use this since it is old
            SequentialApplier applier = new SequentialApplier(handler);

//            if (applier.start(0, 20)) { // Build intentions tree
            applier.start(0,20);
            applier.dumpHashMap("noname", "", "out");
            new IntentionListToDot().process(applier.getEvents(), "noname", "out");
        });

        // For each processed file creates csv with number of nodes in tree
        gatherStatisticsButton.addActionListener(event -> new StatisticsGatherer().gather(textField.getText()));
        // Combine all "stat2" files
        uniteStatisticsButton.addActionListener(event -> new StatisticsGatherer().unite());
        // Gets intentions examples (like in settings)
        extractExamplesButton.addActionListener(event -> new ExamplesExtractor().start());
        // Checks whether intentions work on above-mentioned examples
        assertIntentionsButton.addActionListener(event -> new IntentionsAsserter(handler).start());
        // Makes json file for LabelStudio from processed files
        labelStudioExportButton.addActionListener(event -> LabelStudioExporter.INSTANCE.export());
        // Calculate metrics and export them to csv
        calculateMetricsButton.addActionListener(event -> new MetricsCalculator().calculate());

        // Testing purposes
        testButtonButton.addActionListener(event -> {
//            Graph graph = new Graph();
//            graph.build(new File(textField.getText()));
//            graph.bfs(false);
//            PsiFile psiFile = PsiFileFactory.getInstance(handler.getProject()).createFileFromText(JavaLanguage.INSTANCE, "Ololasdllasdlo"); // Make dumb file so the original is not changed
//            PsiDocumentManager docManager = PsiDocumentManager.getInstance(handler.getProject());
//            @Nullable Document document = docManager.getDocument(psiFile);
////            Editor editor = EditorFactory.getInstance().createEditor(document, handler.getProject(), JavaFileType.INSTANCE, false);
//
//            @Nullable Editor fileOpened = FileEditorManager.getInstance(handler.getProject()).openTextEditor(new OpenFileDescriptor(handler.getProject(), psiFile.getVirtualFile(), 0), true);
//            @Nullable FileEditor selected = FileEditorManager.getInstance(handler.getProject()).getSelectedEditor();
//            FileEditor[] tmp = FileEditorManager.getInstance(handler.getProject()).getAllEditors();
//            System.out.println(tmp);
//            System.out.println(selected);
//            FileEditorManager.getInstance(handler.getProject()).closeFile(psiFile.getVirtualFile());
////            EditorFactory.getInstance().releaseEditor(fileOpened);

            ProgressManager.getInstance().run(new Task.Backgroundable(handler.getProject(), "Title"){
                public void run(@NotNull ProgressIndicator progressIndicator) {

                    // start your process

                    // Set the progress bar percentage and text
                    progressIndicator.setIndeterminate(false);
                    progressIndicator.setFraction(0.10);
                    progressIndicator.setText("90% to finish");


                    // 50% done
                    SequentialApplier applier = new SequentialApplier(handler);
                    applier.start(0, 5);
                    progressIndicator.setFraction(0.50);
                    progressIndicator.setText("50% to finish");


                    // Finished
                    progressIndicator.setFraction(1.0);
                    progressIndicator.setText("finished");

                }});

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
//            System.out.println(intention.getFamilyName());
            comboBox.addItem(intention.getFamilyName());
        }
    }

}