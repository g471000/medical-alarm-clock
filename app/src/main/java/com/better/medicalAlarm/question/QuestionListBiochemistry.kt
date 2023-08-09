package com.better.medicalAlarm.question

class QuestionListBiochemistry {

    fun getBioChemistryQuestions(): List<Question> {
        return bioChemistryQuestions
    }

    private val bioChemistryQuestions = listOf(
        Question(
            id = 0,
            description = "Which molecule is known as the 'energy currency' of the cell?",
            choices = listOf(
                "ATP (Adenosine Triphosphate)",
                "DNA (Deoxyribonucleic Acid)",
                "RNA (Ribonucleic Acid)",
                "NAD+ (Nicotinamide Adenine Dinucleotide)"
            ),
            correctAnswer = 0
        ),
        Question(
            id = 1,
            description = "Which polysaccharide serves as the primary storage form of glucose in animals?",
            choices = listOf("Glycogen", "Cellulose", "Starch", "Chitin"),
            correctAnswer = 1
        ),
        Question(
            id = 2,
            description = "Which enzyme is responsible for breaking down fats (triglycerides) into fatty acids and glycerol?",
            choices = listOf("Lipase", "Amylase", "Trypsin", "Pepsin"),
            correctAnswer = 2
        ),
        Question(
            id = 3,
            description = "How many essential amino acids are there in humans?",
            choices = listOf("9", "12", "20", "22"),
            correctAnswer = 3
        ),
        Question(
            id = 4,
            description = "Which biomolecule carries the genetic information of an organism?",
            choices = listOf("DNA (Deoxyribonucleic Acid)", "RNA (Ribonucleic Acid)", "Protein", "Carbohydrate"),
            correctAnswer = 0
        ),
        Question(
            id = 5,
            description = "During glycolysis, one molecule of glucose is converted into how many molecules of pyruvate?",
            choices = listOf("2", "3", "4", "6"),
            correctAnswer = 1
        ),
        Question(
            id = 6,
            description = "What is the basic building block of proteins?",
            choices = listOf("Amino Acid", "Monosaccharide", "Fatty Acid", "Nucleotide"),
            correctAnswer = 2
        ),
        Question(
            id = 7,
            description = "Which type of RNA carries the genetic code from DNA to the ribosome during protein synthesis?",
            choices = listOf("mRNA (Messenger RNA)", "tRNA (Transfer RNA)", "rRNA (Ribosomal RNA)", "snRNA (Small Nuclear RNA)"),
            correctAnswer = 3
        ),
        Question(
            id = 8,
            description = "Which process involves the conversion of glucose into pyruvate and is considered the first step of cellular respiration?",
            choices = listOf("Glycolysis", "Citric Acid Cycle", "Electron Transport Chain", "Oxidative Phosphorylation"),
            correctAnswer = 0
        ),
        Question(
            id = 9,
            description = "Which lipid molecule forms the basis of cell membranes?",
            choices = listOf("Phospholipid", "Triglyceride", "Steroid", "Cholesterol"),
            correctAnswer = 1
        ),
        Question(
            id = 10,
            description = "What is the full name of the enzyme commonly used in PCR (Polymerase Chain Reaction)?",
            choices = listOf("Taq DNA Polymerase", "RNA Polymerase", "DNA Ligase", "DNA Helicase"),
            correctAnswer = 2
        ),
        Question(
            id = 11,
            description = "In DNA, adenine (A) pairs with which other nitrogenous base?",
            choices = listOf("Thymine (T)", "Cytosine (C)", "Guanine (G)", "Uracil (U)"),
            correctAnswer = 3
        ),
        Question(
            id = 12,
            description = "Which organelle is responsible for protein synthesis in eukaryotic cells?",
            choices = listOf("Ribosome", "Endoplasmic Reticulum", "Golgi Apparatus", "Lysosome"),
            correctAnswer = 0
        ),
        Question(
            id = 13,
            description = "What is the final electron acceptor in the electron transport chain of cellular respiration?",
            choices = listOf("Oxygen (O2)", "NADH", "FADH2", "ATP"),
            correctAnswer = 1
        ),
        Question(
            id = 14,
            description = "Which vitamin is essential for blood clotting and is often referred to as the 'clotting vitamin'?",
            choices = listOf("Vitamin K", "Vitamin C", "Vitamin D", "Vitamin B12"),
            correctAnswer = 2
        ),
        Question(
            id = 15,
            description = "What is the primary function of the enzyme DNA polymerase?",
            choices = listOf(
                "Synthesize new DNA strands during replication",
                "Break down DNA molecules into nucleotides",
                "Transcribe DNA into RNA",
                "Repair damaged DNA molecules"
            ),
            correctAnswer = 3
        ),
        Question(
            id = 16,
            description = "Which process converts glucose into lactic acid in the absence of oxygen?",
            choices = listOf("Lactic Acid Fermentation", "Alcoholic Fermentation", "Glycolysis", "Citric Acid Cycle"),
            correctAnswer = 0
        ),
        Question(
            id = 17,
            description = "Which nitrogenous base is found in RNA but not in DNA?",
            choices = listOf("Uracil (U)", "Thymine (T)", "Cytosine (C)", "Guanine (G)"),
            correctAnswer = 1
        ),
        Question(
            id = 18,
            description = "Which molecule carries amino acids to the ribosome during protein synthesis?",
            choices = listOf("tRNA (Transfer RNA)", "mRNA (Messenger RNA)", "rRNA (Ribosomal RNA)", "snRNA (Small Nuclear RNA)"),
            correctAnswer = 2
        ),
        Question(
            id = 19,
            description = "Which metabolic pathway is responsible for producing the majority of ATP in aerobic respiration?",
            choices = listOf("Electron Transport Chain", "Glycolysis", "Citric Acid Cycle", "Fermentation"),
            correctAnswer = 3
        ),
        Question(
            id = 20,
            description = "Which molecule serves as a carrier of high-energy electrons during cellular respiration?",
            choices = listOf(
                "NAD+ (Nicotinamide Adenine Dinucleotide)",
                "FADH2 (Flavin Adenine Dinucleotide)",
                "ATP (Adenosine Triphosphate)",
                "GTP (Guanosine Triphosphate)"
            ),
            correctAnswer = 0
        ),
        Question(
            id = 21,
            description = "What is the main function of the citric acid cycle (Krebs cycle) in cellular respiration?",
            choices = listOf(
                "Generate high-energy electron carriers",
                "Produce ATP directly",
                "Convert glucose to pyruvate",
                "Break down fatty acids"
            ),
            correctAnswer = 1
        ),
        Question(
            id = 22,
            description = "Which organ is primarily responsible for the detoxification of drugs and metabolites?",
            choices = listOf("Liver", "Kidneys", "Lungs", "Heart"),
            correctAnswer = 2
        ),
        Question(
            id = 23,
            description = "Which amino acid is commonly associated with nitrogenous waste in humans?",
            choices = listOf("Urea", "Ammonia", "Creatinine", "Bilirubin"),
            correctAnswer = 3
        ),
        Question(
            id = 24,
            description = "Which hormone regulates blood sugar levels by promoting the uptake of glucose into cells?",
            choices = listOf("Insulin", "Glucagon", "Epinephrine", "Thyroxine"),
            correctAnswer = 0
        ),
        Question(
            id = 25,
            description = "Which process involves the conversion of light energy into chemical energy in plants?",
            choices = listOf("Photosynthesis", "Cellular Respiration", "Fermentation", "Glycolysis"),
            correctAnswer = 1
        ),
        Question(
            id = 26,
            description = "Which lipoprotein is responsible for transporting cholesterol from the liver to peripheral tissues?",
            choices = listOf(
                "LDL (Low-Density Lipoprotein)",
                "HDL (High-Density Lipoprotein)",
                "VLDL (Very Low-Density Lipoprotein)",
                "Chylomicrons"
            ),
            correctAnswer = 2
        ),
        Question(
            id = 27,
            description = "What is the function of hemoglobin in red blood cells?",
            choices = listOf(
                "Transport oxygen from lungs to tissues",
                "Transport carbon dioxide from tissues to lungs",
                "Regulate blood sugar levels",
                "Produce white blood cells"
            ),
            correctAnswer = 3
        ),
        Question(
            id = 28,
            description = "Which vitamin is essential for proper blood clotting?",
            choices = listOf("Vitamin K", "Vitamin C", "Vitamin D", "Vitamin A"),
            correctAnswer = 0
        ),
        Question(
            id = 29,
            description = "Which molecule is the primary source of energy for cells?",
            choices = listOf("Glucose", "Amino Acids", "Fatty Acids", "Nucleotides"),
            correctAnswer = 1
        ),
// Continue adding 10 more questions here with alternating correct answers
        Question(
            id = 50,
            description = "Which molecule is responsible for storing and transporting genetic information in cells?",
            choices = listOf("DNA (Deoxyribonucleic Acid)", "RNA (Ribonucleic Acid)", "Protein", "Lipid"),
            correctAnswer = 0
        ),
        Question(
            id = 51,
            description = "Which process involves the conversion of glucose into lactic acid in muscle cells?",
            choices = listOf("Lactic Acid Fermentation", "Alcoholic Fermentation", "Glycolysis", "Citric Acid Cycle"),
            correctAnswer = 1
        ),
        Question(
            id = 52,
            description = "Which organelle is responsible for ATP production through oxidative phosphorylation?",
            choices = listOf("Mitochondria", "Chloroplast", "Endoplasmic Reticulum", "Nucleus"),
            correctAnswer = 2
        ),
        Question(
            id = 53,
            description = "Which nucleotide base pairs with adenine in DNA?",
            choices = listOf("Thymine (T)", "Cytosine (C)", "Guanine (G)", "Uracil (U)"),
            correctAnswer = 3
        ),
        Question(
            id = 54,
            description = "Which enzyme catalyzes the conversion of DNA to RNA?",
            choices = listOf("RNA Polymerase", "DNA Polymerase", "Ligase", "Helicase"),
            correctAnswer = 0
        ),
        Question(
            id = 55,
            description = "What is the main function of the Golgi apparatus?",
            choices = listOf(
                "Modify and package proteins for secretion",
                "Synthesize ATP",
                "Perform photosynthesis",
                "Store genetic information"
            ),
            correctAnswer = 1
        ),
        Question(
            id = 56,
            description = "Which molecule is a major component of cell membranes and is responsible for maintaining membrane fluidity?",
            choices = listOf("Phospholipid", "Cholesterol", "Protein", "Triglyceride"),
            correctAnswer = 2
        ),
        Question(
            id = 57,
            description = "Which process involves the conversion of light energy into chemical energy in photosynthetic organisms?",
            choices = listOf("Photosynthesis", "Cellular Respiration", "Fermentation", "Glycolysis"),
            correctAnswer = 3
        ),
        Question(
            id = 58,
            description = "Which hormone regulates calcium levels in the blood?",
            choices = listOf("Parathyroid Hormone (PTH)", "Insulin", "Thyroxine", "Adrenaline"),
            correctAnswer = 0
        ),
        Question(
            id = 59,
            description = "Which metabolic pathway involves the breakdown of glucose to produce ATP and pyruvate?",
            choices = listOf("Glycolysis", "Krebs Cycle", "Electron Transport Chain", "Beta Oxidation"),
            correctAnswer = 1
        ),
        Question(
            id = 60,
            description = "Which molecule is produced during anaerobic respiration in muscle cells and causes muscle fatigue?",
            choices = listOf("Lactic Acid", "Pyruvate", "Acetyl-CoA", "Ethanol"),
            correctAnswer = 0
        ),
        Question(
            id = 61,
            description = "Which molecule plays a crucial role in transporting oxygen in the blood?",
            choices = listOf("Hemoglobin", "Myoglobin", "Cytochrome", "Insulin"),
            correctAnswer = 1
        ),
        Question(
            id = 62,
            description = "Which process involves the conversion of light energy into ATP in photosynthetic organisms?",
            choices = listOf("Photophosphorylation", "Glycolysis", "Krebs Cycle", "Beta Oxidation"),
            correctAnswer = 2
        ),
        Question(
            id = 63,
            description = "Which element is a central component of hemoglobin and myoglobin?",
            choices = listOf("Iron", "Calcium", "Sodium", "Potassium"),
            correctAnswer = 3
        ),
        Question(
            id = 64,
            description = "Which enzyme is responsible for unwinding the DNA double helix during replication?",
            choices = listOf("Helicase", "Polymerase", "Ligase", "Nuclease"),
            correctAnswer = 0
        ),
        Question(
            id = 65,
            description = "Which molecule acts as a signaling molecule in various physiological processes and is derived from arachidonic acid?",
            choices = listOf("Prostaglandin", "Insulin", "Melatonin", "Thyroxine"),
            correctAnswer = 1
        ),
        Question(
            id = 66,
            description = "Which process involves the conversion of pyruvate into acetyl-CoA and is a key step in cellular respiration?",
            choices = listOf("Pyruvate Decarboxylation", "Glycolysis", "Oxidative Phosphorylation", "Fermentation"),
            correctAnswer = 2
        ),
        Question(
            id = 67,
            description = "Which molecule is used as an energy source for short bursts of intense activity, such as sprinting?",
            choices = listOf("Creatine Phosphate", "Glucose", "Fatty Acids", "Lactic Acid"),
            correctAnswer = 3
        ),
        Question(
            id = 68,
            description = "Which type of RNA carries amino acids to the ribosome during protein synthesis?",
            choices = listOf("tRNA (Transfer RNA)", "mRNA (Messenger RNA)", "rRNA (Ribosomal RNA)", "snRNA (Small Nuclear RNA)"),
            correctAnswer = 0
        ),
        Question(
            id = 69,
            description = "Which vitamin is essential for maintaining healthy vision and is often referred to as the 'vision vitamin'?",
            choices = listOf("Vitamin A", "Vitamin E", "Vitamin C", "Vitamin D"),
            correctAnswer = 1
        ),
        Question(
            id = 70,
            description = "Which process involves the conversion of pyruvate into ethanol and carbon dioxide in yeast cells?",
            choices = listOf("Alcoholic Fermentation", "Lactic Acid Fermentation", "Glycolysis", "Oxidative Phosphorylation"),
            correctAnswer = 2
        ),
        Question(
            id = 71,
            description = "Which molecule is a major component of cell membranes and is involved in cell signaling?",
            choices = listOf("Phospholipid", "Cholesterol", "Triglyceride", "Hemoglobin"),
            correctAnswer = 3
        ),
        Question(
            id = 72,
            description = "Which hormone regulates water retention and blood pressure?",
            choices = listOf("Aldosterone", "Insulin", "Growth Hormone", "Thyroid Hormone"),
            correctAnswer = 0
        ),
        Question(
            id = 73,
            description = "Which metabolic pathway involves the breakdown of fatty acids to produce acetyl-CoA?",
            choices = listOf("Beta Oxidation", "Glycolysis", "Citric Acid Cycle", "Electron Transport Chain"),
            correctAnswer = 1
        ),
        Question(
            id = 74,
            description = "Which molecule is responsible for carrying electrons between the complexes of the electron transport chain?",
            choices = listOf(
                "Ubiquinone (Coenzyme Q)",
                "Cytochrome C",
                "Flavin Mononucleotide (FMN)",
                "Nicotinamide Adenine Dinucleotide (NADH)"
            ),
            correctAnswer = 2
        ),
        Question(
            id = 75,
            description = "Which nitrogenous base is found in RNA but not in DNA?",
            choices = listOf("Uracil (U)", "Thymine (T)", "Cytosine (C)", "Guanine (G)"),
            correctAnswer = 3
        ),
        Question(
            id = 76,
            description = "Which process involves the conversion of ammonia into urea in the liver?",
            choices = listOf("Urea Cycle", "Glycolysis", "Krebs Cycle", "Beta Oxidation"),
            correctAnswer = 0
        ),
        Question(
            id = 77,
            description = "Which molecule is a storage form of glucose in animals and is primarily found in liver and muscle cells?",
            choices = listOf("Glycogen", "Starch", "Cellulose", "Chitin"),
            correctAnswer = 1
        ),
        Question(
            id = 78,
            description = "Which enzyme is responsible for breaking down starch into maltose?",
            choices = listOf("Amylase", "Lipase", "Protease", "Nuclease"),
            correctAnswer = 2
        ),
        Question(
            id = 79,
            description = "Which molecule plays a key role in the clotting of blood?",
            choices = listOf("Thrombin", "Hemoglobin", "Insulin", "Glucagon"),
            correctAnswer = 3
        )
    )
}
