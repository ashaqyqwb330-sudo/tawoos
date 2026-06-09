package com.example.data

import com.example.model.FAQItem
import com.example.model.Program
import com.example.model.ProgramCourse
import com.example.model.ProgramTerm

object ProgramsData {
    fun getAllPrograms(): List<Program> {
        return listOf(
            Program(
                id = "nursing_diploma",
                title = "دبلوم التمريض الميداني المتقدم",
                degree = "Advanced Diploma in Field Nursing (OR & Critical Care)",
                department = "قسم التمريض العسكري",
                description = "برنامج تطبيقي مكثف يدمج علوم التمريض المتقدمة بالمهارات التمريضية الجراحية والحرجة والإنعاشية، بما يمكن الخريج من العمل بكفاءة في غرف العمليات، وحدات العناية المركزة، والبيئات القتالية والنائية، كجزء من فريق جراحي متنقل أو داخل المستشفيات العسكرية، والمساهمة في إنقاذ الأرواح وتقليل الوفيات والمضاعفات المرتبطة بالرعاية التمريضية.",
                duration = "6 أشهر (98 ساعة معتمدة)",
                language = "العربية مع استخدام المصطلحات الطبية بالإنجليزية",
                objectives = listOf(
                    "تطبيق منهجية التقييم المتقدم (ABCDE) وإدارة تمريضية متكاملة لمجرى الهواء في إصابات القتال.",
                    "إتقان تقنيات التمريض الجراحي والإنعاش في الظروف الميدانية والمستشفيات.",
                    "إدارة المسالك الهوائية جراحياً (بضع الغشاء الحلقي والدرقي) وتأمين التنفس الصناعي.",
                    "التعامل مع مضاعفات التمريض الحادة (صدمة تحسسية، اكتئاب تنفسي، ارتفاع حرارة خبيث).",
                    "تطبيق مبادئ إنعاش التحكم بالضرر (DCR) وإدارة نقل الدم ومنتجاته في الميدان.",
                    "إدارة الألم الحاد والمزمن لدى المصابين باستخدام بروتوكولات متعددة الوسائط.",
                    "العمل بكفاءة ضمن فريق جراحي متنقل وقيادة عمليات الإخلاء الطبي من منظور تمريضي.",
                    "التعامل مع الإصابات الكيميائية والانفجارية وتقديم الرعاية التمريضية في الرعاية الممتدة (PFC)."
                ),
                plos = listOf(
                    "K1 – يشرح تشريح الجسم البشري مع التركيز على المناطق الحرجة للتمريض الميداني.",
                    "K2 – يحلل فسيولوجيا الجهاز التنفسي والقلبي الوعائي والعصبي وتأثير الإصابات والأمراض عليها.",
                    "K3 – يصف آليات عمل الأدوية المستخدمة في التمريض الميداني (المسكنات، المضادات الحيوية، أدوية الإنعاش).",
                    "K4 – يعدد مضاعفات الرعاية التمريضية الشائعة في الميدان وآليات التعامل معها.",
                    "K5 – يشرح مبادئ الرعاية التكتيكية للمصابين (TCCC) والرعاية المطولة (PFC) من وجهة نظر تمريضية.",
                    "C1 – يشخص الحالة الحرجة ويحدد أولويات التدخل التمريضي وفق مبدأ Damage Control.",
                    "C2 – يقيم فعالية الرعاية التمريضية ويعدل الخطة حسب استجابة المصاب والموارد المتاحة.",
                    "C3 – يحلل نتائج الفحص السريري والمراقبة (غازات الدم، العلامات الحيوية) لاتخاذ قرارات تمريضية.",
                    "C4 – يتخذ قرارات أخلاقية في ظروف نقص الموارد أو الإصابات الجماعية فيما يخص الرعاية التمريضية.",
                    "P1 – يؤمن مجرى هوائي جراحياً (Cricothyroidotomy) خلال دقيقتين.",
                    "P2 – يدخل أنبوباً صدرياً (Chest tube) لتفريغ استرواح الصدر الضاغط.",
                    "P3 – يدير الرعاية التمريضية للمصاب تحت التخدير العام في بيئة ميدانية مع مراقبة أساسية.",
                    "P4 – ينفذ تقنيات تمريضية متقدمة (إدارة الألم، العناية بالجروح، التمريض الناحي)."
                ),
                curriculum = listOf(
                    ProgramTerm(
                        termName = "الفصل الأول (أساسيات التمريض الميداني المتقدم)",
                        courses = listOf(
                            ProgramCourse("NRS101", "مصطلحات تمريضية متقدمة وتطبيقاتها", 4),
                            ProgramCourse("NRS102", "تمريض الصدمات والإنعاش المتقدم", 4),
                            ProgramCourse("NRS103", "فسيولوجيا الصدمة والرعاية الحرجة", 3),
                            ProgramCourse("NRS104", "صيدلة التمريض الميداني المتقدم", 3)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الثاني (التمريض السريري والمتقدم)",
                        courses = listOf(
                            ProgramCourse("NRS201", "تمريض العناية المركزة الأساسي", 4),
                            ProgramCourse("NRS202", "إنعاش قلبي رئوي متقدم (ACLS) للممرض", 4),
                            ProgramCourse("NRS203", "تمريض العمليات الجراحية المتقدم", 4)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الثالث (التمريض التكتيكي والميداني)",
                        courses = listOf(
                            ProgramCourse("TAN301", "الرعاية التكتيكية للمصابين (TCCC) للممرض", 4),
                            ProgramCourse("TAN302", "تمريض العمليات الخاصة والقوات الخاصة", 4),
                            ProgramCourse("TAN303", "الإخلاء الطبي التكتيكي للمرضى (تمريض)", 4)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الرابع (التدريب السريري والميداني الشامل)",
                        courses = listOf(
                            ProgramCourse("SUR401", "مهارات جراحية أساسية للممرض", 3),
                            ProgramCourse("CLN405", "تدريب سريري في التمريض الميداني (مستشفى)", 4),
                            ProgramCourse("FLD408", "التدريب الميداني العسكري (تمرين شامل)", 6)
                        )
                    )
                ),
                faq = listOf(
                    FAQItem("ما هي مدة البرنامج؟", "مدة البرنامج 6 أشهر أكاديمية، بواقع 98 ساعة معتمدة."),
                    FAQItem("ما هي شروط القبول؟", "الحصول على دبلوم الإسعاف الحربي أو دبلوم تمريض سابق بتقدير جيد، وخبرة ميدانية سنة، واجتياز اختبار القبول والمقابلة."),
                    FAQItem("ما هي المهارات التي سأتعلمها؟", "إنعاش متقدم، إدارة مجرى الهواء جراحياً، تمريض جراحي وعناية مركزة، وإدارة الألم.")
                ),
                admissionRequirements = "الحصول على دبلوم الإسعاف الحربي أو دبلوم مساعد طبي / تمريض سابق بتقدير جيد، شهادة الثانوية العامة (قسم علمي)، خبرة ميدانية لا تقل عن سنة، اجتياز اختبار القبول والمقابلة الشخصية، اللياقة الطبية والنفسية، وموافقة الجهة العسكرية.",
                graduationRequirements = "إكمال 98 ساعة معتمدة بنجاح، الحد الأدنى للنجاح 65% في كل مقرر، إكمال سجل المهارات (Logbook) بالكامل، اجتياز التقييم النهائي (المحاكاة الميدانية)، والحصول على تقدير عام لا يقل عن مقبول."
            ),
            Program(
                id = "anesthesia_diploma",
                title = "دبلوم التخدير الميداني المتقدم",
                degree = "Advanced Diploma in Field Anesthesia",
                department = "قسم التخدير والعناية المركزة",
                description = "برنامج تطبيقي مكثف يدمج العلوم الطبية الأساسية بالمهارات التخديرية والإنعاشية المتقدمة، بما يمكن الخريج من العمل بكفاءة في الميدان كمحترف تخدير ميداني يساهم في إنقاذ المصابين والسيطرة على الآلام الشديدة في الجبهات الأمامية والمستشفيات الميدانية.",
                duration = "6 أشهر (98 ساعة معتمدة)",
                language = "العربية مع استخدام المصطلحات الطبية بالإنجليزية",
                objectives = listOf(
                    "إتقان تنصيب وإعداد أجهزة التخدير والمراقبة وتكييفها مع ظروف الميدان والحرائق.",
                    "إعطاء التخدير العام والتخدير الموضعي وتطبيق بروتوكولات التسكين متعدد الوسائط.",
                    "التعامل الاحترافي مع مشاكل وصدمات مجرى الهواء وإتقان بضع الغشاء الحلقي في حالات الطوارئ القصوى.",
                    "تطبيق مبادئ إنعاش السيطرة على الضرر ونقل منتجات الدم والسوائل الدافئة.",
                    "اتخاذ القرارات الحاسمة لتدبير مضاعفات التخدير كالصدمات الحادة وارتفاع الحرارة الخبيث.",
                    "الاحاطة الطبية بإنعاش حالات الغرق والإصابات الكيميائية والبيولوجية في الميدان عسكرياً."
                ),
                plos = listOf(
                    "K1 – فهم عميق لفسيولوجيا وصيدلة التخدير وتأثير الغازات والمسكنات على جريح الحرب.",
                    "K2 – تشخيص وعلاج انسدادات مجرى الهواء وإتقان الطرق الجراحية للتأمين التنفسي.",
                    "K3 – معرفة تامة في إنعاش التسمم الكيمائي والبيولوجي والوقائي عسكرياً في الجبهات.",
                    "P1 – تركيب القسطرة الوريدية المركزية والشريانية في ظروف الحصار والعمل الميداني.",
                    "P2 – تحديد جرعات التخدير الآمنة للمصاب المنصدم وتفادي تفاقم هبوط الضغط.",
                    "P3 – إدارة عملية التخدير والتنفس اليدوي لمدد طويلة في ظروف تفتقر للطاقة المستمرة.",
                    "G1 – المهارة العالية في العمل القيادي والإدارة السريعة للكوارث وحالات الإصابات الجماعية."
                ),
                curriculum = listOf(
                    ProgramTerm(
                        termName = "الفصل الأول (علوم التخدير الأساسية)",
                        courses = listOf(
                            ProgramCourse("ANS101", "مصطلحات تخدير متقدمة وتطبيقاتها", 4),
                            ProgramCourse("ANS102", "علم الأدوية والفارماكولوجي للتخدير الميداني", 4),
                            ProgramCourse("ANS103", "فيزياء ومعدات أجهزة التخدير عسكرياً", 3)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الثاني (تطبيقات التخدير السريري)",
                        courses = listOf(
                            ProgramCourse("ANS201", "التخدير العام وتأمين مجرى الهواء جراحياً", 4),
                            ProgramCourse("ANS202", "التخدير والتحييد الموضعي والناحي للمصاب", 4),
                            ProgramCourse("ANS203", "إنعاش حالات الحوادث والصدمات المتقدم", 4)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الثالث (التخدير التكتيكي والطوارئ)",
                        courses = listOf(
                            ProgramCourse("ANS301", "رعاية التخدير وإدارة الرعاية المطولة (PFC)", 4),
                            ProgramCourse("ANS302", "إدارة إصابات الميدان الكيميائية والنووية عسكرياً", 4),
                            ProgramCourse("ANS303", "التخدير اللوحي وإخلاء المصابين تحت التأثير", 4)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الرابع (التدريب والمحاكاة التطبيقية)",
                        courses = listOf(
                            ProgramCourse("ANS401", "تدريب عملي سريري في غرف العمليات والمشافي عسكرياً", 6),
                            ProgramCourse("ANS402", "مشروع التخرج والمحاكاة الميدانية للتخدير", 4),
                            ProgramCourse("ANS403", "أخلاقيات وقوانين الصحة والطب العسكري", 3)
                        )
                    )
                ),
                faq = listOf(
                    FAQItem("ما هي المدة التدريبية لبرنامج التخدير؟", "مدة البرنامج 6 أشهر مكثفة بواقع 4 فصول دراسية."),
                    FAQItem("هل يمنح البرنامج رخصة ممارسة حربية؟", "نعم يمنح دبلوم متقدم معتمد للعمل الطبي العسكري الميداني."),
                    FAQItem("هل التدريب نظري أم عملي؟", "يركز بنسبة 70% على المحاكاة والتدريب السريري في المشافي العسكرية وغرف العمليات.")
                ),
                admissionRequirements = "أن يكون حاصلاً على دبلوم العناية أو التمريض بتقدير عام جيد جداً، خبرة لا تقل عن سنة في الوحدات الميدانية، واجتياز اختبار القبول والفرز الطبي النفسي بنجاح.",
                graduationRequirements = "إكمال كافة الساعات المعتمدة (98 ساعة)، وتقديم مشروع تخرج متميز في إدارة مضاعفات التخدير، وحضور المحاكاة الميدانية التكتيكية الشاملة."
            ),
            Program(
                id = "general_medicine",
                title = "بكالوريوس الطب البشري والجراحة العسكرية",
                degree = "Bachelor of Medicine and Surgery (MD / Military Surgeon)",
                department = "كلية الشهيد د. زيد طاووس للعلوم الصحية",
                description = "برنامج طبي تخصصي ريادي يدمج بين أرقى معايير التعليم الطبي والسريري العالمي والتدريب التكتيكي على جراحة الحروب ومواجهة الأزمات الميدانية بكفاءة وسرعة، تماشياً مع قيم ورسالة الشهيد الدكتور زيد طاووس.",
                duration = "6 سنوات دراسيّة + سنة الامتياز الطبي العملياتي",
                language = "العربية والإنجليزية المعتمدة مصطلحياً",
                objectives = listOf(
                    "توفير تعليم طبي سريري يواكب التطور الحديث على أساس علمي وعقائدي متين.",
                    "تدريب الطلاب مهارات الميدان التكتيكية لإنقاذ المصابين في الخطوط الأمامية.",
                    "بناء القيادة والجاهزية العالية لتطوير منظومة الرعاية الممتدة والصحة العسكرية."
                ),
                plos = listOf(
                    "K1 – تشخيص وتوصيف الأمراض وصدمات النزيف ومفرزات الحروب عسكرياً وسريرياً.",
                    "C1 – التحليل السريري السريع والحكم الأخلاقي لإتخاذ قرارات الفرز الحاسم للجرحى تحت الضغط الميداني.",
                    "P1 – إتقان فتح مجرى الهواء والإنعاش التكتيكي الكامل وتركيب الأنبوب الصدري والقسطرات الشريانية المتقدمة.",
                    "G1 – مهارات القيادة والعمل التشاركي مع الطواقم الجراحية في الخطوط الدفاعية ومستشفيات النطاق الحرج."
                ),
                curriculum = listOf(
                    ProgramTerm(
                        termName = "المرحلة الأولى أساسية (سنة 1-3)",
                        courses = listOf(
                            ProgramCourse("MED101", "علم التشريح والأنسجة الطبية المدمجة", 5),
                            ProgramCourse("MED102", "علم وظائف الأعضاء والفسيولوجيا والتمريض الأساسي", 5),
                            ProgramCourse("MED103", "الكيمياء السريرية وعلم الحيوية الميكروبية", 4)
                        )
                    ),
                    ProgramTerm(
                        termName = "المرحلة الثانية سريرية (سنة 4-6)",
                        courses = listOf(
                            ProgramCourse("MED201", "الأمراض الباطنية المتقدمة والسريريات التخصصية", 6),
                            ProgramCourse("MED202", "جراحة الحروب والصدمات الطبية المسلحة", 6),
                            ProgramCourse("MED203", "طب الطوارئ والكوارث والإنعاش التكتيكي المتقدم", 5)
                        )
                    )
                ),
                faq = listOf(
                    FAQItem("ما هو برنامج بكالوريوس الطب البشري العسكري؟", "هو برنامج أكاديمي وتكتيكي وعسكري يمتد 6 سنوات بالإضافة إلى سنة تدريب سريري عام في مستشفيات القوات المسلحة لتأهيل ضباط أطباء ذوي مهارات عملياتية عالية."),
                    FAQItem("ما هو معدل القبول المعتمد للطب البشري؟", "يشترط الحصول على معدل ثانوية عامة قسم علمي لا يقل عن 85% مع اجتياز الفحص والفرز العسكري التخصصي.")
                ),
                admissionRequirements = "الحصول على الشهادة الثانوية علمي بمعدل لا يقل عن 85%، اجتياز الفحوص الطبية، واللياقة البدنية الشاملة، والتفوق في كشوف القدرات والمقابلة الشخصية للكلية.",
                graduationRequirements = "إكمال بنجاح جميع سنوات المراحل الست والدورات الميدانية، واجتياز الامتحان السريري العسكري الموحد وعقد سنة الامتياز الطبي بنجاح."
            ),
            Program(
                id = "military_first_aid",
                title = "دبلوم الإسعاف الحربي الشامل",
                degree = "Diploma in Military First Aid",
                department = "المعهد الطبي العسكري - كلية الطب والعلوم الصحية",
                description = "يعد هذا الدبلوم التخصصي برنامجاً أكاديمياً مكثفاً يهدف إلى تأهيل مسعف حربي ميداني متكامل (معالج، وقائد، ومستشار) قادر على تقديم رعاية جراحية وإسعافية منقذة للحياة وفق أحدث المعايير العسكرية والطبية التكتيكية العالمية (TCCC، ATLS، PHTLS).",
                duration = "سنة أكاديمية (36 ساعة معتمدة)",
                language = "العربية مع استخدام المصطلحات الطبية بالإنجليزية",
                objectives = listOf(
                    "ترسيخ وتأصيل القيم الدينية والاعتزاز بالنهج القرآني والعقيدة الجهادية والالتزام التام بأخلاقيات الطب العسكري الميداني.",
                    "تزويد الخريجين بالفهم العلمي المتكامل للعلوم الأساسية من خلال ربط التشريح والفسيولوجيا بمبادئ وتكتيكات الاستدلال التفريقي والتشخيص السريري.",
                    "تمكين الطلاب من إتقان كافة التدخلات الجراحية الميدانية الإسعافية: تحرير مجرى الهواء، السيطرة على النزيف بصمامات التورنيكيت وبدائل الدم في الخنادق، بزل الصدر، البتر الأولي، وتحرير الحجرات تحت الضغط الشديد.",
                    "إتقان مهارات القيادة التكتيكية والصحة الوقائية العسكرية وإدارة الحوادث الجماعية (MASCAL) ونقاط الفرز والإخلاء الطبي (CASEVAC/MEDEVAC).",
                    "بناء الإمكانات لإدارة الرعاية الممتدة والمطولة (PFC) في بيئات الحصار والظروف القاسية لمدة تتراوح بين 48 و72 ساعة."
                ),
                plos = listOf(
                    "K1: يشرح تفاصيل التركيب التشريحي لأجهزة الجسم والفسيولوجيا المرضية لتأثير إصابات المقذوفات والضغط والتفجيرات.",
                    "K2: يعدد خيارات الأدوية الطارئة ومضادات الصدمة وحساب جرعات العقاقير المسكنة والتخدير التكتيكي للمصابين.",
                    "C1: يحلل ويصنف أولويات التدخل في الإصابات الجماعية (MASCAL) ونقاط الفرز (Triage) تحت ضغوط المعارك الحربية.",
                    "C2: يفسر العلامات الحيوية، فصيلة الدم الفورية، الفحص بـ POCUS وغازات الدم لدعم تشخيص الإنعاش السريري بالساتر.",
                    "P1: يتقن مهارات ومناورات MARCH، تحرير وتأمين المجرى الهوائي جراحياً، وبزل الصدر بالإبر ومقاطع النزيف الشديد.",
                    "P2: يجري الجراحة الميدانية الصغرى والنزف الداخلي وعمليات البتر الاضطراري وتحرير حجرات الأطراف وإنقاذ الأعضاء.",
                    "G1: يقود الفصائل والفرق الطبية بمسارح الدفاع، ويوثق بدقة تقارير الإخلاء الطبي العسكرية MIST وبطاقات العلاج الميداني MIST-AT."
                ),
                curriculum = listOf(
                    ProgramTerm(
                        termName = "الفصل الدراسي الأول: الأسس المتقدمة",
                        courses = listOf(
                            ProgramCourse("CQ102", "الثقافة القرآنية (1)", 4),
                            ProgramCourse("ANAT201", "تشريح ووظائف الأعضاء", 4),
                            ProgramCourse("NURS201", "أسس تمريض", 3),
                            ProgramCourse("EMS201", "أمراض شائعة (1+2)", 3),
                            ProgramCourse("WMD401", "الحرب الجرثومية وأسلحة الدمار", 4),
                            ProgramCourse("PHAR301", "علم الأدوية 1+2", 3),
                            ProgramCourse("PUBH301", "مبادئ الطب الوقائي والصحة العامة", 3),
                            ProgramCourse("ENG101", "أساسيات اللغة الإنجليزية الطبية", 3),
                            ProgramCourse("MFA401", "إسعاف حربي منقذ ومتقدم", 4),
                            ProgramCourse("ARAB101", "اللغة العربية والتحرير العسكري", 1)
                        )
                    ),
                    ProgramTerm(
                        termName = "الفصل الدراسي الثاني: الرعاية المتقدمة والطوارئ",
                        courses = listOf(
                            ProgramCourse("QR202", "الثقافة القرآنية (2)", 3),
                            ProgramCourse("ANAT202", "التشريح السريري والعملياتي", 4),
                            ProgramCourse("EMS411", "أساسيات الإنعاش والطوارئ", 3),
                            ProgramCourse("WRIN301", "إصابات الحروب والصدمات التكتيكية", 3),
                            ProgramCourse("PHAR202", "علم الأدوية الطارئة والتسكين التكتيكي", 3),
                            ProgramCourse("BTS301", "أساسيات نقل الدم الميداني وبدائل السوائل", 3),
                            ProgramCourse("MFM401", "إدارة المراكز الميدانية وتخطيط الإخلاء الطبي", 2),
                            ProgramCourse("MEDT101", "مصطلحات طبية ولاتينية عسكرية وبطاقات MIST-AT", 3)
                        )
                    )
                ),
                faq = listOf(
                    FAQItem("ما هو الترتيب الصحيح للأولويات في إسعاف جرحى المعارك وفق بروتوكول MARCH؟", "الترتيب الصحيح هو: 1. M (Massive Bleeding - السيطرة على النزيف الشرياني الصاعق)، 2. A (Airway - فتح وتأمين المجرى الهوائي)، 3. R (Respiration - تدبير مشاكل التنفس بزل الصدر وإغلاق جروح الصدر المفتوحة)، 4. C (Circulation - فحص الدورة الدموية، إنعاش السوائل ونقل الدم)، 5. H (Head/Hypothermia - تدبير إصابات الرأس ومنع انخفاض حرارة الجسم)."),
                    FAQItem("متى يتم تركيب تورنيكيت (Tournitquet) فوق الملابس في إسعاف الجبهات؟", "يتم تركيب التورنيكيت فوق الملابس في مرحلة الرعاية تحت النار (Care Under Fire)، حيث يكون الخطر داهماً والوقت ضيقاً جداً، وتوضع في أعلى جزء ممكن من الطرف (High and Tight) فوق الملابس بسرعة دون إضاعة الوقت في تمزيق الثياب.")
                ),
                admissionRequirements = "يُشترط للقبول بالدبلوم الحصول على ثانوية عامة قسم علمي بنسبة لا تقل عن 65%، واجتياز الفحص الطبي العسكري المعتمد عسكرياً والبدني التخصصي، والمقابلة الشخصية للجنة الطبية التكتيكية، وموافقة الجهة العسكرية مع الالتزام التام بخدمة الجرحى.",
                graduationRequirements = "إتمام 36 ساعة معتمدة بنسبة نجاح لا تقل عن 65% في الأقسام النظرية و70% في الأقسام العملية والسريرية، مع تسجيل ما لا يقل عن 40-50 إجراءً في سجل المهارات العملياتية (Logbook)، واجتياز الاختبار الشامل للفحوص الطبية التكتيكية."
            )
        )
    }

    // Helper to format string list to a lightweight JSON array string
    private fun listToJson(list: List<String>): String {
        return list.joinToString(separator = "\",\"", prefix = "[\"", postfix = "\"]") {
            it.replace("\"", "\\\"").replace("\n", "\\n")
        }
    }

    // Helper to parse JSON array string back to a list
    fun jsonToList(json: String): List<String> {
        if (json.isEmpty() || json == "[]") return emptyList()
        val clean = json.removePrefix("[").removeSuffix("]")
        if (clean.isEmpty()) return emptyList()
        return clean.split("\",\"").map {
            it.removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"").replace("\\n", "\n")
        }
    }

    // Simple custom serializations
    fun serializeCurriculum(curriculum: List<ProgramTerm>): String {
        val result = StringBuilder("[")
        curriculum.forEachIndexed { i, term ->
            if (i > 0) result.append(",")
            result.append("{\"termName\":\"${term.termName}\",\"courses\":[")
            term.courses.forEachIndexed { j, course ->
                if (j > 0) result.append(",")
                result.append("{\"code\":\"${course.code}\",\"name\":\"${course.name}\",\"hours\":${course.hours}}")
            }
            result.append("]}")
        }
        result.append("]")
        return result.toString()
    }

    fun deserializeCurriculum(json: String): List<ProgramTerm> {
        val list = mutableListOf<ProgramTerm>()
        // Let's do simple regex extraction to avoid complex parsers crashing or failing
        val termRegex = "(\"termName\":\"[^\"]+\",\"courses\":\\[[^\\]]*\\])".toRegex()
        val terms = termRegex.findAll(json)
        for (termMatch in terms) {
            val content = termMatch.value
            val nameRegex = "\"termName\":\"([^\"]+)\"".toRegex()
            val name = nameRegex.find(content)?.groupValues?.get(1) ?: ""
            
            val coursesList = mutableListOf<ProgramCourse>()
            val courseRegex = "\\{\"code\":\"([^\"]+)\",\"name\":\"([^\"]+)\",\"hours\":(\\d+)\\}".toRegex()
            val courses = courseRegex.findAll(content)
            for (courseMatch in courses) {
                val code = courseMatch.groupValues[1]
                val nameCourse = courseMatch.groupValues[2]
                val hours = courseMatch.groupValues[3].toIntOrNull() ?: 0
                coursesList.add(ProgramCourse(code, nameCourse, hours))
            }
            list.add(ProgramTerm(name, coursesList))
        }
        return list
    }

    fun serializeFaq(faq: List<FAQItem>): String {
        return faq.joinToString(separator = ",", prefix = "[", postfix = "]") {
            "{\"q\":\"${it.q.replace("\"", "\\\"")}\",\"a\":\"${it.a.replace("\"", "\\\"")}\"}"
        }
    }

    fun deserializeFaq(json: String): List<FAQItem> {
        val list = mutableListOf<FAQItem>()
        val itemRegex = "\\{\"q\":\"([^\"]+)\",\"a\":\"([^\"]+)\"\\}".toRegex()
        val matches = itemRegex.findAll(json)
        for (match in matches) {
            val q = match.groupValues[1].replace("\\\"", "\"")
            val a = match.groupValues[2].replace("\\\"", "\"")
            list.add(FAQItem(q, a))
        }
        return list
    }

    fun toEntity(program: Program): ProgramEntity {
        return ProgramEntity(
            id = program.id,
            title = program.title,
            degree = program.degree,
            department = program.department,
            description = program.description,
            duration = program.duration,
            language = program.language,
            objectivesJson = listToJson(program.objectives),
            plosJson = listToJson(program.plos),
            curriculumJson = serializeCurriculum(program.curriculum),
            faqJson = serializeFaq(program.faq),
            admissionRequirements = program.admissionRequirements,
            graduationRequirements = program.graduationRequirements
        )
    }

    fun toModel(entity: ProgramEntity): Program {
        return Program(
            id = entity.id,
            title = entity.title,
            degree = entity.degree,
            department = entity.department,
            description = entity.description,
            duration = entity.duration,
            language = entity.language,
            objectives = jsonToList(entity.objectivesJson),
            plos = jsonToList(entity.plosJson),
            curriculum = deserializeCurriculum(entity.curriculumJson),
            faq = deserializeFaq(entity.faqJson),
            admissionRequirements = entity.admissionRequirements,
            graduationRequirements = entity.graduationRequirements
        )
    }
}
