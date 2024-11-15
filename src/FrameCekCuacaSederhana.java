
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
public class FrameCekCuacaSederhana extends javax.swing.JFrame {
    
    
public void loadTableDataFromCSV() {
    // Membuka JFileChooser untuk memilih file CSV
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Pilih file CSV");
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
        String filePath = fileChooser.getSelectedFile().getAbsolutePath();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Mendapatkan model tabel
            DefaultTableModel model = (DefaultTableModel) tableRiwayat.getModel();
            model.setRowCount(0); // Mengosongkan tabel sebelum memuat data baru

            String line;
            boolean isFirstLine = true; // Untuk melewati header CSV
            while ((line = reader.readLine()) != null) {
                // Mengabaikan header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Memisahkan data berdasarkan koma
                String[] data = line.split(",");
                
                // Menambahkan baris data ke model tabel
                model.addRow(data);
            }

            JOptionPane.showMessageDialog(this, "Data berhasil dimuat dari file " + filePath, "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memuat file CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    } else {
        JOptionPane.showMessageDialog(this, "Tidak ada file yang dipilih.", "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }
}

public void saveTableDataToCSV() {
    // Membuka JFileChooser untuk memilih lokasi dan nama file
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Pilih Lokasi untuk Menyimpan File");
    fileChooser.setSelectedFile(new File("data_cuaca.csv")); // Set default file name

    // Menyaring file yang akan ditampilkan hanya file CSV
    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));

    int userSelection = fileChooser.showSaveDialog(this);
    
    // Jika pengguna memilih tombol "Save"
    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();

        // Pastikan file yang dipilih memiliki ekstensi ".csv"
        String fileName = fileToSave.getAbsolutePath();
        if (!fileName.endsWith(".csv")) {
            fileName += ".csv"; // Menambahkan ekstensi .csv jika tidak ada
        }
        
        try (FileWriter writer = new FileWriter(fileName)) {
            // Mendapatkan model tabel
            DefaultTableModel model = (DefaultTableModel) tableRiwayat.getModel();
            
            // Menulis header kolom ke file CSV
            writer.append("Kota,Cuaca,Detail Cuaca,Suhu\n");
            
            // Menulis setiap baris data tabel ke file CSV
            for (int i = 0; i < model.getRowCount(); i++) {
                String city = (String) model.getValueAt(i, 0);
                String weather = (String) model.getValueAt(i, 1);
                String description = (String) model.getValueAt(i, 2);
                String temperature = (String) model.getValueAt(i, 3);
                
                // Menulis data baris ke file CSV
                writer.append(city + "," + weather + "," + description + "," + temperature + "\n");
            }
            
            // Menampilkan pesan sukses
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan ke file " + fileName, "Sukses", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            // Menampilkan pesan error jika terjadi kesalahan
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat menyimpan file CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}    
public void fetchWeatherDataFromTextField() {
    String city = inputKota.getText().trim(); // Ambil kota dari TextField
    if (city.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Masukkan nama kota terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String apiKey = "07df57ec686790b155da3f0b59a7c39a";
    String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&lang=id&appid=" + apiKey;

    try {
        // Membuat koneksi ke URL API
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        // Mengecek respons HTTP
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            // Membaca data dari API
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parsing JSON response
            JSONObject jsonResponse = new JSONObject(response.toString());
            String weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main");
            String description = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
            double temperature = jsonResponse.getJSONObject("main").getDouble("temp");
            String iconCode = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("icon");

            // Pemetaan cuaca dari bahasa Inggris ke bahasa Indonesia
            weather = mapWeatherToIndonesian(weather);

            // Menampilkan hasil ke label
            labelCuaca.setText("Cuaca: " + weather);
            labelDetail.setText("Detail: " + description);
            labelSuhu.setText("Suhu: " + temperature + " °C");

            // Mengambil dan menampilkan ikon cuaca
            String iconUrl = "http://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            labelGambar.setIcon(new ImageIcon(new URL(iconUrl)));

            // Menghilangkan teks placeholder setelah ikon muncul
            labelGambar.setText("");

            // Menambahkan data ke tabel
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tableRiwayat.getModel();
            model.addRow(new Object[]{city, weather, description, temperature + " °C"});

            // Menambahkan kota ke ComboBox jika belum ada
            boolean alreadyInComboBox = false;
            for (int i = 0; i < cbbFavorit.getItemCount(); i++) {
                if (cbbFavorit.getItemAt(i).equalsIgnoreCase(city)) {
                    alreadyInComboBox = true;
                    break;
                }
            }
            if (!alreadyInComboBox) {
                cbbFavorit.addItem(city);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Kota tidak ditemukan! Pastikan nama kota benar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Terjadi kesalahan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}// Pemetaan cuaca dari bahasa Inggris ke bahasa Indonesia
private String mapWeatherToIndonesian(String weather) {
    switch (weather.toLowerCase()) {
        case "clear":
            return "Cerah";
        case "clouds":
            return "Berawan";
        case "rain":
            return "Hujan";
        case "snow":
            return "Salju";
        case "thunderstorm":
            return "Badai Petir";
        case "drizzle":
            return "Gerimis";
        case "fog":
            return "Kabut";
        case "mist":
            return "Kabut";
        case "haze":
            return "Berawan Mendung";
        default:
            return weather; // Kembalikan nama cuaca jika tidak ada pemetaan
    }
}

    /**
     * Creates new form FrameCekCuacaSederhana
     */
    public FrameCekCuacaSederhana() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        labelGambar = new javax.swing.JLabel();
        labelCuaca = new javax.swing.JLabel();
        labelDetail = new javax.swing.JLabel();
        labelSuhu = new javax.swing.JLabel();
        btnCek = new javax.swing.JButton();
        inputKota = new javax.swing.JTextField();
        cbbFavorit = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableRiwayat = new javax.swing.JTable();
        btnSimpan = new javax.swing.JButton();
        btnMuat = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jTextField1.setText("jTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Pilih Kota Anda");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel2.setText("Kota Favorit");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(jLabel2, gridBagConstraints);

        labelGambar.setText("Gambar");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(labelGambar, gridBagConstraints);

        labelCuaca.setText("Cuaca:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(labelCuaca, gridBagConstraints);

        labelDetail.setText("Detail:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(labelDetail, gridBagConstraints);

        labelSuhu.setText("Suhu:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(labelSuhu, gridBagConstraints);

        btnCek.setText("Cek Cuaca");
        btnCek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCekActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(btnCek, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(inputKota, gridBagConstraints);

        cbbFavorit.setSelectedIndex(-1);
        cbbFavorit.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbbFavoritItemStateChanged(evt);
            }
        });
        cbbFavorit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbFavoritActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(cbbFavorit, gridBagConstraints);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 200));

        tableRiwayat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kota", "Cuaca", "Detail Cuaca", "Suhu"
            }
        ));
        tableRiwayat.setPreferredSize(new java.awt.Dimension(100, 100));
        jScrollPane1.setViewportView(tableRiwayat);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(jScrollPane1, gridBagConstraints);

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(btnSimpan, gridBagConstraints);

        btnMuat.setText("Memuat");
        btnMuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMuatActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 6, 10);
        jPanel2.add(btnMuat, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cbbFavoritActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbFavoritActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbFavoritActionPerformed

    private void btnCekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCekActionPerformed
    fetchWeatherDataFromTextField();            // TODO add your handling code here:
    }//GEN-LAST:event_btnCekActionPerformed

    private void cbbFavoritItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbbFavoritItemStateChanged
        inputKota.setText(cbbFavorit.getSelectedItem().toString());
        fetchWeatherDataFromTextField();            // TODO add your handling code here:
    }//GEN-LAST:event_cbbFavoritItemStateChanged

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
    saveTableDataToCSV();        // TODO add your handling code here:
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnMuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMuatActionPerformed
    loadTableDataFromCSV();        // TODO add your handling code here:
    }//GEN-LAST:event_btnMuatActionPerformed

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
            java.util.logging.Logger.getLogger(FrameCekCuacaSederhana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameCekCuacaSederhana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameCekCuacaSederhana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameCekCuacaSederhana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameCekCuacaSederhana().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCek;
    private javax.swing.JButton btnMuat;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cbbFavorit;
    private javax.swing.JTextField inputKota;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel labelCuaca;
    private javax.swing.JLabel labelDetail;
    private javax.swing.JLabel labelGambar;
    private javax.swing.JLabel labelSuhu;
    private javax.swing.JTable tableRiwayat;
    // End of variables declaration//GEN-END:variables
}
