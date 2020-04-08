/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, version 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as defined by the
 * Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.spell;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.prereq.PrereqsPanel;
import com.trollworks.gcs.skill.SkillDifficulty;
import com.trollworks.gcs.skill.SkillLevel;
import com.trollworks.gcs.weapon.MeleeWeaponEditor;
import com.trollworks.gcs.weapon.RangedWeaponEditor;
import com.trollworks.gcs.weapon.WeaponStats;
import com.trollworks.gcs.widgets.outline.ListRow;
import com.trollworks.gcs.widgets.outline.RowEditor;
import com.trollworks.toolkit.ui.UIUtilities;
import com.trollworks.toolkit.ui.layout.ColumnLayout;
import com.trollworks.toolkit.utility.I18n;
import com.trollworks.toolkit.utility.text.NumberFilter;
import com.trollworks.toolkit.utility.text.Numbers;
import com.trollworks.toolkit.utility.text.Text;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** The detailed editor for {@link RitualMagicSpell}s. */
public class RitualMagicSpellEditor extends BaseSpellEditor<RitualMagicSpell> {
    private JTextField mPrerequisiteSpellsCountField;


    /**
     * Creates a new {@link Spell} {@link RowEditor}.
     *
     * @param spell The row being edited.
     */
    protected RitualMagicSpellEditor(RitualMagicSpell spell) {
        super(spell);

        Container content      = new JPanel(new ColumnLayout(2));
        Container fields       = new JPanel(new ColumnLayout());
        Container wrapper1     = new JPanel(new ColumnLayout(3));
        Container wrapper2     = new JPanel(new ColumnLayout(4));
        Container wrapper3     = new JPanel(new ColumnLayout(2));
        Container noGapWrapper = new JPanel(new ColumnLayout(2, 0, 0));
        JLabel    icon         = new JLabel(spell.getIcon(true));
        Dimension size         = new Dimension();
        Container ptsPanel;

        mNameField = createCorrectableField(wrapper1, wrapper1, I18n.Text("Name"), spell.getName(), I18n.Text("The name of the spell, without any notes"));
        fields.add(wrapper1);

        createTechLevelFields(wrapper1);
        mCollegeField = createField(wrapper2, wrapper2, I18n.Text("College"), spell.getCollege(), I18n.Text("The college the spell belongs to"), 0);
        mPowerSourceField = createField(wrapper2, wrapper2, I18n.Text("Power Source"), spell.getPowerSource(), I18n.Text("The source of power for the spell"), 0);
        mClassField = createCorrectableField(wrapper2, wrapper2, I18n.Text("Class"), spell.getSpellClass(), I18n.Text("The class of spell (Area, Missile, etc.)"));
        mCastingCostField = createCorrectableField(wrapper2, wrapper2, I18n.Text("Casting Cost"), spell.getCastingCost(), I18n.Text("The casting cost of the spell"));
        mMaintenanceField = createField(wrapper2, wrapper2, I18n.Text("Maintenance Cost"), spell.getMaintenance(), I18n.Text("The cost to maintain a spell after its initial duration"), 0);
        mCastingTimeField = createCorrectableField(wrapper2, wrapper2, I18n.Text("Casting Time"), spell.getCastingTime(), I18n.Text("The casting time of the spell"));
        mDurationField = createCorrectableField(wrapper2, wrapper2, I18n.Text("Duration"), spell.getDuration(), I18n.Text("The duration of the spell once its cast"));
        mPrerequisiteSpellsCountField = createNumberField(wrapper2, wrapper2, I18n.Text("Prerequisite Count"), I18n.Text("The penalty to skill level based on the number of prerequisite spells"), mRow.getPrerequisiteSpellsCount(), 2);
        fields.add(wrapper2);

        ptsPanel = createPointsFields();
        fields.add(ptsPanel);

        mNotesField = createField(wrapper3, wrapper3, I18n.Text("Notes"), spell.getNotes(), I18n.Text("Any notes that you would like to show up in the list along with this spell"), 0);
        mCategoriesField = createField(wrapper3, wrapper3, I18n.Text("Categories"), spell.getCategoriesAsString(), I18n.Text("The category or categories the spell belongs to (separate multiple categories with a comma)"), 0);
        mReferenceField = createField(ptsPanel, noGapWrapper, I18n.Text("Page Reference"), mRow.getReference(), I18n.Text("A reference to the book and page this spell appears on (e.g. B22 would refer to \"Basic Set\", page 22)"), 6);
        noGapWrapper.add(new JPanel());
        ptsPanel.add(noGapWrapper);
        fields.add(wrapper3);

        determineLargest(wrapper1, 3, size);
        determineLargest(wrapper2, 4, size);
        determineLargest(ptsPanel, 100, size);
        determineLargest(wrapper3, 2, size);
        applySize(wrapper1, 3, size);
        applySize(wrapper2, 4, size);
        applySize(ptsPanel, 100, size);
        applySize(wrapper3, 2, size);

        icon.setVerticalAlignment(SwingConstants.TOP);
        icon.setAlignmentY(-1.0f);
        content.add(icon);
        content.add(fields);
        add(content);

        mTabPanel = new JTabbedPane();
        mPrereqs = new PrereqsPanel(mRow, mRow.getPrereqs());
        mMeleeWeapons = MeleeWeaponEditor.createEditor(mRow);
        mRangedWeapons = RangedWeaponEditor.createEditor(mRow);
        Component panel = embedEditor(mPrereqs);
        mTabPanel.addTab(panel.getName(), panel);
        mTabPanel.addTab(mMeleeWeapons.getName(), mMeleeWeapons);
        mTabPanel.addTab(mRangedWeapons.getName(), mRangedWeapons);
        if (!mIsEditable) {
            UIUtilities.disableControls(mMeleeWeapons);
            UIUtilities.disableControls(mRangedWeapons);
        }
        UIUtilities.selectTab(mTabPanel, getLastTabName());
        add(mTabPanel);
    }

    protected Container createPointsFields() {
        boolean forCharacter = mRow.getCharacter() != null;
        boolean forTemplate  = mRow.getTemplate() != null;
        int     columns      = forTemplate ? 8 : 6;
        JPanel  panel        = new JPanel(new ColumnLayout(forCharacter ? 10 : columns));

        JLabel label = new JLabel(I18n.Text("Difficulty"), SwingConstants.RIGHT);
        label.setToolTipText(Text.wrapPlainTextForToolTip(I18n.Text("The difficulty of the spell")));
        panel.add(label);

        SkillDifficulty[] allowedDifficulties = {SkillDifficulty.A, SkillDifficulty.H};
        mDifficultyCombo = createComboBox(panel, allowedDifficulties, mRow.getDifficulty(), I18n.Text("The difficulty of the spell"));

        if (forCharacter || forTemplate) {
            mPointsField = createField(panel, panel, I18n.Text("Points"), Integer.toString(mRow.getPoints()), I18n.Text("The number of points spent on this spell"), 4);
            new NumberFilter(mPointsField, false, false, false, 4);
            mPointsField.addActionListener(this);

            if (forCharacter) {
                String levelText    = makeLevelFieldText(mRow.getLevel(), mRow.getRelativeLevel());
                String levelTooltip = I18n.Text("The spell level and relative spell level to roll against.\n") + mRow.getLevelToolTip();
                mLevelField = createField(panel, panel, I18n.Text("Level"), levelText, levelTooltip, 7);
                mLevelField.setEnabled(false);
            }
        }
        return panel;
    }

    public static String makeLevelFieldText(int level, int relativeLevel) {
        if (level < 0) {
            return "-";
        }
        return Numbers.format(level) + "/" + Numbers.formatWithForcedSign(relativeLevel);
    }

    protected int getPrerequisiteSpellsCount() {
        return Numbers.extractInteger(mPrerequisiteSpellsCountField.getText(), 0, true);
    }

    @Override
    protected boolean applyChangesSelf() {
        boolean modified = mRow.setName(mNameField.getText());

        modified |= mRow.setReference(mReferenceField.getText());
        if (mHasTechLevel != null) {
            modified |= mRow.setTechLevel(mHasTechLevel.isSelected() ? mTechLevel.getText() : null);
        }
        modified |= mRow.setCollege(mCollegeField.getText());
        modified |= mRow.setPowerSource(mPowerSourceField.getText());
        modified |= mRow.setSpellClass(mClassField.getText());
        modified |= mRow.setCastingCost(mCastingCostField.getText());
        modified |= mRow.setMaintenance(mMaintenanceField.getText());
        modified |= mRow.setCastingTime(mCastingTimeField.getText());
        modified |= mRow.setDuration(mDurationField.getText());
        modified |= mRow.setPrerequisiteSpellsCount(getPrerequisiteSpellsCount());
        if (mRow.getCharacter() != null || mRow.getTemplate() != null) {
            modified |= mRow.setPoints(Numbers.extractInteger(mPointsField.getText(), 0, true));
        }
        modified |= mRow.setNotes(mNotesField.getText());
        modified |= mRow.setCategories(mCategoriesField.getText());
        modified |= mRow.setPrereqs(mPrereqs.getPrereqList());

        List<WeaponStats> list = new ArrayList<>(mMeleeWeapons.getWeapons());
        list.addAll(mRangedWeapons.getWeapons());
        modified |= mRow.setWeapons(list);

        return modified;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);
        Object src = event.getSource();
        if (src == mPrerequisiteSpellsCountField) {
            if (mLevelField != null) {
                recalculateLevel(mLevelField);
            }
        }
    }

    protected void recalculateLevel(JTextField levelField) {
        GURPSCharacter  character         = mRow.getCharacter();
        String          spellName         = mNameField.getText();
        String          collegeName       = mCollegeField.getText();
        String          powerSource       = mPowerSourceField.getText();
        Set<String>     categories        = ListRow.createCategoriesList(mCategoriesField.getText());
        SkillDifficulty difficulty        = getDifficulty();
        int             prereqSpellsCount = getPrerequisiteSpellsCount();
        int             points            = getPoints();
        SkillLevel      skillLevel        = RitualMagicSpell.calculateLevel(character, spellName, collegeName, powerSource, categories, difficulty, prereqSpellsCount, points);

        // FIXME: the skill level does not account for the "penalty from default" assigned to the SkillDefault
        String levelFieldText    = makeLevelFieldText(skillLevel.getLevel(), skillLevel.getRelativeLevel());
        String levelFieldTooltip = I18n.Text("The spell level and relative spell level to roll against.\n") + skillLevel.getToolTip();
        levelField.setText(levelFieldText);
        levelField.setToolTipText(Text.wrapPlainTextForToolTip(levelFieldTooltip));
    }
}
