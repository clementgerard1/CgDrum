import java.io.Serializable;

import com.cycling74.max.MaxObject;

public class Parametre implements Serializable{

	double value;
	double min;
	double max;
	int etape;
	
	public Parametre(){
		this.value = 0.;
		this.min = 0.;
		this.max = 1.;
		this.etape = 0;
	}
	
	public Parametre(double f){
		this.value = f;
		this.min = 0.;
		this.max = 1.;
		this.etape = 0;
	}
	
	public void setValue(double velocity){
		this.value = velocity;
	}
	
	public void setMin(double min){
		this.min = min;
	}
	
	public void setMax(double max){
		this.max = max;
	}
	
	public double getValue(){
		return (((this.value * (this.max - this.min)) + this.min));
	}
	
	public double getValueMulti(){
		if(this.etape != 0){
			return (this.value * this.etape);
		}else{
			return this.value;
		}
	}
	
	public double getMin(){
		return this.min;
	}
	
	public double getMax(){
		return this.max;
	}
	
	public void setEtape(int etape){
		this.etape = etape;
	}
	
	public double getEtape(){
		return this.etape;
	}
	
}
