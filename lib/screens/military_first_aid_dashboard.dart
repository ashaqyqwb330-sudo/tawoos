import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../data/military_first_aid_data.dart';
import '../data/military_first_aid_faq.dart';

class MilitaryFirstAidDashboard extends StatefulWidget {
  const MilitaryFirstAidDashboard({Key? key}) : super(key: key);

  @override
  State<MilitaryFirstAidDashboard> createState() => _MilitaryFirstAidDashboardState();
}

class _MilitaryFirstAidDashboardState extends State<MilitaryFirstAidDashboard> {
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';
  int _activeSemesterIndex = 0; // 0 for Semester 1, 1 for Semester 2

  // Track completed course codes. By default, some courses can be completed to showcase tracking!
  final Set<String> _completedCourseCodes = {
    'CQ-102',
    'ANAT-201',
    'MFA-401',
  };

  // Admission requirements interactive checklist variables
  double _enteredGpa = 72.0; // Default high school GPA percentage
  bool _isMedicalCleared = false; // Medical / physical health clearance
  bool _isActiveMilitary = false; // Military status and permission
  bool _hasScientificHighSchool = false; // High school background or prior health diploma

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Map<String, dynamic> getCourseCategoryInfo(String code) {
    final prefix = code.split('-')[0].toUpperCase();
    if (['ANAT', 'PHAR', 'EMS'].contains(prefix)) {
      return {
        'label': 'أساسية',
        'color': Colors.blueAccent,
        'bgColor': Colors.blueAccent.withOpacity(0.12),
      };
    } else if (['NURS', 'WMD', 'BTS'].contains(prefix)) {
      return {
        'label': 'مهارات',
        'color': Colors.tealAccent,
        'bgColor': Colors.tealAccent.withOpacity(0.12),
      };
    } else if (['CQ', 'QR', 'PUBH', 'ENG', 'ARAB', 'MEDT'].contains(prefix)) {
      return {
        'label': 'اختيارية',
        'color': Colors.amberAccent,
        'bgColor': Colors.amberAccent.withOpacity(0.12),
      };
    } else {
      return {
        'label': 'احترافية',
        'color': Colors.redAccent,
        'bgColor': Colors.redAccent.withOpacity(0.12),
      };
    }
  }

  // Calculate stats
  Map<String, double> getCategoryHoursPercentages() {
    final curriculum = MilitaryFirstAidData.detailedCurriculum;
    double asasiya = 0;
    double maharat = 0;
    double ikhtiyariya = 0;
    double ihtirafiya = 0;
    double total = 0;

    for (var year in curriculum) {
      for (var sem in year['semesters'] as List<dynamic>) {
        for (var course in sem['courses'] as List<dynamic>) {
          final hours = (course['hours'] as int).toDouble();
          total += hours;
          final info = getCourseCategoryInfo(course['code'] as String);
          final label = info['label'] as String;
          if (label == 'أساسية') {
            asasiya += hours;
          } else if (label == 'مهارات') {
            maharat += hours;
          } else if (label == 'اختيارية') {
            ikhtiyariya += hours;
          } else {
            ihtirafiya += hours;
          }
        }
      }
    }

    if (total == 0) return {};
    return {
      'أساسية': (asasiya / total) * 100,
      'مهارات': (maharat / total) * 100,
      'اختيارية': (ikhtiyariya / total) * 100,
      'احترافية': (ihtirafiya / total) * 100,
    };
  }

  int getCompletedHours() {
    final curriculum = MilitaryFirstAidData.detailedCurriculum;
    int completedHours = 0;
    for (var year in curriculum) {
      for (var sem in year['semesters'] as List<dynamic>) {
        for (var course in sem['courses'] as List<dynamic>) {
          if (_completedCourseCodes.contains(course['code'])) {
            completedHours += course['hours'] as int;
          }
        }
      }
    }
    return completedHours;
  }

  int getTotalHours() {
    final curriculum = MilitaryFirstAidData.detailedCurriculum;
    int totalHours = 0;
    for (var year in curriculum) {
      for (var sem in year['semesters'] as List<dynamic>) {
        for (var course in sem['courses'] as List<dynamic>) {
          totalHours += course['hours'] as int;
        }
      }
    }
    return totalHours;
  }

  String _normalizeArabic(String text) {
    String clean = text.toLowerCase();
    clean = clean.replaceAll(RegExp(r'[أإآ]'), 'ا');
    clean = clean.replaceAll(RegExp(r'[ى]'), 'ي');
    clean = clean.replaceAll(RegExp(r'[ة]'), 'ه');
    clean = clean.replaceAll(RegExp(r'[ًٌٍَُِّْ]'), ''); // Remove diacritics
    return clean.trim();
  }

  List<Map<String, dynamic>> _performSearch() {
    if (_searchQuery.trim().isEmpty) return [];
    
    final query = _normalizeArabic(_searchQuery);
    final List<Map<String, dynamic>> matches = [];

    final curriculum = MilitaryFirstAidData.detailedCurriculum;
    for (int yIndex = 0; yIndex < curriculum.length; yIndex++) {
      final yearData = curriculum[yIndex];
      final semesters = yearData['semesters'] as List<dynamic>;
      
      for (int sIndex = 0; sIndex < semesters.length; sIndex++) {
        final semData = semesters[sIndex];
        final semName = semData['semester'] as String;
        final courses = semData['courses'] as List<dynamic>;

        for (var course in courses) {
          final code = course['code'] as String;
          final name = course['name'] as String;
          final desc = course['desc'] as String;

          final normCode = _normalizeArabic(code);
          final normName = _normalizeArabic(name);
          final normDesc = _normalizeArabic(desc);

          if (normCode.contains(query) || normName.contains(query) || normDesc.contains(query)) {
            matches.add({
              'course': course,
              'semesterName': semName,
            });
          }
        }
      }
    }
    return matches;
  }

  Future<void> _exportStudyPlan() async {
    try {
      final directory = await getTemporaryDirectory();
      final filePath = '${directory.path}/military_first_aid_study_plan.txt';
      final file = File(filePath);

      StringBuffer buffer = StringBuffer();
      buffer.writeln('========================================================================');
      buffer.writeln('        الخطة الدراسية التفصيلية - دبلوم الإسعاف الحربي الشامل');
      buffer.writeln('          منهج الشهيد الدكتور زيد طاووس للعلوم الصحية العسكرية');
      buffer.writeln('          تاريخ التصدير والاستخراج: ${DateTime.now().toLocal()}');
      buffer.writeln('========================================================================\n');

      final curriculum = MilitaryFirstAidData.detailedCurriculum;
      int totalProgramHours = 0;
      int completedHoursSum = 0;

      for (var yearIndex = 0; yearIndex < curriculum.length; yearIndex++) {
        final yearData = curriculum[yearIndex];
        buffer.writeln('📍 المسار الأكاديمي: ${yearData['year']}');
        buffer.writeln('------------------------------------------------------------------------\n');

        final semesters = yearData['semesters'] as List<dynamic>;
        for (var semIndex = 0; semIndex < semesters.length; semIndex++) {
          final semData = semesters[semIndex];
          buffer.writeln('  ▫️ ${semData['semester']}');
          buffer.writeln('  ------------------------------------------');

          final courses = semData['courses'] as List<dynamic>;
          for (var course in courses) {
            final code = course['code'] as String;
            final name = course['name'] as String;
            final hours = course['hours'] as int;
            final desc = course['desc'] as String;
            final isDone = _completedCourseCodes.contains(code) ? '✅ [منجز]' : '⏳ [قيد المتابعة]';

            totalProgramHours += hours;
            if (_completedCourseCodes.contains(code)) {
              completedHoursSum += hours;
            }

            buffer.writeln('    - [$code] $name ($hours ساعة معتمدة) | الحالة: $isDone');
            buffer.writeln('      التوصيف: $desc');
            buffer.writeln();
          }
        }
      }

      buffer.writeln('========================================================================');
      buffer.writeln('📊 ملخص الكفاءة والإنجاز الأكاديمي:');
      buffer.writeln('- إجمالي الساعات المطلوبة للتخرج: $totalProgramHours ساعة معتمدة');
      buffer.writeln('- الساعات التي تم إنجازها: $completedHoursSum ساعة معتمدة');
      final double progressPercent = totalProgramHours > 0 ? (completedHoursSum / totalProgramHours) * 100 : 0.0;
      buffer.writeln('- نسبة التقدم الإجمالية للدبلوم: ${progressPercent.toStringAsFixed(1)}%');
      buffer.writeln('========================================================================');

      await file.writeAsString(buffer.toString());

      await Share.shareXFiles(
        [XFile(file.path)],
        text: 'مشاركة الخطة الدراسية التفاعلية لدبلوم الإسعاف الحربي الشامل المنجز',
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'خطأ أثناء تصدير الخطة الدراسية: $e',
            style: const TextStyle(fontFamily: 'Cairo'),
          ),
          backgroundColor: Colors.redAccent,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    const Color gold = Color(0xFFC5A44E);
    const Color slate = Color(0xFFE0E1DD);
    const Color navyDark = Color(0xFF0D1B2A);

    final totalHours = getTotalHours();
    final completedHours = getCompletedHours();
    final progressVal = totalHours > 0 ? completedHours / totalHours : 0.0;
    
    final searchMatches = _performSearch();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Top Header Actions
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              'الخطة الدراسية التفاعلية',
              style: TextStyle(
                color: gold,
                fontSize: 16,
                fontWeight: FontWeight.bold,
                fontFamily: 'Cairo',
              ),
            ),
            IconButton(
              onPressed: _exportStudyPlan,
              icon: const Icon(Icons.share_arrival_outlined, color: gold),
              tooltip: 'تصدير ومشاركة سجل التقدم',
            ).animate().scale(),
          ],
        ),
        const SizedBox(height: 8),

        // 1. Comprehensive Dynamic Progression stats card
        Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.02),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: gold.withOpacity(0.15)),
          ),
          child: Column(
            children: [
              Row(
                children: [
                  Stack(
                    alignment: Alignment.center,
                    children: [
                      SizedBox(
                        width: 60,
                        height: 60,
                        child: CircularProgressIndicator(
                          value: progressVal,
                          strokeWidth: 6,
                          backgroundColor: Colors.white.withOpacity(0.05),
                          valueColor: const AlwaysStoppedAnimation<Color>(gold),
                        ),
                      ),
                      Text(
                        '${(progressVal * 100).toStringAsFixed(0)}%',
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'تتبع كفاءة الدبلوم الأكاديمي',
                          style: TextStyle(
                            color: gold,
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                            fontFamily: 'Cairo',
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'تم إنجاز $completedHours ساعة من إجمالي $totalHours ساعة معتمدة.',
                          style: TextStyle(
                            color: slate.withOpacity(0.8),
                            fontSize: 12,
                            fontFamily: 'Cairo',
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'سجل المهارات المطلوب للتخرج: 40 - 50 تدخلاً معتمداً.',
                          style: TextStyle(
                            color: slate.withOpacity(0.5),
                            fontSize: 11,
                            fontStyle: FontStyle.italic,
                            fontFamily: 'Cairo',
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const Divider(color: Colors.white10),
              const SizedBox(height: 8),
              
              // Progression categories percentages
              _buildProgressionChart(gold, slate),
            ],
          ),
        ).animate().fadeIn(duration: 400.ms),

        const SizedBox(height: 16),

        // 2. Beautiful Search Field
        TextField(
          controller: _searchController,
          onChanged: (val) {
            setState(() {
              _searchQuery = val;
            });
          },
          style: const TextStyle(color: Colors.white, fontSize: 13, fontFamily: 'Cairo'),
          decoration: InputDecoration(
            hintText: 'ابحث عن كود المادة، أو اسمها، أو التوصيف...',
            hintStyle: TextStyle(color: slate.withOpacity(0.4), fontSize: 12, fontFamily: 'Cairo'),
            prefixIcon: const Icon(Icons.search, color: gold, size: 20),
            suffixIcon: _searchQuery.isNotEmpty
                ? IconButton(
                    icon: const Icon(Icons.clear, color: gold, size: 18),
                    onPressed: () {
                      _searchController.clear();
                      setState(() {
                        _searchQuery = '';
                      });
                    },
                  )
                : null,
            filled: true,
            fillColor: Colors.white.withOpacity(0.02),
            contentPadding: const EdgeInsets.symmetric(vertical: 8),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: BorderSide(color: Colors.white.withOpacity(0.08)),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(12),
              borderSide: const BorderSide(color: gold),
            ),
          ),
        ),

        const SizedBox(height: 12),

        // Results or normal curriculum
        if (_searchQuery.isNotEmpty)
          _buildSearchResults(searchMatches, gold, slate)
        else ...[
          // Semester selector tabs
          Row(
            children: [
              Expanded(
                child: InkWell(
                  onTap: () => setState(() => _activeSemesterIndex = 0),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 10),
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: _activeSemesterIndex == 0 ? gold.withOpacity(0.12) : Colors.transparent,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: _activeSemesterIndex == 0 ? gold : Colors.white10,
                        width: 1,
                      ),
                    ),
                    child: Text(
                      'الفصل الدراسي الأول',
                      style: TextStyle(
                        color: _activeSemesterIndex == 0 ? gold : Colors.white60,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: InkWell(
                  onTap: () => setState(() => _activeSemesterIndex = 1),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 10),
                    alignment: Alignment.center,
                    decoration: BoxDecoration(
                      color: _activeSemesterIndex == 1 ? gold.withOpacity(0.12) : Colors.transparent,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: _activeSemesterIndex == 1 ? gold : Colors.white10,
                        width: 1,
                      ),
                    ),
                    child: Text(
                      'الفصل الدراسي الثاني',
                      style: TextStyle(
                        color: _activeSemesterIndex == 1 ? gold : Colors.white60,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ).animate().fadeIn(),

          const SizedBox(height: 12),

          // Course lists for the active semester
          _buildSemesterList(_activeSemesterIndex, gold, slate),
        ],

        const SizedBox(height: 24),

        // Admission Requirements Verification Card Component
        _buildAdmissionRequirementsCard(gold, slate),

        const SizedBox(height: 24),

        // 3. Mirroring the beautiful advisor box
        _buildAdvisorBox(gold, slate),
      ],
    );
  }

  Widget _buildAdmissionRequirementsCard(Color gold, Color slate) {
    final bool isGpaValid = _enteredGpa >= 65.0;
    
    // Calculate satisfied requirements count
    int satisfiedCount = 0;
    if (isGpaValid) satisfiedCount++;
    if (_isMedicalCleared) satisfiedCount++;
    if (_isActiveMilitary) satisfiedCount++;
    if (_hasScientificHighSchool) satisfiedCount++;
    
    final bool isEligible = satisfiedCount == 4;
    final double eligibilityProgress = satisfiedCount / 4.0;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.015),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(
          color: isEligible ? Colors.greenAccent.withOpacity(0.2) : gold.withOpacity(0.15),
          width: isEligible ? 1.5 : 1.0,
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Title row
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: isEligible ? Colors.greenAccent.withOpacity(0.1) : gold.withOpacity(0.1),
                  shape: BoxShape.circle,
                ),
                child: Icon(
                  isEligible ? Icons.verified_user_outlined : Icons.assignment_ind_outlined,
                  color: isEligible ? Colors.greenAccent : gold,
                  size: 22,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'أداة تقييم ومطابقة شروط القبول',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        fontFamily: 'Cairo',
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      'تحقق تفاعلياً من المعايير الأكاديمية والطبية والعسكرية اللازمة للالتحاق',
                      style: TextStyle(
                        color: slate.withOpacity(0.5),
                        fontSize: 10,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          const Divider(color: Colors.white10, height: 1),
          const SizedBox(height: 16),

          // GPA Entry Slider
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'نسبة الثانوية العامة (الفرع العلمي):',
                    style: TextStyle(
                      color: Colors.white80,
                      fontSize: 12,
                      fontFamily: 'Cairo',
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: isGpaValid ? const Color(0xFFC5A44E).withOpacity(0.12) : Colors.redAccent.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Text(
                      '${_enteredGpa.toStringAsFixed(1)}%',
                      style: TextStyle(
                        color: isGpaValid ? const Color(0xFFC5A44E) : Colors.redAccent,
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              SliderTheme(
                data: SliderTheme.of(context).copyWith(
                  trackHeight: 4,
                  activeTrackColor: gold,
                  inactiveTrackColor: Colors.white10,
                  thumbColor: gold,
                  overlayColor: gold.withOpacity(0.15),
                  valueIndicatorTextStyle: const TextStyle(color: Colors.black, fontSize: 12),
                ),
                child: Slider(
                  value: _enteredGpa,
                  min: 50.0,
                  max: 100.0,
                  onChanged: (val) {
                    setState(() {
                      _enteredGpa = val;
                    });
                  },
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 6),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      'الحد الأدنى المطلوب للتسجيل: %65',
                      style: TextStyle(
                        color: slate.withOpacity(0.4),
                        fontSize: 10,
                        fontFamily: 'Cairo',
                      ),
                    ),
                    Row(
                      children: [
                        Icon(
                          isGpaValid ? Icons.check_circle_outline : Icons.error_outline,
                          color: isGpaValid ? Colors.greenAccent : Colors.amberAccent,
                          size: 13,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          isGpaValid ? 'مستوفٍ لشرط نسبة القبول' : 'النسبة أقل من المطلوب للتسجيل',
                          style: TextStyle(
                            color: isGpaValid ? Colors.greenAccent : Colors.amberAccent,
                            fontSize: 10.5,
                            fontWeight: FontWeight.bold,
                            fontFamily: 'Cairo',
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),

          // Interactive checklist items
          const Text(
            'قائمة المعايير والمسوغات اللازم توفيرها:',
            style: TextStyle(
              color: Colors.white70,
              fontSize: 11.5,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          const SizedBox(height: 10),

          // Checklist Item 1: Medical Check up
          _buildChecklistItem(
            title: 'اللائحة الطبية وفحوصات السلامة الميدانية الكاملة',
            subtitle: 'اجتياز الفحص السريري العسكري، فحص البصر الشامل، اللقاحات، وسلامة الحركة.',
            value: _isMedicalCleared,
            onChanged: (val) {
              setState(() {
                _isMedicalCleared = val ?? false;
              });
            },
            gold: gold,
            slate: slate,
          ),
          const SizedBox(height: 8),

          // Checklist Item 2: Military Status
          _buildChecklistItem(
            title: 'الصفة العسكرية والانتساب الفعلي المعتمد',
            subtitle: 'أن تكون من منتسبي ومنتسبي القوات المسلحة أو الوحدات الطبية مع موافقة مرجعية كتابية.',
            value: _isActiveMilitary,
            onChanged: (val) {
              setState(() {
                _isActiveMilitary = val ?? false;
              });
            },
            gold: gold,
            slate: slate,
          ),
          const SizedBox(height: 8),

          // Checklist Item 3: Scientific high school certificate
          _buildChecklistItem(
            title: 'شهادة الثانوية العامة - الفرع العلمي (أو دبلوم تمريض)',
            subtitle: 'حيازة أصل الشهادة بنسبة مئوية مستوفاة أو دبلوم مساعد طبي/تمريض سابق معادل.',
            value: _hasScientificHighSchool,
            onChanged: (val) {
              setState(() {
                _hasScientificHighSchool = val ?? false;
              });
            },
            gold: gold,
            slate: slate,
          ),
          const SizedBox(height: 18),

          // Real-time Eligibility feedback Banner
          AnimatedContainer(
            duration: 350.ms,
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: isEligible 
                  ? Colors.greenAccent.withOpacity(0.04) 
                  : Colors.amberAccent.withOpacity(0.02),
              borderRadius: BorderRadius.circular(12),
              border: Border.all(
                color: isEligible 
                    ? Colors.greenAccent.withOpacity(0.2) 
                    : Colors.amberAccent.withOpacity(0.1),
              ),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Icon(
                      isEligible ? Icons.verified : Icons.hourglass_empty,
                      color: isEligible ? Colors.greenAccent : Colors.amberAccent,
                      size: 18,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      isEligible ? 'مؤهل بالكامل للقبول النهائي ✅' : 'حالة الأهلية المبدئية الحالية:',
                      style: TextStyle(
                        color: isEligible ? Colors.greenAccent : Colors.amberAccent,
                        fontSize: 12.5,
                        fontWeight: FontWeight.bold,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Text(
                  isEligible
                      ? 'تهانينا! ملفك الأكاديمي والبدني والعسكري مستوفٍ بنسبة 100% لمعايير الالتحاق بدبلوم الإسعاف الحربي الشامل.'
                      : 'تم استيفاء ($satisfiedCount من 4) من متطلبات القبول والتسجيل حتى الآن. يرجى تلبية ما تبقى من شروط لتأكيد الأهلية.',
                  style: TextStyle(
                    color: slate.withOpacity(0.8),
                    fontSize: 11,
                    height: 1.5,
                    fontFamily: 'Cairo',
                  ),
                ),
                const SizedBox(height: 12),
                ClipRRect(
                  borderRadius: BorderRadius.circular(3),
                  child: LinearProgressIndicator(
                    value: eligibilityProgress,
                    minHeight: 6,
                    backgroundColor: Colors.white.withOpacity(0.05),
                    valueColor: AlwaysStoppedAnimation<Color>(isEligible ? Colors.greenAccent : Colors.amberAccent),
                  ),
                ),
              ],
            ),
          ).animate(target: isEligible ? 1.0 : 0.0).shimmer(duration: 1200.ms),
        ],
      ),
    );
  }

  Widget _buildChecklistItem({
    required String title,
    required String subtitle,
    required bool value,
    required ValueChanged<bool?> onChanged,
    required Color gold,
    required Color slate,
  }) {
    return InkWell(
      onTap: () => onChanged(!value),
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
        decoration: BoxDecoration(
          color: Colors.white.withOpacity(0.005),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            GestureDetector(
              onTap: () => onChanged(!value),
              behavior: HitTestBehavior.opaque,
              child: Container(
                width: 44,
                height: 44,
                alignment: Alignment.center,
                child: AnimatedContainer(
                  duration: 200.ms,
                  width: 22,
                  height: 22,
                  decoration: BoxDecoration(
                    color: value ? gold : Colors.transparent,
                    borderRadius: BorderRadius.circular(5),
                    border: Border.all(
                      color: value ? gold : Colors.white24,
                      width: 1.5,
                    ),
                  ),
                  child: value
                      ? const Icon(
                          Icons.check,
                          color: Colors.black,
                          size: 15,
                        )
                      : null,
                ),
              ),
            ),
            const SizedBox(width: 4),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const SizedBox(height: 6),
                  Text(
                    title,
                    style: TextStyle(
                      color: value ? gold : Colors.white90,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                      fontFamily: 'Cairo',
                    ),
                  ),
                  const SizedBox(height: 3),
                  Text(
                    subtitle,
                    style: TextStyle(
                      color: slate.withOpacity(0.45),
                      fontSize: 10,
                      height: 1.4,
                      fontFamily: 'Cairo',
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProgressionChart(Color gold, Color slate) {
    final percentages = getCategoryHoursPercentages();
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'توزيع نوع الدراسات والمجالات التدريبية:',
          style: TextStyle(
            color: Colors.white70,
            fontSize: 11,
            fontWeight: FontWeight.bold,
            fontFamily: 'Cairo',
          ),
        ),
        const SizedBox(height: 10),
        Row(
          children: [
            Expanded(child: _buildLegendItem('أساسية (طبية)', '${percentages['أساسية']?.toStringAsFixed(0)}%', Colors.blueAccent)),
            Expanded(child: _buildLegendItem('مهارات (ميدان)', '${percentages['مهارات']?.toStringAsFixed(0)}%', Colors.tealAccent)),
            Expanded(child: _buildLegendItem('اختيارية (ثقافة)', '${percentages['اختيارية']?.toStringAsFixed(0)}%', Colors.amberAccent)),
            Expanded(child: _buildLegendItem('احترافية (جراحة)', '${percentages['احترافية']?.toStringAsFixed(0)}%', Colors.redAccent)),
          ],
        ),
        const SizedBox(height: 12),
        // Combined horizontal progress segmented bar
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: Container(
            height: 10,
            width: double.infinity,
            color: Colors.white.withOpacity(0.05),
            child: Row(
              children: [
                Expanded(
                  flex: (percentages['أساسية'] ?? 0).ceil(),
                  child: Container(color: Colors.blueAccent),
                ),
                Expanded(
                  flex: (percentages['مهارات'] ?? 0).ceil(),
                  child: Container(color: Colors.tealAccent),
                ),
                Expanded(
                  flex: (percentages['اختيارية'] ?? 0).ceil(),
                  child: Container(color: Colors.amberAccent),
                ),
                Expanded(
                  flex: (percentages['احترافية'] ?? 0).ceil(),
                  child: Container(color: Colors.redAccent),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildLegendItem(String title, String percent, Color color) {
    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(width: 8, height: 8, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
            const SizedBox(width: 4),
            Text(
              percent,
              style: const TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 2),
        Text(
          title,
          style: TextStyle(color: Colors.white70.withOpacity(0.6), fontSize: 9, fontFamily: 'Cairo'),
          textAlign: TextAlign.center,
        ),
      ],
    );
  }

  Widget _buildSemesterList(int semesterIndex, Color gold, Color slate) {
    final yearData = MilitaryFirstAidData.detailedCurriculum[0];
    final semesterData = (yearData['semesters'] as List<dynamic>)[semesterIndex];
    final courses = semesterData['courses'] as List<dynamic>;

    return ListView.builder(
      physics: const NeverScrollableScrollPhysics(),
      shrinkWrap: true,
      itemCount: courses.length,
      itemBuilder: (context, index) {
        final course = courses[index];
        final code = course['code'] as String;
        final name = course['name'] as String;
        final hours = course['hours'] as int;
        final desc = course['desc'] as String;
        
        final isCompleted = _completedCourseCodes.contains(code);
        final catInfo = getCourseCategoryInfo(code);

        return Container(
          margin: const EdgeInsets.only(bottom: 10),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.02),
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: isCompleted ? gold.withOpacity(0.2) : Colors.white.withOpacity(0.04),
              width: isCompleted ? 1.2 : 1,
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  // Checkbox to track completion
                  InkWell(
                    onTap: () {
                      setState(() {
                        if (_completedCourseCodes.contains(code)) {
                          _completedCourseCodes.remove(code);
                        } else {
                          _completedCourseCodes.add(code);
                        }
                      });
                    },
                    child: Container(
                      width: 20,
                      height: 20,
                      decoration: BoxDecoration(
                        color: isCompleted ? gold : Colors.transparent,
                        borderRadius: BorderRadius.circular(4),
                        border: Border.all(color: isCompleted ? gold : slate.withOpacity(0.4)),
                      ),
                      child: isCompleted
                          ? const Icon(Icons.check, color: Colors.black, size: 14)
                          : null,
                    ),
                  ),
                  const SizedBox(width: 8),

                  // Course code pill
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: catInfo['bgColor'] as Color,
                      borderRadius: BorderRadius.circular(4),
                      border: Border.all(color: (catInfo['color'] as Color).withOpacity(0.3)),
                    ),
                    child: Text(
                      code,
                      style: TextStyle(
                        color: catInfo['color'] as Color,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),

                  Expanded(
                    child: Text(
                      name,
                      style: TextStyle(
                        color: isCompleted ? gold : Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ),

                  Text(
                    '$hours س',
                    style: TextStyle(
                      color: slate.withOpacity(0.5),
                      fontSize: 11,
                      fontFamily: 'Cairo',
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Padding(
                padding: const EdgeInsets.only(right: 28),
                child: Text(
                  desc,
                  style: TextStyle(
                    color: slate.withOpacity(0.7),
                    fontSize: 11.5,
                    height: 1.5,
                    fontFamily: 'Cairo',
                  ),
                ),
              ),
            ],
          ),
        ).animate().slideX(begin: 0.05, end: 0, duration: 250.ms, delay: (index * 40).ms);
      },
    );
  }

  Widget _buildSearchResults(List<Map<String, dynamic>> results, Color gold, Color slate) {
    if (results.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(20),
        alignment: Alignment.center,
        child: Column(
          children: [
            Icon(Icons.search_off, Colors.redAccent.withOpacity(0.8), size: 36),
            const SizedBox(height: 8),
            const Text(
              'لا توجد مواد مطابقة للاستعلام.',
              style: TextStyle(color: Colors.white70, fontSize: 12, fontFamily: 'Cairo'),
            ),
          ],
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Text(
            'نتائج البحث عن "${_searchQuery}": (${results.length} مواد مطابقة)',
            style: const TextStyle(color: Colors.white70, fontSize: 11, fontFamily: 'Cairo'),
          ),
        ),
        const SizedBox(height: 8),
        ListView.builder(
          physics: const NeverScrollableScrollPhysics(),
          shrinkWrap: true,
          itemCount: results.length,
          itemBuilder: (context, index) {
            final item = results[index];
            final course = item['course'];
            final code = course['code'] as String;
            final name = course['name'] as String;
            final desc = course['desc'] as String;
            final semName = item['semesterName'] as String;
            
            final isCompleted = _completedCourseCodes.contains(code);
            final catInfo = getCourseCategoryInfo(code);

            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.03),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: gold.withOpacity(0.1)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: catInfo['bgColor'] as Color,
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text(
                          code,
                          style: TextStyle(color: catInfo['color'] as Color, fontSize: 9, fontWeight: FontWeight.bold),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          name,
                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13, fontFamily: 'Cairo'),
                        ),
                      ),
                      Text(
                        semName.split(':')[0], // Compact name
                        style: TextStyle(color: gold.withOpacity(0.8), fontSize: 10, fontFamily: 'Cairo'),
                      ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    desc,
                    style: TextStyle(color: slate.withOpacity(0.7), fontSize: 11, fontFamily: 'Cairo'),
                  ),
                ],
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildAdvisorBox(Color gold, Color slate) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [gold.withOpacity(0.08), Colors.white.withOpacity(0.01)],
          begin: Alignment.topRight,
          end: Alignment.bottomLeft,
        ),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: gold.withOpacity(0.15)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.psychology_outlined, color: gold, size: 24),
              const SizedBox(width: 8),
              const Text(
                'الاستشاري الأكاديمي للدبلوم',
                style: TextStyle(
                  color: gold,
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                  fontFamily: 'Cairo',
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(
            'استناداً على إنجازك الأكاديمي المسجل، نوصيك بتوجيه التركيز إلى مقرر "إصابات الحروب والصدمات التكتيكية" لمساندة التدخلات التكميلية. كما يمكنك النقر فوق أي مادة دراسية بالأعلى لتحديث حالة تقدمك وإرشادك فورياً.',
            style: TextStyle(
              color: slate.withOpacity(0.8),
              fontSize: 11.5,
              height: 1.6,
              fontFamily: 'Cairo',
            ),
          ),
        ],
      ),
    );
  }
}
