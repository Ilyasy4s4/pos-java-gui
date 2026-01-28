


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */


/**
 *
 * @author bsame
 */
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class dashboard extends javax.swing.JFrame {

    public dashboard() {
        initComponents();
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH); 
        this.setLocationRelativeTo(null);
    }

    // --- 1. METHOD STATISTIK DENGAN FILTER KASIR ---
    private void hitungSemuaStatistik() {
        java.util.Date tglAwal = jDateChooser1.getDate();
        java.util.Date tglAkhir = jDateChooser2.getDate();

        if (tglAwal == null || tglAkhir == null) {
            JOptionPane.showMessageDialog(this, "Silakan pilih periode tanggal dahulu!");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String mulai = sdf.format(tglAwal);
        String selesai = sdf.format(tglAkhir);
        String pilihKasir = jComboBox1.getSelectedItem().toString();

        try {
            java.sql.Connection conn = koneksi.getConnection();

            // Query Pendapatan + Join User untuk Filter Kasir
            String sqlPendapatan = "SELECT SUM(t.total_harga) AS total FROM transaksi t "
                                 + "JOIN user u ON t.id_user = u.id_user "
                                 + "WHERE t.tanggal_transaksi BETWEEN ? AND ? ";
            
            if (!pilihKasir.equalsIgnoreCase("semua kasir")) {
                sqlPendapatan += " AND u.username = '" + pilihKasir + "'";
            }

            java.sql.PreparedStatement pst1 = conn.prepareStatement(sqlPendapatan);
            pst1.setString(1, mulai);
            pst1.setString(2, selesai);
            java.sql.ResultSet rs1 = pst1.executeQuery();

            double totalPendapatan = 0;
            if (rs1.next()) {
                totalPendapatan = rs1.getDouble("total");
                jLabel2.setText("Rp " + String.format("%,.0f", totalPendapatan));
            }

            // Query HPP + Join User untuk Filter Kasir
            String sqlHPP = "SELECT SUM(b.harga_beli * dt.jumlah) AS hpp "
                          + "FROM detail_transaksi dt "
                          + "JOIN barang b ON dt.id_barang = b.id_barang "
                          + "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi "
                          + "JOIN user u ON t.id_user = u.id_user "
                          + "WHERE t.tanggal_transaksi BETWEEN ? AND ? ";
            
            if (!pilihKasir.equalsIgnoreCase("semua kasir")) {
                sqlHPP += " AND u.username = '" + pilihKasir + "'";
            }

            java.sql.PreparedStatement pst2 = conn.prepareStatement(sqlHPP);
            pst2.setString(1, mulai);
            pst2.setString(2, selesai);
            java.sql.ResultSet rs2 = pst2.executeQuery();

            double totalHPP = 0;
            if (rs2.next()) {
                totalHPP = rs2.getDouble("hpp");
                jLabel3.setText("Rp " + String.format("%,.0f", totalHPP));
            }

            double labaBersih = totalPendapatan - totalHPP;
            jLabel4.setText("Rp " + String.format("%,.0f", labaBersih));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error Statistik: " + e.getMessage());
        }
    }

    // --- 2. METHOD GRAFIK (DIKUNCI UKURANNYA AGAR TIDAK RUSAK) ---
    private void tampilkanGrafik() {
        java.util.Date tglAwal = jDateChooser1.getDate();
        java.util.Date tglAkhir = jDateChooser2.getDate();
        if (tglAwal == null || tglAkhir == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String mulai = sdf.format(tglAwal);
        String selesai = sdf.format(tglAkhir);
        String pilihKasir = jComboBox1.getSelectedItem().toString();

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            java.sql.Connection conn = koneksi.getConnection();
            String sql = "SELECT t.tanggal_transaksi, SUM(t.total_harga) AS total FROM transaksi t "
                       + "JOIN user u ON t.id_user = u.id_user "
                       + "WHERE t.tanggal_transaksi BETWEEN ? AND ? ";

            if (!pilihKasir.equalsIgnoreCase("semua kasir")) {
                sql += " AND u.username = '" + pilihKasir + "'";
            }
            sql += " GROUP BY t.tanggal_transaksi ORDER BY t.tanggal_transaksi ASC";

            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, mulai);
            pst.setString(2, selesai);
            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                dataset.addValue(rs.getDouble("total"), "Pendapatan", rs.getString("tanggal_transaksi"));
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Tren Penjualan Harian", "Tanggal", "Total (Rp)", 
                    dataset, PlotOrientation.VERTICAL, false, true, false);

            ChartPanel chartPanel = new ChartPanel(barChart);
            
            // KUNCI UKURAN GRAFIK: Agar tidak menutupi tabel
            chartPanel.setPreferredSize(new java.awt.Dimension(panelGraffik.getWidth(), panelGraffik.getHeight()));

            panelGraffik.removeAll(); 
            panelGraffik.setLayout(new java.awt.BorderLayout());
            panelGraffik.add(chartPanel, java.awt.BorderLayout.CENTER);
            panelGraffik.revalidate();
            panelGraffik.repaint();

        } catch (SQLException e) {
            System.out.println("Error Grafik: " + e.getMessage());
        }
    }
    
    private void tampilkanGrafikBulat() {
    // Ambil tanggal untuk filter query
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String mulai = sdf.format(jDateChooser1.getDate());
    String selesai = sdf.format(jDateChooser2.getDate());

    org.jfree.data.general.DefaultPieDataset dataset = new org.jfree.data.general.DefaultPieDataset();

    try {
        java.sql.Connection conn = koneksi.getConnection();
        // Query kontribusi kasir BERDASARKAN TANGGAL yang dipilih
        String sql = "SELECT u.username, SUM(t.total_harga) AS omzet " +
                     "FROM transaksi t JOIN user u ON t.id_user = u.id_user " +
                     "WHERE t.tanggal_transaksi BETWEEN ? AND ? " +
                     "GROUP BY u.username";
        
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, mulai);
        pst.setString(2, selesai);
        java.sql.ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            dataset.setValue(rs.getString("username"), rs.getDouble("omzet"));
        }

        org.jfree.chart.JFreeChart pieChart = org.jfree.chart.ChartFactory.createPieChart(
                "Kontribusi Kasir (%)", dataset, true, true, false);

        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(pieChart);
        
        // --- KUNCI UKURAN: Sesuaikan dengan ukuran panel di NetBeans kamu ---
        chartPanel.setPreferredSize(new java.awt.Dimension(panelPieChart.getWidth(), panelPieChart.getHeight())); 

        panelPieChart.removeAll();
        panelPieChart.setLayout(new java.awt.BorderLayout());
        panelPieChart.add(chartPanel, java.awt.BorderLayout.CENTER);
        panelPieChart.revalidate();
        panelPieChart.repaint();

    } catch (SQLException e) {
        System.out.println("Error Pie Chart: " + e.getMessage());
    }
}


    // --- 3. METHOD TABEL PRODUK TERLARIS ---
    private void tampilkanProdukTerlaris() {
        java.util.Date tglAwal = jDateChooser1.getDate();
        java.util.Date tglAkhir = jDateChooser2.getDate();
        if (tglAwal == null || tglAkhir == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String mulai = sdf.format(tglAwal);
        String selesai = sdf.format(tglAkhir);

        // Ganti baris pembuatan model yang lama dengan ini
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"Produk", "Terjual", "Total Pendapatan"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Mengunci sel agar tidak bisa diketik manual
            }
        };

        try {
            java.sql.Connection conn = koneksi.getConnection();
            String sql = "SELECT b.nama_barang, SUM(dt.jumlah) AS total_qty, SUM(dt.sub_total) AS total_uang "
                       + "FROM detail_transaksi dt JOIN barang b ON dt.id_barang = b.id_barang "
                       + "JOIN transaksi t ON dt.id_transaksi = t.id_transaksi "
                       + "WHERE t.tanggal_transaksi BETWEEN ? AND ? GROUP BY b.nama_barang "
                       + "ORDER BY total_qty DESC LIMIT 5";
            
            java.sql.PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, mulai);
            pst.setString(2, selesai);
            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("nama_barang"),
                    rs.getString("total_qty"),
                    "Rp " + String.format("%,.0f", rs.getDouble("total_uang"))
                });
            }
            jTable2.setModel(model);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // --- 4. METHOD TABEL KINERJA KASIR ---
    private void tampilkanKinerjaKasir() {
    java.util.Date tglAwal = jDateChooser1.getDate();
    java.util.Date tglAkhir = jDateChooser2.getDate();
    if (tglAwal == null || tglAkhir == null) return;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String mulai = sdf.format(tglAwal);
    String selesai = sdf.format(tglAkhir);

    // 1. BUAT MODEL DENGAN OVERRIDE DI SINI (Hapus kode pembuatan model yang lama)
    javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
        new Object [][] {},
        new String [] {"Nama", "Total Transaksi", "Total Omzet"}
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // Tabel terkunci, tidak bisa diedit manual
        }
    };

    try {
        java.sql.Connection conn = koneksi.getConnection();
        String sql = "SELECT u.username, COUNT(t.id_transaksi) AS jml_trx, SUM(t.total_harga) AS omzet "
                   + "FROM transaksi t JOIN user u ON t.id_user = u.id_user "
                   + "WHERE t.tanggal_transaksi BETWEEN ? AND ? GROUP BY u.username "
                   + "ORDER BY omzet DESC";
        
        java.sql.PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, mulai);
        pst.setString(2, selesai);
        java.sql.ResultSet rs = pst.executeQuery();

        // 2. ISI DATA KE MODEL
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getString("username"),
                rs.getString("jml_trx"),
                "Rp " + String.format("%,.0f", rs.getDouble("omzet")) // Format mata uang rapi
            });
        }
        
        // 3. SET MODEL KE TABEL KAMU
        jTable3.setModel(model); 

    } catch (SQLException e) {
        System.out.println(e.getMessage());
    }
}
            
    // --- SISANYA ADALAH KODE GENERATED NETBEANS (Jangan diubah manual di Source) ---
    // Pastikan jButton2ActionPerformed memanggil keempat method di atas.
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel10 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        panelGraffik = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        panelPieChart = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(39, 174, 96));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Dashboard Pemilik Bisnis");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Selamat Datang, Umi Owner");

        jButton3.setText("Logout");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 805, Short.MAX_VALUE)
                        .addComponent(jButton3)
                        .addGap(54, 54, 54))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jButton3))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText(" total pendapatan");

        jLabel5.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel5.setText(" total pendapatan");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jLabel2)))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(27, 27, 27))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel3.setText("total pengeluaran");

        jLabel6.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel6.setText("total pengeluaran");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(40, 40, 40))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(25, 25, 25))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setText(" laba bersih");

        jLabel12.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel12.setText(" laba bersih");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel12))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(jLabel4)))
                .addContainerGap(60, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(26, 26, 26))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jButton2.setBackground(new java.awt.Color(39, 174, 96));
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("tampilkan data");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel10.setText("Dari");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "yusuf", "tyo", "semua kasir" }));

        jLabel11.setText("Pilih Kasir");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel11)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 153, 0));
        jLabel1.setText("Filter Periode ");

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/icons8-username-24.png"))); // NOI18N
        jLabel7.setText("Owner");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout panelGraffikLayout = new javax.swing.GroupLayout(panelGraffik);
        panelGraffik.setLayout(panelGraffikLayout);
        panelGraffikLayout.setHorizontalGroup(
            panelGraffikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 325, Short.MAX_VALUE)
        );
        panelGraffikLayout.setVerticalGroup(
            panelGraffikLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 188, Short.MAX_VALUE)
        );

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Produk", "Terjual", "Total Pendapatan"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Nama", "Total Transaksi", "Total Omzet"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable3);

        javax.swing.GroupLayout panelPieChartLayout = new javax.swing.GroupLayout(panelPieChart);
        panelPieChart.setLayout(panelPieChartLayout);
        panelPieChartLayout.setHorizontalGroup(
            panelPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 316, Short.MAX_VALUE)
        );
        panelPieChartLayout.setVerticalGroup(
            panelPieChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 204, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelPieChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(panelGraffik, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelPieChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(panelGraffik, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        // TODO add your handling code here:
        
    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
                                        
  // 1. VALIDASI WAJIB: Cek tanggal dulu sebelum menjalankan fungsi lain
    if (jDateChooser1.getDate() == null || jDateChooser2.getDate() == null) {
        JOptionPane.showMessageDialog(this, "Silakan pilih periode tanggal dahulu!");
        return; // Berhenti di sini jika tanggal kosong
    }

    // 2. Jika tanggal sudah diisi, jalankan semua method secara berurutan
    hitungSemuaStatistik();
    tampilkanProdukTerlaris();
    tampilkanKinerjaKasir();
    tampilkanGrafik();      // Grafik Batang
    tampilkanGrafikBulat(); // Grafik Bulat (Sekarang aman dipanggil di sini)

    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
          // Tampilkan dialog konfirmasi
    int pilihan = JOptionPane.showConfirmDialog(
        this,                             // frame parent
        "Apakah Anda yakin ingin logout?", // pesan
        "Konfirmasi Logout",               // judul dialog
        JOptionPane.YES_NO_OPTION,         // opsi tombol: YES / NO
        JOptionPane.QUESTION_MESSAGE       // ikon dialog
    );

    // Jika pengguna memilih YES, jalankan logout
    if (pilihan == JOptionPane.YES_OPTION) {
        LoginForm login = new LoginForm();
        login.setLocationRelativeTo(null); // tampil di tengah layar
        login.setVisible(true);

        this.dispose(); // tutup MenuForm
    }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable3MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new RunnableImpl());
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JPanel panelGraffik;
    private javax.swing.JPanel panelPieChart;
    // End of variables declaration//GEN-END:variables

    private static class RunnableImpl implements Runnable {

        public RunnableImpl() {
        }

        @Override
        public void run() {
            new dashboard().setVisible(true);
        }
    }
}
