// Kayttoliittymaluokka.
import java.util.Scanner;
import java.util.ArrayList;
public class Kayttoliittyma {
    public static void lueKomento(char komento) {
        try {
        if (komento == 'L' || komento == 'l')
            suoritaTyokohdelisays();
        else if (komento == 'T' || komento == 't')
            tallennaTuntityot();   
        else if (komento == 'H' || komento == 'h')
            muodostaHintaArvio();     
        else if (komento == 'K' || komento == 'k')
            kokoaTuntityolasku();
	    else if (komento == 'M' || komento == 'm')
		    lahetaJalkilaskut(2);
	    else if (komento == 'P' || komento == 'p')
		    lahetaJalkilaskut(3);
        else if (komento == 'U' || komento == 'u')
            teeUrakkatarjous();    
        else if (komento == 'O' || komento == 'o')
            annaOhjeet();
        else if (komento == 'S' || komento == 's') {
            System.out.println("Lopetit laskutusjarjestelman kayton.");  
            System.out.println("Taman jarjestelman on tehnyt Huhtikuussa 2016 Valtteri Ylisalo, ylisalo.t.valtteri@student.uta.fi");
        }
        else
            System.out.println("Antamasi kirjain ei suorita mitaan komentoa!");
        }
        catch (Exception e) {
            System.out.println("Tapahtui odottamaton poikkeus."+e.getMessage());
        }
    }
        
    // Tapahtuma, jossa lisataan asiakkaalle xx uusi tyokohde.    
    public static void suoritaTyokohdelisays() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Anna asiakkaan henkilotunnus:");
        String hetu = sc.next();
        while (hetu.length() != 11) {
            System.out.println("Hetun pituus on tasan 11 merkkia! Anna asiakkaan henkilotunnus:");
            hetu = sc.next();   
        }
        String etunimi, sukunimi, asiakasOsoite;
        String[] asiakastiedot = Tiko2016.haeAsiakastiedot(hetu);
        if (asiakastiedot[3].equals("EI_OLEMASSA")) {
            System.out.println("Kyseessa uusi asiakas. Anna asiakkaan etunimi (kutsumanimi):");
            etunimi = sc.next();
            System.out.println("Anna asiakkaan sukunimi:");
            sukunimi = sc.next();
            System.out.println("Anna asiakkaan osoite (valilyonti alaviivalla):");
                asiakasOsoite = sc.next();
            }
        else {
            etunimi = asiakastiedot[0];
            sukunimi = asiakastiedot[1];
            asiakasOsoite = asiakastiedot[2];
            System.out.print("Kyseessa vanha asiakas. ");
        }
            System.out.println("Anna lisattavan tyokohteen osoite (valilyonti alaviivalla):");
            String tyokohdeOsoite = sc.next();
            Tiko2016.lisaaTyokohde(hetu, etunimi, sukunimi, asiakasOsoite, tyokohdeOsoite);
        }
        
    // Tapahtuma, jossa tallennetaan tuntityot paivan paatteeksi.
    public static void tallennaTuntityot() {
        Scanner sc = new Scanner(System.in);
        ArrayList<Tyosuoritus> tyosuorituslista = new ArrayList<Tyosuoritus>();
        System.out.println("Anna tyokohteen numero, johon liittyvat tyosuoritukset ja tarvikkeet tallennetaan:");
        int tyokohdeNro = sc.nextInt();
        boolean onOlemassa = Tiko2016.tarkistaOnkoTyokohdeOlemassa(tyokohdeNro);
        boolean onUrakka = Tiko2016.tarkistaOnkoUrakka(tyokohdeNro);
        boolean onLaskutettu = Tiko2016.tarkistaOnkoLaskutettu(tyokohdeNro);
        if (!onUrakka && !onLaskutettu && onOlemassa) {
            char komento = 'K';
            while (komento != 'E' && komento != 'e') {
                System.out.println("Anna tyosuorituksen tyyppi: (S)uunnittelu, (T)yo, (A)putyo");
                char tyyppi = sc.next().charAt(0);
                while (tyyppi != 'S' && tyyppi != 's' && tyyppi != 'T' && tyyppi != 't' && tyyppi != 'A' && tyyppi != 'a') {
                    System.out.println("Syote ei kelpaa, anna tyosuorituksen tyyppi: (S)uunnittelu, (T)yo, (A)putyo");
                    tyyppi = sc.next().charAt(0);
                }   
                 System.out.println("Anna tyosuorituksen kesto tunteina:");
                int kesto = sc.nextInt();
                while (kesto < 1) {
                    System.out.println("Kesto ei voi olla nolla tai negatiivinen luku!");
                    kesto = sc.nextInt();
                }
                System.out.println("Anna tyosuorituksen alennusprosentti:");
                double suoritusAlennus = sc.nextDouble();
                while (suoritusAlennus < 0 || suoritusAlennus >= 100) {
                    System.out.println("Alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                    suoritusAlennus = sc.nextInt();
                }
                Tyosuoritus tyosuoritus = new Tyosuoritus(tyyppi, kesto, suoritusAlennus);
                System.out.println("Montako eri tyyppista tarviketta tyosuoritukseen liittyi? Anna luku numeroina:");
                int maara = sc.nextInt();
                while (maara < 0) {
                    System.out.println("Erityyppisten tarvikkeiden maara ei voi olla negatiivinen luku!");
                    maara = sc.nextInt();
                }
                for (int i = 1; i < maara+1; i++) {
                    boolean varastotilanneOK = false;
                    String nimike = "";
                    int yksikkomaara = 0;
                    while (!varastotilanneOK) {
                        System.out.println("Anna tyosuoritukseen liittyvaan tarvikkeen nimike ("+i+"/"+maara+"):");
                        nimike = sc.next();
                        System.out.println("Anna tyosuoritukseen liittyvaan tarvikkeen yksikkomaara (kpl/m):");
                        yksikkomaara = sc.nextInt();
                        while (yksikkomaara < 1) {
                            System.out.println("Tarvikkeen yksikkomaara ei voi olla nolla tai negatiivinen luku!");
                            yksikkomaara = sc.nextInt();
                        }
                        varastotilanneOK = Tiko2016.tarkistaTarvikkeenSaatavuus(nimike, yksikkomaara);
                    }
                    System.out.println("Anna tyosuoritukseen liittyvaan tarvikkeen alennusprosentti:");
                    double tarvikeAlennus = sc.nextDouble();
                    while (tarvikeAlennus < 0 && tarvikeAlennus >= 100) {
                        System.out.println("Tarvikkeen alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                        tarvikeAlennus = sc.nextInt();
                    }
                    Tarvike tarvike = new Tarvike(nimike, yksikkomaara, tarvikeAlennus);
                    tyosuoritus.tarvikkeet.add(tarvike);
                }
                tyosuorituslista.add(tyosuoritus);
                System.out.println("Lisaa uusi tyosuoritus? (K)ylla, (E)i.");
                komento = sc.next().charAt(0);
                while (komento != 'K' && komento != 'k' && komento != 'E' && komento != 'e') {
                    System.out.println("Anna (K)ylla tai (E)i!");
                    komento = sc.next().charAt(0);
                }
            }
            Tiko2016.tallennaTuntityot(tyokohdeNro, tyosuorituslista);
        }
        else
            System.out.println("Antamasi tyokohde ei ole maaritelty tuntityokohteeksi tai kohteesta on jo lahetetty lasku!");
    }
        
    // Tapahtuma, jossa muodostetaan hinta-arvio kohteeseen x.
    public static void muodostaHintaArvio() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Anna tyokohteen numero, josta hinta-arvio muodostetaan:");  
        int tyokohdeNro = sc.nextInt();      
        Tiko2016.muodostaHintaArvio(tyokohdeNro);
    }
        
    // Tapahtuma, johon kootaan tuntityolasku.
    public static void kokoaTuntityolasku() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Anna laskun numero:");
        int laskuNro = sc.nextInt();
        System.out.println("Onko lasku (1) peruslasku, (2) muistutuslasku vai (3) perintalasku? Valitse numero:");
        int monesko = sc.nextInt();
        while (monesko <= 0 || monesko >= 4) {
            System.out.println("Anna (1) peruslasku, (2) muistutuslasku tai (3) perintalasku!");
            monesko = sc.nextInt();
        }
        Tiko2016.kokoaTuntityolasku(laskuNro, monesko);
    }
	
	// Tapahtumat, jossa kootaan maksamattomista laskuista joko muistutuslaskuista tai karhulaskuista.
    // Ovat toteutettu samassa metodissa, koska implementaatio on lahes identtinen.
    public static void lahetaJalkilaskut(int monesko) {
        if (monesko == 2)
            System.out.println("Lahetetaanko muistutuslaskut kaikista erapaivan umpeutuneista laskuista? (K)ylla, (E)i.");
        else if (monesko == 3)
            System.out.println("Lahetetaanko perintalaskut kaikista erapaivan umpeutuneista muistutuslaskuista? (K)ylla, (E)i.");
        Scanner sc = new Scanner(System.in);
        char komento = sc.next().charAt(0);
        while (komento != 'K' && komento != 'k' && komento != 'E' && komento != 'e') {
            System.out.println("Anna (K)ylla tai (E)i!");
            komento = sc.next().charAt(0);
        }
        if (komento == 'K' || komento == 'k') {
            System.out.println("Anna paivamaara muodossa PAIVA KUUKAUSI VUOSI (esim. 3 12 2016), eli erottele valilyonneilla."); 
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
            Tiko2016.lahetaJalkilaskut(toimituspvm.toString(), paiva, kuukausi, vuosi, monesko);
	    }
        else if ((komento == 'E' || komento == 'e') && monesko == 2)
            System.out.println("Muistutuslaskuja ei haluttu lahettaa.");
        else if ((komento == 'E' || komento == 'e') && monesko == 3)
            System.out.println("Perintalaskuja ei haluttu lahettaa.");
    }
	
    // Tapahtuma, jossa muodostetaan urakkatarjous.
    public static void teeUrakkatarjous() {
        Scanner sc = new Scanner(System.in);
        Urakkatarjous tarjous = new Urakkatarjous();
        int[] kestotiedot = new int[3];
        double[] alennustiedot = new double[3];
        System.out.println("Anna tyokohteen numero, johon urakkatarjousarvio kohdistuu:");
        int tyokohdeNro = sc.nextInt();
        boolean onOlemassa = Tiko2016.tarkistaOnkoTyokohdeOlemassa(tyokohdeNro);
        boolean onUrakka = Tiko2016.tarkistaOnkoUrakka(tyokohdeNro);
        boolean onLaskutettu = Tiko2016.tarkistaOnkoLaskutettu(tyokohdeNro);
        if (onUrakka && !onLaskutettu && onOlemassa) {
            tarjous.tkNro(tyokohdeNro);
            System.out.println("Laskutetaanko urakka osissa? (K)ylla, (E)i.");
            char osissa = sc.next().charAt(0);
            while (osissa != 'K' && osissa != 'k' && osissa != 'E' && osissa != 'e') {
                System.out.println("Anna (K)ylla tai (E)i!");
                osissa = sc.next().charAt(0);
            }
            if (osissa == 'K' || osissa == 'k')
                tarjous.osissa(true);
            else if (osissa == 'E' || osissa == 'e')
                tarjous.osissa(false);
            System.out.println("Anna arvioitu suunnittelutyon osuus tunteina:");
            kestotiedot[0] = sc.nextInt();
            while (kestotiedot[0] < 0) {
                System.out.println("Suunnittelutyon osuus ei voi olla negatiivinen luku!");
                kestotiedot[0] = sc.nextInt();
            }
            System.out.println("Anna arvioitu suunnittelutyon alennusprosentti:");
            alennustiedot[0]  = sc.nextInt();
            while (alennustiedot[0] < 0 && alennustiedot[0] >= 100) {
                System.out.println("Alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                alennustiedot[0] = sc.nextInt();
            }
            System.out.println("Anna arvioitu asennustyon osuus tunteina:");
            kestotiedot[1]  = sc.nextInt();
            while (kestotiedot[1] < 0) {
                System.out.println("Asennustyon osuus ei voi olla negatiivinen luku!");
                kestotiedot[1] = sc.nextInt();
            }
            System.out.println("Anna arvioitu asennustyon alennusprosentti:");
            alennustiedot[1]  = sc.nextInt();
            while (alennustiedot[1] < 0 && alennustiedot[1] >= 100) {
                System.out.println("Alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                alennustiedot[1] = sc.nextInt();
            }
            System.out.println("Anna arvioitu aputyon osuus tunteina:");
            kestotiedot[2]  = sc.nextInt();
            while (kestotiedot[2] < 0) {
                System.out.println("Aputyon osuus ei voi olla negatiivinen luku!");
                kestotiedot[2] = sc.nextInt();
            }
            System.out.println("Anna arvioitu aputyon alennusprosentti:");
            alennustiedot[2]  = sc.nextInt();
            while (alennustiedot[2] < 0 && alennustiedot[2] >= 100) {
                System.out.println("Alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                alennustiedot[2] = sc.nextInt();
            }
            System.out.println("Montako eri tyyppista tarviketta urakkaan arviolta liittyy? Anna luku numeroina:");
            int maara = sc.nextInt();
            while (maara < 0) {
                System.out.println("Erityyppisten tarvikkeiden maara ei voi olla negatiivinen luku!");
                maara = sc.nextInt();
            }
            ArrayList<Tarvike> tarvikkeet = new ArrayList<Tarvike>();
            for (int i = 1; i < maara+1; i++) {
                boolean varastotilanneOK = false;
                String nimike = "";
                int yksikkomaara = 0;
                while (!varastotilanneOK) {
                    System.out.println("Anna tarvikkeen nimike ("+i+"/"+maara+"):");
                    nimike = sc.next();
                    System.out.println("Anna tarvikkeen yksikkomaara (kpl tai metri):");
                    yksikkomaara = sc.nextInt();
                    while (yksikkomaara < 1) {
                        System.out.println("Tarvikkeen yksikkomaara ei voi olla nolla tai negatiivinen luku!");
                        yksikkomaara = sc.nextInt();
                    }
                    varastotilanneOK = Tiko2016.tarkistaTarvikkeenSaatavuus(nimike, yksikkomaara);
                }
                System.out.println("Anna tarvikkeen alennusprosentti:");
                double tarvikeAlennus = sc.nextDouble();
                while (tarvikeAlennus < 0 && tarvikeAlennus >= 100) {
                    System.out.println("Tarvikkeen alennus ei voi olla negatiivinen tai 100% tai sita suurempi!");
                    tarvikeAlennus = sc.nextInt();
                }
                Tarvike tarvike = new Tarvike(nimike, yksikkomaara, tarvikeAlennus);
                tarvikkeet.add(tarvike);
            }
            Tiko2016.raportoiUrakkatarjous(tarjous, kestotiedot, alennustiedot, tarvikkeet);
        }
        else
            System.out.println("Antamasi tyokohde ei ole maaritelty urakkatilauskohteeksi tai se on jo laskutettu!");
    }

    public static void annaOhjeet() {
        System.out.println("Mista toiminnoista haluat ohjeita? Anna jokin seuraavista kirjaimista:");
        System.out.println("(L)isaa tyokohde, (T)untityotallennus, (H)inta-arvio, (K)okoa tuntityolasku");
        System.out.println("(M)uistutuslasku, (P)erintalasku, (U)rakkatarjous, (S)ulje ohjelma");
        System.out.println("Jos haluat poistua ohjetilasta, anna mika tahansa muu kirjain.");
        System.out.println("------------------------------------------------------------------------------------");
        Scanner sc = new Scanner(System.in);
        char komento = sc.next().charAt(0);
        if (komento == 'L' || komento == 'l') {
            System.out.println("Tyokohteen lisayksessa anna ensin sen asiakkaan henkilotunnus, joka omistaa");
            System.out.println("kyseisen tyokohteen. Jos kyseessa on uusi asiakas, niin anna siita tarvittavat");
            System.out.println("tiedot jotka ohjelma kysyy. Jos kyseessa on vanha asiakas, niin riittaa etta ");
            System.out.println("annat vain henkilotunnuksen. Tyokohteen maaritellessa anna sen osoite seka kerro");
            System.out.println("onko kyseessa tuntityopohjainen vai urakkapohjainen tyokohde.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
        if (komento == 'T' || komento == 't') {
            System.out.println("Tuntityotallennuksessa anna ensin tyokohteen osoite, johon liittyvat tuntiyot");
            System.out.println("ja tarviketiedot tallennetaan. Anna sitten siihen liittyvat suunnittelu-, asennus-");
            System.out.println("ja aputyotiedot seka naissa toissa kaytetyt tarviketiedot");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
        if (komento == 'H' || komento == 'h') {
            System.out.println("Hinta-arvio muodostaa yksinkertaisesti kohteen hinta-arvion siihen lisattyjen tunti-");
            System.out.println("ja tarviketietojen perusteella tai vaihtoehtoisesti urakkasopimuksesta");
            annaOhjeet();
        }
         if (komento == 'K' || komento == 'k') {
            System.out.println("Tuntityolaskun kokoamisessa voit hakea laskuraportin mista tahansa tyokohteesta, joka");
            System.out.println("on maaritelty tuntityopohjaiseksi tyokohteeksi. Raportin jalkeen voit lahettaa laskun");
            System.out.println("asiakkaalle, mikali sita ei ole lahetetty aiemmin.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
         if (komento == 'M' || komento == 'm') {
            System.out.println("Muistutuslaskun lahettaminen kohdistuu kaikkiin peruslaskuihin, joita ei ole maksettu");
            System.out.println("erapaivaan mennessa. Syota ohjelmaan paivamaara, joka nyt on (tai joku muu paivamaara");
            System.out.println("ja sen jalkeen muistutuslasku lahetetaan kaikista peruslaskuista, joiden erapaiva on");
            System.out.println("umpeutunut ja joista ei ole aiemmin lahetetty muistutuslaskua.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
        if (komento == 'P' || komento == 'p') {
            System.out.println("Perintalaskun lahettaminen kohdistuu kaikki muistutuslaskuiin, joita ei ole maksettu");
            System.out.println("erapaivaan mennessa. Syota ohjelmaan paivamaara, joka nyt on (tai joku muu paivamaara");
            System.out.println("ja sen jalkeen perintalasku lahetetaan kaikista muistutuslaskuista, joiden erapaiva on");
            System.out.println("umpeutunut ja joista ei ole aiemmin lahetetty perintalaskua.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
         if (komento == 'U' || komento == 'u') {
            System.out.println("Urakkatarjous voidaan muodostaa vain urakkapohjaisiksi maariteltyihin kohteiksi. Anna");
            System.out.println("urakan suunnittelu-, asennus- ja aputyon tiedot seka tarvittavat tarviketiedot.");
            System.out.println("Urakkatarjous voidaan hyvaksya urakkasopimukseksi, mikali asiakas on hyvaksynyt");
            System.out.println("urakkatarjouksen.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }
         if (komento == 'S' || komento == 's') {
            System.out.println("Ohjelman voi lopettaa turvallisesti talla toiminnolla paavalikossa. Mikali on tarpeen");
            System.out.println("poistua ohjelmasta valittomasti missa tahansa tilanteessa, niin voi painaa Ctrl+C.");
            System.out.println("------------------------------------------------------------------------------------");
            annaOhjeet();
        }                           
    }
}