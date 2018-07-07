import com.cycling74.max.Atom;
import com.cycling74.max.MaxBox;
import com.cycling74.max.MaxObject;
import com.cycling74.msp.MSPSignal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

public class Ligne implements Serializable{
	
	private int id;
	private int nbTps;
	private transient int tps;
	private int note;
	private int canal;
	private int nbDiv;
	private float master;
	private transient boolean muetSolo; 
	public ArrayList<Note> notes;
	private transient int noteActuel ;
	private transient MaxBox menu;
	private transient MaxBox canalBox;
	private transient MaxBox bpatcher;
	private transient MaxBox prependName;
	private transient MaxBox noteButton;
	private transient MaxBox divButton;
	private transient MaxBox tpsButton;
	private transient MaxBox multi;
	private transient MaxBox min;
	private transient MaxBox max;
	private transient MaxBox etape;
	private transient MaxBox etape2;
	private transient MaxBox bondo;
	private transient MaxBox counter;
	private transient MaxBox mult;	
	private transient MaxBox muet;
	private transient MaxBox solo;
	private transient MaxBox masterBox;
	private transient MaxBox  setAllBox;
	private transient CgDrum parent;

	
	public Ligne(int id, CgDrum parent){
		this.parent = parent;
		String[] args = {"notes", "@args", "ligne" + id, "#1", "@patching_rect", "0", String.valueOf(id * 60), "1500", "60", "@varname", "ligne" + id};
		this.bpatcher = parent.LignesPatcher.newDefault(0, id * 60, "bpatcher", Atom.newAtom(args));
		this.prependName = this.bpatcher.getSubPatcher().getNamedBox("RythmiquePrependSuppr");
		this.noteButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueNote");
		this.divButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueDiv");
		this.tpsButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueTps");
		this.multi = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMultiSlider");
		this.min = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMin");
		this.max = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMax");
		this.etape = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueEtape");
		this.etape2 = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueEtape2");
		this.bondo = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueBondo");
		this.counter = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueCounter");
		this.mult = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMult");
		this.muet = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMuet");
		this.solo = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueSolo");
		this.masterBox = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMaster");
		this.menu = this.bpatcher.getSubPatcher().getNamedBox("CGEMenu");
		this.canalBox = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueCanal");
		this.notes = new ArrayList<Note>();
		this.nbTps = 4;
		this.nbDiv = 4;
		updateTpsNote();
		this.id = id;
		this.noteActuel = 0;
		this.note = 60;
		this.master = 1;
		this.canal = 1;
		
		int[] args2 = {this.nbDiv, this.nbTps};
		this.bondo.send("set", Atom.newAtom(args2));
		
		for(int i = 0 ; i < (this.nbDiv * this.nbTps) ; i++){
			notes.add(new Note((double) i * (1 / (double) this.nbDiv), 0));
		}
		
		if(parent.actualSolo){
			int[] args3 = {1};
			this.muet.send("set", Atom.newAtom(args3));
			this.muetSolo = true;
		}else{
			this.muetSolo = false;
		}
		
	}
	
	public void add(double start, int velocity){
		Note t;
		notes.add(t = new Note(start, velocity));
		LinkedHashMap<String, Double> params = this.notes.get(0).getParams();
		Set<String> e = params.keySet();
		for(String elem : e){
			t.addParametre(elem, 0);
		}
		Collections.sort(notes);
	}
	
	public void start(){
		tps++;
		if(tps >= this.nbTps){			
			noteActuel = 0;
			tps = 0;
		}
		if(notes.get(0).getEtat() && tps == 0 && notes.get(0).getStart() == 0){
			if(!this.muetSolo){
				outCanal();
				out(notes.get(0).getVelocity(), notes.get(0).getParams());
			}
			noteActuel++;
		}
	}
	
	public void perform(MSPSignal[] in, MSPSignal[] out){
		if(noteActuel < notes.size()){
			if(notes.get(noteActuel).getEtat() && (in[0].vec[0] + tps) > notes.get(noteActuel).getStart()){
				if(!this.muetSolo){
					outCanal();
					out(notes.get(noteActuel).getVelocity(), notes.get(noteActuel).getParams());
					outNote();
				}
				noteActuel++;
			}
			if(!notes.get(noteActuel).getEtat() && (in[0].vec[0] + tps) > notes.get(noteActuel).getStart()){
				if(!this.muetSolo){
					outCanal();
					outParams(notes.get(noteActuel).getParams());
				}
				noteActuel++;
			}
		}
	}
	
	public void out(double velocity, LinkedHashMap<String, Double> params){
		outParams(params);
		parent.classe.outlet(1, velocity * master);	
		parent.classe.outlet(0, note);
	}
	
	public void outParams(LinkedHashMap<String, Double> params){
		parent.classe.outlet(3, note);
		Set<String> e = params.keySet();
		for(String elem : e){
			String value = elem;
			if(!value.equals("velocity")){
				parent.classe.outlet(2, value, notes.get(noteActuel).getParametre(value));
			}
		}
	}
	
	public void outNote(){
	}
	
	public void outCanal(){
		parent.classe.outlet(4, this.canal);
	}
	
	public void actu(){
		this.id--;
		this.bpatcher.setName("ligne" + this.id);
		String[] args = {"ligne" + this.id};
		this.prependName.send("set", Atom.newAtom(args));
		this.bpatcher.setRect(0, id * 60, 1430, (id * 60) + 60);
	}
	
	public void delete(){
		String[] args = {"delete", "ligne" + this.id };
		parent.LignesPatcher.send("script", Atom.newAtom(args));
	}

	public void setNbDiv(int nbDiv) {
		this.nbDiv = nbDiv;
		notes.clear();
		for( int i = 0; i < nbDiv * nbTps ; i++){
			add((double)((float)i / (float)nbDiv), 0);
		}
	}
	
	public void setNbTps(int nbTps){
		for(int i = (this.nbTps - 1) * this.nbDiv ; i > ((nbTps-1) * this.nbDiv); i--){
			this.notes.remove(i);
		}
		for(int i = this.nbTps * this.nbDiv ; i < nbTps * this.nbDiv ; i++){
			add((double)i / (double)nbDiv, 0);
		}
		this.nbTps = nbTps;
		this.tps = parent.idTps % this.nbTps;
	}
	
	public void setNote(int note){
		this.note = note;
	}	
	
	public void setCanal(int canal){
		this.canal = canal;
	}
	
	public void addParam(String name){
		for( int i = 0 ; i < this.notes.size() ; i++){
			this.notes.get(i).addParametre(name, 0);
		}
	}
	
	public void deleteParam(String name){
		for( int i = 0 ; i < this.notes.size() ; i++){
			this.notes.get(i).deleteParametre(name);
		}
	}
	
	public void updateTpsNote(){
		this.tps = parent.idTps % this.nbTps;
		// On trouve la note la plus proche (arrondis supérieur)
		this.noteActuel = (int) ((parent.actual % 1) / (1 / (double)this.nbDiv)) + (this.tps * this.nbDiv); // -1 + 1
	}
	
	public void setActual(String name){
		Atom[] args = new Atom[notes.size() * 2];
		for( int i = 0 ; i < notes.size() ; i++){
			args[i * 2] = Atom.newAtom(i + 1);
			args[(i * 2) + 1] = Atom.newAtom(this.notes.get(i).getParametreMulti(name));
		}
		this.min.send((float)this.notes.get(0).getMin(name));
		this.max.send((float)this.notes.get(0).getMax(name));
		double nbEtape = this.notes.get(0).getEtape(name);
		if(nbEtape != 0){
			nbEtape--;
		}
		String[] args2 = {String.valueOf((int)nbEtape)};
		this.etape2.send((int)nbEtape);//, Atom.newAtom(args2));
		this.etape.send("set", Atom.newAtom(args2));
		this.multi.send("set", args);
	}
	
	public void setMuet(boolean b){
		this.muetSolo = b;
		int[] args = new int[1];
		if(b){
			args[0] = 1;
			int[] args2 = {0};
			this.solo.send("set", Atom.newAtom(args2));
		}else{
			args[0] = 0;
			if(parent.actualSolo){
				int[] args2 = {1};
				this.solo.send("set", Atom.newAtom(args2));
			}else{
				int[] args2 = {0};
				this.solo.send("set", Atom.newAtom(args2));
			}
		}
		this.muet.send("set", Atom.newAtom(args));
	}
	
	public boolean getMuet(){
		return this.muetSolo;
	}
	
	public void setMaster(float f){
		this.master = f;
	}
	
	public void setParent(CgDrum parent){
		this.parent = parent;
	}
	
	public void reconstruct(CgDrum parent){
		
		String[] args = {"notes", "@args", "ligne" + id, "#1", "@patching_rect", "0", String.valueOf(id * 60), "1430", "60", "@varname", "ligne" + id};
		this.bpatcher = parent.LignesPatcher.newDefault(0, id * 60, "bpatcher", Atom.newAtom(args));
		this.prependName = this.bpatcher.getSubPatcher().getNamedBox("RythmiquePrependSuppr");

		this.noteButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueNote");
		String[] args2 = {String.valueOf(this.note)};
		this.noteButton.send("set", Atom.newAtom(args2));
		
		this.canalBox = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueCanal");
		String[] args14 = {};
		this.canalBox.send(this.canal);
		
		this.divButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueDiv");
		String[] args3 = {String.valueOf(this.nbDiv)};
		this.divButton.send("set", Atom.newAtom(args3));
		
		this.tpsButton = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueTps");
		String[] args4 = {String.valueOf(this.nbTps)};
		this.tpsButton.send("set", Atom.newAtom(args4));
		
		this.bondo = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueBondo");
		int[] args5 = {this.nbDiv, this.nbTps};
		this.bondo.send("set", Atom.newAtom(args5));
		
		this.multi = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMultiSlider");
		this.min = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMin");
		this.max = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMax");
		this.etape = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueEtape");
		this.etape2 = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueEtape2");
		
		int[] args6 = {85, 20, 75 * this.nbTps, 40};
		this.multi.send("presentation_rect", Atom.newAtom(args6));
		
		String[] args7 = {String.valueOf(this.nbDiv * this.nbTps)};
		this.multi.send("size", Atom.newAtom(args7));
		
		Atom[] args8 = new Atom[notes.size() * 2];
		for(int i = 0 ; i < notes.size() ; i++){
			args8[i * 2] = Atom.newAtom((int)(i + 1));
			args8[(i * 2) + 1] = Atom.newAtom(notes.get(i).getVelocity());
		}
		this.multi.send("set", args8);
		
		this.mult = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMult");
		double[] args9 = {(double)1 / (double)this.nbDiv};
		this.mult.send("ft1", Atom.newAtom(args9));		
		
		this.counter = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueCounter");
		int[] args10 = {this.nbTps * this.nbDiv};
		this.counter.send("max", Atom.newAtom(args10));	
		
		String[] args11 = {this.nbDiv+""};
		this.multi.send("candycane", Atom.newAtom(args11));
		
		this.menu = this.bpatcher.getSubPatcher().getNamedBox("CGEMenu");
		this.menu.send("clear", Atom.emptyArray);
		LinkedHashMap<String, Double> params = this.notes.get(0).getParams();
		Set<String> e = params.keySet();
		for(String elem : e){
			String[] args12 = {elem};
			this.menu.send("append", Atom.newAtom(args12));
		}
		
		this.masterBox = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMaster");
		float[] args13 = {this.master};
		this.masterBox.send("set", Atom.newAtom(args13));
		
		this.setAllBox = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueAll");
		this.setAllBox.send(11 + (this.nbTps + 1) * 75);
		
		this.muet = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueMuet");
		this.solo = this.bpatcher.getSubPatcher().getNamedBox("RythmiqueSolo");
		
	}
}
