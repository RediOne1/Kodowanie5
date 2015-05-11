import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class Main {

    public static double entropia(byte data[]) {
        int znaki[] = new int[256];
        int ilosc_znakow = 0;
        for (byte aData : data) {
            if (aData >= 0)
                znaki[aData]++;
            else
                znaki[256 + aData]++;
            ilosc_znakow++;
        }

        double entropia = 0.0;
        for (int i = 0; i < 256; i++){
            if (znaki[i] != 0) {
                double pi = (double)znaki[i]/(double)ilosc_znakow;
                //System.out.println(pi);
                entropia = entropia + (pi*(Math.log(1/pi)/Math.log(2)));
            }
        }
        return entropia;
    }

    public static void info(Vector<Byte> przed, Vector<Byte> po, byte[] data) {
        System.out.println("Dlugosc pliku przed kompresja: " + data.length);
        System.out.println("Dlugosc pliku po kompresja: " + przed.size());
        System.out.println("Dlugosc pliku po dekompresji: " + po.size());
        System.out.println("Entropia: " + entropia(data));
        System.out.println("Stopieñ kompresji: " + (double) przed.size() / data.length * 100 + " %");
    }

    public static Vector<Byte> kompresuj(byte[] data, int slownik, int bufor) {
        Vector<Byte> kod = new Vector<Byte>();
        int i = 1;
        kod.add((byte) 0);//plik jest zero wiec wrzucamy 0
        kod.add(data[0]);//wysylamy pierwszy znak
        while (i < data.length) {//przegladamy plik
            int tab[] = new int[slownik]; // tablica pomocnicza gdzie zapiszemy wyniki
            for (int j = 1; j <= slownik; j++) { // przegladamy wszystkie mozliwosci wstecz
                if (i - j < 0) { // jesli wyjdziemy poza tablice to nie ma sensu cofac sie dalej
                    break;
                }
                else if (data[i] == data[i - j]) { // sprawdzilismy ze liera ktora sprawdzamy na itej pozycji rowna sie litera z i - j
                    tab[j-1] = 1; // plusujemy odpowiednie miejsce w tablicy
                    int p = 1;
                    while (i+p < i + bufor && i + p < data.length){ // sprawdzamy do miejsca i + bufor lub jesli zabraknie tablicy
                        if (data[i-j+p] == data[i + p]) {// porownujemy kolejne litery czy sie rownaja i szukamy najdluzszego dopasowania
                            tab[j-1]++;
                            p++;
                        }
                        else {
                            break;
                        }
                    }
                }
            }
            int max = 0;
            int war = tab[0];
            for (int j = 0; j < slownik; j++) {//szukamy maxa w tablicy
                if (tab[j] > max) {
                    max = tab[j];
                    war = j;
                }
                //System.out.print(tab[j] + " ");
            }
            //System.out.println(i);
            if (max == 0) { // jesli sie nie cofalismy to wysylamy 0 i kod litery
                kod.add((byte) 0);
                kod.add(data[i]);
                i++;
            }
            else { // jesli cofalismy sie to wysylamy ile sie cofalismy sie i ile liczb kopiujemy
                kod.add((byte) (war + 1));
                kod.add((byte) (max));
                i = i + max;
            }
        }
        //System.out.println(il);
        return kod;
    }

    public static Vector<Byte> dekompresuj(Vector<Byte> data) {
        Vector<Byte> kod = new Vector<Byte>();
        int i = 0;
        while (i < data.size()) {
            int b = data.get(i).byteValue(); // czytamy pierwsza wartosc
            //System.out.print(b + " ");
            i++;
            if (b == 0) { // jesli pierwsza wartosc jest zero to nie bylo zadnego dopaspwania i musimy pobrac bit znaku
                kod.add(data.get(i).byteValue()); // bit znaku ktory wstamiamy do pliku
            }
            else { // musielismy sie cofnac i skopiowac ilesc liter
                if (b < 0) {// pracujemy na liczbach od -128 do 127 musialo byc wiecej niz 128 wiec liczba jest ujemna wiec musimy ja przerobic na liczbe wieksza niz 128
                    //int t = b;

                    b = 256 + b;
                    //System.out.println(b + " " + t);
                }
                else if (b == -127) // if wyzej nie chce czytac liczby -128 czli po odwroceniu 128
                    b = 128;
                //System.out.print(kod.size());
                int m = kod.size() - b; // od w ktorym aktualnie jestesmy cofamy sie o b czyli wedlug algorytmu
                //System.out.println(m + " " + b + " " + t);
                int ile = data.get(i).byteValue(); // pobieramy ile liter mamy skopioac tak jak wyzej pracujemy na liczbach +-128
                if (ile < 0)
                    ile = 256 + ile;
                for (int j = 0; j < ile; j++) {
                    kod.add(kod.get(m).byteValue());//kopiujemy ile liter
                    m++;
                }
            }
            i++;
        }
        return kod;
    }

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("tadeusz.txt");
        byte[] data = Files.readAllBytes(path);

        Vector<Byte> kompresja = kompresuj(data, 4, 4);//dablica z bajtami pliku wejsciowego, rozmiar slownika, rozmiar bufora (max 255)
        Vector<Byte> kod = dekompresuj(kompresja);

        byte[] data2 = new byte[kod.size()];
        for(int i = 0; i < kod.size(); ++i)
        {
            data2[i] = kod.get(i).byteValue();//przekopiowanie z vectora do tablicy do zapisu
        }
        Path path2 = Paths.get("odblokowany.txt");
        Files.write(path2, data2);

        info(kompresja, kod, data);
        //System.out.println(kompresja); // wypisywanie zawartosci po kompresji

    }
}