/* Tietokantaohjelmointi 2016 Harjoitustyo
 * Valtteri Ylisalo (415552)
 * Ryhma 7.
 */
import java.util.Scanner;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.*;

public class Tiko2016 {
    public static void main(String[] args) {
        System.out.println("      +----------------------------------------------------+");
        System.out.println("      | Tervetuloa kayttamaan laskutusjarjestelmaa, Seppo! |");
        System.out.println("      +----------------------------------------------------+");
        luoRelaatiot();
        Scanner sc = new Scanner(System.in);
        char komento = ' ';
        while (komento != 'S' && komento != 's') {
            System.out.println("------------------------------------------------------------------");
            System.out.println("(L)isaa tyokohde       (T)untityotallennus    (H)inta-arvio");
            System.out.println("(K)okoa tuntityolasku  (M)uistutuslasku       (P)erintalasku ");
            System.out.println("(U)rakkatarjous        (O)hje                 (S)ulje ohjelma");
            komento = sc.next().charAt(0);
            Kayttoliittyma.lueKomento(komento);
        }
    }
    
    // Muodostaa yhteyden tietokantaan ja palauttaa tarvittavat tiedot yksiulotteisena String-taulukkona.
    public static String[] muodostaYhteysTietokantaan() {
        String host = "//dbstud.sis.uta.fi";
        String database = "a618968";  // Oma kayttajatunnus
        int port = 5432;
        String driver = "jdbc:postgresql:";
        String username = "a618968";  // Oma kayttajatunnus
        String password = "salasana"; // Tietokannan salasana 
        try {
            Class.forName("org.postgresql.Driver");
            String[] tietokantatiedot = new String[3];
            tietokantatiedot[0] = driver + host + ":" + port + "/" + database;
            tietokantatiedot[1] = username;
            tietokantatiedot[2] = password;
            return tietokantatiedot;
        } 
        catch (ClassNotFoundException e) {
            System.out.println("Ajurin lataaminen ei onnistunut. Lopetetaan ohjelman suoritus");
            return null;
        }
    }
    
    // Sulkee yhteyden tietokantaan ja puhdistaa tietokantaympariston.
    public static void suljeYhteysTietokantaan(Connection con, Statement stmt, ResultSet rset) {
        try {
            if (rset != null)
                rset.close();
            if (stmt != null)
                stmt.close();
            if (con != null)
                con.close();
        }
        catch (SQLException e) {
            System.out.println("Tietokannan sulkemisessa tapahtui virhe: "+e.getMessage());
        }        
    }

    // Hakee asiakastiedot.    
    public static String[] haeAsiakastiedot(String hetu) {
        String[] asiakastiedot = new String[4];
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT etunimi, sukunimi, osoite FROM asiakas WHERE hetu = '"+hetu+"';");
            if (rset.next()) {
                asiakastiedot[0] = rset.getString("etunimi");
                asiakastiedot[1] = rset.getString("sukunimi");
                asiakastiedot[2] = rset.getString("osoite");
                asiakastiedot[3] = "OLEMASSA";
            } 
            else
                asiakastiedot[3] = "EI_OLEMASSA";
        }     
        catch (SQLException e) {
            System.out.println("Asiakastietojen haussa tietokannasta tapahtui virhe: "+e.getMessage());
        }
        return asiakastiedot;
    }
    
    // Lisaa tyokohteen jarjestelmaan (pakollinen).
    public static void lisaaTyokohde(String hetu, String etunimi, String sukunimi, String asiakasOsoite, String tyokohdeOsoite) {
        Tyokohde tyokohde = new Tyokohde(hetu, etunimi, sukunimi, asiakasOsoite, tyokohdeOsoite);
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT hetu FROM asiakas WHERE hetu = '"+hetu+"';");
            // Lisataan asiakastiedot tietokantaan jos annetun henkilon tietoja ei siella viela ollut.
            if (!rset.next())
                stmt.executeUpdate("INSERT INTO asiakas VALUES ('"+tyokohde.omistaja().hetu()+"', '"+tyokohde.omistaja().etunimi()+"', '"+tyokohde.omistaja().sukunimi()+"', '"+tyokohde.omistaja().osoite()+"');");
            stmt.executeUpdate("INSERT INTO tyokohde (osoite, hetu) VALUES ('"+tyokohde.osoite()+"' ,'"+tyokohde.omistaja().hetu()+"');");
            // Haetaan tyokohteen numero laskupohjalle, jota sitten taydennetaan tyosuoritustlisaysten myota.
            rset = stmt.executeQuery("SELECT tk_nro FROM tyokohde WHERE osoite = '"+tyokohdeOsoite+"';");
            //int tyokohdeNro;
            Scanner sc = new Scanner(System.in);
            System.out.println("Onko kyseessa (U)rakkatyo vai (T)untityopohjainen tyotilaus?");
            char komento = sc.next().charAt(0);
            while (komento != 'U' && komento != 'u' && komento != 'T' && komento != 't') {
                System.out.println("Valitse (U)rakkatyo tai (T)untipohjainen tyotilaus!");
                komento = sc.next().charAt(0);
            }
            String onkoUrakka = "";
            if (komento == 'T' || komento == 't')
                onkoUrakka = "ei";
            else if (komento == 'U' || komento == 'u')
                onkoUrakka = "kylla";
            if (rset.next()) {
                int tyokohdeNro = rset.getInt("tk_nro");          
                stmt.executeUpdate("INSERT INTO lasku (l_nro, monesko, loppusumma, alv_osuus, urakka, ktv, tk_nro) VALUES ('"+tyokohdeNro+"', '1', '0', '0', '"+onkoUrakka+"', '20', '"+tyokohdeNro+"');");            
            }
            con.commit();
            System.out.println("Tyokohteen lisays onnistui!");
        }
        catch (SQLException e) {
            System.out.println("Tyokohteen lisays ei onnistunut: "+e.getMessage());
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        } 
    }
    
    // Tallentaa tuntityot ja tarviketiedot jarjestelmaan (pakollinen).
    public static void tallennaTuntityot(int tyokohdeNro, ArrayList tyosuorituslista) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            con.setAutoCommit(false);
            stmt = con.createStatement();
            // Tallennetaan tyosuoritukset.
            for (int i = 0; i < tyosuorituslista.size(); i++) {
                Tyosuoritus tyosuoritus = (Tyosuoritus)tyosuorituslista.get(i);
                stmt.executeUpdate("INSERT INTO tyosuoritus (tyyppi, hinta, kesto, alennuspros, tk_nro) VALUES ('"+tyosuoritus.tyyppi()+"' ,'"+
                                               tyosuoritus.hinta()+"', '"+tyosuoritus.kesto()+"', '"+
                                               tyosuoritus.alennuspros()+"', '"+tyokohdeNro+"');");
                con.commit();
                con.setAutoCommit(false);
                // Haetaan asken lisatty tyosuoritus tietokannasta, jotta saadaan sen ts_nro.
                rset = stmt.executeQuery("SELECT ts_nro FROM tyosuoritus ORDER BY ts_nro DESC LIMIT 1;");
                if (rset.next()) {
                    int tyosuoritusNro = rset.getInt("ts_nro");
                    for (int j = 0; j < tyosuoritus.tarvikkeet().size(); j++) {
                        Tarvike tarvike = (Tarvike)tyosuoritus.tarvikkeet().get(j);
                        rset = stmt.executeQuery("SELECT t_nro FROM tarvike WHERE nimike = '"+tarvike.nimike()+"';");
                        if (rset.next()) {
                            int tarvikeNro = rset.getInt("t_nro");          
                            stmt.executeUpdate("INSERT INTO kaytetaan VALUES('"+tarvikeNro+"', '"+tyosuoritusNro+"', '"+
                                                                                tarvike.yksikkomaara()+"', '"+tarvike.alennuspros()+"');");
                            stmt.executeUpdate("UPDATE tarvike SET varastotilanne = varastotilanne - "+tarvike.yksikkomaara()+" WHERE t_nro = "+tarvikeNro+";");
                            con.commit();
                            con.setAutoCommit(false);
                        }
                    }                         
                }                    
            }
            double verollinenHinta = 0;
            double alvOsuus = 0;
            // Ensin etsitaan kaikki kohteen tyosuoritukset joihin liittyi yksi tai useampi tarvike.
            rset = stmt.executeQuery("SELECT hinta, kesto, SUM(myyntihinta * tarvike.alv / 100) AS alv_osuus, tyosuoritus.alennuspros AS tyoalennus, SUM(myyntihinta * (100 - kaytetaan.alennuspros) / 100 * kaytetaan.maara) AS tarvikehinta "+
                                     "FROM tyosuoritus, kaytetaan, tarvike, tyokohde "+
                                     "WHERE tyosuoritus.tk_nro = "+tyokohdeNro+" AND kaytetaan.t_nro = tarvike.t_nro"+
                                     " AND kaytetaan.ts_nro = tyosuoritus.ts_nro"+" AND tyosuoritus.tk_nro = tyokohde.tk_nro"+
                                     " GROUP BY tyosuoritus.ts_nro;");
            
            while (rset.next()) {  
                double suorituksenHinta = rset.getInt("hinta");
                // Suorituksen hinta on kerrottava sen tuntimaaralla, jotta saadaan oikea luku.
                suorituksenHinta = (suorituksenHinta * rset.getInt("kesto") * (100 - rset.getDouble("tyoalennus")) / 100); // 45
                alvOsuus = alvOsuus + suorituksenHinta * 0.24;
                suorituksenHinta += rset.getDouble("tarvikehinta");
                alvOsuus += rset.getDouble("alv_osuus");
                verollinenHinta += suorituksenHinta;
            }
            // Sitten etsitaan kaikki kohteen tyosuoritukset joissa EI kaytetty tarvikkeita.
            rset = stmt.executeQuery("SELECT hinta, kesto, alennuspros FROM tyosuoritus "+
                                     "WHERE NOT EXISTS (SELECT * from kaytetaan WHERE kaytetaan.ts_nro = tyosuoritus.ts_nro) "+ 
                                     "AND tk_nro = "+tyokohdeNro+";");
            while (rset.next()) {
                double suorituksenHinta = rset.getInt("hinta");
                // Suorituksen hinta on kerrottava sen tuntimaaralla, jotta saadaan oikea luku.
                suorituksenHinta = (suorituksenHinta * rset.getInt("kesto") * (100 - rset.getDouble("alennuspros")) / 100);
                verollinenHinta += suorituksenHinta;
                alvOsuus = alvOsuus + (suorituksenHinta * 0.24);
            }
            // Paivitetaan laskun hinta vastaamaan lisattyjen tyosuoritusten myota kasvanutta kokonaissummaa.
            stmt.executeUpdate("UPDATE lasku SET loppusumma = "+verollinenHinta+", alv_osuus = "+alvOsuus+" WHERE lasku.tk_nro = +"+tyokohdeNro+";");
            con.commit();
            System.out.println("Tyosuoritusten lisays onnistui!");
        }
        catch (SQLException e) {
            System.out.println("Tyosuoritusten lisays ei onnistunut: "+e.getMessage());
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }     
    }
    
    // Muodostaa hinta-arvion kohteeseen x (pakollinen).
    public static void muodostaHintaArvio(int tyokohdeNro) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT osoite, loppusumma FROM lasku, tyokohde "+
                                     "WHERE lasku.tk_nro = tyokohde.tk_nro AND tyokohde.tk_nro = "+tyokohdeNro+";");
            if (rset.next())
                System.out.println("Tyokohteen '"+rset.getString("osoite")+"' hinta-arvio on "+rset.getDouble("loppusumma")+" euroa.");
            else
                System.out.println("Antamasi tyokohteen osoitetta ei loydy tietokannasta!");
        }
        catch (SQLException e) {
            System.out.println("Hinta-arvion muodostus ei onnistunut: "+e.getMessage());
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        } 
    }
    
    // Kokoaa tuntityolaskun (pakollinen) ja lahettaa sen asiakkaalle.
    public static void kokoaTuntityolasku(int laskuNro, int monesko) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT etunimi, sukunimi, asiakas.hetu AS hetu, asiakas.osoite AS asiakkaan_osoite, tyokohde.osoite AS tyokohteen_osoite, loppusumma, ktv, monesko, alv_osuus "+
                                     "FROM asiakas, tyokohde, lasku "+
                                     "WHERE tyokohde.hetu = asiakas.hetu AND lasku.tk_nro = tyokohde.tk_nro "+
                                     "AND lasku.l_nro = "+laskuNro+" AND monesko = "+monesko+" AND urakka = 'ei';");
            if (rset.next()) {
                System.out.println("+-----------------------------------------------------------------");
                if (monesko == 1)
                    System.out.println("| LASKU");
                else if (monesko == 2)
                    System.out.println("| MUISTUTUSLASKU");
                else if (monesko == 3)
                    System.out.println("| KARHULASKU");
                System.out.println("| Laskun numero: "+laskuNro);
                System.out.println("| Myyjan nimi: Seppo Tarsky");
                System.out.println("| Asiakkaan nimi: "+rset.getString("etunimi")+" "+rset.getString("sukunimi"));
                System.out.println("| Asiakkaan henkilotunnus: "+rset.getString("hetu"));
                System.out.println("| Asiakkaan osoite: "+rset.getString("asiakkaan_osoite"));
                System.out.println("| Tyokohteen osoite: "+rset.getString("tyokohteen_osoite"));
                double verollinenSumma = rset.getDouble("loppusumma");
                double alvOsuus = rset.getDouble("alv_osuus");
                double verotonSumma = verollinenSumma - alvOsuus;
                double ktvKelpoisuus = rset.getDouble("ktv");
                // Tuntierittely.
                rset = stmt.executeQuery("SELECT tyyppi, hinta, kesto, alennuspros, monesko FROM tyosuoritus, tyokohde, lasku "+
                                         "WHERE tyosuoritus.tk_nro = tyokohde.tk_nro "+
                                         "AND lasku.tk_nro = tyokohde.tk_nro AND l_nro = "+laskuNro+" AND monesko = "+monesko+";");                                      
                while(rset.next())
                    System.out.println("| Tyosuoritus: "+rset.getString("tyyppi")+", "+rset.getInt("hinta")+"e x "+rset.getInt("kesto")+
                                       "h, ale-% "+rset.getDouble("alennuspros")+"%, alv-% 24,00");       
                // Tarvike-erittely.
                rset = stmt.executeQuery("SELECT nimike, myyntihinta, tarvike.alv AS tarvikealv, maara, kaytetaan.alennuspros AS tarvikealennus, monesko "+
                                         "FROM tyosuoritus, kaytetaan, tarvike, tyokohde, lasku "+
                                         "WHERE kaytetaan.t_nro = tarvike.t_nro AND tyosuoritus.tk_nro = tyokohde.tk_nro"+
                                         " AND kaytetaan.ts_nro = tyosuoritus.ts_nro AND lasku.tk_nro = tyokohde.tk_nro "+
                                         " AND l_nro = "+laskuNro+" AND monesko = "+monesko+";");
                while(rset.next())
                    System.out.println("| Tarvike: "+rset.getString("nimike")+", "+rset.getDouble("myyntihinta")+
                                       "e, "+rset.getInt("maara")+" kpl, ale-% "+rset.getDouble("tarvikealennus")+"%, alv-% "+rset.getDouble("tarvikealv"));
                if (monesko == 1)
                    System.out.println("| Yhteensa (EUR): verollinen hinta "+verollinenSumma+", veroton hinta "+verotonSumma);
                else if (monesko == 2 || monesko == 3)
                    System.out.println("| Yhteensa (EUR): verollinen hinta "+verollinenSumma+", veroton hinta "+verotonSumma+", sisaltaa laskutuslisan.");
                System.out.println("| Kotitalousvahennyskelpoisuus summasta: "+ktvKelpoisuus+"%"); 
                System.out.println("+-----------------------------------------------------------------");
                // Koottu lasku voidaan lahettaa asiakkaalle, jos sita ei ole viela lahetetty.
                rset = stmt.executeQuery("SELECT toimituspvm FROM lasku "+
                                         "WHERE l_nro = "+laskuNro+" AND monesko = "+monesko+" AND toimituspvm IS NULL;");
                if (rset.next())
                    lahetaLasku(laskuNro, false);
                else
                    System.out.println("Tama lasku on jo aiemmin lahetetty asiakkaalle.");
            }
            else
                System.out.println("Tuolle luvulle ei loytynyt olemassa olevaa tuntityolaskua!");
        }
        catch (SQLException e) {
            System.out.println("Laskun kokoaminen ei onnistunut: "+e.getMessage());
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        } 
    }
    
    // Lahettaa laskun asiakkaalle.
    public static void lahetaLasku(int laskuNro, boolean urakka) {
        try {
            Scanner sc = new Scanner(System.in);
            char komento;
            if (!urakka) {
                System.out.println("Laheta lasku asiakkaalle? (K)ylla, (E)i");
                komento = sc.next().charAt(0);
                while (komento != 'K' && komento != 'k' && komento != 'E' && komento != 'e') {
                    System.out.println("Anna (K)ylla tai (E)i!");
                    komento = sc.next().charAt(0);
                }
            }
            // Hyvaksytysta urakkatarjouksesta eli urakkasopimuksesta lahetetaan lasku heti kun se on hyvakstty.
            // Annettu urakan valmistusmispaivamaara tulee tietysti olla realistisesti arvioitu.
            else
                komento = 'K';
            if (komento == 'K' || komento == 'k') {
                if (!urakka)
                    System.out.println("Anna tuntityolaskun laskutuspaivamaara muodossa PAIVA KUUKAUSI VUOSI (esim. 3 12 2016), eli erottele valilyonneilla."); 
                else
                    System.out.println("Anna urakkatyolaskun laskutuspaivamaara muodossa PAIVA KUUKAUSI VUOSI (esim. 17 9 2016), eli erottele valilyonneilla.");
                int paiva = sc.nextInt();
                int kuukausi = sc.nextInt();
                int vuosi = sc.nextInt();
                StringBuilder toimituspvm = new StringBuilder();
                toimituspvm.append(Integer.toString(vuosi));
                if (kuukausi >= 10)
                    toimituspvm.append(Integer.toString(kuukausi));
                else if (kuukausi < 10) {
                    toimituspvm.append("0");
                    toimituspvm.append(Integer.toString(kuukausi));
                }
                if (paiva >= 10)
                    toimituspvm.append(Integer.toString(paiva));
                else if (paiva < 10) {
                    toimituspvm.append("0");
                    toimituspvm.append(Integer.toString(paiva));
                } 
                Date[] paivamaarat = new Date[2];
                paivamaarat = luoPaivamaarat(toimituspvm.toString(), paiva, kuukausi, vuosi);
                String[] tietokantatiedot = muodostaYhteysTietokantaan();
                Connection con = null;
                Statement stmt = null;
                ResultSet rset = null;
                try {
                    con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
                    con.setAutoCommit(false);
                    stmt = con.createStatement();
                    stmt.executeUpdate("UPDATE lasku SET toimituspvm = '"+paivamaarat[0]+"', erapvm = '"+paivamaarat[1]+"' WHERE l_nro = "+laskuNro+";");
                    con.commit();
                    System.out.println("Laskun lahetys onnistui!");
                }
                catch (SQLException e) {
                    System.out.println("Laskun lahetys ei onnistunut!"+e.getMessage());
                }    
                finally {
                   suljeYhteysTietokantaan(con, stmt, rset);
                }
            }
            else if (komento == 'E' || komento == 'e')
                System.out.println("Laskua ei haluttu lahettaa asiakkaalle.");  
        } 
        catch (Exception e) {
            System.out.println("Tapahtui poikkeus.");
        }
    }
	
    // Lahettaa muistutuslaskut tai perintalaskut laskuista (riippuen kumpi on valittu), joita ei ole maksettu erapaivaan
	// mennessa ja joista ei ole aiemmin lahetetty muistutus/perintalaskua.
    public static void lahetaJalkilaskut(String toimituspvm, int paiva, int kuukausi, int vuosi, int monesko) {
        int viimelasku = monesko- 1;
	    try {
            Date[] paivamaarat = new Date[2];
            paivamaarat = luoPaivamaarat(toimituspvm, paiva, kuukausi, vuosi);
            String[] tietokantatiedot = muodostaYhteysTietokantaan();
            Connection con = null;
            Statement stmt = null;
            ResultSet rset = null;
            try {
                con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
                stmt = con.createStatement();
                con.setAutoCommit(false);
                rset = stmt.executeQuery("SELECT * FROM lasku WHERE maksupvm IS NULL AND monesko = "+viimelasku+" AND erapvm < '"+paivamaarat[0]+"' AND myohassa IS NULL;");
                ArrayList<String> paivitykset = new ArrayList<String>();
                while (rset.next()) {
				    int l_nro = rset.getInt("l_nro");
                    // Laskutuslisa tulee mukaan.
					double verollinenSumma = rset.getDouble("loppusumma") + 5;
                    double alvOsuus = rset.getDouble("alv_osuus") * ((verollinenSumma + 5) / verollinenSumma);
					double kotitalousvahennys = rset.getDouble("ktv");
                    String urakka = rset.getString("urakka");
					int tkNro = rset.getInt("tk_nro");
                    String myohassa = "";
                    if (monesko == 2)
                        myohassa = "muistutettu";
                    else if (monesko == 3)
                        myohassa = "karhutettu";
                    paivitykset.add("UPDATE LASKU set myohassa='"+myohassa+"' WHERE maksupvm IS NULL AND myohassa IS NULL AND monesko = "+viimelasku+" AND erapvm < '"+paivamaarat[0]+"';");
                    paivitykset.add("INSERT INTO lasku VALUES ('"+l_nro+"', '"+monesko+"', '"+verollinenSumma+"', '"+alvOsuus+"', '"+urakka+"', '"+paivamaarat[0]+"', NULL, '16', '"+kotitalousvahennys+"', '"+paivamaarat[1]+"', NULL, '"+tkNro+"');");
                }
                for (int i = 0; i < paivitykset.size() ; i++) {
                    String paivitys = (String)paivitykset.get(i);
                    stmt.executeUpdate(paivitys);
                }
                con.commit();
                if (monesko == 2)
                    System.out.println("Muistutuslaskut lahetettiin asiakkaille!");
                else if (monesko == 3)
                    System.out.println("Karhulaskut lahetettiin asiakkaille!");
            }
			catch (SQLException e) {
                System.out.println("Jalkilaskujen lahettamisessa tapahtui virhe: "+e.getMessage());
            }
            finally {
                suljeYhteysTietokantaan(con, stmt, rset);
            }             
		}
		catch (Exception e) {
		    System.out.println("Tapahtui poikkeus.");
		}
	}

    // Muodostaa urakkatarjouksen ja mahdollisesti urakkasopimuksen, jonka lasku voidaan lahettaa asiakkaalle.
    public static void raportoiUrakkatarjous(Urakkatarjous tarjous, int[] kestotiedot, double[] alennustiedot, ArrayList<Tarvike> tarvikkeet) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
			con.setAutoCommit(false);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT etunimi, sukunimi, asiakas.hetu AS asiakashetu, asiakas.osoite AS asiakasosoite, tyokohde.osoite AS tyokohdeosoite "+
                                     "FROM asiakas, tyokohde WHERE asiakas.hetu = tyokohde.hetu AND tyokohde.tk_nro = "+tarjous.tkNro()+";");
            if (rset.next()) {
                System.out.println("+-----------------------------------------------------------------");
                System.out.println("| URAKKATARJOUS KOHTEELLE NUMERO "+tarjous.tkNro());
                System.out.println("| Asiakkaan nimi: "+rset.getString("etunimi")+" "+rset.getString("sukunimi"));
                System.out.println("| Asiakkaan henkilotunnus: "+rset.getString("asiakashetu"));
                System.out.println("| Asiakkaan osoite: "+rset.getString("asiakasosoite"));
                System.out.println("| Tyokohteen osoite: "+rset.getString("tyokohdeosoite"));
            }
            double suunnittelualennus = 100 - alennustiedot[0];
            double suunnittelusumma = 55 * kestotiedot[0] * (100 - alennustiedot[0]) / 100;
            double asennusalennus = 100 - alennustiedot[1];
            double asennustyosumma = 45 * kestotiedot[1] * (100 - alennustiedot[1]) / 100;
            double aputyoalennus = 100 - alennustiedot[2];
            double aputyosumma = 35 * kestotiedot[2] * (100 - alennustiedot[2]) / 100;
            if (kestotiedot[0] > 0)
                System.out.println("| Suunnittelutyota: 55e * "+kestotiedot[0]+"h * "+suunnittelualennus+"% = "+suunnittelusumma+", alv 24,00%");
            if (kestotiedot[1] > 0)
                System.out.println("| Asennustyota: 45e * "+kestotiedot[1]+"h * "+asennusalennus+"% = "+asennustyosumma+", alv 24,00%");
            if (kestotiedot[2] > 0)
                System.out.println("| Aputyota: 35e * "+kestotiedot[2]+"h * "+aputyoalennus+"% = "+aputyosumma+", alv 24,00%");
			ArrayList<Integer>tarvikenimiluettelo = new ArrayList<Integer>();
            ArrayList<Integer>tarvikemaaraluettelo = new ArrayList<Integer>();
            double tarvikkeidenYhteishinta = 0;
            double tarvikkeidenAlvHinta = 0;
            for (int i = 0; i < tarvikkeet.size(); i++) {
                Tarvike tarvike = tarvikkeet.get(i);
                rset = stmt.executeQuery("SELECT t_nro, myyntihinta, alv, yksikko FROM tarvike WHERE nimike = '"+tarvike.nimike()+"';");
                if (rset.next()) {
				    int tNro = rset.getInt("t_nro");
                    double alennus = 100 - tarvike.alennuspros();
                    double tarvikehinta = tarvike.yksikkomaara() * rset.getDouble("myyntihinta") * (alennus / 100);
                    tarvikkeidenAlvHinta = tarvikkeidenAlvHinta + (tarvikehinta * rset.getDouble("alv") / 100);
                    tarvikkeidenYhteishinta += tarvikehinta;
                    System.out.println("| Tarvike: "+tarvike.nimike()+" "+tarvike.yksikkomaara()+" "+rset.getString("yksikko")+
                                       ", "+rset.getDouble("myyntihinta")+"e * "+alennus+"% = "+tarvikehinta+", alv-osuus "+rset.getDouble("alv")+"%");
                    tarvikenimiluettelo.add(tNro);
                    tarvikemaaraluettelo.add(tarvike.yksikkomaara());
                    stmt.executeUpdate("UPDATE tarvike SET varastotilanne = varastotilanne - "+tarvike.yksikkomaara()+" WHERE t_nro = "+tNro+";");
				}
            }
            System.out.println("| Urakka laskutetaan osissa: "+tarjous.osissa());
            double alvitonSumma = ((suunnittelusumma + asennustyosumma + aputyosumma) * 0.24) + tarvikkeidenAlvHinta;
            double loppusumma = suunnittelusumma + asennustyosumma + aputyosumma + tarvikkeidenYhteishinta;
            double verotonHinta = loppusumma - alvitonSumma;
			tarjous.urakkasumma(loppusumma);
            System.out.println("| Urakan arvioitu verollinen hinta: "+loppusumma);
            System.out.println("| Urakan arvioitu veroton hinta: "+verotonHinta);
            System.out.println("+-----------------------------------------------------------------"); 
            Scanner sc = new Scanner(System.in);
            System.out.println("Onko asiakas hyvaksynyt urakkatarjouksen? (K)ylla, (E)i. Hyvaksytysta urakkatarjouksesta");
            System.out.println("lahetetaan lasku paivalle, jolloin arvioit urakan valmistuvan.");
            char komento = sc.next().charAt(0);
            while (komento != 'K' && komento != 'k' && komento != 'E' && komento != 'e') {
                System.out.println("Anna (K)ylla tai (E)i!");
                komento = sc.next().charAt(0);
            }
            if (komento == 'K' || komento == 'k') {
                tarjous.hyvaksytty(true);

                stmt.executeUpdate("INSERT INTO urakkatarjous (hyvaksytty, osissa, urakkasumma, tk_nro) "+
			                       "VALUES ('"+tarjous.hyvaksytty()+"', '"+tarjous.osissa()+"', '"+tarjous.urakkasumma()+"', '"+tarjous.tkNro()+"')");
                con.commit();
			    con.setAutoCommit(false);
                rset = stmt.executeQuery("SELECT ut_nro FROM urakkatarjous ORDER BY ut_nro DESC LIMIT 1;");
                int urakkatarjousNro = 0;
                if (rset.next())
                    urakkatarjousNro = rset.getInt("ut_nro");
	    		// Lisataan myos tarvikeluettelotiedot tietokantaan.
	    		for (int i = 0; i < tarvikenimiluettelo.size(); i++) {
		    	    int tarvikeNro = tarvikenimiluettelo.get(i);
                    int tarvikemaara = tarvikemaaraluettelo.get(i);
		    	    stmt.executeUpdate("INSERT INTO luettelee VALUES ('"+urakkatarjousNro+"', '"+tarvikeNro+"', '"+tarvikemaara+"');");         
		    	}

                // Lisataan urakan tyosuoritustiedot valmiiksi tietokantaan ja maaritellaan ne pohjautuvan urakkasopimukseen.
                if (kestotiedot[0] > 0)
                stmt.executeUpdate("INSERT INTO tyosuoritus (tyyppi, hinta, kesto, alennuspros, ut_nro, tk_nro) "+
                                   "VALUES ('suunnittelu', '55', '"+kestotiedot[0]+"', '"+alennustiedot[0]+"', '"+urakkatarjousNro+"', '"+tarjous.tkNro()+"');");
                if (kestotiedot[1] > 0)
                stmt.executeUpdate("INSERT INTO tyosuoritus (tyyppi, hinta, kesto, alennuspros, ut_nro, tk_nro) "+
                                   "VALUES ('suunnittelu', '45', '"+kestotiedot[1]+"', '"+alennustiedot[1]+"', '"+urakkatarjousNro+"', '"+tarjous.tkNro()+"');");
                if (kestotiedot[2] > 0)
                stmt.executeUpdate("INSERT INTO tyosuoritus (tyyppi, hinta, kesto, alennuspros, ut_nro, tk_nro) "+
                                   "VALUES ('suunnittelu', '35', '"+kestotiedot[2]+"', '"+alennustiedot[2]+"', '"+urakkatarjousNro+"', '"+tarjous.tkNro()+"');");                             
                double alvOsuus = loppusumma - verotonHinta;
                stmt.executeUpdate("UPDATE lasku SET loppusumma = "+loppusumma+", alv_osuus = "+alvOsuus+" WHERE lasku.tk_nro = "+tarjous.tkNro()+";");
                con.commit();
                lahetaLasku(tarjous.tkNro(), true);
            }
            else if (komento == 'E' || komento == 'e') {
                tarjous.hyvaksytty(false); 
                System.out.println("Tarjousta ei ole hyvaksytty, siispa urakkasopimusta ei lisatty tietokantaan.");
            }
        }
        catch (SQLException e) {
            System.out.println("Urakkatarjouksen lisays tietokantaan ei onnistunut: "+e.getMessage());
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }    
    }
   
    // Tarkistaa, loytyyko annettua tyokohdenumeroa tietokannasta.   
    public static boolean tarkistaOnkoTyokohdeOlemassa(int tkNro) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT tk_nro FROM tyokohde WHERE tk_nro = "+tkNro+";");
            if (rset.next())
                return true;
            else
                return false;
        }
        catch (SQLException e) {
            return false;
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }           
    }

    // Tarkistaa, onko annettua tarviketta kirjattu varastoon ja kuinka paljon.    
    public static boolean tarkistaTarvikkeenSaatavuus(String nimike, int maara) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT varastotilanne FROM tarvike WHERE nimike = '"+nimike+"';");
            if (rset.next()) {
                if (rset.getInt("varastotilanne") < maara && maara - rset.getInt("varastotilanne") >= 0) {
                    System.out.println("Varastossa ei ole tarpeeksi antamaasi tarviketta, yrita pienemmalla lukumaaralla.");
                    return false;
                }
                else
                    return true;
            }
            else {
                System.out.println("Antamaasi tarviketta ei ole kirjattu varastoon. Tarkista tarvikkeen nimi.");
                return false;
            }
        }
        catch (SQLException e) {
            return false;
        }    
    }   

    // Tarkistaa, onko annettu tyokohde urakkapohjainen.    
    public static boolean tarkistaOnkoUrakka(int tkNro) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {       
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT urakka FROM lasku WHERE tk_nro = "+tkNro+" AND monesko = 1;");        
            String urakka = "";           
            if (rset.next()) {
                urakka = rset.getString("urakka");
                if (urakka.equals("kylla"))
                    return true;
                else
                    return false;
            }
            else {
                System.out.println("Antamaasi tyokohdetta ei loytynyt tietokannasta.");
                return false;
            }       
        }
        catch (SQLException e) {
            return false;
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }           
    }

    // Tarkistaa, onko tyokohteesta jo lahetetty lasku (samasta kohteestai ei voi laskuttaa useasti paitsi osissa).    
    public static boolean tarkistaOnkoLaskutettu(int tkNro) {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {       
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            stmt = con.createStatement();
            rset = stmt.executeQuery("SELECT tyokohde.tk_nro FROM lasku, tyokohde "+
                                     "WHERE lasku.tk_nro = tyokohde.tk_nro AND toimituspvm IS NOT NULL "+
                                     "AND tyokohde.tk_nro = "+tkNro+" AND monesko = 1");
            if (rset.next())
                return true;
            else
                return false;
        }
        catch (SQLException e) {
            return false;
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }                    
    }
 
    // Palauttaa toimitus- ja erapaivamaaran sopivassa muodossa tietokantaan lisaysta varten. 
    public static Date[] luoPaivamaarat(String toimituspvm, int paiva, int kuukausi, int vuosi) {
        try {
            Date[] paivamaarat = new Date[2];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date parsed = sdf.parse(toimituspvm);
            paivamaarat[0] = new Date(parsed.getTime());
	        Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, paiva);
            cal.set(Calendar.MONTH, kuukausi-1);
            cal.set(Calendar.YEAR, vuosi);
            cal.add(Calendar.DATE, 14);
	        String kaksiViikkoaLisaa = (String)sdf.format(cal.getTime());
	        Date parsed2 = sdf.parse(kaksiViikkoaLisaa);
	        paivamaarat[1] = new Date(parsed2.getTime());  
            return paivamaarat;
        }
        catch (Exception e) {
            System.out.println("Paivamaarien kasittelyssa tapahtui poikkeus!");
            return null;
        } 
    }
 
    // Luo tarvittavat relaatiot ja lisaa tarvike-relaation esimerkkidataa mikali sita ei ole viela tehty. 
    public static void luoRelaatiot() {
        String[] tietokantatiedot = muodostaYhteysTietokantaan();
        Connection con = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            con = DriverManager.getConnection(tietokantatiedot[0], tietokantatiedot[1], tietokantatiedot[2]);
            con.setAutoCommit(false);
            stmt = con.createStatement();        
            DatabaseMetaData dbm = con.getMetaData();   
            ResultSet tables = dbm.getTables(null, null, "asiakas", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE asiakas(hetu CHAR(11), etunimi VARCHAR(32) NOT NULL, sukunimi VARCHAR(32) NOT NULL, osoite VARCHAR(32) NOT NULL, PRIMARY KEY (hetu));");
                con.commit();
            }
            tables = dbm.getTables(null, null, "tyokohde", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE tyokohde(tk_nro SERIAL, osoite VARCHAR(32) NOT NULL, hetu CHAR(11) NOT NULL, PRIMARY KEY (tk_nro), FOREIGN KEY (hetu) REFERENCES asiakas);");
                con.commit();
            }                
            tables = dbm.getTables(null, null, "lasku", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE lasku(l_nro INT, monesko INT, loppusumma DECIMAL(8,2), alv_osuus DECIMAL(8,2), urakka VARCHAR(5), toimituspvm DATE, maksupvm DATE, viivastyskorko DECIMAL(4,2), ktv DECIMAL(6,2), erapvm DATE, myohassa VARCHAR(12), tk_nro INT NOT NULL, PRIMARY KEY (l_nro, monesko), FOREIGN KEY (tk_nro) REFERENCES tyokohde);");
                con.commit();
            }
            tables = dbm.getTables(null, null, "tarvike", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE tarvike(t_nro SERIAL, nimike VARCHAR(32) NOT NULL, varastotilanne INT, yksikko VARCHAR(16) NOT NULL, myyntihinta DECIMAL (7,2), sisaanostohinta DECIMAL (7,2) NOT NULL, alv DECIMAL (4,2) NOT NULL, PRIMARY KEY (t_nro));");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('pistorasia', '100', 'kpl', '12.50', '10', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('sahkojohto', '1000', 'm', '5.00', '4.00', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('kaapelikela', '1200', 'm', '3.00', '2.40', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('kohdelamppu', '100', 'kpl', '10', '8.00', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('hehkulamppu', '200', 'kpl', '1', '0.80', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('tulppasulake', '300', 'kpl', '0.40', '0.32', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('kytkin', '100', 'kpl', '1', '8', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('maakaapeli', '1000', 'm', '4', '3.20', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('dieselgeneraattori', '3', 'kpl', '5000', '4000', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('1-vaihe-bensiinigeneraattori', '8', 'kpl', '1250', '1000', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('3-vaihe-bensiinigeneraattori', '6', 'kpl', '2500', '2000', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('akkulaturi', '80', 'kpl', '12.50', '10', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('litium-paristo', '100', 'kpl', '7.50', '6', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('aurinkopaneeli', '40', 'kpl', '87.50', '70', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('muovilista', '1000', 'm', '5', '4', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('laajakaista-antenni', '40', 'kpl', '18.75', '15', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('jatkojohto', '1200', 'm', '4.00', '3.20', '24');");
                stmt.executeUpdate("INSERT INTO tarvike (nimike, varastotilanne, yksikko, myyntihinta, sisaanostohinta, alv) VALUES ('opaskirja', '100', 'kpl', '10', '8', '10');");                
                con.commit();
            }
            tables = dbm.getTables(null, null, "urakkatarjous", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE urakkatarjous(ut_nro SERIAL, hyvaksytty VARCHAR(5) NOT NULL, osissa VARCHAR(5), urakkasumma DECIMAL(8,2), tk_nro INT NOT NULL, PRIMARY KEY (ut_nro), FOREIGN KEY (tk_nro) REFERENCES tyokohde);");
                con.commit();
            }
            tables = dbm.getTables(null, null, "luettelee", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE luettelee(ut_nro INT, t_nro INT, maara INT, PRIMARY KEY (ut_nro, t_nro), FOREIGN KEY (ut_nro) REFERENCES urakkatarjous, FOREIGN KEY (t_nro) REFERENCES tarvike);");
                con.commit();
            }
            tables = dbm.getTables(null, null, "tyosuoritus", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE tyosuoritus(ts_nro SERIAL, tyyppi VARCHAR(32), hinta INT, kesto INT, alennuspros DECIMAL (4,2), ut_nro INT, tk_nro INT NOT NULL, PRIMARY KEY (ts_nro), FOREIGN KEY (ut_nro) REFERENCES urakkatarjous, FOREIGN KEY (tk_nro) REFERENCES tyokohde);");
                con.commit();
            }
            tables = dbm.getTables(null, null, "kaytetaan", null);
            if (!tables.next()) {
                con.setAutoCommit(false);
                stmt.executeUpdate("CREATE TABLE kaytetaan(t_nro INT, ts_nro INT, maara INT NOT NULL, alennuspros DECIMAL(4,2), PRIMARY KEY (t_nro, ts_nro), FOREIGN KEY (t_nro) REFERENCES tarvike, FOREIGN KEY (ts_nro) REFERENCES tyosuoritus);");
                con.commit();
            }
        }
        catch (SQLException e) {
            System.out.println("Relaatioiden luonnissa tapahtui virhe!");
        }
        finally {
            suljeYhteysTietokantaan(con, stmt, rset);
        }                
    }
}