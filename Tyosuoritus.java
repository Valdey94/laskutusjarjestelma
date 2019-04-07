// Tyosuoritusta mallintava luokka.
import java.util.ArrayList;
public class Tyosuoritus {
    private String tyyppi;
    private int hinta;
    private int kesto;
    private double alennuspros;
    private int tkNro;
    private int usNro;
    public ArrayList<Tarvike> tarvikkeet;

    public Tyosuoritus(char t, int k, double a) {
        tyyppi(t);
        kesto(k);
        alennuspros(a);
        this.tarvikkeet = new ArrayList<Tarvike>();
    }
    
    public String tyyppi() { return tyyppi; }
    // Työsuorituksen hinta asetetaan samalla kun määrätään tyyppi.
    public void tyyppi(char t) {
        if (t == 'S' || t == 's') {
            tyyppi = "suunnittelu";
            hinta = 55;
        }
        else if (t == 'T' || t == 't') {
            tyyppi = "tyo";
            hinta = 45;
        }
        else if (t == 'A' || t == 'a') {
            tyyppi = "aputyo";
            hinta = 35;
        }
    }
    
    public int hinta() { return hinta; }
    
    public int kesto() { return kesto; }
    public void kesto(int k) {
        if (k > 0)
            kesto = k;
    }
    
    public double alennuspros() { return alennuspros; }
    public void alennuspros(double a) {
        if (a >= 0)
            alennuspros = a;
    }   
    
    public ArrayList tarvikkeet() { return tarvikkeet; }
}