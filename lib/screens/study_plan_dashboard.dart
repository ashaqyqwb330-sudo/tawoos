import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';
import '../data/bachelor_medicine_data.dart';
import '../data/bachelor_medicine_faq.dart';
import '../data/military_first_aid_data.dart';
import '../data/military_first_aid_faq.dart';

class StudyPlanDashboard extends StatefulWidget {
  final List<dynamic>? curriculumOverride;
  final List<Map<String, String>>? faqOverride;
  final String? programTitle;
  final String? programId;

  const StudyPlanDashboard({
    Key? key,
    this.curriculumOverride,
    this.faqOverride,
    this.programTitle,
    this.programId = 'bachelor',
  }) : super(key: key);

  @override
  State<StudyPlanDashboard> createState() => _StudyPlanDashboardState();
}

class _StudyPlanDashboardState extends State<StudyPlanDashboard> {
  int _selectedYearIndex = 0;
  String? _expandedSemesterName;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  final Set<String> _completedCourseCodes = {
    'ANAT-101',
    'PHYS-101',
    'BIOC-101',
    'PREV-101',
  };

  Map<String, dynamic> getCourseCategoryInfo(String code) {
    final prefix = code.split('-')[0].toUpperCase();
    if (['ANAT', 'PHYS', 'BIOC', 'HIST', 'PATH', 'PHAR', 'MICR', 'MED', 'SURG', 'PED', 'GYN', 'ENDO', 'UROL', 'OPHT'].contains(prefix)) {
      return {
        'label': 'أساسية',
        'color': Colors.blueAccent,
        'bgColor': Colors.blueAccent.withOpacity(0.12),
      };
    } else if (['FAID', 'NURS', 'STAT', 'CLIN', 'RAD', 'FORE', 'PREV'].contains(prefix)) {
      return {
        'label': 'مهارات',
        'color': Colors.tealAccent,
        'bgColor': Colors.tealAccent.withOpacity(0.12),
      };
    } else if (['TERM', 'ETHC', 'GENE', 'PSYC'].contains(prefix)) {
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

  Map<String, double> getCategoryHoursPercentages() {
    final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
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

  int getYearTotalHours(Map<String, dynamic> yearData) {
    final semesters = yearData['semesters'] as List<dynamic>;
    int sum = 0;
    for (var sem in semesters) {
      final courses = sem['courses'] as List<dynamic>;
      for (var c in courses) {
        sum += c['hours'] as int;
      }
    }
    return sum;
  }

  int getYearCompletedHours(Map<String, dynamic> yearData) {
    final semesters = yearData['semesters'] as List<dynamic>;
    int sum = 0;
    for (var sem in semesters) {
      final courses = sem['courses'] as List<dynamic>;
      for (var c in courses) {
        if (_completedCourseCodes.contains(c['code'] as String)) {
          sum += c['hours'] as int;
        }
      }
    }
    return sum;
  }

  // Icons and labels matching the study phases
  final List<Map<String, dynamic>> _yearMetadata = [
    {
      'title': 'السنة الأولى',
      'phase': 'العلوم الطبية الأساسية',
      'icon': Icons.science_outlined,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'السنة الثانية',
      'phase': 'العلوم الأساسية المدمجة',
      'icon': Icons.biotech_outlined,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'السنة الثالثة',
      'phase': 'السريريات التمهيدية',
      'icon': Icons.monitor_heart,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'السنة الرابعة',
      'phase': 'السريريات التخصصية 1',
      'icon': Icons.child_care_outlined,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'السنة الخامسة',
      'phase': 'السريريات التخصصية 2',
      'icon': Icons.personal_injury_outlined,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'السنة السادسة',
      'phase': 'طوارئ وجراحة الحروب',
      'icon': Icons.local_hospital_outlined,
      'color': Color(0xFFC5A44E),
    },
    {
      'title': 'سنة الامتياز',
      'phase': 'أطباء الميدان والامتياز',
      'icon': Icons.military_tech_outlined,
      'color': Color(0xFFC5A44E),
    },
  ];

  Map<String, dynamic> _getYearMeta(int index, List<dynamic> curriculum) {
    if (widget.programId == 'diploma_first_aid') {
      return {
        'title': 'الدبلوم الأكاديمي الشامل',
        'phase': 'الرعاية الجراحية الطارئة والإسعاف الحربي المتقدم',
        'icon': Icons.military_tech,
        'color': const Color(0xFFC5A44E),
      };
    }
    if (index < _yearMetadata.length) {
      return _yearMetadata[index];
    }
    return {
      'title': curriculum[index]['year'] as String,
      'phase': 'المسار التعليمي والتدريب السريري',
      'icon': Icons.school,
      'color': const Color(0xFFC5A44E),
    };
  }

  @override
  void initState() {
    super.initState();
    final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
    if (_selectedYearIndex >= curriculum.length) {
      _selectedYearIndex = 0;
    }
    // Default expand the first semester of the current year
    final selectedYearData = curriculum[_selectedYearIndex];
    final semesters = selectedYearData['semesters'] as List<dynamic>;
    if (semesters.isNotEmpty) {
      _expandedSemesterName = semesters[0]['semester'] as String;
    }
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  String _normalizeArabic(String text) {
    String clean = text.toLowerCase();
    clean = clean.replaceAll(RegExp(r'[أإآ]'), 'ا');
    clean = clean.replaceAll(RegExp(r'[ى]'), 'ي');
    clean = clean.replaceAll(RegExp(r'[ة]'), 'ه');
    clean = clean.replaceAll(RegExp(r'[ًٌٍَُِّْ]'), ''); // Remove diacritics
    return clean.trim();
  }

  List<Map<String, dynamic>> _getFilteredCoursesAndSemesters() {
    if (_searchQuery.trim().isEmpty) return [];
    final query = _normalizeArabic(_searchQuery);
    final List<Map<String, dynamic>> matches = [];

    final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
    for (int yIndex = 0; yIndex < curriculum.length; yIndex++) {
      final yearData = curriculum[yIndex];
      final yearName = yearData['year'] as String;
      final semesters = yearData['semesters'] as List<dynamic>;

      for (int sIndex = 0; sIndex < semesters.length; sIndex++) {
        final semData = semesters[sIndex];
        final semName = semData['semester'] as String;
        final courses = semData['courses'] as List<dynamic>;

        // Calculate actual credit hours dynamically
        final actualHours = courses.fold<int>(0, (sum, c) => sum + (c['hours'] as int));

        // Check if semester name matches
        final isSemMatch = _normalizeArabic(semName).contains(query);

        // Check if any course matches
        final List<Map<String, dynamic>> matchingCourses = [];
        for (var course in courses) {
          final cCode = _normalizeArabic(course['code'] as String);
          final cName = _normalizeArabic(course['name'] as String);
          final cDesc = _normalizeArabic(course['desc'] as String);

          if (cCode.contains(query) || cName.contains(query) || cDesc.contains(query)) {
            matchingCourses.add(Map<String, dynamic>.from(course));
          }
        }

        if (isSemMatch || matchingCourses.isNotEmpty) {
          matches.add({
            'yearIndex': yIndex,
            'yearName': yearName,
            'semesterName': semName,
            'semesterTotalHours': actualHours,
            'allCourses': courses,
            'matchingCourses': matchingCourses,
            'isSemesterMatchOnly': isSemMatch && matchingCourses.isEmpty,
          });
        }
      }
    }
    return matches;
  }

  Future<void> _exportStudyPlan() async {
    try {
      final directory = await getTemporaryDirectory();
      final programId = widget.programId ?? 'bachelor';
      final filePath = '${directory.path}/${programId}_study_plan.txt';
      final file = File(filePath);

      StringBuffer buffer = StringBuffer();
      buffer.writeln('========================================================================');
      buffer.writeln('        الخطة الدراسية التفصيلية - ${widget.programTitle ?? "بكالوريوس الطب والجراحة العسكري"}');
      buffer.writeln('          منهج الشهيد الدكتور زيد طاووس للعلوم الصحية العسكرية');
      buffer.writeln('           تاريخ التصدير والاستخراج: ${DateTime.now().toLocal()}');
      buffer.writeln('========================================================================\n');

      final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
      int totalProgramHours = 0;

      for (var yearIndex = 0; yearIndex < curriculum.length; yearIndex++) {
        final yearData = curriculum[yearIndex];
        final yearName = yearData['year'] as String;
        final semesters = yearData['semesters'] as List<dynamic>;

        buffer.writeln('\n------------------------------------------------------------------------');
        buffer.writeln(' 🌟 $yearName');
        buffer.writeln('------------------------------------------------------------------------');

        for (var semData in semesters) {
          final semName = semData['semester'] as String;
          final courses = semData['courses'] as List<dynamic>;
          final semHours = courses.fold<int>(0, (sum, c) => sum + (c['hours'] as int));
          totalProgramHours += semHours;

          buffer.writeln('\n  🔹 $semName (إجمالي الساعات: $semHours ساعة معتمدة):');
          buffer.writeln('     --------------------------------------------------------------------');

          for (var course in courses) {
            final cCode = course['code'] as String;
            final cName = course['name'] as String;
            final cHours = course['hours'] as int;
            final cDesc = course['desc'] as String;

            buffer.writeln('     [$cCode] $cName ($cHours ساعات)');
            buffer.writeln('     الوصف: $cDesc');
            buffer.writeln('     - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -');
          }
        }
      }

      buffer.writeln('\n========================================================================');
      buffer.writeln(' إجمالي الساعات المعتمدة للبرنامج بالكامل: $totalProgramHours ساعة معتمدة ومثبتة.');
      buffer.writeln(' متطلبات التخرج: النجاح بمعدل تراكمي لا يقل عن تقدير جيد جداً أو مقبول،');
      buffer.writeln(' وإكمال سنة الامتياز بنجاح كامل، واجتياز اختبار التقييم التكتيكي الموحد للصحة العسكرية.');
      buffer.writeln('========================================================================');

      await file.writeAsString(buffer.toString());

      await Share.shareXFiles(
        [XFile(file.path)],
        text: 'مشاركة الخطة الدراسية لبكالوريوس الطب والجراحة العسكري - نهج الشهيد زيد طاووس',
      );
    } catch (e) {
      if (!mounted) return;
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

  // Get active FAQs depending on the chosen academic year
  List<Map<String, String>> _getFilteredFaqs(int index) {
    final allFaq = widget.faqOverride ?? BachelorMedicineFaq.faq;
    List<Map<String, String>> result = [];

    if (widget.programId == 'diploma_first_aid') {
      return allFaq.sublist(0, allFaq.length > 15 ? 15 : allFaq.length);
    }

    if (index == 0 || index == 1) {
      // First / Second Year Questions
      result = allFaq.where((q) {
        final ques = q['question'] ?? '';
        return ques.contains('برنامج') || ques.contains('زيد') || ques.contains('شروط') || ques.contains('سنوات');
      }).toList();
    } else if (index == 2 || index == 3) {
      // Third / Fourth Year Questions
      result = allFaq.where((q) {
        final ques = q['question'] ?? '';
        return ques.contains('مقررات') || ques.contains('التدريب');
      }).toList();
    } else if (index == 4 || index == 5) {
      // Fifth / Sixth Year Questions
      result = allFaq.where((q) {
        final ques = q['question'] ?? '';
        return ques.contains('تقييم') || ques.contains('مخرج') || ques.contains('زيد');
      }).toList();
    } else {
      // Internship (year 6/7)
      result = allFaq.where((q) {
        final ques = q['question'] ?? '';
        return ques.contains('خدمة') || ques.contains('التدريب') || ques.contains('المنحة');
      }).toList();
    }

    if (result.isEmpty) {
      result = [allFaq.first];
    }
    return result;
  }

  @override
  Widget build(BuildContext context) {
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateColor = Color(0xFFE0E1DD);

    final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
    if (_selectedYearIndex >= curriculum.length) {
      _selectedYearIndex = 0;
    }
    final selectedYearData = curriculum[_selectedYearIndex];
    final semesters = selectedYearData['semesters'] as List<dynamic>;
    final activeFaqs = _getFilteredFaqs(_selectedYearIndex);
    final searchMatches = _getFilteredCoursesAndSemesters();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Search & Export Action Row
        Row(
          children: [
            Expanded(
              child: Container(
                height: 48,
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.04),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.white.withOpacity(0.06)),
                ),
                child: TextField(
                  controller: _searchController,
                  onChanged: (val) {
                    setState(() {
                      _searchQuery = val;
                    });
                  },
                  textDirection: TextDirection.rtl,
                  style: const TextStyle(color: Colors.white, fontSize: 13, fontFamily: 'Cairo'),
                  decoration: InputDecoration(
                    prefixIcon: const Icon(Icons.search, color: goldenBrass, size: 18),
                    hintText: 'البحث السريع في المواد والفصول...',
                    hintStyle: TextStyle(color: slateColor.withOpacity(0.4), fontSize: 12, fontFamily: 'Cairo'),
                    border: InputBorder.none,
                    contentPadding: const EdgeInsets.symmetric(vertical: 14),
                    suffixIcon: _searchQuery.isNotEmpty
                        ? IconButton(
                            icon: const Icon(Icons.clear, color: goldenBrass, size: 16),
                            onPressed: () {
                              setState(() {
                                _searchQuery = '';
                                _searchController.clear();
                              });
                            },
                          )
                        : null,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 10),
            // Export / Download Plan button
            Tooltip(
              message: 'تصدير الخطة الدراسية',
              child: InkWell(
                onTap: _exportStudyPlan,
                borderRadius: BorderRadius.circular(12),
                child: Container(
                  height: 48,
                  width: 48,
                  decoration: BoxDecoration(
                    color: goldenBrass.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(color: goldenBrass.withOpacity(0.3)),
                  ),
                  child: const Icon(Icons.download_for_offline_outlined, color: goldenBrass, size: 22),
                ),
              ),
            ),
          ],
        ),

        const SizedBox(height: 18),

        if (_searchQuery.trim().isNotEmpty)
          _buildSearchResults(searchMatches, goldenBrass, slateColor)
        else ...[
          // Recharts Progression Chart
          _buildProgressionChart(curriculum, goldenBrass, slateColor),

          // Interactive horizontal timeline tracking academic progression
          _buildTimeline(curriculum, goldenBrass, slateColor),

          const SizedBox(height: 18),

          // Title and phase summary
          _buildActivePhaseSummary(selectedYearData, goldenBrass, slateColor),

          const SizedBox(height: 16),

          // Accordion Semester List
          ...semesters.map((sem) {
            final semName = sem['semester'] as String;
            final courses = sem['courses'] as List<dynamic>;
            // Calculate total hours automatically as requested
            final semHours = courses.fold<int>(0, (sum, c) => sum + (c['hours'] as int));
            final isExpanded = _expandedSemesterName == semName;

            return Container(
              margin: const EdgeInsets.only(bottom: 12),
              decoration: BoxDecoration(
                color: isExpanded ? Colors.white.withOpacity(0.04) : Colors.white.withOpacity(0.02),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isExpanded ? goldenBrass.withOpacity(0.4) : Colors.white.withOpacity(0.06),
                  width: isExpanded ? 1.5 : 1,
                ),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: Column(
                  children: [
                    // Clickable Header Card
                    InkWell(
                      onTap: () {
                        setState(() {
                          if (isExpanded) {
                            _expandedSemesterName = null;
                          } else {
                            _expandedSemesterName = semName;
                          }
                        });
                      },
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
                        child: Row(
                          children: [
                            // Graphic Status Dot / Icon
                            Container(
                              padding: const EdgeInsets.all(8),
                              decoration: BoxDecoration(
                                color: isExpanded ? goldenBrass.withOpacity(0.15) : Colors.white.withOpacity(0.05),
                                shape: BoxShape.circle,
                              ),
                              child: Icon(
                                isExpanded ? Icons.folder_open_outlined : Icons.folder_outlined,
                                color: isExpanded ? goldenBrass : slateColor.withOpacity(0.6),
                                size: 18,
                              ),
                            ),
                            const SizedBox(width: 12),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    semName,
                                    style: TextStyle(
                                      color: isExpanded ? goldenBrass : Colors.white,
                                      fontSize: 13,
                                      fontWeight: FontWeight.bold,
                                      fontFamily: 'Cairo',
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    'محتويات وتكامل المرحلة: ${courses.length} مقررات طليعية',
                                    style: TextStyle(
                                      color: slateColor.withOpacity(0.5),
                                      fontSize: 11,
                                      fontFamily: 'Cairo',
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            // Hour indicator
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: isExpanded ? goldenBrass.withOpacity(0.18) : Colors.white.withOpacity(0.05),
                                borderRadius: BorderRadius.circular(12),
                                border: Border.all(
                                  color: isExpanded ? goldenBrass.withOpacity(0.3) : Colors.white.withOpacity(0.08),
                                ),
                              ),
                              child: Text(
                                '$semHours ساعة',
                                style: TextStyle(
                                  color: isExpanded ? goldenBrass : slateColor.withOpacity(0.8),
                                  fontSize: 10.5,
                                  fontWeight: FontWeight.bold,
                                  fontFamily: 'Cairo',
                                ),
                              ),
                            ),
                            const SizedBox(width: 8),
                            Icon(
                              isExpanded ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
                              color: isExpanded ? goldenBrass : slateColor.withOpacity(0.4),
                              size: 18,
                            ),
                          ],
                        ),
                      ),
                    ),

                    // Semester Courses List View
                    if (isExpanded)
                      Container(
                        color: Colors.black.withOpacity(0.15),
                        padding: const EdgeInsets.all(12.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Divider(color: Colors.white10, height: 1),
                            const SizedBox(height: 10),
                            ...courses.map((course) {
                              final cCode = course['code'] as String;
                              final cName = course['name'] as String;
                              final cHours = course['hours'] as int;
                              final cDesc = course['desc'] as String;
                              final isCompleted = _completedCourseCodes.contains(cCode);
                              final catInfo = getCourseCategoryInfo(cCode);

                              return Container(
                                margin: const EdgeInsets.only(bottom: 10),
                                padding: const EdgeInsets.all(12),
                                decoration: BoxDecoration(
                                  color: isCompleted
                                      ? Colors.green.withOpacity(0.04)
                                      : Colors.white.withOpacity(0.01),
                                  borderRadius: BorderRadius.circular(12),
                                  border: Border.all(
                                    color: isCompleted
                                        ? Colors.green.withOpacity(0.3)
                                        : Colors.white.withOpacity(0.03),
                                  ),
                                ),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        // Checkbox with 36x36 touch target footprint
                                        InkWell(
                                          onTap: () {
                                            setState(() {
                                              if (isCompleted) {
                                                _completedCourseCodes.remove(cCode);
                                              } else {
                                                _completedCourseCodes.add(cCode);
                                              }
                                            });
                                          },
                                          borderRadius: BorderRadius.circular(20),
                                          child: Container(
                                            height: 36,
                                            width: 36,
                                            alignment: Alignment.center,
                                            child: Container(
                                              height: 18,
                                              width: 18,
                                              decoration: BoxDecoration(
                                                color: isCompleted ? Colors.greenAccent : Colors.transparent,
                                                borderRadius: BorderRadius.circular(4),
                                                border: Border.all(
                                                  color: isCompleted ? Colors.greenAccent : goldenBrass.withOpacity(0.6),
                                                  width: 1.5,
                                                ),
                                              ),
                                              child: isCompleted
                                                  ? const Icon(Icons.check, size: 12, color: Colors.black)
                                                  : null,
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 4),
                                        // Code
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 7, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: goldenBrass.withOpacity(0.1),
                                            borderRadius: BorderRadius.circular(4),
                                            border: Border.all(color: goldenBrass.withOpacity(0.2)),
                                          ),
                                          child: Text(
                                            cCode,
                                            style: const TextStyle(
                                              color: goldenBrass,
                                              fontSize: 9,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 6),
                                        // Category custom tag label
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: catInfo['bgColor'] as Color,
                                            borderRadius: BorderRadius.circular(4),
                                            border: Border.all(color: (catInfo['color'] as Color).withOpacity(0.2)),
                                          ),
                                          child: Text(
                                            catInfo['label'] as String,
                                            style: TextStyle(
                                              color: catInfo['color'] as Color,
                                              fontSize: 9,
                                              fontWeight: FontWeight.bold,
                                              fontFamily: 'Cairo',
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 10),
                                        // Name
                                        Expanded(
                                          child: Text(
                                            cName,
                                            style: TextStyle(
                                              color: isCompleted ? Colors.white70 : Colors.white,
                                              fontSize: 12.5,
                                              fontWeight: FontWeight.bold,
                                              fontFamily: 'Cairo',
                                              decoration: isCompleted ? TextDecoration.lineThrough : null,
                                              decorationColor: Colors.greenAccent,
                                            ),
                                          ),
                                        ),
                                        // Hours
                                        Text(
                                          '$cHours ساعة',
                                          style: TextStyle(
                                            color: isCompleted ? Colors.greenAccent : goldenBrass.withOpacity(0.8),
                                            fontSize: 11,
                                            fontWeight: FontWeight.bold,
                                            fontFamily: 'Cairo',
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 6),
                                    Padding(
                                      padding: const EdgeInsets.only(right: 40.0),
                                      child: Text(
                                        cDesc,
                                        style: TextStyle(
                                          color: isCompleted ? slateColor.withOpacity(0.4) : slateColor.withOpacity(0.7),
                                          fontSize: 11.5,
                                          height: 1.5,
                                          fontFamily: 'Cairo',
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              ).animate().fadeIn(duration: 200.ms).slideY(begin: 0.05, end: 0, duration: 200.ms);
                            }).toList(),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
            ).animate().fadeIn(duration: 300.ms);
          }).toList(),

          const SizedBox(height: 12),

          // FAQs / Counselor Advisory Box referencing BachelorMedicineFaq
          _buildAdvisorBox(activeFaqs, goldenBrass, slateColor),
        ],
      ],
    );
  }

  // Top progression timeline
  Widget _buildTimeline(
    List<Map<String, dynamic>> curriculum,
    Color gold,
    Color slate,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.02),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.04)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.only(right: 8.0, bottom: 10),
            child: Row(
              children: [
                Icon(Icons.timeline, color: gold, size: 16),
                const SizedBox(width: 6),
                const Text(
                  'المسار الزمني لتأهيل أطباء الميدان والأزمات',
                  style: TextStyle(
                    color: Colors.white70,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
              ],
            ),
          ),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: List.generate(curriculum.length, (index) {
                final yearData = curriculum[index];
                final yearTitle = yearData['year'] as String;
                final meta = _getYearMeta(index, curriculum);
                final isSelected = _selectedYearIndex == index;
                final isCompleted = _selectedYearIndex > index;

                return GestureDetector(
                  onTap: () {
                    setState(() {
                      _selectedYearIndex = index;
                      // Default first semester selection
                      final semesters = yearData['semesters'] as List<dynamic>;
                      if (semesters.isNotEmpty) {
                        _expandedSemesterName = semesters[0]['semester'] as String;
                      } else {
                        _expandedSemesterName = null;
                      }
                    });
                  },
                  child: Row(
                    children: [
                      // Node Card
                      AnimatedContainer(
                        duration: const Duration(milliseconds: 300),
                        width: 90,
                        padding: const EdgeInsets.all(8),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? gold.withOpacity(0.12)
                              : isCompleted
                                  ? Colors.green.withOpacity(0.05)
                                  : Colors.transparent,
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: isSelected
                                ? gold
                                : isCompleted
                                    ? Colors.green.withOpacity(0.4)
                                    : Colors.white.withOpacity(0.06),
                            width: isSelected ? 1.5 : 1,
                          ),
                          boxShadow: isSelected
                              ? [
                                  BoxShadow(
                                    color: gold.withOpacity(0.15),
                                    blurRadius: 8,
                                    offset: const Offset(0, 2),
                                  )
                                ]
                              : null,
                        ),
                        child: Column(
                          children: [
                            // Phase Icon Background
                            Container(
                              padding: const EdgeInsets.all(6),
                              decoration: BoxDecoration(
                                color: isSelected
                                    ? gold.withOpacity(0.2)
                                    : isCompleted
                                        ? Colors.green.withOpacity(0.15)
                                        : Colors.white.withOpacity(0.04),
                                shape: BoxShape.circle,
                              ),
                              child: Icon(
                                isCompleted ? Icons.check : meta['icon'] as IconData,
                                color: isSelected
                                    ? gold
                                    : isCompleted
                                        ? Colors.greenAccent
                                        : slate.withOpacity(0.5),
                                size: 16,
                              ),
                            ),
                            const SizedBox(height: 6),
                            // Year title
                            Text(
                              yearTitle,
                              style: TextStyle(
                                color: isSelected
                                    ? gold
                                    : isCompleted
                                        ? Colors.greenAccent
                                        : Colors.white70,
                                fontSize: 11,
                                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                                fontFamily: 'Cairo',
                              ),
                            ),
                            const SizedBox(height: 6),
                            _buildYearProgressBar(yearData, gold),
                          ],
                        ),
                      ),
                      // Connecting dotted line
                      if (index < curriculum.length - 1)
                        Container(
                          width: 16,
                          height: 2,
                          color: isCompleted ? Colors.green.withOpacity(0.5) : Colors.white10,
                        ),
                    ],
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }

  // Active year details
  Widget _buildActivePhaseSummary(Map<String, dynamic> yearData, Color gold, Color slate) {
    final yearTitle = yearData['year'] as String;
    final curriculum = widget.curriculumOverride ?? BachelorMedicineData.detailedCurriculum;
    final meta = _getYearMeta(_selectedYearIndex, curriculum);
    final totalYearHours = (yearData['semesters'] as List<dynamic>).fold<int>(0, (prev, element) => prev + (element['totalHours'] as int));

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.02),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.05)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: gold.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  meta['phase'] as String,
                  style: TextStyle(
                    color: gold,
                    fontWeight: FontWeight.bold,
                    fontSize: 10.5,
                    fontFamily: 'Cairo',
                  ),
                ),
              ),
              const Spacer(),
              Text(
                'مجموع الساعات للمرحلة: $totalYearHours ساعة',
                style: TextStyle(
                  color: slate.withOpacity(0.6),
                  fontSize: 10.5,
                  fontFamily: 'Cairo',
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            'محاور $yearTitle:',
            style: const TextStyle(
              color: Colors.white,
              fontSize: 13,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          const SizedBox(height: 4),
          Text(
            _getPhaseDescription(_selectedYearIndex),
            style: TextStyle(
              color: slate.withOpacity(0.8),
              fontSize: 11.5,
              height: 1.5,
              fontFamily: 'Cairo',
            ),
          ),
        ],
      ),
    ).animate().fadeIn(duration: 400.ms).slideX(begin: 0.05, end: 0, duration: 400.ms);
  }

  String _getPhaseDescription(int index) {
    switch (index) {
      case 0:
        return 'تركز السنة الأولى على ترسيخ البنية العلمية والمعرفية المتكاملة للجسم البشري السليم من خلال التشريح الوظيفي والمصطلحات الطبية المتخصصة، بالتوازي مع كسب مهارات الإسعاف الأولي الميداني ومبادئ الصحة الوقائية العقائدية.';
      case 1:
        return 'استكمال دراسة الآليات الفطرية الممرضة وعلم الأدوية العام وطلب الطوارئ التكتيكي الفوري (TCCC) لإعداد الطالب ذهنياً وعملياً للتعامل مع المضاعفات والعمل ضمن فريق عسكري منقذ.';
      case 2:
        return 'المرحلة التمهيدية السريرية الحقيقية؛ حيث يبدأ تدريب الفحص السريري الدقيق وأخذ التاريخ المرضي لأجهزة الباطنة والجراحة، ورصد الأوبئة وإعداد بروتوكولات حاسمة وتدبير الأزمات.';
      case 3:
        return 'تمتد السنة الرابعة لأمراض وجراحات الأطفال والنساء والتوليد، مع تسليح عسكري للأشعة السريرية والتشخيص بالأمواج فوق الصوتية لسرعة الرصد في مستشفيات وعيادات الميدان.';
      case 4:
        return 'الغوص والتأهيل السريري في السريريات والعمليات الكبرى للجراحة العامة والباطنة التخصصية، ومكافحة الأسلحة السامة والحروق ومضاعفاتها المعقدة تكتيكياً وميدانياً.';
      case 5:
        return 'سنة الحسم الأكاديمي والعملياتي الشامل: تركز مباشرة على طب وجراحة الطوارئ المتكاملة، جراحة الفرز والطلقات النارية وإجهادات القصف وطب النفس التكتيكي.';
      default:
        return 'مرحلة الامتياز العسكري والعمل السريري المباشر في المشافي العسكرية والوطنية وجبهات الواجب، لترجمة كافة العلوم وصقل القيادة الطبية الحكيمة والإنقاذ التكتيكي الفعلي.';
    }
  }

  // Beautiful contextual Advisor box pulling FAQ from BachelorMedicineFaq
  Widget _buildAdvisorBox(List<Map<String, String>> faqs, Color gold, Color slate) {
    return Container(
      margin: const EdgeInsets.only(top: 8, bottom: 20),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: gold.withOpacity(0.04),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: gold.withOpacity(0.2), width: 1.2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: gold.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.school, color: gold, size: 18),
              ),
              const SizedBox(width: 10),
              const Text(
                'إضاءة أكاديمية من مستشار البرنامج',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 13,
                  fontWeight: FontWeight.bold,
                  fontFamily: 'Cairo',
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ...faqs.take(2).map((faq) => Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Icon(Icons.help_outline_rounded, color: gold, size: 14),
                        const SizedBox(width: 6),
                        Expanded(
                          child: Text(
                            faq['question'] ?? 'سؤال أكاديمي',
                            style: const TextStyle(
                              color: Colors.white,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              fontFamily: 'Cairo',
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Padding(
                      padding: const EdgeInsets.only(right: 20.0),
                      child: Text(
                        faq['answer'] ?? 'الإجابة التخصيصية لمسار الكلية',
                        style: TextStyle(
                          color: slate.withOpacity(0.8),
                          fontSize: 11.5,
                          height: 1.5,
                          fontFamily: 'Cairo',
                        ),
                      ),
                    ),
                    const Divider(color: Colors.white10),
                  ],
                ),
              )),
        ],
      ),
    );
  }

  Widget _buildSearchResults(List<Map<String, dynamic>> results, Color gold, Color slate) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.search, color: gold, size: 18),
            const SizedBox(width: 8),
            Text(
              'نتائج البحث لـ "${_searchQuery}":',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 14,
                fontWeight: FontWeight.bold,
                fontFamily: 'Cairo',
              ),
            ),
            const Spacer(),
            TextButton.icon(
              onPressed: () {
                setState(() {
                  _searchQuery = '';
                  _searchController.clear();
                });
              },
              icon: Icon(Icons.clear, color: gold, size: 16),
              label: Text(
                'إلغاء البحث',
                style: TextStyle(color: gold, fontSize: 12, fontFamily: 'Cairo'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (results.isEmpty)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 40, horizontal: 20),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.01),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: Colors.white.withOpacity(0.03)),
            ),
            child: Column(
              children: [
                Icon(Icons.search_off_outlined, color: slate.withOpacity(0.3), size: 48),
                const SizedBox(height: 16),
                Text(
                  'لم نجد أي مقررات أو فصول تطابق هذا البحث',
                  style: TextStyle(color: slate.withOpacity(0.8), fontSize: 13, fontFamily: 'Cairo'),
                ),
                const SizedBox(height: 4),
                Text(
                  'تأكد من كتابة الكلمات بشكل صحيح (مثال: تشريح، جراحة، عسكري)',
                  style: TextStyle(color: slate.withOpacity(0.4), fontSize: 11, fontFamily: 'Cairo'),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          )
        else
          ...results.map((res) {
            final int yearIndex = res['yearIndex'] as int;
            final String yearName = res['yearName'] as String;
            final String semesterName = res['semesterName'] as String;
            final int semesterTotalHours = res['semesterTotalHours'] as int;
            final List<dynamic> matchingCourses = res['matchingCourses'] as List<dynamic>;

            return Container(
              margin: const EdgeInsets.only(bottom: 14),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.02),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: gold.withOpacity(0.15)),
              ),
              child: ClipRRect(
                borderRadius: BorderRadius.circular(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Heading detailing Year + Semester
                    Container(
                      color: gold.withOpacity(0.06),
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      child: Row(
                        children: [
                          Icon(Icons.auto_stories, color: gold, size: 16),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  yearName,
                                  style: TextStyle(
                                    color: gold,
                                    fontSize: 11,
                                    fontWeight: FontWeight.bold,
                                    fontFamily: 'Cairo',
                                  ),
                                ),
                                Text(
                                  semesterName,
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontSize: 12.5,
                                    fontWeight: FontWeight.bold,
                                    fontFamily: 'Cairo',
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Text(
                            '$semesterTotalHours ساعة معتمدة',
                            style: TextStyle(
                              color: slate.withOpacity(0.7),
                              fontSize: 10.5,
                              fontFamily: 'Cairo',
                            ),
                          ),
                          const SizedBox(width: 10),
                          // Nav Button
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: gold,
                              foregroundColor: Colors.black,
                              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                              minimumSize: Size.zero,
                              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(8),
                              ),
                            ),
                            onPressed: () {
                              setState(() {
                                _selectedYearIndex = yearIndex;
                                _expandedSemesterName = semesterName;
                                _searchQuery = '';
                                _searchController.clear();
                              });
                            },
                            child: const Text(
                              'انتقال',
                              style: TextStyle(
                                fontSize: 10.5,
                                fontWeight: FontWeight.bold,
                                fontFamily: 'Cairo',
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    const Divider(color: Colors.white10, height: 1),
                    // Courses block
                    Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          if (matchingCourses.isNotEmpty) ...[
                            Text(
                              'المقررات المطابقة:',
                              style: TextStyle(
                                color: gold.withOpacity(0.8),
                                fontSize: 11.5,
                                fontWeight: FontWeight.bold,
                                fontFamily: 'Cairo',
                              ),
                            ),
                            const SizedBox(height: 8),
                            ...matchingCourses.map((course) {
                              final cCode = course['code'] as String;
                              final cName = course['name'] as String;
                              final cHours = course['hours'] as int;
                              final cDesc = course['desc'] as String;
                              final isCompleted = _completedCourseCodes.contains(cCode);
                              final catInfo = getCourseCategoryInfo(cCode);

                              return Container(
                                margin: const EdgeInsets.only(bottom: 8),
                                padding: const EdgeInsets.all(10),
                                decoration: BoxDecoration(
                                  color: isCompleted
                                      ? Colors.green.withOpacity(0.04)
                                      : Colors.white.withOpacity(0.01),
                                  borderRadius: BorderRadius.circular(8),
                                  border: Border.all(
                                    color: isCompleted
                                        ? Colors.green.withOpacity(0.3)
                                        : Colors.white.withOpacity(0.03),
                                  ),
                                ),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        // Completed checkbox
                                        InkWell(
                                          onTap: () {
                                            setState(() {
                                              if (isCompleted) {
                                                _completedCourseCodes.remove(cCode);
                                              } else {
                                                _completedCourseCodes.add(cCode);
                                              }
                                            });
                                          },
                                          borderRadius: BorderRadius.circular(20),
                                          child: Container(
                                            height: 36,
                                            width: 36,
                                            alignment: Alignment.center,
                                            child: Container(
                                              height: 18,
                                              width: 18,
                                              decoration: BoxDecoration(
                                                color: isCompleted ? Colors.greenAccent : Colors.transparent,
                                                borderRadius: BorderRadius.circular(4),
                                                border: Border.all(
                                                  color: isCompleted ? Colors.greenAccent : gold.withOpacity(0.6),
                                                  width: 1.5,
                                                ),
                                              ),
                                              child: isCompleted
                                                  ? const Icon(Icons.check, size: 12, color: Colors.black)
                                                  : null,
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 4),
                                        // Code
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: gold.withOpacity(0.1),
                                            borderRadius: BorderRadius.circular(4),
                                          ),
                                          child: Text(
                                            cCode,
                                            style: TextStyle(
                                              color: gold,
                                              fontSize: 9,
                                              fontWeight: FontWeight.bold,
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 6),
                                        // Tag Label
                                        Container(
                                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                                          decoration: BoxDecoration(
                                            color: catInfo['bgColor'] as Color,
                                            borderRadius: BorderRadius.circular(4),
                                            border: Border.all(color: (catInfo['color'] as Color).withOpacity(0.2)),
                                          ),
                                          child: Text(
                                            catInfo['label'] as String,
                                            style: TextStyle(
                                              color: catInfo['color'] as Color,
                                              fontSize: 9,
                                              fontWeight: FontWeight.bold,
                                              fontFamily: 'Cairo',
                                            ),
                                          ),
                                        ),
                                        const SizedBox(width: 8),
                                        // Name
                                        Expanded(
                                          child: Text(
                                            cName,
                                            style: TextStyle(
                                              color: isCompleted ? Colors.white70 : Colors.white,
                                              fontSize: 12,
                                              fontWeight: FontWeight.bold,
                                              fontFamily: 'Cairo',
                                              decoration: isCompleted ? TextDecoration.lineThrough : null,
                                              decorationColor: Colors.greenAccent,
                                            ),
                                          ),
                                        ),
                                        Text(
                                          '$cHours ساعة',
                                          style: TextStyle(
                                            color: isCompleted ? Colors.greenAccent : gold.withOpacity(0.8),
                                            fontSize: 10.5,
                                            fontWeight: FontWeight.bold,
                                            fontFamily: 'Cairo',
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 6),
                                    Padding(
                                      padding: const EdgeInsets.only(right: 40.0),
                                      child: Text(
                                        cDesc,
                                        style: TextStyle(
                                          color: isCompleted ? slate.withOpacity(0.4) : slate.withOpacity(0.7),
                                          fontSize: 11.5,
                                          height: 1.4,
                                          fontFamily: 'Cairo',
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              );
                            }).toList(),
                          ] else ...[
                            // Only semester matched, show summary list of courses
                            Text(
                              'كافة مقررات الفصل الدراسي:',
                              style: TextStyle(
                                color: gold.withOpacity(0.8),
                                fontSize: 11.5,
                                fontWeight: FontWeight.bold,
                                fontFamily: 'Cairo',
                              ),
                            ),
                            const SizedBox(height: 6),
                            Wrap(
                              spacing: 6,
                              runSpacing: 6,
                              children: (res['allCourses'] as List<dynamic>).map((c) {
                                return Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: Colors.white.withOpacity(0.03),
                                    borderRadius: BorderRadius.circular(6),
                                  ),
                                  child: Text(
                                    c['name'] as String,
                                    style: TextStyle(
                                      color: slate.withOpacity(0.9),
                                      fontSize: 11,
                                      fontFamily: 'Cairo',
                                    ),
                                  ),
                                );
                              }).toList(),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            );
          }),
      ],
  Widget _buildProgressionChart(List<Map<String, dynamic>> curriculum, Color gold, Color slate) {
    int totalOverallHours = 0;
    int completedOverallHours = 0;

    for (var year in curriculum) {
      for (var sem in year['semesters'] as List<dynamic>) {
        for (var course in sem['courses'] as List<dynamic>) {
          final h = course['hours'] as int;
          totalOverallHours += h;
          if (_completedCourseCodes.contains(course['code'] as String)) {
            completedOverallHours += h;
          }
        }
      }
    }

    final double overallPercent = totalOverallHours > 0 
        ? (completedOverallHours / totalOverallHours) * 100 
        : 0.0;

    List<Map<String, dynamic>> chartPoints = [];
    int runningTarget = 0;
    int runningCompleted = 0;

    for (var i = 0; i < curriculum.length; i++) {
      final yearData = curriculum[i];
      final label = _getYearMeta(i, curriculum)['title'] as String;
      
      int yearTarget = 0;
      int yearCompleted = 0;
      for (var sem in yearData['semesters'] as List<dynamic>) {
        for (var course in sem['courses'] as List<dynamic>) {
          final h = course['hours'] as int;
          yearTarget += h;
          if (_completedCourseCodes.contains(course['code'] as String)) {
            yearCompleted += h;
          }
        }
      }

      runningTarget += yearTarget;
      runningCompleted += yearCompleted;

      chartPoints.add({
        'label': label,
        'cumulativeTarget': runningTarget,
        'cumulativeCompleted': runningCompleted,
      });
    }

    // Category distribution percentages
    final catPercentages = getCategoryHoursPercentages();

    return Container(
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.only(bottom: 18),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.02),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.04)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: gold.withOpacity(0.12),
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.bar_chart_outlined, color: gold, size: 20),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'مخطط تراكم الساعات المعتمدة الأكاديمية (Recharts Engine)',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 13,
                        fontWeight: FontWeight.bold,
                        fontFamily: 'Cairo',
                      ),
                    ),
                    Text(
                      'تمثيل بياني لتراكم الساعات المخططة مقابل المنجزة عبر سنوات البرنامج',
                      style: TextStyle(
                        color: slate.withOpacity(0.5),
                        fontSize: 10.5,
                        fontFamily: 'Cairo',
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 18),

          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'التقدم الإجمالي للبكالوريوس:',
                      style: TextStyle(color: slate.withOpacity(0.8), fontSize: 11, fontFamily: 'Cairo'),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.baseline,
                      textBaseline: TextBaseline.alphabetic,
                      children: [
                        Text(
                          '$completedOverallHours',
                          style: const TextStyle(
                            color: Colors.greenAccent,
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                            fontFamily: 'Cairo',
                          ),
                        ),
                        Text(
                          ' / $totalOverallHours ساعة معتمدة',
                          style: TextStyle(
                            color: slate.withOpacity(0.5),
                            fontSize: 12,
                            fontFamily: 'Cairo',
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  color: Colors.green.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: Colors.greenAccent.withOpacity(0.3)),
                ),
                child: Text(
                  '${overallPercent.toStringAsFixed(1)}%',
                  style: const TextStyle(
                    color: Colors.greenAccent,
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
              ),
            ],
          ),

          const SizedBox(height: 14),

          ClipRRect(
            borderRadius: BorderRadius.circular(8),
            child: Container(
              height: 10,
              width: double.infinity,
              color: Colors.white.withOpacity(0.04),
              child: Stack(
                children: [
                  FractionallySizedBox(
                    alignment: Alignment.centerRight,
                    widthFactor: (overallPercent / 100.0).clamp(0.0, 1.0),
                    child: Container(
                      decoration: BoxDecoration(
                        gradient: LinearGradient(
                          colors: [gold, Colors.greenAccent],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(height: 20),

          const Text(
            'التراكم السنوي للساعات (تراكمي مجهّز):',
            style: TextStyle(
              color: Colors.white60,
              fontSize: 11,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          const SizedBox(height: 10),
          
          SizedBox(
            height: 160,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: List.generate(chartPoints.length, (idx) {
                final pt = chartPoints[idx];
                final String label = pt['label'] as String;
                final int target = pt['cumulativeTarget'] as int;
                final int completed = pt['cumulativeCompleted'] as int;
                
                final double targetHeightFactor = (target / 199.0).clamp(0.05, 1.0);
                final double completedHeightFactor = (completed / 199.0).clamp(0.0, 1.0);

                return Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 4.0),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: [
                        Expanded(
                          child: LayoutBuilder(
                            builder: (context, constraints) {
                              final maxHeight = constraints.maxHeight;
                              return Tooltip(
                                message: '$label\nتركم الساعات المخططة: $target س\nالتراكم المكتمل المنجز: $completed س',
                                triggerMode: TooltipTriggerMode.tap,
                                child: Stack(
                                  alignment: Alignment.bottomCenter,
                                  children: [
                                    Container(
                                      height: maxHeight * targetHeightFactor,
                                      width: 24,
                                      decoration: BoxDecoration(
                                        color: Colors.white.withOpacity(0.03),
                                        borderRadius: BorderRadius.circular(4),
                                        border: Border.all(color: Colors.white.withOpacity(0.05)),
                                      ),
                                    ),
                                    if (completed > 0)
                                      AnimatedContainer(
                                        duration: const Duration(milliseconds: 300),
                                        height: maxHeight * completedHeightFactor,
                                        width: 24,
                                        decoration: BoxDecoration(
                                          gradient: LinearGradient(
                                            begin: Alignment.bottomCenter,
                                            end: Alignment.topCenter,
                                            colors: [
                                              Colors.green.withOpacity(0.6),
                                              Colors.greenAccent,
                                            ],
                                          ),
                                          borderRadius: BorderRadius.circular(4),
                                          boxShadow: [
                                            BoxShadow(
                                              color: Colors.greenAccent.withOpacity(0.2),
                                              blurRadius: 4,
                                              offset: const Offset(0, -1),
                                            )
                                          ],
                                        ),
                                      ),
                                    Positioned(
                                      top: (maxHeight * (1 - targetHeightFactor) - 18).clamp(0.0, maxHeight),
                                      child: Text(
                                        '$target',
                                        style: TextStyle(
                                          color: gold,
                                          fontSize: 9,
                                          fontFamily: 'Cairo',
                                          fontWeight: FontWeight.bold,
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              );
                            },
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          label.replaceAll('السنة ', 'س '),
                          style: TextStyle(
                            color: slate.withOpacity(0.7),
                            fontSize: 9,
                            fontFamily: 'Cairo',
                          ),
                          textAlign: TextAlign.center,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                );
              }),
            ),
          ),

          const SizedBox(height: 20),
          const Divider(color: Colors.white15, height: 1),
          const SizedBox(height: 12),

          const Text(
            'توزيع فئات المقررات الأكاديمية (النسبة المئوية من المجموع الأكاديمي الحقيقي):',
            style: TextStyle(
              color: Colors.white60,
              fontSize: 10.5,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          const SizedBox(height: 10),
          
          ClipRRect(
            borderRadius: BorderRadius.circular(6),
            child: Container(
              height: 10,
              width: double.infinity,
              child: Row(
                children: [
                  if (catPercentages.containsKey('أساسية'))
                    Expanded(
                      flex: catPercentages['أساسية']!.round(),
                      child: Container(color: Colors.blueAccent),
                    ),
                  if (catPercentages.containsKey('مهارات'))
                    Expanded(
                      flex: catPercentages['مهارات']!.round(),
                      child: Container(color: Colors.tealAccent),
                    ),
                  if (catPercentages.containsKey('اختيارية'))
                    Expanded(
                      flex: catPercentages['اختيارية']!.round(),
                      child: Container(color: Colors.amberAccent),
                    ),
                  if (catPercentages.containsKey('احترافية'))
                    Expanded(
                      flex: catPercentages['احترافية']!.round(),
                      child: Container(color: Colors.redAccent),
                    ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),

          Wrap(
            alignment: WrapAlignment.spaceBetween,
            spacing: 12,
            runSpacing: 8,
            children: [
              _buildLegendItem('أساسية', '${catPercentages['أساسية']?.toStringAsFixed(1) ?? "0"}%', Colors.blueAccent),
              _buildLegendItem('مهارات', '${catPercentages['مهارات']?.toStringAsFixed(1) ?? "0"}%', Colors.tealAccent),
              _buildLegendItem('اختيارية', '${catPercentages['اختيارية']?.toStringAsFixed(1) ?? "0"}%', Colors.amberAccent),
              _buildLegendItem('احترافية', '${catPercentages['احترافية']?.toStringAsFixed(1) ?? "0"}%', Colors.redAccent),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem(String title, String percent, Color color) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 4),
        Text(
          '$title ($percent)',
          style: const TextStyle(
            color: Colors.white70,
            fontSize: 9.5,
            fontFamily: 'Cairo',
          ),
        ),
      ],
    );
  }

  Widget _buildYearProgressBar(Map<String, dynamic> yearData, Color gold) {
    final semesters = yearData['semesters'] as List<dynamic>;
    int total = 0;
    int completed = 0;

    for (var sem in semesters) {
      for (var c in sem['courses'] as List<dynamic>) {
        final h = c['hours'] as int;
        total += h;
        if (_completedCourseCodes.contains(c['code'] as String)) {
          completed += h;
        }
      }
    }

    final ratio = total > 0 ? (completed / total) : 0.0;
    final pct = (ratio * 100).toInt();

    return Column(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: Container(
            height: 4,
            width: double.infinity,
            color: Colors.white.withOpacity(0.08),
            child: Stack(
              children: [
                FractionallySizedBox(
                  alignment: Alignment.centerRight,
                  widthFactor: ratio,
                  child: Container(
                    color: ratio >= 1.0 ? Colors.greenAccent : gold,
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 2),
        Text(
          '%$pct',
          style: TextStyle(
            color: ratio >= 1.0 ? Colors.greenAccent : Colors.white60,
            fontSize: 9,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
