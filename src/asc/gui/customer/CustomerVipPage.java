package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class CustomerVipPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;

    public CustomerVipPage(CustomerDashboard m) {
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
        panel.add(main.sectionTitle("My VIP Status & Coupons", CustomerDashboard.ICON_BASE + "VIP & Coupons(Blue).png"), BorderLayout.NORTH);
        

        VipAccount vip = FileManager.findVipByCustomerId(main.currentCustomer.getUserId());

        // Auto-issue monthly coupon if eligible and notify user
        if (vip != null) {
            Coupon newCpn = FileManager.issueMonthlyCouponIfNeeded(vip);
            if (newCpn != null) {
                JOptionPane.showMessageDialog(this,
                    String.format("Your monthly VIP coupon has been issued!\n\nCode: %s\nValue: RM %.2f off\nExpires: %s",
                        newCpn.getCode(), newCpn.getDiscountValue(), newCpn.getExpiryDate()),
                    "Monthly Coupon Issued", JOptionPane.INFORMATION_MESSAGE);
                vip = FileManager.findVipByCustomerId(main.currentCustomer.getUserId());
            }
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.DARK_NAVY);

        // â”€â”€ Main VIP banner card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        final VipAccount fVip = vip;
        JPanel vipCard = new JPanel(new BorderLayout(16, 8)) {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1, c2;
                if (fVip == null) { c1 = new Color(40,60,110); c2 = new Color(25,40,80); }
                else switch (fVip.getTier()) {
                    case VipAccount.TIER_BLACKGOLD: c1=new Color(120,90,0);  c2=new Color(60,40,0);  break;
                    case VipAccount.TIER_GOLD:      c1=new Color(140,100,10);c2=new Color(80,55,0);  break;
                    case VipAccount.TIER_BRONZE:    c1=new Color(110,65,20); c2=new Color(65,35,10); break;
                    default:                        c1=new Color(40,65,120); c2=new Color(25,45,90); break;
                }
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),getHeight(),c2));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.dispose();
            }
        };
        vipCard.setOpaque(false);
        vipCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 215));
        vipCard.setBorder(BorderFactory.createCompoundBorder(
            new UIUtils.RoundedBorder(vip != null ? vip.tierColor() : UIUtils.ACCENT_BLUE, 18, 1),
            new EmptyBorder(22, 28, 18, 28)));

        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);

        if (vip != null) {
            Color tc = vip.tierColor();
            JLabel tierLbl = new JLabel(vip.tierBadge());
            tierLbl.setFont(UIUtils.uiFont(Font.BOLD, 26)); tierLbl.setForeground(tc);

            JLabel ptsLbl = new JLabel(String.format("%,d pts  \u2014  Lifetime spend: RM %.0f",
                    vip.getPoints(), vip.getTotalSpent()));
            ptsLbl.setFont(UIUtils.uiFont(Font.BOLD, 13)); ptsLbl.setForeground(UIUtils.WHITE);

            double disc = vip.getDiscountPct();
            JLabel discLbl = new JLabel(disc > 0
                    ? String.format("Service Discount: %.0f%% off all services", disc)
                    : "No service discount  (reach GOLD for 2% off)");
            discLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));
            discLbl.setForeground(disc > 0 ? UIUtils.GREEN_OK : UIUtils.DIM_TEXT);

            String cpnTxt;
            if (VipAccount.TIER_BLACKGOLD.equals(vip.getTier()))  cpnTxt = "Monthly Coupon: RM 50.00 / month";
            else if (VipAccount.TIER_NONE.equals(vip.getTier()))  cpnTxt = "No monthly coupon  (reach BRONZE for RM 20/month)";
            else                                                    cpnTxt = "Monthly Coupon: RM 20.00 / month";
            JLabel cpnLbl = new JLabel(cpnTxt);
            cpnLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));
            cpnLbl.setForeground(VipAccount.TIER_NONE.equals(vip.getTier()) ? UIUtils.DIM_TEXT : CustomerDashboard.ACCENT_YELLOW);

            JLabel phoneLbl = new JLabel("VIP Phone: " + vip.getPhone()
                    + "   |   Member since: " + vip.getCreatedDate());
            phoneLbl.setFont(UIUtils.uiFont(Font.PLAIN, 11));
            phoneLbl.setForeground(UIUtils.DIM_TEXT);

            leftInfo.add(tierLbl); leftInfo.add(Box.createVerticalStrut(6));
            leftInfo.add(ptsLbl); leftInfo.add(Box.createVerticalStrut(3));
            leftInfo.add(discLbl); leftInfo.add(Box.createVerticalStrut(3));
            leftInfo.add(cpnLbl); leftInfo.add(Box.createVerticalStrut(3));
            leftInfo.add(phoneLbl); leftInfo.add(Box.createVerticalStrut(10));
            leftInfo.add(main.buildTierProgressBar(vip));
        } else {
            JLabel noVip = new JLabel("Not a VIP Member");
            noVip.setFont(UIUtils.uiFont(Font.BOLD, 18)); noVip.setForeground(UIUtils.LIGHT_GRAY);
            JLabel hint  = new JLabel("Contact Counter Staff to enrol and earn points  (1 RM spent = 1 pt).");
            hint.setFont(UIUtils.uiFont(Font.PLAIN, 12)); hint.setForeground(UIUtils.DIM_TEXT);
            leftInfo.add(noVip); leftInfo.add(Box.createVerticalStrut(6)); leftInfo.add(hint);
        }
        vipCard.add(leftInfo, BorderLayout.CENTER);
        content.add(vipCard);
        content.add(Box.createVerticalStrut(12));

        // â”€â”€ Tier benefits comparison row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (vip != null) {
            JPanel benefitsRow = main.buildTierBenefitsRow(vip);
            benefitsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            content.add(benefitsRow);
            content.add(Box.createVerticalStrut(12));
            content.add(main.buildHealthRecordsSection());
            content.add(Box.createVerticalStrut(12));
        }

        // â”€â”€ Coupons table â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JLabel coupHdr = new JLabel("My Coupons");
        coupHdr.setFont(UIUtils.uiFont(Font.BOLD, 14));
        coupHdr.setForeground(CustomerDashboard.ACCENT_YELLOW);
        coupHdr.setBorder(new EmptyBorder(4, 0, 6, 0));
        content.add(coupHdr);

        String[] cols = {"Coupon ID","Code","Value","Expiry","Status","Issued By","Date"};
        DefaultTableModel dm = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Coupon cp : FileManager.findCouponsByCustomerId(main.currentCustomer.getUserId())) {
            String disc = "FIXED".equals(cp.getDiscountType())
                    ? "RM " + String.format("%.2f", cp.getDiscountValue())
                    : cp.getDiscountValue() + "%";
            dm.addRow(new Object[]{ cp.getCouponId(), cp.getCode(), disc,
                    cp.getExpiryDate(), cp.isUsed() ? "USED" : "AVAILABLE",
                    cp.getIssuedBy(), cp.getIssuedDate() });
        }
        JTable couponTable = UIUtils.createStyledTable(dm);
        couponTable.setRowHeight(28);
        couponTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                setForeground("AVAILABLE".equals(v) ? CustomerDashboard.GREEN_OK : UIUtils.DIM_TEXT);
                setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                setHorizontalAlignment(CENTER);
                return this;
            }
        });
        JScrollPane couponScroll = new JScrollPane(couponTable);
        couponScroll.setPreferredSize(new Dimension(-1, 185));
        UIUtils.styleScrollPane(couponScroll);
        content.add(couponScroll);

        JScrollPane outer = new JScrollPane(content);
        outer.getVerticalScrollBar().setUnitIncrement(16);
        UIUtils.styleScrollPane(outer);
        panel.add(outer, BorderLayout.CENTER);
        return panel;
    }

}
