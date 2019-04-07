// Urakkatarjousta edustava luokka.
import java.util.ArrayList;
public class Urakkatarjous {
    private String hyvaksytty;
    private String osissa;
    private double urakkasumma;
    private int tkNro;
    
    public String hyvaksytty() { return hyvaksytty; }
    public void hyvaksytty(boolean h) {
        if (h)
            hyvaksytty = "kylla";
        if (!h)
            hyvaksytty = "ei";
    }       

    public String osissa() { return osissa; }
    public void osissa(boolean o) {
        if (o)
            osissa = "kylla";
        if (!o)
            osissa = "ei";
    }      
    
    public double urakkasumma() {  return urakkasumma; }
    public void urakkasumma(double u) {
        if (u >= 0)
            urakkasumma = u;
    }
    
    public int tkNro() { return tkNro; }
    public void tkNro(int t) {
        if (t >= 0)
            tkNro = t;
    }        
}