package vue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import controleur.DataModele;
import controleur.IControleurC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import model.audio.IFourierFacile;
import model.music.Note;

public class VueTableView extends IVueTask {

	private IVueA vueMere;
	private IControleurC controleur;
	private BorderPane root;

	@FXML
	private TableColumn<CellData, String> tableColumn1;

	@FXML
	private TableColumn<CellData, String> tableColumn2;

	@FXML
	private TableView<CellData> tableView;

	@FXML
	private Button btnStart;

	private ChoiceBox choiceBox1;
	private ChoiceBox choiceBox2;
	private long currentTime;
	private int frameTick;
	private boolean start;

	public class CellData {
		private double amplitudeRaw;
		private double freqSommet;
		private double freqFond;
		private double amplitudeDb;
		private double temps;
		private Note note;
		
		public CellData(double pTemps, double pAmplitudeRaw, double pAmplitudeDb, double pFreqSommet,
				double pFreqFond, Note pNote) {
			setTemps(pTemps);
			setAmplitudeRaw(pAmplitudeRaw);

			setAmplitudeDb(pAmplitudeDb);
			setFreqSommet(pFreqSommet);
			setFreqFond(pFreqFond);
			setNote(pNote);
		}

		public double getAmplitudeRaw() {
			return amplitudeRaw;
		}

		public void setAmplitudeRaw(double amplitudeRaw) {
			this.amplitudeRaw = amplitudeRaw;
		}

		public double getFreqSommet() {
			return freqSommet;
		}

		public void setFreqSommet(double freqSommet) {
			this.freqSommet = freqSommet;
		}

		public double getFreqFond() {
			return freqFond;
		}

		public void setFreqFond(double freqFond) {
			this.freqFond = freqFond;
		}

		public double getAmplitudeDb() {
			return amplitudeDb;
		}

		public void setAmplitudeDb(double amplitudeDb) {
			this.amplitudeDb = amplitudeDb;
		}

		public double getTemps() {
			return temps;
		}

		public void setTemps(double temps) {
			this.temps = temps;
		}

		public Note getNote() {
			return note;
		}

		public void setNote(Note note) {
			this.note = note;
		}

	}

	private static enum cfEnum {
		temps("t(s)"), amplitudeRaw("Amplitude (W/m²)"), amplitudeDb("Amplitude (dB)"),
		freqSommet("Fréquence sommet (Hz)"), freqFond("Fréquence fondamentale (Hz)"), note("Note jouée (piano)");

		private String abrev;

		cfEnum(String pAbrev) {
			abrev = pAbrev;
		}
	};

	public VueTableView(IVueA pVueMere, IControleurC pControleur) {
		super();

		vueMere = pVueMere;
		controleur = pControleur;
		wndptr.setOnCloseRequest(e -> {
			close = true;
			vueMere.disposeWindow(this);
		});

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("VueTableView.fxml"));
			loader.setController(this);
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		start = false;
		wndptr.setScene(new Scene(root));
		wndptr.initOwner(vueMere.getScene().getWindow());
		wndptr.setTitle("Tableau série temporelle");
		wndptr.show();
	}

	@FXML
	private void initialize() {
		choiceBox1 = new ChoiceBox(FXCollections.observableArrayList(cfEnum.temps.abrev));
		choiceBox1.setPrefWidth(tableColumn1.getPrefWidth());
		choiceBox1.getSelectionModel().selectFirst();
		choiceBox2 = new ChoiceBox(FXCollections.observableArrayList(cfEnum.amplitudeRaw.abrev,
				cfEnum.amplitudeDb.abrev, cfEnum.freqSommet.abrev, cfEnum.freqFond.abrev, cfEnum.note.abrev));

		choiceBox2.setPrefWidth(tableColumn2.getPrefWidth());
		choiceBox2.getSelectionModel().selectFirst();
		choiceBox2.getSelectionModel().selectedIndexProperty().addListener((o, ov, nv) ->

		tableColumn2.setCellValueFactory(
				new PropertyValueFactory<CellData, String>(cfEnum.values()[nv.intValue() + 1].name())));

		tableColumn1.setGraphic(choiceBox1);
		tableColumn1.setCellValueFactory(new PropertyValueFactory<CellData, String>(cfEnum.temps.name()));

		tableColumn2.setGraphic(choiceBox2);
		tableColumn2.setCellValueFactory(new PropertyValueFactory<CellData, String>(cfEnum.amplitudeRaw.name()));

		tableView.setItems(FXCollections.observableArrayList(new ArrayList<CellData>()));

		currentTime = System.currentTimeMillis();
	}

	@Override
	void draw() {
		// TODO Auto-generated method stub

	}

	@Override
	void update() {
		if (start && (frameTick = (frameTick + 1) % 3) == 1) {
			double t = (System.currentTimeMillis() - currentTime) / 1000d;

			DataModele d = vueMere.getLastDataModele();
			
			float a = d.getAmplitudes()[1];

			tableView.getItems().add(new CellData(t, a, IFourierFacile.soundIntensityToDecibels(a),
					d.getPeakFrequency(), d.getHarmonicFrequency(), d.getLastNote()));
			tableView.scrollTo(tableView.getItems().size() - 1);
		}
	}

	@FXML
	void toggleStart() {
		start = !start;
		btnStart.setText(!start ? "Commencer le suivi" : "Arrêter");
	}

	@FXML
	void clear() {
		tableView.getItems().clear();
	}

	// https://stackoverflow.com/questions/29727276/how-to-copy-paste-table-cells-in-a-tableview
	@FXML
	void copy() {
		StringBuilder clipboardString = new StringBuilder();

		int prevRow = -1;

		for (int row = 0; row < tableView.getItems().size(); row++) {
			for (int col = 0; col < tableView.getColumns().size(); col++) {

				Object cell = (Object) tableView.getColumns().get(col).getCellData(row);

				// null-check: provide empty string for nulls
				if (cell == null) {
					cell = "";
				}

				// determine whether we advance in a row (tab) or a column
				// (newline).
				if (prevRow == row) {

					clipboardString.append('\t');

				} else if (prevRow != -1) {

					clipboardString.append('\n');

				}

				// create string from cell
				String text = cell.toString().replace('.', ',');

				// add new item to clipboard
				clipboardString.append(text);

				// remember previous
				prevRow = row;
			}
		}

		// create clipboard content
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(clipboardString.toString());

		// set clipboard content
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}
}
