import numpy as np
class LookupManager:
    def __init__(self):
        self.stageTnm_lookup = {
            "ZERO": 0.0, "I": 1.5, "IA1": 1.3125, "IA": 1.375, "IA2": 1.375, "IA3": 1.4375, "IB": 1.5,
            "II": 2.5, "IIA": 2.25, "IIB": 2.5, "IIC": 2.75,
            "III": 3.5, "IIIA": 3.25, "IIIB": 3.5, "IIIC": 3.75,
            "IV": 4.5, "IVA": 4.25, "IVB": 4.5, "IVC": 4.75,
            "M": 4.5, "NA": np.nan, "X": np.nan,
        }

        self.tnmM_lookup = {
            "M0": 0,
            "M1": 1, "M1A": 1.25, "M1B": 1.5, "M1C": 1.75,
            "M_MINUS": 0,
            "X": np.nan,
        }

        self.tnmN_lookup = {
            "N0": 0,
            "N1": 1.5, "N1A": 1.25,"N1B": 1.5, "N1C": 1.75, "N1M": 1.5,
            "N2": 2.5, "N2A": 2.25, "N2B": 2.5,
            "X": np.nan
        }

        self.tnmT_lookup = {
            "T0": 0,
            "T_IS": 0.5,
            "T1": 1,
            "T2": 2,
            "T3": 3,
            "T4A": 4.25,
            "T4B": 4.5,
            "X": np.nan
        }

        self.lookup_dictionary = {
            "anorectalVergeDistanceCategory": {
                "ZERO_TO_FIVE_CM": 2.5,
                "FIVE_TO_TEN_CM": 7.5,
                "TEN_TO_FIFTEEN_CM": 12.5,
                "OVER_FIFTEEN_CM": 17.5,
            },
            "numberOfLiverMetastases": {
                "ONE": 1,
                "TWO": 2,
                "THREE": 3,
                "FOUR": 4,
                "FIVE_OR_MORE": 5,
                "MULTIPLE_BUT_EXACT_NUMBER_UNKNOWN": 5  # The median number of multiple metastases is 5+
            },
            "asaAssessmentAtMetastaticDiagnosis": {
                "I": 1,
                "II": 2,
                "III": 3,
                "IV": 4,
                "V": 5,
                "VI": 6,
            },
            "tumorRegression": {
                "CANNOT_BE_DETERMINED": np.nan,
                "FULL_REGRESSION": 1,
                "MINIMAL_FOCI": 0.9,  # https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4946373/
                "MINIMAL_REGRESSION": 0.2,
                "MODERATE_REGRESSION": 0.5,
                "NO_SIGNS_OF_REGRESSION": 0,
                "NA": np.nan,
            },
            "differentiationGrade": {
                "GRADE_1_OR_WELL_DIFFERENTIATED": 1,
                "GRADE_2_OR_MODERATELY_DIFFERENTIATED": 2,
                "GRADE_3_OR_POORLY_DIFFERENTIATED": 3,
                "GRADE_4_OR_UNDIFFERENTIATED_OR_ANAPLASTIC_OR_GGG4": 4
            },
            "pathologicalTnmT": self.tnmT_lookup,
            "pathologicalTnmN": self.tnmN_lookup,
            "pathologicalTnmM": self.tnmM_lookup,
            "clinicalTnmT": self.tnmT_lookup,
            "clinicalTnmN": self.tnmN_lookup,
            "clinicalTnmM": self.tnmM_lookup,
            "clinicalTumorStage": self.stageTnm_lookup,
            "pathologicalTumorStage": self.stageTnm_lookup
        }

        self.features = [
            'firstSystemicTreatmentAfterMetastaticDiagnosis',
            'sex',
            'ageAtMetastaticDiagnosis',
            'numberOfPriorTumors',
            'hasDoublePrimaryTumor',
            'primaryTumorType',
            'primaryTumorLocation',
            'sidedness',
            'anorectalVergeDistanceCategory',
            'mesorectalFasciaIsClear',
            'distanceToMesorectalFasciaMm',
            'differentiationGrade',
            'clinicalTnmT',
            'clinicalTnmN',
            'clinicalTnmM',
            'pathologicalTnmT',
            'pathologicalTnmN',
            'pathologicalTnmM',
            'clinicalTumorStage',
            'pathologicalTumorStage',
            'investigatedLymphNodesCountPrimaryDiagnosis',
            'positiveLymphNodesCountPrimaryDiagnosis',
            'presentedWithIleus',
            'presentedWithPerforation',
            'extraMuralInvasionCategory',
         
            'daysBetweenPrimaryAndMetastaticDiagnosis',
            'hasLiverOrIntrahepaticBileDuctMetastases',
            'numberOfLiverMetastases',
            'maximumSizeOfLiverMetastasisMm',
            'hasLymphNodeMetastases',
            'investigatedLymphNodesCountMetastaticDiagnosis',
            'positiveLymphNodesCountMetastaticDiagnosis',
            'hasPeritonealMetastases',
            'hasBronchusOrLungMetastases',
            'hasBrainMetastases',
            'hasOtherMetastases',        

            'whoAssessmentAtMetastaticDiagnosis',
            'asaAssessmentAtMetastaticDiagnosis',
            'lactateDehydrogenaseAtMetastaticDiagnosis',
            'alkalinePhosphataseAtMetastaticDiagnosis',
            'leukocytesAbsoluteAtMetastaticDiagnosis',
            'carcinoembryonicAntigenAtMetastaticDiagnosis',
            'albumineAtMetastaticDiagnosis',
            'neutrophilsAbsoluteAtMetastaticDiagnosis',

            'hasHadPrimarySurgeryPriorToMetastaticTreatment',
            'hasHadPrimarySurgeryDuringMetastaticTreatment',
            'hasHadGastroenterologySurgeryPriorToMetastaticTreatment',
            'hasHadGastroenterologySurgeryDuringMetastaticTreatment',
            'hasHadHipecPriorToMetastaticTreatment',
            'hasHadHipecDuringMetastaticTreatment',
            'hasHadPrimaryRadiotherapyPriorToMetastaticTreatment',
            'hasHadPrimaryRadiotherapyDuringMetastaticTreatment',

            'hasHadMetastaticSurgery',
            'hasHadMetastaticRadiotherapy',
            
            'charlsonComorbidityIndex',
            'hasAids',
            'hasCongestiveHeartFailure',
            'hasCollagenosis',
            'hasCopd',
            'hasCerebrovascularDisease',
            'hasDementia',
            'hasDiabetesMellitus',
            'hasDiabetesMellitusWithEndOrganDamage',
            'hasOtherMalignancy',
            'hasOtherMetastaticSolidTumor',
            'hasMyocardialInfarct',
            'hasMildLiverDisease',
            'hasHemiplegiaOrParaplegia',
            'hasPeripheralVascularDisease',
            'hasRenalDisease',
            'hasLiverDisease',
            'hasUlcerDisease',

            'hasMsi',
            'hasBrafMutation',
            'hasBrafV600EMutation',
            'hasRasMutation',
            'hasKrasG12CMutation',
        ]
        
lookup_manager = LookupManager()