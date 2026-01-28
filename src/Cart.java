/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author LENOVO
 */
import java.util.ArrayList;

public class Cart {
    private ArrayList<CartItem> items = new ArrayList<>();

    public void addItem(CartItem newItem) {
        for (CartItem item : items) {
            if (item.getIdBarang().equals(newItem.getIdBarang())) {
                item.tambahJumlah(newItem.getJumlah());
                return;
            }
        }
        items.add(newItem);
    }

    public void removeItem(int index) {
    if (index >= 0 && index < items.size()) {
        items.remove(index);
    }
}


    public ArrayList<CartItem> getItems() {
        return items;
    }

    public double getSubTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getTotalHarga();
        }
        return total;
    }
    
      public double getTotal() {
        return getSubTotal();
    }


    public void clear() {
        items.clear();
    }
}
