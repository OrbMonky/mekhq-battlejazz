/*
 * Copyright (c) 2024 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.gui.dialog;

import megamek.client.bot.princess.BehaviorSettings;
import megamek.client.ui.dialogs.BotConfigDialog;
import megamek.client.ui.dialogs.CamoChooserDialog;
import megamek.common.Entity;
import megamek.common.EntityListFile;
import megamek.common.MULParser;
import mekhq.MekHQ;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.BotForce;
import mekhq.gui.FileDialogs;
import mekhq.gui.baseComponents.DefaultMHQScrollablePanel;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;

public class CustomizeBotForceDialog  extends JDialog {

    private JFrame frame;
    private BotForce botForce;
    private Campaign campaign;

    //gui components
    private JTextField txtName;
    private JComboBox<String> choiceTeam;
    private JButton btnCamo;
    private JPanel panBehavior;
    private DefaultMHQScrollablePanel panFixedEntity;
    private JButton btnLoadUnits;
    private JButton btnSaveUnits;
    private JButton btnDeleteUnits;
    private JLabel lblCowardice;
    private JLabel lblSelfPreservation;
    private JLabel lblAggression;
    private JLabel lblHerdMentality;
    private JLabel lblPilotingRisk;
    private JLabel lblForcedWithdrawal;
    private JLabel lblAutoFlee;

    public CustomizeBotForceDialog(JFrame parent, boolean modal, BotForce bf, Campaign c) {
        super(parent, modal);
        this.frame = parent;
        if (null == bf) {
            botForce = new BotForce();
            botForce.setName("New Bot Force");
            // assume enemy by default
            botForce.setTeam(2);
        } else {
            botForce = bf;
        }
        campaign = c;
        initComponents();
        setLocationRelativeTo(parent);
        pack();
    }

    private void initComponents() {

        final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.CustomizeBotForceDialog",
                MekHQ.getMHQOptions().getLocale());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceMap.getString("title"));

        getContentPane().setLayout(new BorderLayout());
        JPanel panName = new JPanel(new GridBagLayout());
        JPanel panLeft = new JPanel(new GridBagLayout());
        JPanel panCenter = new JPanel(new GridBagLayout());

        //panMain.add(panLeft);
        //panMain.add(panRight);
        //getContentPane().add(panMain, BorderLayout.CENTER);
        getContentPane().add(panName, BorderLayout.NORTH);
        getContentPane().add(panLeft, BorderLayout.WEST);
        getContentPane().add(panCenter, BorderLayout.CENTER);

        JPanel panButtons = new JPanel(new GridLayout(0, 2));
        JButton btnOK = new JButton(resourceMap.getString("btnOK.text"));
        btnOK.addActionListener(this::done);
        JButton btnClose = new JButton(resourceMap.getString("btnClose.text"));
        btnClose.addActionListener(this::cancel);
        panButtons.add(btnOK);
        panButtons.add(btnClose);
        getContentPane().add(panButtons, BorderLayout.PAGE_END);

        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panName.add(new JLabel(resourceMap.getString("lblName.text")), gbc);

        txtName = new JTextField(botForce.getName());
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panName.add(txtName, gbc);

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panLeft.add(new JLabel(resourceMap.getString("lblTeam.text")), gbc);

        choiceTeam = new JComboBox();
        for (int i = 1; i < 6; i++) {
            String choice = resourceMap.getString("choiceTeam.text") + " " + i;
            if (i ==1) {
                choice = choice + " (" + resourceMap.getString("choiceAllied.text") + ")";
            }
            choiceTeam.addItem(choice);
        }
        choiceTeam.setSelectedIndex(botForce.getTeam() - 1);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panLeft.add(choiceTeam, gbc);

        btnCamo = new JButton();
        btnCamo.setIcon(botForce.getCamouflage().getImageIcon());
        btnCamo.setMinimumSize(new Dimension(84, 72));
        btnCamo.setPreferredSize(new Dimension(84, 72));
        btnCamo.setMaximumSize(new Dimension(84, 72));
        btnCamo.addActionListener(this::editCamo);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        panLeft.add(btnCamo, gbc);


        intBehaviorPanel(resourceMap);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        panLeft.add(panBehavior, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        btnLoadUnits = new JButton(resourceMap.getString("btnLoadUnits.text"));
        btnLoadUnits.setToolTipText(resourceMap.getString("btnLoadUnits.tooltip"));
        btnLoadUnits.addActionListener(this::loadUnits);
        panCenter.add(btnLoadUnits);
        gbc.gridx++;
        btnSaveUnits = new JButton(resourceMap.getString("btnSaveUnits.text"));
        btnSaveUnits.setToolTipText(resourceMap.getString("btnSaveUnits.tooltip"));
        btnSaveUnits.addActionListener(this::saveUnits);
        panCenter.add(btnSaveUnits);
        gbc.gridx++;
        gbc.weightx = 1.0;
        btnDeleteUnits = new JButton(resourceMap.getString("btnDeleteUnits.text"));
        btnDeleteUnits.setToolTipText(resourceMap.getString("btnDeleteUnits.tooltip"));
        btnDeleteUnits.addActionListener(this::deleteUnits);
        panCenter.add(btnDeleteUnits, gbc);

        panFixedEntity = new DefaultMHQScrollablePanel(frame, "panFixedEntity", new GridBagLayout());
        refreshFixedEntityPanel();
        JScrollPane scrollFixedEntity = new JScrollPane(panFixedEntity);
        scrollFixedEntity.setMinimumSize(new Dimension(400, 200));
        scrollFixedEntity.setPreferredSize(new Dimension(400, 200));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        panCenter.add(scrollFixedEntity, gbc);

    }

    private void intBehaviorPanel(ResourceBundle resourceMap) {
        panBehavior = new JPanel(new GridBagLayout());
        panBehavior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(resourceMap.getString("panBehavior.title")),
                BorderFactory.createEmptyBorder(5,5,5,5)));

        BehaviorSettings behavior = botForce.getBehaviorSettings();

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.weightx = 0.0;
        gbcLeft.weighty = 0.0;
        gbcLeft.fill = GridBagConstraints.NONE;
        gbcLeft.anchor = GridBagConstraints.WEST;
        gbcLeft.insets = new Insets(0, 0, 0, 5);

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 1;
        gbcRight.gridy = 0;
        gbcRight.weightx = 1.0;
        gbcRight.weighty = 0.0;
        gbcRight.fill = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.CENTER;
        gbcRight.insets = new Insets(0, 5, 0, 0);

        lblCowardice = new JLabel(Integer.toString(behavior.getBraveryIndex()));
        lblSelfPreservation = new JLabel(Integer.toString(behavior.getSelfPreservationIndex()));
        lblAggression = new JLabel(Integer.toString(behavior.getHyperAggressionIndex()));
        lblHerdMentality = new JLabel(Integer.toString(behavior.getHerdMentalityIndex()));
        lblPilotingRisk = new JLabel(Integer.toString(behavior.getFallShameIndex()));
        lblForcedWithdrawal = new JLabel(getForcedWithdrawalDescription(behavior));
        lblAutoFlee = new JLabel(getAutoFleeDescription(behavior));


        panBehavior.add(new JLabel(resourceMap.getString("lblCowardice.text")), gbcLeft);
        panBehavior.add(lblCowardice, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblSelfPreservation.text")), gbcLeft);
        panBehavior.add(lblSelfPreservation, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblAggression.text")), gbcLeft);
        panBehavior.add(lblAggression, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblHerdMentality.text")), gbcLeft);
        panBehavior.add(lblHerdMentality, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblPilotingRisk.text")), gbcLeft);
        panBehavior.add(lblPilotingRisk, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblForcedWithdrawal.text")), gbcLeft);
        panBehavior.add(lblForcedWithdrawal, gbcRight);
        gbcLeft.gridy++;
        gbcRight.gridy++;
        panBehavior.add(new JLabel(resourceMap.getString("lblAutoFlee.text")), gbcLeft);
        panBehavior.add(lblAutoFlee, gbcRight);

        JButton btnBehavior = new JButton(resourceMap.getString("btnBehavior.text"));
        btnBehavior.addActionListener(this::editBehavior);
        gbcLeft.gridy++;
        gbcLeft.gridwidth = 2;
        panBehavior.add(btnBehavior, gbcLeft);
    }

    private void refreshFixedEntityPanel() {

        panFixedEntity.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(2, 5, 0, 0);
        for(String en : Utilities.generateEntityStub(botForce.getFixedEntityList())) {
            panFixedEntity.add(new JLabel(en), gbc);
            gbc.gridy++;
        }
        panFixedEntity.revalidate();
        panFixedEntity.repaint();
    }

    public BotForce getBotForce() {
        return botForce;
    }

    private void editBehavior(ActionEvent evt) {
        BotConfigDialog bcd = new BotConfigDialog(frame, botForce.getName(), botForce.getBehaviorSettings(), null);
        bcd.setVisible(true);
        if(!bcd.getResult().isCancelled()) {
            botForce.setBehaviorSettings(bcd.getBehaviorSettings());
            lblCowardice.setText(Integer.toString(botForce.getBehaviorSettings().getBraveryIndex()));
            lblSelfPreservation.setText(Integer.toString(botForce.getBehaviorSettings().getSelfPreservationIndex()));
            lblAggression.setText(Integer.toString(botForce.getBehaviorSettings().getHyperAggressionIndex()));
            lblHerdMentality.setText(Integer.toString(botForce.getBehaviorSettings().getHerdMentalityIndex()));
            lblPilotingRisk.setText(Integer.toString(botForce.getBehaviorSettings().getFallShameIndex()));
            lblForcedWithdrawal.setText(getForcedWithdrawalDescription(botForce.getBehaviorSettings()));
            lblAutoFlee.setText(getAutoFleeDescription(botForce.getBehaviorSettings()));
        }
    }

    private void editCamo(ActionEvent evt) {
        CamoChooserDialog ccd = new CamoChooserDialog(frame, botForce.getCamouflage());
        if (ccd.showDialog().isConfirmed()) {
            botForce.setCamouflage(ccd.getSelectedItem());
            btnCamo.setIcon(botForce.getCamouflage().getImageIcon());
        }
    }

    private void loadUnits(ActionEvent evt) {
        Optional<File> units = FileDialogs.openUnits(frame);
        if (units.isPresent() && units.get() != null) {
            final MULParser parser;
            try {
                parser = new MULParser(units.get(), campaign.getGameOptions());
            } catch (Exception ex) {
                LogManager.getLogger().error("Could not parse BotForce entities", ex);
                return;
            }
            botForce.setFixedEntityList(Collections.list(parser.getEntities().elements()));
            refreshFixedEntityPanel();
        }
    }

    private void saveUnits(ActionEvent evt) {
        Optional<File> saveUnits = FileDialogs.saveUnits(frame,
                (botForce.getName().length() > 0) ? botForce.getName() : "BotForce");

        if(saveUnits.isPresent() && saveUnits.get() != null) {
            try {
                EntityListFile.saveTo(saveUnits.get(), (ArrayList<Entity>) botForce.getFixedEntityListDirect());
            } catch (Exception ex) {
                LogManager.getLogger().error("Could not save BotForce to file", ex);
            }
        }
    }

    private void deleteUnits(ActionEvent evt) {
        botForce.setFixedEntityList(new ArrayList<>());
        refreshFixedEntityPanel();
    }

    private String getForcedWithdrawalDescription(BehaviorSettings behavior) {
        if(!behavior.isForcedWithdrawal()) {
            return "NONE";
        } else {
            return behavior.getRetreatEdge().toString();
        }
    }
    private String getAutoFleeDescription(BehaviorSettings behavior) {
        if(!behavior.shouldAutoFlee()) {
            return "NO";
        } else {
            return behavior.getDestinationEdge().toString();
        }
    }

    private void done(ActionEvent evt) {
        botForce.setName(txtName.getText());
        botForce.setTeam(choiceTeam.getSelectedIndex()+1);
        this.setVisible(false);
    }
    private void cancel(ActionEvent evt) {
        botForce = null;
        this.setVisible(false);
    }

}
