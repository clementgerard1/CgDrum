import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.cycling74.max.*;
import com.cycling74.msp.*;

public class CgDrum extends MSPObject{
	
	private Method _perform;
	private float memoire;
	private String filePath;
	public int idTps;
	public ArrayList<Ligne> lignes;
	public CgDrum classe;
	public MaxPatcher LignesPatcher;
	public MaxBox plusButton;
	public float actual;
	public boolean actualSolo;
	
	public CgDrum(){
		this.filePath = this.getParentPatcher().getPath();
		this.classe = this;
		declareInlets( new int[] { SIGNAL, DataTypes.ANYTHING } );
	    declareOutlets( new int[] { DataTypes.INT, DataTypes.FLOAT, DataTypes.ANYTHING, DataTypes.INT, DataTypes.INT } );
	    this.lignes = new ArrayList<Ligne>();
	    this.memoire = 0;
	    this.idTps = 0;
	    this.actualSolo = false;
		this._perform = getPerformMethod("perform");
	}
	
	public int getIdTps(){
		return idTps;
	}
	
	public ArrayList<Ligne> getLignes(){
		return this.lignes;
	}
	
	public CgDrum getClasse(){
		return this.classe;
	}
	
	public MaxPatcher getLignesPatcher(){
		return this.LignesPatcher;
	}
	
	public MaxBox getPlusButton(){
		return this.plusButton;
	}
	
	public boolean getActualSolo(){
		return this.actualSolo;
	}
	
	public float getActual(){
		return this.actual;
	}
	
	public void start(){
		this.LignesPatcher = this.getParentPatcher().getNamedBox("RythmiqueBPatcher").getSubPatcher();
		this.plusButton = this.LignesPatcher.getNamedBox("RythmiquePlus");
	}

	void perform(MSPSignal[] in, MSPSignal[] out){  
		
		// On actualise la variable
		this.actual = in[0].vec[0];
		
		// On réinitialise lorsque le phasor repasse à 0
		if((in[0].vec[0] - memoire) < 0){
			for( int i = 0 ; i < lignes.size() ; i++){
				lignes.get(i).start();
			}
			this.idTps = (this.idTps + 1) % 30000;
		}
		memoire = in[0].vec[0];
		
		// On perform les différentes lignes
		for( int i = 0 ; i < lignes.size() ; i++){
			lignes.get(i).perform(in, out);
		}
	}
	
	public void savePreset(Atom[] adresse){
		String adresseFile = adresse[0].getString().replaceAll("Macintosh HD:", "");
		try {
			File fichier =  new File(String.valueOf(adresseFile.replaceAll("\\.ser",  "") + ".ser")) ;
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fichier));
			oos.writeObject(lignes) ;
			oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readPreset(Atom[] adresse){
		String adresseFile = adresse[0].getString().replaceAll("Macintosh HD:", "");
		try {
			File fichier =  new File(String.valueOf(adresseFile)) ;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier));
			try {
				lignes  = (ArrayList<Ligne>)ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for( int i = 0 ; i < lignes.size() ; i++){
				lignes.get(i).setParent(this);
				lignes.get(i).updateTpsNote();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readPresetInFolder(Atom[] adresse){
		try {
			File fichier =  new File(String.valueOf(this.filePath + "/" + adresse[0] + ".ser")) ;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fichier));
			try {
				lignes  = (ArrayList<Ligne>)ois.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for( int i = 0 ; i < lignes.size() ; i++){
				lignes.get(i).updateTpsNote();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void inlet(Atom[] ask){
		List<Atom> args = retirerAdresse(ask);
		try {			
			Method method = this.getClass().getMethod(String.valueOf(ask[1]), List.class);
			try {
				method.invoke(this, args);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addLigne(Atom[] bang){
		this.lignes.add(new Ligne(this.lignes.size(), this));
		String args[] = {"0", String.valueOf(this.lignes.size() * 60), "30", "27"};
		this.plusButton.send("patching_rect", Atom.newAtom(args));
	}
	
	public void deleteLigne(Atom[] nom){
		int id = Integer.valueOf(nom[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).delete();
		this.lignes.remove(id);
		String args[] = {"0", String.valueOf(this.lignes.size() * 60), "30", "27"};
		for( int i = id ; i < this.lignes.size(); i++){
			this.lignes.get(i).actu();
		}
		this.plusButton.send("patching_rect", Atom.newAtom(args));
	}
	
	public static List<Atom> retirerAdresse(Atom[] args){
		List<Atom> resultats = new ArrayList<Atom>();
		for( int i = 2 ; i < args.length ; i++){
			resultats.add(args[i]);
		}	
		return resultats;		
	}
	
	public void setNbDiv(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setNbDiv(args[1].toInt());		
	}
	
	public void setNbTps(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setNbTps(args[1].toInt());
		/*for( int i = 0 ; i < this.lignes.get(id).notes.size(); i ++ ){
			if(this.lignes.get(id).notes.get(i).getStart() >= args[1].toInt()){
				this.lignes.get(id).notes.remove(i);
			}
		}*/
	}

	public void reconstruct(Atom[] args){
		MaxBox[] destruct = this.LignesPatcher.getAllBoxes();
		for(int i = 0 ; i < destruct.length ; i++){
			if(destruct[i].getMaxClass().equals("jpatcher")){
				destruct[i].remove();
			}
		}
		
		this.idTps = 0;
		for(int i = 0 ; i < lignes.size() ; i++){
			this.lignes.get(i).reconstruct(this);
		}
		String args2[] = {"0", String.valueOf(this.lignes.size() * 60), "30", "27"};
		this.plusButton.send("patching_rect", Atom.newAtom(args2));
	}
	
	public void setParam(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).notes.get(args[4].toInt()).setStart(args[3].toDouble());
		this.lignes.get(id).notes.get(args[4].toInt()).setParam(args[1].toString(), args[2].toDouble(), args[5].toDouble(),args[6].toDouble(), args[7].toInt());
	}
	
	public void setMuet(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		if(args[1].toInt() == 1){
			this.lignes.get(id).setMuet(true);
		}else{
			this.lignes.get(id).setMuet(false);
		}
		this.verifAllSolo();
	}
	
	public void setSolo(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		if(args[1].toInt() == 1){
			this.actualSolo = true;
			this.lignes.get(id).setMuet(false);
			for(int i = 0 ; i < this.lignes.size() ; i++){
				if(i != id){
					this.lignes.get(i).setMuet(true);
				}
			}
		}else{
			this.actualSolo = false;
			this.lignes.get(id).setMuet(false);
			for(int i = 0 ; i < this.lignes.size() ; i++){
				if(i != id){
					this.lignes.get(i).setMuet(false);
				}
			}
		}
		this.verifAllSolo();
	}
	
	public void verifAllSolo(){
		int count = 0;
		for(int i = 0; i < this.lignes.size() ; i++ ){
			if(!this.lignes.get(i).getMuet()){
				count++;
			}
		}
		if( count == this.lignes.size()){
			this.actualSolo = false;
			for(int i = 0; i < this.lignes.size() ; i++ ){
				this.lignes.get(i).setMuet(false);
			}
		}
	}
	
	public void setNote(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setNote(args[1].toInt());
	}
	
	public void setActual(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setActual(args[1].toString());
	}
	
	public void setCanal(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setCanal(args[1].toInt());
	}
	
	public void setMaster(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).setMaster(args[1].toFloat());
	}
	
	public void addParam(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).addParam(args[1].toString());
	}
	
	public void deleteParam(Atom[] args){
		int id = Integer.valueOf(args[0].toString().replaceAll("ligne", ""));
		this.lignes.get(id).deleteParam(args[1].toString());
	}
	
	public void clear(Atom[] args){
		for( int i = this.lignes.size() - 1 ; i >= 0 ; i--){
			Atom[] args2 = {Atom.newAtom("ligne" + i)};
			this.deleteLigne(args2);
		}
	}
	
	@Override
	public Method dsp(MSPSignal[] arg0, MSPSignal[] arg1) {
		return _perform;
	}
	
}
