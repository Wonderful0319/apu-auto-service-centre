package asc.gui.manager;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ManagerApprovalsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    public ManagerApprovalsPage(ManagerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        panel.add(main.sectionTitle("Pending Registrations", ManagerDashboard.MGR_ICON_BASE + "Pending Approvals(Blue).png"), BorderLayout.NORTH);

        String[] cols = {"Reg ID", "Username", "Role", "Full Name", "Email", "Phone", "Applied Date", "Status"};
        main.pendingModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        main.pendingTable = UIUtils.createStyledTable(main.pendingModel);
        main.pendingTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        main.pendingTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        main.pendingTable.getColumnModel().getColumn(4).setPreferredWidth(170);

        // Status column colouring
        main.pendingTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = v == null ? "" : v.toString();
                setForeground("PENDING".equals(s) ? ManagerDashboard.ACCENT_YELLOW : ("APPROVED".equals(s) ? ManagerDashboard.GREEN_OK : UIUtils.ERROR_RED));
                setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        main.refreshPendingTable();
        JScrollPane scroll = new JScrollPane(main.pendingTable); UIUtils.styleScrollPane(scroll);

        UIUtils.RoundedButton approveBtn = new UIUtils.RoundedButton("\u2714  Approve", UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton rejectBtn  = new UIUtils.RoundedButton("\u2716  Reject",  UIUtils.ERROR_RED);
        UIUtils.RoundedButton detailBtn  = new UIUtils.RoundedButton("\uD83D\uDCCB  View Details", UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton refreshBtn = new UIUtils.RoundedButton("\u21BB  Refresh", UIUtils.MEDIUM_NAVY);
        approveBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        rejectBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        detailBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        refreshBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));

        approveBtn.setPreferredSize(new Dimension(130, 34));
        rejectBtn .setPreferredSize(new Dimension(130, 34));
        detailBtn .setPreferredSize(new Dimension(150, 34));
        refreshBtn.setPreferredSize(new Dimension(110, 34));

        approveBtn.addActionListener(e -> handleApproval(true));
        rejectBtn .addActionListener(e -> handleApproval(false));
        detailBtn .addActionListener(e -> showRegDetail());
        refreshBtn.addActionListener(e -> main.refreshPendingTable());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(approveBtn); btnRow.add(rejectBtn); btnRow.add(detailBtn); btnRow.add(refreshBtn);

        panel.add(scroll, BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    void showRegDetail() {
        int row = main.pendingTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a registration first."); return; }
        String regId = (String) main.pendingModel.getValueAt(row, 0);
        PendingRegistration reg = FileManager.readAllPendingRegistrations().stream()
                .filter(r -> r.getRegId().equals(regId)).findFirst().orElse(null);
        if (reg == null) return;
        String info = "Registration ID : " + reg.getRegId() + "\n"
                + "Role           : " + reg.getRole() + "\n"
                + "Username       : " + reg.getUsername() + "\n"
                + "Full Name      : " + reg.getName() + "\n"
                + "Gender         : " + reg.getGender() + "\n"
                + "Email          : " + reg.getEmail() + "\n"
                + "Phone          : " + reg.getPhone() + "\n"
                + "Vehicle        : " + (reg.getVehicleInfo().isEmpty() ? "N/A" : reg.getVehicleInfo()) + "\n"
                + "Applied Date   : " + reg.getAppliedDate() + "\n"
                + "Status         : " + reg.getStatus();
        JOptionPane.showMessageDialog(this, info, "Registration Details", JOptionPane.INFORMATION_MESSAGE);
    }

    void handleApproval(boolean approve) {
        int row = main.pendingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a pending registration first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String regId  = (String) main.pendingModel.getValueAt(row, 0);
        String status = (String) main.pendingModel.getValueAt(row, 7);
        if (!"PENDING".equals(status)) {
            JOptionPane.showMessageDialog(this, "This registration has already been reviewed.", "Already Reviewed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PendingRegistration reg = FileManager.readAllPendingRegistrations().stream()
                .filter(r -> r.getRegId().equals(regId)).findFirst().orElse(null);
        if (reg == null) return;

        String action = approve ? "APPROVE" : "REJECT";
        int confirm = JOptionPane.showConfirmDialog(this,
                action + " registration for " + reg.getName() + " (" + reg.getRole() + ")?",
                "Confirm " + action, JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        reg.setStatus(approve ? "APPROVED" : "REJECTED");
        reg.setReviewedBy(main.currentManager.getUserId());
        reg.setReviewedDate(LocalDate.now().toString());
        FileManager.updatePendingReg(reg);

        if (approve) {
            switch (reg.getRole()) {
                case "CUSTOMER":
                    String custId = FileManager.generateCustomerId();
                    FileManager.saveCustomer(new Customer(custId, reg.getUsername(), reg.getPassword(),
                            reg.getName(), reg.getEmail(), reg.getPhone(), reg.getVehicleInfo(), reg.getGender()));
                    main.movePendingAvatar(reg.getRegId(), custId);
                    break;
                case "COUNTER_STAFF":
                    String csId = FileManager.generateCounterStaffId();
                    FileManager.saveCounterStaff(new CounterStaff(csId, reg.getUsername(), reg.getPassword(),
                            reg.getName(), reg.getEmail(), reg.getPhone(), reg.getGender()));
                    main.movePendingAvatar(reg.getRegId(), csId);
                    break;
                case "TECHNICIAN":
                    String techId = FileManager.generateTechnicianId();
                    FileManager.saveTechnician(new Technician(techId, reg.getUsername(), reg.getPassword(),
                            reg.getName(), reg.getEmail(), reg.getPhone(), reg.getGender()));
                    main.movePendingAvatar(reg.getRegId(), techId);
                    break;
                case "MANAGER":
                    String mgrId = FileManager.generateManagerId();
                    FileManager.saveManager(new Manager(mgrId, reg.getUsername(), reg.getPassword(),
                            reg.getName(), reg.getEmail(), reg.getPhone(), reg.getGender()));
                    main.movePendingAvatar(reg.getRegId(), mgrId);
                    break;
            }
            JOptionPane.showMessageDialog(this, "Registration APPROVED. Account created successfully!", "Approved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Registration REJECTED.", "Rejected", JOptionPane.WARNING_MESSAGE);
        }
        main.refreshPendingTable();
    }

}


