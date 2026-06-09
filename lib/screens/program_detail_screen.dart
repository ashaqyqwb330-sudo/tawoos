import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../data/programs_data.dart';
import '../data/programs_faq.dart';
import 'program_chat_screen.dart';

class ProgramDetailScreen extends StatefulWidget {
  final String programId;
  const ProgramDetailScreen({Key? key, required this.programId}) : super(key: key);

  @override
  State<ProgramDetailScreen> createState() => _ProgramDetailScreenState();
}

class _ProgramDetailScreenState extends State<ProgramDetailScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  ProgramInfo? _program;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
    _program = ProgramsData.getProgramById(widget.programId);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_program == null) {
      return Scaffold(
        backgroundColor: const Color(0xFF0D1B2A),
        body: Center(
          child: Text(
            'لم يتم العثور على البرنامج المعرف: ${widget.programId}',
            style: const TextStyle(color: Colors.redAccent, fontFamily: 'Cairo'),
          ),
        ),
      );
    }

    const Color darkNavy = Color(0xFF0D1B2A);
    const Color solidNavy = Color(0xFF1B263B);
    const Color goldenBrass = Color(0xFFC5A44E);
    const Color slateLight = Color(0xFFE0E1DD);

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: darkNavy,
        appBar: AppBar(
          backgroundColor: solidNavy,
          elevation: 4,
          iconTheme: const IconThemeData(color: goldenBrass),
          title: Text(
            _program!.title,
            style: const TextStyle(
              color: goldenBrass,
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          bottom: TabBar(
            controller: _tabController,
            isScrollable: true,
            indicatorColor: goldenBrass,
            labelColor: goldenBrass,
            unselectedLabelColor: Colors.white60,
            labelStyle: const TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 13,
              fontFamily: 'Cairo',
            ),
            tabs: const [
              Tab(text: 'نظرة عامة'),
              Tab(text: 'الأهداف والمخرجات'),
              Tab(text: 'الخطة الدراسية'),
              Tab(text: 'القبول والتساؤلات'),
            ],
          ),
        ),
        body: Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              colors: [solidNavy, darkNavy],
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
            ),
          ),
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildOverviewTab(goldenBrass, slateLight),
              _buildObjectivesTab(goldenBrass, slateLight),
              _buildCurriculumTab(goldenBrass, slateLight),
              _buildAdmissionTab(goldenBrass, slateLight),
            ],
          ),
        ).animate().fadeIn(duration: 500.ms).slideY(begin: 0.05, end: 0, duration: 500.ms),
        floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => ProgramChatScreen(programId: widget.programId),
              ),
            );
          },
          backgroundColor: goldenBrass,
          foregroundColor: Colors.black,
          elevation: 6,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          icon: const Icon(Icons.chat_bubble_outline),
          label: const Text(
            'مستشار البرنامج الذكي',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 13,
              fontFamily: 'Cairo',
            ),
          ),
        ).animate().scale(delay: 400.ms, duration: 400.ms, curve: Curves.easeOut),
      ),
    );
  }

  Widget _buildOverviewTab(Color gold, Color slate) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildCard(
            title: 'الوصف العام للبرنامج',
            content: _program!.description,
            goldColor: gold,
            slateColor: slate,
            icon: Icons.info_outline,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'تفاصيل مسار الدرجة والشعبة',
            content: "القسم الأكاديمي: ${_program!.department}\nالدرجة الممنوحة: ${_program!.degree}\nالمدة الزمنية للدراسة: ${_program!.duration}\nلغة الدراسة الرئيسية: ${_program!.language}",
            goldColor: gold,
            slateColor: slate,
            icon: Icons.school,
          ),
        ],
      ),
    );
  }

  Widget _buildObjectivesTab(Color gold, Color slate) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'الأهداف الإستراتيجية والأكاديمية للبرنامج',
            style: TextStyle(
              color: Color(0xFFC5A44E),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ).animate().fadeIn(delay: 100.ms),
          const SizedBox(height: 8),
          ..._program!.objectives.map((obj) => Padding(
            padding: const EdgeInsets.symmetric(vertical: 4.0),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(Icons.check_circle, color: gold, size: 20),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    obj,
                    style: TextStyle(color: slate, fontSize: 13, fontFamily: 'Cairo'),
                  ),
                ),
              ],
            ),
          )),
          const SizedBox(height: 24),
          const Text(
            'مخرجات التعلم المستهدفة (PLOs)',
            style: TextStyle(
              color: Color(0xFFC5A44E),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ).animate().fadeIn(delay: 200.ms),
          const SizedBox(height: 8),
          ..._program!.plos.map((plo) {
            final parts = plo.split(':');
            final tag = parts[0];
            final detail = parts.length > 1 ? parts[1] : '';
            return Container(
              margin: const EdgeInsets.symmetric(vertical: 6.0),
              padding: const EdgeInsets.all(12.0),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.02),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: Colors.white.withOpacity(0.05)),
              ),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                    decoration: BoxDecoration(
                      color: gold.withOpacity(0.15),
                      borderRadius: BorderRadius.circular(4),
                      border: Border.all(color: gold.withOpacity(0.4)),
                    ),
                    child: Text(
                      tag,
                      style: TextStyle(color: gold, fontSize: 11, fontWeight: FontWeight.bold),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Text(
                      detail,
                      style: TextStyle(color: slate, fontSize: 13, fontFamily: 'Cairo'),
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildCurriculumTab(Color gold, Color slate) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'الخطة الدراسية السنوية التكتيكية',
            style: TextStyle(
              color: Color(0xFFC5A44E),
              fontSize: 16,
              fontWeight: FontWeight.bold,
              fontFamily: 'Cairo',
            ),
          ),
          const SizedBox(height: 12),
          ..._program!.curriculum.map((term) {
            final termName = term['termName'] as String;
            final courses = term['courses'] as List<dynamic>;
            return Container(
              margin: const EdgeInsets.bottom(16),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.02),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.white.withOpacity(0.05)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    termName,
                    style: TextStyle(color: gold, fontWeight: FontWeight.bold, fontSize: 13, fontFamily: 'Cairo'),
                  ),
                  const Divider(color: Colors.white10),
                  ...courses.map((course) {
                    final code = course['code'] as String;
                    final name = course['name'] as String;
                    final hours = course['hours'] as int;
                    return ListTile(
                      contentPadding: EdgeInsets.zero,
                      dense: true,
                      leading: Icon(Icons.book, color: gold.withOpacity(0.5), size: 18),
                      title: Text(name, style: TextStyle(color: slate, fontSize: 12, fontFamily: 'Cairo')),
                      subtitle: Text(code, style: const TextStyle(color: Colors.white30, fontSize: 10)),
                      trailing: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.white12,
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: Text('$hours ساعات', style: TextStyle(color: slate, fontSize: 10, fontFamily: 'Cairo')),
                      ),
                    );
                  }),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildAdmissionTab(Color gold, Color slate) {
    final faqs = ProgramsFaq.getFaqByProgramId(widget.programId);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildCard(
            title: 'شروط وبنود القبول والتسجيل',
            content: _program!.admissionRequirements,
            goldColor: gold,
            slateColor: slate,
            icon: Icons.check_circle_outline,
          ),
          const SizedBox(height: 16),
          _buildCard(
            title: 'شروط ومتطلبات التخرج والشهادة',
            content: _program!.graduationRequirements,
            goldColor: gold,
            slateColor: slate,
            icon: Icons.military_tech_outlined,
          ),
          const SizedBox(height: 24),
          if (faqs.isNotEmpty) ...[
            Row(
              children: [
                Icon(Icons.question_answer_outlined, color: gold),
                const SizedBox(width: 8),
                const Text(
                  'الأسئلة الأكاديمية والتوجيهية',
                  style: TextStyle(
                    color: Color(0xFFC5A44E),
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ...faqs.map((faqItem) => Container(
              margin: const EdgeInsets.only(bottom: 12),
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.03),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.white.withOpacity(0.05)),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    faqItem['question'] ?? '',
                    style: TextStyle(
                      color: gold,
                      fontSize: 13,
                      fontWeight: FontWeight.bold,
                      fontFamily: 'Cairo',
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    faqItem['answer'] ?? '',
                    style: TextStyle(
                      color: slate.withOpacity(0.9),
                      fontSize: 12,
                      fontFamily: 'Cairo',
                      height: 1.5,
                    ),
                  ),
                ],
              ),
            )),
          ],
        ],
      ),
    );
  }

  Widget _buildCard({
    required String title,
    required String content,
    required Color goldColor,
    required Color slateColor,
    required IconData icon,
  }) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.03),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.08), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, color: goldColor, size: 22),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  title,
                  style: TextStyle(
                    color: goldColor,
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    fontFamily: 'Cairo',
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            content,
            style: TextStyle(
              color: slateColor,
              fontSize: 12.5,
              height: 1.6,
              fontFamily: 'Cairo',
            ),
          ),
        ],
      ),
    ).animate().fadeIn(duration: 400.ms).slideX(begin: 0.05, end: 0, duration: 400.ms);
  }
}
