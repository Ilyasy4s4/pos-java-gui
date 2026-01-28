/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author LENOVO
 */
public class CartItem {
    String idBarang;
    String namaBarang;
    int jumlah;
    private double harga;
    private double diskon; // persen (0 - 100)

    public CartItem(String idBarang, String namaBarang, int jumlah, double harga, double diskon) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.jumlah = jumlah;
        this.harga = harga;
        this.diskon = diskon;
    }

    public String getIdBarang() {
        return idBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public int getJumlah() {
        return jumlah;
    }

    public double getHarga() {
        return harga;
    }

    public double getDiskon() {
        return diskon;
    }

    public void tambahJumlah(int qty) {
        this.jumlah += qty;
    }

    public double getTotalHarga() {
        double total = harga * jumlah;
        if (diskon > 0) {
            total -= total * (diskon / 100);
        }
        return total;
    }

   public double getTotal() {
    double total = harga * jumlah;
    if (diskon > 0) {
        total -= total * (diskon / 100);
    }
    return total;
}

}
