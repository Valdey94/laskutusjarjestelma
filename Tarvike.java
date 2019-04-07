// Tarviketta edustava luokka.
public class Tarvike {
    private String nimike;
    private int yksikkomaara;
    private double alennuspros;
    
    public Tarvike(String n, int y, double a) {
        nimike(n);
        yksikkomaara(y);
        alennuspros(a);
    }

    public String nimike() { return nimike; }
    public void nimike(String n) {
        if (n != null && n.length() > 0) {
            nimike = n;
        }
    }
  
    public int yksikkomaara() { return yksikkomaara; }
    public void yksikkomaara(int y) {
        if (y > 0) {
            yksikkomaara = y;
        }
    }
    
    public double alennuspros() { return alennuspros; }
    public void alennuspros(double a) {
        if (a > 0) {
            alennuspros = a;
        }
    }
}