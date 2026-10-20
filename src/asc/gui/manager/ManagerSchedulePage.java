package asc.gui.manager;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * ManagerSchedulePage - Allows the manager to view, manage, reassign,
 * modify and delete appointments for technicians and counter staff.
 *
 * Features:
 *  - Tab-based filtering: Technicians | Counter Staff | All Appointments
 *  - Calendar grid with colour-coded appointment dots
 *  - Day detail timeline on the right
 *  - Edit dialog: reassign technician, change date/time, change service type, add notes
 *  - Delete appointment with confirmation
 *  - Add new appointment (quick schedule)
 */
public class ManagerSchedulePage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    // Tab filter
    private static final String TAB_ALL        = "All Appointments";
    private static final String TAB_TECH       = "Technicians";
    private static final String TAB_COUNTER    = "Counter Staff";

    public ManagerSchedulePage(ManagerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(UIUtils.DARK_NAVY);
        setBorder(new EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), BorderLayout.CENTER);
    }

    private JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Title
        panel.add(main.sectionTitle("Staff Schedule Management",
                ManagerDashboard.MGR_ICON_BASE + "Schedule.png"), BorderLayout.NORTH);

        // Tabbed pane for role-based filtering
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIUtils.TABLE_BG);
        tabs.setForeground(UIUtils.WHITE);
        tabs.setFont(UIUtils.uiFont(Font.BOLD, 12));
        tabs.addTab("Technicians",   buildSchedulePanel(TAB_TECH));
        tabs.addTab("Counter Staff", buildSchedulePanel(TAB_COUNTER));
        tabs.addTab("All Appointments", buildSchedulePanel(TAB_ALL));

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    //  Schedule panel (calendar + day detail) for a given filter
    // =========================================================================

    private JPanel buildSchedulePanel(String tabFilter) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Staff filter combo (for Technician / Counter Staff tabs)
        JComboBox<String> staffCombo = new JComboBox<>();
        staffCombo.setBackground(UIUtils.TABLE_BG);
        staffCombo.setForeground(UIUtils.WHITE);
        staffCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        staffCombo.setPreferredSize(new Dimension(220, 30));
        staffCombo.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 6, 1),
                new EmptyBorder(4, 8, 4, 8)));

        if (TAB_TECH.equals(tabFilter)) {
            staffCombo.addItem("-- All Technicians --");
            for (Technician t : FileManager.readAllTechnicians()) {
                staffCombo.addItem(t.getUserId() + " | " + t.getName());
            }
        } else if (TAB_COUNTER.equals(tabFilter)) {
            staffCombo.addItem("-- All Counter Staff --");
            for (CounterStaff cs : FileManager.readAllCounterStaff()) {
                staffCombo.addItem(cs.getUserId() + " | " + cs.getName());
            }
        } else {
            staffCombo.addItem("-- All Staff --");
            staffCombo.setEnabled(false);
        }

        // Action buttons
        UIUtils.RoundedButton addBtn     = new UIUtils.RoundedButton("+ New Appointment", UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton refreshBtn = new UIUtils.RoundedButton("Refresh", UIUtils.MEDIUM_NAVY);
        addBtn.setPreferredSize(new Dimension(160, 30));
        refreshBtn.setPreferredSize(new Dimension(100, 30));

        // Filter row
        JPanel filterRow = new JPanel(new BorderLayout(8, 0));
        filterRow.setBackground(UIUtils.DARK_NAVY);
        filterRow.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterLeft.setBackground(UIUtils.DARK_NAVY);
        filterLeft.add(staffCombo);
        filterLeft.add(addBtn);
        filterLeft.add(refreshBtn);
        filterRow.add(filterLeft, BorderLayout.WEST);

        // Calendar navigation
        final LocalDate[] selectedDate = { LocalDate.now() };

        JLabel monthLabel = new JLabel("", JLabel.CENTER);
        monthLabel.setFont(UIUtils.uiFont(Font.BOLD, 16));
        monthLabel.setForeground(UIUtils.ACCENT_BLUE);

        UIUtils.RoundedButton prevBtn  = new UIUtils.RoundedButton("\u2039  Prev",  new Color(60, 90, 150));
        UIUtils.RoundedButton nextBtn  = new UIUtils.RoundedButton("Next  \u203a", new Color(60, 90, 150));
        UIUtils.RoundedButton todayBtn = new UIUtils.RoundedButton("Today",         UIUtils.SUCCESS_GREEN);
        prevBtn .setPreferredSize(new Dimension(100, 30));
        nextBtn .setPreferredSize(new Dimension(100, 30));
        todayBtn.setPreferredSize(new Dimension(80,  30));

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        navLeft.setOpaque(false);
        navLeft.add(prevBtn); navLeft.add(todayBtn); navLeft.add(nextBtn);

        JPanel navRow = new JPanel(new BorderLayout(10, 0));
        navRow.setOpaque(false);
        navRow.add(navLeft,    BorderLayout.WEST);
        navRow.add(monthLabel, BorderLayout.CENTER);

        // Calendar grid
        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);
        calendarGrid.setBackground(UIUtils.DARK_NAVY);

        // Day detail panel
        JPanel daySchedule = new JPanel(new BorderLayout(0, 6));
        daySchedule.setBackground(UIUtils.TABLE_BG);
        daySchedule.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1),
                new EmptyBorder(14, 16, 14, 16)));
        daySchedule.setMinimumSize(new Dimension(380, 0));

        JLabel dayTitle = new JLabel("", JLabel.CENTER);
        dayTitle.setFont(UIUtils.uiFont(Font.BOLD, 14));
        dayTitle.setForeground(UIUtils.ACCENT_BLUE);
        dayTitle.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(UIUtils.TABLE_BG);

        JScrollPane timelineScroll = new JScrollPane(timelinePanel);
        timelineScroll.setBorder(null);
        timelineScroll.getViewport().setBackground(UIUtils.TABLE_BG);
        UIUtils.styleScrollPane(timelineScroll);

        daySchedule.add(dayTitle,       BorderLayout.NORTH);
        daySchedule.add(timelineScroll, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        legend.setOpaque(false);
        legend.add(legendItem("\u25CF  Scheduled",     UIUtils.LIGHT_GRAY));
        legend.add(legendItem("\u25CF  In Progress",   new Color(100, 180, 255)));
        legend.add(legendItem("\u25CF  Awaiting Pay",  UIUtils.WARN_YELLOW));
        legend.add(legendItem("\u25CF  Completed",     UIUtils.GREEN_OK));
        legend.add(legendItem("\u25A0  Today",         UIUtils.ACCENT_BLUE));

        // ---- Build-calendar lambda ----
        Runnable[] buildCalendar = { null };
        buildCalendar[0] = () -> {
            java.util.Map<String, java.util.List<Appointment>> apptMap = loadScheduleMap(tabFilter, staffCombo);
            calendarGrid.removeAll();
            LocalDate first = selectedDate[0].withDayOfMonth(1);
            monthLabel.setText(first.getMonth().getDisplayName(
                    java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH)
                    + "  " + first.getYear());

            for (String d : new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"}) {
                JLabel lbl = new JLabel(d, JLabel.CENTER);
                lbl.setFont(UIUtils.uiFont(Font.BOLD, 11));
                lbl.setForeground(UIUtils.LIGHT_GRAY);
                calendarGrid.add(lbl);
            }

            int startDow = first.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < startDow; i++) calendarGrid.add(new JLabel(""));

            for (int day = 1; day <= first.lengthOfMonth(); day++) {
                LocalDate date    = first.withDayOfMonth(day);
                String    dateStr = date.toString();
                boolean hasAppt  = apptMap.containsKey(dateStr);
                boolean isToday  = date.equals(LocalDate.now());
                boolean isSel    = date.equals(selectedDate[0]);

                int open = 0, inProg = 0, awaiting = 0, completed = 0;
                if (hasAppt) {
                    for (Appointment a : apptMap.get(dateStr)) {
                        if      (Appointment.STATUS_COMPLETED.equalsIgnoreCase(a.getStatus()))        completed++;
                        else if (Appointment.STATUS_AWAITING_PAYMENT.equalsIgnoreCase(a.getStatus())) awaiting++;
                        else if (Appointment.STATUS_IN_PROGRESS.equalsIgnoreCase(a.getStatus()))      inProg++;
                        else                                                                              open++;
                    }
                }

                final int fDay = day;
                JPanel cell = new JPanel(new BorderLayout(0, 2)) {
                    private static final long serialVersionUID = 1L;
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color bg = isSel    ? UIUtils.ACCENT_BLUE
                                 : isToday  ? new Color(40, 80, 150)
                                 : hasAppt  ? new Color(30, 55, 90)
                                 : UIUtils.TABLE_BG;
                        g2.setColor(bg);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                        if (isToday && !isSel) {
                            g2.setColor(UIUtils.ACCENT_BLUE);
                            g2.setStroke(new BasicStroke(2));
                            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                        }
                        g2.dispose();
                    }
                };
                cell.setOpaque(false);
                cell.setBorder(new EmptyBorder(4, 4, 4, 4));
                cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                JLabel dayNum = new JLabel(String.valueOf(day), JLabel.CENTER);
                dayNum.setFont(UIUtils.uiFont(isSel || isToday ? Font.BOLD : Font.PLAIN, 12));
                dayNum.setForeground(isSel ? Color.WHITE : isToday ? UIUtils.ACCENT_BLUE : UIUtils.WHITE);
                cell.add(dayNum, BorderLayout.NORTH);

                if (hasAppt) {
                    JPanel dots = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
                    dots.setOpaque(false);
                    if (open > 0)      dots.add(dot(UIUtils.LIGHT_GRAY));
                    if (inProg > 0)    dots.add(dot(new Color(100, 180, 255)));
                    if (awaiting > 0)  dots.add(dot(UIUtils.WARN_YELLOW));
                    if (completed > 0) dots.add(dot(UIUtils.GREEN_OK));
                    cell.add(dots, BorderLayout.CENTER);

                    int total = open + inProg + awaiting + completed;
                    JLabel cnt = new JLabel(total + " appt" + (total > 1 ? "s" : ""), JLabel.CENTER);
                    cnt.setFont(UIUtils.uiFont(Font.PLAIN, 9));
                    cnt.setForeground(UIUtils.LIGHT_GRAY);
                    cell.add(cnt, BorderLayout.SOUTH);
                }

                cell.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        selectedDate[0] = first.withDayOfMonth(fDay);
                        buildCalendar[0].run();
                    }
                });
                calendarGrid.add(cell);
            }

            calendarGrid.revalidate();
            calendarGrid.repaint();
            showDayDetail(selectedDate[0], apptMap, dayTitle, timelinePanel, timelineScroll, tabFilter);
        };

        // Wire navigation buttons
        prevBtn.addActionListener(e -> { selectedDate[0] = selectedDate[0].minusMonths(1).withDayOfMonth(1); buildCalendar[0].run(); });
        nextBtn.addActionListener(e -> { selectedDate[0] = selectedDate[0].plusMonths(1).withDayOfMonth(1);  buildCalendar[0].run(); });
        todayBtn.addActionListener(e -> { selectedDate[0] = LocalDate.now();                                  buildCalendar[0].run(); });
        refreshBtn.addActionListener(e -> buildCalendar[0].run());
        staffCombo.addActionListener(e -> buildCalendar[0].run());

        addBtn.addActionListener(e -> {
            showNewAppointmentDialog(tabFilter, staffCombo);
            buildCalendar[0].run();
        });

        // Build the calendar layout
        buildCalendar[0].run();

        JScrollPane calScroll = new JScrollPane(calendarGrid);
        calScroll.setBorder(new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1));
        calScroll.getViewport().setBackground(UIUtils.DARK_NAVY);
        calScroll.setBackground(UIUtils.DARK_NAVY);
        calScroll.getVerticalScrollBar().setUnitIncrement(12);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, calScroll, daySchedule);
        split.setResizeWeight(0.55);
        split.setDividerLocation(0.55);
        split.setDividerSize(6);
        split.setBackground(UIUtils.DARK_NAVY);
        split.setBorder(null);
        split.setOpaque(false);
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.55));

        panel.add(filterRow, BorderLayout.NORTH);
        panel.add(navRow,    BorderLayout.CENTER);
        // Wrap split + legend
        JPanel centerWrap = new JPanel(new BorderLayout(0, 8));
        centerWrap.setBackground(UIUtils.DARK_NAVY);
        centerWrap.add(split,  BorderLayout.CENTER);
        centerWrap.add(legend, BorderLayout.SOUTH);
        panel.add(centerWrap, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    //  Data loading with filter
    // =========================================================================

    private java.util.Map<String, java.util.List<Appointment>> loadScheduleMap(String tabFilter, JComboBox<String> staffCombo) {
        String selected = (String) staffCombo.getSelectedItem();
        String filterId = null;
        if (selected != null && !selected.startsWith("--")) {
            filterId = selected.split("\\|")[0].trim();
        }

        java.util.Map<String, java.util.List<Appointment>> map = new java.util.LinkedHashMap<>();
        List<Appointment> allApts = FileManager.readAllAppointments();

        for (Appointment a : allApts) {
            boolean include = false;

            switch (tabFilter) {
                case TAB_TECH:
                    if (filterId != null) {
                        include = a.getTechnicianId().equals(filterId);
                    } else {
                        include = true; // all technicians
                    }
                    break;
                case TAB_COUNTER:
                    if (filterId != null) {
                        include = a.getCounterStaffId().equals(filterId);
                    } else {
                        include = true; // all counter staff
                    }
                    break;
                case TAB_ALL:
                default:
                    include = true;
                    break;
            }

            if (include) {
                map.computeIfAbsent(a.getDate(), k -> new java.util.ArrayList<>()).add(a);
            }
        }

        // Sort each day's appointments by time
        for (java.util.List<Appointment> list : map.values()) {
            list.sort((a, b) -> a.getTime().compareTo(b.getTime()));
        }

        return map;
    }

    // =========================================================================
    //  Day detail with Edit / Delete buttons per appointment
    // =========================================================================

    private void showDayDetail(LocalDate date,
            java.util.Map<String, java.util.List<Appointment>> apptMap,
            JLabel dayTitle, JPanel timelinePanel,
            JScrollPane timelineScroll, String tabFilter) {

        dayTitle.setText(date.format(
                DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", java.util.Locale.ENGLISH)));
        timelinePanel.removeAll();

        java.util.List<Appointment> apts = new java.util.ArrayList<>(
                apptMap.getOrDefault(date.toString(), java.util.Collections.emptyList()));

        if (apts.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(UIUtils.TABLE_BG);
            JLabel none = new JLabel("No appointments on this day");
            none.setFont(UIUtils.uiFont(Font.ITALIC, 13));
            none.setForeground(UIUtils.LIGHT_GRAY);
            empty.add(none);
            timelinePanel.add(empty);
        } else {
            for (Appointment a : apts) {
                timelinePanel.add(buildAppointmentCard(a, tabFilter));
                timelinePanel.add(Box.createVerticalStrut(8));
            }
        }

        timelinePanel.revalidate();
        timelinePanel.repaint();
        timelineScroll.getVerticalScrollBar().setValue(0);
    }

    // =========================================================================
    //  Appointment card with Edit and Delete buttons
    // =========================================================================

    private JPanel buildAppointmentCard(Appointment a, String tabFilter) {
        Color accent = statusColor(a.getStatus());

        JPanel slot = new JPanel(new BorderLayout(8, 4)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 45, 78));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        slot.setOpaque(false);
        slot.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(accent, 10, 1),
                new EmptyBorder(10, 14, 10, 14)));
        slot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        slot.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Resolve staff names
        Customer   c  = FileManager.findCustomerById(a.getCustomerId());
        Technician t  = FileManager.findTechnicianById(a.getTechnicianId());
        CounterStaff cs = FileManager.findCounterStaffById(a.getCounterStaffId());

        // Top row: time + status
        JLabel timeLbl = new JLabel(a.getTime() + "  |  " + a.getDurationHours() + " hr");
        timeLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));
        timeLbl.setForeground(accent);

        JLabel statusLbl = new JLabel(a.getStatusLabel());
        statusLbl.setFont(UIUtils.uiFont(Font.BOLD, 11));
        statusLbl.setForeground(accent);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(timeLbl,   BorderLayout.WEST);
        topRow.add(statusLbl, BorderLayout.EAST);

        // Info section
        JLabel svcLbl = new JLabel(a.getServiceType() + " Service  -  " + a.getAppointmentId());
        svcLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));
        svcLbl.setForeground(UIUtils.WHITE);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.add(svcLbl);
        info.add(Box.createVerticalStrut(3));
        info.add(infoLbl("Customer:   " + (c != null ? c.getName() : a.getCustomerId())));
        info.add(infoLbl("Technician: " + (t != null ? t.getName() : a.getTechnicianId())));
        info.add(infoLbl("Counter:    " + (cs != null ? cs.getName() : a.getCounterStaffId())));
        if (a.getNotes() != null && !a.getNotes().isEmpty()) {
            info.add(infoLbl("Notes:      " + a.getNotes()));
        }

        // Action buttons (Edit + Delete)
        boolean canEdit = a.isEditable() || Appointment.STATUS_SCHEDULED.equalsIgnoreCase(a.getStatus());
        boolean canDelete = canEdit; // only editable appointments can be deleted

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);

        if (canEdit) {
            UIUtils.RoundedButton editBtn = new UIUtils.RoundedButton("Edit", UIUtils.ACCENT_BLUE);
            editBtn.setPreferredSize(new Dimension(70, 26));
            editBtn.setFont(UIUtils.uiFont(Font.BOLD, 11));
            final Appointment apt = a;
            editBtn.addActionListener(e -> {
                showEditAppointmentDialog(apt);
            });
            btnRow.add(editBtn);
        }

        if (canDelete) {
            UIUtils.RoundedButton delBtn = new UIUtils.RoundedButton("Delete", UIUtils.ERROR_RED);
            delBtn.setPreferredSize(new Dimension(70, 26));
            delBtn.setFont(UIUtils.uiFont(Font.BOLD, 11));
            final Appointment apt = a;
            delBtn.addActionListener(e -> {
                deleteAppointment(apt);
            });
            btnRow.add(delBtn);
        }

        // Layout
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        body.add(topRow, BorderLayout.NORTH);
        body.add(info,   BorderLayout.CENTER);
        body.add(btnRow, BorderLayout.SOUTH);

        slot.add(body, BorderLayout.CENTER);
        return slot;
    }

    // =========================================================================
    //  Edit Appointment Dialog
    // =========================================================================

    private void showEditAppointmentDialog(Appointment apt) {
        JDialog d = new JDialog((Dialog) null, "Edit Appointment - " + apt.getAppointmentId(), true);
        d.setSize(500, 480);
        d.setLocationRelativeTo(null);
        JPanel p = main.dlgPanel();
        GridBagConstraints gbc = main.dlgGbc();

        // Read-only fields
        JLabel aptIdLbl = new JLabel(apt.getAppointmentId());
        aptIdLbl.setForeground(UIUtils.ACCENT_BLUE);
        aptIdLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));

        Customer c = FileManager.findCustomerById(apt.getCustomerId());
        JLabel custLbl = new JLabel(c != null ? c.getName() + " (" + apt.getCustomerId() + ")" : apt.getCustomerId());
        custLbl.setForeground(UIUtils.WHITE);
        custLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));

        main.addRow(p, gbc, 0, "Appointment ID:", aptIdLbl);
        main.addRow(p, gbc, 1, "Customer:", custLbl);

        // Service type
        JComboBox<String> svcCombo = new JComboBox<>(new String[]{"NORMAL", "MAJOR"});
        svcCombo.setSelectedItem(apt.getServiceType());
        svcCombo.setBackground(UIUtils.TABLE_BG);
        svcCombo.setForeground(UIUtils.WHITE);
        svcCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        main.addRow(p, gbc, 2, "Service Type:", svcCombo);

        // Status
        String[] statusOptions;
        if (Appointment.STATUS_AWAITING_PAYMENT.equalsIgnoreCase(apt.getStatus())) {
            statusOptions = new String[]{
                    Appointment.STATUS_AWAITING_PAYMENT,
                    Appointment.STATUS_COMPLETED
            };
        } else if (Appointment.STATUS_IN_PROGRESS.equalsIgnoreCase(apt.getStatus())) {
            statusOptions = new String[]{
                    Appointment.STATUS_SCHEDULED,
                    Appointment.STATUS_IN_PROGRESS,
                    Appointment.STATUS_AWAITING_PAYMENT
            };
        } else if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(apt.getStatus())) {
            statusOptions = new String[]{ Appointment.STATUS_COMPLETED };
        } else {
            statusOptions = new String[]{
                    Appointment.STATUS_SCHEDULED,
                    Appointment.STATUS_IN_PROGRESS,
                    Appointment.STATUS_AWAITING_PAYMENT,
                    Appointment.STATUS_COMPLETED
            };
        }
        JComboBox<String> statusCombo = new JComboBox<>(statusOptions);
        statusCombo.setSelectedItem(apt.getStatus());
        statusCombo.setBackground(UIUtils.TABLE_BG);
        statusCombo.setForeground(UIUtils.WHITE);
        statusCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        main.addRow(p, gbc, 3, "Status:", statusCombo);

        // Technician reassignment
        JComboBox<String> techCombo = new JComboBox<>();
        techCombo.setBackground(UIUtils.TABLE_BG);
        techCombo.setForeground(UIUtils.WHITE);
        techCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        for (Technician t : FileManager.readAllTechnicians()) {
            techCombo.addItem(t.getUserId() + " | " + t.getName());
            if (t.getUserId().equals(apt.getTechnicianId())) {
                techCombo.setSelectedItem(t.getUserId() + " | " + t.getName());
            }
        }
        main.addRow(p, gbc, 4, "Technician:", techCombo);

        // Counter staff reassignment
        JComboBox<String> csCombo = new JComboBox<>();
        csCombo.setBackground(UIUtils.TABLE_BG);
        csCombo.setForeground(UIUtils.WHITE);
        csCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        for (CounterStaff cs : FileManager.readAllCounterStaff()) {
            csCombo.addItem(cs.getUserId() + " | " + cs.getName());
            if (cs.getUserId().equals(apt.getCounterStaffId())) {
                csCombo.setSelectedItem(cs.getUserId() + " | " + cs.getName());
            }
        }
        main.addRow(p, gbc, 5, "Counter Staff:", csCombo);

        // Date
        JTextField dateF = main.tf();
        dateF.setText(apt.getDate());
        main.addRow(p, gbc, 6, "Date (yyyy-MM-dd):", dateF);

        // Time
        String[] timeSlots = generateTimeSlots();
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        timeCombo.setSelectedItem(apt.getTime());
        timeCombo.setBackground(UIUtils.TABLE_BG);
        timeCombo.setForeground(UIUtils.WHITE);
        timeCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        main.addRow(p, gbc, 7, "Time:", timeCombo);

        // Notes
        JTextField notesF = main.tf();
        notesF.setText(apt.getNotes() != null ? apt.getNotes() : "");
        main.addRow(p, gbc, 8, "Notes:", notesF);

        // Vehicle info (read-only)
        JLabel vehicleLbl = new JLabel(apt.getVehicleInfo());
        vehicleLbl.setForeground(UIUtils.WHITE);
        vehicleLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        main.addRow(p, gbc, 9, "Vehicle:", vehicleLbl);

        // Save button
        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Update Appointment", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(200, 36));
        save.addActionListener(e -> {
            // Validate date
            String newDate = dateF.getText().trim();
            try {
                LocalDate.parse(newDate);
            } catch (Exception ex) {
                main.err(d, "Invalid date format. Use yyyy-MM-dd.");
                return;
            }

            String newTechId = ((String) techCombo.getSelectedItem()).split("\\|")[0].trim();
            String newCsId   = ((String) csCombo.getSelectedItem()).split("\\|")[0].trim();
            String newSvc    = (String) svcCombo.getSelectedItem();
            String newStatus = (String) statusCombo.getSelectedItem();
            String newTime   = (String) timeCombo.getSelectedItem();
            String newNotes  = notesF.getText().trim();

            // Check technician availability (skip if same tech, same date, same time)
            if (!newTechId.equals(apt.getTechnicianId())
                    || !newDate.equals(apt.getDate())
                    || !newTime.equals(apt.getTime())) {
                if (!FileManager.isTechnicianAvailable(newTechId, newDate, newTime, newSvc)) {
                    main.err(d, "Technician is not available at the selected time on " + newDate + ".");
                    return;
                }
            }

            // Apply changes
            apt.setTechnicianId(newTechId);
            apt.setCounterStaffId(newCsId);
            apt.setServiceType(newSvc);
            apt.setStatus(newStatus);
            apt.setDate(newDate);
            apt.setTime(newTime);
            apt.setNotes(newNotes);

            FileManager.updateAppointment(apt);

            JOptionPane.showMessageDialog(d, "Appointment updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });

        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        p.add(save, gbc);

        d.setContentPane(p);
        d.setVisible(true);
    }

    // =========================================================================
    //  Delete Appointment
    // =========================================================================

    private void deleteAppointment(Appointment apt) {
        String msg = "Delete this appointment?\n\n"
                + "ID: " + apt.getAppointmentId() + "\n"
                + "Service: " + apt.getServiceType() + "\n"
                + "Date: " + apt.getDate() + " " + apt.getTime() + "\n"
                + "Status: " + apt.getStatusLabel();

        int confirm = JOptionPane.showConfirmDialog(this, msg,
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // Delete the appointment
        FileManager.deleteAppointment(apt.getAppointmentId());

        JOptionPane.showMessageDialog(this,
                "Appointment " + apt.getAppointmentId() + " deleted.",
                "Deleted", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================================
    //  New Appointment Dialog (Quick Schedule)
    // =========================================================================

    private void showNewAppointmentDialog(String tabFilter, JComboBox<String> staffCombo) {
        JDialog d = new JDialog((Dialog) null, "New Appointment", true);
        d.setSize(500, 520);
        d.setLocationRelativeTo(null);
        JPanel p = main.dlgPanel();
        GridBagConstraints gbc = main.dlgGbc();

        // Customer
        JComboBox<String> custCombo = new JComboBox<>();
        custCombo.setBackground(UIUtils.TABLE_BG);
        custCombo.setForeground(UIUtils.WHITE);
        custCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        custCombo.addItem("-- Select Customer --");
        for (Customer c : FileManager.readAllCustomers()) {
            custCombo.addItem(c.getUserId() + " | " + c.getName());
        }
        main.addRow(p, gbc, 0, "Customer:", custCombo);

        // Service type
        JComboBox<String> svcCombo = new JComboBox<>(new String[]{"NORMAL", "MAJOR"});
        svcCombo.setBackground(UIUtils.TABLE_BG);
        svcCombo.setForeground(UIUtils.WHITE);
        svcCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        svcCombo.setSelectedItem("NORMAL");
        main.addRow(p, gbc, 1, "Service Type:", svcCombo);

        // Technician
        JComboBox<String> techCombo = new JComboBox<>();
        techCombo.setBackground(UIUtils.TABLE_BG);
        techCombo.setForeground(UIUtils.WHITE);
        techCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        techCombo.addItem("-- Select Technician --");
        for (Technician t : FileManager.readAllTechnicians()) {
            techCombo.addItem(t.getUserId() + " | " + t.getName());
        }
        main.addRow(p, gbc, 2, "Technician:", techCombo);

        // Counter staff
        JComboBox<String> csCombo = new JComboBox<>();
        csCombo.setBackground(UIUtils.TABLE_BG);
        csCombo.setForeground(UIUtils.WHITE);
        csCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        csCombo.addItem("-- Select Counter Staff --");
        for (CounterStaff cs : FileManager.readAllCounterStaff()) {
            csCombo.addItem(cs.getUserId() + " | " + cs.getName());
        }
        main.addRow(p, gbc, 3, "Counter Staff:", csCombo);

        // Date
        JTextField dateF = main.tf();
        dateF.setText(LocalDate.now().toString());
        main.addRow(p, gbc, 4, "Date (yyyy-MM-dd):", dateF);

        // Time
        String[] timeSlots = generateTimeSlots();
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        timeCombo.setBackground(UIUtils.TABLE_BG);
        timeCombo.setForeground(UIUtils.WHITE);
        timeCombo.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        timeCombo.setSelectedItem("09:00");
        main.addRow(p, gbc, 5, "Time:", timeCombo);

        // Vehicle
        JTextField vehicleF = main.tf();
        main.addRow(p, gbc, 6, "Vehicle:", vehicleF);

        // Notes
        JTextField notesF = main.tf();
        main.addRow(p, gbc, 7, "Notes:", notesF);

        // Save
        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Create Appointment", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(200, 36));
        save.addActionListener(e -> {
            String custSel  = (String) custCombo.getSelectedItem();
            String techSel  = (String) techCombo.getSelectedItem();
            String csSel    = (String) csCombo.getSelectedItem();

            if (custSel == null || custSel.startsWith("--")) { main.err(d, "Please select a customer."); return; }
            if (techSel == null || techSel.startsWith("--")) { main.err(d, "Please select a technician."); return; }
            if (csSel == null || csSel.startsWith("--"))     { main.err(d, "Please select a counter staff."); return; }

            String custId = custSel.split("\\|")[0].trim();
            String techId = techSel.split("\\|")[0].trim();
            String csId   = csSel.split("\\|")[0].trim();
            String svc    = (String) svcCombo.getSelectedItem();
            String date   = dateF.getText().trim();
            String time   = (String) timeCombo.getSelectedItem();
            String vehicle = vehicleF.getText().trim();
            String notes  = notesF.getText().trim();

            // Validate date
            try { LocalDate.parse(date); } catch (Exception ex) {
                main.err(d, "Invalid date format. Use yyyy-MM-dd."); return;
            }

            // Check availability
            if (!FileManager.isTechnicianAvailable(techId, date, time, svc)) {
                main.err(d, "Technician is not available at " + time + " on " + date + ".");
                return;
            }

            Appointment apt = new Appointment(
                    FileManager.generateAppointmentId(),
                    custId, techId, csId, svc,
                    Appointment.STATUS_SCHEDULED,
                    date, time, vehicle, notes);

            FileManager.saveAppointment(apt);

            JOptionPane.showMessageDialog(d,
                    "Appointment created!\nID: " + apt.getAppointmentId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        p.add(save, gbc);

        d.setContentPane(p);
        d.setVisible(true);
    }

    // =========================================================================
    //  UI Helpers
    // =========================================================================

    private String[] generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            slots.add(String.format("%02d:00", h));
            slots.add(String.format("%02d:30", h));
        }
        slots.add("18:00");
        return slots.toArray(new String[0]);
    }

    private JLabel infoLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtils.uiFont(Font.PLAIN, 11));
        l.setForeground(UIUtils.LIGHT_GRAY);
        return l;
    }

    private JLabel dot(Color color) {
        JLabel d = new JLabel("\u25CF");
        d.setForeground(color);
        d.setFont(UIUtils.uiFont(Font.PLAIN, 8));
        return d;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(UIUtils.uiSymbolFont(Font.BOLD, 11));
        return l;
    }

    private Color statusColor(String status) {
        if (Appointment.STATUS_COMPLETED.equalsIgnoreCase(status))        return UIUtils.GREEN_OK;
        if (Appointment.STATUS_AWAITING_PAYMENT.equalsIgnoreCase(status)) return UIUtils.WARN_YELLOW;
        if (Appointment.STATUS_IN_PROGRESS.equalsIgnoreCase(status))      return new Color(100, 180, 255);
        if (Appointment.STATUS_SCHEDULED.equalsIgnoreCase(status))       return UIUtils.LIGHT_GRAY;
        return UIUtils.DIM_TEXT;
    }
}
