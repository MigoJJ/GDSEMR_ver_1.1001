package com.emr.gds.main.thyroid;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data model for a single thyroid-related EMR snapshot or visit.
 * Designed to work with ThyroidPane.
 */
public class ThyroidEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum VisitType {
        NEW,
        FOLLOW_UP,
        POST_OP,
        POST_RAI
    }

    public enum MainCategory {
        HYPOTHYROIDISM,
        HYPERTHYROIDISM,
        NODULE,
        CANCER,
        THYROIDITIS,
        GOITER,
        OTHER
    }

    public enum Symptom {
        HYPER_NERVOUSNESS    ("\t[ ✔️ ] Nervousness, anxiety, and irritability\n"),
        HYPER_PALPITATIONS   ("\t[ ✔️ ] Rapid or irregular heartbeat (palpitations)\n"),
        HYPER_WEIGHT_LOSS    ("\t[ ✔️ ] Weight loss despite normal/increased appetite\n"),
        HYPER_HEAT_INTOLERANCE("\t[ ✔️ ] Sensitivity to heat and increased sweating\n"),
        HYPER_TREMORS        ("\t[ ✔️ ] Tremors (shaking hands) and muscle weakness\n"),
        HYPER_INSOMNIA       ("\t[ ✔️ ] Difficulty sleeping (insomnia)\n"),
        HYPER_BOWEL          ("\t[ ✔️ ] Frequent bowel movements\n"),
        HYPER_MENSES_LIGHT   ("\t[ ✔️ ] Changes in menstrual cycles (lighter/less frequent)\n"),

        HYPO_FATIGUE         ("\t[ ✔️ ] Fatigue and lethargy\n"),
        HYPO_COLD            ("\t[ ✔️ ] Sensitivity to cold\n"),
        HYPO_WEIGHT_GAIN     ("\t[ ✔️ ] Unexplained weight gain\n"),
        HYPO_CONSTIPATION    ("\t[ ✔️ ] Constipation\n"),
        HYPO_DRY_SKIN_HAIR   ("\t[ ✔️ ] Dry skin and thinning/brittle hair\n"),
        HYPO_PUFFY_FACE      ("\t[ ✔️ ] Puffy face and hoarseness\n"),
        HYPO_SLOWED_HEART    ("\t[ ✔️ ] Slowed heart rate\n"),
        HYPO_DEPRESSION      ("\t[ ✔️ ] Depression and impaired memory\n"),
        HYPO_HEAVY_MENSES    ("\t[ ✔️ ] Heavy or irregular menstrual periods\n"),

        GENERAL_GOITER       ("\t[ ✔️ ] Goiter (enlarged thyroid gland) - can occur in both\n"),
        GENERAL_NODULES      ("\t[ ✔️ ] Thyroid Nodules (lumps in neck)\n"),
        GENERAL_EYE_PROBLEMS ("\t[ ✔️ ] Eye problems (bulging/redness) - specific to Graves' disease\n");


        private final String label;

        Symptom(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * Utility items for symptom dropdowns in the UI.
     */
    public static String[] getSymptomDropdownItems() {
        return Arrays.stream(Symptom.values())
                .map(Symptom::getLabel)
                .toArray(String[]::new);
    }

    public enum HypoEtiology {
        HASHIMOTO,
        IATROGENIC,
        POST_RAI,
        POST_OP,
        SUBCLINICAL,
        OTHER
    }

    public enum HyperEtiology {
        GRAVES,
        TOXIC_MNG,
        TOXIC_ADENOMA,
        THYROIDITIS,
        OTHER
    }

    public enum CancerHistology {
        PTC,
        FTC,
        MTC,
        ATC,
        OTHER
    }

    // Basic info
    private VisitType visitType;
    private List<MainCategory> categories = new ArrayList<>();
    private List<Symptom> symptoms = new ArrayList<>();
    private String symptomNegatives;

    // Hypo / Hyper details
    private HypoEtiology hypoEtiology;
    private Boolean hypoOvert;
    private HyperEtiology hyperEtiology;
    private Boolean hyperActive;

    // Cancer related
    private CancerHistology cancerHistology;
    private String tnmStage;
    private String ataRisk;

    // Detailed ATA Risk Factors
    private Boolean grossExtrathyroidalExtension;
    private Boolean incompleteResection;
    private Boolean distantMetastases;
    private Boolean aggressiveHistology;
    private Boolean vascularInvasion;
    private Integer lymphNodeCount;
    private Double largestNodeSizeCm;

    private Boolean raiDone;
    private Double raiDoseMci;
    private LocalDate raiDate;
    private String cancerStatus;

    // Patient details
    private Double patientWeightKg;

    // Nodule / TI-RADS
    private Integer tiRadsScore;
    private String tiRadsLevel;

    // Labs
    private Double tsh;
    private Double freeT4;
    private Double freeT3;
    private Double tpoAb;
    private Double tg;
    private Double tgAb;
    private Double trab;
    private Double calcitonin;
    private LocalDate lastLabDate;

    // Treatment
    private Double lt4DoseMcgPerDay;
    private String atdName;
    private Double atdDoseMgPerDay;
    private String betaBlockerName;
    private String betaBlockerDose;
    private String otherMeds;

    // Imaging
    private String usSummary;
    private LocalDate usDate;
    private String scanSummary;
    private LocalDate scanDate;

    // Follow-up
    private String followUpInterval;
    private String followUpPlanText;

    // Notes
    private String clinicianNote;
    private String problemListSummary;

    public ThyroidEntry() {
    }

    private ThyroidEntry(Builder builder) {
        this.visitType = builder.visitType;
        this.categories = builder.categories;
        this.symptoms = builder.symptoms;
        this.symptomNegatives = builder.symptomNegatives;
        this.hypoEtiology = builder.hypoEtiology;
        this.hypoOvert = builder.hypoOvert;
        this.hyperEtiology = builder.hyperEtiology;
        this.hyperActive = builder.hyperActive;
        this.cancerHistology = builder.cancerHistology;
        this.tnmStage = builder.tnmStage;
        this.ataRisk = builder.ataRisk;
        this.grossExtrathyroidalExtension = builder.grossExtrathyroidalExtension;
        this.incompleteResection = builder.incompleteResection;
        this.distantMetastases = builder.distantMetastases;
        this.aggressiveHistology = builder.aggressiveHistology;
        this.vascularInvasion = builder.vascularInvasion;
        this.lymphNodeCount = builder.lymphNodeCount;
        this.largestNodeSizeCm = builder.largestNodeSizeCm;
        this.raiDone = builder.raiDone;
        this.raiDoseMci = builder.raiDoseMci;
        this.raiDate = builder.raiDate;
        this.cancerStatus = builder.cancerStatus;
        this.patientWeightKg = builder.patientWeightKg;
        this.tiRadsScore = builder.tiRadsScore;
        this.tiRadsLevel = builder.tiRadsLevel;
        this.tsh = builder.tsh;
        this.freeT4 = builder.freeT4;
        this.freeT3 = builder.freeT3;
        this.tpoAb = builder.tpoAb;
        this.tg = builder.tg;
        this.tgAb = builder.tgAb;
        this.trab = builder.trab;
        this.calcitonin = builder.calcitonin;
        this.lastLabDate = builder.lastLabDate;
        this.lt4DoseMcgPerDay = builder.lt4DoseMcgPerDay;
        this.atdName = builder.atdName;
        this.atdDoseMgPerDay = builder.atdDoseMgPerDay;
        this.betaBlockerName = builder.betaBlockerName;
        this.betaBlockerDose = builder.betaBlockerDose;
        this.otherMeds = builder.otherMeds;
        this.usSummary = builder.usSummary;
        this.usDate = builder.usDate;
        this.scanSummary = builder.scanSummary;
        this.scanDate = builder.scanDate;
        this.followUpInterval = builder.followUpInterval;
        this.followUpPlanText = builder.followUpPlanText;
        this.clinicianNote = builder.clinicianNote;
        this.problemListSummary = builder.problemListSummary;
    }

    // Getters and Setters

    public VisitType getVisitType() { return visitType; }
    public void setVisitType(VisitType visitType) { this.visitType = visitType; }

    public List<MainCategory> getCategories() { return categories; }
    public void setCategories(List<MainCategory> categories) { this.categories = (categories != null) ? categories : new ArrayList<>(); }

    public List<Symptom> getSymptoms() { return symptoms; }
    public void setSymptoms(List<Symptom> symptoms) { this.symptoms = (symptoms != null) ? symptoms : new ArrayList<>(); }
    public String getSymptomNegatives() { return symptomNegatives; }
    public void setSymptomNegatives(String symptomNegatives) { this.symptomNegatives = symptomNegatives; }

    public HypoEtiology getHypoEtiology() { return hypoEtiology; }
    public void setHypoEtiology(HypoEtiology hypoEtiology) { this.hypoEtiology = hypoEtiology; }

    public Boolean isHypoOvert() { return hypoOvert; }
    public void setHypoOvert(Boolean hypoOvert) { this.hypoOvert = hypoOvert; }

    public HyperEtiology getHyperEtiology() { return hyperEtiology; }
    public void setHyperEtiology(HyperEtiology hyperEtiology) { this.hyperEtiology = hyperEtiology; }

    public Boolean isHyperActive() { return hyperActive; }
    public void setHyperActive(Boolean hyperActive) { this.hyperActive = hyperActive; }

    public CancerHistology getCancerHistology() { return cancerHistology; }
    public void setCancerHistology(CancerHistology cancerHistology) { this.cancerHistology = cancerHistology; }

    public String getTnmStage() { return tnmStage; }
    public void setTnmStage(String tnmStage) { this.tnmStage = tnmStage; }

    public String getAtaRisk() { return ataRisk; }
    public void setAtaRisk(String ataRisk) { this.ataRisk = ataRisk; }

    public Boolean getGrossExtrathyroidalExtension() { return grossExtrathyroidalExtension; }
    public void setGrossExtrathyroidalExtension(Boolean grossExtrathyroidalExtension) { this.grossExtrathyroidalExtension = grossExtrathyroidalExtension; }

    public Boolean getIncompleteResection() { return incompleteResection; }
    public void setIncompleteResection(Boolean incompleteResection) { this.incompleteResection = incompleteResection; }

    public Boolean getDistantMetastases() { return distantMetastases; }
    public void setDistantMetastases(Boolean distantMetastases) { this.distantMetastases = distantMetastases; }

    public Boolean getAggressiveHistology() { return aggressiveHistology; }
    public void setAggressiveHistology(Boolean aggressiveHistology) { this.aggressiveHistology = aggressiveHistology; }

    public Boolean getVascularInvasion() { return vascularInvasion; }
    public void setVascularInvasion(Boolean vascularInvasion) { this.vascularInvasion = vascularInvasion; }

    public Integer getLymphNodeCount() { return lymphNodeCount; }
    public void setLymphNodeCount(Integer lymphNodeCount) { this.lymphNodeCount = lymphNodeCount; }

    public Double getLargestNodeSizeCm() { return largestNodeSizeCm; }
    public void setLargestNodeSizeCm(Double largestNodeSizeCm) { this.largestNodeSizeCm = largestNodeSizeCm; }

    public Boolean getRaiDone() { return raiDone; }
    public void setRaiDone(Boolean raiDone) { this.raiDone = raiDone; }

    public Double getRaiDoseMci() { return raiDoseMci; }
    public void setRaiDoseMci(Double raiDoseMci) { this.raiDoseMci = raiDoseMci; }

    public LocalDate getRaiDate() { return raiDate; }
    public void setRaiDate(LocalDate raiDate) { this.raiDate = raiDate; }

    public String getCancerStatus() { return cancerStatus; }
    public void setCancerStatus(String cancerStatus) { this.cancerStatus = cancerStatus; }

    public Double getPatientWeightKg() { return patientWeightKg; }
    public void setPatientWeightKg(Double patientWeightKg) { this.patientWeightKg = patientWeightKg; }

    public Integer getTiRadsScore() { return tiRadsScore; }
    public void setTiRadsScore(Integer tiRadsScore) { this.tiRadsScore = tiRadsScore; }

    public String getTiRadsLevel() { return tiRadsLevel; }
    public void setTiRadsLevel(String tiRadsLevel) { this.tiRadsLevel = tiRadsLevel; }

    public Double getTsh() { return tsh; }
    public void setTsh(Double tsh) { this.tsh = tsh; }

    public Double getFreeT4() { return freeT4; }
    public void setFreeT4(Double freeT4) { this.freeT4 = freeT4; }

    public Double getFreeT3() { return freeT3; }
    public void setFreeT3(Double freeT3) { this.freeT3 = freeT3; }

    public Double getTpoAb() { return tpoAb; }
    public void setTpoAb(Double tpoAb) { this.tpoAb = tpoAb; }

    public Double getTg() { return tg; }
    public void setTg(Double tg) { this.tg = tg; }

    public Double getTgAb() { return tgAb; }
    public void setTgAb(Double tgAb) { this.tgAb = tgAb; }

    public Double getTrab() { return trab; }
    public void setTrab(Double trab) { this.trab = trab; }

    public Double getCalcitonin() { return calcitonin; }
    public void setCalcitonin(Double calcitonin) { this.calcitonin = calcitonin; }

    public LocalDate getLastLabDate() { return lastLabDate; }
    public void setLastLabDate(LocalDate lastLabDate) { this.lastLabDate = lastLabDate; }

    public Double getLt4DoseMcgPerDay() { return lt4DoseMcgPerDay; }
    public void setLt4DoseMcgPerDay(Double lt4DoseMcgPerDay) { this.lt4DoseMcgPerDay = lt4DoseMcgPerDay; }

    public String getAtdName() { return atdName; }
    public void setAtdName(String atdName) { this.atdName = atdName; }

    public Double getAtdDoseMgPerDay() { return atdDoseMgPerDay; }
    public void setAtdDoseMgPerDay(Double atdDoseMgPerDay) { this.atdDoseMgPerDay = atdDoseMgPerDay; }

    public String getBetaBlockerName() { return betaBlockerName; }
    public void setBetaBlockerName(String betaBlockerName) { this.betaBlockerName = betaBlockerName; }

    public String getBetaBlockerDose() { return betaBlockerDose; }
    public void setBetaBlockerDose(String betaBlockerDose) { this.betaBlockerDose = betaBlockerDose; }

    public String getOtherMeds() { return otherMeds; }
    public void setOtherMeds(String otherMeds) { this.otherMeds = otherMeds; }

    public String getUsSummary() { return usSummary; }
    public void setUsSummary(String usSummary) { this.usSummary = usSummary; }

    public LocalDate getUsDate() { return usDate; }
    public void setUsDate(LocalDate usDate) { this.usDate = usDate; }

    public String getScanSummary() { return scanSummary; }
    public void setScanSummary(String scanSummary) { this.scanSummary = scanSummary; }

    public LocalDate getScanDate() { return scanDate; }
    public void setScanDate(LocalDate scanDate) { this.scanDate = scanDate; }

    public String getFollowUpInterval() { return followUpInterval; }
    public void setFollowUpInterval(String followUpInterval) { this.followUpInterval = followUpInterval; }

    public String getFollowUpPlanText() { return followUpPlanText; }
    public void setFollowUpPlanText(String followUpPlanText) { this.followUpPlanText = followUpPlanText; }

    public String getClinicianNote() { return clinicianNote; }
    public void setClinicianNote(String clinicianNote) { this.clinicianNote = clinicianNote; }

    public String getProblemListSummary() { return problemListSummary; }
    public void setProblemListSummary(String problemListSummary) { this.problemListSummary = problemListSummary; }

    /**
     * Very small helper to build a compact problem-list summary.
     */
    public String buildProblemListSummary() {
        StringBuilder sb = new StringBuilder();

        if (categories != null && categories.contains(MainCategory.HYPOTHYROIDISM)) {
            sb.append("Hypothyroidism");
            if (hypoEtiology != null) {
                sb.append(" (").append(hypoEtiology.name()).append(")");
            }
            if (Boolean.TRUE.equals(hypoOvert)) {
                sb.append(", overt");
            } else if (Boolean.FALSE.equals(hypoOvert)) {
                sb.append(", subclinical");
            }
            if (lt4DoseMcgPerDay != null) {
                sb.append(" on LT4 ").append(lt4DoseMcgPerDay).append(" mcg/day");
            }
            sb.append(". ");
        }

        if (categories != null && categories.contains(MainCategory.HYPERTHYROIDISM)) {
            sb.append("Hyperthyroidism");
            if (hyperEtiology != null) {
                sb.append(" (").append(hyperEtiology.name()).append(")");
            }
            if (Boolean.TRUE.equals(hyperActive)) {
                sb.append(" - active.");
            } else if (Boolean.FALSE.equals(hyperActive)) {
                sb.append(" - in remission.");
            }
            sb.append(" ");
        }

        if (categories != null && categories.contains(MainCategory.CANCER)) {
            sb.append("Thyroid cancer");
            if (cancerHistology != null) {
                sb.append(" (").append(cancerHistology.name()).append(")");
            }
            if (tnmStage != null && !tnmStage.isBlank()) {
                sb.append(", TNM ").append(tnmStage);
            }
            if (ataRisk != null && !ataRisk.isBlank()) {
                sb.append(", ATA risk ").append(ataRisk);
            }
            if (cancerStatus != null && !cancerStatus.isBlank()) {
                sb.append(", status ").append(cancerStatus);
            }
            sb.append(". ");
        }

        this.problemListSummary = sb.toString().trim();
        return this.problemListSummary;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private VisitType visitType;
        private List<MainCategory> categories = new ArrayList<>();
        private List<Symptom> symptoms = new ArrayList<>();
        private String symptomNegatives;
        private HypoEtiology hypoEtiology;
        private Boolean hypoOvert;
        private HyperEtiology hyperEtiology;
        private Boolean hyperActive;
        private CancerHistology cancerHistology;
        private String tnmStage;
        private String ataRisk;
        private Boolean grossExtrathyroidalExtension;
        private Boolean incompleteResection;
        private Boolean distantMetastases;
        private Boolean aggressiveHistology;
        private Boolean vascularInvasion;
        private Integer lymphNodeCount;
        private Double largestNodeSizeCm;
        private Boolean raiDone;
        private Double raiDoseMci;
        private LocalDate raiDate;
        private String cancerStatus;
        private Double patientWeightKg;
        private Integer tiRadsScore;
        private String tiRadsLevel;
        private Double tsh;
        private Double freeT4;
        private Double freeT3;
        private Double tpoAb;
        private Double tg;
        private Double tgAb;
        private Double trab;
        private Double calcitonin;
        private LocalDate lastLabDate;
        private Double lt4DoseMcgPerDay;
        private String atdName;
        private Double atdDoseMgPerDay;
        private String betaBlockerName;
        private String betaBlockerDose;
        private String otherMeds;
        private String usSummary;
        private LocalDate usDate;
        private String scanSummary;
        private LocalDate scanDate;
        private String followUpInterval;
        private String followUpPlanText;
        private String clinicianNote;
        private String problemListSummary;

        public Builder visitType(VisitType visitType) {
            this.visitType = visitType;
            return this;
        }

        public Builder addCategory(MainCategory category) {
            if (category != null) {
                this.categories.add(category);
            }
            return this;
        }

        public Builder categories(List<MainCategory> categories) {
            this.categories = (categories != null) ? categories : new ArrayList<>();
            return this;
        }

        public Builder addSymptom(Symptom symptom) {
            if (symptom != null) {
                this.symptoms.add(symptom);
            }
            return this;
        }

        public Builder symptoms(List<Symptom> symptoms) {
            this.symptoms = (symptoms != null) ? symptoms : new ArrayList<>();
            return this;
        }

        public Builder symptomNegatives(String symptomNegatives) {
            this.symptomNegatives = symptomNegatives;
            return this;
        }

        public Builder hypoEtiology(HypoEtiology hypoEtiology) {
            this.hypoEtiology = hypoEtiology;
            return this;
        }

        public Builder hypoOvert(Boolean hypoOvert) {
            this.hypoOvert = hypoOvert;
            return this;
        }

        public Builder hyperEtiology(HyperEtiology hyperEtiology) {
            this.hyperEtiology = hyperEtiology;
            return this;
        }

        public Builder hyperActive(Boolean hyperActive) {
            this.hyperActive = hyperActive;
            return this;
        }

        public Builder cancerHistology(CancerHistology cancerHistology) {
            this.cancerHistology = cancerHistology;
            return this;
        }

        public Builder tnmStage(String tnmStage) {
            this.tnmStage = tnmStage;
            return this;
        }

        public Builder ataRisk(String ataRisk) {
            this.ataRisk = ataRisk;
            return this;
        }

        public Builder grossExtrathyroidalExtension(Boolean grossExtrathyroidalExtension) {
            this.grossExtrathyroidalExtension = grossExtrathyroidalExtension;
            return this;
        }

        public Builder incompleteResection(Boolean incompleteResection) {
            this.incompleteResection = incompleteResection;
            return this;
        }

        public Builder distantMetastases(Boolean distantMetastases) {
            this.distantMetastases = distantMetastases;
            return this;
        }

        public Builder aggressiveHistology(Boolean aggressiveHistology) {
            this.aggressiveHistology = aggressiveHistology;
            return this;
        }

        public Builder vascularInvasion(Boolean vascularInvasion) {
            this.vascularInvasion = vascularInvasion;
            return this;
        }

        public Builder lymphNodeCount(Integer lymphNodeCount) {
            this.lymphNodeCount = lymphNodeCount;
            return this;
        }

        public Builder largestNodeSizeCm(Double largestNodeSizeCm) {
            this.largestNodeSizeCm = largestNodeSizeCm;
            return this;
        }

        public Builder raiDone(Boolean raiDone) {
            this.raiDone = raiDone;
            return this;
        }

        public Builder raiDoseMci(Double raiDoseMci) {
            this.raiDoseMci = raiDoseMci;
            return this;
        }

        public Builder raiDate(LocalDate raiDate) {
            this.raiDate = raiDate;
            return this;
        }

        public Builder cancerStatus(String cancerStatus) {
            this.cancerStatus = cancerStatus;
            return this;
        }

        public Builder patientWeightKg(Double patientWeightKg) {
            this.patientWeightKg = patientWeightKg;
            return this;
        }

        public Builder tiRadsScore(Integer tiRadsScore) {
            this.tiRadsScore = tiRadsScore;
            return this;
        }

        public Builder tiRadsLevel(String tiRadsLevel) {
            this.tiRadsLevel = tiRadsLevel;
            return this;
        }

        public Builder tsh(Double tsh) {
            this.tsh = tsh;
            return this;
        }

        public Builder freeT4(Double freeT4) {
            this.freeT4 = freeT4;
            return this;
        }

        public Builder freeT3(Double freeT3) {
            this.freeT3 = freeT3;
            return this;
        }

        public Builder tpoAb(Double tpoAb) {
            this.tpoAb = tpoAb;
            return this;
        }

        public Builder tg(Double tg) {
            this.tg = tg;
            return this;
        }

        public Builder tgAb(Double tgAb) {
            this.tgAb = tgAb;
            return this;
        }

        public Builder trab(Double trab) {
            this.trab = trab;
            return this;
        }

        public Builder calcitonin(Double calcitonin) {
            this.calcitonin = calcitonin;
            return this;
        }

        public Builder lastLabDate(LocalDate lastLabDate) {
            this.lastLabDate = lastLabDate;
            return this;
        }

        public Builder lt4DoseMcgPerDay(Double lt4DoseMcgPerDay) {
            this.lt4DoseMcgPerDay = lt4DoseMcgPerDay;
            return this;
        }

        public Builder atdName(String atdName) {
            this.atdName = atdName;
            return this;
        }

        public Builder atdDoseMgPerDay(Double atdDoseMgPerDay) {
            this.atdDoseMgPerDay = atdDoseMgPerDay;
            return this;
        }

        public Builder betaBlockerName(String betaBlockerName) {
            this.betaBlockerName = betaBlockerName;
            return this;
        }

        public Builder betaBlockerDose(String betaBlockerDose) {
            this.betaBlockerDose = betaBlockerDose;
            return this;
        }

        public Builder otherMeds(String otherMeds) {
            this.otherMeds = otherMeds;
            return this;
        }

        public Builder usSummary(String usSummary) {
            this.usSummary = usSummary;
            return this;
        }

        public Builder usDate(LocalDate usDate) {
            this.usDate = usDate;
            return this;
        }

        public Builder scanSummary(String scanSummary) {
            this.scanSummary = scanSummary;
            return this;
        }

        public Builder scanDate(LocalDate scanDate) {
            this.scanDate = scanDate;
            return this;
        }

        public Builder followUpInterval(String followUpInterval) {
            this.followUpInterval = followUpInterval;
            return this;
        }

        public Builder followUpPlanText(String followUpPlanText) {
            this.followUpPlanText = followUpPlanText;
            return this;
        }

        public Builder clinicianNote(String clinicianNote) {
            this.clinicianNote = clinicianNote;
            return this;
        }

        public Builder problemListSummary(String problemListSummary) {
            this.problemListSummary = problemListSummary;
            return this;
        }

        public ThyroidEntry build() {
            return new ThyroidEntry(this);
        }
    }
}
