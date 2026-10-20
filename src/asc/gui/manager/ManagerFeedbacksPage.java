package asc.gui.manager;

import asc.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class ManagerFeedbacksPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    public ManagerFeedbacksPage(ManagerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel=new JPanel(new BorderLayout(0,10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(18,20,18,20));
        panel.add(main.sectionTitle("Feedbacks & Customer Comments", ManagerDashboard.MGR_ICON_BASE+"Feedbacks & Comments(Blue).png"), BorderLayout.NORTH);

        JTabbedPane tabs=new JTabbedPane(); tabs.setBackground(UIUtils.TABLE_BG); tabs.setForeground(UIUtils.WHITE);

        // Feedbacks
        main.feedbackModel=new DefaultTableModel(new String[]{"FB ID","Apt ID","Tech ID","Technician","Content","Date"},0){public boolean isCellEditable(int r,int c){return false;}};
        main.feedbackTable=UIUtils.createStyledTable(main.feedbackModel);
        main.feedbackTable.getColumnModel().getColumn(4).setPreferredWidth(280);
        JScrollPane fbSp=new JScrollPane(main.feedbackTable); UIUtils.styleScrollPane(fbSp);
        JPanel fbPanel=new JPanel(new BorderLayout()); fbPanel.setBackground(UIUtils.DARK_NAVY); fbPanel.setBorder(new EmptyBorder(8,8,8,8));
        fbPanel.add(fbSp,BorderLayout.CENTER);
        UIUtils.RoundedButton rfBtn=new UIUtils.RoundedButton("↻ Refresh",UIUtils.MEDIUM_NAVY);
        rfBtn.addActionListener(e->main.refreshFeedbacks()); rfBtn.setPreferredSize(new Dimension(110,30));
        JPanel fbBtnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT)); fbBtnRow.setBackground(UIUtils.DARK_NAVY); fbBtnRow.add(rfBtn);
        fbPanel.add(fbBtnRow,BorderLayout.SOUTH);

        // Comments
        main.commentModel=new DefaultTableModel(new String[]{"CMT ID","Apt ID","Customer","CS Comment","CS ★","Tech Comment","Tech ★","Date"},0){public boolean isCellEditable(int r,int c){return false;}};
        main.commentTable=UIUtils.createStyledTable(main.commentModel);
        main.commentTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        main.commentTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        JScrollPane cmtSp=new JScrollPane(main.commentTable); UIUtils.styleScrollPane(cmtSp);
        JPanel cmtPanel=new JPanel(new BorderLayout()); cmtPanel.setBackground(UIUtils.DARK_NAVY); cmtPanel.setBorder(new EmptyBorder(8,8,8,8));
        cmtPanel.add(cmtSp,BorderLayout.CENTER);
        UIUtils.RoundedButton rcBtn=new UIUtils.RoundedButton("↻ Refresh",UIUtils.MEDIUM_NAVY);
        rcBtn.addActionListener(e->main.refreshComments()); rcBtn.setPreferredSize(new Dimension(110,30));
        JPanel cmtBtnRow=new JPanel(new FlowLayout(FlowLayout.RIGHT)); cmtBtnRow.setBackground(UIUtils.DARK_NAVY); cmtBtnRow.add(rcBtn);
        cmtPanel.add(cmtBtnRow,BorderLayout.SOUTH);

        main.refreshFeedbacks(); main.refreshComments();
        tabs.addTab("Technician Feedbacks",fbPanel);
        tabs.addTab("Customer Comments",cmtPanel);
        panel.add(tabs,BorderLayout.CENTER); return panel;
    }


}
