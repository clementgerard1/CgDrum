import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

import com.cycling74.max.MaxObject;

public class Note implements Comparable<Note>, Serializable{

	private double start;
	private LinkedHashMap<String, Parametre> params;
	private boolean etat;
	
	public Note(double start, int velocity){
		params = new LinkedHashMap<String, Parametre>();
		params.put("velocity", new Parametre());
		params.get("velocity").setValue(velocity);
		this.start = start;
		this.etat = false;
	}
	
	public double getVelocity(){
		return params.get("velocity").getValue();
	}
	
	public double getStart(){
		return this.start;
	}
	
	public void setStart(double start){
		this.start = start;
	}
	
	public void setEtat(boolean etat){
		this.etat = etat;
	}
	
	public void addParametre(String name, double defaultValue){
		params.put(name, new Parametre(defaultValue));
	}
	
	public void deleteParametre(String name){
		params.remove(name);
	}
	
	public double getParametre(String name){
		return params.get(name).getValue();
	}
	
	public double getParametreMulti(String name){
		int i = 0;
		for(String parad : params.keySet()){
			
			i++;
		}
		return params.get(name).getValueMulti();
	}
	
	public double getEtape(String name){
		return params.get(name).getEtape();
	}
	
	public double getMin(String name){
		return params.get(name).getMin();
	}
	
	public double getMax(String name){
		return params.get(name).getMax();
	}
	
	public void setParam(String param, double value, double min, double max, int etape){
		params.get(param).setMin(min);
		params.get(param).setMax(max);
		params.get(param).setEtape(etape);
		params.get(param).setValue(value);
		if(param.equals("velocity")){
			if(params.get("velocity").getValue() > 0){
				setEtat(true);
			}else{
				setEtat(false);
			}
		}
	}
	
	public LinkedHashMap<String, Double> getParams(){
		LinkedHashMap<String, Double> retour = new LinkedHashMap<String, Double>();
		Set<String> e = params.keySet();
		for(String elem : e){
			retour.put(elem, params.get(elem).getValue());
		}
		return retour;
	}
	
	public boolean getEtat(){
		return this.etat;
	}

	@Override
	public int compareTo(Note o) {
		if(this.start < o.getStart()){
			return -1;
		}else if(this.start > o.getStart()){
			return 1;
		}else{
			return 0;
		}
	}
	
}
