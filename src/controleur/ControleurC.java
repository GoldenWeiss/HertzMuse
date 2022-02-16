package controleur;

import java.io.File;

import javafx.application.Platform;
import javafx.scene.Scene;
import model.audio.ThreadModele;
import model.music.MusicFactory;
import model.music.NoteEnum;
import vue.IVueA;
import vue.VueA;
import vue.VueS;

public class ControleurC implements IControleurC {
	private IVueA vue;
	public ThreadModele modele;
	private MusicFactory musicFactory;
	
	public ControleurC() {
		
		musicFactory = new MusicFactory(this, 100, 4, 4);
		modele = new ThreadModele(this, musicFactory);
		vue = new VueA(this);
	}


	@Override
	public Scene getScene() {
		return vue.getScene();
	}
	
	public MusicFactory getMusicFactory() {
		return musicFactory;
	}
	
	public void setKey(NoteEnum newKey)
	{
		musicFactory.getMusicSheet().transposeTo(newKey);
	}
	
	public void setTempo(int tempo)
	{
		musicFactory.setTempo(tempo);
	}
	
	public int getTempo()
	{
		return musicFactory.getTempo();
	}

	public void ecouteurModele(DataModele pDataModele) {
		Platform.runLater(()->vue.setValeur(pDataModele));
	}
	
	public boolean ecouteurTogglePlay() {
		return modele.togglePlay();
	}

	public void ecouteurFenetrage(String pSystemTitle) {
		modele.setWindowFunction(pSystemTitle);
	}

	public void ecouteurDetectionf0(String pSystemTitle) {
		modele.setPitchDetectionFunction(pSystemTitle);
		
	}

	public void ecouteurCanal(int pSystemChannel) {
		modele.setDisplayedChannel(pSystemChannel);
	}


	public void ecouteurUnites(int pUnitsIndex) {
		modele.setDisplayedUnits(pUnitsIndex);
		
	}
	public void ecouteurToggleCentrage() {
		modele.toggleCentrage();
		
	}
	public void ecouteurToggleNorm() {
		modele.toggleNorm();
		
	}
	
	@Override
	public void ecouteurDragRead(int pNewRealCurrentIndex, boolean isPrimaryButton, boolean isDragging) {
		modele.setRealCurrentIndex(pNewRealCurrentIndex, isPrimaryButton, isDragging);
	}


	@Override
	public void ecouteurLoadAudioFile(File f) {
		modele.loadMedia(f);
	}


	@Override
	public void ecouteurAjouterNote() {
		vue.ajouterNote();	
	}
	
	@Override
	public void ecouteurNouvelleFeuille() {
		vue.ecouteurNouvelleFeuille();
		
	}


	@Override
	public int ecouteurNewLayer(int pType, File pFile) {
		
		modele.newLayer(pType, pFile);
		
		return modele.getLayersSize()-1;
	}


	@Override
	public void ecouteurSetLayer(int pIndice) {
		modele.setCurrentLayerIndex(pIndice);
		
	}


	@Override
	public void ecouteurSetVolume(double pValue) {
		
		modele.setCurrentLayerVolume(pValue);
	}


	@Override
	public int ecouteurDeleteCurrentLayer() {
		modele.deleteCurrentLayer();
		return modele.getCurrentLayerIndex();
	}


	@Override
	public void ecouteurSetDetectTonguing(boolean pDetectTonguing) {
		musicFactory.setDetectTonguing(pDetectTonguing);
		
	}


	
}
