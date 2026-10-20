package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

public class CustomerFeedbacksPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;

    public CustomerFeedbacksPage(CustomerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY); panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(main.sectionTitle("Technician Feedbacks for My Appointments", CustomerDashboard.ICON_BASE + "View Feedback(Blue).png"), BorderLayout.NORTH);
        Map<String, Appointment> aptMap = new HashMap<>();
        for (Appointment a : main.currentCustomer.getMyAppointments()) aptMap.put(a.getAppointmentId(), a);
        List<Feedback> feedbacks = main.currentCustomer.getMyFeedbacks();
        if (feedbacks.isEmpty()) { panel.add(main.emptyState("\uD83D\uDD0D", "No technician feedback available yet."), BorderLayout.CENTER); return panel; }
        JPanel list = new JPanel(); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(UIUtils.DARK_NAVY); list.setBorder(new EmptyBorder(0, 0, 10, 0));
        for (Feedback fb : feedbacks) {
            Appointment a = aptMap.get(fb.getAppointmentId());
            String aptInfo = a == null ? fb.getAppointmentId() : fb.getAppointmentId() + "  |  " + a.getDate() + "  |  " + a.getServiceType() + "  |  " + a.getVehicleInfo();
            JPanel card = new JPanel(new BorderLayout(6, 6)); card.setBackground(UIUtils.TABLE_BG);
            card.setBorder(BorderFactory.createCompoundBorder(new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1), new EmptyBorder(14, 18, 14, 18)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            JLabel aptLbl = new JLabel("Appointment: " + aptInfo); aptLbl.setForeground(UIUtils.ACCENT_BLUE); aptLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
            JLabel techLbl = new JLabel("Technician: " + fb.getTechnicianId() + "   |   Date: " + fb.getFeedbackDate()); techLbl.setForeground(UIUtils.DIM_TEXT); techLbl.setFont(UIUtils.uiFont(Font.ITALIC, 11));
            JPanel top = new JPanel(new GridLayout(2, 1, 0, 2)); top.setBackground(UIUtils.TABLE_BG); top.add(aptLbl); top.add(techLbl);
            JTextArea ta = new JTextArea(fb.getContent()); ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
            ta.setBackground(UIUtils.TABLE_BG); ta.setForeground(UIUtils.WHITE); ta.setFont(UIUtils.uiFont(Font.PLAIN, 13)); ta.setBorder(new EmptyBorder(6, 0, 0, 0));
            card.add(top, BorderLayout.NORTH); card.add(ta, BorderLayout.CENTER);
            list.add(card); list.add(Box.createVerticalStrut(10));
        }
        JScrollPane scroll = new JScrollPane(list); UIUtils.styleScrollPane(scroll);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }


}
