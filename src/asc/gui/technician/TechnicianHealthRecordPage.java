package asc.gui.technician;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.List;

public class TechnicianHealthRecordPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final TechnicianDashboard main;

    public TechnicianHealthRecordPage(TechnicianDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(main.sectionTitle("Vehicle Health Records", TechnicianDashboard.ICON_BASE + "Vehicle Health Records(Blue).png"), BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        String[] cols = {"Record ID","Date","Appointment","Customer","Tyre Pressure","Brake (mm)","Oil Type","Spark Plug","Mileage","Notes"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIUtils.createStyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {70, 90, 70, 110, 100, 80, 100, 90, 80, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setRowHeight(28);

        // ── Feature: Sort columns by clicking header ──────────────────────────
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // ── Load data ─────────────────────────────────────────────────────────
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (VehicleHealthRecord r : FileManager.readAllHealthRecords()) {
                if (!r.getTechnicianId().equals(main.currentTech.getUserId())) continue;
                Customer c = FileManager.findCustomerById(r.getCustomerId());
                model.addRow(new Object[]{
                    r.getRecordId(), r.getRecordDate(), r.getAppointmentId(),
                    (c != null) ? c.getName() : r.getCustomerId(),
                    r.getTirePressure(), r.getBrakeThickness(), r.getOilType(),
                    r.getSparkPlugStatus(), r.getMileage() + " km", r.getNotes()
                });
            }
        };
        refresh.run();

        JScrollPane scroll = new JScrollPane(table);
        UIUtils.styleScrollPane(scroll);

        // ── Feature: Search / Filter bar ─────────────────────────────────────
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(UIUtils.LIGHT_GRAY);
        searchLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));

        JTextField searchField = new JTextField(16);
        UIUtils.styleTextField(searchField);
        searchField.setToolTipText("Search by Record ID, Appointment, Customer or Date");

        JLabel oilLbl = new JLabel("Oil Type:");
        oilLbl.setForeground(UIUtils.LIGHT_GRAY);
        oilLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));

        // Collect unique oil types dynamically
        java.util.Set<String> oilSet = new java.util.LinkedHashSet<>();
        oilSet.add("All");
        for (VehicleHealthRecord r : FileManager.readAllHealthRecords()) {
            if (r.getTechnicianId().equals(main.currentTech.getUserId()))
                oilSet.add(r.getOilType());
        }
        JComboBox<String> oilFilter = new JComboBox<>(oilSet.toArray(new String[0]));
        UIUtils.styleCombo(oilFilter);

        UIUtils.RoundedButton searchBtn = new UIUtils.RoundedButton("🔍 Search", new Color(70, 130, 200));
        UIUtils.RoundedButton clearBtn  = new UIUtils.RoundedButton("✖ Clear",   new Color(180, 60, 60));
        searchBtn.setPreferredSize(new Dimension(110, 30));
        clearBtn .setPreferredSize(new Dimension(90,  30));

        searchBtn.addActionListener(e ->
                main.filterHealthRecords(searchField.getText().trim(),
                        (String) oilFilter.getSelectedItem(), sorter));

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            oilFilter.setSelectedIndex(0);
            sorter.setRowFilter(null);
            refresh.run();
        });

        // Live search as user types
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                main.filterHealthRecords(searchField.getText().trim(),
                        (String) oilFilter.getSelectedItem(), sorter);
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterRow.setBackground(UIUtils.DARK_NAVY);
        filterRow.add(searchLbl);
        filterRow.add(searchField);
        filterRow.add(oilLbl);
        filterRow.add(oilFilter);
        filterRow.add(searchBtn);
        filterRow.add(clearBtn);

        // ── Buttons ───────────────────────────────────────────────────────────
        UIUtils.RoundedButton addBtn  = new UIUtils.RoundedButton("+ Add Health Record", UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton editBtn = new UIUtils.RoundedButton("\u270E Edit Record",       UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton refBtn  = new UIUtils.RoundedButton("\u21BB Refresh",           new Color(60, 90, 150));
        editBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        refBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        for (JButton b : new JButton[]{addBtn, editBtn, refBtn})
            b.setPreferredSize(new Dimension(180, 34));

        addBtn.addActionListener(e -> { showAddHealthRecordDialog(); refresh.run(); });
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a record.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String rid = (String) model.getValueAt(modelRow, 0);
            VehicleHealthRecord rec = FileManager.readAllHealthRecords().stream()
                    .filter(r -> r.getRecordId().equals(rid)).findFirst().orElse(null);
            if (rec != null) { showEditHealthRecordDialog(rec); refresh.run(); }
        });
        refBtn.addActionListener(e -> {
            sorter.setRowFilter(null);
            searchField.setText("");
            oilFilter.setSelectedIndex(0);
            refresh.run();
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(addBtn); btnRow.add(editBtn); btnRow.add(refBtn);

        // ── Centre: filter + table ────────────────────────────────────────────
        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(filterRow, BorderLayout.NORTH);
        centre.add(scroll,    BorderLayout.CENTER);

        panel.add(centre, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    /** Called from Appointments page after marking a job done — skips the apt picker. */
    void showAddForAppointment(Appointment apt) {
        if (apt == null) return;
        Customer cust = FileManager.findCustomerById(apt.getCustomerId());

        JDialog d = new JDialog(main, "Add Vehicle Health Record — " + apt.getAppointmentId(), true);
        d.setSize(500, 520); d.setLocationRelativeTo(main);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIUtils.DARK_NAVY); p.setBorder(new EmptyBorder(16, 28, 16, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(7, 5, 7, 5);

        // Info row (read-only)
        int r = 0;
        JLabel aptInfo = new JLabel("<html><b>" + apt.getAppointmentId() + "</b>  |  "
                + apt.getServiceType() + "  |  " + apt.getDate()
                + "  |  " + (cust != null ? cust.getName() : apt.getCustomerId()) + "</html>");
        aptInfo.setForeground(UIUtils.ACCENT_BLUE);
        aptInfo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        gbc.gridx=0; gbc.gridy=r++; gbc.gridwidth=2; p.add(aptInfo, gbc); gbc.gridwidth=1;

        JTextField tyreF  = new JTextField("32 psi"); UIUtils.styleTextField(tyreF);
        JTextField brakeF = new JTextField("8 mm");   UIUtils.styleTextField(brakeF);
        JTextField oilF   = new JTextField("5W-30");  UIUtils.styleTextField(oilF);
        JTextField sparkF = new JTextField("Good");   UIUtils.styleTextField(sparkF);
        JTextField mileF  = new JTextField("0");       UIUtils.styleTextField(mileF);
        JTextField notesF = new JTextField();          UIUtils.styleTextField(notesF);

        main.addDRow(p, gbc, r++, "Tyre Pressure:",  tyreF);
        main.addDRow(p, gbc, r++, "Brake Thickness:",brakeF);
        main.addDRow(p, gbc, r++, "Oil Type:",       oilF);
        main.addDRow(p, gbc, r++, "Spark Plug:",     sparkF);
        main.addDRow(p, gbc, r++, "Mileage (km):",   mileF);
        main.addDRow(p, gbc, r++, "Notes:",          notesF);

        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Save Health Record", UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton skip = new UIUtils.RoundedButton("Skip", new Color(80, 80, 100));
        save.setPreferredSize(new Dimension(180, 34));
        skip.setPreferredSize(new Dimension(100, 34));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(save); btnRow.add(skip);
        gbc.gridx=0; gbc.gridy=r; gbc.gridwidth=2; p.add(btnRow, gbc);

        save.addActionListener(e -> {
            int km = 0;
            try { km = Integer.parseInt(mileF.getText().trim()); } catch (Exception ex) {}
            VehicleHealthRecord rec = new VehicleHealthRecord(
                    FileManager.generateHealthRecordId(),
                    apt.getAppointmentId(), apt.getCustomerId(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    tyreF.getText().trim(), brakeF.getText().trim(),
                    oilF.getText().trim(), sparkF.getText().trim(),
                    km, notesF.getText().trim(), main.currentTech.getUserId());
            FileManager.saveHealthRecord(rec);
            JOptionPane.showMessageDialog(d, "Health record saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        skip.addActionListener(e -> d.dispose());

        d.setContentPane(new JScrollPane(p));
        d.setVisible(true);
    }

    void showAddHealthRecordDialog() {
        // Only show completed appointments that belong to this technician
        List<Appointment> eligible = FileManager.readAllAppointments().stream()
                .filter(a -> a.getTechnicianId().equals(main.currentTech.getUserId())
                        && "COMPLETED".equalsIgnoreCase(a.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        if (eligible.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No completed appointments to add a record for.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog d = new JDialog((Dialog) null, "Add Vehicle Health Record", true);
        d.setSize(500, 500); d.setLocationRelativeTo(null);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIUtils.DARK_NAVY); p.setBorder(new EmptyBorder(16, 28, 16, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(7, 5, 7, 5);

        String[] aptOpts = eligible.stream().map(a -> {
            Customer c = FileManager.findCustomerById(a.getCustomerId());
            return a.getAppointmentId() + " | " + a.getDate() + " | " + ((c != null) ? c.getName() : a.getCustomerId());
        }).toArray(String[]::new);
        JComboBox<String> aptCb = new JComboBox<>(aptOpts); UIUtils.styleCombo(aptCb);
        JTextField tyreF   = new JTextField("32 psi"); UIUtils.styleTextField(tyreF);
        JTextField brakeF  = new JTextField("8 mm");   UIUtils.styleTextField(brakeF);
        JTextField oilF    = new JTextField("5W-30");  UIUtils.styleTextField(oilF);
        JTextField sparkF  = new JTextField("Good");   UIUtils.styleTextField(sparkF);
        JTextField mileF   = new JTextField("0");       UIUtils.styleTextField(mileF);
        JTextField notesF  = new JTextField();          UIUtils.styleTextField(notesF);

        int r = 0;
        main.addDRow(p, gbc, r++, "Appointment:",    aptCb);
        main.addDRow(p, gbc, r++, "Tyre Pressure:",  tyreF);
        main.addDRow(p, gbc, r++, "Brake Thickness:",brakeF);
        main.addDRow(p, gbc, r++, "Oil Type:",       oilF);
        main.addDRow(p, gbc, r++, "Spark Plug:",     sparkF);
        main.addDRow(p, gbc, r++, "Mileage (km):",   mileF);
        main.addDRow(p, gbc, r++, "Notes:",          notesF);

        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Save Record", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(180, 34));
        save.addActionListener(e -> {
            int idx = aptCb.getSelectedIndex(); if (idx < 0) return;
            Appointment apt = eligible.get(idx);
            int km = 0;
            try { km = Integer.parseInt(mileF.getText().trim()); } catch (Exception ex) {}
            VehicleHealthRecord rec = new VehicleHealthRecord(
                    FileManager.generateHealthRecordId(), apt.getAppointmentId(), apt.getCustomerId(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    tyreF.getText().trim(), brakeF.getText().trim(),
                    oilF.getText().trim(), sparkF.getText().trim(),
                    km, notesF.getText().trim(), main.currentTech.getUserId());
            FileManager.saveHealthRecord(rec);

            // Award points to VIP customer
            VipAccount vip = FileManager.findVipByCustomerId(apt.getCustomerId());
            if (vip != null) {
                Payment pay = FileManager.findPaymentByAppointmentId(apt.getAppointmentId());
                if (pay != null) {
                    vip.addPointsFromPayment(pay.getAmount());
                    FileManager.updateVipAccount(vip);
                }
            }

            JOptionPane.showMessageDialog(d, "Health record saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        gbc.gridx=0; gbc.gridy=r; gbc.gridwidth=2; p.add(save, gbc);
        d.setContentPane(new JScrollPane(p)); d.setVisible(true);
    }

    void showEditHealthRecordDialog(VehicleHealthRecord rec) {
        JDialog d = new JDialog((Dialog) null, "Edit Health Record - " + rec.getRecordId(), true);
        d.setSize(480, 440); d.setLocationRelativeTo(null);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIUtils.DARK_NAVY); p.setBorder(new EmptyBorder(16, 28, 16, 28));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(7, 5, 7, 5);

        JTextField tyreF  = new JTextField(rec.getTirePressure());   UIUtils.styleTextField(tyreF);
        JTextField brakeF = new JTextField(rec.getBrakeThickness());  UIUtils.styleTextField(brakeF);
        JTextField oilF   = new JTextField(rec.getOilType());         UIUtils.styleTextField(oilF);
        JTextField sparkF = new JTextField(rec.getSparkPlugStatus()); UIUtils.styleTextField(sparkF);
        JTextField mileF  = new JTextField(String.valueOf(rec.getMileage())); UIUtils.styleTextField(mileF);
        JTextField notesF = new JTextField(rec.getNotes());            UIUtils.styleTextField(notesF);

        int r = 0;
        main.addDRow(p, gbc, r++, "Tyre Pressure:",  tyreF);
        main.addDRow(p, gbc, r++, "Brake Thickness:",brakeF);
        main.addDRow(p, gbc, r++, "Oil Type:",       oilF);
        main.addDRow(p, gbc, r++, "Spark Plug:",     sparkF);
        main.addDRow(p, gbc, r++, "Mileage (km):",   mileF);
        main.addDRow(p, gbc, r++, "Notes:",          notesF);

        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Update Record", UIUtils.ACCENT_BLUE);
        save.setPreferredSize(new Dimension(180, 34));
        save.addActionListener(e -> {
            int km = 0;
            try { km = Integer.parseInt(mileF.getText().trim()); } catch (Exception ex) {}
            rec.setTirePressure(tyreF.getText().trim());
            rec.setBrakeThickness(brakeF.getText().trim());
            rec.setOilType(oilF.getText().trim());
            rec.setSparkPlugStatus(sparkF.getText().trim());
            rec.setMileage(km);
            rec.setNotes(notesF.getText().trim());
            FileManager.updateHealthRecord(rec);
            JOptionPane.showMessageDialog(d, "Record updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        gbc.gridx=0; gbc.gridy=r; gbc.gridwidth=2; p.add(save, gbc);
        d.setContentPane(p); d.setVisible(true);
    }


}
