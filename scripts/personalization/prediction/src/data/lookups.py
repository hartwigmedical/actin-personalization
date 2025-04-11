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
            "cciNumberOfCategories": {
                "ZERO_CATEGORIES": 0,
                "ONE_CATEGORY": 1,
            },
            "numberOfLiverMetastases": {
                "ONE": 1,
                "TWO": 2,
                "THREE": 3,
                "FOUR": 4,
                "FIVE_OR_MORE": 5,
                "MULTIPLE_BUT_EXACT_NUMBER_UNKNOWN": 5  # The median number of multiple metastases is 5+
            },
            "asaClassificationPreSurgeryOrEndoscopy": {
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
            "tumorDifferentiationGrade": {
                "GRADE_1_OR_WELL_DIFFERENTIATED": 1,
                "GRADE_2_OR_MODERATELY_DIFFERENTIATED": 2,
                "GRADE_3_OR_POORLY_DIFFERENTIATED": 3,
                "GRADE_4_OR_UNDIFFERENTIATED_OR_ANAPLASTIC_OR_GGG4": 4
            },
            "tnmCT": self.tnmT_lookup,
            "tnmPT": self.tnmT_lookup,
            "tnmCN": self.tnmN_lookup,
            "tnmPN": self.tnmN_lookup,
            "tnmCM": self.tnmM_lookup,
            "tnmPM": self.tnmM_lookup,
            "stageCTNM": self.stageTnm_lookup,
            "stagePTNM": self.stageTnm_lookup,
            "stageTNM": self.stageTnm_lookup,
        }

        self.features = [
            'ageAtMetastasisDetection',
            'albumine',
            'alkalinePhosphatase',
            'anorectalVergeDistanceCategory',
            'asaClassificationPreSurgeryOrEndoscopy',
            'carcinoEmbryonicAntigen',
            'cci',
            'cciHasAids',
            'cciHasCerebrovascularDisease',
            'cciHasCollagenosis',
            'cciHasCongestiveHeartFailure',
            'cciHasCopd',
            'cciHasDementia',
            'cciHasDiabetesMellitus',
            'cciHasDiabetesMellitusWithEndOrganDamage',
            'cciHasHemiplegiaOrParaplegia',
            'cciHasLiverDisease',
            'cciHasMildLiverDisease',
            'cciHasMyocardialInfarct',
            'cciHasOtherMalignancy',
            'cciHasOtherMetastaticSolidTumor',
            'cciHasPeripheralVascularDisease',
            'cciHasRenalDisease',
            'cciHasUlcerDisease',
            'cciNumberOfCategories',
            'consolidatedTumorType',
            'distanceToMesorectalFasciaMm',
            'distantMetastasesDetectionStatus',
            'extraMuralInvasionCategory',
            'hasBrafMutation',
            'hasBrafV600EMutation',
            'hasDoublePrimaryTumor',
            'hasHadPriorTumor',
            'hasKrasG12CMutation',
            'hasMsi',
            'hasRasMutation',
            'investigatedLymphNodesNumber',
            'lactateDehydrogenase',
            'leukocytesAbsolute',
            'lymphaticInvasionCategory',
            'maximumSizeOfLiverMetastasisMm',
            'mesorectalFasciaIsClear',
            'metastasesSurgeries',
            'metastasisLocationGroupsPriorToSystemicTreatment',
            'neutrophilsAbsolute',
            'numberOfLiverMetastases',
            'positiveLymphNodesNumber',
            'presentedWithIleus',
            'presentedWithPerforation',
            'radiotherapies',
            'sex',
            'sidedness',
            'stageCTNM',
            'stagePTNM',
            'stageTNM',
            'systemicTreatmentPlan',
            'tumorBasisOfDiagnosis',
            'tumorDifferentiationGrade',
            'tumorIncidenceYear',
            'tumorLocation',
            'tumorRegression',
            'venousInvasionDescription',
            'whoStatusPreTreatmentStart',
        ]
        
lookup_manager = LookupManager()